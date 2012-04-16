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

import java.util.EventObject;

public class ClickEvent extends EventObject {

    private final int clickX;

    private final int clickY;

    public ClickEvent(int clickX, int clickY, Object source) {
        super(source);
        this.clickX = clickX;
        this.clickY = clickY;
    }

    public int getClickX() {
        return clickX;
    }

    public int getClickY() {
        return clickY;
    }

}
