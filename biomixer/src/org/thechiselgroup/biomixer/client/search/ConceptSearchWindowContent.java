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
package org.thechiselgroup.biomixer.client.search;

import org.thechiselgroup.biomixer.client.AbstractSearchWindowContent;
import org.thechiselgroup.biomixer.client.Concept;
import org.thechiselgroup.biomixer.client.AbstractSearchCallbackFactory;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetFactory;
import org.thechiselgroup.biomixer.client.services.search.concept.ConceptSearchServiceAsync;
import org.thechiselgroup.biomixer.client.workbench.ui.configuration.ViewWindowContentProducer;

import com.google.inject.Inject;

public class ConceptSearchWindowContent extends AbstractSearchWindowContent {

    private ConceptSearchServiceAsync searchService;

    @Inject
    public ConceptSearchWindowContent(ResourceSetFactory resourceSetFactory,
            ConceptSearchServiceAsync searchService,
            ViewWindowContentProducer viewFactory) {
        super(resourceSetFactory, Concept.UI_LABEL, viewFactory,
                ConceptSearchCommand.NCBO_CONCEPT_SEARCH);
        this.searchService = searchService;
    }

    @Override
    protected void searchForTerm(String queryTerm,
            AbstractSearchCallbackFactory callBack) {
        searchService.searchConcept(queryTerm, callBack);
    }

}