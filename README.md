[![Build Status](https://ci.loohpjames.com/job/InteractiveChat-DiscordSRV-Addon/badge/icon)](https://ci.loohpjames.com/job/InteractiveChat-DiscordSRV-Addon/)
# MultiChat DiscordSRV Addon

This is a fork of [InteractiveChat-DiscordSRV-Addon](https://github.com/LOOHP/InteractiveChat-DiscordSRV-Addon)!\
Have InteractiveChat, ZelChat and ChatControl Placeholders translated on DiscordSRV discord messages. As well as sharing items, inventories to discord and sharing images and gifs to the game from discord!

## Changes by this Fork
This fork aims to bring the functionality of the original InteractiveChat addon to other plugins which have similar functionalities.\
This fork mostly removes the complete dependency on InteractiveChat - A lot of InteractiveChat classes are carried over to still maintain compatibility.\
All code credits go towards LOOHP, who has given permission for this fork to be created and maintained by me.

> [!WARNING]
> If you encounter bugs while using this fork, **do not report this to the original author!**\
> You should create an issue for [this fork](https://github.com/TerraByteDev/MultiChat-DIscordSRV-Addon/issues).

## Built against Spigot
Built against [Spigot's API](https://www.spigotmc.org/wiki/buildtools/) (required mc versions are listed on the spigot page above).
Plugins built against Spigot usually also work with [Paper](https://papermc.io/).

## Development Builds

- TODO

## Dependencies 

### Required
- [DiscordSRV](https://www.spigotmc.org/resources/discordsrv.18494/)

### Optional (1 required)
- [InteractiveChat](https://www.spigotmc.org/resources/75870/)
- [ZelChat](https://builtbybit.com/resources/zelchat-high-performance-simple.47406/)
- [ChatControl](https://builtbybit.com/resources/chatcontrol-format-filter-chat.18217/)

## Compiling Yourself
### Prerequisites
- [Maven](https://maven.apache.org/) - This must be installed in order to compile the plugin.

You must compile CraftBukkit versions 1.20 to 1.21.4 via [BuildTools](https://www.spigotmc.org/wiki/buildtools/).\
Ensure the compiled versions are saved to your .m2 repository - Go to Options > Generate Remapped Jars (tick it).\
Ensure that, under Compilation Options, CraftBukkit is set to true. (Options)

### Compiling
Run `mvn clean package` in a terminal in the same folder as the addon.\
The plugin will be available in the `target` directory.