/*
 * Copyright 2010 Brendan Kenny
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package gwt.ns.webworker.linker;

import gwt.ns.webworker.rebind.WorkerFactoryGenerator;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;


import com.google.gwt.core.ext.LinkerContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.linker.AbstractLinker;
import com.google.gwt.core.ext.linker.Artifact;
import com.google.gwt.core.ext.linker.ArtifactSet;
import com.google.gwt.core.ext.linker.CompilationResult;
import com.google.gwt.core.ext.linker.LinkerOrder;
import com.google.gwt.core.ext.linker.LinkerOrder.Order;
import com.google.gwt.core.ext.linker.SelectionProperty;
import com.google.gwt.core.ext.linker.StatementRanges;
import com.google.gwt.core.ext.linker.SymbolData;
import com.google.gwt.core.ext.linker.impl.StandardCompilationResult;
import com.google.gwt.dev.Permutation;
import com.google.gwt.dev.cfg.StaticPropertyOracle;
import com.google.gwt.dev.jjs.PermutationResult;
import com.google.gwt.dev.util.Util;

/**
 * Linker partner to {@link WorkerFactoryGenerator}. All Worker modules
 * requested via the artifact set are compiled and the actual paths to the
 * scripts, strongly named based on the contents of the scripts themselves, are
 * inserted into native-worker supporting permutations.
 * 
 * <p>
 * Note that this Linker can be invoked within a recursive compilation by a
 * Worker that needs to spawn a sub-Worker. In this case, the relative URL
 * contains no strong name (since it will share the Worker's directory) and the
 * request can be passed up to the original Linker, thus preventing repeated
 * compilations and unbounded recursion.
 * </p>
 */
@LinkerOrder(Order.PRE)
public class WorkerCompilationLinker extends AbstractLinker {

    @Override
    public String getDescription() {
        return "Worker Compiler";
    }

    @Override
    public ArtifactSet link(TreeLogger logger, LinkerContext context,
            ArtifactSet artifacts) throws UnableToCompleteException {

        ArtifactSet toReturn = new ArtifactSet(artifacts);

        // get set of requests for insideWorker compilations from artifacts
        SortedSet<WorkerRequestArtifact> workerRequests = toReturn
                .find(WorkerRequestArtifact.class);
        if (workerRequests.size() == 0) {
            logger.log(TreeLogger.SPAM,
                    "No Worker compilations requested. No action taken.");
            return toReturn; // early exit, sorry
        }

        // compile all requested workers
        // if this is a recursive call, requests were passed up to parent so
        // returned value is null
        SortedMap<WorkerRequestArtifact, String> workerScripts = WorkerCompiler
                .run(logger, workerRequests);

        // if they exist, deal with compiled scripts:
        if (workerScripts != null && workerScripts.size() != 0) {
            // directory strong name from all scripts
            byte[][] bytejs = Util.getBytes(workerScripts.values().toArray(
                    new String[0]));
            String scriptDirStrongName = Util.computeStrongName(bytejs);

            // emit worker scripts
            for (Map.Entry<WorkerRequestArtifact, String> script : workerScripts
                    .entrySet()) {
                WorkerRequestArtifact req = script.getKey();
                toReturn.add(emitString(logger, script.getValue(), req
                        .getRelativePath(scriptDirStrongName, File.separator)));
            }

            // get the set of current compilation results
            SortedSet<CompilationResult> compResults = toReturn
                    .find(CompilationResult.class);

            /*
             * Reading the js from and writing it to a new CompilationResult is
             * expensive (possibly disk cached), so read once and write only if
             * altered.
             */
            for (CompilationResult compRes : compResults) {
                // assume all need modification
                // TODO: rule out emulated permutations via properties
                toReturn.remove(compRes);

                CompilationResult altered = replacePlaceholder(logger, compRes,
                        WorkerRequestArtifact.getPlaceholderStrongName(),
                        scriptDirStrongName);
                toReturn.add(altered);
            }
        }

        return toReturn;
    }

