/*******************************************************************************
 * Copyright 2009, 2010 Lars Grammel, Bo Fu 
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
package org.thechiselgroup.biomixer.client;

import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.ResourceManager;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetFactory;
import org.thechiselgroup.biomixer.client.core.resources.ui.AbstractDetailsWidgetHelper;
import org.thechiselgroup.biomixer.client.core.resources.ui.ResourceSetAvatar;
import org.thechiselgroup.biomixer.client.core.resources.ui.ResourceSetAvatarFactory;
import org.thechiselgroup.biomixer.client.core.resources.ui.ResourceSetAvatarType;
import org.thechiselgroup.biomixer.client.core.util.url.BioportalWebUrlBuilder;
import org.thechiselgroup.biomixer.client.core.util.url.UrlBuilder;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.biomixer.client.dnd.resources.DraggableResourceSetAvatar;
import org.thechiselgroup.biomixer.client.dnd.resources.ResourceSetAvatarDragController;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class BioMixerDetailsWidgetHelper extends AbstractDetailsWidgetHelper {

    private final ResourceSetAvatarDragController dragController;

    private final ResourceManager resourceManager;

    @Inject
    public BioMixerDetailsWidgetHelper(ResourceSetFactory resourceSetFactory,
            ResourceSetAvatarFactory dragAvatarFactory,
            ResourceSetAvatarDragController dragController,
            ResourceManager resourceManager) {

        super(resourceSetFactory, dragAvatarFactory);

        this.dragController = dragController;
        this.resourceManager = resourceManager;
    }

    protected ResourceSetAvatar createAvatar(String label,
            ResourceSet resourceSet) {
        ResourceSetAvatar avatar = new DraggableResourceSetAvatar(label,
                "avatar-resourceSet", resourceSet, ResourceSetAvatarType.SET);
        avatar.setEnabled(true);
        dragController.setDraggable(avatar, true);
        return avatar;
    }

    // TODO use dragAvatarFactory (injection)
    @Override
    public Widget createDetailsWidget(VisualItem visualItem) {
        ResourceSet resourceSet = visualItem.getResources();
        VerticalPanel verticalPanel = GWT.create(VerticalPanel.class);
        final Resource resource = resourceSet.getFirstElement();

        // FIXME use generic way to put in custom widgets
        if (Concept.isConcept(resource)) {
            // making the concept label clickable
            ResourceSetAvatar avatar = createAvatar(
                    (String) resource.getValue(Concept.LABEL), resourceSet);
            avatar.addClickHandler(new com.google.gwt.event.dom.client.ClickHandler() {
                @Override
                public void onClick(com.google.gwt.event.dom.client.ClickEvent e) {
                    com.google.gwt.user.client.Window.open(
                            (String) resource.getValue(Concept.FULL_ID),
                            "_blank", "");
                }
            });
            verticalPanel.add(avatar);

            addRow(resource, verticalPanel, "Ontology",
                    Concept.CONCEPT_ONTOLOGY_NAME);
            addRow(resource, verticalPanel, "Ontology ID",
                    Concept.VIRTUAL_ONTOLOGY_ID);
            addRow(resource, verticalPanel, "Concept ID", Concept.SHORT_ID);

        }
        if (Ontology.isOntology(resource)) {
            // making the ontology label clickable
            ResourceSetAvatar avatar = createAvatar(
                    (String) resource.getValue(Ontology.ONTOLOGY_NAME),
                    resourceSet);
            final UrlBuilder ontologySummaryUrl = BioportalWebUrlBuilder
                    .generateOntologySummaryUrl((String) resource
                            .getValue(Ontology.VIRTUAL_ONTOLOGY_ID));
            ClickHandler urlClickHandler = new ClickHandler() {
                @Override
                public void onClick(com.google.gwt.event.dom.client.ClickEvent e) {

                    com.google.gwt.user.client.Window.open(
                            ontologySummaryUrl.toString(), "_blank", "");
                }
            };
            avatar.addClickHandler(urlClickHandler);
            verticalPanel.add(avatar);

            // The summary url is also clickable. Perhaps they can have
            // different targets? Not sure...
            // solving the above problem - solution: ontology name points to the
            // BioPortal link; homepage points to the original ontology site.
            // addRow("Summary", ontologySummaryUrl, true, verticalPanel);

            addRow(resource, verticalPanel, "Ontology Homepage",
                    Ontology.HOMEPAGE);
            addRow(resource, verticalPanel, "Ontology Acronym",
                    Ontology.ACRONYM);
            addRow(resource, verticalPanel, "Ontology ID",
                    Ontology.VIRTUAL_ONTOLOGY_ID);
            addRow(resource, verticalPanel, "Ontology Format", Ontology.FORMAT);
            addRow(resource, verticalPanel, "Number of Classes",
                    Ontology.NUMBER_OF_CLASSES);
            addRow(resource, verticalPanel, "Number of Individuals",
                    Ontology.NUMBER_OF_INDIVIDUALS);
            addRow(resource, verticalPanel, "Number of Properties",
                    Ontology.NUMBER_OF_PROPERTIES);
            // add text wrapping to the ontology description. the default value
            // "true" turns off wrapping.
            addRow(resource, verticalPanel, "Ontology Description",
                    Ontology.DESCRIPTION, false);

        } else if (Mapping.isMapping(resource)) {
            verticalPanel.add(createAvatar("Mapping", resourceSet));

            addRow(resource, verticalPanel, "Created", Mapping.DATE);
            addRow(resource, verticalPanel, "Mapping source",
                    Mapping.MAPPING_SOURCE);
            addRow(resource, verticalPanel, "Mapping source name",
                    Mapping.MAPPING_SOURCE_NAME);
            addRow(resource, verticalPanel, "Mapping type",
                    Mapping.MAPPING_TYPE);

            Resource sourceConcept = resourceManager.getByUri((String) resource
                    .getValue(Mapping.SOURCE));
            if (sourceConcept != null) {
                addRow(sourceConcept, verticalPanel, "Source concept",
                        Concept.LABEL);
                addRow(sourceConcept, verticalPanel, "Source ontology ID",
                        Concept.VIRTUAL_ONTOLOGY_ID);
                // TODO ontology names (might need service for ontologies)
            }

            Resource targetConcept = resourceManager.getByUri((String) resource
                    .getValue(Mapping.TARGET));
            if (targetConcept != null) {
                addRow(targetConcept, verticalPanel, "Target concept",
                        Concept.LABEL);
                addRow(targetConcept, verticalPanel, "Target ontology ID",
                        Concept.VIRTUAL_ONTOLOGY_ID);
                // TODO ontology names (might need service for ontologies)
            }
        } else {
            verticalPanel.add(avatarFactory.createAvatar(resourceSet));

            String value = "";
            HTML html = GWT.create(HTML.class);
            html.setHTML(value);
            verticalPanel.add(html);
        }

        return verticalPanel;
    }
}