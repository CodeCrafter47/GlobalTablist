/*
 * GlobalTablist - get the global tablist back
 *
 * Copyright (C) 2014 Florian Stober
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package codecrafter47.globaltablist;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class TabListListener implements Listener {

    private final GlobalTablist plugin;

    public TabListListener(GlobalTablist plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PostLoginEvent e) throws NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException {
        if (plugin.getConfig().useGlobalTablist) {
            ProxiedPlayer player = e.getPlayer();
            GlobalTablistHandler tablistHandler = new GlobalTablistHandler(player, plugin);
            GlobalTablist.setTablistHandler(player, tablistHandler);
        }
    }

    @EventHandler
    public void onDevJoin(PostLoginEvent e) {
        if (plugin.getDescription().getAuthor().equalsIgnoreCase(e.getPlayer().
                getName())) {
            e.getPlayer().sendMessage(new ComponentBuilder("Hello " + e.
                    getPlayer().getName() + ", this server uses your plugin: " + plugin.
                    getDescription().getName()).color(ChatColor.AQUA).create());
        }
    }
}