    /**
     * Searches for all instances of a placeholder String in a
     * CompilationResult. If found, they are replaced as specified and a new
     * CompilationResult, with a newly generated strong name, is returned. If no
     * occurrences were found, the original CompilationResult is returned.
     * 
     * @param logger
     * @param result
     *            CompilationResult to process
     * @param placeholder
     *            String to be replaced
     * @param replacement
     *            String to insert in place of placeholder
     * 
     * @return A CompilationResult suitable for emitting
     */
    public CompilationResult replacePlaceholder(TreeLogger logger,
            final CompilationResult result, String placeholder,
            String replacement) {

        boolean needsMod = false;

        String[] js = result.getJavaScript();
        final StringBuffer[] jsbuf = new StringBuffer[js.length];
        for (int i = 0; i < jsbuf.length; i++) {
            jsbuf[i] = new StringBuffer(js[i]);
        }

        // search and replace in all fragments
        for (StringBuffer fragment : jsbuf) {
            needsMod |= replaceAll(fragment, placeholder, replacement);
        }

        // by default, returning unaltered result
        CompilationResult toReturn = result;

        // code has been altered, need to create new CompilationResult
        if (needsMod) {
            logger.log(TreeLogger.SPAM,
                    "Compilation permutation " + result.getPermutationId()
                            + " being modified.");

            // new js for compilation result
            final byte[][] newcode = new byte[jsbuf.length][];
            for (int i = 0; i < jsbuf.length; i++) {
                newcode[i] = Util.getBytes(jsbuf[i].toString());
            }

            // // new strong name
            // String strongName = Util.computeStrongName(newcode);

            // same symbolMap copied over since none altered
            // symbolMap a little more complicated
            // can only get deserialized version, need to reserialize
            // code from com.google.gwt.dev.jjs.JavaToJavaScriptCompiler
            // TODO: turns out this can get pretty bad. Workaround?
            SymbolData[] symbolMap = result.getSymbolMap();
            final byte[] serializedSymbolMap;
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                Util.writeObjectToStream(baos, (Object) symbolMap);
                serializedSymbolMap = baos.toByteArray();
            } catch (IOException e) {
                // should still never happen
                logger.log(TreeLogger.ERROR, "IOException while reserializing "
                        + "SymbolMap.");
                throw new RuntimeException(
                        "Should never happen with in-memory stream", e);
            }
            //
            // StandardCompilationResult altered =
            // new StandardCompilationResult(strongName, newcode,
            // serializedSymbolMap, result.getStatementRanges(),
            // result.getPermutationId());

            @SuppressWarnings("serial")
            PermutationResult permResult = new PermutationResult() {

                @Override
                public StatementRanges[] getStatementRanges() {
                    return result.getStatementRanges();
                }

                @Override
                public byte[] getSerializedSymbolMap() {
                    return serializedSymbolMap;
                }

                @Override
                public Permutation getPermutation() {
                    return new Permutation(result.getPermutationId(),
                            (StaticPropertyOracle) null);
                }

                @Override
                public byte[][] getJs() {
                    return newcode;
                }

                @Override
                public ArtifactSet getArtifacts() {
                    return null;
                }

                @Override
                public void addArtifacts(
                        Collection<? extends Artifact<?>> newArtifacts) {

                }
            };

            StandardCompilationResult altered = new StandardCompilationResult(
                    permResult);

            // need to copy permutation properties to new result
            for (Map<SelectionProperty, String> propertyMap : result
                    .getPropertyMap()) {
                altered.addSelectionPermutation(propertyMap);
            }

            toReturn = altered;

            logger.log(TreeLogger.SPAM,
                    "Compilation permuation " + toReturn.getPermutationId()
                            + " altered to include path to worker script(s).");
        }

        return toReturn;
    }

    /**
     * Searches StringBuffer for all occurrences of search and replaces them
     * with replace.
     * 
     * @param buf
     *            StringBuffer to search
     * @param search
     *            Substring to search for
     * @param replace
     *            Replacement String if search is found
     * 
     * @return True if substring search was found.
     */
    protected static boolean replaceAll(StringBuffer buf, String search,
            String replace) {
        int len = search.length();
        boolean searchFound = false;
        for (int pos = buf.indexOf(search); pos >= 0; pos = buf.indexOf(search,
                pos + 1)) {
            buf.replace(pos, pos + len, replace);
            searchFound = true;
        }

        return searchFound;
    }
}
