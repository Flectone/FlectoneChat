package net.flectone.listeners;

import net.flectone.Main;
import net.flectone.integrations.discordsrv.FDiscordSRV;
import net.flectone.integrations.supervanish.FSuperVanish;
import net.flectone.managers.FPlayerManager;
import net.flectone.misc.components.FComponent;
import net.flectone.misc.entity.FDamager;
import net.flectone.misc.entity.FPlayer;
import net.flectone.utils.ObjectUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Bed;
import org.bukkit.block.data.type.RespawnAnchor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import static net.flectone.managers.FileManager.config;
import static net.flectone.managers.FileManager.locale;

public class PlayerDeathEventListener implements Listener {

    private static PlayerDeathEventListener instance;
    private static Player lastInteractPlayer;
    private static Material lastBlockInteract;

    public PlayerDeathEventListener() {
        instance = this;
    }

    public static void unregister() {
        if (instance == null) return;
        PlayerDeathEvent.getHandlerList().unregister(instance);
        PlayerInteractEvent.getHandlerList().unregister(instance);
        EntityDamageEvent.getHandlerList().unregister(instance);
    }

    public static void register() {
        if (instance != null) return;

        Bukkit.getPluginManager().registerEvents(new PlayerDeathEventListener(), Main.getInstance());
    }

    public static void reload() {
        if (config.getBoolean("death.message.enable")) register();
        else unregister();
    }

    @EventHandler
    public void onPlayerClickOnBed(@NotNull PlayerInteractEvent event) {
        if (event.getClickedBlock() == null
                || !event.getAction().equals(Action.RIGHT_CLICK_BLOCK)
                || FSuperVanish.isVanished(event.getPlayer())) return;

        Block block = event.getClickedBlock();
        BlockData blockData = block.getBlockData();
        World.Environment worldEnvironment = block.getWorld().getEnvironment();

        if ((blockData instanceof Bed && !worldEnvironment.equals(World.Environment.NORMAL))
                || (blockData instanceof RespawnAnchor && !worldEnvironment.equals(World.Environment.NETHER))) {

            lastInteractPlayer = event.getPlayer();
            lastBlockInteract = block.getBlockData().getMaterial();
        }
    }

