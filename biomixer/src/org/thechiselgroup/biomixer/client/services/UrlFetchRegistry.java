package org.thechiselgroup.biomixer.client.services;

import java.util.HashMap;
import java.util.Map;

public class UrlFetchRegistry {

    private static UrlFetchRegistry singleton;

    Map<String, Fetch> registryMap = new HashMap<String, Fetch>();

    private int totalFetchesRequested = 0;

    private int completedFetches = 0;

    private UrlFetchRegistry() {

    }

    public void addFetch(Fetch fetch) {
        // Add fetch to registry, and give fetch reference to registry.
        fetch.registeredWith(this);
        this.registryMap.put(fetch.getUrl(), fetch);
        this.totalFetchesRequested++;
    }

    public void completeFetch(Fetch fetch) {
        this.completedFetches++;
    }

    public int getTotalRequested() {
        return this.totalFetchesRequested;
    }

    public int getTotalCompleted() {
        return this.completedFetches;
    }

    /**
     * Whenever it seems important to reset, such as when all expected calls are
     * returned from a load routine.
     */
    public void resetCounts() {
        this.completedFetches = 0;
        this.totalFetchesRequested = 0;
    }

    public static UrlFetchRegistry getSingleton() {
        if (null == singleton) {
            singleton = new UrlFetchRegistry();
        }
        return singleton;
    }

}
