package codecrafter47.globaltablist.placeholders;

import de.codecrafter47.globaltablist.Placeholder;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.regex.Matcher;

public final class ListenerMaxPlayersPlaceholder extends Placeholder {
    public ListenerMaxPlayersPlaceholder() {
        super("{shown_max}");
    }

    @Override
    protected void onActivate() {

    }

    @Override
    protected void onDeactivate() {

    }

    @Override
    public String getReplacement(ProxiedPlayer player, Matcher matcher) {
        return Integer.toString(player.getPendingConnection().getListener().getMaxPlayers());
    }
}
