# JawaPermissions

This is a permissions and user management plugin written for Minecraft Spigot using the Elastic Search database. This plugin is a constant work in progress and will constantly evolve. I DO NOT RECOMMEND using this yet. It is still an alpha plugin and is really only intended for my own server (mc.foxelbox.com)

## Getting Started

1. Clone the repo:
```
git clone https://github.com/arthurbulin/JawaPermissions
```
2. Either open the project in Netbeans and build with the Maven plugin or build from the CLI. NOTE: I never build from the CLI because I'm lazy.
3. You need the Elastic Search Database installed. I test with version 7.2.0, I haven't tried older or newer versions.
4. You will need to initialize the Elastic Search indexes for the plugin. To do this you need another Java application I have written, but not yet put on GitHub. Once it is up there you can create a proper index.

### Prerequisites

You will need Maven and Elastic Search, and of course a Minecraft server running on Spigot.

### Installing

1. Place the JawaPermissions-1.15.X.jar into the server's plugin folder and start the server. Needed configuration files will be created.
2. Edit the config.yml and include your Elastic Search database details. I DO NOT RECOMMEND turning on debug. Its gonna give you a ton of output.
3. Edit the permissions/world.yml and duplicate and rename it for each world you have.
Example:
```
world_the_nether.yml
creative_world.yml
ringworld.yml
etc.
```
4. Restart your server. If you have any malformed config files the server will let you know, although I cannot guarantee how enlightening what it tells you will be.
5. Use the Java application that I have not put on GitHub yet to install yourself as the owner on the server. Owner rank cannot be set except using the Java application as a security precaution.
6. When you join the server it should inform you that you have been installed. This means it's working. Every time you join after that it will inform you that you have been loaded.

## Commands and Permissions

uuid:
description: This command returns the UUID of a user.
usage: /uuid <online user>
permission: jawapermissions.getuuid

setrank:
description: This command will set the player rank.
usage: /setrank <-[o]> -p <user>  -r <rank>
permission: jawapermissions.setrank

ban:
description: Will ban a player
usage: /ban <-[u|s]> -p <playername> -r <reason for ban> [<-d|h|m> <integer>] [<-b> <your username>]
permission: jawapermissions.ban

unban:
description: Will unban a player.
usage: /unban -p <playername> -r <reason for unban>
permission: jawapermissions.unban

reloadperms:
description: Reloads the permissions from disk and recaches them.
permission: jawapermissions.reload

who:
description: Gives relevant user data.
usage: /who <online player>
permission: jawapermissions.who

## Built With

* [NetBeans](https://netbeans.org/) - The IDE used
* [Maven](https://maven.apache.org/) - Dependency Management

## Authors

* **Arthur Bulin aka Jawamaster** - [Arthur Bulin](https://github.com/arthurbulin)

## License

This project is licensed under the MIT License. Just don't be a jerk about it.
