package me.playground.punishments;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public enum Type {

    /**
     * A ban from the Minecraft Server.
     */
    MINECRAFT_BAN("MINECRAFT_BAN", "Banned", "Ban"),
    /**
     * An IP ban from the Minecraft Server.
     */
    MINECRAFT_IP_BAN("MINECRAFT_IP_BAN", "IP Banned", "IP Ban"),
    /**
     * A global chat mute on the Minecraft Server, player can still message in party channels or DM staff members/friends.
     */
    MINECRAFT_GLOBAL_MUTE("MINECRAFT_GLOBAL_MUTE", "Muted", "Mute"),
    /**
     * A full mute on the Minecraft Server. The player can no longer dm anybody.
     */
    MINECRAFT_FULL_MUTE("MINECRAFT_FULL_MUTE", "Full-Muted", "Full-Mute"),
    /**
     * A kick from the Minecraft Server.
     */
    MINECRAFT_KICK("MINECRAFT_KICK", "Kicked", "Kick", false),
    /**
     * Other punishments.
     */
    MINECRAFT_OTHER("MINECRAFT_OTHER", "...", "..."),

    /**
     * A ban from the Discord.
     */
    DISCORD_BAN("DISCORD_BAN", "Banned", "Ban"),
    /**
     * A mute in the Discord that prevents typing to In-Game users.
     */
    DISCORD_INGAME_MUTE("DISCORD_INGAME_MUTE", "In-Game Channel Muted", "In-Game Channel Mute"),
    /**
     * A mute in the Discord that prevents typing at all.
     */
    DISCORD_FULL_MUTE("DISCORD_FULL_MUTE", "Muted", "Mute"),
    /**
     * A kick from the Discord.
     */
    DISCORD_KICK("DISCORD_KICK", "Kicked", "Kick", false),
    /**
     * Other punishments.
     */
    DISCORD_OTHER("DISCORD_OTHER", "...", "..."),

    /**
     * A mute on the Website that prevents typing to In-Game users.
     */
    WEBSITE_INGAME_MUTE("WEBSITE_INGAME_MUTE", "In-Game Tab Muted", "In-Game Tab Mute"),
    /**
     * A mute on the Website, preventing any form of forum or profile posting.
     */
    WEBSITE_FULL_MUTE("WEBSITE_FULL_MUTE", "Forum Muted", "Forum Mute");

    private static final Map<String, Type> byIdentifier = new HashMap<>();

    static {
        for (Type type : values())
            byIdentifier.put(type.getIdentifier(), type);
    }

    private final String identifier;
    private final String pastTense;
    private final String presentTense;
    private final boolean hasDuration;

    Type(String identifier, String past, String present, boolean hasDuration) {
        this.identifier = identifier;
        this.pastTense = past;
        this.presentTense = present;
        this.hasDuration = hasDuration;
    }

    Type(String identifier, String readable, String present) {
        this(identifier, readable, present, true);
    }

    public String getIdentifier() {
        return identifier;
    }

    public boolean hasDuration() {
        return hasDuration;
    }

    public String getPastTense() {
        return pastTense;
    }

    public String getPresentTense() {
        return presentTense;
    }

    @Nullable
    public static Type fromIdentifier(String identifier) {
        return byIdentifier.getOrDefault(identifier.toUpperCase(), null);
    }

}
