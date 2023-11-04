package net.flectone.chat.builder;

import net.flectone.chat.FlectoneChat;
import net.flectone.chat.component.FComponent;
import net.flectone.chat.component.FLocaleComponent;
import net.flectone.chat.component.FPlayerComponent;
import net.flectone.chat.component.FURLComponent;
import net.flectone.chat.manager.FPlayerManager;
import net.flectone.chat.model.message.WordParams;
import net.flectone.chat.model.player.FPlayer;
import net.flectone.chat.module.FModule;
import net.flectone.chat.module.playerMessage.formatting.FormattingModule;
import net.flectone.chat.module.playerMessage.patterns.PatternsModule;
import net.flectone.chat.module.playerMessage.swearProtection.SwearProtectionModule;
import net.flectone.chat.util.MessageUtil;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static net.flectone.chat.manager.FileManager.config;

public class MessageBuilder {

    private final List<WordParams> messages = new ArrayList<>();
    private final ComponentBuilder componentBuilder = new ComponentBuilder();
    private final ItemStack itemStack;
    private final Player sender;

    public MessageBuilder(@Nullable Player player, @Nullable ItemStack itemStack, @NotNull String message, @NotNull List<String> featuresList) {
        this.itemStack = itemStack;
        this.sender = player;

        if (featuresList.contains("patterns")) {
            FModule fModule = FlectoneChat.getModuleManager().get(PatternsModule.class);
            if (fModule instanceof PatternsModule patternsModule) {
                message = patternsModule.replace(sender, message);
            }
        }
        if (featuresList.contains("swear-protection")) {
            FModule fModule = FlectoneChat.getModuleManager().get(SwearProtectionModule.class);
            if (fModule instanceof SwearProtectionModule swearProtectionModule) {
                message = swearProtectionModule.replace(sender, message);
            }
        }

        FModule fModule = FlectoneChat.getModuleManager().get(FormattingModule.class);
        if (fModule instanceof FormattingModule formattingModule) {
            boolean mentionEnabled = featuresList.contains("mention");
            formattingModule.replace(sender, message, "", messages, itemStack, mentionEnabled, featuresList.contains("formatting"));
        } else {
            WordParams wordParams = new WordParams();
            wordParams.setText(message);
            messages.add(wordParams);
        }
    }

    @NotNull
    public String getMessage(String color) {
        StringBuilder stringBuilder = new StringBuilder();
        for (WordParams wordParams : messages) {
            if (wordParams == null) {
                stringBuilder.append(" ");
                continue;
            }

            String word = wordParams.getText();
            if (wordParams.isHide()) {
                word = wordParams.getHideMessage();
                assert word != null;
            }

            if (wordParams.isEdited() || wordParams.isFormatted()) {
                word = MessageUtil.formatAll(null, word);
                word = ChatColor.stripColor(word);
            }

            if (sender.hasPermission("flectonechat.player-message.placeholders")) {
                word = MessageUtil.formatPAPI(sender, sender, word, true);
            }

            if (sender.hasPermission("flectonechat.player-message.formatting")) {
                word = MessageUtil.formatAll(sender, sender, word, true);
            }

            color = ChatColor.getLastColors(color) + ChatColor.getLastColors(word);

            String newFormatting = wordParams.getFormatting();
            if (!newFormatting.isEmpty()) {
                word = color + newFormatting + word + ChatColor.RESET + color;
            }

            stringBuilder.append(word);
        }

        return stringBuilder.toString();
    }

    @NotNull
    public BaseComponent[] buildFormat(@Nullable Player sender, @NotNull Player recipient, @NotNull String format, boolean formatClickable) {
        ComponentBuilder componentBuilder = new ComponentBuilder();

        String[] formats = format.split("<message>");

        formats[0] = MessageUtil.formatAll(sender, recipient, MessageUtil.formatPlayerString(sender, formats[0]));

        FComponent fComponent = formatClickable
                ? new FPlayerComponent(sender, recipient, formats[0])
                : new FComponent(formats[0]);

        componentBuilder.append(fComponent.get());

        String color = ChatColor.getLastColors(formats[0]);

        componentBuilder.append(buildMessage(sender, recipient, color), ComponentBuilder.FormatRetention.NONE);

        if (formats.length > 1) {
            String string = formats[1];
            string = MessageUtil.formatAll(sender, recipient, string);
            componentBuilder.append(FComponent.fromLegacyText(color + string), ComponentBuilder.FormatRetention.NONE);
        }

        return componentBuilder.create();
    }

    @NotNull
    public BaseComponent[] buildMessage(@Nullable Player sender, @NotNull Player recipient, @NotNull String lastColor) {
        ComponentBuilder componentBuilder = new ComponentBuilder();

        for(WordParams wordParams : messages) {

            if (wordParams == null) {
                componentBuilder.append(" ");
                continue;
            }

            String word = lastColor + wordParams.getFormatting() + wordParams.getText();
            if (wordParams.isEdited() && !wordParams.isHide()) {
                word = MessageUtil.formatAll(sender, recipient, word);
            }

            if (sender == null || sender.hasPermission("flectonechat.player-message.placeholders")) {
                word = MessageUtil.formatPAPI(sender, recipient, word, true);
            }

            if (sender == null || (sender.hasPermission("flectonechat.player-message.formatting") && !wordParams.isEdited())) {
                wordParams.setFormatted(true);

                String newWord = MessageUtil.formatAll(sender, recipient, lastColor + wordParams.getText(), true);

                lastColor = ChatColor.getLastColors(newWord);

                word = lastColor + wordParams.getFormatting() + ChatColor.stripColor(newWord);
            }

            FComponent wordComponent = new FComponent(word);

            if (wordParams.isItem()) {
                componentBuilder.append(createItemComponent(itemStack, lastColor, recipient, sender));
                continue;
            }

            if (wordParams.isClickable()) {
                FPlayer fPlayer = FPlayerManager.get(wordParams.getPlayerPingName());
                if (fPlayer != null) {
                    wordComponent = new FPlayerComponent(fPlayer.getPlayer(), recipient, word);
                }

            } else if (wordParams.isUrl()) {
                wordComponent = new FURLComponent(sender, recipient, word, wordParams.getUrl());
            } else if (wordParams.isHide()) {
                wordComponent = new FComponent(MessageUtil.formatAll(sender, recipient, wordParams.getHideMessage()));
                wordComponent.addHoverText(word);
            }

            componentBuilder.append(wordComponent.get(), ComponentBuilder.FormatRetention.NONE);
        }

        return componentBuilder.create();
    }

    @NotNull
    private BaseComponent[] createItemComponent(@NotNull ItemStack itemStack, @NotNull String lastColor, @NotNull Player recipient, @Nullable Player sender) {
        ComponentBuilder itemBuilder = new ComponentBuilder();

        String[] componentsStrings = config.getVaultString(sender, "player-message.formatting.list.item.format").split("<message>");
        BaseComponent[] color = FComponent.fromLegacyText(lastColor);

        return itemBuilder
                .append(color)
                .append(FComponent.fromLegacyText(MessageUtil.formatAll(sender, recipient, componentsStrings[0])))
                .append(new FLocaleComponent(itemStack).get())
                .append(FComponent.fromLegacyText(MessageUtil.formatAll(sender, recipient, componentsStrings[1])))
                .append(color)
                .create();
    }

    @NotNull
    public BaseComponent[] create() {
        return componentBuilder.create();
    }
}
