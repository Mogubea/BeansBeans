package me.playground.recipes;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

import me.playground.items.BeanItem;
import me.playground.main.Main;

public class RecipesShaped {
	
	private final Main plugin;
	private final RecipeManager manager;
	
	public RecipesShaped(Main plugin, RecipeManager manager) {
		this.plugin = plugin;
		this.manager = manager;

		shapedRecipe(BeanItem.IRON_CHEST, "AAA","ABA","AAA")
				.setIngredient('A', Material.IRON_BLOCK)
				.setIngredient('B', Material.CHEST);

		shapedRecipe(BeanItem.GOLDEN_CHEST, "AAA", "ABA", "AAA")
				.setIngredient('A', Material.GOLD_BLOCK)
				.setIngredient('B', BeanItem.IRON_CHEST.getOriginalStack());

		shapedRecipe(BeanItem.DIAMOND_CHEST, "AAA", "ABA", "AAA")
				.setIngredient('A', Material.DIAMOND_BLOCK)
				.setIngredient('B', BeanItem.GOLDEN_CHEST.getOriginalStack());

		shapedRecipe(BeanItem.LIVING_HOPPER, "A A", "ABA", " A ")
				.setIngredient('A', BeanItem.LIVING_METAL_INGOT.getOriginalStack())
				.setIngredient('B', Material.CHEST);
	}
	
	private ShapedRecipe shapedRecipe(String name, ItemStack result, String...shape) {
		ShapedRecipe sr = new ShapedRecipe(plugin.keyRecipe(name), result);
		sr.shape(shape);
		manager.addRecipe(sr);
		return sr;
	}

	private ShapedRecipe shapedRecipe(BeanItem result, String... shape) {
		return shapedRecipe(result.getIdentifier().toLowerCase(), result.getOriginalStack(), shape);
	}
	
}
