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
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.connection.LoginResult;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import net.md_5.bungee.tab.TabList;

import java.util.*;

/**
 * @author Florian Stober
 */
public class GlobalTablistHandler18 extends GlobalTablistHandlerBase {
    private final Collection<UUID> uuids = new HashSet<>();
    private final UUIDSet globalUUIDs = new UUIDSet();
    private final Map<UUID, String> displayNames = new HashMap<>();

    public GlobalTablistHandler18(ProxiedPlayer player, GlobalTablist plugin) {
        super(player, plugin);
    }

    @Override
    public void onUpdate(PlayerListItem playerListItem) {
        if (!plugin.getConfig().showPlayersOnOtherServersAsSpectators && playerListItem.getAction() == PlayerListItem.Action.UPDATE_GAMEMODE) {
            List<PlayerListItem.Item> itemList = new ArrayList<>();
            for (PlayerListItem.Item item : playerListItem.getItems()) {
                if (item.getUuid().equals(getPlayer().getUniqueId())) {
                    for (GlobalTablistHandlerBase tablistHandler : tablistHandlers) {
                        if (tablistHandler instanceof GlobalTablistHandler18) {
                            ((GlobalTablistHandler18) tablistHandler).onGlobalPlayerGamemodeChange(this.player, item.getGamemode());
                        }
                    }
                } else if (!globalUUIDs.contains(item.getUuid())) {
                    itemList.add(item);
                }
            }
            if (!itemList.isEmpty()) {
                playerListItem.setItems(itemList.toArray(new PlayerListItem.Item[itemList.size()]));
                this.player.unsafe().sendPacket(playerListItem);
            }
            return;
        }
        if (playerListItem.getAction() == PlayerListItem.Action.UPDATE_DISPLAY_NAME) {
            if (plugin.getConfig().forwardDisplayNames) {
                List<PlayerListItem.Item> itemList = new ArrayList<>();
                for (PlayerListItem.Item item : playerListItem.getItems()) {
                    if (item.getUuid().equals(getPlayer().getUniqueId())) {
                        for (GlobalTablistHandlerBase tablistHandler : tablistHandlers) {
                            if (tablistHandler instanceof GlobalTablistHandler18) {
                                ((GlobalTablistHandler18) tablistHandler).onGlobalPlayerDisplayNameChange(this.player, item.getDisplayName());
                            }
                        }
                    } else if (!globalUUIDs.contains(item.getUuid())) {
                        itemList.add(item);
                    }
                }
                if (!itemList.isEmpty()) {
                    playerListItem.setItems(itemList.toArray(new PlayerListItem.Item[itemList.size()]));
                    this.player.unsafe().sendPacket(playerListItem);
                }
            }
            return;
        }
        if (!plugin.getConfig().updatePing && playerListItem.getAction() == PlayerListItem.Action.UPDATE_LATENCY) {
            return;
        }
        onUpdate0(playerListItem);
    }

