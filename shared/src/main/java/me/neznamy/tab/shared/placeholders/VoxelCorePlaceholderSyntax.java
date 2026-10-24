package me.neznamy.tab.shared.placeholders;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.function.UnaryOperator;

/**
 * VoxelCore's colon-delimited UI glyph aliases, including namespaced aliases and pixel offsets.
 * This recognises tokens only; the owning VoxelCore plugin decides which aliases are valid.
 */
public final class VoxelCorePlaceholderSyntax {

    private static final Pattern TOKEN = Pattern.compile(":[A-Za-z0-9_][A-Za-z0-9_./-]*:");

    private VoxelCorePlaceholderSyntax() {
    }

    /**
     * Finds complete colon-delimited identifiers in appearance order. Adjacent aliases such as
     * {@code :offset_-16::warps_background:} are detected independently.
     */
    @NotNull
    public static List<String> detect(@NotNull String text) {
        if (text.indexOf(':') < 0) return Collections.emptyList();
        List<String> placeholders = new ArrayList<>();
        Matcher matcher = TOKEN.matcher(text);
        while (matcher.find()) {
            placeholders.add(matcher.group());
        }
        return placeholders;
    }

    /** Post-processes the resolved output from LuckPerms, PAPI and TAB placeholders. */
    @NotNull
    public static String processOutput(@NotNull String value, @NotNull UnaryOperator<String> resolver) {
        return value.indexOf(':') < 0 ? value : resolver.apply(value);
    }

    /** Checks whether the input is one complete colon-delimited VoxelCore alias. */
    public static boolean isValid(@NotNull String identifier) {
        return TOKEN.matcher(identifier).matches();
    }
}
