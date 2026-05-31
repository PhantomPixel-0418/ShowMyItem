<!-- Project badges (replace links and images as needed) -->
<div align="center">
  <a href="https://github.com/PhantomPixel-0418/ShowMyItem">
    <img src="https://github.com/PhantomPixel-0418/ShowMyItem/blob/master/src/main/assets/showmyitem/icon.png?raw=true" alt="Logo" width="80" height="80">
  </a>

<h3 align="center">Show My Item</h3>

  <p align="center">
    Elegantly display your held item, inventory, or ender chest in chat — making sharing easier!
    <br />
    <a href="https://github.com/PhantomPixel-0418/ShowMyItem"><strong>Explore the docs »</strong></a>
    <br />
    <br />
    <a href="https://modrinth.com/mod/showmyitem">View Modrinth Page</a>
    ·
    <a href="https://github.com/PhantomPixel-0418/ShowMyItem">View GitHub Page</a>
    ·
    <a href="https://github.com/PhantomPixel-0418/ShowMyItem/issues">Report Bug</a>
    ·
    <a href="https://github.com/PhantomPixel-0418/ShowMyItem/issues">Request Feature</a>
  </p>
</div>

<!-- Table of Contents -->
<details>
  <summary>Table of Contents</summary>
  <ol>
    <li><a href="#about-the-project">About The Project</a>
      <ul>
        <li><a href="#built-with">Built With</a></li>
      </ul>
    </li>
    <li><a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#prerequisites">Prerequisites</a></li>
        <li><a href="#installation">Installation</a></li>
      </ul>
    </li>
    <li><a href="#usage">Usage</a></li>
    <li><a href="#configuration">Configuration</a></li>
    <li><a href="#roadmap">Roadmap</a></li>
    <li><a href="#contributing">Contributing</a></li>
    <li><a href="#license">License</a></li>
    <li><a href="#contact">Contact</a></li>
    <li><a href="#acknowledgments">Acknowledgments</a></li>
  </ol>
</details>

## About The Project

