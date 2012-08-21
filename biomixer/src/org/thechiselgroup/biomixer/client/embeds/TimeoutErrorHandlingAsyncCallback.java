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
package org.thechiselgroup.biomixer.client.embeds;

import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandler;
import org.thechiselgroup.biomixer.client.core.error_handling.ErrorHandlingAsyncCallback;
import org.thechiselgroup.biomixer.client.visualization_component.graph.CallbackException;

public abstract class TimeoutErrorHandlingAsyncCallback<T> extends
        ErrorHandlingAsyncCallback<T> {

    public TimeoutErrorHandlingAsyncCallback(ErrorHandler errorHandler) {
        super(errorHandler);
    }

    protected abstract String getMessage(Throwable caught);

    @Override
    protected final Throwable wrapException(Throwable caught) {
        return new CallbackException(getMessage(caught), caught);
    }
}