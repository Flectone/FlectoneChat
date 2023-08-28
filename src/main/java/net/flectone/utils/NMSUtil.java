package net.flectone.utils;

import com.google.common.reflect.ClassPath;
import net.flectone.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NMSUtil {

    private static final Map<String, String> entityKeys = getEntityKeys();
    private static Class<?> bannerClass;
    private static Method nmsStackSaveMethod = null;
    private static double version;

    // Put the version into a double
    static {
        try {
            String version = Bukkit.getVersion(); // "git-Paper-153 MC: 1.13.2"
            String[] spaceSplit = version.split(" "); // { "git-Paper-153", "MC:", "1.13.2" }
            String numberVersion = spaceSplit[2].replace("(", "").replace(")", ""); // "1.13.2"
            String[] dotSplit = numberVersion.split("\\."); // { "1", "13", "2" }
            NMSUtil.version = Double.parseDouble(dotSplit[0] + "." + dotSplit[1]); // 1.13

            bannerClass = getItemBannerClass();
        } catch (Exception e) {
            // In case anything goes wrong, assume it's a newer version
            version = 1.18;
        }
    }

    @NotNull
    public static String getCorrectlyName(@Nullable ItemStack itemStack) {
        if (itemStack == null) return "";
        return itemStack.getItemMeta() == null || itemStack.getItemMeta().getDisplayName().isEmpty()
                ? getMinecraftName(itemStack)
                : net.md_5.bungee.api.ChatColor.ITALIC + itemStack.getItemMeta().getDisplayName();
    }

    public static String getMinecraftName(ItemStack is) {
        try {
            if (version >= 1.18) {
                Material material = is.getType();
                return (material.isBlock() ? "block" : "item") + ".minecraft." + material.toString().toLowerCase();
            }

            Object nmsStack = asNMSCopy(is);

            Object item = nmsStack.getClass().getMethod("getItem").invoke(nmsStack);

            // We can do a simple version check like this which should work for future versions
            if (version > 1.12) {
                return (String) item.getClass().getMethod("getName").invoke(item);
            } else {
                if (bannerClass.isAssignableFrom(item.getClass())) {
                    Object enumColor = item.getClass().getMethod("c", nmsStack.getClass()).invoke(item, nmsStack);
                    String color = enumColor.getClass().getMethod("d").invoke(enumColor).toString();

                    return "item.banner." + color + ".name";
                } else {
                    return item.getClass().getMethod("a", nmsStack.getClass()).invoke(item, nmsStack).toString() +
                            ".name";
                }
            }
        } catch (Exception ex) {
            return "";
        }
    }

    public static String getMinecraftType(Entity entity) {
        if (entity == null) return "";

        String string = entityKeys.get(entity.getType().name().toUpperCase());

        if (string == null) string = "entity.minecraft." + entity.getType().name().toLowerCase();

        return string;
    }

    public static String getMinecraftName(Entity entity) {
        if (entity == null) return "";

        if (entity.getCustomName() != null) return entity.getCustomName();

        return getMinecraftType(entity);
    }

    /**
     * @return 0 if the item has never been repaired or -1 if it is no longer repairable.
     */
    public static int getXPForRepair(ItemStack is) {
        try {
            Object nmsStack = asNMSCopy(is);

            if (version >= 1.18) {
                int cost = (int) is.getItemMeta().serialize().getOrDefault("repair-cost", 0);
                boolean repairable = cost <= 40;
                return repairable ? cost : -1;
            }

            boolean hasTag = (boolean) nmsStack.getClass().getMethod("hasTag").invoke(nmsStack);

            if (!hasTag)
                return 0;

            Object tag = nmsStack.getClass().getMethod("getTag").invoke(nmsStack);

            boolean hasKey = (boolean) tag.getClass().getMethod("hasKey", String.class).invoke(tag, "RepairCost");

            if (!hasKey)
                return 0;

            int cost = (int) tag.getClass().getMethod("getInt", String.class).invoke(tag, "RepairCost");

            boolean repairable = cost <= 40;

            if (repairable)
                return cost;
            else
                return -1;
        } catch (Exception ex) {
            return 0;
        }
    }

    public static String getItemAsJson(ItemStack is) {
        String itemJson;

        try {
            Object nmsStack = asNMSCopy(is);

            // loop through methods until we find the save method, identified by a return type and input parameter
            // of NBTTagCompound
            if (version >= 1.18) {
                Class<?> nbtTagCompoundClass = getNBTTagCompoundClass();
                if (nmsStackSaveMethod == null) {
                    for (Method method : nmsStack.getClass().getMethods()) {
                        if (method.getReturnType().equals(nbtTagCompoundClass) && method.getParameterCount() == 1
                                && method.getParameterTypes()[0].equals(nbtTagCompoundClass)) {
                            nmsStackSaveMethod = method;
                            break;
                        }
                    }
                }

                Object emptyTag = nbtTagCompoundClass.getConstructor().newInstance();
                Object savedTag = nmsStackSaveMethod.invoke(nmsStack, emptyTag);
                itemJson = savedTag.toString();
            } else {
                Class<?> nbtTagCompoundClazz = getNBTTagCompoundClass();
                Method saveMethod = nmsStack.getClass().getMethod("save", nbtTagCompoundClazz);

                Object nmsNbtTagCompoundObj = nbtTagCompoundClazz.getConstructor().newInstance();
                Object jsonItem = saveMethod.invoke(nmsStack, nmsNbtTagCompoundObj);

                itemJson = jsonItem.toString();
            }
        } catch (Exception ex) {
            return "";
        }

        // Prevent sending a packet that could be mishandled by bungeecord
        if (itemJson.getBytes(StandardCharsets.UTF_8).length > Short.MAX_VALUE) {
            itemJson = getItemAsJson(new ItemStack(is.getType(), 1));
        }

        return itemJson;
    }

    private static Object asNMSCopy(ItemStack is) {
        try {
            return is.getClass().getMethod("asNMSCopy", ItemStack.class).invoke(null, is);
        } catch (Exception ex) {
            return null;
        }
    }

    private static Class<?> getItemBannerClass() {
        if (version >= 1.17) {
            try {
                return Class.forName("net.minecraft.world.item.ItemBanner");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return getNMSClass("ItemBanner");
        }
    }

    private static Class<?> getNBTTagCompoundClass() {
        if (version >= 1.17) {
            try {
                return Class.forName("net.minecraft.nbt.NBTTagCompound");
            } catch (ClassNotFoundException ex) {
                return null;
            }
        } else {
            return getNMSClass("NBTTagCompound");
        }
    }

    private static Class<?> getNMSClass(String name) {
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

        try {
            return Class.forName("net.minecraft.server." + version + "." + name);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static void registerClasses(String packageName, RegisterInterface registerInterface) {
        try {
            for (ClassPath.ClassInfo classInfo : ClassPath.from(Main.getInstance().getClass().getClassLoader()).getTopLevelClassesRecursive(packageName)) {

                Class<?> c = Class.forName(classInfo.getName());

                registerInterface.register(c);
            }

        } catch (IOException | NoSuchMethodException | ClassNotFoundException | InstantiationException |
                 IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, String> getEntityKeys() {
        final LinkedHashMap<String, String> keys = new LinkedHashMap<>();
        keys.put("LIGHTNING", "entity.minecraft.lightning_bolt");
        keys.put("FIREWORK", "entity.minecraft.firework_rocket");
        keys.put("DROPPED_ITEM", "entity.minecraft.item");
        keys.put("SPLASH_POTION", "item.minecraft.splash_potion"); // added
        keys.put("LINGERING_POTION", "item.minecraft.lingering_potion"); // added
        keys.put("PIG_ZOMBIE", "entity.minecraft.zombified_piglin");
        keys.put("ZOMBIE_VILLAGER", "entity.minecraft.villager");
        keys.put("BLACK_CAT", "entity.minecraft.cat");
        keys.put("RED_CAT", "entity.minecraft.cat");
        keys.put("SIAMESE_CAT", "entity.minecraft.cat");
        keys.put("RABBIT.THE_KILLER_BUNNY", "entity.minecraft.killer_bunny");
        keys.put("LLAMA_SPIT", "entity.minecraft.llama");
        keys.put("PRIMED_TNT", "entity.minecraft.tnt");
        keys.put("MINECART", "entity.minecraft.minecart");
        keys.put("MINECART_HOPPER", "entity.minecraft.minecart_hopper");
        keys.put("MINECART_CHEST", "entity.minecraft.minecart_chest");
        keys.put("MINECART_COMMAND", "item.minecraft.minecartcommand_block");
        keys.put("MINECART_FURNACE", "item.minecraft.minecart_furnace");
        keys.put("MINECART_MOB_SPAWNER", "entity.minecraft.minecart");
        keys.put("UNKNOWN", "entity.minecraft.generic");
        keys.put("ENDER_CRYSTAL", "item.minecraft.end_crystal");
        keys.put("ENDER_SIGNAL", "item.minecraft.end_crystal");
        keys.put("FISHING_HOOK", "item.minecraft.fishing_rod");
        keys.put("COMPLEX_PART", "entity.minecraft.ender_dragon");
        return keys;
    }

    public static double getVersion() {
        Matcher m = Pattern.compile("1\\.(\\d+(\\.\\d+)?)").
                matcher(Bukkit.getVersion());

        if (!m.find()) return 0.0;

        try {
            return Double.parseDouble(m.group(1));
        } catch (Exception e) {
            return 0.0;
        }
    }

    @Nullable
    public static Class<?> getNMSClass(String pack, String name, boolean useVs) {
        Package aPackage = Bukkit.getServer().getClass().getPackage();

        String version = aPackage.getName().split("\\.")[3];
        pack = pack != null ? pack : "net.minecraft.server";

        try {
            return Class.forName(pack + (useVs ? "." + version : "") + "." + name);
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    public static Class<?> getBukkitClass(String name) {
        return getNMSClass("org.bukkit.craftbukkit", name, true);
    }

    @Nullable
    public static Object getObject(@Nullable Class<?> clazz, Object initial, String method) {
        try {
            clazz = clazz != null ? clazz : initial.getClass();
            return clazz.getDeclaredMethod(method).invoke(initial);
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    public static Object getObject(Object initial, String method, String... extraArgs) {
        Object obj = getObject(null, initial, method);

        if (extraArgs == null)
            return obj;

        for (String arg : extraArgs)
            obj = getObject(obj, arg);

        return obj;
    }

    @Nullable
    public static ItemStack getBukkitItem(Object nmsItem) {
        Class<?> clazz = getBukkitClass("inventory.CraftItemStack");
        if (clazz == null) return null;

        Constructor<?> ct;
        try {
            ct = clazz.getDeclaredConstructor(nmsItem.getClass());
        } catch (NoSuchMethodException e) {
            return null;
        }

        ct.setAccessible(true);
        try {
            return (ItemStack) ct.newInstance(nmsItem);
        } catch (Exception e) {
            return null;
        }
    }

    public static String checkValue(Object value, String def) {
        return value == null ? def : value.toString();
    }

    @Nullable
    public static String checkValue(Object value) {
        return checkValue(value, null);
    }
}