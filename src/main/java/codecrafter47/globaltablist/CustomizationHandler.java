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

import de.codecrafter47.globaltablist.Placeholder;
import de.codecrafter47.globaltablist.PlaceholderUpdateListener;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.protocol.packet.Team;
import net.md_5.bungee.tab.TabList;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

/**
 * @author Florian Stober
 */
public class CustomizationHandler implements Listener {
    private final GlobalTablistPlugin plugin;
    private List<CustomizationElement> customText = new CopyOnWriteArrayList<>();

    final static String[] fakePlayers = {"§m§4§7§k§o§l§0§r", "§m§4§7§k§o§l§1§r", "§m§4§7§k§o§l§2§r", "§m§4§7§k§o§l§3§r", "§m§4§7§k§o§l§4§r", "§m§4§7§k§o§l§5§r", "§m§4§7§k§o§l§6§r", "§m§4§7§k§o§l§7§r", "§m§4§7§k§o§l§8§r", "§m§4§7§k§o§l§9§r", "§m§4§7§k§o§l§a§r", "§m§4§7§k§o§l§b§r", "§m§4§7§k§o§l§c§r", "§m§4§7§k§o§l§d§r", "§m§4§7§k§o§l§e§r", "§m§4§7§k§o§l§f§r"};

    public CustomizationHandler(GlobalTablistPlugin plugin) {
        this.plugin = plugin;

        loadCustomizations();

        plugin.getProxy().getPluginManager().registerListener(plugin, this);
    }

    public void reload() {
        for (CustomizationElement customizationElement : customText) {
            customizationElement.unload();
        }
        customText.clear();
        loadCustomizations();
        for (ProxiedPlayer player : plugin.getProxy().getPlayers()) {
            sendCustomization(player);
        }
    }

    private void loadCustomizations() {
        customText.add(new HeaderFooter(plugin.getConfig().header, plugin.getConfig().footer));

        for (int i = 0; i < plugin.getConfig().custom_lines_top.size() && i < fakePlayers.length; i++) {
            final String text = plugin.getConfig().custom_lines_top.get(i);
            customText.add(new CustomSlot(i, text));
        }
    }

    @EventHandler
    public void onLogin(PostLoginEvent event) {
        ProxiedPlayer player = event.getPlayer();
        sendCustomization(player);
    }

    private void sendCustomization(ProxiedPlayer player) {
        for (CustomizationElement customizationElement : customText) {
            customizationElement.update(player);
        }
    }

    private static String[] splitText(String s) {
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

    private static abstract class CustomizationElement implements PlaceholderUpdateListener {
        protected Set<Placeholder> placeholders = new LinkedHashSet<>();

        protected abstract void update(ProxiedPlayer player);

        @Override
        public void onUpdate(ProxiedPlayer player) {
            update(player);
        }

        protected String replacePlaceholders(String text, final ProxiedPlayer player) {
            for (Placeholder placeholder : placeholders) {
                StringBuffer buffer = new StringBuffer();
                Matcher matcher = placeholder.getPattern().matcher(text);
                while (matcher.find()) {
                    matcher.appendReplacement(buffer, placeholder.getReplacement(player, matcher));
                }
                matcher.appendTail(buffer);
                text = buffer.toString();
            }
            return text;
        }

        public void unload() {
            for (Placeholder placeholder : placeholders) {
                placeholder.removeUpdateListener(this);
            }
        }
    }

    private final class HeaderFooter extends CustomizationElement {
        private final String header;
        private final String footer;

        private HeaderFooter(String header, String footer) {
            this.header = header;
            this.footer = footer;

            for (Placeholder placeholder : plugin.getPlaceholders()) {
                if (placeholder.getPattern().matcher(header).find() || placeholder.getPattern().matcher(footer).find()) {
                    placeholders.add(placeholder);
                    placeholder.addUpdateListener(this);
                }
            }
        }

        @Override
        protected void update(ProxiedPlayer player) {
            if (player.getPendingConnection().getVersion() >= 47) {
                player.setTabHeader(
                        TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', replacePlaceholders(header, player))),
                        TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', replacePlaceholders(footer, player))));
            }
        }
    }

    private final class CustomSlot extends CustomizationElement {
        private final int id;
        private final String text;

        private CustomSlot(int id, String text) {
            this.id = id;
            this.text = text;

            for (Placeholder placeholder : plugin.getPlaceholders()) {
                if (placeholder.getPattern().matcher(text).find()) {
                    placeholders.add(placeholder);
                    placeholder.addUpdateListener(this);
                }
            }
        }

        @Override
        protected void update(final ProxiedPlayer player) {
            if (player.getPendingConnection().getVersion() < 47) {
                if (player.getServer() == null) {
                    plugin.getProxy().getScheduler().schedule(plugin, new Runnable() {
                        @Override
                        public void run() {
                            update(player);
                        }
                    }, 200, TimeUnit.MILLISECONDS);
                    return;
                }
                TabList tablistHandler = null;
                try {
                    tablistHandler = ReflectionUtil.getTablistHandler(player);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }
                if (tablistHandler != null && tablistHandler instanceof GlobalTablistHandler17) {
                    String text = replacePlaceholders(this.text, player);
                    text = ChatColor.translateAlternateColorCodes('&', text);
                    String split[] = splitText(text);
                    Team t = new Team();
                    t.setName("GTAB#" + id);
                    t.setMode(!((GlobalTablistHandler17) tablistHandler).createdCustomSlots.contains(id) ? (byte) 0 : (byte) 2);
                    t.setPrefix(split[0]);
                    t.setDisplayName("");
                    t.setSuffix(split[1]);
                    t.setPlayers(new String[]{fakePlayers[id]});
                    player.unsafe().sendPacket(t);
                    ((GlobalTablistHandler17) tablistHandler).createdCustomSlots.add(id);
                }
            }
        }
    }
}
