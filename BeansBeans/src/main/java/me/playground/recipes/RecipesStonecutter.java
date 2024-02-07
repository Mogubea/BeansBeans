package me.playground.recipes;

import me.playground.main.Main;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.StonecuttingRecipe;

public class RecipesStonecutter {

	private final Main plugin;
	private final RecipeManager manager;

	public RecipesStonecutter(Main plugin, RecipeManager manager) {
		this.plugin = plugin;
		this.manager = manager;

		// Stone -> Cobblestone Recipes
		recipe(new ItemStack(Material.COBBLESTONE), Material.STONE);
		recipe(new ItemStack(Material.COBBLESTONE_SLAB, 2), Material.STONE);
		recipe(new ItemStack(Material.COBBLESTONE_STAIRS), Material.STONE);
		recipe(new ItemStack(Material.COBBLESTONE_WALL), Material.STONE);

		// Deepslate -> Deepslate Recipes
		recipe(new ItemStack(Material.CHISELED_DEEPSLATE), Material.DEEPSLATE);
		recipe(new ItemStack(Material.COBBLED_DEEPSLATE), Material.DEEPSLATE);
		recipe(new ItemStack(Material.COBBLED_DEEPSLATE_SLAB, 2), Material.DEEPSLATE);
		recipe(new ItemStack(Material.COBBLED_DEEPSLATE_STAIRS), Material.DEEPSLATE);
		recipe(new ItemStack(Material.COBBLED_DEEPSLATE_WALL), Material.DEEPSLATE);
		recipe(new ItemStack(Material.DEEPSLATE_BRICK_SLAB, 2), Material.DEEPSLATE);
		recipe(new ItemStack(Material.DEEPSLATE_BRICK_STAIRS), Material.DEEPSLATE);
		recipe(new ItemStack(Material.DEEPSLATE_BRICK_WALL), Material.DEEPSLATE);
		recipe(new ItemStack(Material.DEEPSLATE_BRICKS), Material.DEEPSLATE);
		recipe(new ItemStack(Material.DEEPSLATE_TILE_SLAB, 2), Material.DEEPSLATE);
		recipe(new ItemStack(Material.DEEPSLATE_TILE_STAIRS), Material.DEEPSLATE);
		recipe(new ItemStack(Material.DEEPSLATE_TILE_WALL), Material.DEEPSLATE);
		recipe(new ItemStack(Material.DEEPSLATE_TILES), Material.DEEPSLATE);
		recipe(new ItemStack(Material.POLISHED_DEEPSLATE_SLAB, 2), Material.DEEPSLATE);
		recipe(new ItemStack(Material.POLISHED_DEEPSLATE_STAIRS), Material.DEEPSLATE);
		recipe(new ItemStack(Material.POLISHED_DEEPSLATE_WALL), Material.DEEPSLATE);
		recipe(new ItemStack(Material.POLISHED_DEEPSLATE), Material.DEEPSLATE);
	}
	
	private void recipe(ItemStack result, Material source) {
		StonecuttingRecipe sr = new StonecuttingRecipe(plugin.keyRecipe("stonecutter_" + source.name().toLowerCase() + "_to_" + result.getType().name().toLowerCase()), result, source);
		manager.addRecipe(sr);
	}
	
}
