package ru.flectone;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.Vector;
import ru.flectone.commands.TabComplets;
import ru.flectone.utils.FileResource;
import ru.flectone.utils.PlayerUtils;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class FActions implements Listener {

    private FileResource locale = Main.locale;

    private FileResource config = Main.config;

    @EventHandler
    public void joinPlayer(PlayerJoinEvent event){
        Player player = event.getPlayer();

        removeBugEntities(player);

        new FPlayer(player);

        event.setJoinMessage(null);
        setActionMessage("join", player.getName());
    }

    @EventHandler
    public void leftPlayer(PlayerQuitEvent event){
        Player player = event.getPlayer();

        removeBugEntities(player);

        event.setQuitMessage(null);
        setActionMessage("left", player.getName());

        PlayerUtils.removePlayer(player);
    }

    private void removeBugEntities(Player player){
        player.getWorld().getNearbyEntities(player.getLocation(), 20, 20, 20, entity -> entity.isGlowing()).forEach(entity -> {

            if(entity.isSilent() && entity.isInvulnerable() && !entity.isVisualFire()) entity.remove();

            entity.setGlowing(false);
        });
    }

    private void setActionMessage(String eventType, String playerName){
        for(Player playerOnline : Bukkit.getOnlinePlayers()){
            playerOnline.sendMessage(locale.getFormatString(eventType + ".message", playerOnline)
                    .replace("<player>", playerName));
        }
    }

    @EventHandler
    public void inventoryClick(InventoryClickEvent event){

        if(PlayerUtils.getPlayer(event.getWhoClicked()).getInventoryList() == null
                || !PlayerUtils.getPlayer(event.getWhoClicked()).getInventoryList().contains(event.getInventory())) return;


        event.setCancelled(true);

        if(event.getCurrentItem() == null) return;


        Player eventPlayer = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        switch(clickedItem.getType()){
            case PLAYER_HEAD: {

                String secondPlayerName = clickedItem.getItemMeta().getLocalizedName();
                OfflinePlayer secondPlayer = Bukkit.getOfflinePlayer(secondPlayerName);

                PlayerUtils.getPlayer(eventPlayer).getIgnoreList().remove(secondPlayer.getUniqueId().toString());
                eventPlayer.sendMessage(locale.getFormatString("ignore.success_unignore", eventPlayer).
                        replace( "<player>", secondPlayerName));

                event.getInventory().remove(event.getCurrentItem());

                eventPlayer.closeInventory();

                List<Inventory> inventoryList = PlayerUtils.getPlayer(eventPlayer).getInventoryList();
                for(int x = 0; x < inventoryList.size(); x++){
                    if(inventoryList.get(x) == event.getClickedInventory()){
                        PlayerUtils.getPlayer(eventPlayer).setNumberLastInventory(x);
                        break;
                    }
                }

                Bukkit.dispatchCommand(eventPlayer, "ignore-list");

                break;
            }

            case SPECTRAL_ARROW: {
                eventPlayer.closeInventory();

                List<Inventory> inventoryList = PlayerUtils.getPlayer(eventPlayer).getInventoryList();

                for(int x = 0; x < inventoryList.size(); x++){
                    if(inventoryList.get(x) == event.getClickedInventory()){
                        eventPlayer.openInventory(inventoryList.get(x+1));
                        break;
                    }
                }
                break;
            }

            case ARROW:{
                eventPlayer.closeInventory();

                List<Inventory> inventoryList = PlayerUtils.getPlayer(eventPlayer).getInventoryList();

                for(int x = 1; x < inventoryList.size(); x++){
                    if(inventoryList.get(x) == event.getClickedInventory()){
                        eventPlayer.openInventory(inventoryList.get(x-1));
                        break;
                    }
                }
                break;
            }
        }

    }

    @EventHandler
    public void inventoryOpen(InventoryOpenEvent event){

        List<Inventory> inventoryList = PlayerUtils.getPlayer(event.getPlayer()).getInventoryList();
        if(inventoryList == null
                || !inventoryList.contains(event.getInventory())) return;


        List<String> ignoreList = PlayerUtils.getPlayer(event.getPlayer()).getIgnoreList();

        int indexItem = 0;
        int numberInventory = 0;
        int maxSlotsInventory = 17;
        for(int y = 0; y < ignoreList.size(); y++){

            if(y > maxSlotsInventory){
                maxSlotsInventory += 18;
                indexItem = 0;
                numberInventory++;
            }

            ItemStack blockForHead = new ItemStack(Material.PLAYER_HEAD);

            SkullMeta skullMeta = (SkullMeta) blockForHead.getItemMeta();

            OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(ignoreList.get(y)));

            skullMeta.setDisplayName("§e" + player.getName());
            skullMeta.setLocalizedName(player.getName());
            skullMeta.setOwner(player.getName());
            blockForHead.setItemMeta(skullMeta);
            inventoryList.get(numberInventory).setItem(indexItem++, blockForHead);

        }

        for(int x = 0; x < inventoryList.size(); x++){
            Inventory inventory = inventoryList.get(x);

            if(inventoryList.size() > x+1){
                inventory.setItem(26, createArrowItem(new ItemStack(Material.SPECTRAL_ARROW), "spectral_arrow", event.getPlayer()));
            }
            if(x != 0){
                inventory.setItem(18, createArrowItem(new ItemStack(Material.ARROW), "arrow", event.getPlayer()));
            }
        }

    }

    private ItemStack createArrowItem(ItemStack arrow, String arrowName, HumanEntity player){
        ItemMeta itemMeta = arrow.getItemMeta();
        itemMeta.setDisplayName(locale.getFormatString("ignore-list." + arrowName, (Player) player));
        arrow.setItemMeta(itemMeta);
        return arrow;
    }

    @EventHandler
    public void updateServerList(ServerListPingEvent event){
        if(config.getBoolean("server.motd.enable")){
            event.setMotd(locale.getFormatString("server.motd.message", null));
        }

        if(config.getBoolean("server.online.enable")){
            event.setMaxPlayers(config.getInt("server.online.number"));
        }
    }


    @EventHandler
    public void changeWorldEvent(PlayerChangedWorldEvent event){
        PlayerUtils.getPlayer(event.getPlayer()).setName(event.getPlayer().getWorld());
    }

    @EventHandler
    public void playerItemClick(PlayerInteractEvent event){
        if(!config.getBoolean("mark.enable")) return;

        if(event.getItem() == null) return;

        if(event.getItem().getType().equals(Material.NETHER_STAR)){
            String itemName = event.getItem().getItemMeta().getDisplayName();
            if(!itemName.isEmpty() && itemName.toLowerCase().equals("flectone")){
                Bukkit.dispatchCommand(event.getPlayer(), "mark " + TabComplets.chatColorValues[((int) (Math.random()*TabComplets.chatColorValues.length))]);
                return;
            }
        }

        Material markItem;

        try {
            markItem = Material.valueOf(Main.config.getString("mark.item").toUpperCase());

        } catch (IllegalArgumentException | NullPointerException exception ){
            Main.getInstance().getLogger().warning("Item for mark was not found");
            markItem = Material.WOODEN_SWORD;
        }

        if(!event.getItem().getType().equals(markItem)) return;

        String itemName = event.getItem().getItemMeta().getDisplayName().toUpperCase();

        String command = "mark";

        if(!itemName.isEmpty() && Arrays.asList(TabComplets.chatColorValues).contains(itemName)){
            command += " " + itemName;
        }

        Bukkit.dispatchCommand(event.getPlayer(), command);

    }

    @EventHandler
    public void checkCustomEntitySpawn(EntitySpawnEvent event){
        if(!(event.getEntity() instanceof MagmaCube)) return;

        Location location = event.getEntity().getLocation();

        if(location.getDirection().equals(new Vector(0, 1, 0))){

            MagmaCube entity = (MagmaCube) event.getEntity();

            entity.setGravity(false);
            entity.setSilent(true);
            entity.setInvulnerable(true);
            entity.setGlowing(true);
            entity.setVisualFire(false);
            entity.setAI(false);
            entity.setSize(1);
            entity.setInvisible(true);
            entity.setGlowing(true);
        }
    }
}
