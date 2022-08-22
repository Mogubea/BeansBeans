package me.playground.recipes;

import me.playground.items.BeanItem;
import me.playground.main.Main;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;

public class RecipesShapeless {

	private final Main plugin;
	private final RecipeManager manager;

	public RecipesShapeless(Main plugin, RecipeManager manager) {
		this.plugin = plugin;
		this.manager = manager;

		ItemStack metalChunk = BeanItem.LIVING_METAL_CHUNK.getOriginalStack();
		metalChunk.setAmount(16);
		shapelessRecipe(BeanItem.LIVING_METAL_INGOT).addIngredient(4, metalChunk);

		ItemStack metalIngot = BeanItem.LIVING_METAL_INGOT.getOriginalStack();
		metalIngot.setAmount(16);
		shapelessRecipe(BeanItem.LIVING_METAL_BLOCK).addIngredient(4, metalIngot);

		shapelessRecipe("up_quality_wheat", new ItemStack(Material.WHEAT)).addIngredient(9, BeanItem.POOR_QUALITY_WHEAT.getOriginalStack());
		shapelessRecipe("up_quality_carrot", new ItemStack(Material.CARROT)).addIngredient(9, BeanItem.POOR_QUALITY_CARROT.getOriginalStack());
		shapelessRecipe("up_quality_potato", new ItemStack(Material.POTATO)).addIngredient(9, BeanItem.POOR_QUALITY_POTATO.getOriginalStack());
		shapelessRecipe("up_quality_beetroot", new ItemStack(Material.BEETROOT)).addIngredient(9, BeanItem.POOR_QUALITY_BEETROOT.getOriginalStack());

	}
	
	private ShapelessRecipe shapelessRecipe(String name, ItemStack result) {
		ShapelessRecipe sr = new ShapelessRecipe(plugin.keyRecipe(name), result);
		manager.addRecipe(sr);
		return sr;
	}

	private ShapelessRecipe shapelessRecipe(BeanItem result) {
		return shapelessRecipe(result.getIdentifier().toLowerCase(), result.getOriginalStack());
	}
	
}
