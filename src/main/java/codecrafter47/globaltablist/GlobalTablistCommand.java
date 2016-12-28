package codecrafter47.globaltablist;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.plugin.Command;

public class GlobalTablistCommand extends Command {
    private final GlobalTablistPlugin plugin;

    public GlobalTablistCommand(GlobalTablistPlugin plugin) {
        super("globaltablist", "globaltablist.admin");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 1 && "reload".equalsIgnoreCase(args[0])) {
            if (plugin.reload()) {
                sender.sendMessage(new ComponentBuilder("GlobalTablist has ben reloaded.").color(ChatColor.GREEN).create());
            } else {
                sender.sendMessage(new ComponentBuilder("GlobalTablist failed to load config.yml.").color(ChatColor.RED).create());
            }
        } else {
            sender.sendMessage(new ComponentBuilder("GlobalTablist v" + plugin.getDescription().getVersion()).color(ChatColor.YELLOW).create());
            sender.sendMessage(new ComponentBuilder("Usage: /globaltablist reload").color(ChatColor.YELLOW).event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/globaltablist reload")).create());
        }
    }
}
