package me.playground.items.tracking;

import me.playground.items.BeanItem;
import me.playground.main.Main;
import me.playground.playerprofile.stats.DirtyLong;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * A class simply designed to keep track of how many times a certain item has been manifested into the server by a player.
 * It is easier to store a local copy of these values as well as firing asynchronous requests rather than doing synchronous
 * requests every time an item is created.<br><br>
 * <b>Items spawned in by Administrators should never be considered officially manifested unless a flag is specified.</b>
 *
 * @apiNote Honestly this is mostly here for tracking fun and the cool graphs that can be made.
 * @author Mogubean
 */
public class ItemTrackingManager {

    private boolean isReloading;
    private final Map<String, Map<ManifestationReason, DirtyLong>> itemCreationCounter = new HashMap<>();
    private final Map<String, Map<DemanifestationReason, DirtyLong>> itemRemovalCounter = new HashMap<>();
//  private final Map<Material, Long> vanillaCreationCounter = new HashMap<>();
    private final ItemTrackingDatasource datasource;

    public ItemTrackingManager(Main pl) {
        this.datasource = new ItemTrackingDatasource(pl, this);
        datasource.loadAll(); // Load the current counter for each item
    }

    /**
     * For use by {@link ItemTrackingDatasource#saveAll()}.
     * @return All the current manifestation counts.
     */
    protected Map<String, Map<ManifestationReason, DirtyLong>> getAllManifestations() {
        return itemCreationCounter;
    }

    /**
     * For use by {@link ItemTrackingDatasource#saveAll()}.
     * @return All the current demanifestation counts.
     */
    protected Map<String, Map<DemanifestationReason, DirtyLong>> getAllDemanifestations() {
        return itemRemovalCounter;
    }

    /**
     * Grab the amount of times this item has been manifested from the specific {@link ManifestationReason}.
     */
    public long getTimesManifested(@NotNull String identifier, @NotNull ManifestationReason reason) {
        Map<ManifestationReason, DirtyLong> map = itemCreationCounter.getOrDefault(identifier, null);
        if (map != null)
            return map.containsKey(reason) ? map.get(reason).getValue() : 0L;
        return 0L;
    }

    /**
     * Grab the amount of times this item has been demanifested from the specific {@link DemanifestationReason}.
     */
    public long getTimesDemanifested(@NotNull String identifier, @NotNull DemanifestationReason reason) {
        Map<DemanifestationReason, DirtyLong> map = itemRemovalCounter.getOrDefault(identifier, null);
        if (map != null)
            return map.containsKey(reason) ? map.get(reason).getValue() : 0L;
        return 0L;
    }

    /**
     * Sets the local amount of times that an item has been manifested.<br><br>
     * <b>This should only ever be called during the {@link ItemTrackingDatasource#loadAll()} call.</b> And because of this, will never be flagged dirty.
     */
    protected void setTimesManifested(@NotNull String identifier, @NotNull ManifestationReason reason, long count) {
        if (!itemCreationCounter.containsKey(identifier))
            itemCreationCounter.put(identifier, new HashMap<>());

        Map<ManifestationReason, DirtyLong> map = itemCreationCounter.get(identifier);
        map.put(reason, new DirtyLong(count));
    }

    /**
     * Increments the amount of times this {@link BeanItem} has been created via the specified {@link ManifestationReason}. Any and all changes
     * to the local manifestation counter will be saved to the database during the {@link ItemTrackingDatasource#saveAll()} call.
     * @return new value
     */
    public long incrementManifestationCount(@NotNull BeanItem item, @NotNull ManifestationReason reason, long count) {
        incrementManifestationCount(item.getIdentifier(), ManifestationReason.TOTAL, count);
        return incrementManifestationCount(item.getIdentifier(), reason, count);
    }

    /**
     * Increments the amount of times this {@link ItemStack} has been created via the specified {@link ManifestationReason}. Any and all changes
     * to the local manifestation counter will be saved to the database during the {@link ItemTrackingDatasource#saveAll()} call.
     * @return new value
     */
    public long incrementManifestationCount(@NotNull ItemStack item, @NotNull ManifestationReason reason, long count) {
        incrementManifestationCount(BeanItem.getIdentifier(item), ManifestationReason.TOTAL, count);
        return incrementManifestationCount(BeanItem.getIdentifier(item), reason, count);
    }

    private long incrementManifestationCount(String identifier, @NotNull ManifestationReason reason, long count) {
        if (!itemCreationCounter.containsKey(identifier))
            itemCreationCounter.put(identifier, new HashMap<>());

        Map<ManifestationReason, DirtyLong> map = itemCreationCounter.get(identifier);
        if (!map.containsKey(reason))
            map.put(reason, new DirtyLong(0L));

        DirtyLong dirtyLong = map.get(reason);
        dirtyLong.setValue(dirtyLong.getValue() + count, true);
        return dirtyLong.getValue();
    }

    /**
     * Sets the local amount of times that an item has been demanifested.<br><br>
     * <b>This should only ever be called during the {@link ItemTrackingDatasource#loadAll()} call.</b> And because of this, will never be flagged dirty.
     */
    protected void setTimesDemanifested(String identifier, @NotNull DemanifestationReason reason, long count) {
        if (!itemRemovalCounter.containsKey(identifier))
            itemRemovalCounter.put(identifier, new HashMap<>());

        Map<DemanifestationReason, DirtyLong> map = itemRemovalCounter.get(identifier);
        map.put(reason, new DirtyLong(count));
    }

    /**
     * Increments the amount of times this {@link ItemStack} has been destroyed via the specified {@link DemanifestationReason}. Any and all changes
     * to the local manifestation counter will be saved to the database during the {@link ItemTrackingDatasource#saveAll()} call.
     * @return new value
     */
    public long incrementDemanifestationCount(@NotNull ItemStack item, @NotNull DemanifestationReason reason, long count) {
        String identifier = BeanItem.getIdentifier(item);
        incrementDemanifestationCount(identifier, DemanifestationReason.TOTAL, count);
        return incrementDemanifestationCount(identifier, reason, count);
    }

    private long incrementDemanifestationCount(String identifier, @NotNull DemanifestationReason reason, long count) {
        if (!itemRemovalCounter.containsKey(identifier))
            itemRemovalCounter.put(identifier, new HashMap<>());

        Map<DemanifestationReason, DirtyLong> map = itemRemovalCounter.get(identifier);
        if (!map.containsKey(reason))
            map.put(reason, new DirtyLong(0L));

        DirtyLong dirtyLong = map.get(reason);
        dirtyLong.setValue(dirtyLong.getValue() + count, true);
        return dirtyLong.getValue();
    }

    /**
     * Resets all the local creation counts.<br><br>
     * <b>This should only ever be called during the {@link ItemTrackingDatasource#loadAll()}</b> call.
     */
    protected void clearCounter() {
        this.itemCreationCounter.clear();
        this.itemRemovalCounter.clear();
    }

}
