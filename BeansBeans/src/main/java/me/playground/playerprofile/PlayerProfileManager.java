package me.playground.playerprofile;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import me.playground.data.Datasource;
import me.playground.main.Main;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PlayerProfileManager {

    private final Main plugin;
    private final PlayerProfileDatasource datasource;

    private final LoadingCache<UUID, PlayerProfile> profileCache = CacheBuilder.from("maximumSize=500,expireAfterAccess=6m")
            .build(
                    new CacheLoader<>() {
                        public PlayerProfile load(@NotNull UUID playerUUID) { // if the key doesn't exist, request it via this method
                            PlayerProfile prof = Datasource.getOrMakeProfile(playerUUID);
                            if (prof != null)
                                if (prof.isOnline()) // assign bar player if online
                                    prof.getSkills().setBarPlayer();
                            return prof;
                        }
                    });

    public PlayerProfileManager(Main plugin) {
        this.plugin = plugin;
        this.datasource = new PlayerProfileDatasource(plugin, this);
    }

    protected PlayerProfileDatasource getDatasource() {
        return datasource;
    }

    protected LoadingCache<UUID, PlayerProfile> getCache() {
        return profileCache;
    }

    protected Main getPlugin() {
        return plugin;
    }

}
