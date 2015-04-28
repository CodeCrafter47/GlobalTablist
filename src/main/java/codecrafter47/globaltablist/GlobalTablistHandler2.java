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

import lombok.Synchronized;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.connection.LoginResult;
import net.md_5.bungee.protocol.packet.PlayerListItem;

import java.util.*;

/**
 * @author Florian Stober
 */
public class GlobalTablistHandler2 extends GlobalTablistHandler {
    private final Collection<UUID> uuids = new HashSet();
    private final Collection<UUID> globalUUIDs = new HashSet<>();

    public GlobalTablistHandler2(ProxiedPlayer player, GlobalTablist plugin) {
        super(player, plugin);
    }

    @Synchronized
    public void onUpdate(PlayerListItem playerListItem) {
        PlayerListItem.Item[] var2 = playerListItem.getItems();
        int var3 = var2.length;

        if (playerListItem.getAction() == PlayerListItem.Action.ADD_PLAYER) {
            for (int var4 = 0; var4 < var3; ++var4) {
                PlayerListItem.Item item = var2[var4];
                this.uuids.add(item.getUuid());
            }
        } else if (playerListItem.getAction() == PlayerListItem.Action.REMOVE_PLAYER) {
            List<PlayerListItem.Item> itemList = new ArrayList<>();
            List<PlayerListItem.Item> gamemodeList = new ArrayList<>();
            for (int var4 = 0; var4 < var3; ++var4) {
                PlayerListItem.Item item = var2[var4];
                this.uuids.remove(item.getUuid());
                if (!globalUUIDs.contains(item.getUuid())) {
                    itemList.add(item);
                } else {
                    item.setGamemode(3);
                    gamemodeList.add(item);
                }
            }

            if(!gamemodeList.isEmpty()){
                PlayerListItem pli = new PlayerListItem();
                pli.setAction(PlayerListItem.Action.UPDATE_GAMEMODE);
                pli.setItems(gamemodeList.toArray(new PlayerListItem.Item[gamemodeList.size()]));
                this.player.unsafe().sendPacket(pli);
            }

            playerListItem.setItems(itemList.toArray(new PlayerListItem.Item[itemList.size()]));
        }

        if (playerListItem.getItems().length > 0) this.player.unsafe().sendPacket(playerListItem);
    }

    @Synchronized
    public void onServerChange() {
        List<PlayerListItem.Item> removeList = new ArrayList<>();
        List<PlayerListItem.Item> gamemodeList = new ArrayList<>();

        for (UUID uuid : this.uuids) {
            PlayerListItem.Item item = new PlayerListItem.Item();
            item.setUuid(uuid);
            if (!globalUUIDs.contains(uuid)) {
                removeList.add(item);
            } else {
                item.setGamemode(3);
                gamemodeList.add(item);
            }
        }

        PlayerListItem packet = new PlayerListItem();
        packet.setAction(PlayerListItem.Action.REMOVE_PLAYER);
        packet.setItems(removeList.toArray(new PlayerListItem.Item[removeList.size()]));
        if (packet.getItems().length > 0) this.player.unsafe().sendPacket(packet);

        packet = new PlayerListItem();
        packet.setAction(PlayerListItem.Action.UPDATE_GAMEMODE);
        packet.setItems(gamemodeList.toArray(new PlayerListItem.Item[gamemodeList.size()]));
        if (packet.getItems().length > 0) this.player.unsafe().sendPacket(packet);

        this.uuids.clear();
    }

    public void onConnect() {
        for(ProxiedPlayer player: ProxyServer.getInstance().getPlayers()){
            onGlobalPlayerConnect(player);
        }

        super.onConnect();
    }

    @Synchronized
    public void onGlobalPlayerConnect(ProxiedPlayer player) {
        globalUUIDs.add(player.getUniqueId());
        PlayerListItem pli = new PlayerListItem();
        pli.setAction(PlayerListItem.Action.ADD_PLAYER);
        PlayerListItem.Item item = new PlayerListItem.Item();
        item.setPing(player.getPing());
        item.setUsername(player.getName());
        item.setGamemode(uuids.contains(player.getUniqueId()) ? ((UserConnection) player).getGamemode() : 3);
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
        pli.setItems(new PlayerListItem.Item[]{item});
        this.player.unsafe().sendPacket(pli);
    }

    @Synchronized
    public void onGlobalPlayerDisconnect(ProxiedPlayer player) {
        globalUUIDs.remove(player.getUniqueId());
        if(uuids.contains(player.getUniqueId())){
            return;
        }
        PlayerListItem pli = new PlayerListItem();
        pli.setAction(PlayerListItem.Action.REMOVE_PLAYER);
        PlayerListItem.Item item = new PlayerListItem.Item();
        item.setUuid(player.getUniqueId());
        pli.setItems(new PlayerListItem.Item[]{item});
        this.player.unsafe().sendPacket(pli);
    }

    @Synchronized
    public void onGlobalPlayerPingChange(ProxiedPlayer player, int ping) {
        if(uuids.contains(player.getUniqueId()))return;
        PlayerListItem pli = new PlayerListItem();
        pli.setAction(PlayerListItem.Action.UPDATE_LATENCY);
        PlayerListItem.Item item = new PlayerListItem.Item();
        item.setUuid(player.getUniqueId());
        item.setPing(ping);
        pli.setItems(new PlayerListItem.Item[]{item});
        this.player.unsafe().sendPacket(pli);
    }
}
