# PrefixMod

A server-side **Fabric** mod for Minecraft 1.21.1 that provides:

- **Per-player configurable prefixes** stored in `config/prefixmod/prefixes.json`
- **TAB header/footer** with server title, subtitle, ping, online count, and session time
- **Chat formatting** with the prefix prepended to every message
- All settings controlled via in-game `/prefix` commands

---

## Requirements

| Component | Version |
|---|---|
| Minecraft (Java Edition) | 1.21.1 |
| Fabric Loader | ≥ 0.16.9 |
| Fabric API | ≥ 0.110.0+1.21.1 |
| Java | 21 |

> **Note:** If your server uses a different Minecraft patch (e.g. a future 1.21.x release),
> update `minecraft_version`, `yarn_mappings`, `loader_version`, and `fabric_version` in
> `gradle.properties` to match.

---

## Building

```bash
# Clone the repo
git clone https://github.com/A114193/prefixmod.git
cd prefixmod

# Build (produces build/libs/prefixmod-1.0.0.jar)
./gradlew build
```

The compiled jar will be at `build/libs/prefixmod-1.0.0.jar`
(the `-sources` jar next to it is not needed on the server).

---

## Installation

1. Copy `build/libs/prefixmod-1.0.0.jar` into your server's `mods/` folder.
2. Make sure **Fabric API** is also present in `mods/`.
3. Start the server.  
   Config files are auto-generated on first run:
   - `config/prefixmod/config.json` – TAB / chat settings
   - `config/prefixmod/prefixes.json` – nick → prefix mapping

---

## Configuration files

### `config/prefixmod/config.json`

```json
{
  "tabEnabled": true,
  "tabTitle": "&6&lMy Server",
  "tabSubtitle": "&7Welcome to the server!",
  "tabFooterFormat": "&7Ping: &a{ping}ms &r| &7Online: &a{online}&7/&a{max} &r| &7Session: &a{session}",
  "chatEnabled": true,
  "chatFormat": "{prefix}&f{name}&r: {msg}"
}
```

#### TAB footer placeholders

| Placeholder | Description |
|---|---|
| `{ping}` | Player's latency in milliseconds |
| `{online}` | Current player count |
| `{max}` | Max player count |
| `{session}` | Time since player joined (HH:MM:SS) |

#### Chat format placeholders

| Placeholder | Description |
|---|---|
| `{prefix}` | Player's prefix (empty if none) |
| `{name}` | Player's username |
| `{msg}` | The chat message |

### Colour codes

Use `&` codes anywhere in config values or command arguments:

```
&0 black   &1 dark blue  &2 dark green  &3 dark aqua
&4 dark red &5 dark purple &6 gold      &7 gray
&8 dark gray &9 blue      &a green      &b aqua
&c red      &d light purple &e yellow   &f white
&k obfuscated  &l bold  &m strikethrough  &n underline  &o italic  &r reset
```

---

## Commands

All commands require **operator level 3** (`/op`).

| Command | Description |
|---|---|
| `/prefix set <nick> <prefix…>` | Set a prefix for a player (supports `&` colour codes) |
| `/prefix clear <nick>` | Remove a player's prefix |
| `/prefix get <nick>` | Show the current prefix for a player |
| `/prefix list` | List all configured prefixes |
| `/prefix reload` | Reload both config files from disk and re-apply teams |
| `/prefix tab enable <true\|false>` | Toggle TAB header/footer |
| `/prefix tab title <text…>` | Change the TAB title line |
| `/prefix tab subtitle <text…>` | Change the TAB subtitle line |
| `/prefix chat enable <true\|false>` | Toggle chat formatting |

### Examples

```
/prefix set Notch &c[ADMIN]
/prefix set Steve &a[VIP]
/prefix clear Notch
/prefix tab title &6&lAwesome Server
/prefix tab subtitle &7Play fair, have fun!
/prefix reload
```

---

## How it works

### Prefixes in TAB

Each unique prefix string maps to a **scoreboard team** whose name starts with `pm_`.  
The team's *prefix* property is set to `<coloured-prefix> `, which Minecraft displays
before the player's name in the TAB list.  Teams not managed by this mod (no `pm_` prefix)
are never modified.

### TAB header / footer

A `PlayerListHeaderS2CPacket` is sent to every player once per second.  The header
contains two lines (title + subtitle); the footer shows per-player stats resolved from
the configured placeholders.

### Chat formatting

`ServerMessageEvents.ALLOW_CHAT_MESSAGE` cancels the original signed message and
broadcasts a custom formatted text to all online players.  The server console also
receives an `&`-coded echo.

---

## License

MIT – see [LICENSE](LICENSE).
