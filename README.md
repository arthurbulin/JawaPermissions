# JawaPermissions

JawaPermissions is a permissions and user management plugin written for Minecraft Paper/Spigot using the ElasticSearch database.


## Getting Started

1. Clone the repo:
```
git clone https://github.com/arthurbulin/JawaPermissions
```
2. Either open the project in Netbeans and build with the Maven plugin or build from the CLI. NOTE: I never build from the CLI because I'm lazy.

### Prerequisites

You will need 
    - Maven to build the plugin
    - Java 17
    - ElasticSearch (7.15.0 is the current version)
    - Minecraft server running on Paper/Spigot (Paper is prefered and I will probably not support Spigot by the end of 1.20 support)

### Installing

1. Place the JawaPermissions-1.20.X-#.#.jar into the server's plugin folder and start the server. Needed configuration files will be created.
2. Shutdown your server
2. Edit the config.yml. See the Configuration Paramaters section below.
4. Restart your server. If you have any malformed config files the server will let you know, although I cannot guarantee how enlightening what it tells you will be.

## Configuration Parameters
### JawaPermissions/config.yml
Within the JawaPermissions folder you will find a config.yml file. This config must be strictly YAML formatted or it may fail to load and will be overwritten.

* debug: \<true/false\>
  - If true debug prints to logs
* new-player-message: \<Arbitrary string\>
  - Message to notify players when they have been installed for the first time (first join)
* ban-appeal-suffix: \<Arbitrary String\>
  - The message to append to ban notifications that contains appeal information
* ban-reason-length: \<integer\>
  - An arbitrary length to force staff to input legit reasons for bans. This forces the ban reason to be 3 words long. 3 is the minimum value and cannot be lowered as 3 is the hard coded minimum.
* unban-reason-length
  - An arbitrary length to force staff to input legit reasons for unbans. This forces the unban reason to be 3 words long. 3 is the minimum value and cannot be lowered as 3 is the hard coded minimum.
* geoip-enabled: \<true/false\>
  - This is false by default. Enabling GeoIP information within the /who command requries some external setup. You need the MaxMind geoIP city database for this to work.
* geoip-database: \<name of the GeoLite .mmdb file\>
  - This is the name of the .mmdb file used for the GeoIP lookup. The deafult is the default name as of writting this. The mmdb should be placed within the JawaPermissions folder. You need an account with MaxMind to get this file but it is free.
* alt-notification:
    * enabled: \<true/false\>
      - Enables or Disabled automated notification of staff of possible alts joining. 
    * minimum-immunity: \<integer\>
      - Sets the immunity level of an alt account based on the immunities provided within the permissions file. i.e. I as the owner can have an alt, but my immunity setting (0) does not notify staff of my alt status. Don't missue this.
* index-customization:
    * bans: \<arbitrary string\>
      - This is the index name that will be used by the plugins to store ban data. I recommend naming it bans-<minecraft version> i.e. bans-120. So that in the future you can keep database versions seperate if I change the schema. Although if I do I'll try to ensure the update is automatic but up to this point, it is NOT.

### JawaPermissions/permissions/<worldname>.yml
Permissions within JawaPermissions are based on the world the player is. Each YAML file must be named with the world name. YAML files CAN be symlinks. i.e. I can write the world.yml file and then create symlinks for the nether and end if they have the same permissions.

A permissions definition yml file can accept any number of ranks and permissions. It also defines the default username color for that rank, the immunity level (higher the less trust they get), a description which can be viewed by staff and players, and a list of permissions and prohibitions.
A permission entitles a user to perform an action (i.e. set a home location). A prohibition denies a user to perform an action (i.e. deny access to a world). Whether the node is a permission or prohibition depends on the plugin's default setting for that command/action.

There are two ranks that a server must have at the moment. These are guest and owner. A guest is nearly entirly unprivilaged while an ownder is fully privilaged.
* owner
  - The owner rank is protected in various ways. An owner cannot be banned, noone can set a user to owner (you must use autoelevate), owners can execute commands with no respect to immunity controls.
