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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.thechiselgroup.biomixer.client.Concept;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.services.AbstractJsonParserTest;
import org.thechiselgroup.biomixer.server.workbench.util.json.JacksonJsonParser;

public class TermWithoutRelationshipsJsonParserTest extends
        AbstractJsonParserTest {

    private TermWithoutRelationshipsJsonParser underTest;

    private Resource parseResource(String ontologyId, String jsonFilename)
            throws IOException {
        return underTest.parseConcept(ontologyId,
                getFileContentsAsString(jsonFilename));
    }

    @Test
    public void parseResponse() throws IOException {
        Resource concept = parseResource("44103",
                "term-service-light-norelationships.json");

        assertThat(concept.getUri(), equalTo(Concept.toConceptURI("44103",
                "http://purl.bioontology.org/ontology/ICD10/O80-O84.9")));
        assertThat((String) concept.getValue(Concept.FULL_ID),
                equalTo("http://purl.bioontology.org/ontology/ICD10/O80-O84.9"));
        assertThat((String) concept.getValue(Concept.VIRTUAL_ONTOLOGY_ID),
                equalTo("44103"));
        assertThat((String) concept.getValue(Concept.LABEL),
                equalTo("Delivery"));
        assertThat((String) concept.getValue(Concept.TYPE), equalTo("class"));
    }

    @Before
    public void setUp() {
        underTest = new TermWithoutRelationshipsJsonParser(
                new JacksonJsonParser());
    }

}
