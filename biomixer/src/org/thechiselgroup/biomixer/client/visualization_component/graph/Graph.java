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
package org.thechiselgroup.biomixer.client.visualization_component.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.thechiselgroup.biomixer.client.core.command.CommandManager;
import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.geometry.DefaultSizeInt;
import org.thechiselgroup.biomixer.client.core.geometry.Point;
import org.thechiselgroup.biomixer.client.core.geometry.SizeInt;
import org.thechiselgroup.biomixer.client.core.persistence.Memento;
import org.thechiselgroup.biomixer.client.core.persistence.PersistableRestorationService;
import org.thechiselgroup.biomixer.client.core.resources.DefaultResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.resources.ResourceCategorizer;
import org.thechiselgroup.biomixer.client.core.resources.ResourceManager;
import org.thechiselgroup.biomixer.client.core.resources.ResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.UnionResourceSet;
import org.thechiselgroup.biomixer.client.core.resources.persistence.ResourceSetAccessor;
import org.thechiselgroup.biomixer.client.core.resources.persistence.ResourceSetCollector;
import org.thechiselgroup.biomixer.client.core.ui.SidePanelSection;
import org.thechiselgroup.biomixer.client.core.util.DataType;
import org.thechiselgroup.biomixer.client.core.util.NoSuchAdapterException;
import org.thechiselgroup.biomixer.client.core.util.animation.AnimationRunner;
import org.thechiselgroup.biomixer.client.core.util.animation.GwtAnimationRunner;
import org.thechiselgroup.biomixer.client.core.util.collections.CollectionFactory;
import org.thechiselgroup.biomixer.client.core.util.collections.Delta;
import org.thechiselgroup.biomixer.client.core.util.collections.LightweightCollection;
import org.thechiselgroup.biomixer.client.core.util.executor.GwtDelayedExecutor;
import org.thechiselgroup.biomixer.client.core.visualization.model.AbstractViewContentDisplay;
import org.thechiselgroup.biomixer.client.core.visualization.model.Slot;
import org.thechiselgroup.biomixer.client.core.visualization.model.ViewContentDisplayCallback;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItem;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemContainer;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemInteraction;
import org.thechiselgroup.biomixer.client.core.visualization.model.VisualItemInteraction.Type;
import org.thechiselgroup.biomixer.client.core.visualization.model.extensions.RequiresAutomaticResourceSet;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutAlgorithm;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.circle.CircleLayoutAlgorithm;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.force_directed.BoundsAwareAttractionCalculator;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.force_directed.BoundsAwareRepulsionCalculator;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.force_directed.CompositeForceCalculator;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.force_directed.ForceDirectedLayoutAlgorithm;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.tree.HorizontalTreeLayoutAlgorithm;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.tree.VerticalTreeLayoutAlgorithm;
import org.thechiselgroup.biomixer.client.visualization_component.graph.svg_widget.GraphDisplayController;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.GraphDisplay;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.GraphLayouts;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.Node;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.NodeDragEvent;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.NodeDragHandleMouseDownEvent;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.NodeDragHandleMouseDownHandler;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.NodeDragHandleMouseMoveEvent;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.NodeDragHandleMouseMoveHandler;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.NodeDragHandler;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.NodeEvent;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.NodeMenuItemClickedHandler;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.NodeMouseClickEvent;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.NodeMouseClickHandler;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.NodeMouseOutEvent;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.NodeMouseOutHandler;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.NodeMouseOverEvent;
import org.thechiselgroup.biomixer.client.visualization_component.graph.widget.NodeMouseOverHandler;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

