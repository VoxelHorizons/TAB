package me.neznamy.tab.shared.placeholders;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class VoxelCorePlaceholderSyntaxTest {

    @Test
    void detectsNamedNamespacedAndSignedOffsetAliasesInTextualOrder() {
        assertEquals(
                Arrays.asList(":offset_-16:", ":voxel/warps_background:", ":offset_4:", ":branding_icon_small:"),
                VoxelCorePlaceholderSyntax.detect("§f:offset_-16::voxel/warps_background: %player_name% " +
                        ":offset_4::branding_icon_small:")
        );
    }

    @Test
    void preservesOccurrencesAndDoesNotIncludeOrdinaryPunctuation() {
        assertEquals(Arrays.asList(":icon:", ":icon:"),
                VoxelCorePlaceholderSyntax.detect("http://example.test :icon: plain: words :icon:"));
        assertEquals(Collections.emptyList(), VoxelCorePlaceholderSyntax.detect("no placeholders here"));
        assertEquals(Collections.emptyList(), VoxelCorePlaceholderSyntax.detect(":bad alias:"));
    }

    @Test
    void processesAliasReturnedByLuckPermsPrefix() {
        // TAB config stays tagprefix: "%luckperms-prefix%"; the returned value is :player:.
        String resolved = VoxelCorePlaceholderSyntax.processOutput(":player:",
                text -> text.replace(":player:", "§f\\ue123"));
        assertEquals("§f\\ue123", resolved);
    }

    @Test
    void processesMultipleReturnedAliasesAndPreservesRemainingPlaceholders() {
        String output = VoxelCorePlaceholderSyntax.processOutput(
                "&7:player: :offset_-16::voxel/warps_background: %player_name%",
                text -> text.replace(":player:", "[rank]")
                        .replace(":offset_-16:", "[negative spacing]")
                        .replace(":voxel/warps_background:", "[background]"));
        assertEquals("&7[rank] [negative spacing][background] %player_name%", output);
    }

    @Test
    void ignoresReturnedTextWithoutColonTokens() {
        assertEquals("plain %luckperms-prefix%", VoxelCorePlaceholderSyntax.processOutput(
                "plain %luckperms-prefix%", text -> { throw new AssertionError("should not run"); }));
    }

    @Test
    void validatesIdentifiersWithoutChangingExistingPlaceholderSyntax() {
        assertTrue(PlaceholderIdentifier.isValid(":offset_-320:"));
        assertTrue(PlaceholderIdentifier.isValid(":voxel/ui.profile:"));
        assertTrue(PlaceholderIdentifier.isValid("%player_name%"));
        assertTrue(PlaceholderIdentifier.isValid("<some_tag>"));
        assertFalse(PlaceholderIdentifier.isValid(":"));
        assertFalse(PlaceholderIdentifier.isValid("::"));
        assertFalse(PlaceholderIdentifier.isValid(":bad alias:"));
        assertFalse(PlaceholderIdentifier.isValid(":icon: trailing text"));
        assertFalse(PlaceholderIdentifier.isValid("hello:world:"));
    }
}
