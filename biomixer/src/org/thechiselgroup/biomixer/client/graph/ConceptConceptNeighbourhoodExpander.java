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

import java.util.ArrayList;
import java.util.List;

import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.ResourceManager;
import org.thechiselgroup.biomixer.client.core.ui.dialog.DialogExitCallback;
import org.thechiselgroup.biomixer.client.core.ui.dialog.DialogManager;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollections;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.biomixer.client.embeds.NeighbourCapBreachDialog;
import org.thechiselgroup.biomixer.client.embeds.TermNeighbourhoodLoader;
import org.thechiselgroup.biomixer.client.services.term.ConceptNeighbourhoodServiceAsync;
import org.thechiselgroup.biomixer.client.visualization_component.graph.NodeExpansionCallback;
import org.thechiselgroup.biomixer.client.visualization_component.graph.ViewWithResourceManager;

import com.google.inject.Inject;

public class ConceptConceptNeighbourhoodExpander<T extends ViewWithResourceManager>
        extends AbstractConceptConceptNeighbourhoodExpander<T> {

    @Inject
    protected DialogManager dialogManager;

    private NeighbourCapBreachDialog neighbourBreachDialog;

    private List<Resource> neighbourhood;

    private NodeExpansionCallback<T> callback;

    private VisualItem visualItem;

    final private DialogExitCallback dialogExitCallback = new DialogExitCallback() {
        @Override
        public void dialogExited() {
            setGraphViewResources(neighbourBreachDialog.getExitCode() == NeighbourCapBreachDialog.OK_WITH_CAP_EXIT_CODE);
        }
    };

    @Inject
    public ConceptConceptNeighbourhoodExpander(ErrorHandler errorHandler,
            ResourceManager resourceManager,
            ConceptNeighbourhoodServiceAsync conceptNeighbourhoodService,
            DialogManager dialogManager) {

        super(errorHandler, resourceManager, conceptNeighbourhoodService);
        this.dialogManager = dialogManager;
    }

    @Override
    protected void expandNeighbourhood(VisualItem visualItem,
            Resource resource, NodeExpansionCallback<T> callback,
            List<Resource> neighbourhood) {
        // Set members so we can easily use the callback.
        this.neighbourhood = neighbourhood;
        this.callback = callback;
        this.visualItem = visualItem;

        if (neighbourhood.size() > TermNeighbourhoodLoader.MAX_NUMBER_OF_NEIGHBOURS) {
            // Callback will perform setGraphViewResources() for us.
            // setGraphViewResources(true);
            userPromptForNeighbourCap(neighbourhood.size(),
                    TermNeighbourhoodLoader.MAX_NUMBER_OF_NEIGHBOURS);
        } else {
            setGraphViewResources(false);
        }
    }

    private void setGraphViewResources(boolean capNodes) {
        List<Resource> setToRender = neighbourhood;
        if (capNodes) {
            setToRender = updateRenderedNeighboursWithMaximumNumber(neighbourhood);
        } else {
            setToRender = neighbourhood;
        }
        // TODO Why don't we use the following line as we saw in
        // TermNeighbourhoodLoader, or this graph approach in that place? We use
        // two different approaches...
        // graphView.getResourceModel().addResourceSet(setToRender);
        for (Resource neighbour : setToRender) {
            if (!callback.containsResourceWithUri(neighbour.getUri())) {
                callback.addAutomaticResource(neighbour);
            }
        }
        callback.updateArcsForVisuaItems(LightweightCollections
                .toCollection(visualItem));
    }

    /**
     * This asks the user if they would like to limit the number of rendered
     * results. Since we cannot have synchronous things in GWT (JS is single
     * threaded), we are stuck using callbacks, ultimately.
     * 
     * @param setToRender
     * @return
     */
    private void userPromptForNeighbourCap(int numResources, int maxDefault) {
        neighbourBreachDialog = new NeighbourCapBreachDialog(numResources,
                maxDefault);
        neighbourBreachDialog.setDialogExitCallback(dialogExitCallback);
        dialogManager.show(neighbourBreachDialog);
    }

    /**
     * Updates the graph view with the neighborhood, with a maximum number of
     * neighbors to enhance performance with large neighborhoods.
     * 
     * @return
     * 
     */
    private List<Resource> updateRenderedNeighboursWithMaximumNumber(
            List<Resource> fullNeighbourhood) {
        long i = 0;
        List<Resource> cappedSet = new ArrayList<Resource>(
                TermNeighbourhoodLoader.MAX_NUMBER_OF_NEIGHBOURS);

        for (Resource res : fullNeighbourhood) {
            cappedSet.add(res);
            i++;
            // Put break here, so we have a minimum of 1, in case max is 0 for
            // some reason.
            if (i >= TermNeighbourhoodLoader.MAX_NUMBER_OF_NEIGHBOURS) {
                break;
            }
        }
        return cappedSet;
    }
}