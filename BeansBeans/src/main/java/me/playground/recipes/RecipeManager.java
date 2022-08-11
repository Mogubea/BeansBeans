package me.playground.recipes;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.inventory.Recipe;

import me.playground.main.Main;

public class RecipeManager {
	private final ArrayList<Recipe> recipes = new ArrayList<Recipe>();
	
	/**
	 * Recipes from this manager will be unlocked via the Bean's Beans Datapack
	 * @param plugin The main server plugin
	 */
	public RecipeManager(Main plugin) {
		new RecipesShaped(plugin, this);
		new RecipesSmithing(plugin, this);
		
		// Register all the recipes after they're done.
		recipes.forEach(recipe -> Bukkit.addRecipe(recipe));
	}
	
	public Recipe addRecipe(Recipe r) {
		recipes.add(r);
		return r;
	}
	
	public void unregisterRecipes() {
		for (Recipe r : recipes)
			Bukkit.removeRecipe(((Keyed)r).getKey());
	}
	
}
