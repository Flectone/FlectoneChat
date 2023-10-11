# Configuration

## General

| name       | description              | type   | default          |
| ---------- | ------------------------ | ------ | ---------------- |
| `language` | Plugin locale            | string | `en`             |
| `database` | Sqlite database filename | string | `database`       |
| `version`  | Don't touch that. PLEASE | string | (plugin version) |

## scoreboard

| name     | description              | type | default |
| -------- | ------------------------ | ---- | ------- |
| `custom` | Enable custom scoreboard | bool | `false` |

## Color

| name   | description          | type  | default   |
| ------ | -------------------- | ----- | --------- |
| first  | First default color  | color | `#1abaf0` |
| second | Second default color | color | `#77d7f7` |

## Tab

checkout [tab feature](features/tab.md) to learn how to customize your tab

*   `tab.header-message`

| name   | description              | type | default |
| ------ | ------------------------ | ---- | ------- |
| enable | Enable custom tab header | bool | `true`  |

*   `tab.footer-message`

| name   | description              | type | default |
| ------ | ------------------------ | ---- | ------- |
| enable | Enable custom tab footer | bool | `true`  |

*   `tab.update`

| name   | description                | type   | default |
| ------ | -------------------------- | ------ | ------- |
| enable | Enable tab updates         | bool   | `true`  |
| rate   | Tab update rate (in ticks) | number | `40`    |

*   `tab.player-ping`

| name   | description                | type | default |
| ------ | -------------------------- | ---- | ------- |
| enable | Enable ping display in tab | bool | `true`  |

## Server

checkout [branding feature](features/branding.md) to learn how to customize your server's branding

* `server.brand`

| name   | description         | type | default |
| ------ | ------------------- | ---- | ------- |
| enable | Enable custom brand | bool | `true`  |

* `server.brand.update`

| name   | description                  | type   | default |
| ------ | ---------------------------- | ------ | ------- |
| enable | Enable custom brand updating | bool   | `true`  |
| rate   | Custom brand update rate     | number | `20`    |

* `server.motd.messages`

| name     | description        | type | default |
| -------- | ------------------ | ---- | ------- |
| `enable` | Enable custom motd | bool | `true`  |

*   `server.online`
    *   `server.online.count`

| name     | description                             | type   | default |
| -------- | --------------------------------------- | ------ | ------- |
| `enable` | Enable custom max players (only visual) | bool   | `true`  |
| `digit`  | Max players count                       | number | `69`    |

*   `server.icon`

| name     | description          | type                  | default                          |
| -------- | -------------------- | --------------------- | -------------------------------- |
| `enable` | Enable custom icon   | bool                  | `true`                           |
| `mode`   | Icon displaying mode | string{random/single} | `random`                         |
| `names`  | Icon file names      | stirng\[]             | `[server-icon-1, server-icon-2]` |

## Chat

### `chat.auto-message`

| name     | description                                 | type   | default |
| -------- | ------------------------------------------- | ------ | ------- |
| `enable` | Enable auto-messages                        | bool   | `false` |
| `random` | If `true`, then the messages will be random | bool   | `false` |
| `period` | Delay between auto-messages                 | number | `600`   |

### `chat.local`

| name            | description                         | type   | default |
| --------------- | ----------------------------------- | ------ | ------- |
| `enable`        | Enable local chat                   | bool   | `true`  |
| `range`         | Local chat range                    | number | `100`   |
| `set-cancelled` | Cancel chat event for other plugins | bool   | `true`  |

*   `chat.local.no-recipients`

| name     | description                                     | type | default |
| -------- | ----------------------------------------------- | ---- | ------- |
| `enable` | Enable no-recipients notification in local chat | bool | `true`  |

### `chat.swear-protection`

| name     | description             | type | default |
| -------- | ----------------------- | ---- | ------- |
| `enable` | Enable swear protection | bool | `false` |

### `chat.global`

| name            | description                         | type | default |
| --------------- | ----------------------------------- | ---- | ------- |
| `enable`        | Enable global chat                  | bool | `true`  |
| `set-cancelled` | Cancel chat event for other plugins | bool | `true`  |

*   `chat.global.prefix`

| name      | description                                                      | type | default |
| --------- | ---------------------------------------------------------------- | ---- | ------- |
| `cleared` | Remove global prefix for other plugins support (DiscordSRV etc.) | bool | `true`  |

### `chat.bubble`

