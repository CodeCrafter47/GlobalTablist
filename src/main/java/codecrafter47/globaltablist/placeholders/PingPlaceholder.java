package codecrafter47.globaltablist.placeholders;

import de.codecrafter47.globaltablist.Placeholder;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.regex.Matcher;

/**
 * Ping placeholder.
 */
public final class PingPlaceholder extends Placeholder {
    public PingPlaceholder() {
        super("{ping}");
    }

    @Override
    protected void onActivate() {

    }

    @Override
    protected void onDeactivate() {

    }

    @Override
    public String getReplacement(ProxiedPlayer player, Matcher matcher) {
        return Integer.toString(player.getPing());
    }

    /**
     * This method is called by our custom {@link net.md_5.bungee.tab.TabList} whenever the ping of a player is changed.
     * @param player the affected player
     */
    public void onPingChange(ProxiedPlayer player) {
        if (isInUse()) {
            update(player);
        }
    }
}
