package org.thechiselgroup.biomixer.client.core.error_handling;

import org.thechiselgroup.biomixer.client.core.util.callbacks.TrackingAsyncCallback;
import org.thechiselgroup.biomixer.client.workbench.util.url.JsonpUrlFetchService;

import com.google.gwt.jsonp.client.TimeoutException;

public class RetryAsyncCallbackErrorHandler extends AsyncCallbackErrorHandler {

    private final int prevNumberOfTries;

    private final String url;

    private final TrackingAsyncCallback<String> fetchCallback;

    private final JsonpUrlFetchService urlFetchService;

    static private final int MAX_NUMBER_OF_TRIES = 3;

    public RetryAsyncCallbackErrorHandler(
            TrackingAsyncCallback<String> callback, final String url,
            int prevNumberOfTries, JsonpUrlFetchService fetchService) {
        super(callback);
        this.prevNumberOfTries = prevNumberOfTries;
        this.url = url;
        this.fetchCallback = callback;
        this.urlFetchService = fetchService;
    }

    @Override
    public void handleError(Throwable error) {
        if (error.getClass().equals(TimeoutException.class)
                && prevNumberOfTries <= Math.max(MAX_NUMBER_OF_TRIES, 1)) {
            // Re-trigger the call
            urlFetchService.fetchURL(url, fetchCallback, prevNumberOfTries + 1);
        } else if (1 == 0) {
            // Deal with 500 server errors
            urlFetchService.fetchURL(url, fetchCallback, prevNumberOfTries + 1);
        } else {
            // Window.alert("Retried " + prevNumberOfTries
            // + " and Eric sees an error num: " + error.getCause()
            // + " class " + error.getClass() + " error message: "
            // + error.getMessage());
            // Window.alert("Investigating error " + error.getClass() + " "
            // + error.getMessage());
            super.handleError(error);
        }
    }

    /**
     * Use this in response to error codes that do not necessarily reflect real
     * world errors. Maybe this is not necessary.
     * 
     * @param exception
     * 
     */
    public boolean manualRetry() {
        if (prevNumberOfTries <= Math.max(MAX_NUMBER_OF_TRIES, 1)) {
            // Re-trigger the call
            urlFetchService.fetchURL(url, fetchCallback, prevNumberOfTries + 1);
            return true;
        }
        return false;
    }

}
