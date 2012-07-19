/*******************************************************************************
 * Copyright 2012 Bo Fu
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

import java.util.List;

import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.ResourceManager;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.biomixer.client.visualization_component.graph.AbstractGraphNodeNeighbourhoodRemover;
import org.thechiselgroup.biomixer.client.visualization_component.graph.GraphNodeExpansionCallback;
import org.thechiselgroup.biomixer.client.visualization_component.graph.ResourceNeighbourhood;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class GraphNodeNeighbourhoodRemover extends
        AbstractGraphNodeNeighbourhoodRemover {

    public GraphNodeNeighbourhoodRemover(ErrorHandler errorHandler,
            ResourceManager resourceManager) {
        super(errorHandler, resourceManager);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected boolean isNeighbourhoodLoaded(VisualItem visualItem,
            Resource resource) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    protected List<Resource> reconstructNeighbourhood(VisualItem visualItem,
            Resource resource) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected void removeNeighbourhood(VisualItem visualItem,
            Resource resource, AsyncCallback<ResourceNeighbourhood> callback) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void removeNeighbourhood(VisualItem visualItem,
            Resource resource, GraphNodeExpansionCallback graph,
            List<Resource> neighbourhood) {
        // TODO Auto-generated method stub

    }

}
