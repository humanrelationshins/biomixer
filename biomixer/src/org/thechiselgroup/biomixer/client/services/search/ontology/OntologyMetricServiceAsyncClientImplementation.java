/*******************************************************************************
 * Copyright (C) 2011 Lars Grammel 
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
package org.thechiselgroup.biomixer.client.services.search.ontology;

import org.thechiselgroup.biomixer.client.Ontology;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.util.transform.Transformer;
import org.thechiselgroup.biomixer.client.core.util.url.UrlBuilderFactory;
import org.thechiselgroup.biomixer.client.core.util.url.UrlFetchService;
import org.thechiselgroup.biomixer.client.services.AbstractWebResourceService;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

/**
 * This service doesn't have a version for the ontology virtual id at the
 * moment, so it uses the ontolgoy version id instead.
 * 
 * @author everbeek
 * 
 */
public class OntologyMetricServiceAsyncClientImplementation extends
        AbstractWebResourceService implements OntologyMetricServiceAsync {

    private final OntologyMetricJsonParser responseParser;

    @Inject
    public OntologyMetricServiceAsyncClientImplementation(
            UrlFetchService urlFetchService,
            UrlBuilderFactory urlBuilderFactory,

            OntologyMetricJsonParser responseParser) {
        super(urlFetchService, urlBuilderFactory);

        this.responseParser = responseParser;
    }

    protected String buildUrl(String ontologyAcronym) {
        // http://stagedata.bioontology.org/ontologies/"+ontologyAcronym+"/metrics
        return urlBuilderFactory.createUrlBuilder()
                .path("/ontologies/" + ontologyAcronym + "/metrics/")
                .toString();
    }

    @Override
    public void getMetrics(final String ontologyAcronym,
            final AsyncCallback<OntologyMetrics> callback) {
        assert ontologyAcronym != null;
        assert callback != null;

        final String url = buildUrl(ontologyAcronym);
        fetchUrl(callback, url, new Transformer<String, OntologyMetrics>() {
            @Override
            public OntologyMetrics transform(String json) throws Exception {
                responseParser.setOntologyAcronym(ontologyAcronym);
                return responseParser.parse(json);
            }
        });

    }

    @Override
    public void getMetrics(Resource ontology,
            AsyncCallback<OntologyMetrics> callback) {
        getMetrics((String) ontology.getValue(Ontology.ONTOLOGY_ACRONYM),
                callback);
    }

}