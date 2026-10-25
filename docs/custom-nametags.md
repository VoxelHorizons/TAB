# Custom overhead player names in VoxelHorizons TAB

On modern Bukkit/Paper, defining `tagname` in `groups.yml` or `users.yml`
automatically enables the TextDisplay-based overhead nametag renderer, without
requiring changes to `config.yml` or enabling the sidebar scoreboard.

## groups.yml

```yaml
_DEFAULT_:
  tabprefix: "%logic_rank%%deluxetags_tag%"
  tagprefix: "%luckperms-prefix%"
  tagname: "%player_displayname%"
  tagsuffix: "%luckperms-suffix%"
  customtabname: "%player_displayname%"
```

Run `/tab reload` after saving the file. Tag names also support user-specific
and per-world settings. Without a matching `tagname` for a player, the normal
`%player%` value is used for their replacement label.

You can leave both visible scoreboards and the scoreboard-teams toggle off:

```yaml
scoreboard:
  enabled: false
scoreboard-teams:
  enabled: false
```

The sidebar stays off. When `tagname` is present and supported, TAB enables
**internal scoreboard teams** for sorting and suppression of the native
username. These are packets, not a visible sidebar; they may conflict with
other plugins that also manage scoreboard teams. With `tagname` removed and
`/tab reload`, the original behavior returns.

## Compatibility and limitations

- Requires TextDisplay (Bukkit/Paper 1.19.4+), is disabled on Folia and
  cannot work on proxy-only installs. Older platforms keep vanilla labels.
- Does not change GameProfile usernames, player skins or UUIDs.
- Uses TAB's placeholder refresh for prefix, name and suffix.
- Honors tab nametag hide/view commands, world isolation and canSee vanish.
- One shared display per player means **per-viewer relational placeholders**
  are not supported in the overlay.
- TextDisplay movement may differ from vanilla while sneaking, swimming,
  mounting, changing worlds or teleporting.
- Bedrock/Geyser compatibility depends on display entity translation.

## Smoke tests

1. On Paper 26.3, disable `scoreboard` and `scoreboard-teams`; add
   `tagname: "%player_displayname%"` only to `groups.yml` and reload TAB.
2. Confirm the nickname displays exactly once over the player's head and
   the sidebar does not appear.
3. Change nickname live and check placeholder refresh.
4. Test TAB hide/show, toggleview, vanish, reconnect and world switch.
5. Test sneaking, swimming, death, mounts and teleportation; inspect cleanup.
6. Remove `tagname` and reload; confirm original nametags return.
7. Test old server fallback and Bedrock/Geyser clients.
