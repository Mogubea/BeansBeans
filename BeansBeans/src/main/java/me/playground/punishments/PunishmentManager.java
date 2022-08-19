package me.playground.punishments;

import com.google.common.cache.CacheLoader;
import me.playground.main.Main;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public class PunishmentManager {

    private final Main plugin;
    private final PunishmentDatasource datasource;

    public PunishmentManager(Main plugin) {
        this.plugin = plugin;
        this.datasource = new PunishmentDatasource(this, plugin);
    }

    protected PunishmentDatasource getDatasource() {
        return datasource;
    }

    public Punishment banPlayer(OfflinePlayer target, Category category, long duration) {
        return banPlayer(target.getUniqueId(), 0, duration, category, Type.BAN.getDefaultMessage());
    }

    public Punishment banPlayer(UUID uuid, int punisher, long duration, Category category, String reason) {
        return null;
    }


}
