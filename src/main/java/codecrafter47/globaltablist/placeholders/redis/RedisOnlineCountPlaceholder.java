package codecrafter47.globaltablist.placeholders.redis;

import codecrafter47.globaltablist.placeholders.OnlineCountPlaceholder;
import com.imaginarycode.minecraft.redisbungee.RedisBungee;
import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI;
import de.codecrafter47.globaltablist.Placeholder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

public class RedisOnlineCountPlaceholder extends Placeholder {
    private final Plugin plugin;

    private ScheduledTask updateTask = null;
    private int onlineCount = 0;

    public RedisOnlineCountPlaceholder(Plugin plugin) {
        super("{redis_online}");
        this.plugin = plugin;
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
            int onlineCount = api.getPlayerCount();
            if (onlineCount != RedisOnlineCountPlaceholder.this.onlineCount) {
                RedisOnlineCountPlaceholder.this.onlineCount = onlineCount;
                updateToAll();
            }
        }
    }
}
