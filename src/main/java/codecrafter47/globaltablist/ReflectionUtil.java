package codecrafter47.globaltablist;

import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.tab.TabList;

import java.lang.reflect.Field;

public class ReflectionUtil {
    public static void setTablistHandler(ProxiedPlayer player, TabList tablistHandler) throws NoSuchFieldException, IllegalAccessException {
        setField(UserConnection.class, player, "tabListHandler", tablistHandler, 5);
    }

    public static TabList getTablistHandler(ProxiedPlayer player) throws NoSuchFieldException, IllegalAccessException {
        return getField(UserConnection.class, player, "tabListHandler", 5);
    }

    public static ChannelWrapper getChannelWrapper(ProxiedPlayer player) throws NoSuchFieldException, IllegalAccessException {
        return getField(UserConnection.class, player, "ch", 50);
    }

    public static void setField(Class<?> clazz, Object instance, String field, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field f = clazz.getDeclaredField(field);
        f.setAccessible(true);
        f.set(instance, value);
    }

    public static void setField(Class<?> clazz, Object instance, String field, Object value, int tries) throws NoSuchFieldException, IllegalAccessException {
        while (--tries > 0) {
            try {
                setField(clazz, instance, field, value);
                return;
            } catch (NoSuchFieldException | IllegalAccessException ignored) {
            }
        }
        setField(clazz, instance, field, value);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getField(Class<?> clazz, Object instance, String field) throws NoSuchFieldException, IllegalAccessException {
        Field f = clazz.getDeclaredField(field);
        f.setAccessible(true);
        return (T) f.get(instance);
    }

    public static <T> T getField(Class<?> clazz, Object instance, String field, int tries) throws NoSuchFieldException, IllegalAccessException {
        while (--tries > 0) {
            try {
                return getField(clazz, instance, field);
            } catch (NoSuchFieldException | IllegalAccessException ignored) {
            }
        }
        return getField(clazz, instance, field);
    }
}
