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
package org.thechiselgroup.biomixer.client.graph;

import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.biomixer.client.visualization_component.graph.Graph;
import org.thechiselgroup.biomixer.client.visualization_component.graph.NodeExpander;
import org.thechiselgroup.biomixer.client.visualization_component.graph.NodeExpansionCallback;

import com.google.inject.Inject;

public class AutomaticConceptExpander implements NodeExpander<Graph> {

    private final ConceptMappingNeighbourhoodLoader conceptMappingNeighbourhoodLoader;

    private final ConceptConceptNeighbourhoodLoader conceptConceptNeighbourhoodLoader;

    @Inject
    public AutomaticConceptExpander(
            ConceptMappingNeighbourhoodLoader conceptMappingNeighbourhoodLoader,
            ConceptConceptNeighbourhoodLoader conceptConceptNeighbourhoodLoader) {

        this.conceptMappingNeighbourhoodLoader = conceptMappingNeighbourhoodLoader;
        this.conceptConceptNeighbourhoodLoader = conceptConceptNeighbourhoodLoader;
    }

    @Override
    public void expand(VisualItem item, NodeExpansionCallback<Graph> graph) {
        // Window.alert("Autoexpand concept");
        // TODO Test to ensure these do the work that I think we can use bulk
        // calls on
        // conceptMappingNeighbourhoodLoader.expand(item, graph);
        // conceptConceptNeighbourhoodLoader.expand(item, graph);
    }
}