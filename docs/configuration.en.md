Configuration

## General

| name       | description              | type   | default    |
|------------|--------------------------|--------|------------|
| `language` | Plugin locale            | string | `en`       |
| `database` | Sqlite database filename | string | `database` |

## Color

`color.*`

| name   | description          | type  | default   |
|--------|----------------------|-------|-----------|
| first  | First default color  | color | `#1abaf0` |
| second | Second default color | color | `#77d7f7` |

## Tab

`tab`

- `tab.header-message`

| name   | description              | type | default |
|--------|--------------------------|------|---------|
| enable | Enable custom tab header | bool | `true`  |

- `tab.footer-message`

| name   | description              | type | default |
|--------|--------------------------|------|---------|
| enable | Enable custom tab footer | bool | `true`  |

- `tab.update`

| name   | description                  | type | default |
|--------|------------------------------|------|---------|
| enable | Enable tab updates           | bool | `true`  |
| rate   | Tab update rate (in seconds) | int  | `2`     |

- `tab.player-ping`

| name   | description                | type | default |
| ------ | -------------------------- | ---- | ------- |
| enable | Enable ping display in tab | bool | `true`  |

## Server

`server.*`

- `server.motd`
    * `server.motd.messages`

| name     | description        | type | default |
|----------|--------------------|------|---------|
| `enable` | Enable custom motd | bool | `true`  |

- `server.online`
    * `server.online.count`

| name     | description                                    | type | default |
|----------|------------------------------------------------|------|---------|
| `enable` | Enable custom max online players (only visual) | bool | `true`  |
| `digit`  | Max players count                              | int  | `69`    |

- `server.icon`

| name     | description          | type                  | default                          |
| -------- | -------------------- | --------------------- | -------------------------------- |
| `enable` | Enable custom icon   | bool                  | `true`                           |
| `mode`   | Icon displaying mode | string{random/single} | `random`                         |
| `names`  | Icon file names      | stirng[]              | `[server-icon-1, server-icon-2]` |

## Chat

`chat.*`

### `chat.local`

| name            | description                         | type | default |
|-----------------|-------------------------------------|------|---------|
| `range`         | Local chat range                    | int  | `100`   |
| `set-cancelled` | Cancel chat event for other plugins | bool | `true`  |

- `chat.local.no-recipients`

| name     | description                                     | type | default |
|----------|-------------------------------------------------|------|---------|
| `enable` | Enable no-recipients notification in local chat | bool | `true`  |

- `chat.local.admin-see`

| name         | description                         | type   | default                            |
|--------------|-------------------------------------|--------|------------------------------------|
| `enable`     | Enable local chat spying for admins | bool   | `false`                            |
| `permission` | Permission for local chat spying    | string | `flectonechat.localchat.admin_see` |

### `chat.global`

| name            | description                         | type | default |
|-----------------|-------------------------------------|------|---------|
| `enable`        | Enable global chat                  | bool | `true`  |
| `set-cancelled` | Cancel chat event for other plugins | bool | `true`  |

- `chat.global.prefix`

| name      | description                                                      | type | default |
|-----------|------------------------------------------------------------------|------|---------|
| `cleared` | Remove global prefix for other plugins support (DiscordSRV etc.) | bool | `true`  |

### `chat.patterns`

