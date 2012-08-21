/*******************************************************************************
 * Copyright 2012 Lars Grammel, David Rusk 
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
package org.thechiselgroup.biomixer.client.visualization_component.graph.layout.implementation;

import java.util.ArrayList;
import java.util.List;

import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.geometry.PointDouble;
import org.thechiselgroup.biomixer.client.core.util.animation.AnimationRunner;
import org.thechiselgroup.biomixer.client.core.util.executor.Executor;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutComputation;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutComputationFinishedEvent;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutComputationFinishedHandler;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutGraph;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.LayoutNode;
import org.thechiselgroup.biomixer.client.visualization_component.graph.layout.animations.NodeAnimator;

import com.google.web.bindery.event.shared.HandlerRegistration;

/**
 * <p>
 * Abstract layout computation that deals with managing event handlers, running
 * state, etc. It can only be run once (create and then call {@link #run()}).
 * </p>
 * <p>
 * Subclasses just need to implement {@link #computeIteration()}.
 * </p>
 * 
 * @author Lars Grammel
 */
public abstract class AbstractLayoutComputation implements LayoutComputation,
        Runnable {

    private final List<LayoutComputationFinishedHandler> eventHandlers = new ArrayList<LayoutComputationFinishedHandler>();

    protected final LayoutGraph graph;

    private boolean running = true;

    /**
     * Set to true if the algorithm should stop.
     */
    protected boolean shouldStop = false;

    private final Executor executor;

    private final ErrorHandler errorHandler;

    private NodeAnimator nodeAnimator;

    public AbstractLayoutComputation(LayoutGraph graph, Executor executor,
            ErrorHandler errorHandler, AnimationRunner animationRunner) {

        assert graph != null;
        assert executor != null;
        assert errorHandler != null;

        this.errorHandler = errorHandler;
        this.executor = executor;
        this.graph = graph;
        this.nodeAnimator = new NodeAnimator(animationRunner);
    }

    @Override
    public HandlerRegistration addEventHandler(
            final LayoutComputationFinishedHandler handler) {

        assert handler != null;

        eventHandlers.add(handler);

        return new HandlerRegistration() {
            @Override
            public void removeHandler() {
                eventHandlers.remove(handler);
            }
        };
    }

    protected void animateTo(LayoutNode node, PointDouble location, int duration) {
        nodeAnimator.animateNodeTo(node, location, duration);
    }

    /**
     * Computes an iteration of the graph layout. For most algorithms, there
     * just needs to be a single iteration, but multiple iterations can be used
     * if the algorithm takes a long time to run or if it is continuous.
     * Splitting the algorithm into iterations helps maintaining a responsive UI
     * in single-threaded environments (e.g. JavaScript). The next iteration
     * will only be started if the computation has not been aborted in the
     * meantime.
     * 
     * @return true, if the algorithm will need more iterations to complete,
     *         false otherwise.
     * 
     * @throws RuntimeException
     *             Something went wrong while calculating the layout.
     */
    protected abstract boolean computeIteration() throws RuntimeException;

    private void fireFinishedEvent(RuntimeException ex) {
        LayoutComputationFinishedEvent event = new LayoutComputationFinishedEvent(
                this, shouldStop, ex);

        for (LayoutComputationFinishedHandler handler : eventHandlers) {
            handler.onLayoutComputationFinished(event);
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public void restart() {
        running = true;
        shouldStop = false;
        run();
    }

    /**
     * Computes an iteration and schedules another iteration if needed. Fires
     * the finished event once done. Should only be called once to trigger the
     * layout calculation (further calls happen automatically).
     */
    @Override
    public void run() {
        try {
            if (running && !shouldStop && computeIteration()) {
                executor.execute(this);
            } else {
                fireFinishedEvent(null);
                running = false;
            }
        } catch (RuntimeException ex) {
            errorHandler.handleError(ex);
            fireFinishedEvent(ex);
            running = false;
        }
    }

    @Override
    public void stop() {
        shouldStop = true;
    }

}