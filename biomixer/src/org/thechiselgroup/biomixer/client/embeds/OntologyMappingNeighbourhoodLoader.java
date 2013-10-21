/*******************************************************************************
 * Copyright 2012 David Rusk, Bo Fu
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
package org.thechiselgroup.biomixer.client.embeds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.thechiselgroup.biomixer.client.Ontology;
import org.thechiselgroup.biomixer.client.AbstractSearchCallback;
import org.thechiselgroup.biomixer.client.AbstractSearchCallbackFactory;
import org.thechiselgroup.biomixer.client.core.configuration.ChooselInjectionConstants;
import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandlingAsyncCallback;
import org.thechiselgroup.biomixer.client.core.error_handling.LoggingErrorHandler;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSetFactory;
import org.thechiselgroup.biomixer.client.core.resources.UriList;
import org.thechiselgroup.biomixer.client.core.visualization.LeftViewTopBarExtension;
import org.thechiselgroup.biomixer.client.core.visualization.View;
import org.thechiselgroup.biomixer.client.dnd.resources.DropEnabledViewContentDisplay;
import org.thechiselgroup.biomixer.client.dnd.windows.ViewWindowContent;
import org.thechiselgroup.biomixer.client.graph.OntologyNodeMappingExpander;
import org.thechiselgroup.biomixer.client.services.Fetch;
import org.thechiselgroup.biomixer.client.services.ontology_overview.OntologyMappingCount;
import org.thechiselgroup.biomixer.client.services.ontology_overview.OntologyMappingCountServiceAsync;
import org.thechiselgroup.biomixer.client.services.ontology_overview.TotalMappingCount;
import org.thechiselgroup.biomixer.client.services.search.ontology.OntologySearchServiceAsync;
import org.thechiselgroup.biomixer.client.visualization_component.graph.Graph;
import org.thechiselgroup.biomixer.client.visualization_component.graph.GraphLayoutSupport;
import org.thechiselgroup.biomixer.client.visualization_component.graph.GraphOntologyOverviewViewContentDisplayFactory;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutAlgorithm;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.animations.NodeAnimator;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.circle.CircleLayoutWithCentralNodeAlgorithm;
import org.thechiselgroup.biomixer.client.workbench.ui.configuration.ViewWindowContentProducer;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * This loader is intended to take a single ontology, and to load the mapping
 * neighbourhood of ontologies for it. It might be desirable to change this to
 * extend EmbeddedViewLoader.
 * 
 * @author everbeek
 * 
 */
