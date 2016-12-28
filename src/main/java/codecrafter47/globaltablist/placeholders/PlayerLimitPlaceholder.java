package codecrafter47.globaltablist.placeholders;

import de.codecrafter47.globaltablist.Placeholder;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlayerLimitPlaceholder extends Placeholder {
    public PlayerLimitPlaceholder() {
        super("{max}");
    }

    @Override
    protected void onActivate() {

    }

    @Override
    protected void onDeactivate() {

    }

    @Override
    public String getReplacement(ProxiedPlayer player, Matcher matcher) {
        return Integer.toString(ProxyServer.getInstance().getConfig().getPlayerLimit());
    }
}
