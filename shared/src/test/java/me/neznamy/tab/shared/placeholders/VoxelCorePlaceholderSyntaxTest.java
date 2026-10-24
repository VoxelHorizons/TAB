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
