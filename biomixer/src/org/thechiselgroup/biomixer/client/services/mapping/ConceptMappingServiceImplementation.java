/*******************************************************************************
 * Copyright (C) 2011 Lars Grammel, Bo Fu
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
package org.thechiselgroup.biomixer.client.services.mapping;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.thechiselgroup.biomixer.client.Concept;
import org.thechiselgroup.biomixer.client.Mapping;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.UriList;
import org.thechiselgroup.biomixer.client.core.util.UriUtils;
import org.thechiselgroup.biomixer.client.core.util.callbacks.TransformingAsyncCallback;
import org.thechiselgroup.biomixer.client.core.util.collections.CollectionFactory;
import org.thechiselgroup.biomixer.client.core.util.transform.Transformer;
import org.thechiselgroup.biomixer.client.core.util.url.UrlBuilderFactory;
import org.thechiselgroup.biomixer.client.core.util.url.UrlFetchService;
import org.thechiselgroup.biomixer.client.services.Fetch;
import org.thechiselgroup.biomixer.client.visualization_component.graph.ResourceNeighbourhood;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.inject.Inject;

/**
 * Gets mappings from the NCBO REST services.
 * 
 * @author Lars Grammel
 * 
 * @see "http://www.bioontology.org/wiki/index.php/BioPortal_Mappings_Service"
 */
public class ConceptMappingServiceImplementation implements
        ConceptMappingServiceAsync {

    private final UrlFetchService urlFetchService;

    private final MappingResponseJsonParser responseParser;

    private final UrlBuilderFactory urlBuilderFactory;

    @Inject
    public ConceptMappingServiceImplementation(
            MappingResponseJsonParser responseParser,
            UrlFetchService urlFetchService, UrlBuilderFactory urlBuilderFactory) {

        this.urlFetchService = urlFetchService;
        this.responseParser = responseParser;
        this.urlBuilderFactory = urlBuilderFactory;
    }

    protected String buildUrl(String ontologyAcronym, String conceptId) {
        // return urlBuilderFactory.createUrlBuilder()
        // .path("bioportal/virtual/mappings/concepts/" + ontologyId)
        // .uriParameter("conceptid", conceptId).toString();
        return urlBuilderFactory
                .createUrlBuilder()
                .path("/ontologies/" + ontologyAcronym + "/classes/"
                        + UriUtils.encodeURIComponent(conceptId) + "/mappings/")
                .toString();
    }

    private Map<String, Serializable> calculatePartialProperties(
            String conceptUri, List<Resource> mappings) {
        UriList outgoingMappings = new UriList();
        UriList incomingMappings = new UriList();
        for (Resource mapping : mappings) {
            String sourceUri = Mapping.getSourceUri(mapping);
            String targetUri = Mapping.getTargetUri(mapping);

            if (!(conceptUri.equals(sourceUri) || conceptUri.equals(targetUri))) {
                continue;
            }

            assert !(conceptUri.equals(sourceUri) && conceptUri
                    .equals(targetUri)) : "'" + conceptUri
                    + "' matches both source and target uri";

            if (sourceUri.equals(conceptUri)) {
                outgoingMappings.add(mapping.getUri());
            } else {
                assert targetUri.equals(conceptUri);
                incomingMappings.add(mapping.getUri());
            }
        }
        Map<String, Serializable> partialProperties = CollectionFactory
                .createStringMap();

        partialProperties.put(Concept.INCOMING_MAPPINGS, incomingMappings);
        partialProperties.put(Concept.OUTGOING_MAPPINGS, outgoingMappings);
        return partialProperties;
    }

    @Override
    public void getMappings(final String ontologyAcronym,
            final String conceptId, final boolean mappingNeighbourhood,
            final AsyncCallback<ResourceNeighbourhood> callback) {

        assert ontologyAcronym != null;
        assert conceptId != null;
        assert callback != null;

        // TODO move code into parser
        Transformer<String, ResourceNeighbourhood> transformer = new Transformer<String, ResourceNeighbourhood>() {
            @Override
            public ResourceNeighbourhood transform(String value)
                    throws Exception {
                List<Resource> mappings = responseParser
                        .parseForConceptMapping(value);

                if (mappingNeighbourhood == true && mappings.size() == 0) {
                    // display a message to the user
                    com.google.gwt.user.client.Window
                            .alert("This concept does not have any mappings.");
                    // hide the loading bar
                    RootPanel.get("loadingMessage").setVisible(false);
                }

                Map<String, Serializable> partialProperties = calculatePartialProperties(
                        Concept.toConceptURI(ontologyAcronym, conceptId), mappings);

                return new ResourceNeighbourhood(partialProperties, mappings);
            }
        };
        String url = buildUrl(ontologyAcronym, conceptId);
        Fetch fetch = new Fetch(url);
        urlFetchService.fetchURL(url,
                TransformingAsyncCallback.create(callback, transformer, fetch));
    }
}