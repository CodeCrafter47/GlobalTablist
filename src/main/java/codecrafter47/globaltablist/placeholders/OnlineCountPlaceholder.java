package codecrafter47.globaltablist.placeholders;

import de.codecrafter47.globaltablist.Placeholder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

/**
 * Online count placeholder.
 */
public final class OnlineCountPlaceholder extends Placeholder {
    private final Plugin plugin;

    private ScheduledTask updateTask = null;
    private int onlineCount = 0;

    public OnlineCountPlaceholder(Plugin plugin) {
        super("{online}");
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
            int onlineCount = plugin.getProxy().getOnlineCount();
            if (onlineCount != OnlineCountPlaceholder.this.onlineCount) {
                OnlineCountPlaceholder.this.onlineCount = onlineCount;
                updateToAll();
            }
        }
    }
}
