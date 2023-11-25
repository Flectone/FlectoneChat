package net.flectone.chat.module.serverMessage.death;

import net.flectone.chat.model.damager.PlayerDamager;
import net.flectone.chat.model.player.FPlayer;
import net.flectone.chat.module.FListener;
import net.flectone.chat.module.FModule;
import net.flectone.chat.module.integrations.IntegrationsModule;
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
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.NotNull;

public class DeathListener extends FListener {

    private static Entity lastInteractEntity;
    private static Projectile lastInteractProjectile;
    private static Material lastBlockInteract;

    public DeathListener(FModule module) {
        super(module);
        init();
    }

    @Override
    public void init() {
        registerEvents();
    }

    @EventHandler
    public void playerProjectileHit(@NotNull ProjectileHitEvent event) {
        if (event.isCancelled()) return;
        if (event.getHitEntity() == null) return;

        Projectile projectile = event.getEntity();
        ProjectileSource projectileSource = projectile.getShooter();
        if (!(projectileSource instanceof Entity entity)) return;

        lastInteractEntity = entity;
        lastInteractProjectile = projectile;
    }

    @EventHandler
    public void onPlayerClickOnBed(@NotNull PlayerInteractEvent event) {
        if (event.getClickedBlock() == null
                || !event.getAction().equals(Action.RIGHT_CLICK_BLOCK)
                || IntegrationsModule.isVanished(event.getPlayer())) return;

        Block block = event.getClickedBlock();
        BlockData blockData = block.getBlockData();
        World.Environment worldEnvironment = block.getWorld().getEnvironment();

        if ((blockData instanceof Bed && !worldEnvironment.equals(World.Environment.NORMAL))
                || (blockData instanceof RespawnAnchor && !worldEnvironment.equals(World.Environment.NETHER))) {

            lastInteractEntity = event.getPlayer();
            lastBlockInteract = block.getBlockData().getMaterial();
        }
    }

