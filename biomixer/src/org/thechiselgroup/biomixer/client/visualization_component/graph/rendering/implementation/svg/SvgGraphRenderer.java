/*******************************************************************************
 * Copyright 2012 David Rusk 
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
package org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.implementation.svg;

import org.thechiselgroup.biomixer.client.core.util.event.ChooselEventHandler;
import org.thechiselgroup.biomixer.client.svg.javascript_renderer.ScrollableSvgWidget;
import org.thechiselgroup.biomixer.client.svg.javascript_renderer.SvgWidget;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.ArcRenderer;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.NodeExpanderRenderer;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.NodeRenderer;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.RenderedArc;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.RenderedNode;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.RenderedNodeExpander;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.implementation.AbstractGraphRenderer;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.implementation.svg.arcs.AbstractSvgRenderedArc;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.implementation.svg.expanders.AbstractSvgRenderedNodeExpander;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.implementation.svg.nodes.AbstractSvgRenderedNode;
import org.thechiselgroup.biomixer.shared.svg.Svg;
import org.thechiselgroup.biomixer.shared.svg.SvgElement;
import org.thechiselgroup.biomixer.shared.svg.SvgElementFactory;

import com.google.gwt.user.client.ui.Widget;

/**
 * Manages low level details of the SVG graph rendering.
 * 
 * @author drusk
 * 
 */
public class SvgGraphRenderer extends AbstractGraphRenderer {

    private SvgWidget svgWidget = null;

    private ScrollableSvgWidget asScrollingWidget = null;

    private CompositeSvgComponent rootSvgComponent = null;

    private CompositeSvgComponent arcGroup;

    private CompositeSvgComponent nodeGroup;

    private CompositeSvgComponent popupGroup;

    private SvgGraphBackground background;

    private SvgElementFactory svgElementFactory;

    private ChooselEventHandler viewWideInteractionHandler;

    public SvgGraphRenderer(int width, int height,
            SvgElementFactory svgElementFactory, NodeRenderer nodeRenderer,
            ArcRenderer arcRenderer, NodeExpanderRenderer nodeExpanderRenderer) {
        super(nodeRenderer, arcRenderer, nodeExpanderRenderer);
        this.graphWidth = width;
        this.graphHeight = height;
        this.svgElementFactory = svgElementFactory;
        initRootSvgElement();
        initBackground(width, height);
        initSvgGroupings();
    }

    @Override
    protected void addArcToGraph(RenderedArc arc) {
        arcGroup.appendChild((AbstractSvgRenderedArc) arc);
    }

    @Override
    protected void addNodeExpanderToGraph(RenderedNodeExpander expander) {
        popupGroup.appendChild((AbstractSvgRenderedNodeExpander) expander);
    }

    @Override
    protected void addNodeToGraph(RenderedNode node) {
        nodeGroup.appendChild((AbstractSvgRenderedNode) node);
    }

    /**
     * For testing.
     * 
     * @return
     */
    public SvgElement asSvg() {
        return rootSvgComponent.asSvgElement();
    }

    @Override
    public void bringToForeground(RenderedNode node) {
        nodeGroup.appendChild((AbstractSvgRenderedNode) node);
    }

    @Override
    public void checkIfScrollbarsNeeded() {
        asScrollingWidget.checkIfScrollbarsNeeded();
    }

    private CompositeSvgComponent createCompositeGroupingComponent(String id) {
        SvgElement groupingElement = svgElementFactory.createElement(Svg.G);
        groupingElement.setAttribute(Svg.ID, id);
        return new CompositeSvgComponent(groupingElement);
    }

    @Override
    public Widget getGraphWidget() {
        if (!isWidgetInitialized()) {
            svgWidget = new SvgWidget();
            rootSvgComponent = new CompositeSvgComponent(
                    svgWidget.getSvgElement(), rootSvgComponent,
                    viewWideInteractionHandler);
            asScrollingWidget = new ScrollableSvgWidget(svgWidget, graphWidth,
                    graphHeight);
            asScrollingWidget.setTextUnselectable();
            asScrollingWidget.getElement().getStyle()
                    .setBackgroundColor("white");
        }
        return asScrollingWidget;
    }

    private void initBackground(int width, int height) {
        background = new SvgGraphBackground(width, height, svgElementFactory);
        rootSvgComponent.appendChild(background);
    }

    private void initRootSvgElement() {
        SvgElement root = svgElementFactory.createElement(Svg.SVG);
        root.setAttribute("xmlns", Svg.NAMESPACE);
        root.setAttribute("version", "1.1");
        root.setAttribute(Svg.WIDTH, graphWidth);
        root.setAttribute(Svg.HEIGHT, graphHeight);
        rootSvgComponent = new CompositeSvgComponent(root);
    }

    private void initSvgGroupings() {
        arcGroup = createCompositeGroupingComponent("arcGroup");
        nodeGroup = createCompositeGroupingComponent("nodeGroup");
        popupGroup = createCompositeGroupingComponent("popupGroup");
        // order is important here - want arcs behind nodes and popups
        rootSvgComponent.appendChild(arcGroup);
        rootSvgComponent.appendChild(nodeGroup);
        rootSvgComponent.appendChild(popupGroup);
    }

    @Override
    public boolean isWidgetInitialized() {
        return svgWidget != null;
    }

    @Override
    protected void removeArcFromGraph(RenderedArc arc) {
        arcGroup.removeChild((AbstractSvgRenderedArc) arc);
    }

    @Override
    protected void removeNodeExpanderFromGraph(RenderedNodeExpander expander) {
        popupGroup.removeChild((AbstractSvgRenderedNodeExpander) expander);
    }

    @Override
    protected void removeNodeFromGraph(RenderedNode node) {
        nodeGroup.removeChild((AbstractSvgRenderedNode) node);
    }

    @Override
    public void setBackgroundEventListener(ChooselEventHandler handler) {
        background.setEventListener(handler);
    }

    @Override
    public void setGraphHeight(int height) {
        super.setGraphHeight(height);
        rootSvgComponent.asSvgElement().setAttribute(Svg.HEIGHT, height);
        background.setHeight(height);
        asScrollingWidget.setScrollableContentHeight(height);
    }

    @Override
    public void setGraphWidth(int width) {
        super.setGraphWidth(width);
        rootSvgComponent.asSvgElement().setAttribute(Svg.WIDTH, width);
        background.setWidth(width);
        asScrollingWidget.setScrollableContentWidth(width);
    }

    @Override
    public void setViewWideInteractionHandler(ChooselEventHandler handler) {
        this.viewWideInteractionHandler = handler;
        rootSvgComponent.setEventListener(handler);
    }

}
