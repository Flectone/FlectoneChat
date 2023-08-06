<p align="center">
 <img src="https://i.imgur.com/odTmiw2.png">
 <a href="https://modrinth.com/plugin/flectonechat">
  <img src="https://chat.flectone.net/assets/getonmodrinth.svg" />
</a>
 <a href="https://www.spigotmc.org/resources/flectonechat.89411/">
   <img src="https://chat.flectone.net/assets/getonspigotmc.svg" />
</a>
</p>

**FlectoneChat - personalize your minecraft server!**

FlectoneChat is a modern plugin for Spigot servers that is designed to improve gameplay without interfering with it, and
focuses on improving chat.

## Features:

- [Custom colors for **each** player](https://chat.flectone.net/features/chatcolors/)
- Support **all** colors: Hex `#1abaf0` and Default `&b`
- [**Voicechat** plugins support](https://chat.flectone.net/integrations/voicechats/)
- **Customizable** MOTD and maximum number of players
- [**Marks to highlight blocks or entities
  **, use the /mark command or an item from the configuration, such as a wooden sword](https://chat.flectone.net/features/mark/)
- Custom TAB
- [Use `%item%` placeholder in **any** messages to show **item in hand
  **](https://chat.flectone.net/features/chat#items-in-chat)
- Custom cooldown for commands and messages
- [**Chat bubbles**](https://chat.flectone.net/features/chatbubbles/)
- Settings for URL links in chat
- **Clickable** player names
- **Vault and [PlaceholderAPI](https://chat.flectone.net/integrations/papi/) support**
- Customize and disable all features at will
- SQLite database
- [**Chat patterns**](https://chat.flectone.net/features/chat#patterns)
- [Item signing](https://chat.flectone.net/features/itemsigning)
- [Custom world prefixes](https://chat.flectone.net/features/worldprefixes)
-
Commands: `/chatcolor`, `/lastonline`, `/firstonline`, `/ignore`, `/ignore-list`, `/me`, `/try`, `/try-cube`, `/msg`, `/mark`, `/reply`, `/stream start/end`, `/flectonechat reload/config/locale`, `/helper`, `/afk`, `/mute`, `/unmute`, `/helper`, `/technical-works`, `/chat`, `/ball`, `/tic-tac-toe`, `/clear-chat`, `/tempban`, `/unban`, `/broadcast`, `/poll`

See the command descriptions in [**our documentation**](https://chat.flectone.net/commands/)

<br/>

## Installation

Just [**download latest release**](https://github.com/Flectone/FlectoneChat/releases) .jar and put in in your `plugins/`
folder

<br/>

## Configuration

Checkout [**our documentation**](https://chat.flectone.net/configuration/) for all config fields!

<br/>

## Russian Video Tutorial
[![Russian Video Tutorial](https://i.ytimg.com/vi/7_WOJbr51Cg/maxresdefault.jpg)](https://www.youtube.com/watch?v=7_WOJbr51Cg)

<br/>

# Features

## Chat Colors

Chat colors is FlectoneChat's proprietary system that allows players to have preferred colors personally for
themselves.  
You can try it with `/chatcolor`

![](https://i.imgur.com/j18BuRO.gif)

## Patterns

Chat patterns is a system for replacing any custom words/patterns.  
Each `chat.patterns` (config.yml) field is a new pattern that specifies the expression to be replaced and the expression
it will be replaced with.  
The expressions are separated by combining the characters "` , `"

A few standard patterns:

```yaml
chat:
  patterns:
    - ":) , ☺"
    - ":D , ☻"
    - ":( , ☹"
    - ":ok: , 🖒"
    - ":+1: , 🖒"
    - ":-1: , 🖓"
    - ":cool: , 😎"
    - "B) , 😎"
    - ":clown: , 🤡"
    - "<3 , ❤"
    - "xd , 😆"
    - "%) , 😵"
    - "=D , 😃"
    - ">:( , 😡"
    - ":idk: , ¯\\_(ツ)_/¯"
    - ":angry: , (╯°□°)╯︵ ┻━┻"
    - ":happy: , ＼(＾O＾)／"
```

## Items in chat

FlectoneChat has a feature to display items in chat.  
To do this you just need to write `%item%` in the message and hold the desired item in your hand.

![](https://i.imgur.com/m26PIre.png)

You can also display the item by clicking on the head slot with the `shift` button pressed.

![](https://i.imgur.com/xN6yvtf.png)

## Formatting in chat

formatting in chat is only available to players with certain permissions.  
The player with op has these permissions by default

If a player has the `flectonechat.formatting` permission, he can use the built-in formatting colors of minecraft (`&c`
for red or `&l` for bold for example).

If a player has the `flectonechat.placeholders` permission, he can use placeholders from PlaceholderAPI directly in
chat.

## Hiding words (spoilers)

Your players can hide words with `||word||` syntax. The word would be shown on hover

![](https://i.imgur.com/2z5nZ6A.gif)

## Pings (mentions)

Your players can `@mention` other players in chat.  
Mentions are clickable and plays custom sound for mentioned player!

## Chat Bubbles

FlectoneChat has a built-in chat bubble system.  
They are compatible with gsit and similar plugins

![](https://i.imgur.com/7zKqJbb.png)

## Marks

Marks are block/entity highlights inspired by other cooperative games.  
You can create them using `/mark` command.  
Also you can use specific item ([wooden_sword by default](../../configuration/#commandmark)) and click RMB/LBM to create
mark.

![](https://i.imgur.com/FNCy6w7.gif)

Marks can have any default minecraft color  
![](https://i.imgur.com/sYsAbV6.png)

To change the color of the binded item, you can rename it to
the [desired color](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Color.html).

## Voice Chats

FlectoneChat have some integrations with popular voicechat plugins.  
Supported plugins:

- [Plasmo Voice](https://modrinth.com/plugin/plasmo-voice)
- [Simple Voice Chat](https://modrinth.com/plugin/simple-voice-chat/)

### mute

`mute` command also mutes in voice chats

![mute](https://i.imgur.com/RBADk74.gif)

## Item signing

Item signing is a feature that allows the player to add their nickname to the item description.  
This can be done by right-clicking on the anvil with the main item in hand and the dye in offhand

![](https://i.imgur.com/fcTl9IL.gif)
![](https://i.imgur.com/XrV2DXK.png)

<br/>
<br/>
<br/>

<a href="https://bstats.org/plugin/bukkit/FlectoneChats/16733" rel="noopener nofollow ugc"><img src="https://bstats.org/signatures/bukkit/FlectoneChats.svg" alt=""></a>
