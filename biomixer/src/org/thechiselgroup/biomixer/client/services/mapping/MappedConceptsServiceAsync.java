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
package org.thechiselgroup.biomixer.client.services.mapping;

import java.util.List;

import org.thechiselgroup.biomixer.client.core.resources.Resource;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface MappedConceptsServiceAsync {

    /**
     * Returns the concepts which are mapped to the specified concepts.
     */
    void getMappedConcepts(String ontologyId, String fullConceptId,
            AsyncCallback<List<Resource>> callback);

}
