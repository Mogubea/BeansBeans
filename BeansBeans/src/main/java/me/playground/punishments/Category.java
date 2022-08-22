package me.playground.punishments;

import javax.annotation.Nullable;
import java.util.*;

@Deprecated
public enum Category {

    OTHER("OTHER", "utilising alternate accounts to bypass punishments"),

    DISRESPECT("DISRESPECT", "disrespectful activity", "participating in racist activity", "participating in sexist activity",
            "harassing members of the community", "harassing members of staff", "refusing to cooperate with members of staff", "pretending to be a member of staff"),

    EXPLOITING("EXPLOITING", "exploiting bugs with malicious intent", "abusing unbalanced vanilla game mechanics", "exploiting duplication bugs"),

    CHEATING("CHEATING", "utilising third party applications to gain an unfair advantage", "utilising third party modifications to gain an unfair advantage",
            "utilising scripts to gain an unfair advantage", "accompanying a player who's cheating to gain an unfair advantage"),

    SPAMMING("SPAMMING", "spamming", "refusing/inability to communicate in English in global channels", "advertising other community servers without permission",
            "advertising other minecraft servers without permission", "flooding chat"),

    GRIEFING("GRIEFING", "griefing regioned territory", "stealing from regioned territory", "griefing and stealing from regioned territory"),

    INAPPROPRIATE("INAPPROPRIATE", "discussing and/or sharing pornographic material", "requesting personal information", "encouraging acts of self harm",
            "discussing and/or encouraging inappropriate topics", "forcing roleplay onto others", "sharing inappropriate item names", "inappropriate signs");


    private static final Map<String, Category> byIdentifier = new HashMap<>();

    static {
        for (Category type : values())
            byIdentifier.put(type.getIdentifier(), type);
    }

    private final String identifier;
    private final List<String> defaultMsg = new ArrayList<>();

    Category(String identifier, String... defaultMsg) {
        this.identifier = identifier;
        this.defaultMsg.addAll(Arrays.asList(defaultMsg));
    }


    public String getIdentifier() {
        return identifier;
    }

    /**
     * Example filler messages for punishing a player
     */
    public List<String> getDefaultMessages() {
        return defaultMsg;
    }

    @Nullable
    public static Category fromIdentifier(String identifier) {
        return byIdentifier.getOrDefault(identifier.toUpperCase(), null);
    }

}
