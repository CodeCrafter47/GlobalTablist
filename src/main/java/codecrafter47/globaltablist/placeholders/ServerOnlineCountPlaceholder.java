package codecrafter47.globaltablist.placeholders;

import de.codecrafter47.globaltablist.Placeholder;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

public final class ServerOnlineCountPlaceholder extends Placeholder {
    private final Plugin plugin;
    private String serverName;

    private ScheduledTask updateTask = null;
    private int onlineCount = 0;

    public ServerOnlineCountPlaceholder(Plugin plugin, String serverName) {
        super(String.format("{online_%s}", serverName));
        this.plugin = plugin;
        this.serverName = serverName;
    }

    @Override
    protected void onActivate() {
        updateTask = plugin.getProxy().getScheduler().schedule(plugin, new UpdateTask(), 1, 1, TimeUnit.SECONDS);
    }

    @Override
    protected void onDeactivate() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
    }

    @Override
    public String getReplacement(ProxiedPlayer player, Matcher matcher) {
        return Integer.toString(onlineCount);
    }

    private final class UpdateTask implements Runnable {

        @Override
        public void run() {
            int onlineCount = 0;
            ServerInfo serverInfo = plugin.getProxy().getServerInfo(serverName);
            if (serverInfo != null) {
                onlineCount = serverInfo.getPlayers().size();
            }
            if (onlineCount != ServerOnlineCountPlaceholder.this.onlineCount) {
                ServerOnlineCountPlaceholder.this.onlineCount = onlineCount;
                updateToAll();
            }
        }
    }
}
