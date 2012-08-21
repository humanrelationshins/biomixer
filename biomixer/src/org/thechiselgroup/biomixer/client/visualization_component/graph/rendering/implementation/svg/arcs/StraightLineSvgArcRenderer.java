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
package org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.implementation.svg.arcs;

import org.thechiselgroup.biomixer.client.core.geometry.PointDouble;
import org.thechiselgroup.biomixer.client.core.ui.Colors;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.ArcRenderer;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.RenderedArc;
import org.thechiselgroup.biomixer.client.visualization_component.graph.rendering.RenderedNode;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.Arc;
import org.thechiselgroup.biomixer.shared.svg.Svg;
import org.thechiselgroup.biomixer.shared.svg.SvgElement;
import org.thechiselgroup.biomixer.shared.svg.SvgElementFactory;

/**
 * Renders an arc as a straight line using SVG.
 * 
 * @author drusk
 * 
 */
public class StraightLineSvgArcRenderer implements ArcRenderer {

    private SvgElementFactory svgElementFactory;

    public StraightLineSvgArcRenderer(SvgElementFactory svgElementFactory) {
        this.svgElementFactory = svgElementFactory;
    }

    @Override
    public RenderedArc createRenderedArc(Arc arc, RenderedNode source,
            RenderedNode target) {
        SvgElement container = svgElementFactory.createElement(Svg.G);
        container.setAttribute(Svg.ID, arc.getId());

        PointDouble sourceNodeLocation = source.getCentre();
        PointDouble targetNodeLocation = target.getCentre();

        SvgElement arcLine = svgElementFactory.createElement(Svg.LINE);
        arcLine.setAttribute(Svg.X1, sourceNodeLocation.getX());
        arcLine.setAttribute(Svg.Y1, sourceNodeLocation.getY());
        arcLine.setAttribute(Svg.X2, targetNodeLocation.getX());
        arcLine.setAttribute(Svg.Y2, targetNodeLocation.getY());
        arcLine.setAttribute(Svg.STROKE, Colors.BLACK);
        container.appendChild(arcLine);

        SvgArrowHead arrowHead = null;
        if (arc.isDirected()) {
            arrowHead = new SvgArrowHead(svgElementFactory, sourceNodeLocation,
                    targetNodeLocation);
            container.appendChild(arrowHead.asSvgElement());
        }

        return new StraightLineRenderedSvgArc(arc, container, arcLine,
                arrowHead, source, target);
    }

}
