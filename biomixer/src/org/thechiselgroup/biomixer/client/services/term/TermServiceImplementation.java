/*******************************************************************************
 * Copyright (C) 2011 Lars Grammel, Bo Fu, Eric Verbeek 
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
package org.thechiselgroup.biomixer.client.services.term;

import org.thechiselgroup.biomixer.client.Concept;
import org.thechiselgroup.biomixer.client.core.error_handling.AsyncCallbackErrorHandler;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.util.transform.Transformer;
import org.thechiselgroup.biomixer.client.core.util.url.UrlBuilderFactory;
import org.thechiselgroup.biomixer.client.core.util.url.UrlFetchService;
import org.thechiselgroup.biomixer.client.embeds.TimeoutErrorHandlingAsyncCallback;
import org.thechiselgroup.biomixer.client.services.AbstractWebResourceService;
import org.thechiselgroup.biomixer.client.services.ontology.OntologyNameServiceAsync;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class TermServiceImplementation extends AbstractWebResourceService
        implements TermServiceAsync {

    private final TermWithoutRelationshipsJsonParser responseParser;

    private OntologyNameServiceAsync ontologyNameService;

    @Inject
    public TermServiceImplementation(UrlFetchService urlFetchService,
            UrlBuilderFactory urlBuilderFactory,
            OntologyNameServiceAsync ontologyNameService,
            TermWithoutRelationshipsJsonParser responseParser) {

        super(urlFetchService, urlBuilderFactory);

        this.responseParser = responseParser;
        this.ontologyNameService = ontologyNameService;
    }

    protected String buildUrl(String ontologyId, String conceptFullId) {
        return urlBuilderFactory.createUrlBuilder()
                .path("bioportal/virtual/ontology/" + ontologyId)
                .parameter("light", "1").parameter("norelations", "1")
                .uriParameter("conceptid", conceptFullId).toString();
    }

    @Override
    public void getBasicInformation(final String ontologyId,
            String conceptFullId, final AsyncCallback<Resource> callback) {

        assert ontologyId != null;
        assert conceptFullId != null;
        assert callback != null;

        final String url = buildUrl(ontologyId, conceptFullId);

        ontologyNameService.getOntologyName(ontologyId,
                new TimeoutErrorHandlingAsyncCallback<String>(
                        new AsyncCallbackErrorHandler(callback)) {

                    @Override
                    protected String getMessage(Throwable caught) {
                        return "Could not retrieve ontology name for virtual ontology id: "
                                + ontologyId;
                    }

                    @Override
                    public void runOnSuccess(final String ontologyName) {
                        fetchUrl(callback, url,
                                new Transformer<String, Resource>() {
                                    @Override
                                    public Resource transform(String value)
                                            throws Exception {
                                        Resource resource = responseParser
                                                .parseConcept(ontologyId, value);
                                        resource.putValue(
                                                Concept.CONCEPT_ONTOLOGY_NAME,
                                                ontologyName);
                                        return resource;
                                    }
                                });
                    }

                });
    }

    public void getBasicInformationNoOntologyName(final String ontologyId,
            String conceptFullId, final AsyncCallback<Resource> callback) {

        assert ontologyId != null;
        assert conceptFullId != null;
        assert callback != null;

        final String url = buildUrl(ontologyId, conceptFullId);

        fetchUrl(callback, url, new Transformer<String, Resource>() {

            @Override
            public Resource transform(String value) throws Exception {
                Resource resource = responseParser.parseConcept(ontologyId,
                        value);
                return resource;

            }
        });
    }
}