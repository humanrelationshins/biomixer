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
package org.thechiselgroup.biomixer.client.visualization_component.graph.svg_widget;

import org.junit.Test;
import org.thechiselgroup.biomixer.client.core.geometry.Point;
import org.thechiselgroup.biomixer.client.core.ui.Colors;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.Arc;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.ArcSettings;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.GraphDisplay;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.Node;

/**
 * This class tests the SVG generated by the GraphSvgDisplay. Unit tests for
 * methods which do not result in SVG output are in GraphSvgDisplayProcessTest.
 * 
 * @author drusk
 * 
 */
public class OntologyOverviewGraphSvgDisplayOutputTest extends
        AbstractOntologyOverviewGraphSvgDisplayTest {

    @Test
    public void addNodePutsRectangleInSvg() throws Exception {
        addNode(N1, LABEL1, TYPE1);
        assertComponentWithIdEqualsFile(N1, "circleNode1");
    }

    @Test
    public void addTwoNodes() throws Exception {
        addNode(N1, LABEL1, TYPE1);
        addNode(N2, LABEL2, TYPE1);
        assertComponentWithIdEqualsFile(N1, "circleNode1");
        assertComponentWithIdEqualsFile(N2, "circleNode2");
    }

    @Test
    public void addTwoNodesAddArcSetLocationShouldCauseArcToReposition()
            throws Exception {
        addNode(N1, LABEL1, TYPE1);
        Node node2 = addNode(N2, LABEL2, TYPE1);
        addArc(A1, N1, N2, TYPE1, true);
        underTest.setLocation(node2, new Point(130, 0));
        assertComponentWithIdEqualsFile(A1, "arc1");
    }

    @Test
    public void addTwoNodesAndSetNewLocation() throws Exception {
        addNode(N1, LABEL1, TYPE1);
        Node node2 = addNode(N2, LABEL2, TYPE1);
        underTest.setLocation(node2, new Point(130, 0));
        assertComponentWithIdEqualsFile(N1, "circleNode1");
        assertComponentWithIdEqualsFile(N2, "node2Moved");
    }

    @Test
    public void addTwoNodesRemoveOne() {
        Node node1 = addNode(N1, LABEL1, TYPE1);
        addNode(N2, LABEL2, TYPE1);
        underTest.removeNode(node1);
        assertUnderTestAsSvgEqualsFile("addTwoNodesRemoveOne");
    }

    @Test
    public void addTwoNodesSetLocationAddArc() throws Exception {
        addNode(N1, LABEL1, TYPE1);
        Node node2 = addNode(N2, LABEL2, TYPE1);
        underTest.setLocation(node2, new Point(130, 0));
        addArc(A1, N1, N2, TYPE1, true);
        assertComponentWithIdEqualsFile(A1, "arc1");
    }

    private Arc addTwoSeparatedNodesWithArc() {
        addNode(N1, LABEL1, TYPE1);
        Node node2 = addNode(N2, LABEL2, TYPE1);
        underTest.setLocation(node2, new Point(130, 0));
        Arc arc = addArc(A1, N1, N2, TYPE1, true);
        return arc;
    }

    @Test
    public void animateToMovesOneNodeFinalDesinationShouldBeNewPoint() {
        Node node = addNode(N1, LABEL1, TYPE1);
        underTest.animateMoveTo(node, new Point(100, 100));
        assertUnderTestAsSvgEqualsFile("animateMoveOneNode");
    }

    @Test
    public void removeArcBetweenTwoNodes() {
        Arc arc = addTwoSeparatedNodesWithArc();
        underTest.removeArc(arc);
        assertUnderTestAsSvgEqualsFile("addTwoNodesSetLocation");
    }

    @Test
    public void removingNodeShouldRemoveArc() {
        Node node1 = addNode(N1, LABEL1, TYPE1);
        Node node2 = addNode(N2, LABEL2, TYPE1);
        addArc(A1, N1, N2, TYPE1, true);
        underTest.setLocation(node2, new Point(130, 0));
        underTest.removeNode(node1);
        assertUnderTestAsSvgEqualsFile("addTwoNodesAddArcMoveNode2RemoveNode1");
    }

    @Test
    public void setArcColor() throws Exception {
        Arc arc = addTwoSeparatedNodesWithArc();
        underTest.setArcStyle(arc, ArcSettings.ARC_COLOR, "#AFC6E5");
        assertComponentWithIdEqualsFile(A1, "arc1Colored");
    }

    @Test
    public void setArcStyleDashed() throws Exception {
        Arc arc = addTwoSeparatedNodesWithArc();
        underTest.setArcStyle(arc, ArcSettings.ARC_STYLE,
                ArcSettings.ARC_STYLE_DASHED);
        assertComponentWithIdEqualsFile(A1, "arc1Dashed");
    }

    @Test
    public void setArcStyleDashedThenSolid() throws Exception {
        Arc arc = addTwoSeparatedNodesWithArc();
        underTest.setArcStyle(arc, ArcSettings.ARC_STYLE,
                ArcSettings.ARC_STYLE_DASHED);
        underTest.setArcStyle(arc, ArcSettings.ARC_STYLE,
                ArcSettings.ARC_STYLE_SOLID);
        assertComponentWithIdEqualsFile(A1, "arc1");
    }

    @Test
    public void setArcStyleSolid() throws Exception {
        Arc arc = addTwoSeparatedNodesWithArc();
        underTest.setArcStyle(arc, ArcSettings.ARC_STYLE,
                ArcSettings.ARC_STYLE_SOLID);
        assertComponentWithIdEqualsFile(A1, "arc1");
    }

    @Test
    public void setArcThickness() throws Exception {
        Arc arc = addTwoSeparatedNodesWithArc();
        underTest.setArcStyle(arc, ArcSettings.ARC_THICKNESS, "3");
        assertComponentWithIdEqualsFile(A1, "arc1Thick");
    }

    @Test
    public void setNodeBackgroundColor() throws Exception {
        Node node = addNode(N1, LABEL1, TYPE1);
        underTest.setNodeStyle(node, GraphDisplay.NODE_BACKGROUND_COLOR,
                Colors.YELLOW_1);
        assertComponentWithIdEqualsFile(N1, "node1BackgroundColored");
    }

    @Test
    public void setNodeBorderColor() throws Exception {
        Node node = addNode(N1, LABEL1, TYPE1);
        underTest.setNodeStyle(node, GraphDisplay.NODE_BORDER_COLOR,
                Colors.YELLOW_2);
        assertComponentWithIdEqualsFile(N1, "node1BorderColored");
    }

    @Test
    public void setNodeFontColor() throws Exception {
        Node node = addNode(N1, LABEL1, TYPE1);
        underTest.setNodeStyle(node, GraphDisplay.NODE_FONT_COLOR,
                Colors.ORANGE);
        assertComponentWithIdEqualsFile(N1, "node1FontColored");
    }

    @Test
    public void setNodeFontWeightBold() throws Exception {
        Node node = addNode(N1, LABEL1, TYPE1);
        underTest.setNodeStyle(node, GraphDisplay.NODE_FONT_WEIGHT,
                GraphDisplay.NODE_FONT_WEIGHT_BOLD);
        assertComponentWithIdEqualsFile(N1, "node1BoldFont");
    }

    @Test
    public void setNodeFontWeightBoldThenNormal() throws Exception {
        Node node = addNode(N1, LABEL1, TYPE1);
        underTest.setNodeStyle(node, GraphDisplay.NODE_FONT_WEIGHT,
                GraphDisplay.NODE_FONT_WEIGHT_BOLD);
        underTest.setNodeStyle(node, GraphDisplay.NODE_FONT_WEIGHT,
                GraphDisplay.NODE_FONT_WEIGHT_NORMAL);
        assertComponentWithIdEqualsFile(N1, "node1NormalFont");
    }
}
