# VoxelCore UI aliases in TAB

On Bukkit/Paper servers with **VoxelCore enabled**, TAB recognises VoxelCore's colon-delimited UI aliases in all text processed by TAB's placeholder pipeline. VoxelCore is an **optional** soft dependency; TAB keeps functioning when it is absent.

## LuckPerms and PlaceholderAPI outputs

**No changes to TAB's groups.yml are needed.** This configuration works as-is:

```yaml
_DEFAULT_:
  tabprefix: "%logic_rank%%deluxetags_tag%"
  tagprefix: "%luckperms-prefix%"
  customtabname: "%player_displayname%"
  tabsuffix: "%luckperms-suffix%"
  tagsuffix: "%luckperms-suffix%"
```

If LuckPerms returns `:player:` for `%luckperms-prefix%`, TAB applies VoxelCore's resolver to **the returned value**, after normal placeholder processing and output replacements. This applies to `%logic_rank%`, other PAPI expansions, tab names, and suffixes, too.

## Direct aliases (also supported)

Use in TAB's `groups.yml`, `users.yml`, `config.yml`, or any other TAB text that supports placeholders:

```yaml
_DEFAULT_:
  tagprefix: ':branding_icon_small: &7'
  tagsuffix: ' :offset_-8::voxel/warps_background:'
```

Supported forms include `:name:`, `:namespace/name:`, and `:offset_-16:`. Multiple adjacent aliases work, including `:offset_-16::warps_background:`. Existing `%placeholderapi_placeholder%` and TAB placeholders continue to work alongside them.

Use `/tab parse <player> :branding_icon_small:` to check an alias. `/tab reload` reloads TAB configuration. VoxelCore content updates are picked up during TAB's normal placeholder refresh cycle; per-alias refresh intervals can be configured in TAB's `config.yml` under `placeholder-refresh-intervals`, using a quoted key such as `':branding_icon_small:': 1000`.

## Implementation details

- Only Bukkit/Paper detects these tokens, and only while VoxelCore is enabled. Proxy builds continue to use their existing placeholder syntax.
- After all normal TAB placeholder processing, output replacements, and nested expansion, TAB post-processes the returned text with VoxelCore's resolver. The final combined string receives the same pass.
- TAB delegates resolution to VoxelCore's **public `TextPlaceholderService.resolve(String)`** via an optional reflective hook. No duplicated glyph registry or compile-time VoxelCore dependency is introduced.
- Aliases are registered as server placeholders since the resolved glyphs and offsets are player-independent. TAB caches and refreshes these values and renders them in nametags, tab lists, and other features.
- Unknown, ambiguous, invalid, or out-of-range aliases remain visible unchanged, matching VoxelCore's resolver behavior.
- VoxelCore's glyph semantics, including inline/GUI authorization and the injected white colour code, are preserved.

**Note:** Displaying a glyph requires the player to have the VoxelCore resource pack. VoxelCore GUI positioning offsets and nonstandard glyph advances may not be visually suitable for vanilla nametag rendering. This integration does not implement Bedrock font rendering; Geyser clients need separate support.
