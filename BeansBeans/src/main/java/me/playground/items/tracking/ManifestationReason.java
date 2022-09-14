package me.playground.items.tracking;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public enum ManifestationReason {

    TOTAL("TOTAL"),
    UNKNOWN("UNKNOWN"),

    SPAWNED("SPAWNED"),
    COOKING("COOKING"),
    CRAFTING("CRAFTING"),
    TRADING("TRADING"),
    BARTERING("BARTERING"),
    TRANSFORMATION("TRANSFORMATION"),
    ENTITY_DROP("ENTITY_DROP"),
    BLOCK_DROP("BLOCK_DROP"),
    AUTO_HARVEST("AUTO_HARVEST"),
    HARVEST("HARVEST"),
    REWARD("REWARD"),
    SHOP("SHOP"),
    LOOT("LOOT"),
    FIRST_TIME_LOGIN("FIRST_TIME_LOGIN");

    private static final Map<String, ManifestationReason> byIdentifier = new HashMap<>();

    static {
        for (ManifestationReason reason : values())
            byIdentifier.put(reason.getIdentifier(), reason);
    }

    private final String identifier;
    ManifestationReason(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    @Nullable
    public static ManifestationReason fromIdentifier(String identifier) {
        return byIdentifier.getOrDefault(identifier.toUpperCase(), null);
    }

}
