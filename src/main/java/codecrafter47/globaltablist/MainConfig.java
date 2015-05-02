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
import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class MainConfig extends Config {

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
            "This text will be shown above the tablist on 1.8 clients",
            " - {player} will be replaced with the name of the player",
            " - {newline} will insert a linebreak"
    })
    public String header = "&6Welcome &f{player}";

    @Comments({
            "This text will be shown below the tablist on 1.8 clients",
            " - {player} will be replaced with the name of the player",
            " - {newline} will insert a linebreak"
    })
    public String footer = "&4minecraft.net";

    @Comments({
            "Shows players on other servers as spectators",
            "This makes it easy to tell who's on the same server as you",
            "This also fixes a bug in spectator mode where players ",
            "on other servers show up in the teleport to list"
    })
    public boolean showPlayersOnOtherServersAsSpectators = true;

    @Comments({
            "On 1.7 clients this replaces the missing header and footer.",
            "You can add some custom text slots at the top of the player list",
            " - {player} will be replaced with the name of the player"
    })
    public List<String> custom_lines_top = Arrays.asList("&6Welcome", "&6{player}", "&6to our server", "", "", "");

    public MainConfig(Plugin plugin) throws InvalidConfigurationException {
        CONFIG_FILE = new File("plugins" + File.separator + plugin.
                getDescription().getName(), "config.yml");
        CONFIG_HEADER = new String[]{
                "",
                ""};

        this.init();
    }
}
