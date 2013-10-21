package org.thechiselgroup.biomixer.client.services;


/**
 * Used to track the number of url fetches we have made and are waiting for.
 * This information can be used to control a progress meter, since most network
 * operations take more time than the browser processing of results.
 * 
 */
public class Fetch {

    boolean completed = false;

    private String url;

    // private AsyncCallback<?> callback;

    private UrlFetchRegistry registry;

    public Fetch(String url
    // , AsyncCallback<?> callback
    ) {
        this.url = url;
        // this.callback = callback;
    }

    public boolean isComplete() {
        return this.completed;
    }

    public void completeFetch() {
        this.completed = true;
        this.registry.completeFetch(this);
    }

    public void registeredWith(UrlFetchRegistry urlFetchRegistry) {
        this.registry = urlFetchRegistry;
    }

    public String getUrl() {
        return this.url;
    }

}
