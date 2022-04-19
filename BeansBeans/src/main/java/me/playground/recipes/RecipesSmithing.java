package me.playground.recipes;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.RecipeChoice.MaterialChoice;
import org.bukkit.inventory.SmithingRecipe;

import me.playground.items.BeanItem;
import me.playground.main.Main;

public class RecipesSmithing {
	
	private final Main plugin;
	private final RecipeManager manager;
	
	public RecipesSmithing(Main plugin, RecipeManager manager) {
		this.plugin = plugin;
		this.manager = manager;
		
		recipe("rose_gold_helmet", BeanItem.ROSE_GOLD_HELMET.getOriginalStack(), new MaterialChoice(Material.GOLDEN_HELMET), new MaterialChoice(Material.COPPER_INGOT), true);
		recipe("rose_gold_chestplate", BeanItem.ROSE_GOLD_CHESTPLATE.getOriginalStack(), new MaterialChoice(Material.GOLDEN_CHESTPLATE), new MaterialChoice(Material.COPPER_INGOT), true);
		recipe("rose_gold_leggings", BeanItem.ROSE_GOLD_LEGGINGS.getOriginalStack(), new MaterialChoice(Material.GOLDEN_LEGGINGS), new MaterialChoice(Material.COPPER_INGOT), true);
		recipe("rose_gold_boots", BeanItem.ROSE_GOLD_BOOTS.getOriginalStack(), new MaterialChoice(Material.GOLDEN_BOOTS), new MaterialChoice(Material.COPPER_INGOT), true);
		recipe("gilded_blackstone", new ItemStack(Material.GILDED_BLACKSTONE), new MaterialChoice(Material.GOLD_ORE), new MaterialChoice(Material.BLACKSTONE), false);
	}
	
	private SmithingRecipe recipe(String name, ItemStack result, RecipeChoice base, RecipeChoice addition, boolean copyNbt) {
		SmithingRecipe sr = new SmithingRecipe(plugin.keyRecipe(name), result, base, addition, copyNbt);
		manager.addRecipe(sr);
		return sr;
	}
	
}
