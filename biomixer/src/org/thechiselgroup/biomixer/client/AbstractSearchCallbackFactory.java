package org.thechiselgroup.biomixer.client;

import org.thechiselgroup.biomixer.client.services.Fetch;

public abstract class AbstractSearchCallbackFactory {

    public AbstractSearchCallbackFactory() {
    }

    abstract public AbstractSearchCallback createSearchCallback(Fetch fetch);
}