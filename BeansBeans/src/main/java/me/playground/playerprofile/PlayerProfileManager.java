package me.playground.playerprofile;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import me.playground.data.Datasource;
import me.playground.main.Main;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class PlayerProfileManager {

    private final Main plugin;
    private final PlayerProfileDatasource datasource;

    private final LoadingCache<UUID, PlayerProfile> profileCache = CacheBuilder.from("maximumSize=500,expireAfterAccess=6m").build(
            new CacheLoader<>() {
                public PlayerProfile load(@NotNull UUID playerUUID) {
                    return Datasource.getOrMakeProfile(playerUUID);
                }
            });

    public PlayerProfileManager(Main plugin) {
        this.plugin = plugin;
        this.datasource = new PlayerProfileDatasource(plugin, this);

        datasource.loadAll();
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

    /**
     * Attempts to get or create a {@link PlayerProfile} for the provided player {@link UUID}.
     * @return The {@link PlayerProfile} associated with the provided player {@link UUID}.
     * @throws RuntimeException - If there was a problem getting or creating a {@link PlayerProfile} for this player.
     */
    @NotNull
    protected PlayerProfile getPlayerProfile(@NotNull UUID uuid) {
        try {
            return profileCache.get(uuid);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return The {@link PlayerProfile} associated with the provided database ID.
     * @throws RuntimeException - If there was a problem getting a {@link PlayerProfile} with the provided database ID.
     */
    @Nullable
    protected static PlayerProfile getProfile(int databaseId) {
        ProfileStore store = ProfileStore.fromIfExists(databaseId);
        return store != null ? getProfile(store.getUniqueId()) : null;
    }

    /**
     * @return The {@link PlayerProfile} associated with the provided player name.
     * @throws RuntimeException - If there was a problem getting a {@link PlayerProfile} with the provided player name.
     */
    @Nullable
    protected static PlayerProfile getProfile(String username) {
        ProfileStore store = ProfileStore.from(username, true);
        return store != null ? getProfile(store.getUniqueId()) : null;
    }

    /**
     * Helper static method for {@link PlayerProfileManager#getPlayerProfile(UUID)}.
     * @return The {@link PlayerProfile} associated with the provided player {@link UUID}.
     * @throws RuntimeException - If there was a problem getting or creating a {@link PlayerProfile} for this player.
     */
    @NotNull
    protected static PlayerProfile getProfile(@NotNull OfflinePlayer p) {
        return getProfile(p.getUniqueId());
    }

    /**
     * Helper static method for {@link PlayerProfileManager#getPlayerProfile(UUID)}.
     * @return The {@link PlayerProfile} associated with the provided player {@link UUID}.
     * @throws RuntimeException - If there was a problem getting or creating a {@link PlayerProfile} for this player.
     */
    @NotNull
    protected static PlayerProfile getProfile(@NotNull UUID uuid) {
        return Main.getInstance().getProfileManager().getPlayerProfile(uuid);
    }

    /**
     * @return The amount of {@link PlayerProfile}s currently loaded.
     */
    public long getSize() {
        return profileCache.size();
    }

}
