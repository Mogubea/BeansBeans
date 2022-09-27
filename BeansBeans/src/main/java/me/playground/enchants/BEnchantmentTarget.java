package me.playground.enchants;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public enum BEnchantmentTarget {

    /**
     * Allows the Enchantment to be placed on only Shovels
     */
	SHOVEL("Shovels") {
        @Override
        public boolean includes(@NotNull Material item) {
            return item.equals(Material.WOODEN_SHOVEL)
            	|| item.equals(Material.STONE_SHOVEL)
            	|| item.equals(Material.IRON_SHOVEL)
            	|| item.equals(Material.GOLDEN_SHOVEL)
            	|| item.equals(Material.DIAMOND_SHOVEL)
            	|| item.equals(Material.NETHERITE_SHOVEL);
        }
    },

    /**
     * Allows the Enchantment to be placed on only Axes.
     */
	AXE("Axes") {
        @Override
        public boolean includes(@NotNull Material item) {
            return item.equals(Material.WOODEN_AXE)
            	|| item.equals(Material.STONE_AXE)
            	|| item.equals(Material.IRON_AXE)
            	|| item.equals(Material.GOLDEN_AXE)
            	|| item.equals(Material.DIAMOND_AXE)
            	|| item.equals(Material.NETHERITE_AXE);
        }
    },

    /**
     * Allows the Enchantment to be placed on only Pickaxes.
     */
	PICKAXE("Pickaxes") {
        @Override
        public boolean includes(@NotNull Material item) {
            return item.equals(Material.WOODEN_PICKAXE)
            	|| item.equals(Material.STONE_PICKAXE)
            	|| item.equals(Material.IRON_PICKAXE)
            	|| item.equals(Material.GOLDEN_PICKAXE)
            	|| item.equals(Material.DIAMOND_PICKAXE)
            	|| item.equals(Material.NETHERITE_PICKAXE);
        }
    },

    /**
     * Allows the Enchantment to be placed on only hoes.
     */
	HOE("Hoes") {
        @Override
        public boolean includes(@NotNull Material item) {
            return item.equals(Material.WOODEN_HOE)
            	|| item.equals(Material.STONE_HOE)
            	|| item.equals(Material.IRON_HOE)
            	|| item.equals(Material.GOLDEN_HOE)
            	|| item.equals(Material.DIAMOND_HOE)
            	|| item.equals(Material.NETHERITE_HOE);
        }
    },

    /**
     * Allows the Enchantment to be placed on only Shears.
     */
    SHEARS("Shears") {
        @Override
        public boolean includes(@NotNull Material item) {
            return item.equals(Material.SHEARS);
        }
    },

    /**
     * Allows the Enchantment to be placed on only Axes and Pickaxes.
     */
	PICKAXE_AXE("Axes", "Pickaxes") {
        @Override
        public boolean includes(@NotNull Material item) {
            return PICKAXE.includes(item)
                || AXE.includes(item);
        }
    },
	/**
	 * Allows the Enchantment to be placed on all tools except the hoe.
	 */
	TOOL_NO_HOE("Axes", "Pickaxes", "Shovels") {
        @Override
        public boolean includes(@NotNull Material item) {
        	return SHOVEL.includes(item)
                || PICKAXE.includes(item)
                || AXE.includes(item);
        }
    },
	HANDHELD {
        @Override
        public boolean includes(@NotNull Material item) {
        	return HOE.includes(item)
                || SHOVEL.includes(item)
                || PICKAXE.includes(item)
                || AXE.includes(item)
                || BOW.includes(item)
                || CROSSBOW.includes(item)
                || WEAPON.includes(item)
                || FISHING_ROD.includes(item);
        }
    },
    /**
     * Allows the Enchantment to be placed on armor
     */
    ARMOR("All Armour") {
        @Override
        public boolean includes(@NotNull Material item) {
            return ARMOR_FEET.includes(item)
                || ARMOR_LEGS.includes(item)
                || ARMOR_HEAD.includes(item)
                || ARMOR_TORSO.includes(item);
        }
    },

    /**
     * Allows the Enchantment to be placed on feet slot armor
     */
    ARMOR_FEET("Boots") {
        @Override
        public boolean includes(@NotNull Material item) {
            return item.equals(Material.LEATHER_BOOTS)
                || item.equals(Material.CHAINMAIL_BOOTS)
                || item.equals(Material.IRON_BOOTS)
                || item.equals(Material.DIAMOND_BOOTS)
                || item.equals(Material.GOLDEN_BOOTS)
                || item.equals(Material.NETHERITE_BOOTS);
        }
    },

    /**
     * Allows the Enchantment to be placed on leg slot armor
     */
    ARMOR_LEGS("Leggings") {
        @Override
        public boolean includes(@NotNull Material item) {
            return item.equals(Material.LEATHER_LEGGINGS)
                || item.equals(Material.CHAINMAIL_LEGGINGS)
                || item.equals(Material.IRON_LEGGINGS)
                || item.equals(Material.DIAMOND_LEGGINGS)
                || item.equals(Material.GOLDEN_LEGGINGS)
                || item.equals(Material.NETHERITE_LEGGINGS);
        }
    },

    /**
     * Allows the Enchantment to be placed on torso slot armor
     */
    ARMOR_TORSO("Chestplates") {
        @Override
        public boolean includes(@NotNull Material item) {
            return item.equals(Material.LEATHER_CHESTPLATE)
                || item.equals(Material.CHAINMAIL_CHESTPLATE)
                || item.equals(Material.IRON_CHESTPLATE)
                || item.equals(Material.DIAMOND_CHESTPLATE)
                || item.equals(Material.GOLDEN_CHESTPLATE)
                || item.equals(Material.NETHERITE_CHESTPLATE);
        }
    },

    /**
     * Allows the Enchantment to be placed on head slot armor
     */
    ARMOR_HEAD("Helmets") {
        @Override
        public boolean includes(@NotNull Material item) {
            return item.equals(Material.LEATHER_HELMET)
                || item.equals(Material.CHAINMAIL_HELMET)
                || item.equals(Material.DIAMOND_HELMET)
                || item.equals(Material.IRON_HELMET)
                || item.equals(Material.GOLDEN_HELMET)
                || item.equals(Material.TURTLE_HELMET)
                || item.equals(Material.NETHERITE_HELMET);
        }
    },

    /**
     * Allows the Enchantment to be placed on weapons (swords)
     */
    WEAPON("Swords") {
        @Override
        public boolean includes(@NotNull Material item) {
            return item.equals(Material.WOODEN_SWORD)
                || item.equals(Material.STONE_SWORD)
                || item.equals(Material.IRON_SWORD)
                || item.equals(Material.DIAMOND_SWORD)
                || item.equals(Material.GOLDEN_SWORD)
                || item.equals(Material.NETHERITE_SWORD);
        }
    },

    /**
     * Allows the Enchantment to be placed on tools (spades, pickaxe, axes)
     */
    TOOL("Axes", "Pickaxes", "Shovels", "Hoes") {
        @Override
        public boolean includes(@NotNull Material item) {
        	return HOE.includes(item)
                || SHOVEL.includes(item)
                || PICKAXE.includes(item)
                || AXE.includes(item);
        }
    },

    /**
     * Allows the Enchantment to be placed on tools (spades, pickaxe, axes)
     */
    TOOL_AND_SWORD("Swords", "Axes", "Pickaxes", "Shovels", "Hoes") {
        @Override
        public boolean includes(@NotNull Material item) {
            return HOE.includes(item)
                    || SHOVEL.includes(item)
                    || PICKAXE.includes(item)
                    || AXE.includes(item)
                    || WEAPON.includes(item);
        }
    },

    /**
     * Allows the Enchantment to be placed on bows.
     */
    BOW("Bows") {
        @Override
        public boolean includes(@NotNull Material item) {
            return item.equals(Material.BOW);
        }
    },

    /**
     * Allows the Enchantment to be placed on fishing rods.
     */
    FISHING_ROD("Fishing Rods") {
        @Override
        public boolean includes(@NotNull Material item) {
            return item.equals(Material.FISHING_ROD);
        }
    },

    /**
     * Allows the enchantment to be placed on items with durability.
     */
    BREAKABLE("Anything") {
        @Override
        public boolean includes(@NotNull Material item) {
            return item.getMaxDurability() > 0 && item.getMaxStackSize() == 1;
        }
    },

    /**
     * Allows the enchantment to be placed on wearable items.
     */
    WEARABLE("Armour", "Wearables") {
        @Override
        public boolean includes(@NotNull Material item) {
            return ARMOR.includes(item)
                    || item.equals(Material.ELYTRA)
                    || item.equals(Material.CARVED_PUMPKIN)
                    || item.equals(Material.JACK_O_LANTERN)
                    || item.equals(Material.SKELETON_SKULL)
                    || item.equals(Material.WITHER_SKELETON_SKULL)
                    || item.equals(Material.ZOMBIE_HEAD)
                    || item.equals(Material.PLAYER_HEAD)
                    || item.equals(Material.CREEPER_HEAD)
                    || item.equals(Material.DRAGON_HEAD);
        }
    },

    /**
     * Allow the Enchantment to be placed on tridents.
     */
    TRIDENT("Tridents") {
        @Override
        public boolean includes(@NotNull Material item) {
            return item.equals(Material.TRIDENT);
        }
    },

    /**
     * Allow the Enchantment to be placed on crossbows.
     */
    CROSSBOW("Crossbows") {
        @Override
        public boolean includes(@NotNull Material item) {
            return item.equals(Material.CROSSBOW);
        }
    },

    /**
     * Allow the Enchantment to be placed on vanishing items.
     */
    VANISHABLE("Durables") {
        @Override
        public boolean includes(@NotNull Material item) {
            return BREAKABLE.includes(item) || (WEARABLE.includes(item) && !item.equals(Material.ELYTRA)) || item.equals(Material.COMPASS);
        }
    };

    private final String[] validTargetStrings;

    BEnchantmentTarget(String... validTargetStrings) {
        this.validTargetStrings = validTargetStrings;
    }

    public String[] getValidTargetStrings() {
        return validTargetStrings;
    }

    /**
     * Check whether this target includes the specified item.
     *
     * @param item The item to check
     * @return True if the target includes the item
     */
    public abstract boolean includes(@NotNull Material item);

    /**
     * Check whether this target includes the specified item.
     *
     * @param item The item to check
     * @return True if the target includes the item
     */
    public boolean includes(@NotNull ItemStack item) {
        return includes(item.getType());
    }
}
