package codecrafter47.globaltablist.placeholders;

import de.codecrafter47.globaltablist.Placeholder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.util.regex.Matcher;

public final class ServerPlaceholder extends Placeholder {
    private final Plugin plugin;
    private final Listener listener = new ServerSwitchListener();

    public ServerPlaceholder(Plugin plugin) {
        super("{server}");
        this.plugin = plugin;
    }

    @Override
    protected void onActivate() {
        // The placeholder is used, register our listener
        plugin.getProxy().getPluginManager().registerListener(plugin, listener);
    }

    @Override
    protected void onDeactivate() {
        // The placeholder is no longer in use, unregister our listener
        plugin.getProxy().getPluginManager().unregisterListener(listener);
    }

    @Override
    public String getReplacement(ProxiedPlayer player, Matcher matcher) {
        Server server = player.getServer();
        return server != null ? server.getInfo().getName() : "";
    }

    // needs to be public, otherwise event listener invocation will fail
    public final class ServerSwitchListener implements Listener {

        @EventHandler
        public void onServerSwitch(ServerSwitchEvent event) {
            // update the placeholder to the affected player
            update(event.getPlayer());
        }
    }
}
