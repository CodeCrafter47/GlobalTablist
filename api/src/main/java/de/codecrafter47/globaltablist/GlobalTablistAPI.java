package de.codecrafter47.globaltablist;

import net.md_5.bungee.api.plugin.Plugin;

/**
 * GlobalTablist API.
 */
public interface GlobalTablistAPI {

    /**
     * Registers a placeholder for use with tab list customizations.
     *
     * @param plugin      the plugin that registers the placeholder
     * @param placeholder the placeholder
     */
    void registerPlaceholder(Plugin plugin, Placeholder placeholder);
}
