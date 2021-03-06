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
package org.thechiselgroup.biomixer.client.core.resources;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.thechiselgroup.biomixer.client.Concept;

// TODO introduce resource ID's
// TODO equality / hash based on ID
public class Resource implements Serializable {

    private static final long serialVersionUID = 5652752520235015241L;

    public static String getTypeFromURI(String uri) {
        assert uri != null;

        int splitIndex = uri.indexOf(':');
        return uri.substring(0, splitIndex);
    }

    // TODO find ways to use better map implementation
    // (CollectionFactory.createStringMap)
    private HashMap<String, Serializable> properties = new HashMap<String, Serializable>();

    // unique resource identifier (URI)
    private String uri;

    // for GWT serialization usage only
    public Resource() {
    }

    static private Map<String, Resource> resourceIndex = new HashMap<String, Resource>();

    static public Resource createIndexedResource(String uri) {
        Resource res = resourceIndex.get(uri);
        if (null == res) {
            res = new Resource(uri);
            resourceIndex.put(uri, res);
        }
        return res;
    }

    public Resource(String uri) {
        assert uri != null;
        this.uri = uri;
    }

    public void addChild(String uri) {
        UriList newChild = new UriList(uri);
        addChildren(newChild);
    }

    // TODO This is not cool. Not all resources are concepts. Not causing a
    // problem at the moment.
    public void addChildren(UriList additionalChildren) {
        UriList currentChildren = getUriListValue(Concept.CHILD_CONCEPTS);
        currentChildren.addAllNew(additionalChildren);
        putValue(Concept.CHILD_CONCEPTS, currentChildren);
    }

    public void addParent(String uri) {
        UriList newParent = new UriList(uri);
        addParents(newParent);
    }

    public void addParents(UriList additionalParents) {
        UriList currentParents = getUriListValue(Concept.PARENT_CONCEPTS);
        currentParents.addAllNew(additionalParents);
        putValue(Concept.PARENT_CONCEPTS, currentParents);
    }

    public void addPartOf(UriList additionalPartOf) {
        UriList currentPartOf = getUriListValue(Concept.PART_OF_CONCEPTS);
        currentPartOf.addAllNew(additionalPartOf);
        putValue(Concept.PART_OF_CONCEPTS, currentPartOf);
    }

    public void addPartOf(String uri) {
        UriList newPartOf = new UriList(uri);
        addPartOf(newPartOf);
    }

    public void addHasPart(UriList additionalHasPart) {
        UriList currentHasPart = getUriListValue(Concept.HAS_PART_CONCEPTS);
        currentHasPart.addAllNew(additionalHasPart);
        putValue(Concept.HAS_PART_CONCEPTS, currentHasPart);
    }

    public void addHasPart(String uri) {
        UriList newHasPart = new UriList(uri);
        addHasPart(newHasPart);
    }

    public void addRelationalProperties(
            Map<String, Serializable> partialProperties) {
        for (Entry<String, Serializable> entry : partialProperties.entrySet()) {
            // This used to clobber existing data, which was a huge problem for
            // cases where relational data was coming from multiple REST calls.
            // Relational data does not get replaced in the normal course of a
            // visualization, so keeping old values is sensible.
            // putValue(entry.getKey(), entry.getValue());
            UriList currentRelation = getUriListValue(entry.getKey());
            currentRelation.addAll((UriList) entry.getValue());
            putValue(entry.getKey(), currentRelation);
        }
    }

    public boolean containsProperty(String property) {
        return properties.containsKey(property);
    }

    /**
     * Equals is just based on the <code>uri</code>. In Choosel there should be
     * just one resource per uri.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Resource other = (Resource) obj;

        return uri.equals(other.uri);
    }

    public HashMap<String, Serializable> getProperties() {
        return properties;
    }

    public String getUri() {
        return uri;
    }

    // return a UriList representation of a resource property
    public UriList getUriListValue(String key) {
        UriList result = (UriList) getValue(key);

        if (result == null) {
            result = new UriList();
            putValue(key, result);
        }

        return result;
    }

    public Object getValue(String key) {
        return properties.get(key);
    }

    /**
     * @return hash code of uri
     */
    @Override
    public int hashCode() {
        return uri.hashCode();
    }

    public boolean isUriList(String key) {
        return getValue(key) instanceof UriList;
    }

    public void putValue(String key, Serializable value) {
        properties.put(key, value);
    }

    public void putValueAsUriList(String key, String uri) {
        UriList uriList = new UriList();
        uriList.add(uri);
        putValue(key, uriList);
    }

    @Override
    public String toString() {
        return "Resource [uri=" + uri + ";properties=" + properties + "]";
    }

}
