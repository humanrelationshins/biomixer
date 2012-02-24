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

public class ExpanderPopupManager {

    private SvgPopupExpanders expanderPopup = null;

    private final GraphSvgDisplay graphDisplay;

    private boolean mousedOver = false;

    public ExpanderPopupManager(GraphSvgDisplay graphDisplay) {
        this.graphDisplay = graphDisplay;
    }

    public SvgPopupExpanders getPopupExpander() {
        return expanderPopup;
    }

    public void onMenuItemClick() {
        assert expanderPopup != null;
        graphDisplay.removeSvgElement(expanderPopup.getContainer());
        expanderPopup = null;
    }

    public void onMouseDown() {
        if (expanderPopup != null && !mousedOver) {
            graphDisplay.removeSvgElement(expanderPopup.getContainer());
            expanderPopup = null;
        }
    }

    public void onMouseOut() {
        mousedOver = false;
    }

    public void onMouseOver() {
        mousedOver = true;
    }

    public void setPopupExpander(SvgPopupExpanders expanderPopup) {
        this.expanderPopup = expanderPopup;
        mousedOver = false;
    }

}