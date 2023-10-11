# Branding

Branding is a text, displayed as server software's brand in debug screen (F3).
![](https://i.imgur.com/tCzvlzS.png)

You can customize it with `server.brand.message` in your lang file
```yaml
server:
  brand:
    message:
      - "&bFlectoneChat"
```

Also, you can animate it by setting animation rate (`server.brand.update.rate`, config.yml)
and adding new lines to `server.brand.message`
```yaml
server:
  brand:
    message:
      - "&bFlectoneChat"
      - "&fFlectoneChat"
```