![Show My Item Screenshot](https://github.com/PhantomPixel-0418/ShowMyItem/blob/master/images/screenshot.png?raw=true)

**Show My Item** is a lightweight Minecraft Fabric mod designed for servers. It allows players to share their held item, entire inventory, or ender chest in chat using simple placeholders like `[item]`, `[inventory]`, or `[enderchest]`. Other players can hover over item names to see full tooltips, or click links to view a read-only container of the shared inventory.

**What problem does it solve?**

- No more manually typing complicated item names or making friends guess what you're holding.
- Share your inventory or ender chest instantly without complicated commands.
- Hover to see details, click to view the full inventory — communication becomes intuitive and visual.
- Purely for sharing and showing off, with no cheating or game-breaking elements.

**Why choose it?**

- **Simple**: Natural language placeholders, works with both English and Chinese.
- **Powerful**: Supports main hand, offhand, inventory (including armor and offhand), and ender chest.
- **Configurable**: Carpet-like chat menu for adjusting expiration time, max snapshots, and language on the fly.
- **Lightweight**: Server-side only; clients do not need to install the mod.
- **Compatible**: Built on Fabric API, compatible with most mods.

Of course, no mod can satisfy everyone. If you have any suggestions, feel free to open an Issue or Pull Request.

<p align="right">(<a href="#top">back to top</a>)</p>

### Built With

- [Minecraft 1.21.4](https://www.minecraft.net)
- [Fabric Loader](https://fabricmc.net/) (>=0.16.9)
- [Fabric API](https://modrinth.com/mod/fabric-api) (>=0.119.4)
- Java 21

<p align="right">(<a href="#top">back to top</a>)</p>

## Getting Started

To use this mod on your server (or in singleplayer), follow these steps.

### Prerequisites

- **Minecraft 1.21.4** server or client (if playing singleplayer).
- **Fabric Loader** 0.16.9 or higher.
- **Fabric API** (required on server; not required on client, but having it doesn't hurt).

### Installation

1. **Download the mod**  
   Download the latest JAR file from [Modrinth](https://modrinth.com/mod/showmyitem) or [GitHub Releases](https://github.com/PhantomPixel-0418/ShowMyItem/releases).

2. **Place the mod**  
   Put the JAR file into the `mods` folder of your server.  
   *For singleplayer, also put it into the client's `mods` folder (it will work on the integrated server).*

3. **Start the game/server**  
   No extra configuration needed — the mod will work automatically, and a config file will be created at `config/showmyitem.json`.

<p align="right">(<a href="#top">back to top</a>)</p>

## Usage

### Placeholders

The mod supports the following placeholders that can be used directly in chat (these are the English version placeholders; see the Chinese documentation for Chinese placeholders):

| Placeholder    | Description                                                                           |
| -------------- | ------------------------------------------------------------------------------------- |
| `[item]`       | Main hand item                                                                        |
| `[offhand]`    | Offhand item                                                                          |
| `[inventory]`  | Share your entire inventory (including armor and offhand); generates a clickable link |
| `[enderchest]` | Share your ender chest; generates a clickable link                                    |

>[!NOTE]
> Placeholders are independent of server language. You can use these English placeholders in any language environment. If you prefer Chinese, you can also use the Chinese equivalents `[物品]`, `[副手]`, `[背包]`, `[末影箱]`.

### Basic Usage

1. Make sure you are holding an item.
2. Type a message containing placeholders, for example:
    ```
    Check this out! [item]
    My inventory: [inventory]
    My ender chest: [enderchest]
    ```
3. After sending, placeholders will be replaced with item names or clickable links.
4. Other players can hover over item names to see tooltips, or click `[Player's Inventory]` / `[Player's Ender Chest]` to open a read-only view.

### Interaction Details

- **Hover**: Hovering over an item name shows its full tooltip (enchantments, durability, custom name, etc.).
- **Click inventory/ender chest links**: Opens a secure, read-only container showing the target's main inventory, armor, offhand, or ender chest. Items cannot be moved or taken, preventing item duplication exploits.
- **Permission**: Only the snapshot creator or OPs can open a link; others will receive an error message.

<p align="right">(<a href="#top">back to top</a>)</p>

## Configuration

The mod features a Carpet-like chat menu accessible via the `/showmyitem` command.

### Commands

| Command                              | Description                        | Permission    |
| ------------------------------------ | ---------------------------------- | ------------- |
| `/showmyitem`                        | Show all settings and version info | Everyone      |
| `/showmyitem category <category>`    | Show settings for a category       | Everyone      |
| `/showmyitem find <query>`           | Fuzzy search for settings          | Everyone      |
| `/showmyitem viewinv <snapshotID>`   | View inventory snapshot            | Creator or OP |
| `/showmyitem viewender <snapshotID>` | View ender chest snapshot          | Creator or OP |
| `/showmyitem set <key> <value>`      | Modify a config option             | OP (level 2)  |
| `/showmyitem reloadconfig`           | Reload configuration from file     | OP (level 2)  |

### Configuration Options

Config file is located at `config/showmyitem.json`. You can edit it directly or change values in-game using the menu/commands:

- `snapshotExpiryMs`: Expiration time for snapshots in milliseconds (default 300000 = 5 minutes).
- `maxSnapshots`: Maximum number of snapshots stored at once (default 100).
- `defaultLanguage`: Server default language, either `en_us` or `zh_cn`.

After editing, use `/showmyitem reloadconfig` to apply changes without restarting.

<p align="right">(<a href="#top">back to top</a>)</p>

## Roadmap

- [x] Main hand item display (`[item]`)
- [x] Offhand item display (`[offhand]`)
- [x] Inventory sharing with armor and offhand (`[背包]` / `[inventory]`)
- [x] Ender chest sharing (`[末影箱]` / `[enderchest]`)
- [x] Server-side only architecture
- [x] Multi-language support (English / Chinese) with auto-detection of placeholders
- [x] Carpet-style config menu with clickable settings
- [x] Config hot-reload
- [x] Secure read-only containers to prevent item duplication
- [ ] Share more info: health, hunger, etc.
- [ ] Share targeted block/entity
- [ ] More language support

See [open issues](https://github.com/PhantomPixel-0418/ShowMyItem/issues) for a full list of requested features and known issues.

<p align="right">(<a href="#top">back to top</a>)</p>

## Contributing

Contributions are what make the open source community such an amazing place to learn, inspire, and create. **Any contributions you make are greatly appreciated**.

If you have a suggestion that would make this better, please fork the repo and create a pull request. You can also simply open an issue with the tag "enhancement". Don't forget to give the project a star! Thanks again!

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

<p align="right">(<a href="#top">back to top</a>)</p>

## Building
- Run `./gradlew build` to build the mod.
- The remapped jar is located in `build/libs/`.
- The development jar is placed in `build/devlibs/` (not for distribution).

## License

Distributed under the MIT License. See [LICENSE](/LICENSE) for more information.

<p align="right">(<a href="#top">back to top</a>)</p>

## Contact

Project Link: [https://github.com/PhantomPixel-0418/ShowMyItem](https://github.com/PhantomPixel-0418/ShowMyItem)

<p align="right">(<a href="#top">back to top</a>)</p>

## Acknowledgments

This README template was inspired by:

- [Best-README-Template](https://github.com/othneildrew/Best-README-Template)
- [Fabric Wiki](https://fabricmc.net/wiki/start)

<p align="right">(<a href="#top">back to top</a>)</p>
