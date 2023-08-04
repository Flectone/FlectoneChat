package net.flectone.utils;

import com.google.common.reflect.ClassPath;
import net.flectone.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

@SuppressWarnings("ConstantConditions")
public class NMSUtil {

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

    public static String getMinecraftName(Entity entity){
        if(entity == null) return "";

        String string = entityKeys.get(entity.getType().name().toUpperCase());

        return string != null ? string : "";
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

    public static String[] getFormattedStringItem(ItemStack itemStack) {

        String itemName = NMSUtil.getMinecraftName(itemStack);
        itemName = itemStack.getItemMeta() == null || itemStack.getItemMeta().getDisplayName().isEmpty() ? itemName : itemStack.getItemMeta().getDisplayName();

        return new String[]{itemName, getItemAsJson(itemStack)};
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

    private static final Map<String, String> entityKeys = getEntityKeys();

    public static Map<String, String> getEntityKeys() {
        final LinkedHashMap<String, String> keys = new LinkedHashMap<>();
        keys.put("DROPPED_ITEM", "entity.minecraft.item");
        keys.put("EXPERIENCE_ORB", "entity.minecraft.experience_orb");
        keys.put("SMALL_FIREBALL", "entity.minecraft.small_fireball");
        keys.put("FIREBALL", "entity.minecraft.fireball");
        keys.put("DRAGON_FIREBALL", "entity.minecraft.dragon_fireball");
        keys.put("SPLASH_POTION", "item.minecraft.splash_potion"); // added
        keys.put("LINGERING_POTION", "item.minecraft.lingering_potion"); // added
        keys.put("ARROW", "entity.minecraft.arrow");
        keys.put("SNOWBALL", "entity.minecraft.snowball");
        keys.put("PAINTING", "entity.minecraft.painting");
        keys.put("ARMOR_STAND", "entity.minecraft.armor_stand");
        keys.put("CREEPER", "entity.minecraft.creeper");
        keys.put("SKELETON", "entity.minecraft.skeleton");
        keys.put("WITHER_SKELETON", "entity.minecraft.wither_skeleton");
        keys.put("STRAY", "entity.minecraft.stray");
        keys.put("SPIDER", "entity.minecraft.spider");
        keys.put("GIANT", "entity.minecraft.giant");
        keys.put("ZOMBIE", "entity.minecraft.zombie");
        keys.put("SLIME", "entity.minecraft.slime");
        keys.put("GHAST", "entity.minecraft.ghast");
        keys.put("PIG_ZOMBIE", "entity.minecraft.zombified_piglin");
        keys.put("ENDERMAN", "entity.minecraft.enderman");
        keys.put("ENDERMITE", "entity.minecraft.endermite");
        keys.put("SILVERFISH", "entity.minecraft.silverfish");
        keys.put("CAVE_SPIDER", "entity.minecraft.cave_spider");
        keys.put("BLAZE", "entity.minecraft.blaze");
        keys.put("MAGMA_CUBE", "entity.minecraft.magma_cube");
        keys.put("MOOSHROOM_COW", "entity.minecraft.mooshroom_cow");
        keys.put("VILLAGER", "entity.minecraft.villager");
        keys.put("ZOMBIE_VILLAGER", "entity.minecraft.villager"); // added
        keys.put("IRON_GOLEM", "entity.minecraft.iron_golem");
        keys.put("SNOW_GOLEM", "entity.minecraft.snow_golem");
        keys.put("ENDER_DRAGON", "entity.minecraft.ender_dragon");
        keys.put("WITHER", "entity.minecraft.wither");
        keys.put("WITCH", "entity.minecraft.witch");
        keys.put("GUARDIAN", "entity.minecraft.guardian");
        keys.put("SHULKER", "entity.minecraft.shulker");
        keys.put("POLAR_BEAR", "entity.minecraft.polar_bear");
        keys.put("EVOKER", "entity.minecraft.evoker");
        keys.put("EVOKER_FANGS", "entity.minecraft.evoker_fangs");
        keys.put("VEX", "entity.minecraft.vex");
        keys.put("VINDICATOR", "entity.minecraft.vindicator");
        keys.put("PARROT", "entity.minecraft.parrot");
        keys.put("ILLUSIONER", "entity.minecraft.illusioner");
        keys.put("VILLAGER.FARMER", "entity.minecraft.villager.farmer");
        keys.put("VILLAGER.FISHERMAN", "entity.minecraft.villager.fisherman");
        keys.put("VILLAGER.SHEPHERD", "entity.minecraft.villager.shepherd");
        keys.put("VILLAGER.FLETCHER", "entity.minecraft.villager.fletcher");
        keys.put("VILLAGER.LIBRARIAN", "entity.minecraft.villager.librarian");
        keys.put("VILLAGER.CLERIC", "entity.minecraft.villager.cleric");
        keys.put("VILLAGER.ARMORER", "entity.minecraft.villager.armorer");
        keys.put("VILLAGER.WEAPON_SMITH", "entity.minecraft.villager.weapon_smith");
        keys.put("VILLAGER.TOOL_SMITH", "entity.minecraft.villager.tool_smith");
        keys.put("VILLAGER.BUTCHER", "entity.minecraft.villager.butcher");
        keys.put("VILLAGER.LEATHERWORKER", "entity.minecraft.villager.leatherworker");
        keys.put("VILLAGER.NITWIT", "entity.minecraft.villager.nitwit");
        keys.put("VILLAGER.CARTOGRAPHER", "entity.minecraft.villager.cartographer");
        keys.put("PIG", "entity.minecraft.pig");
        keys.put("SHEEP", "entity.minecraft.sheep");
        keys.put("COW", "entity.minecraft.cow");
        keys.put("CHICKEN", "entity.minecraft.chicken");
        keys.put("SQUID", "entity.minecraft.squid");
        keys.put("WOLF", "entity.minecraft.wolf");
        keys.put("OCELOT", "entity.minecraft.ocelot");
        keys.put("BLACK_CAT", "entity.minecraft.cat");
        keys.put("RED_CAT", "entity.minecraft.cat");
        keys.put("SIAMESE_CAT", "entity.minecraft.cat");
        keys.put("BAT", "entity.minecraft.bat");
        keys.put("HORSE", "entity.minecraft.horse");
        keys.put("DONKEY", "entity.minecraft.donkey");
        keys.put("MULE", "entity.minecraft.mule");
        keys.put("SKELETON_HORSE", "entity.minecraft.skeleton_horse");
        keys.put("ZOMBIE_HORSE", "entity.minecraft.zombie_horse");
        keys.put("RABBIT", "entity.minecraft.rabbit");
        keys.put("RABBIT.THE_KILLER_BUNNY", "entity.minecraft.killer_bunny");
        keys.put("LLAMA", "entity.minecraft.llama");
        keys.put("LLAMA_SPIT", "entity.minecraft.llama"); // added
        keys.put("PRIMED_TNT", "entity.minecraft.tnt");
        keys.put("FALLING_BLOCK", "entity.minecraft.falling_block");
        keys.put("MINECART", "entity.minecraft.minecart");
        keys.put("MINECART_HOPPER", "entity.minecraft.minecart_hopper");
        keys.put("MINECART_CHEST", "entity.minecraft.minecart_chest");
        keys.put("MINECART_COMMAND", "item.minecraft.minecartcommand_block");
        keys.put("MINECART_FURNACE", "item.minecraft.minecart_furnace");
        keys.put("MINECART_MOB_SPAWNER", "entity.minecraft.minecart");
        keys.put("MINECART_TNT", "item.minecraft.minecart_tnt");
        keys.put("BOAT", "entity.minecraft.boat");
        keys.put("UNKNOWN", "entity.minecraft.generic");
        keys.put("SPECTRAL_ARROW", "item.minecraft.spectral_arrow");
        keys.put("TIPPED_ARROW", "item.minecraft.tipped_arrow");
        keys.put("ENDER_CRYSTAL", "item.minecraft.end_crystal");
        keys.put("ENDER_PEARL", "item.minecraft.ender_pearl");
        keys.put("ENDER_SIGNAL", "item.minecraft.end_crystal");
        keys.put("LEASH_HITCH", "item.minecraft.leash_hitch");
        keys.put("ITEM_FRAME", "item.minecraft.item_frame");
        keys.put("FISHING_HOOK", "item.minecraft.fishing_rod");
        keys.put("COMPLEX_PART", "entity.minecraft.ender_dragon");
        keys.put("TRIDENT", "entity.minecraft.trident");
        return keys;
    }
}