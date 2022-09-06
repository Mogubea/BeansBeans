package me.playground.menushop;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import me.playground.items.lore.Lore;
import me.playground.skills.Skill;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.json.JSONArray;
import org.json.JSONObject;

import me.playground.data.PrivateDatasource;
import me.playground.items.BeanItem;
import me.playground.main.Main;
import me.playground.utils.Utils;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

/**
 * {@link PurchaseOption}s and shit.
 */
public class MenuShopDatasource extends PrivateDatasource {
	private final MenuShopManager manager;
	
	public MenuShopDatasource(Main plugin, MenuShopManager manager) {
		super(plugin);
		this.manager = manager;
	}
	
	/**
	 * Load all products
	 */
	@Override
	public void loadAll() {
		try(Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("SELECT * FROM menushop_purchase_options"); ResultSet r = s.executeQuery()) {
			while(r.next()) {
				int id = r.getInt("id");
				String shopId = r.getString("shop");
				if (shopId == null) continue;
				
				MenuShop shop = manager.getOrMakeShop(shopId);
				
				ItemStack displayItem;
				int dItemType = r.getInt("displayItemType");
				
				try {
					String itemId = r.getString("displayItemId");
					switch (dItemType) {
					case 1: // BeanItem
						displayItem = BeanItem.from(itemId).getItemStack();
						break;
					case 2: // Base64 Item
						displayItem = Utils.itemStackFromBase64(r.getString("displayCompressedStack"));
						break;
					default: // Material Item
						displayItem = new ItemStack(Material.valueOf(itemId));
						break;
					}
					
					displayItem.setAmount(r.getInt("displayItemAmount"));
				} catch (Exception e) {
					continue; // If exception, skip registering the product.
				}
				
				PurchaseOption option = new PurchaseOption(displayItem, GsonComponentSerializer.gson().deserializeOrNull(r.getString("componentName")), r.getString("description") != null ? Lore.getBuilder(r.getString("description")).build() : null);
				option.setPurchaseWord(r.getString("purchaseWord"));
				option.setSubtext(r.getString("subText"));
				option.setEnabled(r.getBoolean("enabled"));
				option.addCoinCost(r.getInt("coinCost"));
				option.addCrystalCost(r.getInt("crystalCost"));
				option.addExperienceCost(r.getInt("xpCost"));
				
				// Add material cost
				String plainItemCost = r.getString("itemCost");
				if (plainItemCost != null && !plainItemCost.isEmpty()) {
					try {
						JSONArray itemArray = new JSONArray(plainItemCost);
					
						int size = itemArray.length();
						for (int x = -1; ++x < size;) {
							JSONObject itemObj = itemArray.optJSONObject(x);
							if (itemObj == null) continue;
							
							int itemType = itemObj.optInt("type", 0);
							String itemId = itemObj.optString("id");
							if (itemId == null) continue;
							
							int amount = itemObj.optInt("amt", 1);
							
							switch (itemType) {
							case 1: // BeanItem
								option.addItemCost(BeanItem.from(itemId), amount);
								break;
							default: // Material Item
								option.addItemCost(Material.valueOf(itemId), amount);
								break;
							}
						} 
					}catch (Exception e) {
						continue; // If exception, skip adding the item
					}
				}

				// Add skill requirements
				String skillRequirement = r.getString("skillRequirements");
				if (skillRequirement != null && !skillRequirement.isEmpty()) {
					try {
						JSONArray array = new JSONArray(skillRequirement);

						int size = array.length();
						for (int x = -1; ++x < size;) {
							JSONObject obj = array.optJSONObject(x);
							if (obj == null) continue;

							Skill skill = Skill.getByName(obj.optString("skill"));
							int level = obj.optInt("level", 1);

							option.addSkillRequirement(skill, level);
						}
					} catch (Exception e) {
						continue;
					}
				}
				
				option.setDBID(id);
				shop.addPurchaseOption(option);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Save all dirty {@link PurchaseOption}s
	 */
	@Override
	public void saveAll() {
		manager.getPurchaseOptions().forEach(shop -> {
			if (!shop.isDirty()) return;
			saveDirtyPurchaseOption(shop);
		});
	}
	
	/**
	 * Delete the specified {@link PurchaseOption} from its {@link MenuShop} and database.
	 */
	protected boolean deleteShop(PurchaseOption option) {
		if (option.getMenuShop() == null || option.getDBID() < 1) return false;
		try(Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("DELETE FROM menushop_purchase_options WHERE id = ?")) {
			s.setInt(1, option.getDBID());
			s.executeUpdate();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Register a new {@link PurchaseOption} to the database.
	 */
	public void register(PurchaseOption option) {
		if (option.getMenuShop() == null || option.getDBID() > 0) return;
		
		try(Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("INSERT INTO menushop_purchase_options (purchaseWord, subText, coinCost, crystalCost, xpCost, displayItemType, displayItemId, "
				+ "displayCompressedStack, displayItemAmount, componentName, description, enabled, shop, itemCost, skillRequirement) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
			int idx = 1;

			s.setString(idx++, option.getPurchaseWord());
			s.setString(idx++, option.getSubtext());

			s.setInt(idx++, option.getCoinCost());
			s.setInt(idx++, option.getCrystalCost());
			s.setInt(idx++, option.getExperienceCost());
			
			ItemStack displayStack = option.getOriginalStack();
			BeanItem dCustom = BeanItem.from(displayStack);
			boolean more = displayStack.getItemMeta().hasEnchants();
			
			s.setInt(idx++, more ? 2 : dCustom == null ? 0 : 1);
			s.setString(idx++, dCustom == null ? displayStack.getType().name() : dCustom.getIdentifier());
			s.setString(idx++, more ? Utils.toBase64(displayStack) : null);
			s.setInt(idx++, displayStack.getAmount());
			
			s.setString(idx++, GsonComponentSerializer.gson().serializeOrNull(displayStack.getItemMeta().displayName()));
			s.setString(idx++, option.getDescription() == null ? null : option.getDescription().getBaseContent());
			
			s.setBoolean(idx++, option.isEnabled());
			s.setString(idx++, option.getMenuShop().getIdentifier());

			// Items
			JSONArray itemArray = new JSONArray();
			
			option.getMaterialCost().forEach((item, quantity) -> {
				JSONObject itemObj = new JSONObject();
				BeanItem custom = BeanItem.from(item);
				if (custom != null)
					itemObj.put("type", 1);
				itemObj.put("id", custom == null ? item.getType().name() : custom.getIdentifier());
				if (quantity > 1)
					itemObj.put("amt", quantity);
				
				itemArray.put(itemObj);
			});
			
			s.setString(idx++, itemArray.toString());

			// Skills
			JSONArray skillArray = new JSONArray();

			option.getSkillRequirements().forEach((skill, level) -> {
				JSONObject obj = new JSONObject();
				obj.put("skill", skill.getName().toLowerCase());
				obj.put("level", level);
				skillArray.put(obj);
			});

			s.setString(idx++, skillArray.toString());
			s.setInt(idx, option.getDBID());
			
			ResultSet r = s.getGeneratedKeys();
			r.next();
			option.setDBID(r.getInt(1));
			r.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void saveDirtyPurchaseOption(PurchaseOption option) {
		if (option.getMenuShop() == null || option.getDBID() < 1) return; // Can't update if it doesn't exist
		
		try(Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("UPDATE menushop_purchase_options SET purchaseWord = ?, subText = ?, coinCost = ?, crystalCost = ?, xpCost = ?, displayItemType = ?, "
				+ "displayItemId = ?, displayCompressedStack = ?, displayItemAmount = ?, componentName = ?, description = ?, enabled = ?, shop = ?, itemCost = ?, skillRequirement = ? WHERE id = ?")) {
			int idx = 1;

			s.setString(idx++, option.getPurchaseWord());
			s.setString(idx++, option.getSubtext());

			s.setInt(idx++, option.getCoinCost());
			s.setInt(idx++, option.getCrystalCost());
			s.setInt(idx++, option.getExperienceCost());
			
			ItemStack displayStack = option.getOriginalStack();
			BeanItem dCustom = BeanItem.from(displayStack);
			boolean more = displayStack.getItemMeta().hasEnchants();
			
			s.setInt(idx++, more ? 2 : dCustom == null ? 0 : 1);
			s.setString(idx++, dCustom == null ? displayStack.getType().name() : dCustom.getIdentifier());
			s.setString(idx++, more ? Utils.toBase64(displayStack) : null);
			s.setInt(idx++, displayStack.getAmount());
			
			s.setString(idx++, option.hasCustomName() ? GsonComponentSerializer.gson().serializeOrNull(displayStack.getItemMeta().displayName()) : null);
			s.setString(idx++, option.getDescription() == null ? null : option.getDescription().getBaseContent());
			
			s.setBoolean(idx++, option.isEnabled());
			s.setString(idx++, option.getMenuShop().getIdentifier());

			// Items
			JSONArray itemArray = new JSONArray();
			
			option.getMaterialCost().forEach((item, quantity) -> {
				JSONObject itemObj = new JSONObject();
				BeanItem custom = BeanItem.from(item);
				if (custom != null)
					itemObj.put("type", 1);
				itemObj.put("id", custom == null ? item.getType().name() : custom.getIdentifier());
				if (quantity > 1)
					itemObj.put("amt", quantity);
				
				itemArray.put(itemObj);
			});

			s.setString(idx++, itemArray.toString());

			// Skills
			JSONArray skillArray = new JSONArray();

			option.getSkillRequirements().forEach((skill, level) -> {
				JSONObject obj = new JSONObject();
				obj.put("skill", skill.getName().toLowerCase());
				obj.put("level", level);
				skillArray.put(obj);
			});

			s.setString(idx++, skillArray.toString());
			
			s.setInt(idx, option.getDBID());
			
			s.executeUpdate();
			option.clean();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
}
