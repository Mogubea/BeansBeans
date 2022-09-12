package me.playground.celestia.logging;

import me.playground.data.LoggingManager;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class CelestiaManager extends LoggingManager<CelestiaLog> {

    public CelestiaManager(Main plugin) {
        super(new CelestiaDatasource(plugin));
    }

    public void log(Player player, CelestiaAction action, String information) {
        log(PlayerProfile.from(player).getId(), action, player.getLocation(), information);
    }

    public void log(Player player, CelestiaAction action, Location location, String information) {
        addLog(new CelestiaLog(PlayerProfile.from(player).getId(), action, location, information));
    }

    public void log(int playerId, CelestiaAction action, Location location, String information) {
        addLog(new CelestiaLog(playerId, action, location, information));
    }

}
