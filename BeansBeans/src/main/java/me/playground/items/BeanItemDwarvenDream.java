package me.playground.items;

import org.bukkit.inventory.ItemStack;

public class BeanItemDwarvenDream extends BeanItem {

	protected BeanItemDwarvenDream(int numeric, String identifier, String name, ItemStack item, ItemRarity rarity, int modelDataInt) {
		super(numeric, identifier, name, item, rarity, modelDataInt);
		// TODO Auto-generated constructor stub
	}
	
	/*private final int requirement;
	private final String upgradesInto;
	
	protected BeanItemDwarvenDream(String identifier, String name, Material material, ItemRarity rarity, int modelDataInt, int maxDurability, int upgradeReq, String upgradesInto) {
		super(identifier, name, new ItemStack(material), rarity, modelDataInt, maxDurability);
		this.requirement = upgradeReq;
		this.upgradesInto = upgradesInto;
		BeanItem.updateItemLore(originalStack);
	}
	
	@Override
	public Multimap<Attribute, AttributeModifier> getAttributes() {
		final Multimap<Attribute, AttributeModifier> attributes = HashMultimap.create();
		attributes.put(Attribute.GENERIC_ATTACK_DAMAGE, new AttributeModifier(UUID.randomUUID(), "generic.attack_damage", 1, Operation.ADD_NUMBER, EquipmentSlot.HAND));
		attributes.put(Attribute.GENERIC_ATTACK_SPEED, new AttributeModifier(UUID.randomUUID(), "generic.attack_speed", 0.1, Operation.ADD_NUMBER, EquipmentSlot.HAND));
		return attributes;
	}
	
	@Override
	public ArrayList<Component> getCustomLore(ItemStack item) {
		final ArrayList<Component> lore = new ArrayList<Component>();
		lore.addAll(Arrays.asList(
				Component.text("\u00a77A Dwarf's potential only grows with every"),
				Component.text("\u00a77block mined! This pickaxe \u00a7dupgrades itself"),
				Component.text("\u00a7dand a random \u00a7rNon-OP Enchantment\u00a77 after").decoration(TextDecoration.ITALIC, false).colorIfAbsent(BeanColor.ENCHANT_OP),
				Component.text("\u00a77it has been used to harvest "+requirement+" ores..."),
				Component.empty(),
				Component.text("\u00a77Ores harvested: \u00a7e"+item.getItemMeta().getPersistentDataContainer().getOrDefault(KEY_COUNTER, PersistentDataType.INTEGER, 0))));
		return lore;
	}
	
	@Override
	public void onBlockMined(BlockBreakEvent e) {
		final Block b = e.getBlock();
		if (!b.hasMetadata("placed") && b.getType().toString().endsWith("ORE")) {
			ItemStack dream = e.getPlayer().getInventory().getItemInMainHand();
			final ItemMeta meta = dream.getItemMeta();
			final PersistentDataContainer container = meta.getPersistentDataContainer();
			final int ack = container.getOrDefault(KEY_COUNTER, PersistentDataType.INTEGER, 0) + 1;
			
			if (ack >= requirement) {
				final String oldName = ((TextComponent)meta.displayName()).content();
				final int oldDura = container.get(KEY_DURABILITY, PersistentDataType.INTEGER);
				final boolean keepOldName = !oldName.equals(BeanItem.from(dream).getDisplayName().content());
				
				dream = BeanItem.fromString(upgradesInto);
				dream.addUnsafeEnchantments(meta.getEnchants());
				final ItemMeta newMeta = dream.getItemMeta();
				final PersistentDataContainer newContainer = newMeta.getPersistentDataContainer();
				
				if (keepOldName)
					newMeta.displayName(Component.text(oldName));
				
				newContainer.set(KEY_COUNTER, PersistentDataType.INTEGER, ack);
				newContainer.set(KEY_DURABILITY, PersistentDataType.INTEGER, oldDura * 2);
				
				dream.setItemMeta(newMeta);
				for (Entry<Enchantment, Integer> ench : dream.getEnchantments().entrySet()) {
					if (ench.getKey().getMaxLevel() >= ench.getValue()) {
						dream.addUnsafeEnchantment(ench.getKey(), ench.getValue() + 1);
						break;
					}
				}
				BeanItem.updateItemLore(dream);
				e.getPlayer().sendMessage(Component.text("\u00a7dYour ").append(toHover(dream)).append(Component.text("\u00a7d has upgraded!")));
				e.getPlayer().getInventory().setItemInMainHand(dream);
			} else {
				container.set(KEY_COUNTER, PersistentDataType.INTEGER, ack);
				ArrayList<Component> lore = (ArrayList<Component>) meta.lore();
				lore.set(lore.size() - 4, Component.text("\u00a77Ores harvested: \u00a7e"+ack));
				meta.lore(lore);
				dream.setItemMeta(meta);
			}
		}
	}
	
	private Component toHover(ItemStack item) {
		if (item == null || item.getType() == Material.AIR)
			return Component.empty();
		
		final ItemMeta meta = item.getItemMeta();
		final Component displayName = meta.hasDisplayName() ? meta.displayName() : Component.text(item.getI18NDisplayName());
		
		return displayName.hoverEvent(item.asHoverEvent());
	}
	*/
}
