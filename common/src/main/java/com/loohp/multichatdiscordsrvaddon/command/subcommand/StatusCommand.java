package com.loohp.multichatdiscordsrvaddon.command.subcommand;

import com.loohp.multichatdiscordsrvaddon.config.Config;
import com.loohp.multichatdiscordsrvaddon.registry.ResourceRegistry;
import com.loohp.multichatdiscordsrvaddon.resources.ResourcePackInfo;
import com.loohp.multichatdiscordsrvaddon.utils.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Permission;
import static com.loohp.multichatdiscordsrvaddon.MultiChatDiscordSrvAddon.plugin;

@Command("multichat")
public class StatusCommand {

    @Command("status")
    @CommandDescription("Check the status of the plugin")
    @Permission(value = {"multichatdiscordsrv.status"})
    public void execute(
            CommandSender sender
    ) {
        ChatUtils.sendMessage(Config.i().getMessages().statusCommand().defaultResourceHash().replaceFirst("%s", plugin.defaultResourceHash + " (" + VersionManager.exactMinecraftVersion + ")"), sender);
        ChatUtils.sendMessage(Config.i().getMessages().statusCommand().loadedResources(), sender);
        
        for (ResourcePackInfo info : plugin.getResourceManager().getResourcePackInfo()) {
            Component name = ResourcePackInfoUtils.resolveName(info);

            if (info.getStatus()) {
                Component component = Component.text(" - ").append(name).color(info.compareServerPackFormat(ResourceRegistry.RESOURCE_PACK_VERSION) == 0 ? NamedTextColor.GREEN : NamedTextColor.YELLOW);
                Component hoverComponent = ResourcePackInfoUtils.resolveDescription(info);
                if (info.compareServerPackFormat(ResourceRegistry.RESOURCE_PACK_VERSION) > 0) {
                    hoverComponent = hoverComponent.append(Component.text("\n")).append(Component.translatable(TranslationKeyUtils.getNewIncompatiblePack()).color(NamedTextColor.YELLOW));
                } else if (info.compareServerPackFormat(ResourceRegistry.RESOURCE_PACK_VERSION) < 0) {
                    hoverComponent = hoverComponent.append(Component.text("\n")).append(Component.translatable(TranslationKeyUtils.getOldIncompatiblePack()).color(NamedTextColor.YELLOW));
                }

                component = component.hoverEvent(HoverEvent.showText(hoverComponent));
                ChatUtils.sendMessage(component, sender);

                if (!(sender instanceof Player)) {
                    for (Component each : ComponentStyling.splitAtLineBreaks(ResourcePackInfoUtils.resolveDescription(info))) {
                        ChatUtils.sendMessage(Component.text("   - ").color(NamedTextColor.GRAY).append(each), sender);

                        if (info.compareServerPackFormat(ResourceRegistry.RESOURCE_PACK_VERSION) > 0) {
                            ChatUtils.sendMessage("<yellow>     " + LanguageUtils.getTranslation(TranslationKeyUtils.getNewIncompatiblePack(), Config.i().getResources().language()).getResult(), sender);
                        } else if (info.compareServerPackFormat(ResourceRegistry.RESOURCE_PACK_VERSION) < 0) {
                            ChatUtils.sendMessage("<yellow>     " + LanguageUtils.getTranslation(TranslationKeyUtils.getOldIncompatiblePack(), Config.i().getResources().language()).getResult(), sender);
                        }
                    }
                }
            } else {
                Component component = Component.text(" - ").append(name).color(NamedTextColor.RED);
                if (info.getRejectedReason() != null) {
                    component = component.hoverEvent(HoverEvent.showText(Component.text(info.getRejectedReason()).color(NamedTextColor.RED)));
                }

                ChatUtils.sendMessage(component, sender);
                if (!(sender instanceof Player)) {
                    ChatUtils.sendMessage(Component.text("   - ").append(Component.text(info.getRejectedReason()).color(NamedTextColor.RED)).color(NamedTextColor.RED), sender);
                }
            }
        }
    }
}
