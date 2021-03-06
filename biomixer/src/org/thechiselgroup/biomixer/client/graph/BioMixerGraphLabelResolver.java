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
package org.thechiselgroup.biomixer.client.graph;

import org.thechiselgroup.biomixer.client.Concept;
import org.thechiselgroup.biomixer.client.Mapping;
import org.thechiselgroup.biomixer.client.Ontology;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.util.DataType;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem.Subset;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemValueResolverContext;
import org.thechiselgroup.biomixer.client.core.visualization.resolvers.FirstResourcePropertyResolver;

public class BioMixerGraphLabelResolver extends FirstResourcePropertyResolver {

    public BioMixerGraphLabelResolver() {
        // TODO This is not good. Made issue #220. Try fixing with another resolver, and also try adding enums instead of hard coded strings, if GWT allows this.
        super(Concept.LABEL, DataType.TEXT);
    }

    @Override
    public boolean canResolve(VisualItem visualItem,
            VisualItemValueResolverContext context) {
        return true;
    }

    @Override
    public Object resolve(VisualItem visualItem,
            VisualItemValueResolverContext context, Subset subset) {

        String type = Resource.getTypeFromURI(visualItem.getId());
        if (Concept.RESOURCE_URI_PREFIX.equals(type)) {
            return super.resolve(visualItem, context, subset);
        }

        // prevents mapping nodes from showing label
        if (Mapping.RESOURCE_URI_PREFIX.equals(type)) {
            return "";
        }

        if (Ontology.RESOURCE_URI_PREFIX.equals(type)) {
            return super.resolve(visualItem, context, subset);
        }

        return "";
    }
}