/*******************************************************************************
 * Copyright 2012 David Rusk, Elena Voyloshnikova 
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0 
 *     
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.  
 *******************************************************************************/
package org.thechiselgroup.biomixer.client.workbench.util.url;

import gwt.ns.json.client.Json;
import gwt.ns.webworker.client.MessageEvent;
import gwt.ns.webworker.client.MessageHandler;

import java.util.logging.Logger;

import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandlingAsyncCallback;
import org.thechiselgroup.biomixer.client.core.error_handling.LoggerProvider;
import org.thechiselgroup.biomixer.client.core.error_handling.RetryAsyncCallbackErrorHandler;
import org.thechiselgroup.biomixer.client.core.util.url.UrlFetchService;
import org.thechiselgroup.biomixer.client.json.JsJsonParser;
import org.thechiselgroup.biomixer.client.services.AbstractJsonResultParser;
import org.thechiselgroup.biomixer.shared.workbench.util.json.JsonParser;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.jsonp.client.JsonpRequestBuilder;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

/**
 * Retrieves the content of URLs using JSONP. Also logs profiles of the
 * retrieval process if the log flag is enabled.
 */
public class JsonpUrlFetchService implements UrlFetchService {

    protected final String ERROR_PREFIX = "Error code, status: ";

    // // Worker module definition
    // @WorkerModuleDef("org.thechiselgroup.biomixer.client.services.RestCallWorker")
    // interface RestCallWorkerFactory extends WorkerFactory {
    // }
    //
    // private final RestCallWorkerFactory factory;
    //
    // private final RestCallWorkerEntryPoint worker;

    LoggerProvider loggerProvider;

    private Logger logger;

    private String url;

    private long start;

    private boolean performProfileLogging = true;

    @Inject
    public JsonpUrlFetchService(LoggerProvider loggerProvider) {
        // public JsonpUrlFetchService() {

        // // Worker creation
        // factory = GWT.create(RestCallWorkerFactory.class);
        // // Won't *actually* start, in terms of useful computation. Just
        // stokes
        // // the engine.
        // worker = (RestCallWorkerEntryPoint) factory.createAndStart();
        //
        this.logger = loggerProvider.getLogger();
    }

    @Override
    public void fetchURL(final String url, final AsyncCallback<String> callback) {
        this.url = url;
        this.start = System.currentTimeMillis();
        if (this.performProfileLogging) {
            logger.info("fetch url '" + url + "'");
        }
        fetchURL(url, callback, 0);
    }

    MessageHandler callbackMessageHandler;

    private MessageHandler createMessageHandler(
            final AsyncCallback<String> callback) {
        return new MessageHandler() {
            @Override
            public void onMessage(MessageEvent event) {
                JavaScriptObject javaScriptObject = Json.parse(event.getData());
                if (event.getData().startsWith(ERROR_PREFIX)) {
                    // This wasn't a success, and we got an error code
                    // we don't understand.
                    // Treat as an error for the callback.
                    if (performProfileLogging) {
                        logger.info("fetch url '" + url + "' failed after "
                                + (System.currentTimeMillis() - start) + "ms");
                    }

                    callback.onFailure(new Exception(event.getData()));
                    return;
                } else {
                    if (performProfileLogging) {
                        logger.info("fetch url '" + url + "' failed after "
                                + (System.currentTimeMillis() - start) + "ms");
                    }
                    callback.onSuccess(javaScriptObject.toString());
                    return;
                }
            }
        };
    }

