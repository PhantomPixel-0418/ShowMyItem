<!-- Project badges (replace links and images as needed) -->
<div align="center">
  <a href="https://github.com/PhantomPixel-0418/ShowMyItem">
    <img src="https://github.com/PhantomPixel-0418/ShowMyItem/blob/master/src/main/assets/showmyitem/icon.png?raw=true" alt="Logo" width="80" height="80">
  </a>

<h3 align="center">Show My Item</h3>

  <p align="center">
    Elegantly display the item in your hand in chat — making sharing easier!
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
    <li><a href="#roadmap">Roadmap</a></li>
    <li><a href="#contributing">Contributing</a></li>
    <li><a href="#license">License</a></li>
    <li><a href="#contact">Contact</a></li>
    <li><a href="#acknowledgments">Acknowledgments</a></li>
  </ol>
</details>

## About The Project

![Show My Item Screenshot](https://github.com/PhantomPixel-0418/ShowMyItem/blob/master/images/screenshot.png?raw=true)

**Show My Item** is a lightweight Minecraft Fabric mod that adds a fun little feature to chat: when you type `[item]` in chat, it automatically replaces it with the name of the item you're holding, and other players can hover over that name to **see the item's full details** (name, enchantments, durability, etc.).

**What problem does it solve?**

- No more manually typing complicated item names or making your friends guess what you're holding.
- Hover to reveal — no extra commands needed, making communication more intuitive.
- Purely for sharing and showing off with friends, with no cheating or game-breaking elements.

**Why choose it?**

- **Simple**: No configuration needed, just install and play.
- **Lightweight**: Server-side only, no performance impact.
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
- **Fabric API** (required on both server and client; however, this mod runs only server-side. If the client does not have it, hover effects won't show.)

### Installation

1. **Download the mod**  
   Download the latest JAR file from [Modrinth](https://modrinth.com/mod/showmyitem) or [GitHub Releases](https://github.com/PhantomPixel-0418/ShowMyItem/releases).

2. **Place the mod**  
   Put the JAR file into the `mods` folder of your server.  
   *For singleplayer, put it into the client's `mods` folder.*

3. **Start the game/server**  
   No extra configuration needed — the mod will work automatically.

<p align="right">(<a href="#top">back to top</a>)</p>

## Usage

### Basic Usage

1. Make sure you are holding any item (if empty-handed, it will show "Empty Hand").
2. Type a message containing `[item]` in chat, for example:

    ```text
    Check this out! [item]
    ```

3. After sending, the `[item]` in the message will be replaced with your main-hand item, formatted as `[Item Name]`.
4. Other players can hover over `[Item Name]` to see the item's detailed tooltip (enchantments, durability, custom name, etc.).

### Example

*Before sending:*
> Check this out! [item]

*After sending (assuming you're holding an enchanted diamond sword):*
> Check this out! [Diamond Sword]
>
> *Hovering over "[Diamond Sword]" shows:*
>
> ```
> Diamond Sword
> Sharpness IV
> Unbreaking III
> etc.
> ```

### Notes

- This mod is designed to work **server-side only** — the client does not need to install it.

<p align="right">(<a href="#top">back to top</a>)</p>

## Roadmap

- [x] Support offhand item display (e.g., `[offhand]` placeholder)
- [ ] Add configuration options to customize display format (e.g., show stack size or not)
- [x] Support more message types (e.g., /tell private messages)
- [ ] Localization support (multiple languages)

Check the [open issues](https://github.com/PhantomPixel-0418/ShowMyItem/issues) for a full list of requested features and known issues.

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