check [Chat features](features/chat.md#patterns) to learn about patterns

## Player

`player.*`

| name           | description                          | type   | default                                                                         |
| -------------- | ------------------------------------ | ------ | ------------------------------------------------------------------------------- |
| `display-name` | Players' displayname format          | string | `<world_prefix><vault_prefix><stream_prefix><player><afk_suffix><vault_suffix>` |
| `tab-name`     | Players' tab name format             | string | `<world_prefix><vault_prefix><stream_prefix><player><afk_suffix><vault_suffix>` |
| `name-visible` | Show displayname above player's head | `bool` | `false`                                                                         |

### `player.world`

| name   | description                                                                              | type | default |
|--------|------------------------------------------------------------------------------------------|------|---------|
| `mode` | World detecting mode. [More info here](https://chat.flectone.net/features/worldprefixes) | bool | `true`  |

- `player.world.prefix`

| name     | description                             | type | default |
|----------|-----------------------------------------|------|---------|
| `enable` | Enable world type prefix in displayname | bool | `true`  |

### `player.name-tag`

| name     | description                                | type   | default |
| -------- | ------------------------------------------ | ------ | ------- |
| `enable` | Show suffix and prefix above player's head | `bool` | `true`  |

### `player.item`

- `player.item.sign`

| name     | description         | type | default |
|----------|---------------------|------|---------|
| `enable` | Enable item signing | bool | `true`  |

### `player.join`

- `player.join.message`

| name     | description         | type | default |
|----------|---------------------|------|---------|
| `enable` | Enable join message | bool | `true`  |

### `player.quit`

- `player.quit.message`

| name     | description         | type | default |
|----------|---------------------|------|---------|
| `enable` | Enable quit message | bool | `true`  |

## Command

### `command.technical-works`

| name         | description                         | type   | default                        |
|--------------|-------------------------------------|--------|--------------------------------|
| `enable`     | Enable technical works              | bool   | `false`                        |
| `permission` | Technical works immunity permission | string | `flectonechat.technical-works` |

### `command.stream`

| name     | description                        | type | default |
|----------|------------------------------------|------|---------|
| `global` | Send stream message to global chat | bool | `true`  |

- `command.stream.offline-prefix`

| name     | description                      | type | default |
| -------- | -------------------------------- | ---- | ------- |
| `enable` | Enable streamer's offline prefix | bool | `true`  |

### `command.afk`

- `command.afk.timeout`

| name     | description                                             | type   | default |
|----------|---------------------------------------------------------|--------|---------|
| `enable` | Automatically turn on AFK mode after inactivity         | bool   | `true`  |
| `time`   | Time after which the AFK mode is activated (in seconds) | number | `3000`  |

### `command.ping`

- `command.ping.bad`

| name    | description        | type   | default |
|---------|--------------------|--------|---------|
| `color` | Bad ping color     | bool   | `true`  |
| `count` | Bad ping threshold | number | `200`   |

- `command.ping.medium`

| name    | description           | type   | default |
|---------|-----------------------|--------|---------|
| `color` | Medium ping color     | bool   | `true`  |
| `count` | Medium ping threshold | number | `200`   |

- `command.ping.good`

| name    | description     | type | default |
|---------|-----------------|------|---------|
| `color` | Good ping color | bool | `true`  |

### `command.mark`

| name     | description         | type | default |
|----------|---------------------|------|---------|
| `enable` | Enable mark feature | bool | `true`  |

### `command.me`

| name     | description                    | type | default |
|----------|--------------------------------|------|---------|
| `global` | Send me message to global chat | bool | `true`  |

### `command.try`

| name     | description                     | type | default |
|----------|---------------------------------|------|---------|
| `global` | Send try message to global chat | bool | `true`  |

### `command.try-cube`

| name         | description                          | type   | default |
|--------------|--------------------------------------|--------|---------|
| `global`     | Send try-cube message to global chat | bool   | `true`  |
| `max-amount` | Max amount of dices                  | number | `9`     |

### `command.ball`

| name     | description                      | type | default |
|----------|----------------------------------|------|---------|
| `global` | Send ball message to global chat | bool | `true`  |

### `command.helper`

- `command.helper.see.permission`

| name     | description           | type   | default                  |
|----------|-----------------------|--------|--------------------------|
| `global` | Permission for seeing | string | `flectonechat.helpersee` |

### `command.poll`

| name         | description                  | type   | default                   |
|--------------|------------------------------|--------|---------------------------|
| `time`       | Poll duration (in seconds)   | number | `50`                      |
| `permission` | Permission for poll creating | string | `flectonechat.pollcreate` |

### `command.mute`

| name       | description                         | type | default |
|------------|-------------------------------------|------|---------|
| `announce` | Announce player mute to all players | bool | `true`  |

### `command.tempban`

| name       | description                          | type | default |
|------------|--------------------------------------|------|---------|
| `announce` | Announce a player ban to all players | bool | `true`  |

## Cooldown

`cool-down.*`

| name     | description            | type | default |
|----------|------------------------|------|---------|
| `enable` | Enable global cooldown | bool | `false` |

Each command can have a custom cooldown.  
You can customize them as follows

```yaml
  command-name:
    enable: false
    time: 5 # time in seconds
    permission: "flectonechat.command-name.cooldown.immune" # immunity permission
```

## Sound

`sound.*`

Each command can have a custom sound.  
You can customize them as follows

```yaml
  command-name:
    enable: true
    type: "BLOCK_NOTE_BLOCK_BELL" # Sound name here 
```

You can get all sound names [here](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Sound.html)
