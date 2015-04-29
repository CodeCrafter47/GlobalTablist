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

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import net.md_5.bungee.tab.TabList;

/**
 * @author Florian Stober
 */
public abstract class GlobalTablistHandlerBase extends TabList {

    protected final GlobalTablist plugin;

    protected int lastPing = 0;

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
                for (ProxiedPlayer p : plugin.getProxy().getPlayers()) {
                    try {
                        TabList tablistHandler = GlobalTablist.getTablistHandler(p);
                        if (tablistHandler instanceof GlobalTablistHandlerBase) {
                            ((GlobalTablistHandlerBase) tablistHandler).onGlobalPlayerPingChange(this.player, i);
                        }
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public void onConnect() {
        // send players
        for (ProxiedPlayer p : plugin.getProxy().getPlayers()) {
            onGlobalPlayerConnect(p);
            if (p != this.player) {
                try {
                    TabList tablistHandler = GlobalTablist.getTablistHandler(p);
                    if (tablistHandler instanceof GlobalTablistHandlerBase) {
                        ((GlobalTablistHandlerBase) tablistHandler).onGlobalPlayerConnect(this.player);
                    }
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        // store ping
        lastPing = getPlayer().getPing();
    }

    @Override
    public void onDisconnect() {
        // remove player
        for (ProxiedPlayer p : plugin.getProxy().getPlayers()) {
            onGlobalPlayerDisconnect(p);
            if (p != this.player) {
                try {
                    TabList tablistHandler = GlobalTablist.getTablistHandler(p);
                    if (tablistHandler instanceof GlobalTablistHandlerBase) {
                        ((GlobalTablistHandlerBase) tablistHandler).onGlobalPlayerDisconnect(this.player);
                    }
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    abstract void onGlobalPlayerConnect(ProxiedPlayer player);

    abstract void onGlobalPlayerDisconnect(ProxiedPlayer player);

    abstract void onGlobalPlayerPingChange(ProxiedPlayer player, int ping);
}
