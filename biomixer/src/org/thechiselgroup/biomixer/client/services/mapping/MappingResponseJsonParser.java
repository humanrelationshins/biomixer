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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.thechiselgroup.biomixer.client.Concept;
import org.thechiselgroup.biomixer.client.Mapping;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.services.AbstractJsonResultParser;
import org.thechiselgroup.biomixer.client.services.term.TermServiceImplementation;
import org.thechiselgroup.biomixer.shared.core.util.date.DateTimeFormat;
import org.thechiselgroup.biomixer.shared.core.util.date.DateTimeFormatFactory;
import org.thechiselgroup.biomixer.shared.workbench.util.json.JsonParser;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class MappingResponseJsonParser extends AbstractJsonResultParser {

    private static final String AUTOMATIC_MAPPING_TYPE = "Automatic";

    private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss.S z";

    private final DateTimeFormat dateFormat;

    @Inject
    private TermServiceImplementation tsi;

    @Inject
    public MappingResponseJsonParser(JsonParser jsonParser,
            DateTimeFormatFactory dateTimeFormatFactory) {
        super(jsonParser);
        this.dateFormat = dateTimeFormatFactory
                .createDateTimeFormat(DATE_PATTERN);
    }

    private Resource parseMapping(Object mapping) {
        String id = asString(get(mapping, "id"));
        Resource resource = new Resource(Mapping.toMappingURI(id));
        resource.putValue(Mapping.ID, id);

        final String sourceOntologyId = getOntologyIdAsString(mapping,
                "sourceOntologyId");
        // NOTE: odd json format -> {source: [{fullId: <fullId>}], target:
        // [{fullId: <fullId>}]}
        final String sourceConceptId = asString(get(
                get(get(mapping, "source"), 0), "fullId"));
        String sourceUri = Concept.toConceptURI(sourceOntologyId,
                sourceConceptId);
        resource.putValue(Mapping.SOURCE, sourceUri);

        // adding definitions for the source concepts
        final AsyncCallback<Resource> sourceCallback = new AsyncCallback<Resource>() {
            @Override
            public void onFailure(Throwable caught) {
            }

            @Override
            public void onSuccess(Resource conceptWithDefinition) {
                Concept.createConceptResource(sourceOntologyId, sourceConceptId)
                        .putValue(
                                Concept.DEFINITION,
                                (String) conceptWithDefinition
                                        .getValue(Concept.DEFINITION));
            }
        };
        if (tsi != null && sourceOntologyId != null && sourceConceptId != null
                && sourceCallback != null) {
            tsi.getBasicInformationNoOntologyName(sourceOntologyId,
                    sourceConceptId, sourceCallback);
        }

        String targetOntologyId = getOntologyIdAsString(mapping,
                "targetOntologyId");
        String targetConceptId = asString(get(get(get(mapping, "target"), 0),
                "fullId"));
        String targetUri = Concept.toConceptURI(targetOntologyId,
                targetConceptId);
        resource.putValue(Mapping.TARGET, targetUri);

        // adding definitions for the target concepts
        final AsyncCallback<Resource> targetCallback = new AsyncCallback<Resource>() {
            @Override
            public void onFailure(Throwable caught) {
            }

            @Override
            public void onSuccess(Resource conceptWithDefinition) {
                Concept.createConceptResource(sourceOntologyId, sourceConceptId)
                        .putValue(
                                Concept.DEFINITION,
                                (String) conceptWithDefinition
                                        .getValue(Concept.DEFINITION));
            }
        };
        if (tsi != null && sourceOntologyId != null && sourceConceptId != null
                && targetCallback != null) {
            tsi.getBasicInformationNoOntologyName(sourceOntologyId,
                    sourceConceptId, targetCallback);
        }

        String mappingType = asString(get(mapping, "mappingType"));
        resource.putValue(Mapping.MAPPING_TYPE, mappingType);
        if (mappingType.equals(AUTOMATIC_MAPPING_TYPE)) {
            resource.putValue(Mapping.MAPPING_SOURCE,
                    asString(get(mapping, "mappingSource")));
            resource.putValue(Mapping.MAPPING_SOURCE_NAME,
                    asString(get(mapping, "mappingSourceName")));
        }

        Date date = dateFormat.parse(asString(get(mapping, "date")));
        resource.putValue(Mapping.DATE, date);

        return resource;
    }

    public List<Resource> parseMapping(String json) {
        List<Resource> result = new ArrayList<Resource>();

        Object mappings = get(
                get(get(get(get(get(get(parse(json), "success"), "data"), 0),
                        "page"), "contents"), "mappings"), "mapping");

        if (mappings == null) {
            return result;
        }

        for (int i = 0; i < length(mappings); i++) {
            result.add(parseMapping(get(mappings, i)));
        }

        return result;
    }
}
