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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Florian Stober
 */
public class GlobalTablistHandler18 extends GlobalTablistHandlerBase {
    private final Collection<UUID> uuids = new HashSet<>();
    private final UUIDSet globalUUIDs = new UUIDSet();
    private static final Map<UUID, String> displayNames = new ConcurrentHashMap<>();

    public GlobalTablistHandler18(ProxiedPlayer player, GlobalTablist plugin) {
        super(player, plugin);
    }

    @Override
    public void onDisconnect() {
        displayNames.remove(getPlayer().getUniqueId());

        super.onDisconnect();
    }

    @Override
    public void onUpdate(PlayerListItem playerListItem) {
        failIfNotInEventLoop();
        if (!plugin.getConfig().showPlayersOnOtherServersAsSpectators && playerListItem.getAction() == PlayerListItem.Action.UPDATE_GAMEMODE) {
            List<PlayerListItem.Item> itemList = new ArrayList<>();
            for (final PlayerListItem.Item item : playerListItem.getItems()) {
                if (item.getUuid().equals(getPlayer().getUniqueId())) {
                    for (final GlobalTablistHandlerBase tablistHandler : tablistHandlers) {
                        if (tablistHandler instanceof GlobalTablistHandler18) {
                            tablistHandler.executeInEventLoop(new Runnable() {
                                @Override
                                public void run() {
                                    ((GlobalTablistHandler18) tablistHandler).onGlobalPlayerGamemodeChange(getPlayer(), item.getGamemode());
                                }
                            });
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
                for (final PlayerListItem.Item item : playerListItem.getItems()) {
                    if (item.getUuid().equals(getPlayer().getUniqueId())) {
                        final String displayName = item.getDisplayName();
                        if (displayName != null) {
                            displayNames.put(item.getUuid(), displayName);
                        } else {
                            displayNames.remove(item.getUuid());
                        }
                        for (final GlobalTablistHandlerBase tablistHandler : tablistHandlers) {
                            if (tablistHandler instanceof GlobalTablistHandler18) {
                                tablistHandler.executeInEventLoop(new Runnable() {
                                    @Override
                                    public void run() {
                                        ((GlobalTablistHandler18) tablistHandler).onGlobalPlayerDisplayNameChange(getPlayer(), displayName);
                                    }
                                });
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

    private void onUpdate0(PlayerListItem playerListItem) {
        PlayerListItem.Item[] var2 = playerListItem.getItems();

        if (playerListItem.getAction() == PlayerListItem.Action.ADD_PLAYER) {
            for (PlayerListItem.Item item : var2) {
                this.uuids.add(item.getUuid());
                if (getPlayer().getUniqueId().equals(item.getUuid())) {
                    displayNames.remove(item.getUuid());
                }
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

    @Override
    public void onServerChange() {
        failIfNotInEventLoop();

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

    @Override
    void onGlobalPlayerConnect(ProxiedPlayer player) {
        failIfNotInEventLoop();
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

    @Override
    void onGlobalPlayerDisconnect(ProxiedPlayer player) {
        failIfNotInEventLoop();
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
    }

    @Override
    void onGlobalPlayerPingChange(ProxiedPlayer player, int ping) {
        failIfNotInEventLoop();
        if (uuids.contains(player.getUniqueId())) return;
        PlayerListItem pli = new PlayerListItem();
        pli.setAction(PlayerListItem.Action.UPDATE_LATENCY);
        PlayerListItem.Item item = new PlayerListItem.Item();
        item.setUuid(player.getUniqueId());
        item.setPing(ping);
        pli.setItems(new PlayerListItem.Item[]{item});
        this.player.unsafe().sendPacket(pli);
    }

    void onGlobalPlayerGamemodeChange(ProxiedPlayer player, int gamemode) {
        failIfNotInEventLoop();
        PlayerListItem pli = new PlayerListItem();
        pli.setAction(PlayerListItem.Action.UPDATE_GAMEMODE);
        PlayerListItem.Item item = new PlayerListItem.Item();
        item.setUuid(player.getUniqueId());
        item.setGamemode(gamemode);
        pli.setItems(new PlayerListItem.Item[]{item});
        this.player.unsafe().sendPacket(pli);
    }

    void onGlobalPlayerDisplayNameChange(ProxiedPlayer player, String name) {
        failIfNotInEventLoop();
        PlayerListItem pli = new PlayerListItem();
        pli.setAction(PlayerListItem.Action.UPDATE_DISPLAY_NAME);
        PlayerListItem.Item item = new PlayerListItem.Item();
        item.setUuid(player.getUniqueId());
        item.setDisplayName(name);
        pli.setItems(new PlayerListItem.Item[]{item});
        this.player.unsafe().sendPacket(pli);
    }
}
