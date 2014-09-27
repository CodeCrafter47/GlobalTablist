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

import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import net.md_5.bungee.protocol.packet.Team;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Florian Stober
 */


public class CustomizationHandler implements Listener {
    private final GlobalTablist plugin;
    private Collection<Variable> variables = new ArrayList<>();
    private List<String> customLines;
    private String header, footer;

    private final static String[] fakePlayers = {"§m§4§7§k§o§l§0§r", "§m§4§7§k§o§l§1§r", "§m§4§7§k§o§l§2§r", "§m§4§7§k§o§l§3§r", "§m§4§7§k§o§l§4§r", "§m§4§7§k§o§l§5§r", "§m§4§7§k§o§l§6§r", "§m§4§7§k§o§l§7§r", "§m§4§7§k§o§l§8§r", "§m§4§7§k§o§l§9§r", "§m§4§7§k§o§l§a§r", "§m§4§7§k§o§l§b§r", "§m§4§7§k§o§l§c§r", "§m§4§7§k§o§l§d§r", "§m§4§7§k§o§l§e§r", "§m§4§7§k§o§l§f§r"};
    private boolean requiresUpdating;

    public CustomizationHandler(GlobalTablist plugin) {
        this.plugin = plugin;
        this.customLines = plugin.getConfig().custom_lines_top;
        this.header = plugin.getConfig().header;
        this.footer = plugin.getConfig().footer;
        addVariables();
        requiresUpdating = false;
        for(Variable var: variables){
            if(!var.isDynamic())continue;
            requiresUpdating |= var.contains(header);
            requiresUpdating |= var.contains(footer);
            for(String s: customLines){
                requiresUpdating |= var.contains(s);
            }
        }
        plugin.getProxy().getPluginManager().registerListener(plugin, this);
        if(requiresUpdating){
            plugin.getProxy().getScheduler().schedule(plugin, new Runnable() {
                @Override
                public void run() {
                    for(ProxiedPlayer player: ProxyServer.getInstance().getPlayers()){
                        updateCustomization(player);
                    }
                }
            }, 1, 1, TimeUnit.SECONDS);
        }
    }

    @EventHandler
    public void onLogin(PostLoginEvent event) {
        ProxiedPlayer player = event.getPlayer();
        sendCustomization(player);
    }

    private void sendCustomization(ProxiedPlayer player) {
        if (player.getPendingConnection().getVersion() < 47) {
            for (int i = 0; i < customLines.size() && i < fakePlayers.length; i++) {
                PlayerListItem pli = new PlayerListItem();
                pli.setAction(PlayerListItem.Action.ADD_PLAYER);
                PlayerListItem.Item item = new PlayerListItem.Item();
                item.setDisplayName(fakePlayers[i]);
                item.setPing(0);
                pli.setItems(new PlayerListItem.Item[]{item});
                player.unsafe().sendPacket(pli);
            }
        }
        updateCustomization(player);
    }

    private void updateCustomization(final ProxiedPlayer player) {
        if(player.getServer() == null && requiresUpdating == false){
            plugin.getProxy().getScheduler().schedule(plugin, new Runnable() {
                @Override
                public void run() {
                    updateCustomization(player);
                }
            }, 200, TimeUnit.MILLISECONDS);
            return;
        }
        if(player.getServer() == null)return;
        if (player.getPendingConnection().getVersion() >= 47) {
            player.setTabHeader(TextComponent.fromLegacyText(ChatColor.
                    translateAlternateColorCodes('&', replaceVariables(header, player))),
                    TextComponent.fromLegacyText(ChatColor.
                            translateAlternateColorCodes('&', replaceVariables(footer, player))));
        } else {
            for (int i = 0; i < customLines.size() && i < fakePlayers.length; i++) {
                String text = replaceVariables(customLines.get(i), player);
                text = ChatColor.translateAlternateColorCodes('&', text);
                String split[] = splitText(text);
                Team t = new Team();
                t.setName("GTAB#" + i);
                t.setMode((!requiresUpdating) || (!player.hasPermission("globaltablist.initialized"+i))? (byte) 0: (byte) 2);
                t.setPrefix(split[0]);
                t.setDisplayName("");
                t.setSuffix(split[1]);
                t.setPlayers(new String[]{fakePlayers[i]});
                player.unsafe().sendPacket(t);
                if(requiresUpdating && !player.hasPermission("globaltablist.initialized"+i)){
                    player.setPermission("globaltablist.initialized"+i, true);
                }
            }
        }
    }

    private String replaceVariables(final String text, final ProxiedPlayer player){
        String s = text;
        for(Variable var: variables){
            s = var.apply(s, player);
        }
        return s;
    }

    private String[] splitText(String s) {
        String ret[] = new String[2];
        int left = s.length();
        if (left <= 16) {
            ret[0] = s;
            ret[1] = "";
        } else {
            int end = s.charAt(15) == ChatColor.COLOR_CHAR ? 15 : 16;
            ret[0] = s.substring(0, end);
            int start = ColorParser.endofColor(s, end);
            String colors = ColorParser.extractColorCodes(s.substring(0, start));
            end = start + 16 - colors.length();
            if (end >= s.length()) {
                end = s.length();
            }
            ret[1] = colors + s.substring(start, end);
        }
        return ret;
    }

    private void addVariables() {
        variables.add(new Variable("newline") {

            @Override
            String getReplacement(ProxiedPlayer player) {
                return "\n";
            }
        });

        variables.add(new Variable("player") {

            @Override
            String getReplacement(ProxiedPlayer player) {
                return player.getDisplayName();
            }
        });

        variables.add(new Variable("server", true) {

            @Override
            String getReplacement(ProxiedPlayer player) {
                return player.getServer().getInfo().getName();
            }
        });

        variables.add(new Variable("online", true) {

            @Override
            String getReplacement(ProxiedPlayer player) {
                return Integer.toString(ProxyServer.getInstance().getOnlineCount());
            }
        });

        variables.add(new Variable("max") {

            @Override
            String getReplacement(ProxiedPlayer player) {
                return Integer.toString(ProxyServer.getInstance().getConfig().getPlayerLimit());
            }
        });
    }

    private static abstract class Variable {
        private String regex;
        private String name;

        @Getter
        private boolean dynamic;

        protected Variable(String name) {
            this.regex = "\\{" + name + "\\}";
            this.name = name;
            dynamic = false;
        }

        protected Variable(String name, boolean isDynamic) {
            this(name);
            dynamic = isDynamic;
        }

        protected String apply(String text, ProxiedPlayer player) {
            return text.replaceAll(regex, getReplacement(player));
        }

        protected boolean contains(String text) {
            return text.contains("{"+name+"}");
        }

        abstract String getReplacement(ProxiedPlayer player);
    }
}
