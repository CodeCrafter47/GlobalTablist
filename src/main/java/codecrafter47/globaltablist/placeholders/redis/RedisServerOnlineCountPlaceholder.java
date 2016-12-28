package codecrafter47.globaltablist.placeholders.redis;

import com.imaginarycode.minecraft.redisbungee.RedisBungee;
import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI;
import de.codecrafter47.globaltablist.Placeholder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

public class RedisServerOnlineCountPlaceholder extends Placeholder {
    private final Plugin plugin;
    private String serverName;

    private ScheduledTask updateTask = null;
    private int onlineCount = 0;

    public RedisServerOnlineCountPlaceholder(Plugin plugin, String serverName) {
        super(String.format("{redis_online_%s}", serverName));
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
            RedisBungeeAPI api = RedisBungee.getApi();
            if (api == null) return;
            int onlineCount = 0;
            if (plugin.getProxy().getServers().containsKey(serverName)) {
                Set<UUID> playersOnServer = api.getPlayersOnServer(serverName);
                onlineCount = playersOnServer != null ? playersOnServer.size() : 0;
            }
            if (onlineCount != RedisServerOnlineCountPlaceholder.this.onlineCount) {
                RedisServerOnlineCountPlaceholder.this.onlineCount = onlineCount;
                updateToAll();
            }
        }
    }
}
