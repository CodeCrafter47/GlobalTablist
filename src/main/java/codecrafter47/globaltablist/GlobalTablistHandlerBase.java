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

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TLinkedHashSet;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import net.md_5.bungee.tab.TabList;

import java.util.Collections;
import java.util.Set;

public abstract class GlobalTablistHandlerBase extends TabList {
    protected final GlobalTablist plugin;

    protected int lastPing = 0;
    protected boolean connected = false;

    protected static Set<GlobalTablistHandlerBase> tablistHandlers = Collections.synchronizedSet(new TLinkedHashSet<GlobalTablistHandlerBase>());

    public GlobalTablistHandlerBase(ProxiedPlayer player, GlobalTablist plugin) {
        super(player);
        this.plugin = plugin;
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
    public void onPingChange(int i) {
        if (plugin.getConfig().updatePing) {
            if (lastPing - i > 50 || lastPing - i < 50) {
                for (GlobalTablistHandlerBase tablistHandler : tablistHandlers) {
                    tablistHandler.onGlobalPlayerPingChange(getPlayer(), i);
                }
                lastPing = i;
            }
        }
    }

    @Override
    public void onConnect() {
        if (!connected) {
            connected = true;
            tablistHandlers.add(this);
            // send players
            for (GlobalTablistHandlerBase tablistHandler : tablistHandlers) {
                tablistHandler.onGlobalPlayerConnect(getPlayer());
                if (tablistHandler != this) {
                    onGlobalPlayerConnect(tablistHandler.getPlayer());
                }
            }

            // store ping
            lastPing = getPlayer().getPing();
        }
    }

    @Override
    public void onDisconnect() {
        if (connected) {
            connected = false;
            // remove player
            for (GlobalTablistHandlerBase tablistHandler : tablistHandlers) {
                tablistHandler.onGlobalPlayerDisconnect(getPlayer());
                if (tablistHandler != this) {
                    onGlobalPlayerDisconnect(tablistHandler.getPlayer());
                }
            }
            tablistHandlers.remove(this);

            // hack to revert changes from https://github.com/SpigotMC/BungeeCord/commit/830f18a35725f637d623594eaaad50b566376e59
            Server server = getPlayer().getServer();
            if (server != null) {
                server.disconnect("Quitting");
            }
            ((UserConnection) getPlayer()).setServer(null);
        }
    }

    abstract void onGlobalPlayerConnect(ProxiedPlayer player);

    abstract void onGlobalPlayerDisconnect(ProxiedPlayer player);

    abstract void onGlobalPlayerPingChange(ProxiedPlayer player, int ping);
}
