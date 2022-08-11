package me.playground.items.values;

import me.playground.items.BeanItem;
import me.playground.main.Main;
import me.playground.playerprofile.stats.DirtyFloat;
import me.playground.playerprofile.stats.DirtyInteger;
import org.bukkit.inventory.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * A class containing flat monetary values which are used for when players sell items to NPCs.
 * These values are purposefully undervalued so players are incentivised to use the Grand Exchange.
 */
public class ItemValues {

    private final ItemValueManager manager;
    private long lastRecalculation;

    /**
     * Values that were loaded from the datasource and are set in stone by Administrators.
     */
    private Map<String, DirtyFloat> enforcedItemValues = new HashMap<>();

    /**
     * Values that were calculated during a calculation call or from the datasource if none occurred this cycle.
     */
    private Map<String, DirtyFloat> calculatedItemValues = new HashMap<>();

    public ItemValues(ItemValueManager manager) {
        this.manager = manager;
    }

    /**
     * Set the current enforced item values and calls {@link #calculateItemValues()}.
     */
    protected void updateAllValues(@NotNull Map<String, DirtyFloat> values, @NotNull Map<String, DirtyFloat> calculatedValues) {
        this.enforcedItemValues = values;
        this.calculatedItemValues = calculatedValues;
        calculateItemValues(); // Re-Calculate to detect any new changes from the last boot.
    }

    @NotNull
    protected Map<String, DirtyFloat> getEnforced() {
        return enforcedItemValues;
    }

    @NotNull
    protected Map<String, DirtyFloat> getCalculated() {
        return calculatedItemValues;
    }

//    public int getEnforcedSize() {
//        return enforcedItemValues.size();
//    }

    public int getCalculatedSize() {
        return calculatedItemValues.size();
    }

    /**
     * Recalculates the values of items that haven't been strictly set by scanning through existing {@link org.bukkit.inventory.Recipe}s.
     */
    public void calculateItemValues() {
        long then = System.currentTimeMillis();
        Map<String, Float> oldestValues = new HashMap<>();

        // Loop 5 times to ensure changes apply to deeper recipes too.
        for (int x = -1; ++x < 5;) {

            // Store old values to check for dirty changes
            Map<String, Float> alreadyCheckedBefore = new HashMap<>();

            // Check for Stone Cutting Recipes first as they tend to have very efficient recipes.
            manager.getPlugin().getServer().recipeIterator().forEachRemaining(recipe -> {
                if (!(recipe instanceof StonecuttingRecipe stoneRecipe)) return;
                ItemStack result = recipe.getResult();

                if (result.getType().isAir()) return; // Unlikely
                if (getEnforcedValue(result) != -1) return; // Do not attempt to override existing enforced values

                float valueOfInput = getItemValue(stoneRecipe.getInput());

                String identifier = BeanItem.getIdentifier(result);
                DirtyFloat dd = calculatedItemValues.getOrDefault(identifier, new DirtyFloat(-1F));
                float oldValue = dd.getValue();
                float newValue = valueOfInput / stoneRecipe.getResult().getAmount();
                newValue = Math.round(newValue * 100F) / 100F; // Round to 2 decimals

                // Only allow for value decreases if the result has already been found before, this way we can guarantee the correct LOWEST value
                if (!alreadyCheckedBefore.containsKey(identifier) || newValue < oldValue) {
                    dd.setValue(newValue);

                    alreadyCheckedBefore.putIfAbsent(identifier, oldValue);
                    calculatedItemValues.put(identifier, dd);
                }
            });

            // Crafting Recipes.
            manager.getPlugin().getServer().recipeIterator().forEachRemaining(recipe -> {
                ItemStack result = recipe.getResult();

                if (result.getType().isAir()) return; // Unlikely
                if (getEnforcedValue(result) != -1) return; // Do not attempt to override existing enforced values
                Collection<ItemStack> inputs = null;
                float valueMultiplier = 1;

                if (recipe instanceof ShapelessRecipe shapelessRecipe) { inputs = shapelessRecipe.getIngredientList(); }
                else if (recipe instanceof ShapedRecipe shapedRecipe) { inputs = shapedRecipe.getIngredientMap().values(); }
                else if (recipe instanceof SmithingRecipe smithingRecipe) { inputs = List.of(smithingRecipe.getBase().getItemStack(), smithingRecipe.getAddition().getItemStack()); valueMultiplier = 1.2F; } // TODO: Consider all Recipe Choices
                else if (recipe instanceof CookingRecipe<?> cookingRecipe) { inputs = List.of(cookingRecipe.getInput()); valueMultiplier = 1.05F; }

                if (inputs == null) return;

                float valueOfInputs = 0;

                for (ItemStack input : inputs)
                    if (input != null)
                        valueOfInputs += getItemValue(input);

                valueOfInputs *= valueMultiplier;

                String identifier = BeanItem.getIdentifier(result);
                DirtyFloat dd = calculatedItemValues.getOrDefault(identifier, new DirtyFloat(-1F));
                float oldValue = dd.getValue();
                float newValue = valueOfInputs / result.getAmount();
                newValue = Math.round(newValue * 100F) / 100F; // Round to 2 decimals

                // Only allow for value decreases if the result has already been found before, this way we can guarantee the correct LOWEST value
                if (!alreadyCheckedBefore.containsKey(identifier) || newValue < oldValue) {
                    dd.setValue(newValue);

                    alreadyCheckedBefore.putIfAbsent(identifier, oldValue);
                    calculatedItemValues.put(identifier, dd);
                }
            });

            if (x == 0)
                oldestValues = alreadyCheckedBefore;
        }



        DirtyInteger dirty = new DirtyInteger(0);
        oldestValues.forEach((identifier, preCalcValue) -> {
            float postCalcValue = getItemValue(identifier);
            if (preCalcValue == postCalcValue) return;

            dirty.addToValue(1);
            new ItemValueLog(manager.getLogger(), identifier, preCalcValue, postCalcValue, 0, false); // Log calculated change
        });

        manager.getPlugin().getSLF4JLogger().info("Calculated " + getCalculatedSize() + " Item Values in " + (System.currentTimeMillis()-then) + "ms");
        manager.getPlugin().getSLF4JLogger().info(dirty.getValue() + " of these values have changed from the last calculation.");
        lastRecalculation = then;
    }

