package me.playground.skills;

import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ordinal is used for this enum very often.
 */
public enum MilestoneTier {

    NONE("NONE", 0x626262, "None", Material.GRAY_DYE), // When a player doesn't have the milestone

    DIRT("DIRT", 0xAA6622, "Dirt", Material.COARSE_DIRT), // The minimum tier allowed for a milestone
    WOOD("WOOD", 0xCC8832, "Wood", Material.OAK_LOG),
    STONE("STONE", 0x9999A4, "Stone", Material.STONE),
    COPPER("COPPER", 0xCC6655, "Copper", Material.COPPER_BLOCK),
    IRON("IRON", 0xDDDDDD, "Iron", Material.IRON_BLOCK),
    GOLD("GOLD", 0xFFDE45, "Gold", Material.GOLD_BLOCK),
    DIAMOND("DIAMOND", 0x38CCFF, "Diamond", Material.DIAMOND_BLOCK),
    EMERALD("EMERALD", 0x54FF72, "Emerald", Material.EMERALD_BLOCK); // The maximum tier allowed for a milestone

    private final TextColor colour;
    private final String identifier;
    private final String displayName;
    private final Material material;
    private int myIdx;

    private static final Map<String, MilestoneTier> identifiers = new HashMap<>();
    private static final List<MilestoneTier> orderedTiers = new ArrayList<>(); // Not sure how pointless all this is

    static {
        for (MilestoneTier tier : values()) {
            identifiers.put(tier.identifier, tier);
            orderedTiers.add(tier);
            tier.myIdx = orderedTiers.indexOf(tier);
        }
    }

    MilestoneTier(String identifier, int colour, String displayName, Material material) {
        this.colour = TextColor.color(colour);
        this.displayName = displayName;
        this.identifier = identifier;
        this.material = material;
    }

    @NotNull
    public TextColor getColour() {
        return colour;
    }

    @NotNull
    public String getName() {
        return displayName;
    }

    @NotNull
    public String getIdentifier() {
        return identifier;
    }

    @NotNull
    public Material getMaterial() {
        return material;
    }

    @NotNull
    public MilestoneTier tierUp() {
        int next = myIdx + 1;
        if (next >= orderedTiers.size()) return this;

        return orderedTiers.get(next);
    }

    public boolean lowerThan(@NotNull MilestoneTier tier) {
        return this.ordinal() < tier.ordinal();
    }

    @NotNull
    public static MilestoneTier fromIdentifier(@NotNull String identifier) {
        return identifiers.getOrDefault(identifier, NONE);
    }

    @Nullable
    public static MilestoneTier fromOrdinal(int ordinal) {
        return orderedTiers.get(ordinal);
    }

}
