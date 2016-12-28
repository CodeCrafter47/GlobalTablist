package de.codecrafter47.globaltablist;

import com.google.common.base.Preconditions;

/**
 * Static entry point for the GlobalTablist API.
 */
public final class GlobalTablist {

    private static GlobalTablistAPI api;

    /**
     * Private constructor to prevent instantiation
     */
    private GlobalTablist() {
    }

    /**
     * Gets the API.
     *
     * @return the API
     * @throws IllegalStateException if the GlobalTablist plugin hasn't been initialized yet.
     */
    public static GlobalTablistAPI getAPI() {
        Preconditions.checkState(api != null, "GlobalTablist not initialized");
        return api;
    }

    /**
     * Sets the API instance. This method is called once when GlobalTablist is initialized. It should not be called by
     * other plugins.
     *
     * @param api the API
     */
    public static void setAPI(GlobalTablistAPI api) {
        Preconditions.checkNotNull(api, "api");
        Preconditions.checkState(GlobalTablist.api == null, "GlobalTablist already initialized");
        GlobalTablist.api = api;
    }
}
