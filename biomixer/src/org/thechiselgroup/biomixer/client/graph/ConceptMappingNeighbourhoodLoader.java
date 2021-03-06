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

import org.thechiselgroup.biomixer.client.Mapping;
import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.ResourceManager;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollections;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.biomixer.client.services.mapping.ConceptMappingServiceAsync;
import org.thechiselgroup.biomixer.client.visualization_component.graph.NodeExpansionCallback;
import org.thechiselgroup.biomixer.client.visualization_component.graph.ViewWithResourceManager;
import org.thechiselgroup.biomixer.client.visualization_component.graph.ViewWithResourceManager.SpecializedResourceManager;

import com.google.inject.Inject;

public class ConceptMappingNeighbourhoodLoader<T extends ViewWithResourceManager>
        extends AbstractConceptMappingNeighbourhoodExpander<T> {

    @Inject
    public ConceptMappingNeighbourhoodLoader(
            ConceptMappingServiceAsync mappingService,
            ResourceManager resourceManager, ErrorHandler errorHandler) {

        super(mappingService, resourceManager, errorHandler);
    }

    @Override
    protected void expandNeighbourhood(VisualItem visualItem,
            Resource resource, NodeExpansionCallback<T> callback,
            List<Resource> neighbourhood) {

        // I set it up so that the mapping resources mingle with the concept
        // resources in the D3 Matrix view. Is that ok?
        SpecializedResourceManager specificResourceManager = callback
                .getDisplay().getSpecificResourceManager();

        /*
         * Adds mappings for which both concept ends are contained in graph (if
         * they are not contained yet)
         */
        List<Resource> displayableMappings = new ArrayList<Resource>();
        for (Resource mapping : neighbourhood) {
            assert Mapping.isMapping(mapping);

            String sourceUri = Mapping.getSourceUri(mapping);
            String targetUri = Mapping.getTargetUri(mapping);

            if (specificResourceManager.containsResourceWithUri(sourceUri)
                    && specificResourceManager
                            .containsResourceWithUri(targetUri)
                    && !specificResourceManager.containsResourceWithUri(mapping
                            .getUri())) {

                displayableMappings.add(mapping);
            }
        }

        for (Resource mapping : displayableMappings) {
            specificResourceManager.addAutomaticResource(mapping);
        }

        callback.updateArcsForVisuaItems(LightweightCollections
                .toCollection(visualItem));
    }
}