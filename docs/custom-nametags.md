# Custom display names above player heads (VoxelHorizons TAB)

Minecraft scoreboard teams render `tagprefix + real username + tagsuffix`.
The optional TextDisplay nametag renderer replaces the visual name without
changing a player's GameProfile, UUID, skin, or tablist name.

## Configuration

`plugins/TAB/config.yml`:

```yaml
scoreboard-teams:
  enabled: true
  invisible-nametags: false
  custom-nametags:
    enabled: true
```

`plugins/TAB/groups.yml`:

```yaml
_DEFAULT_:
  tabprefix: "%logic_rank%%deluxetags_tag%"
  tagprefix: "%luckperms-prefix%"
  tagname: "%player_displayname%"
  customtabname: "%player_displayname%"
  tagsuffix: "%luckperms-suffix%"
```

`tagname` can also be overridden in `users.yml` and per-world sections.
If omitted it defaults to `%player%`. Nicknames refresh through TAB's
placeholder refresh mechanism. Apply changes using `/tab reload`.

## Supported platforms and limitations

- Optional, **off by default**. Requires Bukkit/Paper 1.19.4+, TextDisplay
  support, and enabled scoreboard teams. Folia and proxy-only installations
  use vanilla labels without changes.
- Sorting/collision/team membership remain under TAB's native scoreboard teams.
- One shared TextDisplay exists per player: **relational placeholders cannot
  be displayed differently for different viewers**. Other TAB placeholders,
  LuckPerms strings and VoxelCore aliases follow normal TAB property processing.
- Viewer nametag visibility, TAB hide commands, vanished players and world
  separation are respected. TextDisplays are non-persistent and removed on
  plugin shutdown or disabling this option through `/tab reload`.
- Overlay position follows eye height. Motion is updated every two server
  ticks with optional client interpolation, which may look slightly different
  from vanilla during sneaking, crawling, vehicles or teleportation.
- Bedrock/Geyser compatibility depends on TextDisplay translation. Test with
  GeyserDisplayEntity on your deployed Geyser version.

## Manual verification

1. Build the universal JAR (`./gradlew build -PskipFand=true`) on JDK 25.
2. Enable on Paper 26.3; join with two players, set /nick for one, and confirm
   the original name is replaced exactly once while the TAB player list remains
   unchanged.
3. Change nickname dynamically and validate PlaceholderAPI refresh.
4. Test /tab nametag hide/show, toggleview, vanish, world switch and relog.
5. Test sneaking, swimming, passenger/mounts, flying and dimension teleport.
6. Disable the option then /tab reload; confirm the native username returns and
   no TextDisplay tag entities are left behind.
7. Verify old Bukkit and Folia behavior is unchanged, and test a Bedrock client.
