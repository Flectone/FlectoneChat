# Tab

FlectoneChat has custom tab (playerlist) system.

## Static tab

You can customize tab's header and footer with `tab.header.message` and `tab.footer.message` respectively in your lang file
```yaml
tab:
  header:
    message:
      - "First line"
      - "Second line"
      - "Third line"
      ...
  footer:
    message:
      - "First line"
      - "Second line"
      - "Third line"
      ...
```

## Animated tab

You can create simple animations in your tab.\
first, set the delay value between animation frames in `tab.update.rate` (config.yml).\
Now you can add frames to your localization file, separating them with the technical line `<next>`.

```yaml
tab:
  header:
    message:
      - "First line | frame 1"
      - "Second line | frame 1"
      ...
      - "<next>"
      - "First line | frame 2"
      - "Second line | frame 2"
      ...
  footer:
    message:
      - "First line | frame 1"
      - "Second line | frame 1"
      - "<next>"
      - "First line | frame 2"
      - "Second line | frame 2"
      ...
```
