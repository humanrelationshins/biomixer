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
package org.thechiselgroup.biomixer.client.visualization_component.graph;

import java.util.List;

import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.ResourceManager;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.biomixer.client.visualization_component.graph.svg_widget.GraphDisplayController;

import com.google.gwt.user.client.rpc.AsyncCallback;

public abstract class AbstractGraphNodeNeighbourhoodRemover implements
        GraphNodeRemover {

    protected final GraphDisplayController controller;

    protected final ErrorHandler errorHandler;

    protected final ResourceManager resourceManager;

    protected final Resource getSingleResource(VisualItem visualItem) {
        assert visualItem.getResources().size() == 1;
        return visualItem.getResources().getFirstElement();
    }

    protected abstract boolean isNeighbourhoodLoaded(VisualItem visualItem,
            Resource resource);

    protected abstract List<Resource> reconstructNeighbourhood(
            VisualItem visualItem, Resource resource);

    @Override
    public final void remove(VisualItem visualItem,
            GraphNodeExpansionCallback graph) {

        assert visualItem != null;
        assert graph != null;

        Resource resource = getSingleResource(visualItem);

        if (isNeighbourhoodLoaded(visualItem, resource)) {
            removeNeighbourhood(visualItem, resource, graph,
                    reconstructNeighbourhood(visualItem, resource));
        } else {
            removeNeighbourhood(visualItem, resource, graph);
        }
    }

    protected abstract void removeNeighbourhood(VisualItem visualItem,
            Resource resource, AsyncCallback<ResourceNeighbourhood> callback);

    private void removeNeighbourhood(final VisualItem visualItem,
            final Resource resource, final GraphNodeExpansionCallback graph) {

        String type = resource.getTypeFromURI(resource.getUri());

        if (type != null && type.equalsIgnoreCase("arc")) {
            // to-do: switch from Resource" to Arc
            // controller.removeArc(arc);

        } else if (type != null && type.equalsIgnoreCase("node")) {
            // to-do: swtich from Resource to Node
            // controller.removeNode(node);
        }
    }

    protected abstract void removeNeighbourhood(VisualItem visualItem,
            Resource resource, GraphNodeExpansionCallback graph,
            List<Resource> neighbourhood);

}
