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

import com.imaginarycode.minecraft.redisbungee.RedisBungee;
import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
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
    private List<Updateable> customText = new ArrayList<>();

    private final static String[] fakePlayers = {"§m§4§7§k§o§l§0§r", "§m§4§7§k§o§l§1§r", "§m§4§7§k§o§l§2§r", "§m§4§7§k§o§l§3§r", "§m§4§7§k§o§l§4§r", "§m§4§7§k§o§l§5§r", "§m§4§7§k§o§l§6§r", "§m§4§7§k§o§l§7§r", "§m§4§7§k§o§l§8§r", "§m§4§7§k§o§l§9§r", "§m§4§7§k§o§l§a§r", "§m§4§7§k§o§l§b§r", "§m§4§7§k§o§l§c§r", "§m§4§7§k§o§l§d§r", "§m§4§7§k§o§l§e§r", "§m§4§7§k§o§l§f§r"};

    public CustomizationHandler(final GlobalTablist plugin) {
        this.plugin = plugin;

        customText.add(new Updateable() {
            @Override
            protected void update(ProxiedPlayer player) {
                if(player.getPendingConnection().getVersion() < 47)return;
                player.setTabHeader(TextComponent.fromLegacyText(ChatColor.
                                translateAlternateColorCodes('&', replaceVariables(plugin.getConfig().header, player))),
                        TextComponent.fromLegacyText(ChatColor.
                                translateAlternateColorCodes('&', replaceVariables(plugin.getConfig().footer, player))));
            }

            @Override
            protected boolean contains(Variable var) {
                return var.contains(plugin.getConfig().header) || var.contains(plugin.getConfig().footer);
            }
        });

        for (int i = 0; i < plugin.getConfig().custom_lines_top.size() && i < fakePlayers.length; i++) {
            final String text2 = plugin.getConfig().custom_lines_top.get(i);
            final int id = i;
            customText.add(new Updateable() {
                @Override
                protected void update(final ProxiedPlayer player) {
                    if(player.getPendingConnection().getVersion() >= 47)return;
                    if(player.getServer() == null){
                        plugin.getProxy().getScheduler().schedule(plugin, new Runnable() {
                            @Override
                            public void run() {
                                update(player);
                            }
                        }, 200, TimeUnit.MILLISECONDS);
                        return;
                    }
                    String text = replaceVariables(text2, player);
                    text = ChatColor.translateAlternateColorCodes('&', text);
                    String split[] = splitText(text);
                    Team t = new Team();
                    t.setName("GTAB#" + id);
                    t.setMode(!player.hasPermission("globaltablist.initialized"+id)? (byte) 0: (byte) 2);
                    t.setPrefix(split[0]);
                    t.setDisplayName("");
                    t.setSuffix(split[1]);
                    t.setPlayers(new String[]{fakePlayers[id]});
                    player.unsafe().sendPacket(t);
                    if(!player.hasPermission("globaltablist.initialized"+id)){
                        player.setPermission("globaltablist.initialized"+id, true);
                    }
                }

                @Override
                protected boolean contains(Variable var) {
                    return var.contains(text2);
                }
            });
        }

        addVariables();

        plugin.getProxy().getPluginManager().registerListener(plugin, this);
    }

    @EventHandler
    public void onLogin(PostLoginEvent event) {
        ProxiedPlayer player = event.getPlayer();
        sendCustomization(player);
    }

    private void sendCustomization(ProxiedPlayer player) {
        if (player.getPendingConnection().getVersion() < 47) {
            for (int i = 0; i < plugin.getConfig().custom_lines_top.size() && i < fakePlayers.length; i++) {
                PlayerListItem pli = new PlayerListItem();
                pli.setAction(PlayerListItem.Action.ADD_PLAYER);
                PlayerListItem.Item item = new PlayerListItem.Item();
                item.setDisplayName(fakePlayers[i]);
                item.setPing(0);
                pli.setItems(new PlayerListItem.Item[]{item});
                player.unsafe().sendPacket(pli);
                player.setPermission("globaltablist.initialized"+i, false);
            }
        }
        for(Updateable updateable: customText){
            updateable.update(player);
        }
    }

    private String replaceVariables(final String text, final ProxiedPlayer player){
        String s = text;
        for (Variable var : variables) {
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

        variables.add( new ServerVariable("server", true));

        variables.add(new Variable("online", true) {

            int last = 0;

            @Override
            String getReplacement(ProxiedPlayer player) {
                return Integer.toString(last);
            }

            @Override
            protected void onCreate() {
                super.onCreate();
                plugin.getProxy().getScheduler().schedule(plugin, new Runnable() {
                    @Override
                    public void run() {
                        int i = ProxyServer.getInstance().getOnlineCount();
                        if(i != last){
                            last = i;
                            for(Updateable updateable: onChange){
                                for (ProxiedPlayer player: plugin.getProxy().getPlayers()){
                                    updateable.update(player);
                                }
                            }
                        }
                    }
                }, 1000, 1000, TimeUnit.MILLISECONDS);
            }
        });

        for (final String serverName : plugin.getProxy().getServers().keySet()) {
            variables.add(new Variable(String.format("online_%s", serverName), true) {

                int last = 0;

                @Override
                String getReplacement(ProxiedPlayer player) {
                    return Integer.toString(last);
                }

                @Override
                protected void onCreate() {
                    super.onCreate();
                    plugin.getProxy().getScheduler().schedule(plugin, new Runnable() {
                        @Override
                        public void run() {
                            int i = ProxyServer.getInstance().getServerInfo(serverName).getPlayers().size();
                            if(i != last){
                                last = i;
                                for(Updateable updateable: onChange){
                                    for (ProxiedPlayer player: plugin.getProxy().getPlayers()){
                                        updateable.update(player);
                                    }
                                }
                            }
                        }
                    }, 1000, 1000, TimeUnit.MILLISECONDS);
                }
            });
        }

        variables.add(new Variable("shown_max") {

            @Override
            String getReplacement(ProxiedPlayer player) {
                return Integer.toString(ProxyServer.getInstance().getConfig().getListeners().iterator().next().getMaxPlayers());
            }
        });

        variables.add(new Variable("max") {

            @Override
            String getReplacement(ProxiedPlayer player) {
                return Integer.toString(ProxyServer.getInstance().getConfig().getPlayerLimit());
            }
        });

        if(plugin.getProxy().getPluginManager().getPlugin("RedisBungee") != null){
            variables.add(new Variable("redis_online", true) {

                int last = 0;

                @Override
                String getReplacement(ProxiedPlayer player) {
                    return Integer.toString(last);
                }

                @Override
                protected void onCreate() {
                    super.onCreate();
                    plugin.getProxy().getScheduler().schedule(plugin, new Runnable() {
                        @Override
                        public void run() {
                            int i = RedisBungee.getApi().getPlayerCount();
                            if(i != last){
                                last = i;
                                for(Updateable updateable: onChange){
                                    for (ProxiedPlayer player: plugin.getProxy().getPlayers()){
                                        updateable.update(player);
                                    }
                                }
                            }
                        }
                    }, 1000, 1000, TimeUnit.MILLISECONDS);
                }
            });

            for (final String serverName : plugin.getProxy().getServers().keySet()) {
                variables.add(new Variable(String.format("redis_online_%s", serverName), true) {

                    int last = 0;

                    @Override
                    String getReplacement(ProxiedPlayer player) {
                        return Integer.toString(last);
                    }

                    @Override
                    protected void onCreate() {
                        super.onCreate();
                        plugin.getProxy().getScheduler().schedule(plugin, new Runnable() {
                            @Override
                            public void run() {
                                int i = RedisBungee.getApi().getPlayersOnServer(serverName).size();
                                if(i != last){
                                    last = i;
                                    for(Updateable updateable: onChange){
                                        for (ProxiedPlayer player: plugin.getProxy().getPlayers()){
                                            updateable.update(player);
                                        }
                                    }
                                }
                            }
                        }, 1000, 1000, TimeUnit.MILLISECONDS);
                    }
                });
            }
        }

        for(Variable var: variables){
            for(Updateable updateable: customText){
                if(updateable.contains(var)){
                    var.addUpdateable(updateable);
                }
            }
        }
    }

    public abstract class Variable{
        private String regex;
        private String name;
        protected List<Updateable> onChange = new ArrayList<>();

        @Getter
        private boolean dynamic;

        protected Variable(String name) {
            this.regex = String.format("\\{%s\\}", name);
            this.name = name;
            dynamic = false;
            onCreate();
        }

        protected Variable(String name, boolean isDynamic) {
            this(name);
            dynamic = isDynamic;
        }

        protected String apply(String text, ProxiedPlayer player) {
            return text.replaceAll(regex, getReplacement(player));
        }

        protected boolean contains(String text) {
            return text.contains(String.format("{%s}", name));
        }

        abstract String getReplacement(ProxiedPlayer player);

        protected boolean addUpdateable(Updateable updateable) {
            return onChange.add(updateable);
        }

        protected void onCreate(){

        }
    }

    public class ServerVariable extends Variable implements Listener{


        protected ServerVariable(String name) {
            super(name);
        }

        protected ServerVariable(String name, boolean isDynamic) {
            super(name, isDynamic);
        }

        @Override
        String getReplacement(ProxiedPlayer player) {
            if(player.getServer() == null)return "null";
            return player.getServer().getInfo().getName();
        }

        @EventHandler
        public void onServerSwitch(ServerConnectedEvent event){
            final ProxiedPlayer player = event.getPlayer();
            ProxyServer.getInstance().getScheduler().schedule(plugin, new Runnable() {
                @Override
                public void run() {
                    for(Updateable updateable: onChange){
                        updateable.update(player);
                    }
                }
            }, 1, TimeUnit.SECONDS);
        }

        @Override
        protected void onCreate() {
            plugin.getProxy().getPluginManager().registerListener(plugin, this);
        }
    }

    private static abstract class Updateable{
        protected abstract void update(ProxiedPlayer player);
        protected abstract boolean contains(Variable var);
    }
}
