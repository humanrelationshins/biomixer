package org.thechiselgroup.biomixer.client;

import java.util.Set;

import org.thechiselgroup.biomixer.client.core.resources.Resource;
import org.thechiselgroup.biomixer.client.core.util.callbacks.TrackingAsyncCallback;
import org.thechiselgroup.biomixer.client.services.Fetch;

public abstract class AbstractSearchCallback extends
        TrackingAsyncCallback<Set<Resource>> {

    public AbstractSearchCallback(Fetch fetch) {
        super(fetch);
    }

}
