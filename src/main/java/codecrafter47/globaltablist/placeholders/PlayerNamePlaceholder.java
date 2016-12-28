package codecrafter47.globaltablist.placeholders;

import de.codecrafter47.globaltablist.Placeholder;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.regex.Matcher;

/**
 * Placeholder for the players display name.
 */
public final class PlayerNamePlaceholder extends Placeholder {
    public PlayerNamePlaceholder() {
        super("{player}");
    }

    @Override
    protected void onActivate() {
        // the player's name should't change
    }

    @Override
    protected void onDeactivate() {
        // the player's name should't change
    }

    @Override
    public String getReplacement(ProxiedPlayer player, Matcher matcher) {
        return player.getDisplayName();
    }
}
