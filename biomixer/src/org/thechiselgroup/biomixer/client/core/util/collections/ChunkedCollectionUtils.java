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
package org.thechiselgroup.biomixer.client.core.util.collections;

import org.thechiselgroup.biomixer.client.core.command.ParameterizedCommand;

import com.google.gwt.user.client.Timer;

/**
 * Allows loops to be "chunked" so that they only execute a portion before using
 * a timer to relinquish control to the browser. This should be used on loops
 * that might be very long in order to reduce the likelihood of the browser
 * becoming unresponsive.
 * 
 * @author drusk
 * 
 */
public final class ChunkedCollectionUtils {

    /**
     * Performs a chunked for-each loop.
     * 
     * @param loopItems
     *            the items to iterate over
     * @param loopAction
     *            the action to perform on each item
     * @param chunkSize
     *            the number of items to process before taking a break
     * @param breakTime
     *            the length of each break (in milliseconds)
     */
    public static <T> void forEach(Iterable<T> loopItems,
            ParameterizedCommand<T> loopAction, int chunkSize, int breakTime) {

        int currentIndex = 0;
        for (T item : loopItems) {
            if (currentIndex % chunkSize == 0) {
                takeBreak(breakTime);
            }

            loopAction.execute(item);

            currentIndex++;
        }
    }

    /**
     * Temporarily relinquishes control to the browser.
     * 
     * @param breakTime
     *            the length of the break (in milliseconds)
     */
    private static void takeBreak(int breakTime) {
        new Timer() {
            @Override
            public void run() {
                return;
            }
        }.schedule(breakTime);
    }

}
