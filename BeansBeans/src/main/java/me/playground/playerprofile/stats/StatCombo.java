package me.playground.playerprofile.stats;

import me.playground.playerprofile.PlayerProfile;

public record StatCombo(StatType type, String stat) {

    public int getStatOf(PlayerProfile pp) {
        return pp.getStat(type, stat);
    }

}
