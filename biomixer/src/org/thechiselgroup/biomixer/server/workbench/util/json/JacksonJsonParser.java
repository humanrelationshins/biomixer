/*******************************************************************************
 * Copyright 2012 David Rusk, Lars Grammel
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
package org.thechiselgroup.biomixer.server.workbench.util.json;

import org.thechiselgroup.biomixer.shared.workbench.util.json.JsonParser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonJsonParser implements JsonParser {

    @Override
    public int asInt(Object jsonNode) {
        return ((JsonNode) jsonNode).asInt();
    }

    @Override
    public double asNumber(Object jsonNode) {
        return ((JsonNode) jsonNode).asDouble();
    }

    @Override
    public String asString(Object jsonNode) {
        return ((JsonNode) jsonNode).asText();
    }

    @Override
    public Object get(Object jsonNode, int index) {
        return ((JsonNode) jsonNode).get(index);
    }

    @Override
    public Object get(Object jsonNode, String property) {
        JsonNode node = (JsonNode) jsonNode;
        return node.isObject() ? node.get(property) : null;
    }

    @Override
    public boolean has(Object jsonNode, String property) {
        return ((JsonNode) jsonNode).has(property);
    }

    @Override
    public boolean isArray(Object jsonNode) {
        return ((JsonNode) jsonNode).isArray();
    }

    @Override
    public int length(Object jsonNode) {
        return ((JsonNode) jsonNode).size();
    }

    @Override
    public Object parse(String json) {
        try {
            return new ObjectMapper().readTree(json);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
