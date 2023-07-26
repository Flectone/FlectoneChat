package net.flectone.utils;

import com.google.common.reflect.ClassPath;
import net.flectone.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

@SuppressWarnings("ConstantConditions")
public class ReflectionUtil {

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
            ReflectionUtil.version = Double.parseDouble(dotSplit[0] + "." + dotSplit[1]); // 1.13

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

    public static String[] getFormattedStringItem(ItemStack itemStack){

        String itemName = ReflectionUtil.getMinecraftName(itemStack);
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

    public static void registerClasses(String packageName, RegisterInterface registerInterface){
        try {
            for(ClassPath.ClassInfo classInfo : ClassPath.from(Main.getInstance().getClass().getClassLoader()).getTopLevelClassesRecursive(packageName)){

                Class<?> c = Class.forName(classInfo.getName());

                registerInterface.register(c);
            }

        } catch (IOException | NoSuchMethodException | ClassNotFoundException | InstantiationException |
                 IllegalAccessException | InvocationTargetException e){
            e.printStackTrace();
        }
    }
}