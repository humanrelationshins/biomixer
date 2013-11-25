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
package org.thechiselgroup.biomixer.client.services.term;

import java.util.Map;
import java.util.Set;

import org.thechiselgroup.biomixer.client.Concept;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.util.collections.CollectionFactory;
import org.thechiselgroup.biomixer.shared.workbench.util.json.AbstractJsonResultParser;
import org.thechiselgroup.biomixer.shared.workbench.util.json.JsonParser;

import com.google.gwt.dev.util.collect.HashSet;
import com.google.inject.Inject;

public class TermWithoutRelationshipsJsonParser extends
        AbstractJsonResultParser {

    final String ONTOLOGY_ACRONYM_FROM_URL_PREFIX = "/ontologies/";

    @Inject
    public TermWithoutRelationshipsJsonParser(JsonParser jsonParser) {
        super(jsonParser);
    }

    public Resource parseConcept(String json) {
        Object jsonObject = parse(json);
        return parseConcept(jsonObject);
    }

    public Resource parseConcept(Object jsonObject) {

        String fullId = asString(get(jsonObject, "@id"));
        String label = asString(get(jsonObject, "prefLabel"));
        String type = asString(get(jsonObject, "type"));
        String ontologyHomepage = asString(get(get(jsonObject, "links"),
                "ontology"));
        String ontologyAcronym = ontologyHomepage.substring(ontologyHomepage
                .lastIndexOf(ONTOLOGY_ACRONYM_FROM_URL_PREFIX)
                + ONTOLOGY_ACRONYM_FROM_URL_PREFIX.length());

        Resource result = Concept.createConceptResource(ontologyAcronym,
                fullId, label, type);

        return result;
    }

    /**
     * This might be wrong headed to do it here, this way. We will save the most
     * by ensuring that we make a single call for concepts with children, plus a
     * single parent call per concept, plus whatever mapping calls we need. This
     * is best implemented where automatic expanders are triggered when adding
     * resources...
     * 
     * So perhaps the only problem here then is creating new resources for the
     * children and parents; we merely need to get the properties here.
     * 
     * @param jsonObject
     * @return
     */
    public Set<Resource> parseConceptsWithChildren(Object jsonObject) {
        Set<Resource> results = new HashSet<Resource>();
        Resource targetResource = parseConcept(jsonObject);
        results.add(targetResource);

        // Nested properties if accessing it with include=properties,
        // which is the case when we parse for the target concept, but not the
        // case when parsing children and parents in arrays from the same call.
        if (has(jsonObject, "properties")) {
            Object propertiesArray = get(jsonObject, "properties");
            if (!has(jsonObject, "prefLabel")) {
                String label = asString(get(
                        get(propertiesArray,
                                "http://www.w3.org/2004/02/skos/core#prefLabel"),
                        "string"));
                targetResource.putValue(label, Concept.LABEL);
            }
        }

        // Nested children if accessing it with include=children
        if (has(jsonObject, "children")) {
            Object childrenArray = get(jsonObject, "children");
            int numChildren = length(childrenArray);
            for (int i = 0; i < numChildren; i++) {
                // TODO We probably want to avoid creating this Resour4ce, and
                // get the URI only.
                Resource child = parseConcept(get(childrenArray, i));
                results.add(child);
                // And add children links to target
                targetResource.addChild(child.getUri());
            }
        }

        // Nested parents if accessing it with include=parents
        if (has(jsonObject, "parents")) {
            Object childrenArray = get(jsonObject, "parents");
            int numChildren = length(childrenArray);
            for (int i = 0; i < numChildren; i++) {
                // TODO We probably want to avoid creating this Resour4ce, and
                // get the URI only.
                Resource parent = parseConcept(get(childrenArray, i));
                results.add(parent);
                // And add children links to target
                targetResource.addParent(parent.getUri());
            }
        }

        // So we save ourselves the children and parent calls for this target
        // resource, but we still need to get the mappings for the target, as
        // well as the parent, children and mappings for each neighbor node.
        // So...did we save anything? Maybe not for the first node layer, but
        // when we do a bulk call on the basis of these resources, to get their
        // children and parents...suppose we make a single child, parent and
        // mapping call each, for each neighbour. That is 3*N calls. The bulk
        // calling allows us to do the parent and children calls in bulk each,
        // which makes us do N+2 calls (2 for the child and parent bulks).

        return results;
    }

    /**
     * Receives the same object that we want for the parseConcept() method, but
     * we are looking specifically for the properties in it, and for the hasA
     * and partOf relations in particular. Returns a map of concept ids and
     * their relation type.
     * 
     * @param ontologyAcronym
     * @param jsonObject
     * @return
     */
    public Map<String, String> parseForCompositionProperties(String json) {
        Map<String, String> compositionMap = CollectionFactory
                .createStringMap();
        Object jsonObject = parse(json);
        Object propertiesObject = get(jsonObject, "properties");

        Set<String> propertyNames = getObjectProperties(propertiesObject);
        // Need to do this funny little job because the composition property
        // names are variable, and contain things such as the ontology acronym
        // in them. They appear to all end the same way though...
        for (String name : propertyNames) {
            if (name.endsWith("has_part")) {
                Object propArray = get(propertiesObject, name);
                int length = length(propArray);
                for (int i = 0; i < length; i++) {
                    compositionMap.put(asString(get(propArray, i)),
                            Concept.HAS_PART_CONCEPTS);
                }
            }
            if (name.endsWith("part_of")) {
                Object propArray = get(propertiesObject, name);
                int length = length(propArray);
                for (int i = 0; i < length; i++) {
                    compositionMap.put(asString(get(propArray, i)),
                            Concept.PART_OF_CONCEPTS);
                }
            }
        }

        return compositionMap;
    }
}
