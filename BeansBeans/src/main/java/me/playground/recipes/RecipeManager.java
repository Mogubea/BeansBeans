package me.playground.recipes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import me.playground.main.Main;

public class RecipeManager {
	private final ArrayList<Recipe> recipes = new ArrayList<>();
	
	// Makes it easier to grab the cooked versions of items, which is done throughout the server in random places.
	private final Map<Material, Material> vanillaSmeltingRecipes;

	public RecipeManager(Main plugin) {
		new RecipesShaped(plugin, this);
		new RecipesShapeless(plugin, this);
		new RecipesSmithing(plugin, this);
		
		Map<Material, Material> furnaceRecipes = new HashMap<>();
		Iterator<Recipe> recipes = Bukkit.recipeIterator();
		while(recipes.hasNext()) {
			Recipe recipe = recipes.next();
			if (!(recipe instanceof FurnaceRecipe fRecipe)) continue;

			furnaceRecipes.put(fRecipe.getInput().getType(), fRecipe.getResult().getType());
		}
		
		// For some reason, Charcoal recipes are hard coded.
		for (Material material : Material.values())
			if (material.name().endsWith("_LOG") || material.name().endsWith("_WOOD"))
				furnaceRecipes.put(material, Material.CHARCOAL);
		
		this.vanillaSmeltingRecipes = Map.copyOf(furnaceRecipes);
		
		// Register all the recipes after they're done.
		this.recipes.forEach(Bukkit::addRecipe);
	}
	
	public Recipe addRecipe(Recipe r) {
		recipes.add(r);
		return r;
	}
	
	public void unregisterRecipes() {
		for (Recipe r : recipes)
			Bukkit.removeRecipe(((Keyed)r).getKey());
	}
	
	public ItemStack getCookedVersion(ItemStack toCook) {
		Material newVersion = vanillaSmeltingRecipes.get(toCook.getType());
		if (newVersion == null) return toCook;
		if (newVersion == toCook.getType()) return toCook;
		
		ItemStack ack = toCook.clone();
		ack.setType(newVersion);
		return ack;
	}
	
	public Material getCookedVersion(Material toCook) {
		return vanillaSmeltingRecipes.getOrDefault(toCook, toCook);
	}
	
	public boolean hasCookedVersion(Material toCook) {
		return vanillaSmeltingRecipes.containsKey(toCook);
	}
}
