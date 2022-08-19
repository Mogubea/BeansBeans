package me.playground.punishments;

import me.playground.regions.RegionType;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public enum Type {

    BAN("BAN", "You have been banned."),
    MUTE("MUTE", "You have been muted."),
    KICK("KICK", "You have been kicked.");

    private static final Map<String, Type> byIdentifier = new HashMap<>();

    static {
        for (Type type : values())
            byIdentifier.put(type.getIdentifier(), type);
    }

    private final String identifier;
    private final String defaultMsg;

    Type(String identifier, String defaultMsg) {
        this.identifier = identifier;
        this.defaultMsg = defaultMsg;
    }


    public String getIdentifier() {
        return identifier;
    }

    public String getDefaultMessage() {
        return defaultMsg;
    }

    @Nullable
    public static Type fromIdentifier(String identifier) {
        return byIdentifier.getOrDefault(identifier.toUpperCase(), null);
    }

}
