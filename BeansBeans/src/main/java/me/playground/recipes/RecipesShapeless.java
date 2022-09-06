package me.playground.recipes;

import me.playground.items.BeanItem;
import me.playground.main.Main;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

public class RecipesShapeless {

	private final Main plugin;
	private final RecipeManager manager;

	public RecipesShapeless(Main plugin, RecipeManager manager) {
		this.plugin = plugin;
		this.manager = manager;

		// Cool recipe from @Mysticat_
		ItemStack splashWaterBottle = new ItemStack(Material.SPLASH_POTION);
		splashWaterBottle.editMeta(meta -> ((PotionMeta)meta).setBasePotionData(new PotionData(PotionType.WATER)));
		shapelessRecipe("experience_bottle", new ItemStack(Material.EXPERIENCE_BOTTLE)).addIngredient(splashWaterBottle).addIngredient(Material.DRAGON_BREATH).addIngredient(Material.GHAST_TEAR);

		/*ItemStack metalChunk = BeanItem.LIVING_METAL_CHUNK.getItemStack();
		metalChunk.setAmount(16);
		shapelessRecipe(BeanItem.LIVING_METAL_INGOT).addIngredient(4, metalChunk);

		ItemStack metalIngot = BeanItem.LIVING_METAL_INGOT.getItemStack();
		metalIngot.setAmount(16);
		shapelessRecipe(BeanItem.LIVING_METAL_BLOCK).addIngredient(4, metalIngot);*/

		shapelessRecipe("quality_wheat", new ItemStack(Material.WHEAT)).addIngredient(9, BeanItem.POOR_QUALITY_WHEAT.getItemStack());
		shapelessRecipe("quality_carrot", new ItemStack(Material.CARROT)).addIngredient(9, BeanItem.POOR_QUALITY_CARROT.getItemStack());
		shapelessRecipe("quality_potato", new ItemStack(Material.POTATO)).addIngredient(9, BeanItem.POOR_QUALITY_POTATO.getItemStack());
		shapelessRecipe("quality_beetroot", new ItemStack(Material.BEETROOT)).addIngredient(9, BeanItem.POOR_QUALITY_BEETROOT.getItemStack());

	}
	
	private ShapelessRecipe shapelessRecipe(String name, ItemStack result) {
		ShapelessRecipe sr = new ShapelessRecipe(plugin.keyRecipe("shapeless_" + name), result);
		manager.addRecipe(sr);
		return sr;
	}

	private ShapelessRecipe shapelessRecipe(BeanItem result) {
		return shapelessRecipe(result.getIdentifier().toLowerCase(), result.getItemStack());
	}
	
}
