package de.codecrafter47.globaltablist;

import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 * Listener for placeholder updates.
 */
public interface PlaceholderUpdateListener {

    /**
     * Notifies the listener about an update.
     *
     * @param player the affected player
     */
    void onUpdate(ProxiedPlayer player);
}
