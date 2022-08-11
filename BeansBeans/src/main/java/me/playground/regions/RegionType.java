package me.playground.regions;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public enum RegionType {

    DEFINED("DEFINED"),
    PLAYER("PLAYER");

    private static final Map<String, RegionType> byIdentifier = new HashMap<>();

    static {
        for (RegionType reason : values())
            byIdentifier.put(reason.getIdentifier(), reason);
    }

    private final String identifier;

    RegionType(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    @Nullable
    public static RegionType fromIdentifier(String identifier) {
        return byIdentifier.getOrDefault(identifier.toUpperCase(), null);
    }
}
