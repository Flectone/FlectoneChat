# Chat features
## Patterns
Chat patterns is a system for replacing any custom words/patterns.  
Each `chat.patterns` (config.yml) field is a new pattern that specifies the expression to be replaced and the expression it will be replaced with.  
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

If a player has the `flectonechat.formatting` permission, he can use the built-in formatting colors of minecraft (`&c` for red or `&l` for bold for example).

If a player has the `flectonechat.placeholders` permission, he can use placeholders from PlaceholderAPI directly in chat.

!!! warning
    Access to placeholderAPI is dangerous and can lead to information leakage

## Hiding words (spoilers)
Your players can hide words with `||word||` syntax. The word would be shown on hover

![](https://i.imgur.com/2z5nZ6A.gif)

## Pings (mentions)
Your players can `@mention` other players in chat.  
Mentions are clickable and plays custom sound for mentioned player!
