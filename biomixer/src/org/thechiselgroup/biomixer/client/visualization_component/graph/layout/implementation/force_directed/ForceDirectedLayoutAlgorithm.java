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
package org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.force_directed;

import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.util.executor.DelayedExecutor;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutGraph;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.animations.NodeAnimator;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.AbstractLayoutAlgorithm;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation.AbstractLayoutComputation;

public class ForceDirectedLayoutAlgorithm extends AbstractLayoutAlgorithm {

    protected static final int DELAY_BETWEEN_ITERATIONS = 1;

    private final double damping;

    private ForceCalculator forceCalculator;

    /**
     * 
     * @param forceCalculator
     *            determines what forces are applied to nodes
     * @param damping
     *            coefficient between 0 and 1
     * @param errorHandler
     */
    public ForceDirectedLayoutAlgorithm(ForceCalculator forceCalculator,
            double damping, NodeAnimator nodeAnimator,
            DelayedExecutor executor, ErrorHandler errorHandler) {
        super(errorHandler, nodeAnimator, executor);
        this.forceCalculator = forceCalculator;
        this.damping = damping;

        executor.setDelay(DELAY_BETWEEN_ITERATIONS);

    }

    @Override
    protected AbstractLayoutComputation getLayoutComputation(LayoutGraph graph) {
        return new ForceDirectedLayoutComputation(forceCalculator, damping,
                graph, executor, errorHandler, nodeAnimator,
                DELAY_BETWEEN_ITERATIONS);
    }

}
