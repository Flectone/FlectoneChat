package net.flectone.listeners;

import net.flectone.Main;
import net.flectone.custom.FDamager;
import net.flectone.custom.FPlayer;
import net.flectone.managers.FPlayerManager;
import net.flectone.utils.NMSUtil;
import net.flectone.utils.ObjectUtil;
import net.md_5.bungee.api.chat.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Bed;
import org.bukkit.block.data.type.RespawnAnchor;
import org.bukkit.command.CommandSender;
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

import java.util.ArrayList;
import java.util.List;

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
        if (Main.config.getBoolean("death.message.enable")) register();
        else unregister();
    }

    public static PlayerDeathEventListener getInstance() {
        return instance;
    }

    @EventHandler
    public void onPlayerClickOnBed(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null || !event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;

        Block block = event.getClickedBlock();
        World.Environment worldEnvironment = block.getWorld().getEnvironment();

        if ((block.getBlockData() instanceof Bed && !worldEnvironment.equals(World.Environment.NORMAL))
                || (block.getBlockData() instanceof RespawnAnchor && !worldEnvironment.equals(World.Environment.NETHER))) {

            lastInteractPlayer = event.getPlayer();
            lastBlockInteract = block.getBlockData().getMaterial();
        }
    }

    @EventHandler
    public void onPlayerDamageEvent(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        if (player.getHealth() <= event.getFinalDamage()) return;

        FPlayer fPlayer = FPlayerManager.getPlayer((Player) event.getEntity());
        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent entityDamageByEntityEvent = (EntityDamageByEntityEvent) event;
            fPlayer.setLastDamager(entityDamageByEntityEvent.getDamager());
        }
    }

    @EventHandler
    public void onPlayerDeathEvent(PlayerDeathEvent event) {
        Player player = event.getEntity();
        EntityDamageEvent lastDamageEvent = player.getLastDamageCause();

        if (lastDamageEvent == null) return;

        String formatMessage = Main.locale.getString(getDeathConfigMessage(lastDamageEvent));
        if (formatMessage.isEmpty()) return;

        FPlayer fPlayer = FPlayerManager.getPlayer(player);
        FDamager fDamager = fPlayer.getLastFDamager();

        ArrayList<String> placeholders = new ArrayList<>();
        placeholders.add("<player>");
        placeholders.add("<due_to>");

        if (!fPlayer.isDeathByObject()) fDamager.setKiller(null);

        switch (lastDamageEvent.getCause()) {
            case ENTITY_EXPLOSION:
                placeholders.add("<killer>");

                Entity lastDamager = ((EntityDamageByEntityEvent) lastDamageEvent).getDamager();
                fDamager.setFinalDamager(lastDamager);

                if (isTNT(lastDamager, fDamager)) break;
                if (isProjectile(lastDamager, fDamager)) break;
                break;
            case ENTITY_ATTACK:
                placeholders.add("<killer>");

                Entity lastEntityAttackDamager = ((EntityDamageByEntityEvent) lastDamageEvent).getDamager();
                fDamager.setFinalDamager(lastEntityAttackDamager);

                if (lastEntityAttackDamager instanceof Player) {
                    placeholders.add("<by_item>");

                    ItemStack itemStack = ((Player) lastEntityAttackDamager).getInventory().getItemInMainHand();
                    if (!itemStack.getType().equals(Material.AIR)) {

                        fDamager.setKiller(lastEntityAttackDamager);
                        fDamager.setKillerItem(itemStack);
                    }
                }

                break;

            case FALLING_BLOCK:
                placeholders.add("<killer>");

                Entity lastDamagerBlock = ((EntityDamageByEntityEvent) lastDamageEvent).getDamager();
                fDamager.setFinalDamager(lastDamagerBlock);
                break;
            case BLOCK_EXPLOSION:
                placeholders.add("<block>");

                fDamager.setFinalDamager(lastBlockInteract);
                fDamager.setKiller(lastInteractPlayer);
                break;
            case CONTACT:
                placeholders.add("<block>");

                Block block = ((EntityDamageByBlockEvent) lastDamageEvent).getDamager();
                if(block == null) break;
                fDamager.setFinalDamager(block.getBlockData().getMaterial());
                break;
            case PROJECTILE:
                placeholders.add("<projectile>");

                Entity projectileEntity = ((EntityDamageByEntityEvent) lastDamageEvent).getDamager();
                fDamager.setFinalDamager(projectileEntity);

                if (isProjectile(projectileEntity, fDamager)) break;
                break;

            case MAGIC:
                if (!(lastDamageEvent instanceof EntityDamageByEntityEvent)) {
                    fDamager.setKiller(null);
                    break;
                }

                Entity lastMagicDamager = ((EntityDamageByEntityEvent) lastDamageEvent).getDamager();
                fDamager.setFinalDamager(lastMagicDamager);

                if (isProjectile(lastMagicDamager, fDamager)) break;
                break;

            case ENTITY_SWEEP_ATTACK:
            case THORNS:
                Entity lastEntitySweepAttackDamager = ((EntityDamageByEntityEvent) lastDamageEvent).getDamager();
                fDamager.setKiller(lastEntitySweepAttackDamager);
                break;
        }

        Bukkit.getOnlinePlayers().parallelStream()
                .filter(recipient -> !FPlayerManager.getPlayer(recipient).isIgnored(player))
                .forEach(recipient -> {
                    String string = ObjectUtil.formatString(formatMessage, recipient, player);
                    ArrayList<String> finalPlaceholders = ObjectUtil.splitLine(string, placeholders);

                    recipient.spigot().sendMessage(createDeathComponent(finalPlaceholders, recipient, player, fDamager));
                });

        event.setDeathMessage("");
        fPlayer.resetLastDamager();
    }

    // shit code because text component is shit
    private BaseComponent[] createDeathComponent(ArrayList<String> placeholders, CommandSender recipient, CommandSender sender, FDamager fDamager) {
        String mainColor = "";
        ComponentBuilder mainBuilder = new ComponentBuilder();

        for (String mainPlaceholder : placeholders) {
            switch (mainPlaceholder) {
                case "<player>":
                    mainBuilder.append(createClickableComponent(mainColor, sender, recipient));
                    break;
                case "<projectile>":
                case "<killer>":
                    if (!fDamager.isFinalEntity()) break;
                    Entity entity = fDamager.getFinalEntity();

                    if (entity instanceof Player) {
                        mainBuilder.append(createClickableComponent(mainColor, entity, recipient));
                        break;
                    }

                    mainBuilder
                            .append(TextComponent.fromLegacyText(mainColor))
                            .append(createTranslatableEntityComponent(recipient, sender, entity))
                            .append(TextComponent.fromLegacyText(mainColor));

                    break;
                case "<block>":
                    if (!fDamager.isFinalBlock()) break;

                    mainBuilder
                            .append(TextComponent.fromLegacyText(mainColor))
                            .append(new TranslatableComponent(fDamager.getDamagerTranslateName()))
                            .append(TextComponent.fromLegacyText(mainColor));

                    break;
                case "<due_to>":
                    if (fDamager.getKiller() == null || fDamager.getKiller().equals(fDamager.getFinalEntity())) {
                        break;
                    }



                    String formatDueToMessage = Main.locale.getFormatString("death.due-to", recipient, sender);

                    String dueToColor = "";
                    ComponentBuilder dueToBuilder = new ComponentBuilder();
                    for (String dueToPlaceholder : ObjectUtil.splitLine(formatDueToMessage, new ArrayList<>(List.of("<killer>")))) {
                        if (dueToPlaceholder.equals("<killer>")) {
                            Entity killer = fDamager.getKiller();
                            if (killer instanceof Player) {
                                dueToBuilder.append(createClickableComponent(dueToColor, killer, recipient), ComponentBuilder.FormatRetention.NONE);

                            } else dueToBuilder
                                    .append(TextComponent.fromLegacyText(dueToColor))
                                    .append(createTranslatableEntityComponent(recipient, sender, killer))
                                    .append(TextComponent.fromLegacyText(dueToColor));
                        } else {
                            dueToBuilder.append(TextComponent.fromLegacyText(dueToColor + dueToPlaceholder), ComponentBuilder.FormatRetention.NONE);
                        }

                        dueToColor = ChatColor.getLastColors(dueToColor + dueToBuilder.getCurrentComponent().toString());
                    }

                    mainBuilder.append(dueToBuilder.create(), ComponentBuilder.FormatRetention.NONE);
                    break;
                case "<by_item>":
                    if (fDamager.getKillerItemName() == null) break;
                    String formatMessage = Main.locale.getFormatString("death.by-item", recipient, sender);

                    String byItemColor = "";
                    ComponentBuilder byItemBuilder = new ComponentBuilder();

                    for (String byItemPlaceholder : ObjectUtil.splitLine(formatMessage, new ArrayList<>(List.of("<item>")))) {
                        if (byItemPlaceholder.equals("<item>")) {
                            TranslatableComponent translatableComponent = new TranslatableComponent(fDamager.getKillerItemName());
                            translatableComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new BaseComponent[]{
                                    new TextComponent(fDamager.getKillerItemAsJson())}));

                            byItemBuilder
                                    .append(TextComponent.fromLegacyText(byItemColor))
                                    .append(translatableComponent)
                                    .append(TextComponent.fromLegacyText(byItemColor));
                        } else {
                            TextComponent textComponent1 = new TextComponent(TextComponent.fromLegacyText(byItemColor + byItemPlaceholder));
                            byItemBuilder.append(textComponent1, ComponentBuilder.FormatRetention.NONE);
                        }
                        byItemColor = ChatColor.getLastColors(byItemColor + byItemBuilder.getCurrentComponent().toString());
                    }

                    mainBuilder.append(byItemBuilder.create(), ComponentBuilder.FormatRetention.NONE);
                    break;

                default:
                    mainBuilder.append(TextComponent.fromLegacyText(mainColor + mainPlaceholder), ComponentBuilder.FormatRetention.NONE);
                    break;
            }

            mainColor = ChatColor.getLastColors(mainColor + mainBuilder.getCurrentComponent().toString());
        }

        return mainBuilder.create();
    }

    private TranslatableComponent createTranslatableEntityComponent(CommandSender recipient, CommandSender sender, Entity entity) {
        TranslatableComponent hoverComponent = new TranslatableComponent(NMSUtil.getMinecraftName(entity));

        String formatHoverMessage = Main.locale.getFormatString("entity.hover-message", recipient, sender)
                .replace("<uuid>", entity.getUniqueId().toString());

        ComponentBuilder hoverBuilder = new ComponentBuilder();

        String hoverColor = "";
        for (String hoverPlaceholder : ObjectUtil.splitLine(formatHoverMessage, new ArrayList<>(List.of("<name>")))) {
            if (hoverPlaceholder.equals("<name>")) {
                hoverBuilder.append(TextComponent.fromLegacyText(hoverColor));
                hoverBuilder.append(new TranslatableComponent(NMSUtil.getMinecraftName(entity)));
                hoverBuilder.append(TextComponent.fromLegacyText(hoverColor));
            } else {
                hoverBuilder.append(TextComponent.fromLegacyText(hoverColor + hoverPlaceholder), ComponentBuilder.FormatRetention.NONE);
            }

            hoverColor = ChatColor.getLastColors(hoverColor + hoverBuilder.getCurrentComponent().toString());
        }

        hoverComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverBuilder.create()));
        return hoverComponent;
    }

    private TextComponent createClickableComponent(String chatColor, CommandSender sender, CommandSender recipient) {
        String playerName = sender.getName();
        String suggestCommand = "/msg " + playerName + " ";
        String showText = Main.locale.getFormatString("player.hover-message", recipient, sender)
                .replace("<player>", playerName);

        TextComponent textComponent = new TextComponent(TextComponent.fromLegacyText(chatColor + playerName));
        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, suggestCommand));
        textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(showText)));
        return textComponent;
    }


    private String getDeathConfigMessage(EntityDamageEvent lastDamageEvent) {
        String message;
        EntityDamageEvent.DamageCause damageCause = lastDamageEvent.getCause();
        if (lastDamageEvent instanceof EntityDamageByEntityEvent
                && damageCause == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {

            EntityDamageByEntityEvent lastEntityDamageEvent = (EntityDamageByEntityEvent) lastDamageEvent;

            Entity damager = lastEntityDamageEvent.getDamager();

            message = "death.mob.";

            message += damager instanceof Player
                    ? "player"
                    : Main.config.getBoolean("death.message.mob-default")
                    ? "default"
                    : damager.getType().name().toLowerCase();

        } else message = "death.natural." + damageCause.name().toLowerCase();
        return message.replace(" ", "_");
    }

    private boolean isProjectile(Entity entity, FDamager fDamager) {
        if (entity instanceof Projectile) {
            Projectile projectile = (Projectile) entity;
            Entity shooter = (Entity) projectile.getShooter();
            if (shooter != null) {
                fDamager.setKiller(shooter);
                return true;
            }
        }
        return false;
    }

    private boolean isTNT(Entity entity, FDamager fDamager) {
        if (entity instanceof TNTPrimed) {
            TNTPrimed tntPrimed = (TNTPrimed) entity;
            Entity source = tntPrimed.getSource();
            if (source instanceof Player) {
                fDamager.setKiller(source);
                return true;
            }
        }
        return false;
    }
}
