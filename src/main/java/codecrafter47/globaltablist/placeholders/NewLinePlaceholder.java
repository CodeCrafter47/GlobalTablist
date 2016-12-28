package codecrafter47.globaltablist.placeholders;

import de.codecrafter47.globaltablist.Placeholder;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.regex.Matcher;

/**
 * New line placeholder. Replaces '{newline}' with the new line character ('\n'). For people who can't use yaml.
 */
public final class NewLinePlaceholder extends Placeholder {
    public NewLinePlaceholder() {
        super("{newline}");
    }

    @Override
    protected void onActivate() {
        // nothing to do, this placeholder never changes
    }

    @Override
    protected void onDeactivate() {
        // nothing to do, this placeholder never changes
    }

    @Override
    public String getReplacement(ProxiedPlayer player, Matcher matcher) {
        // return the new line character
        return "\n";
    }
}