    @EventHandler
    public void onPlayerDamageEvent(@NotNull EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)
                || IntegrationsModule.isVanished(player)
                || player.getHealth() <= event.getFinalDamage()
                || !(event instanceof EntityDamageByEntityEvent entityDamageByEntityEvent)) return;

        FPlayer fPlayer = playerManager.get(player);
        if (fPlayer == null) return;

        fPlayer.setLastDamager(entityDamageByEntityEvent.getDamager());
    }

    @EventHandler
    public void onPlayerDeathEvent(@NotNull PlayerDeathEvent event) {
        Player player = event.getEntity();
        EntityDamageEvent lastDamageEvent = player.getLastDamageCause();

        if (hasNoPermission(player)) return;
        if (IntegrationsModule.isVanished(player) || lastDamageEvent == null) return;
        if (!config.getVaultBoolean(player, getModule() + ".enable")) return;

        String typeDeath = getDeathConfigMessage(player, lastDamageEvent);
        String configMessage = locale.getVaultString(player, getModule() + typeDeath);
        if (configMessage.isEmpty()) return;

        if (!config.getVaultBoolean(player, getModule() + ".visible")) {
            event.setDeathMessage("");
            return;
        }

        FPlayer fPlayer = playerManager.get(player);
        if (fPlayer == null) return;

        PlayerDamager playerDamager = fPlayer.getPlayerDamager();
        if (playerDamager == null) return;

        if (!playerDamager.isExpired()) playerDamager.setKiller(null);

        switch (lastDamageEvent.getCause()) {
            case ENTITY_EXPLOSION -> {
                Entity lastDamager = ((EntityDamageByEntityEvent) lastDamageEvent).getDamager();
                playerDamager.setFinalDamager(lastDamager);
                if (isTNT(lastDamager, playerDamager)) break;
                isProjectile(lastDamager, playerDamager);
            }
            case ENTITY_ATTACK -> {
                Entity lastEntityAttackDamager = ((EntityDamageByEntityEvent) lastDamageEvent).getDamager();
                playerDamager.setFinalDamager(lastEntityAttackDamager);
                if (lastEntityAttackDamager instanceof Player ) {
                    ItemStack itemStack = ((Player) lastEntityAttackDamager).getInventory().getItemInMainHand();
                    if (!itemStack.getType().equals(Material.AIR)) {
                        playerDamager.setKiller(lastEntityAttackDamager);
                        playerDamager.setKillerItem(itemStack);
                    }
                }
            }
            case FALLING_BLOCK -> {
                Entity lastDamagerBlock = ((EntityDamageByEntityEvent) lastDamageEvent).getDamager();
                playerDamager.setFinalDamager(lastDamagerBlock);
            }
            case BLOCK_EXPLOSION -> {
                playerDamager.setFinalDamager(lastBlockInteract);
                playerDamager.setKiller(lastInteractEntity);
            }
            case CONTACT -> {
                Block block = ((EntityDamageByBlockEvent) lastDamageEvent).getDamager();
                if (block == null) break;
                playerDamager.setFinalDamager(block.getBlockData().getMaterial());
            }
            case PROJECTILE -> {
                Entity projectileEntity = ((EntityDamageByEntityEvent) lastDamageEvent).getDamager();
                playerDamager.setFinalDamager(projectileEntity);
                isProjectile(projectileEntity, playerDamager);
            }
            case MAGIC -> {
                if (!(lastDamageEvent instanceof EntityDamageByEntityEvent)) {
                    playerDamager.setKiller(lastInteractEntity);
                    playerDamager.setFinalDamager(lastInteractProjectile);
                    break;
                }

                Entity lastMagicDamager = ((EntityDamageByEntityEvent) lastDamageEvent).getDamager();
                playerDamager.setFinalDamager(lastMagicDamager);
                isProjectile(lastMagicDamager, playerDamager);
            }
            case ENTITY_SWEEP_ATTACK, THORNS -> {
                Entity lastEntitySweepAttackDamager = ((EntityDamageByEntityEvent) lastDamageEvent).getDamager();
                playerDamager.setKiller(lastEntitySweepAttackDamager);
            }
        }

        ((DeathModule) getModule()).sendAll(player, playerDamager, configMessage);

        IntegrationsModule.sendDiscordDeath(player, playerDamager, typeDeath);

        event.setDeathMessage("");
        fPlayer.setLastDamager(new PlayerDamager());
    }

    @NotNull
    private String getDeathConfigMessage(@NotNull Player player, @NotNull EntityDamageEvent lastDamageEvent) {
        String message;
        EntityDamageEvent.DamageCause damageCause = lastDamageEvent.getCause();
        if (lastDamageEvent instanceof EntityDamageByEntityEvent lastEntityDamageEvent
                && damageCause == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {

            Entity damager = lastEntityDamageEvent.getDamager();

            message = ".mob.";

            message += damager instanceof Player
                    ? "player"
                    : config.getVaultBoolean(player, getModule() + ".mob-default")
                    ? "default"
                    : damager.getType().name().toLowerCase();

        } else message = ".natural." + damageCause.name().toLowerCase();

        return message.replace(" ", "_");
    }

    private boolean isProjectile(@NotNull Entity entity, @NotNull PlayerDamager playerDamager) {
        if (entity instanceof Projectile projectile) {
            Entity shooter = (Entity) projectile.getShooter();
            if (shooter != null) {
                playerDamager.setKiller(shooter);
                return true;
            }
        }
        return false;
    }

    private boolean isTNT(@NotNull Entity entity, @NotNull PlayerDamager playerDamager) {
        if (entity instanceof TNTPrimed tntPrimed) {
            Entity source = tntPrimed.getSource();
            if (source instanceof Player) {
                playerDamager.setKiller(source);
                return true;
            }
        }
        return false;
    }
}
