# Префиксы миров

По умолчанию префиксы миров имеют цветные точки.\
Чтобы изменить их отображение, проверьте свой lang-файл.

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

Миры могут быть обнаружены в двух режимах: по имени или по типу мира.\
Чтобы изменить это поведение, измените `player.world.mode` в вашем config.yml

Если режим установлен в `type`, то префикс мира может быть задан 4 типами мира:

*   normal
*   nether
*   the\_end
*   custom

Если режим установлен в `name`, то поля `world.prefix` будут определены как имена миров
