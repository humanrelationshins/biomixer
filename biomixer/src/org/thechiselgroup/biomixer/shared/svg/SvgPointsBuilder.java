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
package org.thechiselgroup.biomixer.shared.svg;

import java.util.ArrayList;
import java.util.List;

import org.thechiselgroup.biomixer.client.core.geometry.PointDouble;

public class SvgPointsBuilder {

    private List<PointDouble> points = new ArrayList<PointDouble>();

    public void addPoint(double x, double y) {
        points.add(new PointDouble(x, y));
    }

    public String toPointsString() {
        StringBuilder pointsString = new StringBuilder();
        for (PointDouble point : points) {
            pointsString.append(point.getX() + "," + point.getY() + " ");
        }
        return pointsString.toString();
    }

}
