package me.playground.items;

import me.playground.playerprofile.PlayerProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class BItemPickyAxe extends BItemDurable {

	protected final NamespacedKey KEY_UNTIL_NEW_FAVOURITE = key("PICKY_COUNTER"); // byte
	protected final NamespacedKey KEY_CURRENT_FAVOURITE = key("PICKY_FAVOURITE"); // byte
	protected final NamespacedKey KEY_TOTAL_FAVOURITES = key("PICKY_TOTAL"); // int

	//private AttributeModifier modifier = new AttributeModifier(getUniqueId(Attribute.GENERIC_MAX_HEALTH), Attribute.GENERIC_MAX_HEALTH.translationKey(), 1, AttributeModifier.Operation.ADD_NUMBER);

	private final Material[] potentialFavs = {Material.OAK_LOG, Material.SPRUCE_LOG, Material.BIRCH_LOG, Material.JUNGLE_LOG, Material.ACACIA_LOG, Material.DARK_OAK_LOG};
	private final TextColor[] favColours = {TextColor.color(0xffcf94), TextColor.color(0x987835), TextColor.color(0xffffcc), TextColor.color(0xdd886d), TextColor.color(0x888811)};

	protected BItemPickyAxe(int numeric, String identifier, String name, Material material, ItemRarity rarity, int modelDataInt, int durability) {
		super(numeric, identifier, name, material, rarity, modelDataInt, durability);
		addAttribute(Attribute.GENERIC_ATTACK_SPEED, -3.3, EquipmentSlot.HAND);
		addAttribute(Attribute.GENERIC_ATTACK_DAMAGE, 9, EquipmentSlot.HAND);
		for (Material m : potentialFavs)
			addRepairMaterial(m, 1f);

		setDefaultLore(
				Component.text("An awfully picky and indecisive", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
				Component.text("axe which grants various", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
				Component.text("bonuses when listened to.", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
	}
	
	@Override
	public List<TextComponent> getCustomLore(ItemStack item) {
		final PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
		final byte favourite = pdc.getOrDefault(KEY_CURRENT_FAVOURITE, PersistentDataType.BYTE, (byte)0);

		final List<TextComponent> lore = new ArrayList<>(getDefaultLore());
		lore.add(Component.empty());
		lore.add(Component.text("Current Favourite: ", NamedTextColor.GRAY).append(Component.translatable(potentialFavs[favourite].translationKey(), favColours[favourite])).decoration(TextDecoration.ITALIC, false));
		return lore;
	}

	@Override
	public void onBlockMined(BlockBreakEvent e) {
		if (e.getBlock().hasMetadata("placed")) return;
		if (!e.getBlock().getType().name().endsWith("_LOG")) return;

		final ItemStack item = e.getPlayer().getInventory().getItemInMainHand();
		final PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
		final Material favourite = potentialFavs[pdc.getOrDefault(KEY_CURRENT_FAVOURITE, PersistentDataType.BYTE, (byte)0)];

		item.editMeta(meta -> {
			int untilNew = meta.getPersistentDataContainer().getOrDefault(KEY_UNTIL_NEW_FAVOURITE, PersistentDataType.BYTE, (byte)50) - 1;
			if (untilNew < 1) {
				byte oldFavourite = pdc.getOrDefault(KEY_CURRENT_FAVOURITE, PersistentDataType.BYTE, (byte)0);

				untilNew = 20 + getRandom().nextInt(41);
				byte newFavourite = (byte)getRandom().nextInt(potentialFavs.length);
				while (newFavourite == oldFavourite)
					newFavourite = (byte)getRandom().nextInt(potentialFavs.length);
				pdc.set(KEY_CURRENT_FAVOURITE, PersistentDataType.BYTE, newFavourite);
				pdc.set(KEY_TOTAL_FAVOURITES, PersistentDataType.INTEGER, pdc.getOrDefault(KEY_TOTAL_FAVOURITES, PersistentDataType.INTEGER, 0) + 1);
				BeanItem.formatItem(item);
				e.getPlayer().sendMessage(Component.text("\u00a7b\u00a7l\u2191\u00a77 Your ").append(item.displayName().hoverEvent(item.asHoverEvent())).append(Component.text("\u00a77 has changed its mind on what it prefers to chop...")));
			}

			pdc.set(KEY_UNTIL_NEW_FAVOURITE, PersistentDataType.BYTE, (byte)untilNew);
		});

		if (e.getBlock().getType() == favourite)
			onFavouriteMined(e);
	}

	protected void onFavouriteMined(BlockBreakEvent e) {
		final ItemStack item = e.getPlayer().getInventory().getItemInMainHand();
		final PlayerProfile pp = PlayerProfile.from(e.getPlayer());
		int random = getRandom().nextInt(50000);

		// 1 in 50,000
		if (true/*random > 49998*/) {
			BeanItem.convert(item, BeanItem.PICKIER_AXE);
			e.getPlayer().sendMessage(Component.text("\u00a7b\u00a7l\u2191\u00a77 Your ").append(item.displayName().hoverEvent(item.asHoverEvent())).append(Component.text("\u00a77 has become even pickier!")));
		} else if (random > 49997) {
			pp.addToBalance(10000, "Picky Axe");
		} else if (random > 49992) {
			//pp.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).addModifier();
			//pp.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).addModifier(new AttributeModifier(""));
		}
	}
}
