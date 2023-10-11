# Брэндинг

Брендинг - это текст, отображаемый в качестве бренда ядра на экране отладки (F3).
![](https://i.imgur.com/tCzvlzS.png)

Вы можете настроить его с помощью `server.brand.message` в вашем файле локализации
```yaml
server:
  brand:
    message:
      - "&bFlectoneChat"
```

Также его можно анимировать, задав скорость анимации (`server.brand.update.rate`, config.yml)
и добавив новые строки в `server.brand.message`.
```yaml
server:
  brand:
    message:
      - "&bFlectoneChat"
      - "&fFlectoneChat"
```