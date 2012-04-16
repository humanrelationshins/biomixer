/*******************************************************************************
 * Copyright 2012 Lars Grammel 
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
package org.thechiselgroup.biomixer.shared.svg;

public final class SvgTransforms {

    public static String rotate(double degrees, double centreX, double centreY) {
        return "rotate(" + degrees + " " + centreX + " " + centreY + ")";
    }

    public static String translate(double x, double y) {
        return "translate(" + x + "," + y + ")";
    }

    public static String translate(int x, int y) {
        return "translate(" + x + "," + y + ")";
    }

    private SvgTransforms() {
    }

}