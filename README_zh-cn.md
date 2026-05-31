<!-- Project badges (replace links and images as needed) -->
<div align="center">
  <a href="https://github.com/PhantomPixel-0418/ShowMyItem">
    <img src="https://github.com/PhantomPixel-0418/ShowMyItem/blob/master/src/main/assets/showmyitem/icon.png?raw=true" alt="Logo" width="80" height="80">
  </a>

<h3 align="center">Show My Item</h3>

  <p align="center">
    优雅地在聊天中展示您手中的物品、背包和末影箱——让分享变得更简单！
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
    <li><a href="#configuration">配置</a></li>
    <li><a href="#roadmap">开发路线</a></li>
    <li><a href="#contributing">贡献指南</a></li>
    <li><a href="#license">许可证</a></li>
    <li><a href="#contact">联系方式</a></li>
    <li><a href="#acknowledgments">致谢</a></li>
  </ol>
</details>

## 关于本项目

![Show My Item 截图](https://github.com/PhantomPixel-0418/ShowMyItem/blob/master/images/screenshot.png?raw=true)

**Show My Item** 是一个轻量级的 Minecraft Fabric 模组，专为服务器设计。它允许玩家在聊天中通过简单的占位符（如 `[物品]`、`[背包]`、`[末影箱]`）分享自己手持的物品、整个背包甚至末影箱内容，其他玩家只需悬停或点击即可查看详情。

**它解决了什么问题？**

- 不再需要手动输入复杂的物品名称，也不用让朋友们去猜您拿的是什么。
- 一键分享背包或末影箱，无需繁琐的命令。
- 悬停/点击即可查看——让交流更加直观和高效。
- 纯粹用于分享和炫耀，没有任何作弊或破坏游戏平衡的元素。

**为什么选择它？**

- **简单**：使用自然语言占位符，支持中文和英文。
- **强大**：支持分享主手、副手、背包（含盔甲和副手）、末影箱。
- **可配置**：拥有类 Carpet 的聊天菜单，可随时调整过期时间、最大快照数、语言。
- **轻量**：纯服务端模组，客户端无需安装。
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
- **Fabric API**（服务端需要安装，客户端不需要，但安装了也不影响）。

### 安装

1. **下载模组**  
   从 [Modrinth](https://modrinth.com/mod/showmyitem) 或 [GitHub Releases](https://github.com/PhantomPixel-0418/ShowMyItem/releases) 下载最新的 JAR 文件。

2. **放置模组**  
   将 JAR 文件放入服务器的 `mods` 文件夹中。  
   *如果是单人游戏，也放入客户端的 `mods` 文件夹（它会在内置服务器中自动生效）。*

3. **启动游戏/服务器**  
   无需额外配置——模组将自动运行，配置文件会生成在 `config/showmyitem.json`。

<p align="right">(<a href="#top">回到顶部</a>)</p>

## 使用方法

### 占位符一览

模组支持以下占位符，您可以直接在聊天中使用（这些是中文版的占位符，英文版请参考英文文档）：

| 占位符     | 描述                                                     |
| ---------- | -------------------------------------------------------- |
| `[物品]`   | 主手物品                                                 |
| `[副手]`   | 副手物品                                                 |
| `[背包]`   | 分享您的整个背包（包括盔甲和副手），生成一个可点击的链接 |
| `[末影箱]` | 分享您的末影箱内容，生成一个可点击的链接                 |

>[!NOTE]
> 注意：占位符与服务器语言无关，您可以在任何语言环境下使用这些中文占位符。如果您更习惯英文，可以使用对应的英文占位符 `[item]`、`[offhand]`、`[inventory]`、`[enderchest]`。

### 基本用法示例

1. 确保您手上拿着任意物品。
2. 在聊天中发送包含占位符的消息，例如：
    ```
    看看这个！ [物品]
    我的背包： [背包]
    我的末影箱：[末影箱]
    ```
3. 发送后，占位符会被替换为物品名称或带链接的文字。
4. 其他玩家可以悬停在物品名上查看物品详情，或点击 `[玩家 的背包]` / `[玩家 的末影箱]` 直接打开一个只读界面。

### 交互细节

- **悬停**：鼠标悬停在物品名上会显示完整的物品提示（附魔、耐久、自定义名称等）。
- **点击背包/末影箱链接**：会打开一个安全的只读容器，显示对方的主物品栏、盔甲、副手或末影箱。容器中的物品无法被移动或取出，杜绝刷物品漏洞。
- **权限控制**：只有快照的创建者或 OP 可以打开链接，其他人点击会收到无权提示。

<p align="right">(<a href="#top">回到顶部</a>)</p>

## 配置

模组提供了一个类似 Carpet Mod 的聊天菜单，可通过命令 `/showmyitem` 打开。

### 命令列表

| 命令                             | 描述                     | 权限          |
| -------------------------------- | ------------------------ | ------------- |
| `/showmyitem`                    | 显示所有设置项和版本信息 | 所有人        |
| `/showmyitem category <类别>`    | 显示指定类别的设置       | 所有人        |
| `/showmyitem find <搜索词>`      | 模糊搜索设置项           | 所有人        |
| `/showmyitem viewinv <快照ID>`   | 查看背包快照             | 仅创建者或 OP |
| `/showmyitem viewender <快照ID>` | 查看末影箱快照           | 仅创建者或 OP |
| `/showmyitem set <键> <值>`      | 修改配置项               | OP (2级)      |
| `/showmyitem reloadconfig`       | 重新加载配置文件         | OP (2级)      |

### 配置项说明

配置文件位于 `config/showmyitem.json`，您可以直接编辑或在游戏内通过菜单/命令修改：

- `snapshotExpiryMs`：快照的过期时间，单位毫秒（默认 300000，即 5 分钟）。
- `maxSnapshots`：同时存储的最大快照数量（默认 100）。
- `defaultLanguage`：服务器默认语言，可选 `en_us` 或 `zh_cn`。

修改后可通过 `/showmyitem reloadconfig` 热重载，无需重启服务器。

<p align="right">(<a href="#top">回到顶部</a>)</p>

## 开发路线

- [x] 支持主手物品显示 (`[item]`)
- [x] 支持副手物品显示 (`[offhand]`)
- [x] 支持背包分享 (`[背包]` / `[inventory]`)，包含盔甲和副手
- [x] 支持末影箱分享 (`[末影箱]` / `[enderchest]`)
- [x] 纯服务端架构，客户端无需安装
- [x] 多语言支持（中文/英文），占位符自动识别
- [x] 类 Carpet 的配置菜单，可点击修改
- [x] 配置文件热重载
- [x] 安全只读容器，防止刷物品
- [ ] 分享生命值、饥饿度等更多信息
- [ ] 分享准星指向的方块/实体
- [ ] 更多语言支持

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
