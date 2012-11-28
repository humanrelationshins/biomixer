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
package org.thechiselgroup.biomixer.client.services.search.ontology;

import org.thechiselgroup.biomixer.client.services.AbstractJsonResultParser;
import org.thechiselgroup.biomixer.shared.workbench.util.json.JsonParser;

import com.google.inject.Inject;

/**
 * 
 * @author everbeek
 * 
 */
public class OntologyMetricJsonParser extends AbstractJsonResultParser {

    @Inject
    public OntologyMetricJsonParser(JsonParser jsonParser) {
        super(jsonParser);
    }

    private OntologyMetrics analyzeItem(Object jsonItem) {
        OntologyMetrics stats = new OntologyMetrics(getOntologyIdAsString(
                jsonItem, "id"));
        stats.numberOfClasses = asInt(get(jsonItem, "numberOfClasses"));
        stats.numberOfIndividuals = asInt(get(jsonItem, "numberOfIndividuals"));
        stats.numberOfProperties = asInt(get(jsonItem, "numberOfProperties"));
        stats.maximumDepth = asInt(get(jsonItem, "maximumDepth"));

        return stats;
    }

    @Override
    public OntologyMetrics parse(String json) {
        Object searchResults = get(
                get(get(get(super.parse(json), "success"), "data"), 0),
                "ontologyMetricsBean");
        return analyzeItem(searchResults);
    }

}
