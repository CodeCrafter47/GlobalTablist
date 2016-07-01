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
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import net.md_5.bungee.tab.TabList;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public abstract class GlobalTablistHandlerBase extends TabList {
    protected final GlobalTablist plugin;

    protected int lastPing = 0;
    protected boolean connected = false;

    protected static Set<GlobalTablistHandlerBase> tablistHandlers = Collections.newSetFromMap(new ConcurrentHashMap<GlobalTablistHandlerBase, Boolean>());

    public GlobalTablistHandlerBase(ProxiedPlayer player, GlobalTablist plugin) {
        super(player);
        this.plugin = plugin;
    }

    public void failIfNotInEventLoop() {
        ChannelWrapper ch;
        try {
            ch = ReflectionUtil.getChannelWrapper(getPlayer());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            plugin.getLogger().log(Level.SEVERE, "failed to get ChannelWrapper for player", e);
            return;
        }
        if (!ch.getHandle().eventLoop().inEventLoop()) {
            throw new IllegalStateException("not in event loop");
        }
    }

    public void executeInEventLoop(Runnable runnable) {
        ChannelWrapper ch;
        try {
            ch = ReflectionUtil.getChannelWrapper(getPlayer());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            plugin.getLogger().log(Level.SEVERE, "failed to get ChannelWrapper for player", e);
            return;
        }
        if (ch.getHandle().eventLoop().inEventLoop()) {
            runnable.run();
        } else {
            ch.getHandle().eventLoop().execute(runnable);
        }
    }

    protected ProxiedPlayer getPlayer() {
        return player;
    }

    @Override
    public void onServerChange() {
        // stub
    }

    @Override
    public void onUpdate(PlayerListItem pli) {
        // stub
    }

    @Override
    public void onPingChange(final int i) {
        failIfNotInEventLoop();
        if (plugin.getConfig().updatePing) {
            if (lastPing - i > 50 || lastPing - i < 50) {
                for (final GlobalTablistHandlerBase tablistHandler : tablistHandlers) {
                    tablistHandler.executeInEventLoop(new Runnable() {
                        @Override
                        public void run() {
                            tablistHandler.onGlobalPlayerPingChange(getPlayer(), i);
                        }
                    });
                }
                lastPing = i;
            }
        }
    }

    @Override
    public void onConnect() {
        failIfNotInEventLoop();
        if (!connected) {
            connected = true;
            tablistHandlers.add(this);
            // send players
            for (final GlobalTablistHandlerBase tablistHandler : tablistHandlers) {
                tablistHandler.executeInEventLoop(new Runnable() {
                    @Override
                    public void run() {
                        tablistHandler.onGlobalPlayerConnect(getPlayer());
                    }
                });
                if (tablistHandler != this) {
                    executeInEventLoop(new Runnable() {
                        @Override
                        public void run() {
                            onGlobalPlayerConnect(tablistHandler.getPlayer());
                        }
                    });
                }
            }

            // store ping
            lastPing = getPlayer().getPing();
        }
    }

    @Override
    public void onDisconnect() {
        failIfNotInEventLoop();
        if (connected) {
            connected = false;
            // remove player
            for (final GlobalTablistHandlerBase tablistHandler : tablistHandlers) {
                tablistHandler.executeInEventLoop(new Runnable() {
                    @Override
                    public void run() {
                        tablistHandler.onGlobalPlayerDisconnect(getPlayer());
                    }
                });
                if (tablistHandler != this) {
                    executeInEventLoop(new Runnable() {
                        @Override
                        public void run() {
                            onGlobalPlayerDisconnect(tablistHandler.getPlayer());
                        }
                    });
                }
            }
            tablistHandlers.remove(this);

            if (getPlayer() != ProxyServer.getInstance().getPlayer(getPlayer().getName())) {
                // hack to revert changes from https://github.com/SpigotMC/BungeeCord/commit/830f18a35725f637d623594eaaad50b566376e59
                Server server = getPlayer().getServer();
                if (server != null) {
                    server.disconnect("Quitting");
                }
                ((UserConnection) getPlayer()).setServer(null);
            }
        }
    }

    abstract void onGlobalPlayerConnect(ProxiedPlayer player);

    abstract void onGlobalPlayerDisconnect(ProxiedPlayer player);

    abstract void onGlobalPlayerPingChange(ProxiedPlayer player, int ping);
}
