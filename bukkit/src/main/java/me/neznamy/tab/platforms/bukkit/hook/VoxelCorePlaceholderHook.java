package me.neznamy.tab.platforms.bukkit.hook;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Optional integration with VoxelCore's thread-safe TextPlaceholderService.
 *
 * Resolves a complete alias through VoxelCore itself so TAB does not need to mirror
 * UI glyph allocations, visibility rules, or offset semantics. No compile-time or
 * classloader linkage to VoxelCore is required.
 */
public final class VoxelCorePlaceholderHook {

    private volatile boolean warningLogged;

    public boolean isAvailable() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("VoxelCore");
        return plugin != null && plugin.isEnabled();
    }

    @NotNull
    public String resolve(@NotNull String identifier) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("VoxelCore");
        if (plugin == null || !plugin.isEnabled()) return identifier;
        try {
            Method serviceGetter = plugin.getClass().getMethod("getTextPlaceholderService");
            Object service = serviceGetter.invoke(plugin);
            if (service == null) return identifier;
            Method resolve = service.getClass().getMethod("resolve", String.class);
            Object value = resolve.invoke(service, identifier);
            return value instanceof String ? (String) value : identifier;
        } catch (ReflectiveOperationException | LinkageError exception) {
            if (!warningLogged) {
                synchronized (this) {
                    if (!warningLogged) {
                        warningLogged = true;
                        Throwable cause = exception instanceof InvocationTargetException
                                ? ((InvocationTargetException) exception).getTargetException() : exception;
                        Bukkit.getLogger().warning("[TAB] Unable to resolve VoxelCore UI placeholders: " + cause);
                    }
                }
            }
            return identifier;
        }
    }
}
