/*******************************************************************************
 * Copyright 2012 Lars Grammel, David Rusk 
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
package org.thechiselgroup.biomixer.client.visualization_component.graph.layout;

import java.util.List;

/**
 * Class of nodes.
 * 
 * @author Lars Grammel
 */
public interface LayoutNodeType {

    /**
     * NOTE: we return a list to guarantee node order for testing purposes.
     * There must be no duplicate nodes in this list.
     * 
     * @return all nodes of this node type
     */
    List<LayoutNode> getNodes();

}