| name           | description                                                             | type   | default |
| -------------- | ----------------------------------------------------------------------- | ------ | ------- |
| `enable`       | Enable chat bubbles                                                     | bool   | `true`  |
| `max-per-line` | Maximum number of characters in a line. overflow is moved to a new line | number | `35`    |
| `read-speed`   | Read speed. affect the duration of bubbles                              | number | `800`   |

duration formula (ticks)
```java
duration = (messageLength + 8 * BubblesCount) * 1200 / readSpeed;
```

### `chat.patterns`

checkout [Chat features](features/chat.md#patterns) to learn about patterns

### `chat.anvil-formatting`

| name     | description                 | type | default |
| -------- | --------------------------- | ---- | ------- |
| `enable` | Enable formatting in anvils | bool | `true`  |

### `chat.book-formatting`

| name     | description                | type | default |
| -------- | -------------------------- | ---- | ------- |
| `enable` | Enable formatting in books | bool | `true`  |

### `chat.sign-formatting`

| name     | description                | type | default |
| -------- | -------------------------- | ---- | ------- |
| `enable` | Enable formatting on sings | bool | `true`  |

### `chat.cords`

| name     | description          | type | default |
| -------- | -------------------- | ---- | ------- |
| `enable` | Enable cords feature | bool | `true`  |

### `chat.stats`

| name     | description          | type | default |
| -------- | -------------------- | ---- | ------- |
| `enable` | Enable stats feature | bool | `true`  |

### `chat.url`

| name     | description        | type | default |
| -------- | ------------------ | ---- | ------- |
| `enable` | Enable url feature | bool | `true`  |

### `chat.tooltip`

| name     | description            | type | default |
| -------- | ---------------------- | ---- | ------- |
| `enable` | Enable tooltip feature | bool | `true`  |

### `chat.ping`

| name     | description         | type | default |
| -------- | ------------------- | ---- | ------- |
| `enable` | Enable ping feature | bool | `true`  |

### `chat.markdown-||`

| name     | description         | type | default |
| -------- | ------------------- | ---- | ------- |
| `enable` | Enable hide feature | bool | `true`  |

### `chat.markdown-**`

| name     | description                  | type | default |
| -------- | ---------------------------- | ---- | ------- |
| `enable` | Enable bold markdown feature | bool | `true`  |

### `chat.markdown-##`

| name     | description                    | type | default |
| -------- | ------------------------------ | ---- | ------- |
| `enable` | Enable italic markdown feature | bool | `true`  |

### `chat.markdown-__`

| name     | description                       | type | default |
| -------- | --------------------------------- | ---- | ------- |
| `enable` | Enable underline markdown feature | bool | `true`  |

### `chat.markdown-~~`

| name     | description                           | type | default |
| -------- | ------------------------------------- | ---- | ------- |
| `enable` | Enable strikethrough markdown feature | bool | `true`  |

### `chat.markdown-??`

| name     | description                       | type | default |
| -------- | --------------------------------- | ---- | ------- |
| `enable` | Enable obfuscate markdown feature | bool | `true`  |

## Death

### `death.message`

| name          | description                                                                                                                                    | type | default |
| ------------- | ---------------------------------------------------------------------------------------------------------------------------------------------- | ---- | ------- |
| `enable`      | Enable custom death messages                                                                                                                   | bool | `true`  |
| `visible`     | if disabled, custom death messages will not be shown                                                                                           | bool | `true`  |
| `mob-default` | if enabled, then death from any mob will be located on the "death.mob.default", otherwise it will be necessary to register for each separately | bool | `true`  |
<!-- 184 chars in a line lets gooooooo -->

## Advancement

!!! note Custom advancement messages
    You can customize your advancement announcements with `advancement.*` in your lang file

### `advancement.message`

| name     | description                         | type | default |
| -------- | ----------------------------------- | ---- | ------- |
| `enable` | Enable custom advancements messages | bool | `true`  |

### `advancement.message.task`

| name      | description                         | type | default |
| --------- | ----------------------------------- | ---- | ------- |
| `visible` | Display "task" advancements in chat | bool | `true`  |

### `advancement.message.goal`

| name      | description                         | type | default |
| --------- | ----------------------------------- | ---- | ------- |
| `visible` | Display "task" advancements in chat | bool | `true`  |

### `advancement.message.challenge`

| name      | description                              | type | default |
| --------- | ---------------------------------------- | ---- | ------- |
| `visible` | Display "challenge" advancements in chat | bool | `true`  |

## Player

### `player.*`

| name           | description                          | type   | default |
| -------------- | ------------------------------------ | ------ | ------- |
| `name-visible` | Show displayname above player's head | `bool` | `false` |

### `player.name`

??? tip "Per-group name styles" 
    You can set custom displaynames for specific vault groups.
    To do this, add a style to `player.name.GROUP_NAME`
    ```yaml
    player:
      name:
        admin: "&c<player>"
        default: "<GRADIENT:2C08BA><player></GRADIENT:028A97>"
    ```

| name           | description                          | type   | default                                                                         |
| -------------- | ------------------------------------ | ------ | ------------------------------------------------------------------------------- |
| `display`      | Players' displayname format          | string | `<world_prefix><vault_prefix><stream_prefix><player><afk_suffix><vault_suffix>` |
| `tab`          | Players' tab name format             | string | `<world_prefix><vault_prefix><stream_prefix><name><afk_suffix><vault_suffix>`   |

### `player.world`

| name   | description                                                       | type | default |
| ------ | ----------------------------------------------------------------- | ---- | ------- |
| `mode` | World detecting mode. [More info here](features/worldprefixes.md) | bool | `true`  |

*   `player.world.prefix`

| name     | description                             | type | default |
| -------- | --------------------------------------- | ---- | ------- |
| `enable` | Enable world type prefix in displayname | bool | `true`  |

### `player.name-tag`

| name     | description                                | type   | default |
| -------- | ------------------------------------------ | ------ | ------- |
| `enable` | Show suffix and prefix above player's head | `bool` | `true`  |

### `player.door-knocking`

| name     | description          | type   | default |
| -------- | -------------------- | ------ | ------- |
| `enable` | Enable door-knocking | `bool` | `true`  |

### `player.glass-knocking`

| name     | description           | type   | default |
| -------- | --------------------- | ------ | ------- |
| `enable` | Enable glass-knocking | `bool` | `true`  |

### `player.item`

*   `player.item.sign`

| name     | description                                    | type | default |
| -------- | ---------------------------------------------- | ---- | ------- |
| `enable` | Enable [item signing](features/itemsigning.md) | bool | `true`  |

### `player.join`

*   `player.join.message`

| name     | description         | type | default |
| -------- | ------------------- | ---- | ------- |
| `enable` | Enable join message | bool | `true`  |

### `player.quit`

*   `player.quit.message`

| name     | description         | type | default |
| -------- | ------------------- | ---- | ------- |
| `enable` | Enable quit message | bool | `true`  |

## Command

### `command.maintenance`

| name         | description                     | type   | default                    |
| ------------ | ------------------------------- | ------ | -------------------------- |
| `turn-on`    | Enable maintenance              | bool   | `false`                    |
| `permission` | Maintenance immunity permission | string | `flectonechat.maintenance` |

### `command.stream`

| name     | description                        | type | default |
| -------- | ---------------------------------- | ---- | ------- |
| `global` | Send stream message to global chat | bool | `true`  |

*   `command.stream.offline-prefix`

| name     | description                      | type | default |
| -------- | -------------------------------- | ---- | ------- |
| `enable` | Enable streamer's offline prefix | bool | `true`  |

### `command.afk`

*   `command.afk.timeout`

| name     | description                                             | type   | default |
| -------- | ------------------------------------------------------- | ------ | ------- |
| `enable` | Automatically turn on AFK mode after inactivity         | bool   | `true`  |
| `time`   | Time after which the AFK mode is activated (in seconds) | number | `3000`  |

### `command.warn`

checkout [warn system feature](features/warns.md) to learn about warns

| name                | description                                                                           | type                   | default   |
| ------------------- | ------------------------------------------------------------------------------------- | ---------------------- | --------- |
| `announce`          | Enable warn announcing                                                                | bool                   | `true`    |
| `tab-complete-mode` | Offline: show all players in tab-complete. \ Online: show only players who are online | string{offline/online} | `offline` |

### `command.firstonline`

| name                | description                                                                           | type                   | default   |
| ------------------- | ------------------------------------------------------------------------------------- | ---------------------- | --------- |
| `tab-complete-mode` | Offline: show all players in tab-complete. \ Online: show only players who are online | string{offline/online} | `offline` |

### `command.ignore`

| name                | description                                                                           | type                   | default   |
| ------------------- | ------------------------------------------------------------------------------------- | ---------------------- | --------- |
| `tab-complete-mode` | Offline: show all players in tab-complete. \ Online: show only players who are online | string{offline/online} | `offline` |

### `command.msg`

| name                | description                                                                           | type                   | default   |
| ------------------- | ------------------------------------------------------------------------------------- | ---------------------- | --------- |
| `tab-complete-mode` | Offline: show all players in tab-complete. \ Online: show only players who are online | string{offline/online} | `offline` |

### `command.ping`

*   `command.ping.bad`

| name    | description        | type   | default |
| ------- | ------------------ | ------ | ------- |
| `color` | Bad ping color     | bool   | `true`  |
| `count` | Bad ping threshold | number | `200`   |

*   `command.ping.medium`

| name    | description           | type   | default |
| ------- | --------------------- | ------ | ------- |
| `color` | Medium ping color     | bool   | `true`  |
| `count` | Medium ping threshold | number | `200`   |

*   `command.ping.good`

| name    | description     | type | default |
| ------- | --------------- | ---- | ------- |
| `color` | Good ping color | bool | `true`  |

### `command.mark`

check [mark feature](features/mark.md) for more info

| name     | description           | type   | default        |
| -------- | --------------------- | ------ | -------------- |
| `enable` | Enable mark feature   | bool   | `true`         |
| `item`   | Marking item          | string | `WOODEN_SWORD` |
| `range`  | Mark raycasting range | bool   | `30`           |

### `command.me`

| name     | description                    | type | default |
| -------- | ------------------------------ | ---- | ------- |
| `global` | Send me message to global chat | bool | `true`  |

### `command.try`

| name     | description                     | type | default |
| -------- | ------------------------------- | ---- | ------- |
| `global` | Send try message to global chat | bool | `true`  |

### `command.try-cube`

| name         | description                          | type   | default |
| ------------ | ------------------------------------ | ------ | ------- |
| `global`     | Send try-cube message to global chat | bool   | `true`  |
| `max-amount` | Max amount of dices                  | number | `9`     |

### `command.ball`

| name     | description                      | type | default |
| -------- | -------------------------------- | ---- | ------- |
| `global` | Send ball message to global chat | bool | `true`  |

### `command.helper`

*   `command.helper.see.permission`

| name     | description           | type   | default                  |
| -------- | --------------------- | ------ | ------------------------ |
| `global` | Permission for seeing | string | `flectonechat.helpersee` |

### `command.poll`

| name         | description                  | type   | default                   |
| ------------ | ---------------------------- | ------ | ------------------------- |
| `time`       | Poll duration (in seconds)   | number | `50`                      |
| `permission` | Permission for poll creating | string | `flectonechat.pollcreate` |

### `command.mute`

| name                | description                                                                           | type                   | default   |
| ------------------- | ------------------------------------------------------------------------------------- | ---------------------- | --------- |
| `announce`          | Announce player mute to all players                                                   | bool                   | `true`    |
| `tab-complete-mode` | Offline: show all players in tab-complete. \ Online: show only players who are online | string{offline/online} | `offline` |

### `command.mutelist`

| name       | description                | type   | default |
| ---------- | -------------------------- | ------ | ------- |
| `per-page` | Number of entries per page | number | 4       |

### `command.tempban`

| name                | description                                                                           | type                   | default   |
| ------------------- | ------------------------------------------------------------------------------------- | ---------------------- | --------- |
| `announce`          | Announce a player ban to all players                                                  | bool                   | `true`    |
| `tab-complete-mode` | Offline: show all players in tab-complete. \ Online: show only players who are online | string{offline/online} | `offline` |

### `command.kick`

| name       | description                           | type | default |
| ---------- | ------------------------------------- | ---- | ------- |
| `announce` | Announce a player kick to all players | bool | `true`  |

### `command.spit`

| name   | description      | type   | default     |
| ------ | ---------------- | ------ | ----------- |
| `item` | Fast-action item | string | `WHITE_DYE` |

## Cooldown

`cool-down.*`

| name     | description            | type | default |
| -------- | ---------------------- | ---- | ------- |
| `enable` | Enable global cooldown | bool | `false` |

Each command can have a custom cooldown.\
You can customize them as follows

```yaml
  command-name:
    enable: false
    time: 5 # time in seconds
    permission: "flectonechat.command-name.cooldown.immune" # immunity permission
```

## Sound

`sound.*`

Each command can have a custom sound.\
You can customize them as follows

```yaml
  command-name:
    enable: true
    type: "BLOCK_NOTE_BLOCK_BELL:1:1" 
```

### Sound type format
`SOUND_NAME`:`VOLUME`:`PITCH`

You can get all sound names [here](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Sound.html)
