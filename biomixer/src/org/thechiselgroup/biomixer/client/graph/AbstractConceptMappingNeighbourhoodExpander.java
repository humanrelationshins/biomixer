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
package org.thechiselgroup.biomixer.client.graph;

import java.util.ArrayList;
import java.util.List;

import org.thechiselgroup.biomixer.client.Concept;
import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandlingAsyncCallback;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.ResourceManager;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.biomixer.client.services.mapping.ConceptMappingServiceAsync;
import org.thechiselgroup.biomixer.client.visualization_component.graph.AbstractGraphNodeSingleResourceNeighbourhoodExpander;
import org.thechiselgroup.biomixer.client.visualization_component.graph.ResourceNeighbourhood;
import org.thechiselgroup.biomixer.client.visualization_component.graph.ViewWithResourceManager;

public abstract class AbstractConceptMappingNeighbourhoodExpander<T extends ViewWithResourceManager>
        extends AbstractGraphNodeSingleResourceNeighbourhoodExpander<T> {

    private final ConceptMappingServiceAsync mappingService;

    protected AbstractConceptMappingNeighbourhoodExpander(
            ConceptMappingServiceAsync mappingService,
            ResourceManager resourceManager, ErrorHandler errorHandler) {

        super(errorHandler, resourceManager);

        this.mappingService = mappingService;
    }

    @Override
    protected String getErrorMessageWhenNeighbourhoodloadingFails(
            Resource resource, String additionalMessage) {
        return "Could not expand all mappings for \""
                + resource.getValue(Concept.LABEL) + "\" "
                + getOntologyInfoForErrorMessage(resource) + additionalMessage;
    }

    @Override
    protected boolean isNeighbourhoodLoaded(VisualItem visualItem,
            Resource resource) {
        assert Concept.isConcept(resource);
        return resourceManager.containsAllReferencedResources(resource,
                Concept.INCOMING_MAPPINGS)
                && resourceManager.containsAllReferencedResources(resource,
                        Concept.OUTGOING_MAPPINGS);
    }

    @Override
    protected void loadNeighbourhood(VisualItem visualItem, Resource resource,
            ErrorHandlingAsyncCallback<ResourceNeighbourhood> callback) {

        String ontologyAcronym = (String) resource
                .getValue(Concept.ONTOLOGY_ACRONYM);
        String conceptId = (String) resource.getValue(Concept.ID);
        mappingService.getMappings(ontologyAcronym, conceptId, false, callback);
    }

    @Override
    protected List<Resource> reconstructNeighbourhood(VisualItem visualItem,
            final Resource concept) {

        List<Resource> mappings = new ArrayList<Resource>();

        mappings.addAll(resourceManager.resolveResources(concept,
                Concept.INCOMING_MAPPINGS));
        mappings.addAll(resourceManager.resolveResources(concept,
                Concept.OUTGOING_MAPPINGS));

        return mappings;
    }

}