* guest
  - The lowest rank possible and initially contains no permissions. In the event a user's rank is indeterminant, such as a rank change in the yml but not in the DB, the user is treated as a guest until it is fixed.

The yml should follow the following structure. Note that order does not matter at the relative indention level.

\<rank name\>:
    color: \<Minecraft color code\>
    immunity: \<Integer\>
    description: \<Arbitrary String\>
    inherits: \<another rank name\>
    permissions:
        - \<permission node\>
    prohibition:
        - \<permission node\>

## Commands and Permissions
### Commands and root permission nodes
These are the commands available within JawaPermissions, their descriptions, and relevent command usage permissions nodes.
uuid:
description: This command returns the UUID of a user.
usage: /uuid [<player>]
permission: jawapermissions.getuuid

setrank:
description: This command will set the player rank.
usage: 'Run /setrank with no arguments to see a full help'
permission: jawapermissions.setrank

ban:
description: Will ban a player
usage: 'Run /ban with no arguments to see a full help'
permission: jawapermissions.ban

unban:
description: Will unban a player.
usage: 'Run /unban with no arguments to see a full help'
permission: jawapermissions.unban

testcommand:
usage: Don't
permission: jawapermissions.testcommand

reloadperms:
description: Reloads the permissions from disk and recaches them.
permission: jawapermissions.reload

who:
description: Gives relavant user data.
permission: jawapermissions.who

baninfo:
description: "Gives a user information about another's ban"
permission: jawapermissions.baninfo

alt:
description: "Gives staff information about possible account alternets"
usage: "/alt <player>"
permission: jawapermissions.alt

playerinfo:
description: "Gives staff detailed player info"
permission: jawapermissions.playerinfo

rankinfo:
description: "Provides information about a rank"
usage: "/rankinfo [description|requirements] <rank>"
permission: jawapermissions.rankinfo

playercomment:
description: "Allows the viewing, addition, and removal of comments from a player's data"
usage: "/playercomment <player> [add|remove|list] [<comment>|<comment#>|<page #>]"
permission: jawapermissions.playercomment

### Permissions
Some of these permissions are root permissions (which entitle a user to run a command), but some are additional features of a command.

jawapermissions.alt:
description: "Allows searching for alt accounts based on IP address"
default: op

jawapermissions.baninfo:
description: "Allows viewing of ban data"
default: op

jawapermissions.who:
description: "Allows a user to see who is online"
default: true

jawapermissions.who.detail:
description: "Allows an administrative user to see detailed information about a player"
default: op

jawapermissions.reload:
description: "Allows an administrative user to reload rank permissions, immunity levels, rank descriptions, and colors"
default: op

jawapermissions.ban:
description: "Allows an administrative user to ban a user"
default: op

jawapermissions.unban:
description: "Allows an administative user to unban a user"
default: op

jawapermissions.setrank:
description: "Allows an administative user to set the rank of a user"
default: op

jawapermissions.uuid:
description: "Allows an administative user to see the UUID of a player"
default: op

jawapermissions.rankinfo:
description: "Allows a player to see the description or requirements for a rank"
deafult: true

jawapermissions.playercomment:
description: "Allows an administrative user to see admin comments on a user"
default: false

jawapermissions.playercomment.add:
description: "Allows an administrative user to add admin comments on a user"
default: false

jawapermissions.playercomment.remove:
description: "Allows an administrative user to remove admin comments on a user"
default: false

## Built With

* [NetBeans](https://netbeans.org/) - The IDE used
* [Maven](https://maven.apache.org/) - Dependency Management

## Authors

* **Arthur Bulin aka Jawamaster** - [Arthur Bulin](https://github.com/arthurbulin)

## License

This project is licensed under the MIT License. Just don't be a jerk about it.
