# Commands

## /chatcolor

Set custom (for player) colors for chat and flectonechat's commands

Usage: `/chatcolor <first color> <second color> <player>`\
Permission: `flectonechat.chatcolor` (default)\
Permission (other): `flectonechat.chatcolor.other` (op)

## /spy

Toggle command spying

Usage: `/spy`\
Permission: `flectonechat.spy` (op)

## /lastonline

Find out when a player last logged in

Usage: `/lastonline <player>`\
Aliases: `lonline`\
Permission: `flectonechat.lastonline` (default)

## /firstonline

Find out when a player first logged in

Usage: `/firstonline <player>`\
Aliases: `fonline`\
Permission: `flectonechat.firstonline` (default)

## /ignore

Ignore commands and messages from a player

Usage: `/ignore <player>`\
Permission: `flectonechat.ignore` (default)

## /ignorelist

Open menu with list of ignored players

Usage: `/ignorelist`\
Permission: `flectonechat.ignore` (default)

## /me

Broadcasts a narrative message about yourself

Usage: `/me <message>`\
Permission: `flectonechat.me` (default)

## /try

Broadcasts a narrative message about yourself with random percentage of completion

Usage: `/try <message>`\
Permission: `flectonechat.try` (default)

## /try-cube

Roll N number of dice

Usage: `/try-cube <count>`\
Aliases: `dice`\
Permission: `flectonechat.try-cube` (default)

## /msg

Send private message to player

Usage: `/msg <player> <message>`\
Aliases: `w`, `tell`, `message`, `send`\
Permission: `flectonechat.msg` (default)

## /reply

reply to last received message

Usage: `/reply <message>`\
Aliases: `r`\
Permission: `flectonechat.reply` (default)

## /stream

Enter stream mode and send notification with urls

Usage: `/stream <start/end> [...urls]`\
Permission: `flectonechat.stream` (op)

## /ping

Get your ping or another player's ping

Usage: `/ping <player>`\
Aliases: `p`\
Permission: `flectonechat.ping` (default)

## /flectonechat

FlectoneChat internal command for admins

Usages:

*   reload: `/flectonechat reload <config/locale>`
*   locale: `/flectonechat locale <locale param> set <type> <value>`
*   config: `/flectonechat config <config param> set <type> <value>`

Aliases: `fc`\
Permission: `flectonechat.reload` (op)

## /mark

highlight the block or entity in front of you

Usage: `/mark <color>`\
Aliases: `f`, `m`\
Permission: `flectonechat.mark` (default)

## /mail

Send an offline message to the player (will be sent when the player is online)

Usage: `/mail <player> <message>`\
Permission: `flectonechat.mail` (default)

## /mail-clear

Delete sended mail by id

Usage: `mail-clear <player> <number>`\
Permission: `flectonechat.mail` (default)

## /afk

Set afk status (suffix)

Usage: `/afk`\
Permission: `flectonechat.afk` (default)

## /mute

mute a player

### Time format examples

    10s - 10 seconds
    5m  - 5 minutes
    3h  - 3 hours
    2d  - 2 days
    3w  - 3 weeks
    1y  - 1 year

Usage: `/mute <player> <time> <reason>`\
Permission: `flectonechat.mute` (op)

## /unmute

Unmute a player

Usage: `/unmute <player>`\
Permission: `flectonechat.mute` (op)

## /mutelist

List muted players

Usage: `/mutelist <page>`
Permission: `flectonechat.mutelist` (op)

## /helper

Send message to all players with `flectonechat.helpersee` permission

Usage: `/helper <message>`\
Permission: `flectonechat.helper` (default)\
helpers' permission: `flectonechat.helpersee` (op)

## /maintenance

Kick out all the players and don't allow them to join in.\
Ignores players with `flectonechat.maintenance` permission and ops

Usage: `/maintenance <on/off>`\
Aliases: `technical-works`\
Permission: `flectonechat.maintenance` (op)

## /chat-settings

Show chat settings

Usage: `/chat-settings`\
Permission: `flectonechat.chat-settings` (default)

## /ball

Ask crystal ball for any answer

Usage: `/ball <message>`\
Permission: `flectonechat.ball` (default)

## /tic-tac-toe

Play tic-tac-toe with player right in chat

Usage: `/tic-tac-toe <player>`\
Aliases: `ttt`\
Permission: `flectonechat.tic-tac-toe` (default)

## /clear-chat

clear chat (for yourself)

Usage: `/clearchat`\
Permission: `flectonechat.clear-chat` (default)

## /tempban

Temp ban a player

### Time format examples

    10s - 10 seconds
    5m  - 5 minutes
    3h  - 3 hours
    2d  - 2 days
    3w  - 3 weeks
    1y  - 1 year

    0   - permanent

Usage: `/tempban <player> <time/"permanent"> <reason>`\
Aliases: `ban`, `tban`\
Permission: `flectonechat.ban` (op)

## /unban

Unban a player

Usage: `/unban <player> <time> <reason>`\
Aliases: `pardon`\
Permission: `flectonechat.ban` (op)

## /banlist

List banned players

Usage: `/banlist <page>`\
Permission: `flectonechat.banlist` (op)

## /warn

Warn a player

Usage: `/warn <player> <time> <reason>`\
Permission: `flectonechat.warn` (op)

## /warnlist

List warned players

Usage: `/warnlist <page>`\
Permission: `flectonechat.warnlist` (op)

## /unwarn

Remove warn #<index> from <player>

Usage: `/unwarn <player> <index>`\
Permission: `flectonechat.warn` (op)

## /broadcast

Broadcast a message

Usage: `/broadcast <message>`\
Aliases: `bc`\
Permission: `flectonechat.broadcast` (op)

## /poll

Create a poll

Usage: `/poll create/vote <poll>`\
Permission: `flectonechat.pollvote` (default)\
Creating permission: `flectonechat.pollcreate` (op)

## /kick

Kick a player

Usage: `/kick <player> <message>`\
Permission: `flectonechat.kick` (op)

## /spit

Spit (🙂)

Usage: `/spit`\
Permission: `flectonechat.spit` (default)