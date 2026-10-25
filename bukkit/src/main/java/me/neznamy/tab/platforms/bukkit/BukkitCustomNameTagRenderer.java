package me.neznamy.tab.platforms.bukkit;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.nametags.TeamConfiguration;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Bukkit-only overhead names using TextDisplay. Vanilla team name visibility
 * is suppressed by the shared NameTag feature, but team membership, collision
 * rules and sorting still work normally. Folia and old servers are excluded.
 *
 * The common Bukkit module compiles against 1.16, so we use reflection only
 * for the newer TextDisplay methods and class. All world access runs on tick.
 */
public final class BukkitCustomNameTagRenderer {

    private final JavaPlugin plugin;
    private final Map<UUID, Tag> tags = new HashMap<>();
    private boolean warned;
    private int ticks;

    BukkitCustomNameTagRenderer(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    void start() {
        // Check configuration every cycle to support /tab reload.
        Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 2L, 2L);
    }

    void stop() {
        for (Tag tag : tags.values()) tag.entity.remove();
        tags.clear();
    }

    private boolean enabled() {
        TAB tab = TAB.getInstance();
        if (tab == null || tab.getConfiguration() == null ||
                tab.getConfiguration().getConfig() == null) return false;
        TeamConfiguration config = tab.getConfiguration().getConfig().getTeams();
        return config != null && config.isCustomNameTags() &&
                tab.getPlatform().supportsCustomNameTags();
    }

    private void tick() {
        if (!enabled()) {
            if (!tags.isEmpty()) stop();
            return;
        }
        boolean refresh = ++ticks % 5 == 0;
        Set<UUID> current = new HashSet<>();

        for (Player owner : Bukkit.getOnlinePlayers()) {
            TabPlayer tabOwner = TAB.getInstance().getPlayer(owner.getUniqueId());
            if (tabOwner == null || !tabOwner.isLoaded() ||
                    tabOwner.teamData.isDisabled() ||
                    tabOwner.teamData.prefix == null ||
                    tabOwner.teamData.suffix == null ||
                    tabOwner.teamData.tagname == null) continue;

            UUID id = owner.getUniqueId();
            current.add(id);
            Tag tag = tags.get(id);
            if (tag != null && (tag.entity.isDead() ||
                    !tag.entity.getWorld().equals(owner.getWorld()))) {
                tag.entity.remove();
                tags.remove(id);
                tag = null;
            }

            Location desired = owner.getEyeLocation().add(0, 0.65, 0);
            if (tag == null) {
                Entity display = spawn(desired);
                if (display == null) continue;
                tag = new Tag(display);
                tags.put(id, tag);
                // No accidental vanilla-visible labels until permissions are checked.
                for (Player viewer : Bukkit.getOnlinePlayers()) {
                    setVisibility(viewer, display, false);
                }
                update(tag, owner, tabOwner);
            } else if (tag.entity.getLocation().distanceSquared(desired) > 0.0025) {
                tag.entity.teleport(desired);
            }
            if (refresh) update(tag, owner, tabOwner);
        }

        Iterator<Map.Entry<UUID, Tag>> it = tags.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Tag> entry = it.next();
            if (!current.contains(entry.getKey())) {
                entry.getValue().entity.remove();
                it.remove();
            }
        }
    }

    private Entity spawn(Location location) {
        Entity entity = null;
        try {
            entity = location.getWorld().spawnEntity(location, EntityType.valueOf("TEXT_DISPLAY"));
            entity.setPersistent(false);
            entity.setGravity(false);
            entity.setInvulnerable(true);
            entity.setSilent(true);
            // Never broadcast this entity to a joining player until TAB's
            // vanish/visibility rules explicitly allow the label for them.
            // Available on every Bukkit version supporting TextDisplay.
            entity.getClass().getMethod("setVisibleByDefault", boolean.class).invoke(entity, false);
            Class<?> billboard = Class.forName("org.bukkit.entity.Display$Billboard");
            Object center = null;
            for (Object value : billboard.getEnumConstants()) {
                if (value.toString().equals("CENTER")) center = value;
            }
            if (center == null) throw new IllegalStateException("Missing CENTER billboard");
            entity.getClass().getMethod("setBillboard", billboard).invoke(entity, center);
            optional(entity, "setTeleportDuration", int.class, 2);
            return entity;
        } catch (ReflectiveOperationException | RuntimeException failure) {
            if (entity != null) entity.remove();
            warn("Unable to create TextDisplay custom nametags", failure);
            return null;
        }
    }

    private void update(Tag tag, Player owner, TabPlayer tabOwner) {
        String text = tabOwner.teamData.prefix.get() +
                tabOwner.teamData.tagname.get() +
                tabOwner.teamData.suffix.get();
        if (!text.equals(tag.text)) {
            try {
                tag.entity.getClass().getMethod("setText", String.class).invoke(tag.entity, text);
                tag.text = text;
            } catch (ReflectiveOperationException | RuntimeException failure) {
                warn("Unable to set TextDisplay text", failure);
                tag.entity.remove();
                return;
            }
        }

        Set<UUID> connected = new HashSet<>();
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            UUID id = viewer.getUniqueId();
            connected.add(id);
            TabPlayer tabViewer = TAB.getInstance().getPlayer(id);
            boolean visible = !id.equals(owner.getUniqueId()) &&
                    viewer.getWorld().equals(owner.getWorld()) &&
                    viewer.canSee(owner) &&
                    tabViewer != null && !tabViewer.teamData.isDisabled() &&
                    !tabOwner.teamData.vanishedFor.contains(id) &&
                    tabOwner.teamData.getTeamVisibility(tabViewer);

            // Track Player instances, not just UUIDs, to handle reconnects.
            Player shown = tag.visibleTo.get(id);
            if (visible && shown != viewer) {
                setVisibility(viewer, tag.entity, true);
                tag.visibleTo.put(id, viewer);
            } else if (!visible && shown != null) {
                setVisibility(viewer, tag.entity, false);
                tag.visibleTo.remove(id);
            } else if (!visible) {
                // New viewers should not see this tag until explicitly allowed.
                setVisibility(viewer, tag.entity, false);
            }
        }
        tag.visibleTo.keySet().retainAll(connected);
    }

    /**
     * 1.16's compile-only Bukkit API lacks these methods, though they exist on
     * every server with TextDisplay. Resolve on the live Player implementation.
     */
    private void setVisibility(Player viewer, Entity display, boolean show) {
        try {
            viewer.getClass().getMethod(show ? "showEntity" : "hideEntity",
                    org.bukkit.plugin.Plugin.class, Entity.class).invoke(viewer, plugin, display);
        } catch (ReflectiveOperationException | RuntimeException failure) {
            warn("Unable to update TextDisplay visibility", failure);
        }
    }

    private void warn(String message, Exception failure) {
        if (warned) return;
        warned = true;
        plugin.getLogger().warning(message + ": " + failure);
    }

    private static void optional(Entity entity, String name, Class<?> type, Object value) {
        try {
            Method method = entity.getClass().getMethod(name, type);
            method.invoke(entity, value);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            // Optional smooth teleport interpolation on modern display APIs.
        }
    }

    private static final class Tag {
        private final Entity entity;
        private final Map<UUID, Player> visibleTo = new HashMap<>();
        private String text = "";

        private Tag(Entity entity) {
            this.entity = entity;
        }
    }
}
