# MultiChat DiscordSRV Addon
<div align="center">
    <img src="https://wakatime.com/badge/github/TerraByteDev/MultiChat-DiscordSRV-Addon.svg" alt = "Project Hours">
    <a href="https://github.com/TerraByteDev/MultiChat-DiscordSRV-Addon/actions">
        <img alt="Build Status" src="https://github.com/TerraByteDev/MultiChat-DiscordSRV-Addon/actions/workflows/build.yml/badge.svg">
    </a>
</div>

This is a fork of [InteractiveChat-DiscordSRV-Addon](https://github.com/LOOHP/InteractiveChat-DiscordSRV-Addon).\
This fork allows you to have InteractiveChat, ZelChat, ChatControl and Carbon Chat Placeholders translated on DiscordSRV discord messages. As well as sharing items, inventories to discord and sharing images and gifs to the game from discord!

## Changes by this Fork
This fork aims to bring the functionality of the original InteractiveChat addon to other plugins which have similar functionalities.\
This fork removes the complete dependency on InteractiveChat plugin itself - A lot of InteractiveChat classes are carried over to still maintain compatibility.\
All code credits go towards LOOHP, who has given permission for this fork to be created and maintained by myself.

This fork also removes support for 1.21, 1.21.2 and 1.20.5.\
1.20.5 was just a client update, and 1.21 contains bugs and exploits, so it is not recommended to use these versions.\
1.20, 1.20.1, 1.20.2, 1.20.4, 1.20.6, 1.21.1, 1.21.3, 1.21.4 are still supported.\
Additionally, this plugin will display the filtered messages from your chat plugin in discord if supported.

### TL;DR
- Remove dependency on InteractiveChat plugin itself (still supported)
- Remove dependency on ProtocolLib
- Support ChatControl, ZelChat, and Carbon (filters and item/inv/enderchest showcase)
- Clean up some code (configs, commands)
- Remove some support for some minor MC versions (read above)
- Independent discord system, separate from DiscordSRV (optional) [WIP]

> [!WARNING]
> If you encounter bugs while using this fork, **do not report this to the original author!**\
> You should create an issue for [this fork](https://github.com/TerraByteDev/MultiChat-DIscordSRV-Addon/issues).

## Why fork this?
Yes, InteractiveChat can be, and is designed to, work alongside other chat plugins (e.g. ChatControl).\
However, from what I've seen, InteractiveChat uses a not-insignificant portion of the server thread (TL;DR, it has caused lag in many cases).

Some of this has been addressed very recently, such as the previously-not-configurable fixed processing thread pool of **32 threads** however this will not do much, and the default configs are still too high.

Additionally, some will prefer having a plugin that hooks *directly* into their chat plugin!\
Like mentioned before, this plugin also allows the messages that are passed through to DiscordSRV to be *filtered*.\
In many cases, chat plugins will not edit the AsyncChatEvent when filtered, or will just cancel it. This results in one of two outcomes:
1. The message is not sent at all to discord.
2. The unfiltered message is sent to discord.

This fork will also add a completely independent discord implementation, separate from DiscordSRV, which will natively support proxies and will be able to display rank prefixes (LuckPerms), what servers players are on, etc.

## Missing Features
Right now, this fork does not support the following features (that the original plugin *does* support):
- [ ] Proxy support
- [ ] Image file previews from discord

## Built against Spigot
Built against [Spigot's API](https://www.spigotmc.org/wiki/buildtools/) (required mc versions are listed on the spigot page above).
Plugins built against Spigot usually also work with [Paper](https://papermc.io/).

## Development Builds

Get the latest official stable release from the [Releases Tab](https://github.com/TerraByteDev/MultiChat-DiscordSRV-Addon/releases), or download the latest successful development build from the [Actions Tab](https://github.com/TerraByteDev/MultiChat-DiscordSRV-Addon/actions/workflows/build.yml?query=is%3Asuccess).

## Dependencies 

### Required
- [DiscordSRV](https://www.spigotmc.org/resources/discordsrv.18494/)

### Optional (1 required)
- [InteractiveChat](https://www.spigotmc.org/resources/75870/)
- [ZelChat](https://builtbybit.com/resources/zelchat-high-performance-simple.47406/)
- [ChatControl](https://builtbybit.com/resources/chatcontrol-format-filter-chat.18217/)

## Compiling Yourself
> [!NOTE]
> You can get a pre-compiled dev build from the actions workflow here: https://github.com/TerraByteDev/MultiChat-DIscordSRV-Addon/actions

### Prerequisites
- [Maven](https://maven.apache.org/) - This must be installed in order to compile the plugin.

You must compile CraftBukkit versions 1.20 to 1.21.4 via [BuildTools](https://www.spigotmc.org/wiki/buildtools/).\
Ensure the compiled versions are saved to your .m2 repository - Go to Options > Generate Remapped Jars (tick it).\
Ensure that, under Compilation Options, CraftBukkit is set to true. (Options)

### Compiling
Run `mvn clean package` in a terminal in the same folder as the addon.\
The plugin will be available in the `target` directory.
