# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Show My Item is a server-side Fabric mod for Minecraft 1.21.7 that allows players to share their held item, inventory, or ender chest in chat using placeholders like `[item]`, `[inventory]`, or `[enderchest]`. Other players can hover to see tooltips or click links to view read-only inventory snapshots. The mod requires no client-side installation.

## Build & Run

```bash
# Build the mod (remapped JAR goes to build/libs/)
./gradlew build

# Clean build
./gradlew clean build
```

The Gradle properties are in `gradle.properties`: Minecraft 1.21.7, Fabric Loader 0.16.14, Fabric API 0.129.0, Java 21. The entrypoint is `com.PhantomPixel0418.showmyitem.Showmyitem`.

## Architecture

### Core Flow

1. **`Showmyitem.java`** — Main mod entrypoint. Registers commands, tick-based snapshot cleanup, and the `ServerMessageDecoratorEvent` handler that intercepts chat messages and replaces placeholders with item components or clickable inventory/ender chest links.
2. **`I18n.java`** — Dynamic locale loading from `assets/showmyitem/lang/*.json`. Builds placeholder regex from all supported languages so `[物品]`, `[item]`, etc. all work interchangeably.
3. **`InventorySnapshotManager.java`** — Thread-safe LRU cache of `InventorySnapshot` objects (ConcurrentHashMap + creation-order queue). Auto-evicts oldest when exceeding `maxSnapshots`. Periodic cleanup via `cleanExpired()`.
4. **`ModConfig.java`** — Singleton config loaded from `config/showmyitem.json`. Supports hot-reload via `/showmyitem reloadconfig`. Strips JSON comments before parsing.

### Commands

All commands are under `/showmyitem`:
- **`ViewInventoryCommand.java`** — Handles `viewinv`, `viewender`, `category`, `find`, `set`, `reloadconfig`. Implements the Carpet-style clickable config menu with toggle buttons for language and inline edit suggestions for numeric values.
- **`EnderchestShareCommand.java`** — Handles `/showmyitem enderchest invite|revoke|list|open` for real-time ender chest sharing between players (distinct from snapshot-based sharing).
- **`EnderchestListener.java`** — Listens on right-clicking ender chests to auto-open shared ender chests for invited players.

### UI

- **`CustomInventoryScreenHandler.java`** — Read-only screen handler. Container slots use `ReadOnlySlot` (blocks inserts/removals). Player inventory slots remain interactive. Overrides `onSlotClick` to block swap keys and restore client state on blocked actions. Uses `EmptyInventory` singleton for padding slots.
- **`InventoryUtils.java`** — Helpers: `copyEnderChest()` for deep-copying ender chest items, `openCustomInventoryScreen()` to launch the read-only screen via `SimpleNamedScreenHandlerFactory`.

### Security

- Snapshots are read-only; `quickMove()` returns `EMPTY` and `ReadOnlySlot` blocks all item transfers.
- Only the snapshot creator or OPs (permission level 2) can open snapshot links.
- Endere chest sharing uses persistent `enderchest_shared.json` with atomic writes and backup-restore logic.

## Config

`config/showmyitem.json` (comments supported):
- `snapshotExpiryMs` — Snapshot TTL in ms (default 300000 = 5 min)
- `maxSnapshots` — Max cached snapshots (default 100)
- `defaultLanguage` — `"en_us"` or `"zh_cn"`

## CI/CD

Three GitHub Actions workflows:
- **`release.yml`** — Triggered by `v*` tags. Generates changelog from conventional commits. Publishes to GitHub Releases and Modrinth.
- **`alpha.yml`** — Manual dispatch. Creates alpha prereleases with commit-hash versions.
- **`beta.yml`** — Manual dispatch. Creates beta prereleases.

All workflows: JDK 21 (Temurin), Gradle cache, `./gradlew clean build`, upload to Modrinth project `pMKJZtiq`.

## Language Files

- `src/main/resources/assets/showmyitem/lang/en_us.json`
- `src/main/resources/assets/showmyitem/lang/zh_cn.json`

Keys follow the pattern `text.showmyitem.*`, `config.*`, `placeholder.*`, `category.*`. Adding a new language file auto-registers its placeholders.

## Key Conventions

- Server-only mod (`"environment": "server"` in `fabric.mod.json`). Clients do not need the mod installed.
- Mixins file exists (`showmyitem.mixins.json`) but is currently empty.
- The old `SimpleNamedScreenFactoryWithInventory` was deleted (shown in git status as D).
- Git commit messages use conventional commit format: `type(scope): description` (e.g., `feat:`, `fix:`, `build:`, `refactor:`).
