# Команды

## /chatcolor

Установка кастомных (для игрока) цветов для чата и команд flectonechat

Использование: `/chatcolor <first color> <second color> <player>`\
Разрешение: `flectonechat.chatcolor` (default)\
Разрешение (для других): `flectonechat.chatcolor.other` (op)

## /lastonline

узнать когда игрок последний раз входил на сервер

Использование: `/lastonline <player>`\
Вариации: `lonline`\
Разрешение: `flectonechat.lastonline` (default)

## /firstonline

Узнать когда игрок впервые вошел на сервер

Использование: `/firstonline <player>`\
Вариации: `fonline`\
Разрешение: `flectonechat.firstonline` (default)

## /ignore

Игнорировать команды и сообщения от игрока

Использование: `/ignore <player>`\
Разрешение: `flectonechat.ignore` (default)

## /ignore-list

Открыть меню со списком игнорируемых игроков

Использование: `/ignore-list <player>`\
Разрешение: `flectonechat.ignore` (default)

## /me

Вывести повествовательное сообщение о себе

Использование: `/me <message>`\
Разрешение: `flectonechat.me` (default)

## /try

Вывести сообщение о себе со случайным процентом выполнения

Использование: `/try <message>`\
Разрешение: `flectonechat.try` (default)

## /try-cube

Бросок N количества игральных костей

Использование: `/try-cube <count>`\
Вариации: `dice`\
Разрешение: `flectonechat.try-cube` (default)

## /msg

Отправить личное сообщение игроку

Использование: `/msg <player> <message>`\
Вариации: `w`, `tell`, `message`, `send`\
Разрешение: `flectonechat.msg` (default)

## /reply

Ответить на последнее полученное сообщение

Использование: `/reply <message>`\
Вариации: `r`\
Разрешение: `flectonechat.reply` (default)

## /stream

Войти в режим стримера и отправить уведомление с ссылками

Использование: `/stream <start/end> [...urls]`\
Разрешение: `flectonechat.stream` (op)

## /ping

Получить свой пинг или пинг другого игрока

Использование: `/ping <player>`\
Вариации: `p`\
Разрешение: `flectonechat.ping` (default)

## /flectonechat

Внутренняя команда FlectoneChat для администраторов

Использования:

*   reload: `/flectonechat reload <config/locale>`
*   locale: `/flectonechat locale <locale param> set <type> <value>`
*   config: `/flectonechat config <config param> set <type> <value>`

Вариации: `fc`\
Разрешение: `flectonechat.reload` (op)

## /mark

Выделить блок или сущность, находящийся перед вами

Использование: `/mark <color>`\
Вариации: `f`, `m`\
Разрешение: `flectonechat.mark` (default)

## /mail

Отправить сообщение игроку не в сети  (будет отправлено, когда игрок будет в сети)

Использование: `/mail <player> <message>`\
Разрешение: `flectonechat.mail` (default)

## /mail-clear

Удалить отправленные сообщения

Использование: `mail-clear <player> <number>`\
Разрешение: `flectonechat.mail` (default)

## /afk

Установить статуса afk (суффикс)

Использование: `/afk`\
Разрешение: `flectonechat.afk` (default)

## /mute

Замутить игрока

### Примеры формата времени

    10s - 10 секунд
    5m  - 5 минут
    3h  - 3 часов
    2d  - 2 дня
    3w  - 3 недели
    1y  - 1 год

Использование: `/mute <player> <time> <reason>`\
Разрешение: `flectonechat.mute` (op)

## /unmute

Размутить игрока

Использование: `/mute <player> <time> <reason>`\
Разрешение: `flectonechat.mute` (op)

## /mutelist

Список замученных игроков

Использование: `/mutelist <page>`
Разрешение: `flectonechat.mutelist` (op)

## /helper

Отправить сообщение всем с разрешением `flectonechat.helpersee`

Использование: `/mute <player> <time> <reason>`\
Разрешение: `flectonechat.helper` (default)  helpers'
Разрешение: `flectonechat.helpersee` (op)

## /maintenance

Кикнуть всех игроков и не позволить им присоединиться к игре.\
Игнорирует игроков с разрешением `flectonechat.maintenance` и операторов

Использование: `/maintenance <on/off>`\
Вариации: `technical-works`\
Разрешение: `flectonechat.maintenance` (op)

## /chat

Использования:

*   switch: `/chat switch <local/global>` - писать в локальный/глобальный чат по умолчанию
*   hide: `/chat hide <local/global>` - скрыть локальные/глобальные сообщения

Вариации: `chat`, `switch-chat`, `off-chat`\
Разрешение: `flectonechat.chat` (default)

## /ball

Задать вопрос хрустальному шару, чтобы получить ответ

Использование: `/ball <message>`\
Разрешение: `flectonechat.ball` (default)

## tic-tac-toe

Играть в крестики-нолики с игроком прямо в чате

Использование: `/tic-tac-toe <player>`\
Вариации: `ttt`\
Разрешение: `flectonechat.tic-tac-toe` (default)

## /clear-chat

Очистить чат (для себя)

Использование: `/clearchat`\
Разрешение: `flectonechat.clear-chat` (default)

## /tempban

Временно забанить игрока

### Примеры формата времени

    10s - 10 секунд
    5m  - 5 минут
    3h  - 3 часов
    2d  - 2 дня
    3w  - 3 недели
    1y  - 1 год

    0   - навсегда

Использование: `/tempban <player> <time/"permanent"> <reason>`\
Вариации: `ban`, `tban`\
Разрешение: `flectonechat.ban` (op)

## /unban

Разбанить игрока

Использование: `/unban <player> <time> <reason>`\
Вариации: `pardon`\
Разрешение: `flectonechat.ban` (op)

## /banlist

Список забаненных игроков

Использование: `/banlist <page>`
Разрешение: `flectonechat.banlist` (op)

## /broadcast

Вывести сообщение

Использование: `/broadcast <message>`\
Вариации: `bc`\
Разрешение: `flectonechat.broadcast` (op)

## /poll

Создать опрос

Использование: `/poll create/vote <poll>`\
Разрешение: `flectonechat.pollvote` (default)\
Разрешение на создание: `flectonechat.pollcreate` (op)
