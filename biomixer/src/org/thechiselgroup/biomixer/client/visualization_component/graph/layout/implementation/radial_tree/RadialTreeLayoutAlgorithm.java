package org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.radial_tree;

import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutGraph;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.animations.NodeAnimator;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.AbstractLayoutAlgorithm;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.AbstractLayoutComputation;

public class RadialTreeLayoutAlgorithm extends AbstractLayoutAlgorithm {

    private final boolean leafsToCenter;

    public RadialTreeLayoutAlgorithm(boolean leafsToCenter,
            ErrorHandler errorHandler, NodeAnimator nodeAnimator) {
        super(errorHandler, nodeAnimator);
        this.leafsToCenter = leafsToCenter;
    }

    @Override
    protected AbstractLayoutComputation getLayoutComputation(LayoutGraph graph) {
        return new RadialTreeLayoutComputation(graph, executor, errorHandler,
                nodeAnimator, leafsToCenter);
    }

}
