/*******************************************************************************
 * Copyright 2012 Lars Grammel, Bo Fu 
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
package org.thechiselgroup.biomixer.client.embeds;

import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.util.animation.AnimationRunner;
import org.thechiselgroup.biomixer.client.core.util.animation.GwtAnimationRunner;
import org.thechiselgroup.biomixer.client.core.visualization.LeftViewTopBarExtension;
import org.thechiselgroup.biomixer.client.core.visualization.View;
import org.thechiselgroup.biomixer.client.dnd.resources.DropEnabledViewContentDisplay;
import org.thechiselgroup.biomixer.client.dnd.windows.ViewWindowContent;
import org.thechiselgroup.biomixer.client.visualization_component.graph.Graph;
import org.thechiselgroup.biomixer.client.visualization_component.graph.GraphLayoutSupport;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutAlgorithm;
import org.thechiselgroup.biomixer.client.workbench.ui.configuration.ViewWindowContentProducer;
import org.thechiselgroup.biomixer.shared.core.util.DelayedExecutor;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public abstract class AbstractTermGraphEmbedLoader implements TermEmbedLoader {

    @Named("embed")
    @Inject
    protected ViewWindowContentProducer viewContentProducer;

    @Inject
    protected DelayedExecutor executor;

    protected AnimationRunner animationRunner = new GwtAnimationRunner();

    private final String id;

    private final String label;

    public AbstractTermGraphEmbedLoader(String label, String id) {
        assert label != null;
        assert id != null;

        this.id = id;
        this.label = label;
    }

    @Override
    public final String getId() {
        return id;
    }

    @Override
    public final String getLabel() {
        return label;
    }

    protected abstract LayoutAlgorithm getLayoutAlgorithm(
            ErrorHandler errorHandler);

    protected abstract void loadData(String virtualOntologyId,
            String fullConceptId, View graphView, ErrorHandler errorHandler);

    @Override
    public final void loadView(String virtualOntologyId, String fullConceptId,
            IsWidget topBarWidget, AsyncCallback<IsWidget> callback) {

        View graphView = ((ViewWindowContent) viewContentProducer
                .createWindowContent(Graph.ID)).getView();

        // XXX likely to break when view content setup changes
        // get the error handler from the view content display
        // to show the errors in the view-specific error box (ListBox)
        DropEnabledViewContentDisplay cd1 = (DropEnabledViewContentDisplay) graphView
                .getModel().getViewContentDisplay();
        Graph graph = (Graph) cd1.getDelegate();
        ErrorHandler errorHandler = graph.getErrorHandler();

        graphView.addTopBarExtension(new LeftViewTopBarExtension(topBarWidget));

        // add a loading bar so the user knows the application is being loaded
        Image loadingMessage = new Image(GWT.getModuleBaseURL()
                + "images/ajax-loader-bar.gif");
        graphView
                .addTopBarExtension(new LeftViewTopBarExtension(loadingMessage));
        loadingMessage.getElement().setId("loadingMessage");

        graphView.init();
        setLayoutAlgorithm(graphView, getLayoutAlgorithm(errorHandler));
        callback.onSuccess(graphView);

        loadData(virtualOntologyId, fullConceptId, graphView, errorHandler);

    }

    private void setLayoutAlgorithm(View graphView,
            LayoutAlgorithm layoutAlgorithm) {
        graphView.adaptTo(GraphLayoutSupport.class).registerDefaultLayout(
                layoutAlgorithm);
    }
}
