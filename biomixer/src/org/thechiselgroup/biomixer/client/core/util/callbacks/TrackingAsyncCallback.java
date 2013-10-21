package org.thechiselgroup.biomixer.client.core.util.callbacks;

import org.thechiselgroup.biomixer.client.services.Fetch;
import org.thechiselgroup.biomixer.client.services.UrlFetchRegistry;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Using this instead of AsyncCallback allows us to track the number of
 * asynchronous REST calls made, and to find how many have responses or are
 * timed out. This information may be used to control a coutner or progress meter.
 */
abstract public class TrackingAsyncCallback<T> implements AsyncCallback<T> {

    private Fetch fetch;

    protected final UrlFetchRegistry urlFetchRegistry = UrlFetchRegistry
            .getSingleton();

    public TrackingAsyncCallback(Fetch fetch) {
        this.fetch = fetch;
        urlFetchRegistry.addFetch(fetch);
    }

    @Override
    final public void onFailure(Throwable caught) {
        if (null != fetch) {
            fetch.completeFetch();
        }
        trackedFailure(caught);
    }

    @Override
    final public void onSuccess(T result) {
        if (null != fetch) {
            fetch.completeFetch();
        }
        trackedSuccess(result);
    }

    abstract public void trackedFailure(Throwable caught);

    abstract public void trackedSuccess(T result);
}
