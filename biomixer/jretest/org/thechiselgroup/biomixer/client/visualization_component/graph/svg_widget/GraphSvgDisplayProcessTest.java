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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.thechiselgroup.biomixer.client.core.geometry.Point;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.Node;

/**
 * This class tests methods in the GraphSvgDisplay without checking the SVG
 * output generated. For tests that do test SVG output, see
 * GraphSvgDisplayOutputTest.
 * 
 * @author drusk
 * 
 */
public class GraphSvgDisplayProcessTest extends AbstractGraphSvgDisplayTest {

    @Test
    public void addArcShouldContainArc() {
        addNode(N1, LABEL1, TYPE1);
        addNode(N2, LABEL2, TYPE1);
        addArc(A1, N1, N2, TYPE1, true);
        assertTrue(underTest.containsArc(A1));
    }

    @Test
    public void addNodeShouldContainNode() {
        addNode(N1, LABEL1, TYPE1);
        assertTrue(underTest.containsNode(N1));
    }

    @Test
    public void nodeHasExpectedLocation() {
        Node node = addNode(N1, LABEL1, TYPE1);
        Point newLocation = new Point(100, 100);
        underTest.setLocation(node, newLocation);
        assertThat(underTest.getLocation(node), equalTo(newLocation));
    }
}
