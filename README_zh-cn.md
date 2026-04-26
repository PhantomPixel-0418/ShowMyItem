<!-- Project badges (replace links and images as needed) -->
<div align="center">
  <a href="https://github.com/PhantomPixel-0418/ShowMyItem">
    <img src="https://github.com/PhantomPixel-0418/ShowMyItem/blob/master/src/main/assets/showmyitem/icon.png?raw=true" alt="Logo" width="80" height="80">
  </a>

<h3 align="center">Show My Item</h3>

  <p align="center">
    优雅地在聊天中展示您手中的物品——让分享变得更简单！
    <br />
    <a href="https://github.com/PhantomPixel-0418/ShowMyItem"><strong>探索文档 »</strong></a>
    <br />
    <br />
    <a href="https://modrinth.com/mod/showmyitem">查看 Modrinth 页面</a>
    ·
    <a href="https://github.com/PhantomPixel-0418/ShowMyItem">查看 GitHub 页面</a>
    ·
    <a href="https://github.com/PhantomPixel-0418/ShowMyItem/issues">报告 Bug</a>
    ·
    <a href="https://github.com/PhantomPixel-0418/ShowMyItem/issues">请求新功能</a>
  </p>
</div>

<!-- Table of Contents -->
<details>
  <summary>目录</summary>
  <ol>
    <li><a href="#about-the-project">关于本项目</a>
      <ul>
        <li><a href="#built-with">技术栈</a></li>
      </ul>
    </li>
    <li><a href="#getting-started">快速开始</a>
      <ul>
        <li><a href="#prerequisites">前置要求</a></li>
        <li><a href="#installation">安装</a></li>
      </ul>
    </li>
    <li><a href="#usage">使用方法</a></li>
    <li><a href="#roadmap">开发路线</a></li>
    <li><a href="#contributing">贡献指南</a></li>
    <li><a href="#license">许可证</a></li>
    <li><a href="#contact">联系方式</a></li>
    <li><a href="#acknowledgments">致谢</a></li>
  </ol>
</details>

## 关于本项目

![Show My Item 截图](https://github.com/PhantomPixel-0418/ShowMyItem/blob/master/images/screenshot.png?raw=true)

**Show My Item** 是一个轻量级的 Minecraft Fabric 模组，为聊天增添了一个有趣的小功能：当您在聊天中输入 `[item]` 时，它会自动替换为您手中物品的名称，其他玩家可以将鼠标悬停在该名称上**查看物品的完整信息**（名称、附魔、耐久等）。

**它解决了什么问题？**

- 不再需要手动输入复杂的物品名称，也不用让朋友们去猜您拿的是什么。
- 悬停即可查看——无需额外命令，让交流更加直观。
- 纯粹用于与朋友分享和炫耀，没有任何作弊或破坏游戏平衡的元素。

**为什么选择它？**

- **简单**：无需配置，装上即用。
- **轻量**：纯服务端模组，不影响性能。
- **兼容**：基于 Fabric API，与大多数模组兼容。

当然，没有一个模组能满足所有人的需求。如果您有任何建议，欢迎提交 Issue 或 Pull Request。

<p align="right">(<a href="#top">回到顶部</a>)</p>

### 技术栈

- [Minecraft 1.21.4](https://www.minecraft.net)
- [Fabric Loader](https://fabricmc.net/) (>=0.16.9)
- [Fabric API](https://modrinth.com/mod/fabric-api) (>=0.119.4)
- Java 21

<p align="right">(<a href="#top">回到顶部</a>)</p>

## 快速开始

在您的服务器（或单人游戏）中使用此模组，请按照以下步骤操作。

### 前置要求

- **Minecraft 1.21.4** 服务端或客户端（如果玩单人游戏）。
- **Fabric Loader** 0.16.9 或更高版本。
- **Fabric API**（服务端和客户端都需要；但本模组仅服务端运行。若客户端未安装，悬停效果将无法显示。）

### 安装

1. **下载模组**  
   从 [Modrinth](https://modrinth.com/mod/showmyitem) 或 [GitHub Releases](https://github.com/PhantomPixel-0418/ShowMyItem/releases) 下载最新的 JAR 文件。

2. **放置模组**  
   将 JAR 文件放入服务器的 `mods` 文件夹中。  
   *如果是单人游戏，则放入客户端的 `mods` 文件夹。*

3. **启动游戏/服务器**  
   无需额外配置——模组将自动运行。

<p align="right">(<a href="#top">回到顶部</a>)</p>

## 使用方法

### 基本用法

1. 确保您手上拿着任意物品（如果空手，则会显示“空手”）。
2. 在聊天中发送包含 `[item]` 的消息，例如：

    ```text
    看看这个！ [item]
    ```

3. 发送后，消息中的 `[item]` 会被替换为您主手上物品名称，格式为 `[物品名称]`。
4. 其他玩家可以悬停在 `[物品名称]` 上查看物品的详细提示（附魔、耐久、自定义名称等）。

### 示例

*发送前：*
> 看看这个！ [item]

*发送后（假设您手持一把附魔钻石剑）：*
> 看看这个！ [钻石剑]
>
> *悬停在“[钻石剑]”上会显示：*
>
> ```
> 钻石剑
> 锋利 IV
> 耐久 III
> 等等。
> ```

### 注意事项

- 此模组设计为**纯服务端**运行——客户端无需安装。

<p align="right">(<a href="#top">回到顶部</a>)</p>

## 开发路线

- [x] 支持副手物品显示（例如 `[offhand]` 占位符）
- [ ] 添加配置选项以自定义显示格式（例如是否显示堆叠数量）
- [x] 支持更多消息类型（例如 `/tell` 私人消息）
- [ ] 本地化支持（多语言）

查看 [open issues](https://github.com/PhantomPixel-0418/ShowMyItem/issues) 以获取完整的功能请求和已知问题列表。

<p align="right">(<a href="#top">回到顶部</a>)</p>

## 贡献指南

贡献是使开源社区成为学习、启发和创造的绝佳场所的原因。**您的任何贡献都将受到高度赞赏**。

如果您有能让此项目更好的建议，请 fork 仓库并创建一个 pull request。您也可以只是开一个带有 "enhancement" 标签的 Issue。别忘了给项目点个星！再次感谢！

1. Fork 本项目
2. 创建您的功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交您的更改 (`git commit -m '添加了一些很棒的功能'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开一个 Pull Request

<p align="right">(<a href="#top">回到顶部</a>)</p>

## 构建

- 运行 `./gradlew build` 构建模组。
- 重新混淆的 jar 位于 `build/libs/`。
- 开发版 jar 放在 `build/devlibs/`（不用于分发）。

## 许可证

基于 MIT 许可证分发。更多信息请查看 [LICENSE](/LICENSE)。

<p align="right">(<a href="#top">回到顶部</a>)</p>

## 联系方式

项目链接：[https://github.com/PhantomPixel-0418/ShowMyItem](https://github.com/PhantomPixel-0418/ShowMyItem)

<p align="right">(<a href="#top">回到顶部</a>)</p>

## 致谢

本 README 模板灵感来自：

- [Best-README-Template](https://github.com/othneildrew/Best-README-Template)
- [Fabric Wiki](https://fabricmc.net/wiki/start)

<p align="right">(<a href="#top">回到顶部</a>)</p>