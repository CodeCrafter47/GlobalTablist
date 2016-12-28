package de.codecrafter47.globaltablist;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Base class for a placeholder.
 */
public abstract class Placeholder {

    private final Pattern pattern;
    private final Set<PlaceholderUpdateListener> listeners = new CopyOnWriteArraySet<>();
    private boolean inUse;

    /**
     * Create a placeholder that replaces a specific pattern.
     *
     * @param pattern the pattern
     */
    public Placeholder(Pattern pattern) {
        this.pattern = pattern;
    }

    /**
     * Create a placeholder that replaces a specific string.
     *
     * @param placeholder the string
     */
    public Placeholder(String placeholder) {
        this(Pattern.compile(placeholder, Pattern.LITERAL));
    }

    /**
     * Get the pattern which is replaced by this placeholder.
     * @return
     */
    public final Pattern getPattern() {
        return pattern;
    }

    /**
     * Method to check whether this placeholder is currently being used anywhere.
     *
     * @return true if the placeholder is being used
     */
    public final boolean isInUse() {
        return inUse;
    }

    /**
     * Adds a placeholder update listener.
     *
     * @param listener the listener
     */
    public final void addUpdateListener(PlaceholderUpdateListener listener) {
        synchronized (listeners) {
            if (listeners.isEmpty()) {
                inUse = true;
                onActivate();
            }
            listeners.add(listener);
        }
    }

    /**
     * Removes a placeholder update listener.
     *
     * @param listener the listener
     */
    public final void removeUpdateListener(PlaceholderUpdateListener listener) {
        synchronized (listeners) {
            if (!listeners.isEmpty()) {
                listeners.remove(listener);
                if (listeners.isEmpty()) {
                    inUse = false;
                    onDeactivate();
                }
            }
        }
    }

    /**
     * Notifies all update listeners, that text containing this placeholder should be updated to the given player.
     *
     * @param player the player
     */
    public final void update(ProxiedPlayer player) {
        for (PlaceholderUpdateListener listener : listeners) {
            listener.onUpdate(player);
        }
    }

    /**
     * Notifies the update listeners, that this placeholder should be updated for all players.
     */
    public final void updateToAll() {
        for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
            update(player);
        }
    }

    /**
     * Called when the placeholder is activated (i.e. the value of {@link #isInUse()} has changed to true)
     */
    protected abstract void onActivate();

    /**
     * Called when the placeholder is deactivated (i.e. the value of {@link #isInUse()} has changed to false)
     */
    protected abstract void onDeactivate();

    /**
     * Called to obtain the replacement for the placeholder.
     *  @param player the player
     * @param matcher the matcher
     */
    public abstract String getReplacement(ProxiedPlayer player, Matcher matcher);
}
