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

		// Cool recipes from @Mysticat_
		shapedRecipe("diamond_horse_armour", new ItemStack(Material.DIAMOND_HORSE_ARMOR), "  A", "ABA", "AAA")
				.setIngredient('A', Material.DIAMOND)
				.setIngredient('B', Material.LEATHER);
		shapedRecipe("gold_horse_armour", new ItemStack(Material.DIAMOND_HORSE_ARMOR), "  A", "ABA", "AAA")
				.setIngredient('A', Material.GOLD_INGOT)
				.setIngredient('B', Material.LEATHER);
		shapedRecipe("iron_horse_armour", new ItemStack(Material.DIAMOND_HORSE_ARMOR), "  A", "ABA", "AAA")
				.setIngredient('A', Material.IRON_INGOT)
				.setIngredient('B', Material.LEATHER);
		shapedRecipe("leather_horse_armour", new ItemStack(Material.DIAMOND_HORSE_ARMOR), "  A", "AAA", "AAA")
				.setIngredient('A', Material.LEATHER);

		shapedRecipe("red_mushroom_block", new ItemStack(Material.RED_MUSHROOM_BLOCK), "AA", "AA")
				.setIngredient('A', Material.RED_MUSHROOM);
		shapedRecipe("brown_mushroom_block", new ItemStack(Material.RED_MUSHROOM_BLOCK), "AA", "AA")
				.setIngredient('A', Material.BROWN_MUSHROOM);
		shapedRecipe("mushroom_stem", new ItemStack(Material.RED_MUSHROOM_BLOCK), "AB", "BA")
				.setIngredient('A', Material.BROWN_MUSHROOM)
				.setIngredient('B', Material.RED_MUSHROOM);


		shapedRecipe(BeanItem.IRON_CHEST, "AAA","ABA","AAA")
				.setIngredient('A', Material.IRON_BLOCK)
				.setIngredient('B', Material.CHEST);

		shapedRecipe(BeanItem.GOLDEN_CHEST, "AAA", "ABA", "AAA")
				.setIngredient('A', Material.GOLD_BLOCK)
				.setIngredient('B', BeanItem.IRON_CHEST.getItemStack());

		shapedRecipe(BeanItem.DIAMOND_CHEST, "AAA", "ABA", "AAA")
				.setIngredient('A', Material.DIAMOND_BLOCK)
				.setIngredient('B', BeanItem.GOLDEN_CHEST.getItemStack());

		shapedRecipe(BeanItem.LIVING_HOPPER, "A A", "ABA", " A ")
				.setIngredient('A', BeanItem.LIVING_METAL_INGOT.getItemStack())
				.setIngredient('B', Material.CHEST);
	}
	
	private ShapedRecipe shapedRecipe(String name, ItemStack result, String...shape) {
		ShapedRecipe sr = new ShapedRecipe(plugin.keyRecipe("shaped_" + name), result);
		sr.shape(shape);
		manager.addRecipe(sr);
		return sr;
	}

	private ShapedRecipe shapedRecipe(BeanItem result, String... shape) {
		return shapedRecipe(result.getIdentifier().toLowerCase(), result.getItemStack(), shape);
	}
	
}
