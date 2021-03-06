/*******************************************************************************
 * Copyright (C) 2011 Lars Grammel 
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
import org.thechiselgroup.biomixer.client.services.term.ConceptNeighbourhoodServiceAsync;
import org.thechiselgroup.biomixer.client.visualization_component.graph.AbstractGraphNodeSingleResourceNeighbourhoodExpander;
import org.thechiselgroup.biomixer.client.visualization_component.graph.ResourceNeighbourhood;
import org.thechiselgroup.biomixer.client.visualization_component.graph.ViewWithResourceManager;

public abstract class AbstractConceptConceptNeighbourhoodExpander<T extends ViewWithResourceManager>
        extends AbstractGraphNodeSingleResourceNeighbourhoodExpander<T> {

    protected final ConceptNeighbourhoodServiceAsync conceptNeighbourhoodService;

    public AbstractConceptConceptNeighbourhoodExpander(
            ErrorHandler errorHandler, ResourceManager resourceManager,
            ConceptNeighbourhoodServiceAsync conceptNeighbourhoodService) {

        super(errorHandler, resourceManager);

        this.conceptNeighbourhoodService = conceptNeighbourhoodService;
    }

    @Override
    protected String getErrorMessageWhenNeighbourhoodloadingFails(
            Resource resource, String additionalMessage) {
        return "Could not expand all parent and child concepts for \""
                + resource.getValue(Concept.LABEL) + "\" "
                + getOntologyInfoForErrorMessage(resource) + additionalMessage;
    }

    @Override
    protected boolean isNeighbourhoodLoaded(VisualItem visualItem,
            Resource resource) {
        assert Concept.isConcept(resource);
        return resourceManager.containsAllReferencedResources(resource,
                Concept.CHILD_CONCEPTS)
                && resourceManager.containsAllReferencedResources(resource,
                        Concept.PARENT_CONCEPTS);
    }

    @Override
    protected void loadNeighbourhood(VisualItem visualItem, Resource resource,
            ErrorHandlingAsyncCallback<ResourceNeighbourhood> callback) {

        String ontologyAcronym = (String) resource
                .getValue(Concept.ONTOLOGY_ACRONYM);
        String conceptId = (String) resource.getValue(Concept.ID);

        conceptNeighbourhoodService.getNeighbourhood(ontologyAcronym,
                conceptId, callback, resource);
    }

    @Override
    protected List<Resource> reconstructNeighbourhood(VisualItem visualItem,
            Resource concept) {

        List<Resource> neighbourhood = new ArrayList<Resource>();

        neighbourhood.addAll(resourceManager.resolveResources(concept,
                Concept.PARENT_CONCEPTS));
        neighbourhood.addAll(resourceManager.resolveResources(concept,
                Concept.CHILD_CONCEPTS));
        neighbourhood.addAll(resourceManager.resolveResources(concept,
                Concept.PART_OF_CONCEPTS));
        neighbourhood.addAll(resourceManager.resolveResources(concept,
                Concept.HAS_PART_CONCEPTS));

        return neighbourhood;
    }

}