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
package org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.implementation.svg.nodes;

import org.thechiselgroup.biomixer.client.core.geometry.DefaultSizeDouble;
import org.thechiselgroup.biomixer.client.core.geometry.PointDouble;
import org.thechiselgroup.biomixer.client.core.geometry.SizeDouble;
import org.thechiselgroup.biomixer.client.core.util.collections.Identifiable;
import org.thechiselgroup.biomixer.client.core.util.event.ChooselEventHandler;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.implementation.AbstractRenderedNode;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.implementation.svg.IsSvg;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.GraphDisplay;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.Node;
import org.thechiselgroup.biomixer.shared.svg.Svg;
import org.thechiselgroup.biomixer.shared.svg.SvgElement;
import org.thechiselgroup.biomixer.shared.svg.text_renderer.TextSvgElement;

/**
 * The classic rendering of a node as a rectangle with text inside it.
 * 
 * @author drusk
 * 
 */
public class BoxedTextRenderedSvgNode extends AbstractRenderedNode implements
        Identifiable, IsSvg {

    private final ExpanderTabSvgComponent expanderTab;

    private final BoxedTextSvgComponent boxedText;

    private final SvgElement baseContainer;

    public BoxedTextRenderedSvgNode(Node node, SvgElement baseContainer,
            BoxedTextSvgComponent boxedText, ExpanderTabSvgComponent expanderTab) {
        super(node);
        this.baseContainer = baseContainer;
        baseContainer.appendChild(boxedText.asSvgElement());
        baseContainer.appendChild(expanderTab.asSvgElement());
        this.boxedText = boxedText;
        this.expanderTab = expanderTab;
    }

    @Override
    public SvgElement asSvgElement() {
        return baseContainer;
    }

    @Override
    public ChooselEventHandler getBodyEventHandler() {
        return ((TextSvgElement) boxedText.asSvgElement()).getEventListener();
    }

    @Override
    public PointDouble getExpanderPopupLocation() {
        return getLocation().plus(expanderTab.getLocation());
    }

    @Override
    /* FIXME: this is for testing only. Some other way of firing events? */
    public ChooselEventHandler getExpansionEventHandler() {
        return ((TextSvgElement) expanderTab.asSvgElement()).getEventListener();
    }

    @Override
    public String getId() {
        return getNode().getId();
    }

    @Override
    public double getLeftX() {
        return Double.parseDouble(baseContainer.getAttributeAsString(Svg.X));
    }

    /**
     * 
     * @return the coordinates of the top left corner of the node, using the
     *         base svg element's coordinate system
     */
    public PointDouble getLocation() {
        return new PointDouble(Double.parseDouble(baseContainer
                .getAttributeAsString(Svg.X)), Double.parseDouble(baseContainer
                .getAttributeAsString(Svg.Y)));
    }

    @Override
    public SizeDouble getSize() {
        return new DefaultSizeDouble(boxedText.getTotalWidth(),
                boxedText.getTotalHeight());
    }

    @Override
    public double getTopY() {
        return Double.parseDouble(baseContainer.getAttributeAsString(Svg.Y));
    }

    @Override
    public void setBackgroundColor(String color) {
        boxedText.setBackgroundColor(color);
        expanderTab.setBackgroundColor(color);
    }

    @Override
    public void setBodyEventHandler(ChooselEventHandler handler) {
        boxedText.setEventListener(handler);
    }

    @Override
    public void setBorderColor(String color) {
        boxedText.setBorderColor(color);
        expanderTab.setBorderColor(color);
    }

    @Override
    public void setExpansionEventHandler(ChooselEventHandler handler) {
        expanderTab.setEventListener(handler);
    }

    @Override
    public void setFontColor(String color) {
        boxedText.setFontColor(color);
    }

    @Override
    public void setFontWeight(String styleValue) {
        if (styleValue.equals(GraphDisplay.NODE_FONT_WEIGHT_NORMAL)) {
            boxedText.setFontWeight(Svg.NORMAL);
        } else if (styleValue.equals(GraphDisplay.NODE_FONT_WEIGHT_BOLD)) {
            boxedText.setFontWeight(Svg.BOLD);
        }
    }

    @Override
    public void setLeftX(double x) {
        baseContainer.setAttribute(Svg.X, x);
        updateConnectedArcs();
    }

    @Override
    public void setTopY(double y) {
        baseContainer.setAttribute(Svg.Y, y);
        updateConnectedArcs();
    }

}