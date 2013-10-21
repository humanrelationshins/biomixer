package org.thechiselgroup.biomixer.client.core.error_handling;

import org.thechiselgroup.biomixer.client.core.util.callbacks.TrackingAsyncCallback;

/**
 * 
 * @author everbeek
 * 
 */
public class AsyncCallbackErrorHandler implements ErrorHandler {

    private final TrackingAsyncCallback<?> callback;

    public AsyncCallbackErrorHandler(TrackingAsyncCallback<?> callback) {
        assert callback != null;

        this.callback = callback;
    }

    @Override
    public void handleError(Throwable error) {
        callback.onFailure(error);
    }
}