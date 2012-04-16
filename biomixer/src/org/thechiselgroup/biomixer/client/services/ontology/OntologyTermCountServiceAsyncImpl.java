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
package org.thechiselgroup.biomixer.client.services.ontology;

import org.thechiselgroup.biomixer.client.core.util.transform.Transformer;
import org.thechiselgroup.biomixer.client.core.util.url.UrlBuilderFactory;
import org.thechiselgroup.biomixer.client.core.util.url.UrlFetchService;
import org.thechiselgroup.biomixer.client.services.AbstractXMLWebResourceService;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class OntologyTermCountServiceAsyncImpl extends
        AbstractXMLWebResourceService implements OntologyTermCountServiceAsync {

    private OntologyTermCountParser parser;

    public OntologyTermCountServiceAsyncImpl(UrlFetchService urlFetchService,
            UrlBuilderFactory urlBuilderFactory, OntologyTermCountParser parser) {
        super(urlFetchService, urlBuilderFactory);
        this.parser = parser;
    }

    private String buildUrl(String virtualOntologyId) {
        return urlBuilderFactory
                .createUrlBuilder()
                .path("/bioportal/virtual/ontology/" + virtualOntologyId
                        + "/all").toString();
    }

    @Override
    public void getTermCount(String virtualOntologyId,
            AsyncCallback<String> callback) {
        assert virtualOntologyId != null;
        assert callback != null;

        final String url = buildUrl(virtualOntologyId);

        fetchUrl(callback, url, new Transformer<String, String>() {

            @Override
            public String transform(String xmlText) throws Exception {
                return parser.parse(xmlText);
            }

        });
    }

}
