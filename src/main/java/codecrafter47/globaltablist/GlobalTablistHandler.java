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
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.connection.LoginResult;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import net.md_5.bungee.protocol.packet.PlayerListItem.Item;
import net.md_5.bungee.tab.TabList;

/**
 * @author Florian Stober
 */
public class GlobalTablistHandler extends TabList {

    private final GlobalTablist plugin;

    private int lastPing = 0;

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
        // We need to forward gamemode updates to avoid glitches
        if (is18Client(getPlayer())) {
            if (pli.getAction() == PlayerListItem.Action.ADD_PLAYER) {
                for (Item item : pli.getItems()) {
                    if (item.getUuid().equals(getPlayer().getUniqueId())) {
                        ((UserConnection) player).setGamemode(item.getGamemode());
                        for (ProxiedPlayer p : plugin.getProxy().getPlayers()) {
                            sendPlayerSlot(p, getPlayer());
                        }
                    }
                }
            } else if (pli.getAction() == PlayerListItem.Action.UPDATE_GAMEMODE) {
                for (Item item : pli.getItems()) {
                    if (item.getUuid().equals(getPlayer().getUniqueId())) {
                        ((UserConnection) player).setGamemode(item.getGamemode());
                        PlayerListItem packet = new PlayerListItem();
                        packet.setAction(PlayerListItem.Action.UPDATE_GAMEMODE);
                        packet.setItems(new Item[]{item});
                        for (ProxiedPlayer p : plugin.getProxy().getPlayers()) {
                            p.unsafe().sendPacket(packet);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onPingChange(int i) {
        if (plugin.getConfig().updatePing) {
            if (lastPing - i > 50 || lastPing - i < 50) {
                PlayerListItem pli = new PlayerListItem();
                pli.setAction(PlayerListItem.Action.UPDATE_LATENCY);
                Item item = new Item();
                item.setUsername(getPlayer().getName());
                item.setUuid(getPlayer().getUniqueId());
                String text = player.getDisplayName();
                if (text.length() > 16) {
                    text = text.substring(0, 16);
                }
                item.setDisplayName(text);
                item.setPing(i);
                pli.setItems(new Item[]{item});
                for (ProxiedPlayer p : plugin.getProxy().getPlayers()) {
                    p.unsafe().sendPacket(pli);
                }
            }
        }
    }

    @Override
    public void onConnect() {
        // send players
        for (ProxiedPlayer p : plugin.getProxy().getPlayers()) {
            sendPlayerSlot(p, getPlayer());
            if (p == getPlayer()) {
                continue;
            }
            sendPlayerSlot(getPlayer(), p);
        }

        // store ping
        lastPing = getPlayer().getPing();
    }

    @Override
    public void onDisconnect() {
        // remove player
        for (ProxiedPlayer p : plugin.getProxy().getPlayers()) {
            removePlayerSlot(getPlayer(), p);
        }
    }

    protected boolean is18Client(ProxiedPlayer player) {
        return player.getPendingConnection().getVersion() >= 47;
    }

    protected void sendPlayerSlot(ProxiedPlayer player, ProxiedPlayer receiver) {
        String text = player.getDisplayName();

        if (!is18Client(receiver) && text.length() > 16) {
            text = text.substring(0, 16);
        }

        PlayerListItem pli = new PlayerListItem();
        pli.setAction(PlayerListItem.Action.ADD_PLAYER);
        Item item = new Item();
        item.setPing(player.getPing());
        if (!is18Client(receiver)) {
            item.setDisplayName(text);
        } else {
            item.setUsername(player.getName());
            item.setGamemode(((UserConnection) player).getGamemode());
            item.setUuid(player.getUniqueId());
            item.setProperties(new String[0][0]);
            LoginResult loginResult = ((UserConnection) player).
                    getPendingConnection().getLoginProfile();
            if (loginResult != null) {
                String[][] props = new String[loginResult.getProperties().length][];
                for (int i = 0; i < props.length; i++) {
                    props[i] = new String[]
                            {
                                    loginResult.getProperties()[i].getName(),
                                    loginResult.getProperties()[i].getValue(),
                                    loginResult.getProperties()[i].getSignature()
                            };
                }
                item.setProperties(props);
            } else {
                item.setProperties(new String[0][0]);
            }
        }
        pli.setItems(new Item[]{item});
        receiver.unsafe().sendPacket(pli);
    }

    private void removePlayerSlot(ProxiedPlayer player, ProxiedPlayer receiver) {
        PlayerListItem pli = new PlayerListItem();
        pli.setAction(PlayerListItem.Action.REMOVE_PLAYER);
        Item item = new Item();
        if (is18Client(receiver)) {
            item.setUsername(player.getName());
            item.setUuid(player.getUniqueId());
        } else {
            String text = player.getDisplayName();
            if (text.length() > 16) {
                text = text.substring(0, 16);
            }
            item.setDisplayName(text);
        }
        pli.setItems(new Item[]{item});
        receiver.unsafe().sendPacket(pli);
    }
}
