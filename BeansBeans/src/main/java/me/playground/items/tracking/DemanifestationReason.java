package me.playground.items.tracking;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public enum DemanifestationReason {

    TOTAL("TOTAL"),
    UNKNOWN("UNKNOWN"),
    /**
     * When removed from existence via inventory cleansing, trash slots, creative mode etc.
     */
    BINNED("BINNED"),
    /**
     * When removed from existence via regular de-spawning.
     */
    DESPAWNED("DESPAWNED"),
    /**
     * When removed from existence via being destroyed by anvils, lava, cacti etc.
     */
    DESTROYED("DESTROYED"),
    /**
     * When removed from existence via being used to craft an item.
     */
    CRAFTING("CRAFTING"),
    /**
     * When removed from existence via being used to repair an item.
     */
    REPAIR("REPAIR"),
    /**
     * When removed from existence via being used ot enchant an item.
     */
    ENCHANTING("ENCHANTING"),
    /**
     * When removed from existence via being used to refine an item.
     */
    REFINEMENT("REFINEMENT"),
    /**
     * When removed from existence via villager trading.
     */
    TRADING("TRADING"),
    /**
     * When removed from existence via piglin bartering.
     */
    BARTERING("BARTERING"),
    /**
     * When removed from existence via being used to fuel a station.
     */
    FUEL("FUEL"),
    /**
     * When cooked
     */
    COOKING("COOKING"),
    /**
     * When removed from existence via nom.
     */
    EATEN("EATEN"),
    /**
     * When removed from existence via transforming into a different item.
     */
    TRANSFORMATION("TRANSFORMATION"),
    /**
     * F
     */
    TOOL_BREAK("TOOL_BREAK"),
    /**
     * When used to make a purchase for a better item or upgrade etc.
     */
    PURCHASE("PURCHASE"),
    /**
     * When sold to an NPC for Coins.
     */
    SOLD("SOLD");

    private static final Map<String, DemanifestationReason> byIdentifier = new HashMap<>();

    static {
        for (DemanifestationReason reason : values())
            byIdentifier.put(reason.getIdentifier(), reason);
    }

    private final String identifier;
    DemanifestationReason(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    @Nullable
    public static DemanifestationReason fromIdentifier(String identifier) {
        return byIdentifier.getOrDefault(identifier.toUpperCase(), null);
    }

}
