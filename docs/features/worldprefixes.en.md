# World prefixes

World prefixes are colored dots by default.\
To change their display, check your lang file.

```yaml
  world:
    prefix:
      # mode can be changed in config.yml (default=type)
      # mode=type
      normal: "#4eff52● &f"
      nether: "#ff4e4e● &f"
      the_end: "#834eff● &f"
      custom: "#1abaf0● &f"

      # mode=name
      world: "#4eff52● &f"
      world_nether: "#ff4e4e● &f"
      world_the_end: "#834eff● &f"
```

Worlds can be detected in 2 modes: either by name or by world type.\
To change this behavior change `player.world.mode` in your config.yml

If the mode is set to `type`, the world prefix can be set by 4 world types:

*   normal
*   nether
*   the\_end
*   custom

If the mode is set to `name` the `world.prefix` fields will be defined as world names
