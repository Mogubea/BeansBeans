package me.playground.gui;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import me.playground.currency.Currency;
import me.playground.playerprofile.ProfileStore;
import me.playground.shop.Shop;
import me.playground.utils.SignMenuFactory;
import me.playground.utils.Utils;
import net.kyori.adventure.text.Component;

public class BeanGuiShopCustomer extends BeanGuiShop {
	
	protected static final ItemStack blanks = newItem(new ItemStack(Material.BLACK_STAINED_GLASS_PANE, 1), Component.empty());
	protected static final ItemStack display = newItem(new ItemStack(Material.GLASS_PANE, 1), Component.text("Item Display"));
	protected static final ItemStack blanksS = newItem(new ItemStack(Material.LIME_STAINED_GLASS_PANE, 1), Component.text("\u00a7aYou can purchase here!"));
	protected static final ItemStack blanksB = newItem(new ItemStack(Material.RED_STAINED_GLASS_PANE, 1), Component.text("\u00a7cYou can sell here!"));
	protected static final ItemStack blanksN = newItem(new ItemStack(Material.WHITE_STAINED_GLASS_PANE, 1), Component.text("\u00a77You can't do much here.."));
	protected static final ItemStack adminBtn = newItem(Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjk5NTFkMWI0MTczNzJkMTBmMjY4YzVlNjMxZTBjOWQ5NGRjNDliZjlhN2M5M2Y3NmJlMGU5ZGFhYzM1MGNhNCJ9fX0="), "\u00a76View as Shop Owner");
	protected static final ItemStack shop_sellPrice = newItem(new ItemStack(Material.WARPED_SIGN, 1), Component.text("\u00a7aPurchase Price: \u00a770 Coins"), "", "\u00a78\u00a7oYou cannot buy items here.");
	protected static final ItemStack shop_buyPrice = newItem(new ItemStack(Material.CRIMSON_SIGN, 1), Component.text("\u00a7cSell Price: \u00a770 Coins"), "", "\u00a78\u00a7oYou cannot sell items here.");
	protected static final ItemStack shop_itemStorage = newItem(Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTA5MDVhYmE3MjNlOGY5N2JlZGNkMDYyM2Y0YzU1NzRlN2EyYzViZTNmMTFiYWViYTM4MzNjN2FkOTRlOTkzIn19fQ=="), "");
	
	
	public BeanGuiShopCustomer(Player p, Shop s) {
		super(p, s);
		this.presetSize = 45;
		this.presetInv = new ItemStack[] {
				blank,blank,blank,display,display,display,blank,blank,blank,
				blanks,blanks,blanks,display,null,display,blanks,blanks,blanks,
				blanks,shop_sellPrice,blanks,display,display,display,blanks,shop_buyPrice,blanks,
				blanks,blanks,blanks,blanks,blanks,blanks,blanks,blanks,blanks,
				blank,blank,blank,blank,blank,blank,blank,blank,blank
		};
	}

	@Override
	public void onInventoryClosed(InventoryCloseEvent e) {
		
	}

	@Override
	public void onInventoryClicked(InventoryClickEvent e) {
		e.setCancelled(true);
		final int slot = e.getRawSlot();
		final ItemStack item = e.getClickedInventory().getItem(e.getSlot());
		
		if (slot < 0 || item == null || item.getType() == Material.AIR)
			return;
		
		switch(slot) {
		case 8: // View shop as Owner
			if (p.hasPermission("bean.shop.override")) {
				new BeanGuiShopOwner(p, shop).openInventory();
				return;
			}
			break;
		case 13: // Purchase Item
			if (pp.onCdElseAdd("shop_purchase", 500)) {
			
			} else if (shop.getOwnerId() == pp.getId() || shop.getSellPrice() < 1) {
				p.sendActionBar(Component.text("\u00a7cYou can't purchase from this shop!"));
			} else if (shop.getItemQuantity() < 1) {
				p.sendActionBar(Component.text("\u00a7cThis shop has no stock left!"));
			} else if (pp.getBalance() < shop.getSellPrice()) {
				p.sendActionBar(Component.text("\u00a7cYou don't have enough to buy this!"));
			} else {
				ItemStack take = shop.getItemStack().clone();
				int takeAmt = Math.min(take.getMaxStackSize(), shop.getItemQuantity());
				
				if (takeAmt > 1) {
					p.closeInventory();
					SignMenuFactory.Menu menu = plugin.getSignMenuFactory().newMenu(Arrays.asList("","^^^^^^^^^^", "\u00a7e\u00a7lPurchase Amount", "\u00a7e(1 - "+takeAmt+")"), Material.WARPED_WALL_SIGN)
				            .reopenIfFail(true)
				            .response((player, strings) -> {
				                try {
				                	int amt = Integer.parseInt(strings[0]);
				                	if (amt < 1 || amt > takeAmt) {
				                		p.sendActionBar(Component.text("\u00a7cThat's not a valid amount!"));
				                		throw new RuntimeException();
				                	}
				                	
				                	if (amt*shop.getSellPrice() > pp.getBalance()) {
				                		p.sendActionBar(Component.text("\u00a7cYou don't have enough to buy "+amt+" of this!"));
				                		throw new RuntimeException();
				                	}
				                	
					                tryPurchase(amt);
				                	p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.5F, 0.8F);
				                } catch (RuntimeException ex) {
				                	p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5F, 0.8F);
				                }
				            	
				                Bukkit.getScheduler().runTaskLater(plugin, () -> new BeanGuiShopCustomer(p, shop).openInventory(), 1L);
				                return true;
				            });
							menu.open(p);
				} else {
					tryPurchase(1);
				}
				
			}
			break;
		default: // Sell Item
			if (slot >= e.getInventory().getSize()) {
				if (shop.getBuyPrice() <= 0)
					return;
				
				if (shop.getItemStack() != null && isSimilar(item, shop.getItemStack()) && !tpp.onCdElseAdd("shop_sell", 500)) {
					if (shop.getBuyPrice() > shop.getStoredMoney()) {
						pp.addCooldown("shop_sell", 3000);
						p.sendActionBar(Component.text("\u00a7cThis shop doesn't have enough money!"));
					} else {
						int shopMaxAmt = Math.floorDiv(shop.getStoredMoney(), shop.getBuyPrice());
						int addAmt = Math.min(Math.min(shop.getMaxItemQuantity()-shop.getItemQuantity(), shopMaxAmt), item.getAmount());
						
						if (addAmt < 1) {
							pp.addCooldown("shop_sell", 3000);
							p.sendActionBar(Component.text("\u00a7cThe shop is full!"));
						} else if (addAmt == 1) {
							if (item.getAmount()-addAmt < 0)
								e.getClickedInventory().setItem(e.getSlot(), null);
							else
								item.setAmount(item.getAmount()-1);
								
							p.updateInventory();
							shop.setItemQuantity(shop.getItemQuantity() + 1);
							shop.setStoredMoney(shop.getStoredMoney() - shop.getBuyPrice());
							
							double coin = (double)shop.getBuyPrice();
							if (coin > 20)
								coin*=0.97;
							
							pp.addToBalance((int)coin, "Selling to Shop ID: " + shop.getShopId());
							refreshShopViewers();
						} else {
							p.closeInventory();
							SignMenuFactory.Menu menu = plugin.getSignMenuFactory().newMenu(Arrays.asList("","^^^^^^^^^^", "\u00a7e\u00a7lSell Amount", "\u00a7e(1 - "+addAmt+")"), Material.WARPED_WALL_SIGN)
						            .reopenIfFail(true)
						            .response((player, strings) -> {
						                try {
						                	int amt = Integer.parseInt(strings[0]);
						                	if (amt < 1 || amt > addAmt) {
						                		p.sendActionBar(Component.text("\u00a7cThat's not a valid amount!"));
						                		throw new RuntimeException();
						                	}
						                	
						                	if (amt*shop.getBuyPrice() > shop.getStoredMoney()) {
						                		p.sendActionBar(Component.text("\u00a7cThis shop doesn't have enough to buy "+amt+" of that!"));
						                		throw new RuntimeException();
						                	}
						                	
						                	if ((item.getAmount()-amt) < 0)
												e.getClickedInventory().setItem(e.getSlot(), null);
											else
												item.setAmount(item.getAmount()-amt);
												
											shop.setItemQuantity(shop.getItemQuantity() + amt);
											shop.setStoredMoney(shop.getStoredMoney() - (shop.getBuyPrice()*amt));
											
											double coin = ((double)(shop.getBuyPrice()*amt));
											if (coin > 20)
												coin*=0.97;
											
											pp.addToBalance((int)coin, "Selling to Shop ID: " + shop.getShopId());
											refreshShopViewers();
						                	p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.5F, 0.8F);
						                } catch (RuntimeException ex) {
						                	p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5F, 0.8F);
						                }
						            	
						                p.updateInventory();
						                Bukkit.getScheduler().runTaskLater(plugin, () -> new BeanGuiShopCustomer(p, shop).openInventory(), 1L);
						                return true;
						            });
							menu.open(p);
						}
					}
				}
			}
			break;
		}
	}
	
	private void tryPurchase(int amount) {
		ItemStack take = shop.getItemStack().clone();
		take.setAmount(amount);
		int taken = amount - addToInvReturnLost(p, take);
			
		if (taken == 0) {
			tpp.addCooldown("shop_purchase", 3000);
			p.sendActionBar(Component.text("\u00a7cYour inventory is full!"));
		} else {
			shop.setItemQuantity(shop.getItemQuantity() - taken);
			
			double coin = (double)taken * (double)shop.getSellPrice();
			if (coin > 20)
				coin*=0.97;
			
			shop.setStoredMoney(shop.getStoredMoney() + (int)coin);
			pp.addToBalance(-taken * shop.getSellPrice(), "Purchase from Shop ID: " + shop.getShopId());
			p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.4F, 0.8F);
			refreshShopViewers();
			onInventoryOpened();
		}
	}

	@Override
	public void onInventoryOpened() {
		ItemStack[] contents = i.getContents();
		
		final ItemStack item = shop.getItemStack();
		final int stock = shop.getItemQuantity();
		final boolean isSelling = shop.getSellPrice() > 0 && stock > 0;
		final boolean isBuying = shop.getBuyPrice() > 0 && shop.getStoredMoney() >= shop.getBuyPrice() && stock < shop.getMaxItemQuantity();
		
		contents[8] = p.hasPermission("bean.shop.override") ? adminBtn : blank;
		
		for (int x = 1; x < 4; x++) {
			if (isSelling && !isBuying) {
				contents[9 * x] = blanksS;
				contents[9 * x + 8] = blanksS;
			} else if (isBuying && !isSelling) {
				contents[9 * x] = blanksB;
				contents[9 * x + 8] = blanksB;
			} else if (isSelling && isBuying) {
				contents[9 * x] = blanksS;
				contents[9 * x + 8] = blanksB;
			} else {
				contents[9 * x] = blanksN;
				contents[9 * x + 8] = blanksN;
			}
		}
		
		contents[13] = item;
		contents[30] = newItem(shop_itemStorage, Component.text("\u00a76Shop Stock"), "", "\u00a77There are \u00a7e"+stock+"\u00a77 items in stock", "\u00a77out of a maximum of \u00a76" + shop.getMaxItemQuantity());
		contents[32] = newItem(new ItemStack(Material.RAW_GOLD), Component.text("\u00a76Shop Bank"), "", "\u00a77This shop is holding \u00a7e" + df.format(shop.getStoredMoney()) + " Coins");
		
		if (isSelling) {
			ItemStack i = newItem(new ItemStack(Material.WARPED_SIGN, 1), Component.text("\u00a7aPurchase Price: \u00a76"+ df.format(shop.getSellPrice())+" Coins"), "", "\u00a78\u00a7oClick the \u00a77\u00a7oDisplay Item\u00a78\u00a7o to", "\u00a78\u00a7opurchase an amount of it!");
			i.addUnsafeEnchantment(Enchantment.ARROW_DAMAGE, 1);
			contents[19] = i;
		}
		
		if (isBuying) {
			double coin = (double)shop.getBuyPrice();
			if (coin > 20)
				coin*=0.97;
			ItemStack i = newItem(new ItemStack(Material.CRIMSON_SIGN, 1), Component.text("\u00a7cSell Price: \u00a76" + df.format((int)coin)+" Coins"), "", "\u00a78\u00a7oClick a \u00a77\u00a7ocopy \u00a78\u00a7oof the \u00a77\u00a7oDisplay", "\u00a77\u00a7oItem \u00a78\u00a7oto sell some of it!");
			i.addUnsafeEnchantment(Enchantment.ARROW_DAMAGE, 1);
			contents[25] = i;
		}
		
		final ProfileStore ps = ProfileStore.from(shop.getOwnerId());
		
		contents[31] = newItem(Utils.getSkullFromPlayer(ps.getUniqueId()), ps.getColouredName().append(Component.text("\u00a7e's Shop")));
		contents[40] = newItem(icon_money, Component.text("\u00a77You currently have " + Utils.currencyString(Currency.COINS, pp.getBalance())));
		
		
		i.setContents(contents);
	}
	
}