    /**
     * This is primarily used by the {@link RetryAsyncCallbackErrorHandler} that
     * this uses internally.
     * 
     * @param url
     * @param callback
     * @param previousNumberTries
     */
    public void fetchURL(final String url,
            final AsyncCallback<String> callback, final int previousNumberTries) {

        // TODO I think I want a web worker here, for waiting for the response.
        // I feel like I want it for the callback too, but I can't see yet if
        // that might be a different web worker, or the same one that waited.

        // TODO I think the callback needs to be the MessageHandler, or the
        // thing that made the callback...as a reference in the callback
        // perhaps. The MessageHandler is on the outside of the web worker,
        // and can therefore be in the 'main thread', or as close as is
        // convenient, and capable of manipulating the DOM.
        // I tis typical for the callback.onSuccess() to do some processing,
        // then call things close ot the DOM (such as calling methods on the
        // graph to add arcs and whatnot).
        // The first step should be to set the WebWorker.onMessage() to feed
        // into onSuccess.
        // Really, I need to have the call to onSuccess() replaced by a call to
        // onMessage(), which then calls onSuccess().
        // TODO So...all the rest of this stuff needs to be called in execute()
        // on the web worker?
        // final MessageHandler callbackMessageHandler =
        // createMessageHandler(callback);
        // // worker.setMessageHandler(callback.getWebWorkerMessageHandler());
        // worker.setMessageHandler(callbackMessageHandler);

        // TODO I hate this. We have so many layers of delegation and wrapping
        // that
        // it makes comprehension and modification difficult.
        // Can I push together parts of the request classes (callbacks,
        // fetchUrl(), web worker)?
        // WebWorkerCommand command = worker.new WebWorkerCommand() {
        //
        // @Override
        // public void command() {

        JsonpRequestBuilder jsonp = new JsonpRequestBuilder();
        // Could change timeout, but probably better to change retry
        // attempt
        // number...except that exacerbates server load. Maybe longer
        // timeout is
        // ok.
        jsonp.setTimeout(jsonp.getTimeout() * 4);

        final RetryAsyncCallbackErrorHandler retryHandler = new RetryAsyncCallbackErrorHandler(
                callback, url, previousNumberTries, JsonpUrlFetchService.this);

        ErrorHandlingAsyncCallback<JavaScriptObject> errorHandlingAsync = new ErrorHandlingAsyncCallback<JavaScriptObject>(
                retryHandler) {

            @Override
            protected void runOnSuccess(JavaScriptObject result)
                    throws Exception {
                // Had trouble with injection...explicitly creating
                // instead.
                ErrorCodeJSONParser errorCodeParser = new ErrorCodeJSONParser(
                        new JsJsonParser());

                JSONObject jsonObject = new JSONObject(result);
                String jsonString = jsonObject.toString();

                // Need to check for understood errors in response, such
                // as 403 forbidden.

                Integer errorCode = errorCodeParser.parse(jsonString);

                if (null != errorCode && 500 == errorCode) {
                    // 500 errors don't get here! Caught lower down?
                    // We can retry this once, since I have already seen
                    // cases of very singular failures here.
                    boolean retryAttempted = retryHandler.manualRetry();
                    if (retryAttempted) {
                        String errorMessage = "Error code, status: "
                                + errorCode + ".";
                        callback.onFailure(new Exception(errorMessage));
                        // worker.postResults(JsonUtils
                        // .safeEval(errorMessage));
                        return;
                    }
                    // else if (403 == errorCode) {
                    // // This error code, forbidden, is something I
                    // want
                    // // to ignore at the moment.
                    // return;
                    // }
                } else { // if (null == errorCode) {
                    // TODO I am pretty sure we need to pass to the
                    // callback.onSuccess() and callback.onFailure()
                    // using
                    // onMessage() from the web worker.
                    // The good work happens just after those two
                    // methods, and
                    // the web worker's work is done, after all.
                    // I can't seem to pass exceptions, since I need to
                    // pass a
                    // stringified json from postResults to
                    // postMessage()...
                    // TODO So how do I pass exceptions to the
                    // callback.onFailure()???
                    // TODO I think I need to combine the
                    // RestCallWorkerEntry
                    // and this UrlFetchService.
                    callback.onSuccess(jsonString);
                    // worker.postResults(result);
                    return;
                }

                // This wasn't a success, and we got an error code
                // we don't understand.
                // Treat as an error for the callback.
                String errorMessage = "Error code, status: " + errorCode + ".";
                callback.onFailure(new Exception(errorMessage));
                // worker.postResults(JsonUtils.safeEval(errorMessage));
                // throw new Exception("Status " + errorCode);

            }

        };

        jsonp.requestObject(url, errorHandlingAsync);

    }

    // };
    //
    // worker.execute(command);

    // }

    public boolean isLogging() {
        return performProfileLogging;
    }

    public void setLogging(boolean performProfileLogging) {
        this.performProfileLogging = performProfileLogging;
    }

    private class ErrorCodeJSONParser extends AbstractJsonResultParser {

        @Inject
        public ErrorCodeJSONParser(JsonParser jsonParser) {
            super(jsonParser);
        }

        @Override
        public Integer parse(String json) {
            Integer errorCode = null;
            // Grab possible error code in status...
            // Like:
            /*
             * __gwt_jsonp__.P241.onSuccess( { "status": 403, "body":
             * "{\"errorStatus\": {\
             * "accessedResource\":\"\\\/bioportal\\\/ontologies\\\/metrics\\\/45290\",\"accessDate\":\"2013-02-18
             * 10:38:06.202
             * PST\",\"shortMessage\":\"Forbidden\",\"longMessage\":\"This
             * ontology is either private or licensed. Please go to
             * http:\\\/\\\/
             * bioportal.bioontology.org\\\/ontologies\\\/1578?p=terms to get
             * access to the ontology.\",\"errorCode\":403}}" } );
             */
            try {
                errorCode = asInt(get(super.parse(json), "status"));
            } catch (Exception e) {
                // I may get an exception if there is no such "status" entry.
                // Skip it. We parse the response in a callback later, so we can
                // let things be here. All we want is the error code if it
                // exists.
            }

            return errorCode;
        }

    }
}
