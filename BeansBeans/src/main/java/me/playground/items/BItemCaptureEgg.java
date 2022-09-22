package me.playground.items;

import me.playground.main.Main;
import me.playground.ranks.Permission;
import me.playground.regions.Region;
import me.playground.regions.flags.Flags;
import me.playground.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.ArrayList;
import java.util.Map;

public class BItemCaptureEgg extends BeanItem {
	
	protected BItemCaptureEgg(int numeric, String identifier, String name, Material material, ItemRarity rarity, int modelDataInt) {
		super(numeric, identifier, name, material, rarity, modelDataInt);
		setDefaultLore(
				Component.text("Right-click a creature to contain", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
				Component.text("it inside of this egg.", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
	}

	@Override
	public boolean onInteract(PlayerInteractEvent e) {
		e.setUseItemInHand(Event.Result.DENY);

		/*if (e.getClickedBlock() instanceof Container && !e.getPlayer().isBlocking()) return false; // Allow them to try and open containers.

		// Since it's a Ghast Egg by default, we don't really want players to be spawning Ghasts when right-clicking a block with this egg.
		e.setCancelled(true);
		return true;*/
		return false;
	}

	@Override
	public boolean onEntityInteract(PlayerInteractEntityEvent e) {
		if (!(e.getRightClicked() instanceof LivingEntity entity)) return false;
		final Player p = e.getPlayer();

		// Capture Checks
		if (!p.hasPermission(Permission.BYPASS_CAPTURECHECKS)) {
			final Component entityName = Component.translatable(entity.getType().translationKey());

			// Check if the Entity is valid for regular players to own in an egg.
			if (!isValidEntity(e.getRightClicked().getType())) {
				return false;
			}

			// Check if the Entity is owned by somebody else.
			boolean isMine = false; // A required check as we want people to be able to capture their own pets inside other people's regions.
			if (entity instanceof Tameable tameable) {
				if (tameable.getOwnerUniqueId() != null) {
					isMine = tameable.getOwnerUniqueId().equals(p.getUniqueId());
					if (!isMine) {
						p.sendActionBar(Component.text("This ").append(entityName.append(Component.text(" doesn't belong to you."))).color(NamedTextColor.RED));
						return true;
					}
				}
			}

			// Check if it's a horse with a Chest.
			if (entity instanceof ChestedHorse horse && horse.isCarryingChest()) {
				p.sendActionBar(Component.text("This ").append(entityName.append(Component.text(" is carrying a chest and cannot be picked up."))).color(NamedTextColor.RED));
				return true;
			}

			if (!isMine) {
				// Check if the Entity is passive and is inside a flagged region.
				if (entity instanceof Animals) {
					Region region = Main.getRegionManager().getRegion(p.getLocation());
					if (!region.doesPlayerBypass(p, Flags.PROTECT_ANIMALS)) {
						p.sendActionBar(Component.text("This ").append(entityName.append(Component.text(" is protected by the region."))).color(NamedTextColor.RED));
						return true;
					}
				}

				// Check if the Entity is a Merchant and is inside a flagged region.
				else if (entity instanceof Merchant) {
					Region region = Main.getRegionManager().getRegion(p.getLocation());
					if (!region.doesPlayerBypass(p, Flags.PROTECT_ANIMALS) && !region.doesPlayerBypass(p, Flags.VILLAGER_ACCESS)) {
						p.sendActionBar(Component.text("This ").append(entityName.append(Component.text(" is protected by the region."))).color(NamedTextColor.RED));
						return true;
					}
				}
			}
		}

		ItemStack item = p.getInventory().getItem(e.getHand());
		
		if (e.getHand() == EquipmentSlot.HAND)
			p.swingMainHand();
		else
			p.swingOffHand();

        ItemStack egg;
        net.minecraft.world.item.ItemStack craftEgg;

		Entity nmsEntity = ((CraftEntity) entity).getHandle();
		Location location = entity.getLocation();
		CompoundTag entityNBT = new CompoundTag();
		nmsEntity.saveWithoutId(entityNBT);

		try {
			Material eggType = Material.valueOf(entity.getType().name() + "_SPAWN_EGG");
			craftEgg = CraftItemStack.asNMSCopy(new ItemStack(eggType));

			// Remove tags that are not necessary or are intrusive to the respawning part of the Entity.
			entityNBT.remove("Pos");
			entityNBT.remove("UUID");
			entityNBT.remove("Air");
			entityNBT.remove("FallDistance");
			entityNBT.remove("Fire");
			entityNBT.remove("Motion");
			entityNBT.remove("Passengers");

			CompoundTag tag = new CompoundTag();
			tag.put("EntityTag", entityNBT);
			craftEgg.setTag(tag);
			egg = CraftItemStack.asBukkitCopy(craftEgg);
		} catch (Exception e1) {
			return true;
		}

		// Add information about the captured Creature.
		egg.editMeta(meta -> {
			ArrayList<Component> lore = new ArrayList<>();
			Component entityName = entity.customName();
			if (entityName != null)
				lore.add(Component.text(" • Name: ", NamedTextColor.GRAY).append(entityName).decoration(TextDecoration.ITALIC, false));
			lore.add(Component.text(" • Health: ", NamedTextColor.GRAY).append(Component.text((int)entity.getHealth() + "/" + (int)entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() + " \u2764", NamedTextColor.RED)).decoration(TextDecoration.ITALIC, false));
			if (entity instanceof Sheep sheep) {
				DyeColor color = sheep.getColor();
				if (color != null) // Unsure how this is nullable but alright.
					lore.add(Component.text(" • Colour: ", NamedTextColor.GRAY).append(Component.text(Utils.firstCharUpper(color.name()), TextColor.color(color.getColor().asRGB()))).decoration(TextDecoration.ITALIC, false));
			} else if (entity instanceof Cat cat) {
				Cat.Type catType = cat.getCatType();
				lore.add(Component.text(" • Type: ", NamedTextColor.GRAY).append(Component.text(Utils.firstCharUpper(catType.name()), NamedTextColor.WHITE)).decoration(TextDecoration.ITALIC, false));
			} else if (entity instanceof Fox fox) {
				Fox.Type foxType = fox.getFoxType();
				lore.add(Component.text(" • Type: ", NamedTextColor.GRAY).append(Component.text(Utils.firstCharUpper(foxType.name()), foxType == Fox.Type.RED ? NamedTextColor.RED : NamedTextColor.WHITE)).decoration(TextDecoration.ITALIC, false));
			} else if (entity instanceof Horse horse && entity.getType() == EntityType.HORSE) {
				Horse.Style horseStyle = horse.getStyle();
				Horse.Color horseColor = horse.getColor();
				lore.add(Component.text(" • Colour: ", NamedTextColor.GRAY).append(Component.text(Utils.firstCharUpper(horseColor.name()), NamedTextColor.WHITE)).decoration(TextDecoration.ITALIC, false));
				lore.add(Component.text(" • Style: ", NamedTextColor.GRAY).append(Component.text(Utils.firstCharUpper(horseStyle.name()), NamedTextColor.WHITE)).decoration(TextDecoration.ITALIC, false));
			} else if (entity instanceof Axolotl axolotl) {
				Axolotl.Variant type = axolotl.getVariant();
				lore.add(Component.text(" • Type: ", NamedTextColor.GRAY).append(Component.text(Utils.firstCharUpper(type.name()), NamedTextColor.WHITE)).decoration(TextDecoration.ITALIC, false));
			} else if (entity instanceof Villager villager) {
				Villager.Type type = villager.getVillagerType();
				int xp = villager.getVillagerExperience();
				int lvl = villager.getVillagerLevel();
				lore.add(Component.text(" • Type: ", NamedTextColor.GRAY).append(Component.text(Utils.firstCharUpper(type.name()), NamedTextColor.WHITE)).decoration(TextDecoration.ITALIC, false));
				if (xp > 0 || lvl > 1) {
					Villager.Profession profession = villager.getProfession();
					lore.add(Component.text(" • Profession: ", NamedTextColor.GRAY).append(Component.translatable(profession.translationKey(), NamedTextColor.AQUA)).decoration(TextDecoration.ITALIC, false));
					lore.add(Component.text(" • Level: ", NamedTextColor.GRAY).append(Component.text(lvl, NamedTextColor.GREEN)).decoration(TextDecoration.ITALIC, false));
				}
				addMerchantLore(lore, villager);
			} else if (entity instanceof ZombieVillager zombieVillager) {
				Villager.Type type = zombieVillager.getVillagerType();
				Villager.Profession profession = zombieVillager.getVillagerProfession();
				lore.add(Component.text(" • Type: ", NamedTextColor.GRAY).append(Component.text(Utils.firstCharUpper(type.name()), NamedTextColor.WHITE)).decoration(TextDecoration.ITALIC, false));
				if (profession != null && profession != Villager.Profession.NONE)
					lore.add(Component.text(" • Profession: ", NamedTextColor.GRAY).append(Component.translatable(profession.translationKey(), NamedTextColor.AQUA)).decoration(TextDecoration.ITALIC, false));
			}

			meta.lore(lore);
		});

		formatItem(egg);

		entity.remove(); // Remove the Entity

		// Add to the player's inventory (hand if only 1 egg previously in hand) or drop to the ground
		if (item.subtract().getAmount() < 1 || p.getGameMode() == GameMode.CREATIVE && item.getAmount() == 1)
			p.getInventory().setItem(e.getHand(), egg);
		else if (!p.getInventory().addItem(egg).isEmpty())
			p.getWorld().dropItemNaturally(location, item);

		try { // Play Entity Death Sound because it's funny
			Sound sound = Sound.valueOf("ENTITY_" + entity.getType().name() + "_DEATH");
			p.getWorld().playSound(location, sound, 0.35F, 1.1F);
		} catch (Exception ignored) {
		}

		p.getWorld().playSound(location, Sound.BLOCK_ENDER_CHEST_OPEN, 0.45F, 1.25F);
		p.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, location.add(0, entity.getHeight() / 2, 0), 10, 0.3, entity.getHeight() / 2, 0.3);
		return true;
	}

	/**
	 * Check if the Entity type trying to be captured is valid.
	 */
	private boolean isValidEntity(EntityType type) {
		return switch (type) {
			case WANDERING_TRADER, ARMOR_STAND, PLAYER, WITHER, ENDER_DRAGON, ELDER_GUARDIAN, GHAST/*, WARDEN*/ -> false;
			default -> true;
		};
	}

	/**
	 * Add merchant information about the captured Merchant.
	 */
	private void addMerchantLore(ArrayList<Component> lore, Merchant merchant) {
		lore.add(Component.text("Trades: ", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
		int profitTrades = 0;
		for (MerchantRecipe recipe : merchant.getRecipes()) {
			ItemStack item = recipe.getResult();

			if (item.getType() == Material.EMERALD || item.getType() == Material.EMERALD_BLOCK) {
				profitTrades++;
				continue;
			}

			Component name;

			if (item.getType() == Material.ENCHANTED_BOOK) {
				name = Component.text("Enchanted Book (", NamedTextColor.GRAY);
				EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemMeta();
				int amt = meta.getStoredEnchants().size();
				for (Map.Entry<Enchantment, Integer> enchant : meta.getStoredEnchants().entrySet()) {
					name = name.append(enchant.getKey().displayName(enchant.getValue()));
					if (--amt <= 0) continue;
					name = name.append(Component.text(", ", NamedTextColor.GRAY));
				}
				name = name.append(Component.text(")", NamedTextColor.GRAY));
			} else {
				BeanItem custom = BeanItem.from(item);
				name = custom == null ? Component.translatable(item.translationKey()) : custom.getDisplayName();
			}

			lore.add(Component.text(" \u25b6 ", NamedTextColor.GRAY).append(Component.text(item.getAmount(), NamedTextColor.WHITE))
					.append(Component.text("x ", NamedTextColor.DARK_GRAY)).append(name).colorIfAbsent(NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false));
		}

		if (profitTrades > 0)
			lore.add(Component.text(" \u25b6 " + profitTrades + " Emerald Trades", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
	}
}
