/*******************************************************************************
 * Copyright 2009, 2010 Lars Grammel 
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
package org.thechiselgroup.biomixer.client.services.search.concept;

import java.util.Set;

import org.thechiselgroup.biomixer.client.core.resources.Resource;

import com.google.gwt.user.client.rpc.AsyncCallback;

// TODO generalize search interface
public interface ConceptSearchServiceAsync {

    void searchConcept(String queryText, AsyncCallback<Set<Resource>> callback);

}