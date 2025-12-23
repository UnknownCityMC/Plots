## Plots - a feature packed, WorldGuard based plot system
<img width="2560" height="1440" alt="image" src="https://github.com/user-attachments/assets/7b5f67dc-95d1-4eec-afb3-3cacde4cadbf" />


> [!CAUTION]
> It is not recomened to use this project in production at the moment.
> Most of the funcionality should work and is currently in beta testing on our server, but there may still be some issues with plot ptotection.
> Some features are heavily tailored to the UnknownCity.de server but we plan to make it easier to use for other servers.

#### Motivation
Due to the nature of our server we needed a plot system that allows us to create single plots wherever we want in an existing world.
So why not use already existing solutions like AreaShop or AdvancedRegionMarket?

Some of them are no longer or poorly maintained and basically all of them lack some features we definitly wanted to be possible with our plot system.
Instead of forking an existing solution we decided to completly from scratch. This allows us to build this system tailored to our needs from the beginning.

#### Building
Run `./gradlew shadowJar`

#### Requirements
- Java 21
- Paper 1.21.10
- [Astralib](https://github.com/UnknownCityMC/AstraLib) 0.7.0
- [CoinsEngine](https://www.spigotmc.org/resources/coinsengine-%E2%AD%90-economy-and-custom-currencies.84121/)
- PlaceholderAPI
- FastAsyncWorldEdit

You can download AstraLib at https://ci.unknowncity.de/job/AstraLib/
Please note that AstraLib uses [Sadu](https://github.com/rainbowdashlabs/sadu) for database access, which, in theory, is downloaded automatically at startup, but currently this has some issues due to paper using 
a Google mirror of Maven Central for downloading dependencies and this mirror does not always cache all versions / modules of Sadu.
So maybe you need to manually download it an place it in the `libs` directory of your server.

#### Features

> [!CAUTION]
> Textures in the images are not included
> You need a plugin like Nexo or Itemsadder to export your textures as glphs, then you
> can use the locale file to set them in the gui title

Fully translatable

Create, buy, rent, sell, backup and manage plots.

Add members, change their roles and access, deny players entry or allow everybody to interact.

<img height="400" alt="image" src="https://github.com/user-attachments/assets/42b5b9fa-73e3-4916-8228-13bef2b04a67" />
<img height="400" alt="image" src="https://github.com/user-attachments/assets/dcc215bc-59aa-4e1e-adff-16fb3c5fc0e4" />

Change the plot biome

<img height="400" alt="image" src="https://github.com/user-attachments/assets/58adaa30-ab97-4f24-bfb5-e2b37244416a" />

Decide who can interact for every block individually.

<img height="400" alt="image" src="https://github.com/user-attachments/assets/0db1ef03-5e97-4b30-b445-3b199c6e53ea" />

Plot flags

<img height="400" alt="image" src="https://github.com/user-attachments/assets/f00c6c37-e4cc-4df9-9c69-e0328b18551c" />


Limit how many plots a player can buy based on groups.
Permission: `plots.limit.<group>.<amount> e.g. `plots.limit.starter.2`

Charge money for plots via CoinsEngine.

#### Commands

##### Player commands

| Command                                 | Permission                           | Description                                                    |
| --------------------------------------- | ------------------------------------ | -------------------------------------------------------------- |
| /plot                                   | plots.command.plot                   | Opens the plot settings menu                                   |
| /plots                                  | plots.command.plots                  | Get a overview of your plots and the plots you are a member of |
| /plot claim                             | plots.command.plot.claim             | Claims a plot you are standing on                              |
| /plot auto                              | plots.command.plot.auto              | Claim a random free plot                                       |
| /plot sell                              | plots.command.plot.sell              | Sell a plot you own                                            |
| /plot home [player] [number]            | plots.command.plot.home              | Teleport to a public plot home of a player                     |
| /plot edithome                          | plots.command.plot.edithome          | Change your plot home location / visibility                    |
| /plot info                              | plots.command.plot.info              | Show info about a plot you are standing on                     |
| /plot deny                              | plots.command.plot.deny              | Ban players from you plot                                      |
| /plot undeny                            | plots.command.plot.undeny            | Unban players from your plot                                   |
| /plot member add [palyer] [role]        | plots.command.plot.member.add        | Add a player to your plot                                      |
| /plot member remove [player]            | plots.command.plot.member.remove     | Remove a player from your plot                                 |
| /plot member changerole [member] [role] | plots.command.plot.member.changerole | Change the role of a plot member (e.g Co Owner, Member)        |

##### Admin commands

| Command                                                    | Description                                                                                                                                |
| ---------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------ |
| /plotadmin list                                            | List all plots                                                                                                                             |
| /plotadmin reload                                          | Reload messages and config                                                                                                                 |
| /plotadmin group create [name]                             | Create a plot group                                                                                                                        |
| /plotadmin group delete [name]                             | Deletes a plot group (All plots in the group loose their group but don't get deleted)                                                      |
| /plotadmin group item [group] set / unset                  | Sets the display item for a plotgroup in the `/plots` gui                                                                                  |
| /plotadmin createBuyFromRegion [price] [group]             | Create a buy plot from an existing worldguard region you are standing in                                                                   |
| /plotadmin createRentFromRegion [price] [group] [duration] | Create a rent plot from an existing worldguard region you are standing in                                                                  |
| /plotadmin delete                                          | Delete a plot and all associated settings from the database (Does not delete the WorldGuard region)                                        |
| /plotadmin reset                                           | Resets a plot to the state before a player bought it                                                                                       |
| /plotadmin setGroup [group]                                | Change the group of the plot you are standing in                                                                                           |
| /plotadmin setOwner [player]                               | Change the owner of the plot you are standing in                                                                                           |
| /plotadmin setStatus [status]                              | Change the status of a plot (for example: set to unavailable to prevent players from buying a plot )                                       |
| /plotadmin setRentInterval                                 | Change the time after which the next rent payment is due for a rentplot you are standing in                                                |
| /plotadmin updatehome                                      | Set the plothome to your current position                                                                                                  |
| /plotadmin teleport [plot-id]                              | Teleport to a plot by id                                                                                                                   |
| /plotadmin signs [...]                                     | Manage signs for a plot                                                                                                                    |
| /plotadmin signLink                                        | Activate the signlink mode to view or add signs from / to a plot                                                                           |
| /plotadmin loadBackup [player]                             | Load a backup of a rent plot that was made before the plot expired due to the player not having enough founds to continue renting the plot |
| /plotadmin globalbackup load/create                        | Backup all plots as schematics and load them back in.                                                                                      |