    /**
     * Updates the coin value of the provided {@link ItemStack}, flagging it as {@link DirtyFloat#isDirty()}. Value cannot be less than 0.<br><br>
     * <b>Call {@link #calculateItemValues()} to update the item values of those who's recipes involve the updated item.</b>
     * @return The old value
     */
    public float setItemValue(@NotNull ItemStack itemStack, float newValue, int playerId) {
        return setItemValue(BeanItem.getIdentifier(itemStack), newValue, playerId);
    }

    /**
     * Updates the coin value of the provided item identifier, flagging it as {@link DirtyFloat#isDirty()}. Value cannot be less than 0.<br><br>
     * <b>Call {@link #calculateItemValues()} to update the item values of those who's recipes involve the updated item.</b>
     * @return The old value
     */
    public float setItemValue(@NotNull String identifier, float newValue, int playerId) {
        DirtyFloat dd = enforcedItemValues.get(identifier);
        float oldValue;

        if (dd == null) { // Wasn't in the list before, put it in now.
            dd = new DirtyFloat(-1F);
            enforcedItemValues.put(identifier, dd);
            oldValue = calculatedItemValues.containsKey(identifier) ? calculatedItemValues.get(identifier).getValue() : -1F;
        } else {
            oldValue = dd.getValue();
        }

        if (newValue < 0) newValue = 0;
        dd.setValue(Math.round(newValue * 100F) / 100F); // Round to 2 decimals

        calculatedItemValues.remove(identifier); // No need to have it in here anymore.
        if (dd.isDirty()) new ItemValueLog(manager.getLogger(), identifier, oldValue, newValue, playerId, true);
        return oldValue;
    }

    /**
     * Gets the enforced value if there is one.
     * @return the enforced coin value or -1 if none is enforced.
     */
    protected float getEnforcedValue(@NotNull ItemStack itemStack) {
        return getEnforcedValue(BeanItem.getIdentifier(itemStack));
    }

    /**
     * Gets the enforced value if there is one.
     * @return the enforced coin value or -1 if none is enforced.
     */
    protected float getEnforcedValue(@NotNull String identifier) {
        DirtyFloat dd = enforcedItemValues.get(identifier);

        // No reason to be in the calculatedItemValues map if it has an enforced value
        if (dd != null) calculatedItemValues.remove(identifier);

        return dd == null ? -1 : dd.getValue();
    }

    /**
     * Gets the calculated value if there is one.
     * @return the calculated coin value or -1 if nothing was calculated.
     */
    protected float getCalculatedValue(@NotNull String identifier) {
        DirtyFloat dd = calculatedItemValues.get(identifier);
        return dd == null ? -1 : dd.getValue();
    }

    /**
     * Gets the current value of the item either designated by an Administrator or by recipe calculation.<br><br>
     * <b>This does not take into consideration Stack Size, Refinement Level, Potion Effects or Enchantments.</b>
     * @return the coin value.
     */
    public float getItemValue(@NotNull ItemStack itemStack) {
        return getItemValue(BeanItem.getIdentifier(itemStack));
    }

    /**
     * Gets the current value of the item either designated by an Administrator or by recipe calculation.<br><br>
     * <b>This does not take into consideration Stack Size, Refinement Level, Potion Effects or Enchantments.</b>
     * @return the coin value.
     */
    public float getItemValue(@NotNull String identifier) {
        DirtyFloat dd = enforcedItemValues.get(identifier);
        if (dd != null) return dd.getValue();

        dd = calculatedItemValues.get(identifier);
        return dd == null ? 0 : dd.getValue();
    }

    /**
     * Gets the current value of the item either designated by an Administrator or by recipe calculation.<br><br>
     * <b>This does not take into consideration Stack Size, Refinement Level, Potion Effects or Enchantments.</b>
     * @return the coin value.
     */
    public static float getValue(@NotNull ItemStack itemStack) {
        return Main.getInstance().getItemValueManager().getValue(itemStack);
    }

    /**
     * Gets the current value of the item either designated by an Administrator or by recipe calculation.<br><br>
     * <b>This does not take into consideration Stack Size, Refinement Level, Potion Effects or Enchantments.</b>
     * @return the coin value.
     */
    public static float getValue(@NotNull String identifier) {
        return Main.getInstance().getItemValueManager().getValue(identifier);
    }

    /**
     * Returns the total value of the provided {@link ItemStack}, taking into consideration the
     * <b>Refinement Level</b>, <b>Potion Effects</b> and <b>Enchantments</b>.
     * @param stackSize Whether to consider the Stack Size of the {@link ItemStack}.
     * @return The true total value of the provided {@link ItemStack}.
     */
    public static float getTotalValue(@NotNull ItemStack itemStack, boolean stackSize) {
        return Main.getInstance().getItemValueManager().getTotalValue(itemStack, stackSize);
    }

    protected long getLastRecalculation() {
        return this.lastRecalculation;
    }

}