public class OntologyMappingNeighbourhoodLoader implements OntologyEmbedLoader
// extends EmbeddedViewLoader
{

    private static final double MIN_ANGLE = 0.0;

    private static final double MAX_ANGLE = 360.0;

    public static final String EMBED_MODE = "ontology_mapping_neighbourhood";

    public static final String EMBED_LABEL = "ontology mapping neighbourhood";

    @Override
    public String getId() {
        return EMBED_MODE;
    }

    @Override
    public String getLabel() {
        return EMBED_LABEL;
    }

    @Inject
    private OntologyMappingCountServiceAsync mappingService;

    @Inject
    private OntologySearchServiceAsync searchService;

    @Inject
    private ResourceSetFactory resourceSetFactory;

    @Inject
    private LoggingErrorHandler loggingErrorHandler;

    @Inject
    public OntologyMappingNeighbourhoodLoader() {
    }

    private String centralOntologyUri;

    private void doLoadData(final String centralOntologyAcronym,
            final View graphView, final ErrorHandler errorHandler) {
        mappingService
                .getAllMappingCountsForCentralOntology(centralOntologyAcronym,
                        new ErrorHandlingAsyncCallback<TotalMappingCount>(
                                errorHandler) {

                            @Override
                            protected String getMessage(Throwable caught) {
                                return "Error finding ontology mappings";
                            }

                            @Override
                            protected void runOnSuccess(
                                    TotalMappingCount mappingCountResults)
                                    throws Exception {

                                // if (!graphView.isInitialized()) {
                                // return;
                                // }
                                // Create resources for each ontology, including target
                                // and mapped ontologies.
                                // Set<String> ontologyIds = new HashSet<String>();
                                // ontologyIds.add(centralOntologyVirtualId);

                                Set<String> ontologyAcronyms = new HashSet<String>();
                                ontologyAcronyms.add(centralOntologyAcronym);

                                // Iterate through the neighbourhood
                                // for (OntologyMappingCount ontologyCount :
                                // mappingCountResults) {
                                // ontologyIds.add(ontologyCount.getTargetOntologyAcronym());
                                // }
                                for (OntologyMappingCount ontologyCount : mappingCountResults) {
                                    ontologyAcronyms.add(ontologyCount
                                            .getTargetOntologyAcronym());
                                }

                                // Window.alert("Mapping count: "
                                // + CollectionUtils
                                // .asSortedList(ontologyAcronyms).size()
                                // + "");

                                searchService
                                        .searchOntologiesPredeterminedSet(
                                                ontologyAcronyms,
                                                new OntologySearchCallbackFactory(
                                                        graphView,
                                                        mappingCountResults));
                            }

                        });
    }

    public class OntologySearchCallbackFactory extends AbstractSearchCallbackFactory {
        private final View graphView;

        private final TotalMappingCount mappingCountResults;

        public OntologySearchCallbackFactory(View graphView,
                TotalMappingCount mappingCountResults) {
            super();
            this.graphView = graphView;
            this.mappingCountResults = mappingCountResults;
        }

        @Override
        public AbstractSearchCallback createSearchCallback(Fetch fetch) {
            return new OntologyDetailsCallback(graphView, mappingCountResults,
                    fetch);
        }
    }

    private class OntologyDetailsCallback extends AbstractSearchCallback {

        private final View graphView;

        private final TotalMappingCount mappingCounts;

        public OntologyDetailsCallback(View graphView,
                TotalMappingCount mappingCounts, Fetch fetch) {
            super(fetch);
            this.graphView = graphView;
            this.mappingCounts = mappingCounts;
        }

        @Override
        public void trackedFailure(Throwable caught) {
            // infoLabel.setText("Search failed for '" + searchTerm + "'");
            loggingErrorHandler.handleError(caught);
        }

        @Override
        public void trackedSuccess(Set<Resource> ontologyDetailsResults) {
            // The resources we have here are fully detailed ontology resources.
            // How do we resolve these against the ones we created based on the
            // mapping neighbourhood data?

            if (ontologyDetailsResults.isEmpty()) {
                // This would be unexpected, but nonetheless...
                // infoLabel.setText("No results found for search term '"
                // + searchTerm + "'");
                return;
            }

            // For use with scaling system, which is incomplete, in branch
            // issue240.
            // int minRawSize = 0;
            // int maxRawSize = 0;
            // for (Resource nodeResource : results) {
            // Integer size = (Integer) nodeResource
            // .getValue(Ontology.NUMBER_OF_CONCEPTS);
            // if (size > maxRawSize) {
            // maxRawSize = size;
            // } else if (size < minRawSize) {
            // minRawSize = size;
            // }
            // }
            // graph.getDisplayController().getNodeSizeTransformer()
            // .setScalingContextRange(minRawSize, maxRawSize);

            // TODO add convenience method to resourceSetFactory
            ResourceSet resourceSet = resourceSetFactory.createResourceSet();
            ArrayList<Resource> toAdd = new ArrayList<Resource>();
            for (Resource res : ontologyDetailsResults) {
                // Some results that appear in the mappings don't show up in
                // ontologies list. They should be the ones without Ontology Uri
                // set, or some other properties should be missing.
                if (res.containsProperty(Ontology.ONTOLOGY_URI)
                        && res.getValue(Ontology.ONTOLOGY_URI) != ""
                        && res.getValue(Ontology.ONTOLOGY_URI) != null) {
                    toAdd.add(res);
                }
            }
            resourceSet.addAll(toAdd);

            // Window.alert("Resource set contains " + resourceSet.size());
            graphView.getResourceModel().addResourceSet(resourceSet);

            // Before we use the third tier of API calls to get ontology
            // Descriptions
            // and possibly other data, take care of arcs that need to be added
            // to the graph. Now that all of the resources exist for the
            // neighbourhood, iterate through the results again to create the
            // links. This happens differently for concept mappings in
            // calculatePartialProperties(). See there for contrast.
            Map<String, Resource> itemIdMap = new HashMap<String, Resource>();
            for (Resource ontologyResource : ontologyDetailsResults) {
                itemIdMap.put(Ontology.getOntologyId(ontologyResource),
                        ontologyResource);
            }

            // Need the details callback next, which is triggered with the
            // automatic expander as they are added to the graph

            for (OntologyMappingCount mapping : mappingCounts) {
                Resource sourceResource = itemIdMap.get(mapping
                        .getSourceOntologyAcronym());
                Resource targetResource = itemIdMap.get(mapping
                        .getTargetOntologyAcronym());

                if (null == sourceResource || null == targetResource
                        || !resourceSet.contains(sourceResource)
                        || !resourceSet.contains(targetResource)) {
                    continue;
                }

                int mappingNumberOfConcepts = mapping.getSourceMappingCount();

                if (null != sourceResource && null != targetResource) {
                    UriList sourceOutgoing = sourceResource
                            .getUriListValue(Ontology.OUTGOING_MAPPINGS);

                    UriList targetIncoming = targetResource
                            .getUriListValue(Ontology.INCOMING_MAPPINGS);

                    // This is a memory leak, because if there are
                    // already arcs in here, and the corresponding
                    // nodes have been removed, they don't all get
                    // updated here. But maybe that is not a
                    // responsibility to be handled here?
                    sourceOutgoing.add(Ontology.toOntologyURIWithCount(
                            Ontology.getOntologyId(targetResource),
                            mappingNumberOfConcepts));
                    targetIncoming.add(Ontology.toOntologyURIWithCount(
                            Ontology.getOntologyId(sourceResource),
                            mappingNumberOfConcepts));

                    sourceResource.putValue(Ontology.OUTGOING_MAPPINGS,
                            sourceOutgoing);
                    targetResource.putValue(Ontology.INCOMING_MAPPINGS,
                            targetIncoming);
                }
            }

            graph.updateArcsForResources(ontologyDetailsResults);

            loadingBar.hide();
        }
    }

    protected LayoutAlgorithm getLayoutAlgorithm(ErrorHandler errorHandler) {
        /*
         * NOTE: we use null node animations for this embed because it is prone
         * to performance problems when trying to animate so many highly
         * interconnected nodes at the same time as loading the data.
         */
        // Radial and Force layouts can't run with typical ontology expansions.
        // Too many nodes! So use circle, then place the central node after the
        // layout is done.
        CircleLayoutWithCentralNodeAlgorithm layout = new CircleLayoutWithCentralNodeAlgorithm(
                errorHandler, this.nodeAnimator, centralOntologyUri);
        // new NodeAnimator(new NullNodeAnimationFactory()));
        layout.setAngleRange(MIN_ANGLE, MAX_ANGLE);

        return layout;
    }

    private void setLayoutAlgorithm(View graphView,
            LayoutAlgorithm layoutAlgorithm) {
        graphView.adaptTo(GraphLayoutSupport.class).registerDefaultLayout(
                layoutAlgorithm);
    }

    @Named(ChooselInjectionConstants.EMBED)
    @Inject
    protected ViewWindowContentProducer viewContentProducer;

    private NodeAnimator nodeAnimator;

    private Graph graph;

    private LoadingBarAssistant loadingBar;

    @Override
    public void loadView(ResourceSet virtualOntologies,
            List<String> ontologyAcroynms, IsWidget topBarWidget,
            AsyncCallback<IsWidget> callback) {

        String centralOntologyAcronym = ontologyAcroynms.get(0);

        this.centralOntologyUri = Ontology
                .toOntologyURI(centralOntologyAcronym);

        View graphView = ((ViewWindowContent) viewContentProducer
                .createWindowContent(GraphOntologyOverviewViewContentDisplayFactory.ID))
                .getView();

        // XXX likely to break when view content setup changes
        // get the error handler from the view content display
        // to show the errors in the view-specific error box (ListBox)
        DropEnabledViewContentDisplay cd1 = (DropEnabledViewContentDisplay) graphView
                .getModel().getViewContentDisplay();
        this.graph = (Graph) cd1.getDelegate();
        ErrorHandler errorHandler = graph.getErrorHandler();

        // Disable some automatic expanders
        graph.getExpanderRegistry()
                .removeAutomaticBulkExpander(Ontology.RESOURCE_URI_PREFIX,
                        OntologyNodeMappingExpander.class);

        // Turn off labels. These graphs tend ot have many nodes and arcs, and
        // the labels slow down rendering to problematic levels.
        graph.getDisplayController().setRenderArcLabels(false);

        graphView.addTopBarExtension(new LeftViewTopBarExtension(topBarWidget));

        loadingBar = new LoadingBarAssistant();
        loadingBar.initialize(graphView);

        graphView.init();
        nodeAnimator = graphView.adaptTo(GraphLayoutSupport.class)
                .getNodeAnimator();
        setLayoutAlgorithm(graphView, getLayoutAlgorithm(errorHandler));
        callback.onSuccess(graphView);

        doLoadData(centralOntologyAcronym, graphView, errorHandler);
    }
}
