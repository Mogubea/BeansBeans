package me.playground.items.values;

import me.playground.items.BItemDurable;
import me.playground.main.Main;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Manages the sell value of items on Bean's Beans
 */
public class ItemValueManager {

    private final Main plugin;
    private final ItemValueDatasource datasource;
    private final ItemValueLogger logger;
    private final ItemValues values;
    private final ItemValueTemp tempVals;

    public ItemValueManager(Main plugin) {
        this.plugin = plugin;
        this.datasource = new ItemValueDatasource(plugin, this);
        this.values = new ItemValues(this);
        this.tempVals = new ItemValueTemp(this);
        this.logger = new ItemValueLogger(this);

        datasource.loadAll();
    }

    protected ItemValues getItemValues() {
        return values;
    }

    /**
     * Gets the current value of the singular item either designated by an Administrator or by recipe calculation.<br><br>
     * <b>This does not take into consideration Stack Size, Refinement Level, Potion Effects or Enchantments.</b>
     * @return the coin value.
     */
    public float getValue(@NotNull ItemStack itemStack) {
        return values.getItemValue(itemStack);
    }

    /**
     * Gets the current value of the singular item either designated by an Administrator or by recipe calculation.<br><br>
     * <b>This does not take into consideration Stack Size, Refinement Level, Potion Effects or Enchantments.</b>
     * @return the coin value.
     */
    public float getValue(@NotNull String identifier) {
        return values.getItemValue(identifier);
    }

    /**
     * Returns the total value of the provided {@link ItemStack}, taking into consideration the
     * <b>Refinement Level</b>, <b>Potion Effects</b> and <b>Enchantments</b>.
     * @param stackSize Whether to consider the Stack Size of the {@link ItemStack}.
     * @return The true total value of the provided {@link ItemStack}.
     */
    public float getTotalValue(@NotNull ItemStack itemStack, boolean stackSize) {
        float value = values.getItemValue(itemStack);

        value += tempVals.getExtraValue(itemStack);
        value *= 1 + (0.25 * BItemDurable.getRefinementTier(itemStack));

        if (stackSize)
            value *= itemStack.getAmount();

        return value;
    }

    /**
     * @return True if a value has been manually set to this item.
     */
    public boolean isEnforced(@NotNull String identifier) {
        return values.getEnforcedValue(identifier) > -1;
    }

    /**
     * @return True if a value has been calculated by the server for this item.
     */
    public boolean isCalculated(@NotNull String identifier) {
        return values.getCalculatedValue(identifier) > -1;
    }

    /**
     * Updates the coin value of the provided {@link ItemStack}, flagging it as dirty. Value cannot be less than 0.<br><br>
     * <b>Call {@link #calculateItemValues()} to update the item values of those who's recipes involve the updated item.</b>
     * @return The old value
     */
    public float setValue(@NotNull ItemStack itemStack, float newValue, int playerId) { return values.setItemValue(itemStack, newValue, playerId); }

    /**
     * Updates the coin value of the provided item identifier, flagging it as dirty. Value cannot be less than 0.<br><br>
     * <b>Call {@link #calculateItemValues()} to update the item values of those who's recipes involve the updated item.</b>
     * @return The old value
     */
    public float setValue(@NotNull String identifier, float newValue, int playerId) { return values.setItemValue(identifier, newValue, playerId); }

    public void calculateItemValues() {
        values.calculateItemValues();
    }

    public long getLastRecalculation() {
        return values.getLastRecalculation();
    }

    protected ItemValueDatasource getDatasource() {
        return datasource;
    }

    protected ItemValueLogger getLogger() {
        return logger;
    }

    protected Main getPlugin() {
        return plugin;
    }

}
