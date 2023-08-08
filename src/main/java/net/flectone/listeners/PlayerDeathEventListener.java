package net.flectone.listeners;

import net.flectone.Main;
import net.flectone.misc.entity.FDamager;
import net.flectone.misc.entity.FPlayer;
import net.flectone.integrations.discordsrv.FDiscordSRV;
import net.flectone.integrations.supervanish.FSuperVanish;
import net.flectone.managers.FPlayerManager;
import net.flectone.utils.NMSUtil;
import net.flectone.utils.ObjectUtil;
import net.md_5.bungee.api.chat.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
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
import org.jetbrains.annotations.NotNull;

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

        String formatMessage = Main.locale.getString(getDeathConfigMessage(lastDamageEvent));
        if (formatMessage.isEmpty()) return;

        if (!Main.config.getBoolean("death.message.visible")) {
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

                    recipient.spigot().sendMessage(createDeathComponent(finalPlaceholders, recipient, player, fDamager));
                });

        FDiscordSRV.sendDeathMessage(player,
                formatMessage,
                fDamager.getFinalEntity(),
                fDamager.getFinalBlockDamager(),
                fDamager.getKiller(),
                fDamager.getKillerItem());

        event.setDeathMessage("");
        fPlayer.resetLastDamager();
    }

    @NotNull
    private BaseComponent[] createDeathComponent(@NotNull ArrayList<String> placeholders, @NotNull CommandSender recipient, @NotNull CommandSender sender, @NotNull FDamager fDamager) {
        String mainColor = "";
        ComponentBuilder mainBuilder = new ComponentBuilder();

        for (String mainPlaceholder : placeholders) {
            switch (mainPlaceholder) {
                case "<player>" -> mainBuilder.append(createClickableComponent(mainColor, sender, recipient));
                case "<projectile>", "<killer>" -> {
                    if (fDamager.getFinalEntity() == null) break;
                    Entity entity = fDamager.getFinalEntity();
                    if (entity instanceof Player) {
                        mainBuilder.append(createClickableComponent(mainColor, entity, recipient));
                        break;
                    }
                    mainBuilder
                            .append(TextComponent.fromLegacyText(mainColor))
                            .append(createTranslatableEntityComponent(recipient, sender, entity))
                            .append(TextComponent.fromLegacyText(mainColor));
                }
                case "<block>" -> {
                    if (!fDamager.isFinalBlock()) break;
                    mainBuilder
                            .append(TextComponent.fromLegacyText(mainColor))
                            .append(new TranslatableComponent(fDamager.getDamagerTranslateName()))
                            .append(TextComponent.fromLegacyText(mainColor));
                }
                case "<due_to>" -> {
                    if (fDamager.getKiller() == null || fDamager.getKiller().equals(fDamager.getFinalEntity())
                            || fDamager.getFinalEntity() != null && fDamager.getKiller().getType().equals(fDamager.getFinalEntity().getType())) {
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
                }
                case "<by_item>" -> {
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
                }
                default ->
                        mainBuilder.append(TextComponent.fromLegacyText(mainColor + mainPlaceholder), ComponentBuilder.FormatRetention.NONE);
            }

            mainColor = ChatColor.getLastColors(mainColor + mainBuilder.getCurrentComponent().toString());
        }

        return mainBuilder.create();
    }

    @NotNull
    private TranslatableComponent createTranslatableEntityComponent(@NotNull CommandSender recipient, @NotNull CommandSender sender, @NotNull Entity entity) {
        TranslatableComponent hoverComponent = new TranslatableComponent(NMSUtil.getMinecraftName(entity));

        String formatHoverMessage = Main.locale.getFormatString("entity.hover-message", recipient, sender)
                .replace("<uuid>", entity.getUniqueId().toString());

        ComponentBuilder hoverBuilder = new ComponentBuilder();

        String hoverColor = "";
        for (String hoverPlaceholder : ObjectUtil.splitLine(formatHoverMessage, new ArrayList<>(List.of("<name>", "<type>")))) {
            switch (hoverPlaceholder) {
                case "<name>" ->
                        hoverBuilder.append(TextComponent.fromLegacyText(hoverColor))
                        .append(new TranslatableComponent(NMSUtil.getMinecraftName(entity)))
                        .append(TextComponent.fromLegacyText(hoverColor));
                case "<type>" ->
                        hoverBuilder.append(TextComponent.fromLegacyText(hoverColor))
                        .append(new TranslatableComponent(NMSUtil.getMinecraftType(entity)))
                        .append(TextComponent.fromLegacyText(hoverColor));
                default ->
                        hoverBuilder.append(TextComponent.fromLegacyText(hoverColor + hoverPlaceholder), ComponentBuilder.FormatRetention.NONE);
            }

            hoverColor = ChatColor.getLastColors(hoverColor + hoverBuilder.getCurrentComponent().toString());
        }

        hoverComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverBuilder.create()));
        return hoverComponent;
    }

    @NotNull
    private TextComponent createClickableComponent(@NotNull String chatColor, @NotNull CommandSender sender, @NotNull CommandSender recipient) {
        String playerName = sender.getName();
        String suggestCommand = "/msg " + playerName + " ";
        String showText = Main.locale.getFormatString("player.hover-message", recipient, sender)
                .replace("<player>", playerName);

        TextComponent textComponent = new TextComponent(TextComponent.fromLegacyText(chatColor + playerName));
        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, suggestCommand));
        textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(showText)));
        return textComponent;
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
                    : Main.config.getBoolean("death.message.mob-default")
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
