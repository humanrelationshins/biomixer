/*******************************************************************************
 * Copyright 2009, 2010 Lars Grammel 
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
package org.thechiselgroup.biomixer.client.services.search.ontology;

import java.util.Collection;
import java.util.Set;

import org.thechiselgroup.biomixer.client.Ontology;
import org.thechiselgroup.biomixer.client.AbstractSearchCallbackFactory;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.util.transform.Transformer;
import org.thechiselgroup.biomixer.client.core.util.url.UrlBuilderFactory;
import org.thechiselgroup.biomixer.client.core.util.url.UrlFetchService;
import org.thechiselgroup.biomixer.client.services.AbstractWebResourceService;
import org.thechiselgroup.biomixer.client.services.Fetch;

import com.google.inject.Inject;

public class OntologySearchServiceAsyncClientImplementation extends
        AbstractWebResourceService implements OntologySearchServiceAsync {

    private final OntologySearchResultJsonParser resultParser;

    @Inject
    public OntologySearchServiceAsyncClientImplementation(
            UrlFetchService urlFetchService,
            UrlBuilderFactory urlBuilderFactory,
            OntologySearchResultJsonParser resultParser) {

        super(urlFetchService, urlBuilderFactory);

        this.resultParser = resultParser;
    }

    private String buildUrl(String queryText) {
        // This is perfect for the new API.
        // http://stagedata.bioontology.org/ontologies?apikey=YourAPIKey
        // provides all ontologies.
        // We can do client side filtering on the returned ontology names,
        // rather than requesting a REST service that does so.
        // If we decide to, we can later request a REST service that filters the
        // results.
        return urlBuilderFactory.createUrlBuilder().path("/ontologies/")
                .toString();
    }

    @Override
    public void searchOntologies(final String queryText,
            final AbstractSearchCallbackFactory callbackFactory) {
        final String url = buildUrl(queryText);
        fetchUrl(callbackFactory.createSearchCallback(new Fetch(url)), url,
                new Transformer<String, Set<Resource>>() {
                    @Override
                    public Set<Resource> transform(String responseText)
                            throws Exception {
                        resultParser.setFilterPropertyAndContainedText(
                                Ontology.ONTOLOGY_FULL_NAME, queryText);
                        return resultParser
                                .parseOntologySearchResults(responseText);
                    }
                });
    }

    @Override
    public void searchOntologiesPredeterminedSet(
            final Collection<String> ontologyAcronyms,
            final AbstractSearchCallbackFactory callbackFactory) {

        String url = buildUrl("");

        fetchUrl(callbackFactory.createSearchCallback(new Fetch(url)), url,
                new Transformer<String, Set<Resource>>() {
                    @Override
                    public Set<Resource> transform(String responseText)
                            throws Exception {
                        // resultParser.setFilterPropertyAndContainedText(
                        // Ontology.VIRTUAL_ONTOLOGY_ID, virtualOntologyIds);
                        resultParser.setFilterPropertyAndContainedText(
                                Ontology.ONTOLOGY_ACRONYM, ontologyAcronyms);
                        return resultParser
                                .parseOntologySearchResults(responseText);
                    }
                });

    }
}
