/*******************************************************************************
 * Copyright (C) 2011 Lars Grammel, Bo Fu
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
package org.thechiselgroup.biomixer.client.services.mapping;

import org.thechiselgroup.biomixer.client.visualization_component.graph.ResourceNeighbourhood;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface MappingServiceAsync {

    /**
     * Gets the mapping neighborhood (partial properties & mapping resources)
     * for a given concept.
     */
    void getMappings(String ontologyId, String conceptFullId,
            boolean mappingNeighbourhood,
            AsyncCallback<ResourceNeighbourhood> callback);

}