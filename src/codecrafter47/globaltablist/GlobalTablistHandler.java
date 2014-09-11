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

import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.connection.LoginResult;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import net.md_5.bungee.protocol.packet.PlayerListItem.Item;
import net.md_5.bungee.tab.TabList;

/**
 *
 * @author Florian Stober
 */
public class GlobalTablistHandler extends TabList {

    private final GlobalTablist plugin;

    int lastPing = 0;

    public GlobalTablist getPlugin() {
        return plugin;
    }

    public GlobalTablistHandler(ProxiedPlayer player, GlobalTablist plugin) {
        super(player);
        this.plugin = plugin;
    }

    protected ProxiedPlayer getPlayer() {
        return player;
    }

    @Override
    public void onServerChange() {
        // It's a global tablist. Nothing happens if a player switches the server
    }

    @Override
    public void onUpdate(PlayerListItem pli) {
        // It's a global tablist - we don't pass packets from the server
    }

    @Override
    public void onPingChange(int i) {
        if (lastPing - i > 50 || lastPing - i < 50) {
            PlayerListItem pli = new PlayerListItem();
            pli.setAction(PlayerListItem.Action.UPDATE_LATENCY);
            Item item = new Item();
            item.setUsername(getPlayer().getName());
            item.setUuid(getPlayer().getUniqueId());
            item.setPing(i);
            for (ProxiedPlayer player : plugin.getProxy().getPlayers()) {
                player.unsafe().sendPacket(pli);
            }
        }
    }

    @Override
    public void onConnect() {
        // send header/footer/custom slots
        if (plugin.getConfig().showHeaderFooter) {
            if (is18Client()) {
                getPlayer().setTabHeader(TextComponent.fromLegacyText(ChatColor.
                        translateAlternateColorCodes('&', plugin.
                                getConfig().header.
                                replaceAll("\\{player\\}", getPlayer().
                                        getDisplayName()))),
                        TextComponent.fromLegacyText(ChatColor.
                                translateAlternateColorCodes('&',
                                        plugin.getConfig().footer.
                                        replaceAll("\\{player\\}", getPlayer().
                                                getDisplayName()))));
            } else {
                for (String text : plugin.getConfig().custom_lines_top) {
                    text = text.replaceAll("\\{player\\}", getPlayer().
                            getDisplayName());
                    text = ChatColor.translateAlternateColorCodes('&', text);
                    if (text.length() > 16) {
                        text = text.substring(0, 16);
                    }
                    PlayerListItem pli = new PlayerListItem();
                    pli.setAction(PlayerListItem.Action.ADD_PLAYER);
                    Item item = new Item();
                    item.setUsername(text);
                    item.setPing(0);
                    pli.setItems(new Item[]{item});
                    getPlayer().unsafe().sendPacket(pli);
                }
            }
        }

        // send players
        for (ProxiedPlayer player : plugin.getProxy().getPlayers()) {
            sendPlayerSlot(player, getPlayer());
            if (player == getPlayer()) {
                continue;
            }
            sendPlayerSlot(getPlayer(), player);
        }

        // store ping
        lastPing = getPlayer().getPing();
    }

    @Override
    public void onDisconnect() {
        for (ProxiedPlayer player : plugin.getProxy().getPlayers()) {
            removePlayerSlot(getPlayer(), player);
        }
    }

    protected boolean is18Client() {
        return getPlayer().getPendingConnection().getVersion() >= 47;
    }

    protected boolean isCracked() {
        return !getPlayer().getPendingConnection().isOnlineMode();
    }

    protected boolean is18Client(ProxiedPlayer player) {
        return player.getPendingConnection().getVersion() >= 47;
    }

    protected boolean isCracked(ProxiedPlayer player) {
        return !player.getPendingConnection().isOnlineMode();
    }

    protected void sendPlayerSlot(ProxiedPlayer player, ProxiedPlayer receiver) {
        String text = player.getDisplayName();

        if (!is18Client() && text.length() > 16) {
            text = text.substring(0, 16);
        }

        PlayerListItem pli = new PlayerListItem();
        pli.setAction(PlayerListItem.Action.ADD_PLAYER);
        Item item = new Item();
        item.setUsername(is18Client(receiver) ? player.getName() : text);
        item.setPing(player.getPing());
        if (is18Client(receiver)) {
            item.setGamemode(((UserConnection) player).getGamemode());
            item.setDisplayName(text);
            item.setUuid(player.getUniqueId());
            item.setProperties(new String[0][0]);
            if (!isCracked(receiver)) {
                LoginResult loginResult = ((UserConnection) player).
                        getPendingConnection().getLoginProfile();
                if (loginResult != null) {
                    for (LoginResult.Property s : loginResult.getProperties()) {
                        if (s.getName().equals("textures")) {
                            item.setProperties(new String[][]{{"textures", s.
                                getValue(), s.getSignature()}});
                        }
                    }
                }
            }
        }
        pli.setItems(new Item[]{item});
        receiver.unsafe().sendPacket(pli);
    }

    private void removePlayerSlot(ProxiedPlayer player, ProxiedPlayer receiver) {
        PlayerListItem pli = new PlayerListItem();
        pli.setAction(PlayerListItem.Action.REMOVE_PLAYER);
        Item item = new Item();
        item.setUsername(player.getName());
        if (is18Client(receiver)) {
            item.setUuid(player.getUniqueId());
        }
        pli.setItems(new Item[]{item});
        receiver.unsafe().sendPacket(pli);
    }
}
