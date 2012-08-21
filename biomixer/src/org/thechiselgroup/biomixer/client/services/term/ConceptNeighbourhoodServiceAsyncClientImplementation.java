/*******************************************************************************
 * Copyright 2009, 2010, 2012 Lars Grammel, David Rusk 
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
import org.thechiselgroup.biomixer.client.visualization_component.graph.ResourceNeighbourhood;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

/**
 * @author Lars Grammel
 * 
 * @see "http://www.bioontology.org/wiki/index.php/NCBO_REST_services#Term_services"
 */
public class ConceptNeighbourhoodServiceAsyncClientImplementation extends
        AbstractWebResourceService implements ConceptNeighbourhoodServiceAsync {

    private final FullTermResponseJsonParser responseParser;

    private OntologyNameServiceAsync ontologyNameService;

    @Inject
    public ConceptNeighbourhoodServiceAsyncClientImplementation(
            UrlFetchService urlFetchService,
            UrlBuilderFactory urlBuilderFactory,
            OntologyNameServiceAsync ontologyNameService,
            FullTermResponseJsonParser responseParser) {

        super(urlFetchService, urlBuilderFactory);

        assert responseParser != null;
        assert ontologyNameService != null;

        this.responseParser = responseParser;
        this.ontologyNameService = ontologyNameService;
    }

    private String buildUrl(String fullConceptId, String ontologyId) {
        return urlBuilderFactory.createUrlBuilder()
                .path("bioportal/virtual/ontology/" + ontologyId)
                .uriParameter("conceptid", fullConceptId).toString();
    }

    @Override
    public void getNeighbourhood(final String ontologyId,
            final String conceptId,
            final AsyncCallback<ResourceNeighbourhood> callback) {

        assert ontologyId != null;
        assert conceptId != null;

        final String url = buildUrl(conceptId, ontologyId);

        ontologyNameService.getOntologyName(ontologyId,
                new TimeoutErrorHandlingAsyncCallback<String>(
                        new AsyncCallbackErrorHandler(callback)) {

                    @Override
                    protected String getMessage(Throwable caught) {
                        return "Could not retrieve concept neighbourhood for concept "
                                + conceptId + " in ontology " + ontologyId;
                    }

                    @Override
                    public void runOnSuccess(final String ontologyName) {
                        fetchUrl(
                                callback,
                                url,
                                new Transformer<String, ResourceNeighbourhood>() {
                                    @Override
                                    public ResourceNeighbourhood transform(
                                            String responseText)
                                            throws Exception {

                                        ResourceNeighbourhood neighbourhood = responseParser
                                                .parseNeighbourhood(ontologyId,
                                                        responseText);

                                        for (Resource resource : neighbourhood
                                                .getResources()) {
                                            resource.putValue(
                                                    Concept.CONCEPT_ONTOLOGY_NAME,
                                                    ontologyName);
                                        }

                                        return neighbourhood;
                                    }
                                });
                    }

                });
    }

    @Override
    public void getResourceWithRelations(final String ontologyId,
            final String conceptId, final AsyncCallback<Resource> callback) {

        assert ontologyId != null;
        assert conceptId != null;

        final String url = buildUrl(conceptId, ontologyId);

        ontologyNameService.getOntologyName(ontologyId,
                new TimeoutErrorHandlingAsyncCallback<String>(
                        new AsyncCallbackErrorHandler(callback)) {

                    @Override
                    protected String getMessage(Throwable caught) {
                        return "Could not retrieve concept neighbourhood for concept "
                                + conceptId + " in ontology " + ontologyId;
                    }

                    @Override
                    public void runOnSuccess(final String ontologyName) {
                        fetchUrl(callback, url,
                                new Transformer<String, Resource>() {
                                    @Override
                                    public Resource transform(
                                            String responseText)
                                            throws Exception {
                                        Resource resource = responseParser
                                                .parseResource(ontologyId,
                                                        responseText);
                                        resource.putValue(
                                                Concept.CONCEPT_ONTOLOGY_NAME,
                                                ontologyName);
                                        return resource;
                                    }
                                });
                    }

                });
    }

}