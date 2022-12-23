package me.playground.menushop;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import me.playground.items.lore.Lore;
import me.playground.items.tracking.DemanifestationReason;
import me.playground.ranks.Permission;
import me.playground.ranks.Rank;
import me.playground.skills.Grade;
import me.playground.skills.SkillData;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.playground.items.BeanItem;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import me.playground.skills.Skill;
import me.playground.utils.BeanColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PurchaseOption {
	
	private static final DecimalFormat df = new DecimalFormat("#,###");
	
	private int id;
	private MenuShop menuShop;
	private int coinCost;
	private int crystalCost;
	private int xpCost;
	private final ItemStack displayItem;
	private final ItemStack baseDisplayItem;
	private final BeanItem customItem;
	private Lore description;
	private String purchaseWord = "purchase";
	private String greySubtext;
	private DemanifestationReason demanifestationReason = DemanifestationReason.PURCHASE;
	private final Map<ItemStack, Integer> materialCost = new LinkedHashMap<>();
	private final Map<@NotNull Skill, Integer> skillRequirement = new LinkedHashMap<>();

	private boolean complimentary;
	private boolean enabled;
	private boolean dirty;

	private boolean hasMultiPurchase;
	
	private boolean hasCustomName;
	
	public PurchaseOption(ItemStack itemDisplay, Component displayName, @Nullable Lore lore) {
		this.displayItem = itemDisplay == null ? new ItemStack(Material.PAPER) : itemDisplay.clone();
		this.baseDisplayItem = displayItem.clone();
		customItem = BeanItem.from(itemDisplay);

		if (displayName != null)
			setDisplayName(displayName, false);
		else
			this.displayItem.editMeta(meta -> meta.displayName(Component.text(displayItem.getAmount(), TextColor.color(0xcccccc)).append(Component.text("x ", NamedTextColor.DARK_GRAY))
					.append(customItem != null ? customItem.getDisplayName() : Component.translatable(displayItem.translationKey(), BeanItem.getItemRarity(displayItem).getColour())).decoration(TextDecoration.ITALIC, false)));

		if (lore != null)
			setDescription(lore, false);

		enabled = true;
	}
	
	public PurchaseOption(Material displayItem, Component displayName) {
		this(new ItemStack(displayItem), displayName, null);
	}
	
	public PurchaseOption(ItemStack displayItem, Component displayName) {
		this(displayItem, displayName, null);
	}
	
	public PurchaseOption(Material displayItem, Component displayName, Lore lore) {
		this(new ItemStack(displayItem), displayName, lore);
	}

	/**
	 * If the {@link Player} can purchase this product (see {@link #canPurchase(Player)}), trigger all the necessary cost methods and return true.
	 * @param p The player purchasing the product.
	 * @return If the product was purchased
	 */
	public boolean purchase(@NotNull Player p) {
		return purchase(p, true);
	}

	/**
	 * If the {@link Player} can purchase this product (see {@link #canPurchase(Player)}), trigger all the necessary cost methods and return true.
	 * @param p The player purchasing the product.
	 * @param sound Play the default success or fail sound.
	 * @return Whether the product was purchased or not.
	 */
	public boolean purchase(@NotNull Player p, boolean sound) {
		if (complimentary) return false;
		if (!enabled) return false;

		PlayerProfile pp = PlayerProfile.from(p);
		boolean override = p.getGameMode() == GameMode.CREATIVE && pp.hasPermission(Permission.BYPASS_COSTS_CREATIVE);
		boolean can = false;

		if (override) { // Avoid all costs if overridden.
			can = true;
		} else if (canPurchase(p)) {
			pp.addToCrystals(-crystalCost);
			pp.addToBalance(-coinCost, "PurchasableProduct");
			p.setLevel(p.getLevel() - xpCost);
			ItemStack[] toRemove = new ItemStack[materialCost.size()];
			int x = 0;
			for (Entry<ItemStack, Integer> entry : materialCost.entrySet()) {
				ItemStack a = entry.getKey().clone();
				a.setAmount(entry.getValue());
				toRemove[x++] = a;
				Main.getInstance().getItemTrackingManager().incrementDemanifestationCount(a, demanifestationReason, a.getAmount());
			}
			p.getInventory().removeItem(toRemove);
			can = true;
		}

		if (can) {
			if (sound)
				p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.35F, 1.0F);
			return true;
		}
		
		if (sound)
			p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.4F, 0.8F);
		return false;
	}

	/**
	 * Gets the item that will be shown to the player viewing this product.
	 * @param p The player to check the resources of.
	 * @return The item.
	 */
	@NotNull
	public ItemStack getDisplayItem(@NotNull Player p) {
		return getDisplayItem(p, null, true, true);
	}

	/**
	 * Gets the item that will be shown to the player viewing this product.
	 * @param p The player to check the resources of.
	 * @param components Any additional components prior to listing the costs.
	 * @return The item.
	 */
	@NotNull
	public ItemStack getDisplayItem(@NotNull Player p, @Nullable List<TextComponent> components) {
		return getDisplayItem(p, components, true, true);
	}

	/**
	 * Gets the item that will be shown to the player viewing this product.
	 * @param p The player to check the resources of.
	 * @param components Any additional components prior to listing the costs.
	 * @param def The default result.
	 * @param showOption Show the "Click to XY" option or not.
	 * @return The item.
	 */
	@NotNull
	public ItemStack getDisplayItem(@NotNull Player p, @Nullable List<TextComponent> components, boolean def, boolean showOption) {
		PlayerProfile pp = PlayerProfile.from(p);
		ItemStack displayItem = this.displayItem.clone();
		ItemMeta meta = displayItem.getItemMeta();
		boolean can = def;
		List<Component> lore = meta.lore();

		if (lore == null)
			lore = new ArrayList<>();

		if (greySubtext != null && !greySubtext.isEmpty()) {
			lore.add(0, Component.text("\u00a78" + greySubtext));
			lore.add(1, Component.empty());
		}
		
		if (components != null) {
			lore.add(Component.empty());
			lore.addAll(components);
		}
		
		lore.add(Component.text("---------------", NamedTextColor.DARK_GRAY).decoration(TextDecoration.STRIKETHROUGH, true).decoration(TextDecoration.ITALIC, false));
		lore.add(Component.text("\u00a77Cost"));
		
		if (complimentary) {
			lore.add(Component.text("\u00a78 • \u00a7aComplimentary"));
		} else {
			for (Entry<Component, Boolean> fakeEntries : fakeCosts.entrySet()) {
				boolean has = fakeEntries.getValue();
				lore.add(Component.text("\u00a78 • ").append(fakeEntries.getKey()).append(
						Component.text((!has ? " \u00a78(\u00a7c\u274c\u00a78)" : " \u00a78(\u00a7a\u2714\u00a78)"))).colorIfAbsent(NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false));

				if (!has) can = false;
			}

			for (Entry<ItemStack, Integer> entry : materialCost.entrySet()) {
				boolean has = p.getInventory().containsAtLeast(entry.getKey(), entry.getValue());
				ItemStack i = entry.getKey();
				lore.add(Component.text("\u00a78 • \u00a77" + df.format(entry.getValue()) + "\u00a78x \u00a7f").append((i.getItemMeta().hasDisplayName() ? i.getItemMeta().displayName() : Component.translatable(i)).append(
						Component.text((!has ? " \u00a78(\u00a7c\u274c\u00a78)" : " \u00a78(\u00a7a\u2714\u00a78)")))).colorIfAbsent(NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false));

				if (!has) can = false;
			}

			if (xpCost > 0) {
				boolean xp = p.getLevel() >= xpCost;
				lore.add(Component.text("\u00a78 • \u00a7r" + df.format(xpCost) + " \u25CE Experience Level" + (crystalCost != 1 ? "s " : " ")  + (!xp ? "\u00a78(\u00a7c\u274c\u00a78)" : "\u00a78(\u00a7a\u2714\u00a78)")).colorIfAbsent(BeanColor.EXPERIENCE).decoration(TextDecoration.ITALIC, false));
				if (!xp) can = false;
			}

			if (crystalCost > 0) {
				boolean crystal = pp.getCrystals() >= crystalCost;
				lore.add(Component.text("\u00a78 • \u00a7r" + df.format(crystalCost) + " \u2756 Crystal" + (crystalCost != 1 ? "s " : " ") + (!crystal ? "\u00a78(\u00a7c\u274c\u00a78)" : "\u00a78(\u00a7a\u2714\u00a78)")).colorIfAbsent(BeanColor.CRYSTALS).decoration(TextDecoration.ITALIC, false));
				if (!crystal) can = false;
			}
				
			if (coinCost > 0) {
				boolean coin = pp.getBalance() >= coinCost;
				lore.add(Component.text("\u00a78 • \u00a76" + df.format(coinCost) + " Coin" + (crystalCost != 1 ? "s " : " ") + (!coin ? "\u00a78(\u00a7c\u274c\u00a78)" : "\u00a78(\u00a7a\u2714\u00a78)")));
				if (!coin) can = false;
			}

			if (!skillRequirement.isEmpty()) {
				lore.add(Component.empty());
				lore.add(Component.text("\u00a77Skill Requirements"));

				for (Entry<Skill, Integer> entry : skillRequirement.entrySet()) {
					boolean has = pp.getSkillLevel(entry.getKey()) >= entry.getValue();
					lore.add(Component.text("\u00a78 • ").append(Component.text(entry.getKey().getNameWithIcon() + " Grade " + Grade.fromLevel(entry.getValue()) + " " + (!has ? " \u00a78(\u00a7c\u274c\u00a78)" : "\u00a78(\u00a7a\u2714\u00a78)")).colorIfAbsent(entry.getKey().getColour()).decoration(TextDecoration.ITALIC, false)));
					if (!has) can = false;
				}
			}

			boolean override = p.getGameMode() == GameMode.CREATIVE && pp.hasPermission(Permission.BYPASS_COSTS_CREATIVE);
			if (override)
				can = true;
			
			if (showOption) {
				lore.add(Component.empty());
				if (override)
					lore.add(Component.text("\u00a79» \u00a7rClick to bypass!").colorIfAbsent(Rank.ADMINISTRATOR.getRankColour()).decoration(TextDecoration.ITALIC, false));
				else
					lore.add(Component.text(can ? "\u00a72» \u00a7aClick to " + purchaseWord + "!" : "\u00a7c» Can't " + purchaseWord + "."));
			}
		}
		
		meta.lore(lore);
		displayItem.setItemMeta(meta);
		return displayItem;
	}

	public boolean isCustomItem() {
		return customItem != null;
	}

	@Nullable
	public BeanItem getCustomItem() {
		return customItem;
	}

	@NotNull
	public ItemStack getOriginalStack() {
		return baseDisplayItem.clone();
	}

	/**
	 * Check if the specified {@link Player} has the resources to make this purchase.
	 * @param p The player
	 * @return Whether the player can make this purchase or not.
	 */
	public boolean canPurchase(@NotNull Player p) {
		if (complimentary) return false;
		if (!enabled) return false;

		PlayerProfile pp = PlayerProfile.from(p);
		if (p.getGameMode() == GameMode.CREATIVE && pp.hasPermission(Permission.BYPASS_COSTS_CREATIVE)) return true;

		// Currency Checks
		if (p.getLevel() < xpCost) return false;
		if (pp.getCrystals() < crystalCost) return false;
		if (pp.getBalance() < coinCost) return false;

		// Skill Level Check
		for (Entry<Skill, Integer> entry : skillRequirement.entrySet())
			if (pp.getSkillLevel(entry.getKey()) < entry.getValue()) return false;

		// Material Check
		for (Entry<ItemStack, Integer> entry : materialCost.entrySet())
			if (!p.getInventory().containsAtLeast(entry.getKey(), entry.getValue())) return false;

		// Other Checks
		for (boolean ack : fakeCosts.values())
			if (!ack) return false;
		return true;
	}

	@NotNull
	public Component getDisplayName() {
		return displayItem.getItemMeta().displayName();
	}

	public PurchaseOption setDisplayName(Component newName) {
		setDisplayName(newName, true);
		return this;
	}

	private void setDisplayName(Component newName, boolean dirty) {
		displayItem.editMeta(meta -> {
			hasCustomName = true;
			meta.displayName(newName.decoration(TextDecoration.ITALIC, false));
		});
		this.dirty = dirty;
	}

	public PurchaseOption setDescription(Lore lore) {
		setDescription(lore, true);
		return this;
	}

	private void setDescription(Lore lore, boolean dirty) {
		description = lore;
		displayItem.editMeta(meta -> {
			if (lore == null) meta.lore(null);
			else meta.lore(new ArrayList<>(lore.getLore()));
		});
		this.dirty = dirty;
	}

	public void setSubtext(@Nullable String text) {
		this.greySubtext = text;
	}

	public void addCoinCost(int coins) {
		if (coins < 0) coins = 0;
		this.dirty = true;
		this.coinCost = coins;
	}
	
	public void addCrystalCost(int crystal) {
		if (crystal < 0) crystal = 0;
		this.crystalCost = crystal;
		dirty = true;
	}
	
	public void addItemCost(Material material, int quantity) {
		materialCost.put(new ItemStack(material), quantity);
		dirty = true;
	}
	
	public void addItemCost(ItemStack item, int quantity) {
		materialCost.put(new ItemStack(item.getType()), quantity);
		dirty = true;
	}
	
	public void addItemCost(BeanItem item, int quantity) {
		materialCost.put(item.getItemStack(), quantity);
		dirty = true;
	}

	private final Map<Component, Boolean> fakeCosts = new LinkedHashMap<>();
	/**
	 * These are not saved and are only used for stupid things like enchant table
	 */
	public void addFakeCost(@NotNull Component fakeCost, boolean has) {
		fakeCosts.put(fakeCost, has);
	}

	public void addExperienceCost(int xpLevels) {
		if (xpLevels < 0) xpLevels = 0;
		xpCost = xpLevels;
		dirty = true;
	}

	public void setDemanifestationReason(DemanifestationReason reason) {
		this.demanifestationReason = reason;
	}

	public void addSkillRequirement(@NotNull Skill skill, int level) {
		skillRequirement.put(skill, level);
	}

	public void setPurchaseWord(@Nullable String word) {
		this.purchaseWord = word;
		this.dirty = true;
	}
	
	public PurchaseOption setComplimentary() {
		this.complimentary = true;
		return this;
	}
	
	public PurchaseOption setMenuShop(MenuShop shop) {
		this.menuShop = shop;
		return this;
	}
	
	public PurchaseOption setShop(@NotNull String shop) {
		this.menuShop = Main.getInstance().menuShopManager().getOrMakeShop(shop);
		return this;
	}

	public boolean hasMultiPurchase() {
		return hasMultiPurchase;
	}
	
	protected void setEnabled(boolean enabled) {
		this.enabled = enabled;
		dirty = true;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	protected boolean isDirty() {
		return id > 0 && dirty;
	}
	
	/**
	 * Cleanse of dirty flag
	 */
	protected void clean() {
		this.dirty = false;
	}
	
	/**
	 * Allows for this PurchaseOption instance to be updated and overwritten in the database
	 */
	protected void setDBID(int id) {
		this.id = id;
		dirty = false;
	}
	
	protected int getDBID() {
		return id;
	}
	
	public int getCoinCost() {
		return coinCost;
	}
	
	public int getCrystalCost() {
		return crystalCost;
	}

	public int getExperienceCost() {
		return xpCost;
	}

	@Nullable
	protected Lore getDescription() {
		return description;
	}

	@Nullable
	protected MenuShop getMenuShop() {
		return menuShop;
	}

	@NotNull
	protected Map<ItemStack, Integer> getMaterialCost() {
		return materialCost;
	}

	@NotNull
	protected Map<@NotNull Skill, Integer> getSkillRequirements() {
		return skillRequirement;
	}
	
	protected boolean hasCustomName() {
		return hasCustomName;
	}

	@Nullable
	protected String getPurchaseWord() {
		return purchaseWord;
	}

	@Nullable
	protected String getSubtext() {
		return greySubtext;
	}
}
