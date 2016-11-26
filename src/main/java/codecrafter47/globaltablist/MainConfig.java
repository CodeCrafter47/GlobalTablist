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

import net.cubespace.Yamler.Config.Comments;
import net.cubespace.Yamler.Config.Config;

import java.util.Arrays;
import java.util.List;

public class MainConfig extends Config {

    public MainConfig() {
        super();
        CONFIG_HEADER = new String[]{
                "This is the config file of GlobalTabList,",
                "An innovative and fast tablist plugin",
                "",
                "Variables you can use in header, footer and custom slots:",
                " - {player} will be replaced with the name of the player",
                " - {ping} ping of the player",
                " - {newline} will insert a linebreak, only in header and footer",
                " - {server} the server the player is playing on",
                " - {online} total number of players online",
                " - {online_<server>} numbers of players on a specific server, <server> must be replaced wit the name of the server",
                " - {max} maximum amount of players that may connect to this bungee instance",
                " - {shown_max} player maximum as shown to clients in the server list",
                " - {redis_online} if using RedisBungee the amount of players online on your network",
                " - {redis_online_<server>} amount of players on a specific server if using RedisBungee"
        };
    }

    @Comments({
            "true: global tablist",
            "false: server unique tablist"
    })
    public boolean useGlobalTablist = true;

    @Comments({
            "whether ping is sent to clients",
            "setting this to false can help you reducing network traffic"
    })
    public boolean updatePing = false;

    @Comments({
            "Whether to send header/footer to the clients or not"
    })
    public boolean showHeaderFooter = true;

    @Comments({
            "This text will be shown above the tablist on 1.8 clients"
    })
    public String header = "&6Welcome &f{player}";

    @Comments({
            "This text will be shown below the tablist on 1.8 clients"
    })
    public String footer = "&4minecraft.net";

    @Comments({
            "Whether to pass through display names retrieved from bukkit/ spigot",
            "if enabled you can get tab colors using a plugin like TabPrefixes(http://www.spigotmc.org/resources/tabprefixes.4132/)",
            "NOTE: only 1.8 clients will be able to see the colored names"
    })
    public boolean forwardDisplayNames = true;

    @Comments({
            "Shows players on other servers as spectators",
            "This makes it easy to tell who's on the same server as you",
            "This also fixes a bug in spectator mode where players ",
            "on other servers show up in the teleport to list"
    })
    public boolean showPlayersOnOtherServersAsSpectators = true;

    @Comments({
            "On 1.7 clients this replaces the missing header and footer.",
            "You can add some custom text slots at the top of the player list"
    })
    public List<String> custom_lines_top = Arrays.asList("&6Welcome", "&6{player}", "&6to our server", "", "", "");
}
