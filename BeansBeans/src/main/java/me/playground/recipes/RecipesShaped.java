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
		
		shapedRecipe("shop_stand_1", BeanItem.SHOP_STAND.getOriginalStack(), "GGG","GEG","WgW")
				.setIngredient('G', Material.GLASS)
				.setIngredient('E', Material.EMERALD)
				.setIngredient('g', Material.GOLD_BLOCK)
				.setIngredient('W', Material.DARK_OAK_LOG);
		
	}
	
	private ShapedRecipe shapedRecipe(String name, ItemStack result, String...shape) {
		ShapedRecipe sr = new ShapedRecipe(plugin.keyRecipe(name), result);
		sr.shape(shape);
		manager.addRecipe(sr);
		return sr;
	}
	
}
