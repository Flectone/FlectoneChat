# Warns

You can warn a player by using the `/warn` command\
The warning will be stored and the number of warnings for the player will increase.

## Punishments

You can customize punishments for reaching a certain number of warnings a player has.\
you can set them in `command.warn.action` (config.yml),
setting the number of warnings as the key and the command to execute when the set number of warnings is reached
```yaml
command:
  warn:
    action:
      3: "ban <player> 7d" # ban <player> for 7 days after third warn
      5: "ban <player>" # ban <player> permanently after 5th warn
```
