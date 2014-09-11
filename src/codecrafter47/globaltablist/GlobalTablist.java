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

import java.lang.reflect.Field;
import java.util.logging.Level;
import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

/**
 * Main Class of BungeeTabListPlus
 *
 * @author Florian Stober
 */
public class GlobalTablist extends Plugin {

    /**
     * Holds an INSTANCE of itself if the plugin is enabled
     */
    private static GlobalTablist INSTANCE;

    /**
     * Static getter for the current instance of the plugin
     *
     * @return the current instance of the plugin, null if the plugin is
     * disabled
     */
    public static GlobalTablist getInstance() {
        return INSTANCE;

    }

    /**
     * provides access to the configuration
     */
    private MainConfig config;

    private final TabListListener listener = new TabListListener(this);

    /**
     * Called when the plugin is enabled
     */
    @Override
    public void onEnable() {
        // check whether bungee version is supported
        try {
            Class.forName("net.md_5.bungee.api.Title");
        } catch (ClassNotFoundException ex) {
            getLogger().severe(
                    "This plugin does not support your BungeeCord version");
            getLogger().severe("Please use build #996 or above");
            return;
        }

        INSTANCE = this;
        try {
            config = new MainConfig(this);
        } catch (InvalidConfigurationException ex) {
            getLogger().warning("Unable to load Config");
            getLogger().log(Level.WARNING, null, ex);
            getLogger().info("Disabling Plugin");
            return;
        }

        ProxyServer.getInstance().getPluginManager().registerListener(this,
                listener);
    }

    /**
     * called when the plugin is disabled
     */
    @Override
    public void onDisable() {
        // let the proxy do this
    }

    public MainConfig getConfig() {
        return config;
    }

    public void reportError(Throwable th) {
        getLogger().log(Level.WARNING,
                ChatColor.RED + "An internal error occured! Please send the "
                + "following stacktrace to the developer in order to help"
                + " resolving the problem",
                th);
    }

    public static void setTabList(ProxiedPlayer player, Object tabList) throws
            IllegalArgumentException, IllegalAccessException,
            NoSuchFieldException {
        Class cplayer = UserConnection.class;
        Field tabListHandler = cplayer.getDeclaredField("tabListHandler");
        tabListHandler.setAccessible(true);
        tabListHandler.set(player, tabList);
    }
}
