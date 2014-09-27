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
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.protocol.packet.PlayerListItem;

/**
 * @author Florian Stober
 */


public class CustomizationHandler implements Listener {
    private final GlobalTablist plugin;

    public CustomizationHandler(GlobalTablist plugin) {
        this.plugin = plugin;
        plugin.getProxy().getPluginManager().registerListener(plugin, this);
    }

    @EventHandler
    public void onLogin(PostLoginEvent event) {
        ProxiedPlayer player = event.getPlayer();

        if (plugin.getConfig().showHeaderFooter) {
            if (player.getPendingConnection().getVersion() >= 47) {
                player.setTabHeader(TextComponent.fromLegacyText(ChatColor.
                        translateAlternateColorCodes('&', plugin.
                                getConfig().header.
                                replaceAll("\\{player\\}", player.
                                        getDisplayName()).replaceAll(
                                "\\{newline\\}", "\n"))),
                        TextComponent.fromLegacyText(ChatColor.
                                translateAlternateColorCodes('&',
                                        plugin.getConfig().footer.
                                                replaceAll("\\{player\\}", player.
                                                        getDisplayName()).replaceAll(
                                                "\\{newline\\}", "\n"))));
            } else {
                for (String text : plugin.getConfig().custom_lines_top) {
                    text = text.replaceAll("\\{player\\}", player.
                            getDisplayName());
                    text = ChatColor.translateAlternateColorCodes('&', text);
                    if (text.length() > 16) {
                        text = text.substring(0, 16);
                    }
                    PlayerListItem pli = new PlayerListItem();
                    pli.setAction(PlayerListItem.Action.ADD_PLAYER);
                    PlayerListItem.Item item = new PlayerListItem.Item();
                    item.setDisplayName(text);
                    item.setPing(0);
                    pli.setItems(new PlayerListItem.Item[]{item});
                    player.unsafe().sendPacket(pli);
                }
            }
        }
    }
}