    @EventHandler
    public void onPlayerDamageEvent(@NotNull EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)
                || FSuperVanish.isVanished(player)
                || player.getHealth() <= event.getFinalDamage()
                || !(event instanceof EntityDamageByEntityEvent entityDamageByEntityEvent)) return;

        FPlayer fPlayer = FPlayerManager.getPlayer(player);
        if (fPlayer == null) return;

        fPlayer.setLastDamager(entityDamageByEntityEvent.getDamager());
    }

    @EventHandler
    public void onPlayerDeathEvent(@NotNull PlayerDeathEvent event) {
        Player player = event.getEntity();
        EntityDamageEvent lastDamageEvent = player.getLastDamageCause();

        if (FSuperVanish.isVanished(player) || lastDamageEvent == null) return;

        String formatMessage = locale.getString(getDeathConfigMessage(lastDamageEvent));
        if (formatMessage.isEmpty()) return;

        if (!config.getBoolean("death.message.visible")) {
            event.setDeathMessage("");
            return;
        }

        FPlayer fPlayer = FPlayerManager.getPlayer(player);
        if (fPlayer == null) return;

        FDamager fDamager = fPlayer.getLastFDamager();
        if (fDamager == null) return;

        ArrayList<String> placeholders = new ArrayList<>();
        placeholders.add("<player>");
        placeholders.add("<due_to>");

        if (!fPlayer.isDeathByObject()) fDamager.setKiller(null);

        switch (lastDamageEvent.getCause()) {
            case ENTITY_EXPLOSION -> {
                placeholders.add("<killer>");
                Entity lastDamager = ((EntityDamageByEntityEvent) lastDamageEvent).getDamager();
                fDamager.setFinalDamager(lastDamager);
                if (isTNT(lastDamager, fDamager)) break;
                isProjectile(lastDamager, fDamager);
            }
            case ENTITY_ATTACK -> {
                placeholders.add("<killer>");
                Entity lastEntityAttackDamager = ((EntityDamageByEntityEvent) lastDamageEvent).getDamager();
                fDamager.setFinalDamager(lastEntityAttackDamager);
                if (lastEntityAttackDamager instanceof Player ) {
                    placeholders.add("<by_item>");

                    ItemStack itemStack = ((Player) lastEntityAttackDamager).getInventory().getItemInMainHand();
                    if (!itemStack.getType().equals(Material.AIR)) {
                        fDamager.setKiller(lastEntityAttackDamager);
                        fDamager.setKillerItem(itemStack);
                    }
                }
            }
            case FALLING_BLOCK -> {
                placeholders.add("<killer>");
                Entity lastDamagerBlock = ((EntityDamageByEntityEvent) lastDamageEvent).getDamager();
                fDamager.setFinalDamager(lastDamagerBlock);
            }
            case BLOCK_EXPLOSION -> {
                placeholders.add("<block>");
                fDamager.setFinalDamager(lastBlockInteract);
                fDamager.setKiller(lastInteractPlayer);
            }
            case CONTACT -> {
                placeholders.add("<block>");
                Block block = ((EntityDamageByBlockEvent) lastDamageEvent).getDamager();
                if (block == null) break;
                fDamager.setFinalDamager(block.getBlockData().getMaterial());
            }
            case PROJECTILE -> {
                placeholders.add("<projectile>");
                Entity projectileEntity = ((EntityDamageByEntityEvent) lastDamageEvent).getDamager();
                fDamager.setFinalDamager(projectileEntity);
                isProjectile(projectileEntity, fDamager);
            }
            case MAGIC -> {
                if (!(lastDamageEvent instanceof EntityDamageByEntityEvent)) {
                    fDamager.setKiller(null);
                    break;
                }
                Entity lastMagicDamager = ((EntityDamageByEntityEvent) lastDamageEvent).getDamager();
                fDamager.setFinalDamager(lastMagicDamager);
                isProjectile(lastMagicDamager, fDamager);
            }
            case ENTITY_SWEEP_ATTACK, THORNS -> {
                Entity lastEntitySweepAttackDamager = ((EntityDamageByEntityEvent) lastDamageEvent).getDamager();
                fDamager.setKiller(lastEntitySweepAttackDamager);
            }
        }

        Bukkit.getOnlinePlayers().parallelStream()
                .filter(recipient -> {
                    FPlayer recipientFPlayer = FPlayerManager.getPlayer(recipient);
                    return recipientFPlayer != null && !recipientFPlayer.isIgnored(player);
                })
                .forEach(recipient -> {
                    String string = ObjectUtil.formatString(formatMessage, recipient, player);
                    ArrayList<String> finalPlaceholders = ObjectUtil.splitLine(string, placeholders);

                    recipient.spigot().sendMessage(FComponent.createDeathComponent(finalPlaceholders, recipient, player, fDamager));
                });

        if(FDiscordSRV.isEnable()) {
            FDiscordSRV.sendDeathMessage(player,
                    formatMessage,
                    fDamager.getFinalEntity(),
                    fDamager.getFinalBlockDamager(),
                    fDamager.getKiller(),
                    fDamager.getKillerItem());
        }

        event.setDeathMessage("");
        fPlayer.resetLastDamager();
    }

    @NotNull
    private String getDeathConfigMessage(@NotNull EntityDamageEvent lastDamageEvent) {
        String message;
        EntityDamageEvent.DamageCause damageCause = lastDamageEvent.getCause();
        if (lastDamageEvent instanceof EntityDamageByEntityEvent lastEntityDamageEvent
                && damageCause == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {

            Entity damager = lastEntityDamageEvent.getDamager();

            message = "death.mob.";

            message += damager instanceof Player
                    ? "player"
                    : config.getBoolean("death.message.mob-default")
                    ? "default"
                    : damager.getType().name().toLowerCase();

        } else message = "death.natural." + damageCause.name().toLowerCase();
        return message.replace(" ", "_");
    }

    private boolean isProjectile(@NotNull Entity entity, @NotNull FDamager fDamager) {
        if (entity instanceof Projectile projectile) {
            Entity shooter = (Entity) projectile.getShooter();
            if (shooter != null) {
                fDamager.setKiller(shooter);
                return true;
            }
        }
        return false;
    }

    private boolean isTNT(@NotNull Entity entity, @NotNull FDamager fDamager) {
        if (entity instanceof TNTPrimed tntPrimed) {
            Entity source = tntPrimed.getSource();
            if (source instanceof Player) {
                fDamager.setKiller(source);
                return true;
            }
        }
        return false;
    }
}