    @Synchronized
    private void onUpdate0(PlayerListItem playerListItem) {
        PlayerListItem.Item[] var2 = playerListItem.getItems();

        if (playerListItem.getAction() == PlayerListItem.Action.ADD_PLAYER) {
            for (PlayerListItem.Item item : var2) {
                this.uuids.add(item.getUuid());
                displayNames.remove(item.getUuid());
            }
        } else if (playerListItem.getAction() == PlayerListItem.Action.REMOVE_PLAYER) {
            List<PlayerListItem.Item> itemList = new ArrayList<>();
            List<PlayerListItem.Item> gamemodeList = new ArrayList<>();
            for (PlayerListItem.Item item : var2) {
                this.uuids.remove(item.getUuid());
                if (!globalUUIDs.contains(item.getUuid())) {
                    itemList.add(item);
                } else if (plugin.getConfig().showPlayersOnOtherServersAsSpectators) {
                    item.setGamemode(3);
                    gamemodeList.add(item);
                }
            }

            if (!gamemodeList.isEmpty()) {
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
    @Override
    public void onServerChange() {
        List<PlayerListItem.Item> removeList = new ArrayList<>();
        List<PlayerListItem.Item> gamemodeList = new ArrayList<>();

        for (UUID uuid : this.uuids) {
            PlayerListItem.Item item = new PlayerListItem.Item();
            item.setUuid(uuid);
            if (!globalUUIDs.contains(uuid)) {
                removeList.add(item);
            } else if (plugin.getConfig().showPlayersOnOtherServersAsSpectators) {
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

    @Synchronized
    @Override
    void onGlobalPlayerConnect(ProxiedPlayer player) {
        globalUUIDs.add(player.getUniqueId());
        PlayerListItem pli = new PlayerListItem();
        pli.setAction(PlayerListItem.Action.ADD_PLAYER);
        PlayerListItem.Item item = new PlayerListItem.Item();
        item.setPing(player.getPing());
        item.setUsername(player.getName());
        item.setGamemode(uuids.contains(player.getUniqueId()) || !plugin.getConfig().showPlayersOnOtherServersAsSpectators ? ((UserConnection) player).getGamemode() : 3);
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
        if (displayNames.containsKey(player.getUniqueId())) {
            pli = new PlayerListItem();
            pli.setAction(PlayerListItem.Action.UPDATE_DISPLAY_NAME);
            item = new PlayerListItem.Item();
            item.setUuid(player.getUniqueId());
            item.setDisplayName(displayNames.get(player.getUniqueId()));
            pli.setItems(new PlayerListItem.Item[]{item});
            this.player.unsafe().sendPacket(pli);
        }
    }

    @Synchronized
    @Override
    void onGlobalPlayerDisconnect(ProxiedPlayer player) {
        globalUUIDs.remove(player.getUniqueId());
        if (uuids.contains(player.getUniqueId()) || globalUUIDs.contains(player.getUniqueId())) {
            return;
        }
        PlayerListItem pli = new PlayerListItem();
        pli.setAction(PlayerListItem.Action.REMOVE_PLAYER);
        PlayerListItem.Item item = new PlayerListItem.Item();
        item.setUuid(player.getUniqueId());
        pli.setItems(new PlayerListItem.Item[]{item});
        this.player.unsafe().sendPacket(pli);
        displayNames.remove(player.getUniqueId());
    }

    @Synchronized
    @Override
    void onGlobalPlayerPingChange(ProxiedPlayer player, int ping) {
        if (uuids.contains(player.getUniqueId())) return;
        PlayerListItem pli = new PlayerListItem();
        pli.setAction(PlayerListItem.Action.UPDATE_LATENCY);
        PlayerListItem.Item item = new PlayerListItem.Item();
        item.setUuid(player.getUniqueId());
        item.setPing(ping);
        pli.setItems(new PlayerListItem.Item[]{item});
        this.player.unsafe().sendPacket(pli);
    }

    @Synchronized
    void onGlobalPlayerGamemodeChange(ProxiedPlayer player, int gamemode) {
        PlayerListItem pli = new PlayerListItem();
        pli.setAction(PlayerListItem.Action.UPDATE_GAMEMODE);
        PlayerListItem.Item item = new PlayerListItem.Item();
        item.setUuid(player.getUniqueId());
        item.setGamemode(gamemode);
        pli.setItems(new PlayerListItem.Item[]{item});
        this.player.unsafe().sendPacket(pli);
    }

    @Synchronized
    void onGlobalPlayerDisplayNameChange(ProxiedPlayer player, String name) {
        displayNames.put(player.getUniqueId(), name);
        PlayerListItem pli = new PlayerListItem();
        pli.setAction(PlayerListItem.Action.UPDATE_DISPLAY_NAME);
        PlayerListItem.Item item = new PlayerListItem.Item();
        item.setUuid(player.getUniqueId());
        item.setDisplayName(name);
        pli.setItems(new PlayerListItem.Item[]{item});
        this.player.unsafe().sendPacket(pli);
    }
}
