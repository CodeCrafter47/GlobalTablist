package codecrafter47.globaltablist;

import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.tab.ServerUnique;

public class ServerUniqueTab extends ServerUnique {
    protected final GlobalTablist plugin;

    public ServerUniqueTab(ProxiedPlayer player, GlobalTablist plugin) {
        super(player);
        this.plugin = plugin;
    }

    @Override
    public void onPingChange(int ping) {
        super.onPingChange(ping);
        ((UserConnection)player).setPing(ping);
        plugin.getCustomizationHandler().pingVariable.onPingChange(player);
    }
}
