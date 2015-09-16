package codecrafter47.globaltablist;

import gnu.trove.impl.Constants;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.UUID;

public class UUIDSet {
    private final TObjectIntMap<UUID> map = new TObjectIntHashMap<>(Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, 0);

    public void add(UUID uuid) {
        map.put(uuid, map.get(uuid) + 1);
    }

    public void remove(UUID uuid) {
        if (map.get(uuid) > 1) {
            map.put(uuid, map.get(uuid) - 1);
        } else {
            map.remove(uuid);
        }
    }

    public boolean contains(UUID uuid) {
        return map.containsKey(uuid);
    }
}
