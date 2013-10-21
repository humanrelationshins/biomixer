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
package org.thechiselgroup.biomixer.client.core.util.url;

import java.util.logging.Logger;

import org.thechiselgroup.biomixer.client.core.configuration.ChooselInjectionConstants;
import org.thechiselgroup.biomixer.client.core.error_handling.LoggerProvider;
import org.thechiselgroup.biomixer.client.core.util.callbacks.TrackingAsyncCallback;
import org.thechiselgroup.biomixer.client.services.Fetch;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class ProfilingUrlFetchServiceDecorator implements UrlFetchService {

    private UrlFetchService delegate;

    private Logger logger;

    @Inject
    public ProfilingUrlFetchServiceDecorator(
            @Named(ChooselInjectionConstants.DELEGATE) UrlFetchService delegate,
            LoggerProvider loggerProvider) {

        this.delegate = delegate;
        this.logger = loggerProvider.getLogger();
    }

    @Override
    public void fetchURL(final String url,
            final TrackingAsyncCallback<String> callback) {
        final long start = System.currentTimeMillis();
        logger.info("fetch url '" + url + "'");

        delegate.fetchURL(url,
                new TrackingAsyncCallback<String>(new Fetch(url)) {
                    @Override
                    public void trackedFailure(Throwable caught) {
                        logger.info("fetch url '" + url + "' failed after "
                                + (System.currentTimeMillis() - start) + "ms");
                        callback.onFailure(caught);
                    }

                    @Override
                    public void trackedSuccess(String result) {
                        logger.info("fetch url '" + url + "' succeeded after "
                                + (System.currentTimeMillis() - start) + "ms");
                        callback.onSuccess(result);
                    }
                });
    }

}