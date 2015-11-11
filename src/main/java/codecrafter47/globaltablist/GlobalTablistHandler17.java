package codecrafter47.globaltablist;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.protocol.packet.PlayerListItem;

public class GlobalTablistHandler17 extends GlobalTablistHandlerBase {
    TIntSet createdCustomSlots = new TIntHashSet(0);

    public GlobalTablistHandler17(ProxiedPlayer player, GlobalTablist plugin) {
        super(player, plugin);
    }

    @Override
    public void onConnect() {
        for (int i = 0; i < plugin.getConfig().custom_lines_top.size() && i < CustomizationHandler.fakePlayers.length; i++) {
            PlayerListItem pli = new PlayerListItem();
            pli.setAction(PlayerListItem.Action.ADD_PLAYER);
            PlayerListItem.Item item = new PlayerListItem.Item();
        item.setDisplayName(CustomizationHandler.fakePlayers[i]);
            item.setPing(0);
            pli.setItems(new PlayerListItem.Item[]{item});
            player.unsafe().sendPacket(pli);
        }
        super.onConnect();
    }

    @Override
    void onGlobalPlayerConnect(ProxiedPlayer player) {
        PlayerListItem pli = new PlayerListItem();
        pli.setAction(PlayerListItem.Action.ADD_PLAYER);
        PlayerListItem.Item item = createBasicItem(player);
        item.setPing(player.getPing());
        pli.setItems(new PlayerListItem.Item[]{item});
        this.player.unsafe().sendPacket(pli);
    }

    @Override
    void onGlobalPlayerDisconnect(ProxiedPlayer player) {
        PlayerListItem pli = new PlayerListItem();
        pli.setAction(PlayerListItem.Action.REMOVE_PLAYER);
        PlayerListItem.Item item = createBasicItem(player);
        pli.setItems(new PlayerListItem.Item[]{item});
        this.player.unsafe().sendPacket(pli);
    }

    @Override
    void onGlobalPlayerPingChange(ProxiedPlayer player, int ping) {
        PlayerListItem pli = new PlayerListItem();
        pli.setAction(PlayerListItem.Action.UPDATE_LATENCY);
        PlayerListItem.Item item = createBasicItem(player);
        item.setPing(ping);
        pli.setItems(new PlayerListItem.Item[]{item});
        this.player.unsafe().sendPacket(pli);
    }

    private PlayerListItem.Item createBasicItem(ProxiedPlayer player) {
        PlayerListItem.Item item = new PlayerListItem.Item();
        String text = player.getDisplayName();
        if (text.length() > 16) {
            text = text.substring(0, 16);
        }
        item.setDisplayName(text);
        return item;
    }
}
