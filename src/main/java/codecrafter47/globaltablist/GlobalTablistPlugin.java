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

import codecrafter47.globaltablist.placeholders.*;
import codecrafter47.globaltablist.placeholders.redis.RedisOnlineCountPlaceholder;
import codecrafter47.globaltablist.placeholders.redis.RedisServerOnlineCountPlaceholder;
import de.codecrafter47.globaltablist.GlobalTablist;
import de.codecrafter47.globaltablist.GlobalTablistAPI;
import de.codecrafter47.globaltablist.Placeholder;
import lombok.Getter;
import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;

/**
 * Main Class of GlobalTablist
 *
 * @author Florian Stober
 */
public class GlobalTablistPlugin extends Plugin implements GlobalTablistAPI {

    /**
     * Holds an INSTANCE of itself if the plugin is enabled
     */
    @Getter
    private static GlobalTablistPlugin INSTANCE;
    private CustomizationHandler customizationHandler;

    @Getter
    private Set<Placeholder> placeholders = new CopyOnWriteArraySet<>();

    /**
     * provides access to the configuration
     */
    @Getter
    private MainConfig config;

    private final TabListListener listener = new TabListListener(this);
    @Getter
    private final PingPlaceholder pingPlaceholder = new PingPlaceholder();

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

        // set instance
        INSTANCE = this;

        // load config
        try {
            if (!getDataFolder().exists()) getDataFolder().mkdirs();
            config = new MainConfig();
            config.init(new File(getDataFolder(), "config.yml"));
            config.save();
        } catch (InvalidConfigurationException ex) {
            getLogger().warning("Unable to load Config");
            getLogger().log(Level.WARNING, null, ex);
            getLogger().info("Disabling Plugin");
            return;
        }

        // initialize API
        GlobalTablist.setAPI(this);

        // register built-in placeholders
        registerPlaceholders();

        // load customization handler
        if (config.showHeaderFooter) customizationHandler = new CustomizationHandler(this);

        // register listener
        ProxyServer.getInstance().getPluginManager().registerListener(this, listener);

        // Start metrics
        try {
            Metrics metrics = new Metrics(this);
            metrics.start();
        } catch (Throwable th) {
            getLogger().log(Level.WARNING, "Failed to initialize Metrics", th);
        }
    }

    private void registerPlaceholders() {
        registerPlaceholder(this, new ListenerMaxPlayersPlaceholder());
        registerPlaceholder(this, new NewLinePlaceholder());
        registerPlaceholder(this, new OnlineCountPlaceholder(this));
        registerPlaceholder(this, pingPlaceholder);
        registerPlaceholder(this, new PlayerLimitPlaceholder());
        registerPlaceholder(this, new PlayerNamePlaceholder());
        for (String server : getProxy().getServers().keySet()) {
            registerPlaceholder(this, new ServerOnlineCountPlaceholder(this, server));
        }
        registerPlaceholder(this, new ServerPlaceholder(this));

        if (getProxy().getPluginManager().getPlugin("RedisBungee") != null) {
            registerPlaceholder(this, new RedisOnlineCountPlaceholder(this));
            for (String server : getProxy().getServers().keySet()) {
                registerPlaceholder(this, new RedisServerOnlineCountPlaceholder(this, server));
            }
        }
    }

    /**
     * called when the plugin is disabled
     */
    @Override
    public void onDisable() {
        // let the proxy do this
    }

    @Override
    public void registerPlaceholder(Plugin plugin, Placeholder placeholder) {
        placeholders.add(placeholder);
        if (customizationHandler != null) {
            customizationHandler.reload();
        }
    }
}