// TODO separate out ncbo specific stuff and service calls
// TODO register listener for double click on node --> change expansion state
public class Graph extends AbstractViewContentDisplay implements
        RequiresAutomaticResourceSet, GraphLayoutSupport, GraphLayoutCallback {

    public static class DefaultDisplay extends GraphDisplayController implements
            GraphDisplay {

        // TODO why is size needed in the first place??
        public DefaultDisplay(ErrorHandler errorHandler) {
            this(400, 300, errorHandler);
        }

        public DefaultDisplay(int width, int height, ErrorHandler errorHandler) {
            super(width, height, errorHandler);
        }

    }

    private class GraphEventHandler implements NodeMouseOverHandler,
            NodeMouseOutHandler, NodeMouseClickHandler, MouseMoveHandler,
            NodeDragHandler, NodeDragHandleMouseDownHandler,
            NodeDragHandleMouseMoveHandler {

        /**
         * Node that the mouse is currently over. Set by mouse out and mouse
         * over. Not over a node if null.
         */
        private Node currentNode = null;

        @Override
        public void onDrag(NodeDragEvent event) {
            commandManager.execute(new MoveNodeCommand(graphDisplay, event
                    .getNode(),
                    new Point(event.getStartX(), event.getStartY()), new Point(
                            event.getEndX(), event.getEndY())));
        }

        @Override
        public void onMouseClick(NodeMouseClickEvent event) {
            reportInteraction(Type.CLICK, event);
        }

        @Override
        public void onMouseDown(NodeDragHandleMouseDownEvent event) {
            reportInteraction(Type.MOUSE_DOWN, event);
        }

        @Override
        public void onMouseMove(MouseMoveEvent event) {
            if (currentNode != null) {
                getVisualItem(currentNode).reportInteraction(
                        new VisualItemInteraction(Type.MOUSE_MOVE, event
                                .getClientX(), event.getClientY()));
            }
        }

        @Override
        public void onMouseMove(NodeDragHandleMouseMoveEvent event) {
            reportInteraction(Type.MOUSE_MOVE, event);
        }

        @Override
        public void onMouseOut(NodeMouseOutEvent event) {
            currentNode = null;
            reportInteraction(Type.MOUSE_OUT, event);
        }

        @Override
        public void onMouseOver(NodeMouseOverEvent event) {
            currentNode = event.getNode();
            reportInteraction(Type.MOUSE_OVER, event);
        }

        private void reportInteraction(Type eventType, NodeEvent<?> event) {
            int clientX = event.getMouseX() + asWidget().getAbsoluteLeft();
            int clientY = event.getMouseY() + asWidget().getAbsoluteTop();

            getVisualItem(event).reportInteraction(
                    new VisualItemInteraction(eventType, clientX, clientY));
        }

    }

    public class GraphLayoutAction implements ViewContentDisplayAction {

        private final LayoutAlgorithm layoutAlgorithm;

        private final String layoutLabel;

        public GraphLayoutAction(String layoutLabel,
                LayoutAlgorithm layoutAlgorithm) {
            this.layoutLabel = layoutLabel;
            this.layoutAlgorithm = layoutAlgorithm;
        }

        @Override
        public void execute() {
            commandManager.execute(new GraphLayoutCommand(graphDisplay,
                    layoutAlgorithm, layoutLabel, getAllNodes()));
        }

        @Override
        public String getLabel() {
            return layoutLabel;
        }
    }

    public static final Slot NODE_BORDER_COLOR = new Slot("nodeBorderColor",
            "Node Border Color", DataType.COLOR);

    public static final Slot NODE_BACKGROUND_COLOR = new Slot(
            "nodeBackgroundColor", "Node Color", DataType.COLOR);

    public static final Slot NODE_LABEL_SLOT = new Slot("nodeLabel",
            "Node Label", DataType.TEXT);

    public final static String ID = "org.thechiselgroup.choosel.visualization_component.graph.Graph";

    private static final String MEMENTO_ARC_ITEM_CONTAINERS_CHILD = "arcItemContainers";

    private static final String MEMENTO_NODE_LOCATIONS_CHILD = "nodeLocations";

    private static final String MEMENTO_X = "x";

    private static final String MEMENTO_Y = "y";

    // TODO move
    public static String getArcId(String arcType, String sourceId,
            String targetId) {
        // FIXME this needs escaping of special characters to work properly
        return arcType + ":" + sourceId + "_" + targetId;
    }

    private final ArcTypeProvider arcStyleProvider;

    private final CommandManager commandManager;

    // advanced node class: (incoming, outgoing, expanded: state machine)

    private final GraphDisplay graphDisplay;

    private final GraphExpansionRegistry registry;

    private final ResourceCategorizer resourceCategorizer;

    private final ResourceManager resourceManager;

    private final UnionResourceSet nodeResources = new UnionResourceSet(
            new DefaultResourceSet());

    private final Map<String, ArcItemContainer> arcItemContainersByArcTypeID = CollectionFactory
            .createStringMap();

    private ResourceSet automaticResources;

    /*
     * TODO The callback is meant to check whether the graph is initialized (and
     * not disposed) when methods are called (to prevent errors in asynchronous
     * callbacks that return after the graph has been disposed or before it has
     * been initialized).
     */
    private final GraphNodeExpansionCallback expansionCallback = new GraphNodeExpansionCallback() {

        @Override
        public void addAutomaticResource(Resource resource) {
            Graph.this.addAutomaticResource(resource);
        }

        @Override
        public boolean containsResourceWithUri(String resourceUri) {
            return Graph.this.containsResourceWithUri(resourceUri);
        }

        @Override
        public String getCategory(Resource resource) {
            return Graph.this.getCategory(resource);
        }

        @Override
        public GraphDisplay getDisplay() {
            return Graph.this.getDisplay();
        }

        @Override
        public Resource getResourceByUri(String value) {
            return Graph.this.getResourceByUri(value);
        }

        @Override
        public ResourceManager getResourceManager() {
            return Graph.this.getResourceManager();
        }

        @Override
        public LightweightCollection<VisualItem> getVisualItems(
                Iterable<Resource> resources) {
            return Graph.this.getVisualItems(resources);
        }

        @Override
        public boolean isInitialized() {
            return Graph.this.isInitialized();
        }

        @Override
        public boolean isRestoring() {
            return Graph.this.isRestoring();
        }

        @Override
        public void updateArcsForResources(Iterable<Resource> resources) {
            Graph.this.updateArcsForResources(resources);
        }

        @Override
        public void updateArcsForVisuaItems(
                LightweightCollection<VisualItem> visualItems) {
            Graph.this.updateArcsForVisuaItems(visualItems);
        }
    };

    private ErrorHandler errorHandler;

    @Inject
    public Graph(GraphDisplay display, CommandManager commandManager,
            ResourceManager resourceManager,
            ResourceCategorizer resourceCategorizer,
            ArcTypeProvider arcStyleProvider, GraphExpansionRegistry registry,
            ErrorHandler errorHandler) {

        assert display != null;
        assert commandManager != null;
        assert resourceManager != null;
        assert resourceCategorizer != null;
        assert arcStyleProvider != null;
        assert registry != null;
        assert errorHandler != null;

        this.arcStyleProvider = arcStyleProvider;
        this.resourceCategorizer = resourceCategorizer;
        graphDisplay = display;
        // didn't want to change GraphDisplay's interface yet
        if (graphDisplay instanceof GraphDisplayController) {
            addResizeListener((GraphDisplayController) graphDisplay);
        }
        this.commandManager = commandManager;
        this.resourceManager = resourceManager;
        this.registry = registry;

        /*
         * we init the arc type containers early so they are available for UI
         * customization in Choosel applications.
         */
        initArcTypeContainers();
        this.errorHandler = errorHandler;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T adaptTo(Class<T> clazz) throws NoSuchAdapterException {
        if (GraphLayoutSupport.class.equals(clazz)) {
            return (T) this;
        }

        return super.adaptTo(clazz);
    }

    private void addAutomaticResource(Resource resource) {
        automaticResources.add(resource);
    }

    private boolean containsResourceWithUri(String resourceUri) {
        return nodeResources.containsResourceWithUri(resourceUri);
    }

    private NodeItem createGraphNodeItem(VisualItem visualItem) {
        // TODO get from group id
        String type = getCategory(visualItem.getResources().getFirstElement());

        NodeItem graphItem = new NodeItem(visualItem, type, graphDisplay);

        /*
         * NOTE: When the node is added, a LayoutGraphContentChangedEvent will
         * be fired (see DefaultLayoutGraph), causing the current layout
         * algorithm to be run.
         */
        graphDisplay.addNode(graphItem.getNode());

        // TODO re-enable
        // TODO remove once new drag and drop mechanism works...
        graphDisplay
                .setNodeStyle(graphItem.getNode(), "showDragImage", "false");

        graphDisplay.setNodeStyle(graphItem.getNode(), "showArrow", registry
                .getNodeMenuEntries(type).isEmpty() ? "false" : "true");

        nodeResources.addResourceSet(visualItem.getResources());
        visualItem.setDisplayObject(graphItem);

        /*
         * NOTE: all node configuration should be done when calling the
         * automatic expanders, since they rely on returning the correct graph
         * contents etc.
         * 
         * NOTE: we do not execute the expanders if we are restoring the graph
         */
        registry.getAutomaticExpander(type).expand(visualItem,
                expansionCallback);

        return graphItem;
    }

    // TODO encapsulate in display, use dependency injection
    @Override
    protected Widget createWidget() {
        return graphDisplay.asWidget();
    }

    // TODO better caching?
    // default visibility for test case use
    List<Node> getAllNodes() {
        List<Node> result = new ArrayList<Node>();
        for (VisualItem visualItem : getVisualItems()) {
            result.add(getNode(visualItem));
        }
        return result;
    }

    public ResourceSet getAllResources() {
        return nodeResources;
    }

    public ArcItemContainer getArcItemContainer(String arcTypeID) {
        assert arcTypeID != null;
        assert arcItemContainersByArcTypeID.containsKey(arcTypeID);

        return arcItemContainersByArcTypeID.get(arcTypeID);
    }

    public Iterable<ArcItemContainer> getArcItemContainers() {
        return arcItemContainersByArcTypeID.values();
    }

    // TODO remove if not used
    private ArcItem[] getArcItems() {
        Iterable<ArcItemContainer> arcItemContainers = getArcItemContainers();
        int size = 0;
        for (ArcItemContainer arcItemContainer : arcItemContainers) {
            size += arcItemContainer.getArcItems().size();
        }
        ArcItem[] arcs = new ArcItem[size];
        int i = 0;
        for (ArcItemContainer arcItemContainer : arcItemContainers) {
            for (ArcItem arcItem : arcItemContainer.getArcItems()) {
                arcs[i++] = arcItem;
            }
        }
        return arcs;
    }

    private String getCategory(Resource resource) {
        return resourceCategorizer.getCategory(resource);
    }

    private GraphDisplay getDisplay() {
        return graphDisplay;
    }

    @Override
    public SizeInt getDisplayArea() {
        Widget displayWidget = graphDisplay.asWidget();
        int height = displayWidget.getOffsetHeight();
        int width = displayWidget.getOffsetWidth();
        return new DefaultSizeInt(width, height);
    }

    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    @Override
    public Point getLocation(NodeItem nodeItem) {
        return graphDisplay.getLocation(nodeItem.getNode());
    }

    @Override
    public String getName() {
        return "Graph";
    }

    private Node getNode(VisualItem visualItem) {
        return visualItem.<NodeItem> getDisplayObject().getNode();
    }

    // TODO remove if not used
    private NodeItem[] getNodeItems() {
        LightweightCollection<VisualItem> visualItems = getVisualItems();
        NodeItem[] nodeItems = new NodeItem[visualItems.size()];
        int i = 0;
        for (VisualItem visualItem : visualItems) {
            nodeItems[i++] = visualItem.getDisplayObject();
        }
        return nodeItems;
    }

    public Resource getResourceByUri(String value) {
        return nodeResources.getByUri(value);
    }

    public ResourceManager getResourceManager() {
        return resourceManager;
    }

    @Override
    public SidePanelSection[] getSidePanelSections() {
        List<ViewContentDisplayAction> actions = new ArrayList<ViewContentDisplayAction>();
        // TODO cleanup
        AnimationRunner animationRunner = new GwtAnimationRunner();
        actions.add(new GraphLayoutAction(GraphLayouts.CIRCLE_LAYOUT,
                new CircleLayoutAlgorithm(errorHandler, animationRunner)));
        actions.add(new GraphLayoutAction(GraphLayouts.HORIZONTAL_TREE_LAYOUT,
                new HorizontalTreeLayoutAlgorithm(true, errorHandler,
                        animationRunner)));
        actions.add(new GraphLayoutAction(GraphLayouts.VERTICAL_TREE_LAYOUT,
                new VerticalTreeLayoutAlgorithm(true, errorHandler,
                        animationRunner)));
        actions.add(new GraphLayoutAction(GraphLayouts.FORCE_DIRECTED_LAYOUT,
                new ForceDirectedLayoutAlgorithm(new CompositeForceCalculator(
                        new BoundsAwareAttractionCalculator(graphDisplay
                                .getLayoutGraph()),
                        new BoundsAwareRepulsionCalculator(graphDisplay
                                .getLayoutGraph())), 0.9, animationRunner,
                        new GwtDelayedExecutor(), errorHandler)));

        VerticalPanel layoutPanel = new VerticalPanel();
        for (final ViewContentDisplayAction action : actions) {
            Button w = new Button(action.getLabel());
            w.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    action.execute();
                }
            });
            layoutPanel.add(w);
        }

        return new SidePanelSection[] { new SidePanelSection("Layouts",
                layoutPanel), };
    }

    @Override
    public Slot[] getSlots() {
        return new Slot[] { NODE_LABEL_SLOT, NODE_BORDER_COLOR,
                NODE_BACKGROUND_COLOR };
    }

    private VisualItem getVisualItem(Node node) {
        return getVisualItem(node.getId());
    }

    private VisualItem getVisualItem(NodeEvent<?> event) {
        return getVisualItem(event.getNode());
    }

    @Override
    public void init(VisualItemContainer container,
            ViewContentDisplayCallback callback) {

        super.init(container, callback);

        initStateChangeHandlers();
    }

    private void initArcTypeContainers() {
        for (ArcType arcType : arcStyleProvider.getArcTypes()) {
            arcItemContainersByArcTypeID.put(arcType.getArcTypeID(),
                    new ArcItemContainer(arcType, graphDisplay, this));
        }
    }

    private void initNodeMenuItems() {
        for (Entry<String, List<NodeMenuEntry>> entry : registry
                .getNodeMenuEntriesByCategory()) {

            String category = entry.getKey();
            for (NodeMenuEntry nodeMenuEntry : entry.getValue()) {
                registerNodeMenuItem(category, nodeMenuEntry.getLabel(),
                        nodeMenuEntry.getExpander());
            }
        }
    }

    private void initStateChangeHandlers() {
        GraphEventHandler handler = new GraphEventHandler();
        graphDisplay
                .addEventHandler(NodeDragHandleMouseDownEvent.TYPE, handler);
        graphDisplay.addEventHandler(NodeMouseOverEvent.TYPE, handler);
        graphDisplay.addEventHandler(NodeMouseOutEvent.TYPE, handler);
        graphDisplay.addEventHandler(NodeMouseClickEvent.TYPE, handler);
        graphDisplay.addEventHandler(NodeDragEvent.TYPE, handler);
        graphDisplay.addEventHandler(MouseMoveEvent.getType(), handler);

        initNodeMenuItems();
    }

    @Override
    public boolean isAdaptableTo(Class<?> clazz) {
        if (GraphLayoutSupport.class.equals(clazz)) {
            return true;
        }

        return super.isAdaptableTo(clazz);
    }

    @Override
    public void registerDefaultLayout(LayoutAlgorithm layoutAlgorithm) {
        graphDisplay.registerDefaultLayoutAlgorithm(layoutAlgorithm);
    }

    private void registerNodeMenuItem(String category, String menuLabel,
            final GraphNodeExpander nodeExpander) {

        graphDisplay.addNodeMenuItemHandler(menuLabel,
                new NodeMenuItemClickedHandler() {
                    @Override
                    public void onNodeMenuItemClicked(Node node) {
                        nodeExpander.expand(getVisualItem(node),
                                expansionCallback);
                    }
                }, category);
    }

    private void removeGraphNode(VisualItem visualItem) {
        assert visualItem != null;

        nodeResources.removeResourceSet(visualItem.getResources());
        for (ArcItemContainer arcItemContainer : arcItemContainersByArcTypeID
                .values()) {
            arcItemContainer.removeVisualItem(visualItem);
        }
        graphDisplay.removeNode(getNode(visualItem));
    }

    @Override
    public void restore(Memento state,
            PersistableRestorationService restorationService,
            ResourceSetAccessor accessor) {

        restoreArcItemContainers(restorationService, accessor,
                state.getChild(MEMENTO_ARC_ITEM_CONTAINERS_CHILD));
        restoreNodeLocations(state.getChild(MEMENTO_NODE_LOCATIONS_CHILD));
    }

    private void restoreArcItemContainers(
            PersistableRestorationService restorationService,
            ResourceSetAccessor accessor, Memento child) {
        for (Entry<String, Memento> entry : child.getChildren().entrySet()) {
            arcItemContainersByArcTypeID.get(entry.getKey()).restore(
                    entry.getValue(), restorationService, accessor);
        }
    }

    private void restoreNodeLocations(Memento state) {
        for (VisualItem visualItem : getVisualItems()) {
            NodeItem item = visualItem.getDisplayObject();
            Memento nodeMemento = state.getChild(visualItem.getId());
            Point location = new Point(
                    (Integer) nodeMemento.getValue(MEMENTO_X),
                    (Integer) nodeMemento.getValue(MEMENTO_Y));

            setLocation(item, location);
        }
    }

    @Override
    public void runLayout() {
        graphDisplay.runLayout();
    }

    @Override
    public void runLayout(LayoutAlgorithm layoutAlgorithm) {
        graphDisplay.runLayout(layoutAlgorithm);
    }

    @Override
    public Memento save(ResourceSetCollector resourceSetCollector) {
        Memento result = new Memento();

        result.addChild(MEMENTO_NODE_LOCATIONS_CHILD, saveNodeLocations());
        result.addChild(MEMENTO_ARC_ITEM_CONTAINERS_CHILD,
                saveArcTypeContainers(resourceSetCollector));

        return result;
    }

    private Memento saveArcTypeContainers(
            ResourceSetCollector resourceSetCollector) {

        Memento memento = new Memento();
        for (Entry<String, ArcItemContainer> entry : arcItemContainersByArcTypeID
                .entrySet()) {
            memento.addChild(entry.getKey(),
                    entry.getValue().save(resourceSetCollector));
        }
        return memento;
    }

    private Memento saveNodeLocations() {
        Memento state = new Memento();

        for (VisualItem visualItem : getVisualItems()) {
            NodeItem nodeItem = visualItem.getDisplayObject();

            Point location = graphDisplay.getLocation(nodeItem.getNode());

            Memento nodeMemento = new Memento();
            nodeMemento.setValue(MEMENTO_X, location.getX());
            nodeMemento.setValue(MEMENTO_Y, location.getY());

            state.addChild(visualItem.getId(), nodeMemento);
        }
        return state;
    }

    /**
     * If the arc type becomes invisible, all arcs of this arcType from the view
     * and arcs of this arc type are not shown any more. If the arc types
     * becomes visible, all arcs of this type are added.
     */
    // TODO expose arc type configurations and use listener mechanism
    public void setArcTypeVisible(String arcTypeId, boolean visible) {
        assert arcTypeId != null;
        assert arcItemContainersByArcTypeID.containsKey(arcTypeId);

        arcItemContainersByArcTypeID.get(arcTypeId).setVisible(visible);
    }

    @Override
    public void setAutomaticResources(ResourceSet automaticResources) {
        this.automaticResources = automaticResources;
    }

    @Override
    public void setLocation(NodeItem node, Point location) {
        graphDisplay.setLocation(node.getNode(), location);
    }

    @Override
    public void update(Delta<VisualItem> delta,
            LightweightCollection<Slot> updatedSlots) {

        for (VisualItem addedItem : delta.getAddedElements()) {
            createGraphNodeItem(addedItem);
            updateNode(addedItem);
        }

        updateArcsForVisuaItems(delta.getAddedElements());

        for (VisualItem updatedItem : delta.getUpdatedElements()) {
            updateNode(updatedItem);
        }

        for (VisualItem visualItem : delta.getRemovedElements()) {
            removeGraphNode(visualItem);
        }

        if (!updatedSlots.isEmpty()) {
            for (VisualItem visualItem : getVisualItems()) {
                updateNode(visualItem);
            }
        }
    }

    public void updateArcsForResources(Iterable<Resource> resources) {
        updateArcsForVisuaItems(getVisualItems(resources));
    }

    public void updateArcsForVisuaItems(
            LightweightCollection<VisualItem> visualItems) {
        assert visualItems != null;
        for (ArcItemContainer container : arcItemContainersByArcTypeID.values()) {
            container.update(visualItems);
        }
    }

    private void updateNode(VisualItem visualItem) {
        visualItem.<NodeItem> getDisplayObject().updateNode();
    }

}