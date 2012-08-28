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
package org.thechiselgroup.biomixer.client.core.command;

/**
 * Allows commands (blocks of code) to be encapsulated and then executed later
 * with a given parameter.
 * 
 * @author drusk
 * 
 * @param <T>
 */
public interface ParameterizedCommand<T> {

    /**
     * Executes the command with the given input parameter.
     * 
     * @param param
     *            input parameter for command.
     */
    void execute(T param);

}
