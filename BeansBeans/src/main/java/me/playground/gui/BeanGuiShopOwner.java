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

import me.playground.items.BeanItem;
import me.playground.shop.Shop;
import me.playground.utils.SignMenuFactory;
import me.playground.utils.Utils;
import net.kyori.adventure.text.Component;

public class BeanGuiShopOwner extends BeanGuiShop {
	
	protected static final ItemStack blankk = newItem(new ItemStack(Material.BLACK_STAINED_GLASS_PANE, 1), Component.empty());
	protected static final ItemStack blank2 = newItem(new ItemStack(Material.ORANGE_STAINED_GLASS_PANE, 1), Component.empty());
	protected static final ItemStack display = newItem(new ItemStack(Material.GLASS_PANE, 1), Component.text("Item Display"), "", "\u00a78\u00a7oClick your \u00a77\u00a7oDisplay Item\u00a78\u00a7o, to take out", "\u00a77\u00a7oa stack of \u00a76\u00a7oShop Stock\u00a78\u00a7o.", "", "\u00a79\u00a7oShift Clicking \u00a78\u00a7owill instantly drop all", "\u00a76\u00a7oShop Stock\u00a78\u00a7o at your feet.");
	protected static final ItemStack blankS = newItem(new ItemStack(Material.LIME_STAINED_GLASS_PANE, 1), Component.text("\u00a7aSelling to Players"), "", "\u00a77\u00a7oThe item displayed in this shop", "\u00a77\u00a7ocan be purchased by players!");
	protected static final ItemStack blankB = newItem(new ItemStack(Material.RED_STAINED_GLASS_PANE, 1), Component.text("\u00a7cBuying from Players"), "", "\u00a77\u00a7oThe item displayed in this shop", "\u00a77\u00a7ocan be sold to you by playrs!");
	protected static final ItemStack blankNS = newItem(new ItemStack(Material.WHITE_STAINED_GLASS_PANE, 1), Component.text("\u00a77Not Selling to Players"), "", "\u00a77\u00a7oSet a \u00a7a\u00a7oSell Price\u00a77\u00a7o in order for this", "\u00a77\u00a7oshop to sell items to players!");
	protected static final ItemStack blankNB = newItem(new ItemStack(Material.WHITE_STAINED_GLASS_PANE, 1), Component.text("\u00a77Not Buying from Players"), "", "\u00a77\u00a7oSet a \u00a7c\u00a7oBuy Price\u00a77\u00a7o in order for this", "\u00a77\u00a7oshop to buy items from players!");
	protected static final ItemStack adminBtn = newItem(Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTRkNDYyZmY4NmU1ZGVhYzY4NzNkZTdjOTZhMDk2NzUzZDg3ZmNmMjI4YWE5NzE4NmE5N2Y5OGE2ZDJjZTc0ZiJ9fX0="), "\u00a76View as Customer");
	protected static final ItemStack shop_bank = newItem(new ItemStack(Material.RAW_GOLD), Component.text("\u00a76Shop Bank"), "", "\u00a77This shop is holding \u00a770 Coins", "", "\u00a78\u00a7oLeft Click to \u00a74\u00a7odeposit \u00a78\u00a7osome \u00a76\u00a7oCoins", "\u00a78\u00a7oRight Click to \u00a72\u00a7owithdraw \u00a78\u00a7osome \u00a76\u00a7oCoins", "\u00a78\u00a7o(\u00a79\u00a7oShift\u00a78\u00a7o to do deposit/withdraw all coins)");
	protected static final ItemStack shop_sellPrice = newItem(new ItemStack(Material.WARPED_SIGN, 1), Component.text("\u00a7aSell Price: \u00a770 Coins"), "", "\u00a78\u00a7oThis is the amount of coins Players", "\u00a78\u00a7opay in order to purchase 1x of", "\u00a78\u00a7oof your displayed item.", "", "\u00a77You recieve \u00a7e97%\u00a77 of this amount.");
	protected static final ItemStack shop_buyPrice = newItem(new ItemStack(Material.CRIMSON_SIGN, 1), Component.text("\u00a7cBuy Price: \u00a770 Coins"), "", "\u00a78\u00a7oThis is how much your shop pays to", "\u00a78\u00a7oPlayers when they sell you 1x of the", "\u00a78\u00a7odisplayed item.", "", "\u00a77Sellers recieve \u00a7e97%\u00a77 of this amount.");
	protected static final ItemStack shop_delete = newItem(Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2Y1MTg1Y2FlY2JlZDhlOTg5OWI0YmJiYjkxMTVlOWQ2OWI5NzU3ZjBkZWY3Y2Q3NmZmMDNmOTZkZDdkYzFiYSJ9fX0="), "\u00a7cDestroy Shop", "", "\u00a78\u00a7oDestroying the shop will drop all", "\u00a78\u00a7oremaining \u00a76\u00a7oShop Stock\u00a78\u00a7o onto the ground.");
	
	public BeanGuiShopOwner(Player p, Shop s) {
		super(p, s);
		setName(s.getOwnerId() > 0 ? "Your Shop" : "Server Shop");
		this.presetInv = new ItemStack[] {
				blank,blank2,blank,display,display,display,blank,blank2,blank,
				blankS,blankk,blankk,display,null,display,blankk,blankk,blankB,
				blankS,shop_sellPrice,blankk,display,display,display,blankk,shop_buyPrice,blankB,
				blankS,blankk,blankk,shop_itemStorage,blank,shop_bank,blankk,blankk,blankB,
				blank2,blank,blank2,blank,blank2,blank,blank2,blank,shop_delete
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
		
		// This should never happen, but better be safe than sorry.
		if (shop.getOwnerId() != pp.getId() && !p.hasPermission("bean.shop.override")) {
			p.closeInventory();
			return;
		}
		
		if (slot < 0 || item == null || item.getType() == Material.AIR)
			return;
		
		switch(slot) {
		case 8: // View shop as Customer
			if (p.hasPermission("bean.shop.override")) {
				new BeanGuiShopCustomer(p, shop).openInventory();
				return;
			}
			break;
		case 13: // Take out Item
			if (pp.onCdElseAdd("shop_itemTake", 500, true)) {
				p.sendActionBar(Component.text("\u00a7cPlease slow down!"));
			} else if (shop.getItemQuantity() > 0) {
				if (e.isShiftClick()) {
					// If SHIFT CLICK, drop EVERYTHING on the player
					if (e.isShiftClick()) {
						BeanGuiConfirm confirm = new BeanGuiConfirm(p, 
								Arrays.asList(
										Component.text("\u00a77Confirming will drop \u00a7c" + shop.getItemQuantity() + " Items"),
										Component.text("\u00a77at your feet immediately. Make sure there"),
										Component.text("\u00a77is no one around to steal your stuff!"))) {
							@Override
							public void onAccept() {
								while (shop.getItemQuantity() > 0) {
									ItemStack i = shop.getItemStack().clone();
									i.setAmount(Math.min(i.getMaxStackSize(), shop.getItemQuantity()));
									shop.setItemQuantity(shop.getItemQuantity() - i.getAmount());
									p.getWorld().dropItem(p.getLocation(), i);
								}
								resetShopStack();
								new BeanGuiShopOwner(p, shop).openInventory();
							}

							@Override
							public void onDecline() {
								new BeanGuiShopOwner(p, shop).openInventory();
							}
						};
						confirm.openInventory();
					}
				} else {
					// Take out one stack at a time, directly to the inventory...
					ItemStack take = shop.getItemStack().clone();
					int takeAmt = Math.min(take.getMaxStackSize(), shop.getItemQuantity());
					take.setAmount(takeAmt);
					int taken = takeAmt - addToInvReturnLost(p, take);
						
					if (taken == 0) {
						tpp.addCooldown("shop_itemTake", 5000);
						p.sendActionBar(Component.text("\u00a7cYour inventory is full!"));
					} else {
						shop.setItemQuantity(shop.getItemQuantity() - taken);
						p.sendActionBar(Component.text("\u00a7aSuccessfully taken \u00a7e" + taken + " Items\u00a7a out of your Shop!"));
						refreshShopViewers();
					}
				}
			} else {
				resetShopStack();
			}
			break;
		case 19: // Set the SELL PRICE
			p.closeInventory();
			
			SignMenuFactory.Menu menu = plugin.getSignMenuFactory().newMenu(Arrays.asList("","^^^^^^^^^^", "\u00a7a\u00a7lSell Price", "\u00a7afor your Shop"), Material.WARPED_WALL_SIGN)
            .reopenIfFail(true)
            .response((player, strings) -> {
                try {
                	int newAmount = Integer.parseInt(strings[0]);
                	if (newAmount < 1)
                		newAmount = 0;
                	if (newAmount > 99999999) {
                		p.sendActionBar(Component.text("\u00a7cThat's not a valid amount!"));
                		throw new RuntimeException();
                	}
                	
                	if (shop.getBuyPrice() > 0 && newAmount < shop.getBuyPrice()) {
                		p.sendActionBar(Component.text("\u00a7aSell Price\u00a7c can't be lower than the \u00a74Buy Price\u00a7c!"));
                		throw new RuntimeException();
                	}
                	
                	p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.5F, 0.8F);
                	shop.setSellPrice(newAmount);
                	refreshShopViewers();
                } catch (RuntimeException ex) {
                	p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5F, 0.8F);
                }
            	
                Bukkit.getScheduler().runTaskLater(plugin, () -> new BeanGuiShopOwner(p, shop).openInventory(), 1L);
                return true;
            });
			menu.open(p);
			break;
		case 25: // Set the BUY PRICE
			p.closeInventory();
			
			SignMenuFactory.Menu menuu = plugin.getSignMenuFactory().newMenu(Arrays.asList("","^^^^^^^^^^", "\u00a7c\u00a7lBuy Price", "\u00a7cfor your Shop"), Material.CRIMSON_WALL_SIGN)
            .reopenIfFail(true)
            .response((player, strings) -> {
                try {
                	int newAmount = Integer.parseInt(strings[0]);
                	if (newAmount < 1)
                		newAmount = 0;
                	if (newAmount > 99999999) {
                		p.sendActionBar(Component.text("\u00a7cThat's not a valid amount!"));
                		throw new RuntimeException();
                	}
                	
                	if (shop.getSellPrice() > 0 && newAmount > shop.getSellPrice()) {
                		p.sendActionBar(Component.text("\u00a74Buy Price\u00a7c can't be higher than the \u00a7aSell Price\u00a7c!"));
                		throw new RuntimeException();
                	}
                	
                	p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.5F, 0.8F);
                	shop.setBuyPrice(newAmount);
                	refreshShopViewers();
                } catch (RuntimeException ex) {
                	p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5F, 0.8F);
                }
            	
                Bukkit.getScheduler().runTaskLater(plugin, () -> new BeanGuiShopOwner(p, shop).openInventory(), 1L);
                return true;
            });
			menuu.open(p);
			break;
		case 32: // Shop Bank
			final boolean shift = e.isShiftClick();
			if (pp.onCdElseAdd("shop_moneyTransaction", 3000, true)) {
				p.sendActionBar(Component.text("\u00a7cPlease slow down!"));
				return;
			}
			
			// Add Money
			if (e.isLeftClick()) {
				// ALL
				if (shift) {
					shop.setStoredMoney((int) (shop.getStoredMoney() + pp.getBalance()));
					pp.addToBalance(-pp.getBalance(), "Deposit to Shop ID: " + shop.getShopId());
					refreshShopViewers();
				} else {
					p.closeInventory();
					
					SignMenuFactory.Menu menuuu = plugin.getSignMenuFactory().newMenu(Arrays.asList("","^^^^^^^^^^", "\u00a7e\u00a7lDeposit Funds", "\u00a7eto your Shop"), Material.JUNGLE_WALL_SIGN)
		            .reopenIfFail(true)
		            .response((player, strings) -> {
		                try {
		                	int newAmount = Integer.parseInt(strings[0]);
		                	if (newAmount < 1)
		                		newAmount = 0;
		                	if (newAmount > 99999999 || newAmount > pp.getBalance())
		                		throw new RuntimeException();
		                	
		                	p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.5F, 0.8F);
		                	shop.setStoredMoney((int) (shop.getStoredMoney() + newAmount));
							pp.addToBalance(-newAmount, "Deposit to Shop ID: " + shop.getShopId());
							refreshShopViewers();
		                } catch (RuntimeException ex) {
		                	p.sendActionBar(Component.text("\u00a7cThat's not a valid amount!"));
		                	p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5F, 0.8F);
		                }
		            	
		                Bukkit.getScheduler().runTaskLater(plugin, () -> new BeanGuiShopOwner(p, shop).openInventory(), 1L);
		                return true;
		            });
					menuuu.open(p);
				}
			// Take Money
			} else if (e.isRightClick()) {
				// ALL
				if (shift) {
					pp.addToBalance(shop.getStoredMoney(), "Withdraw from Shop ID: " + shop.getShopId());
					shop.setStoredMoney(0);
				} else {
					p.closeInventory();
					
					SignMenuFactory.Menu menuuu = plugin.getSignMenuFactory().newMenu(Arrays.asList("","^^^^^^^^^^", "\u00a7e\u00a7lWithdraw Funds", "\u00a7efrom your Shop"), Material.JUNGLE_WALL_SIGN)
		            .reopenIfFail(true)
		            .response((player, strings) -> {
		                try {
		                	int newAmount = Integer.parseInt(strings[0]);
		                	if (newAmount < 1)
		                		newAmount = 0;
		                	if (newAmount > 99999999 || newAmount > shop.getStoredMoney())
		                		throw new RuntimeException();
		                	
		                	p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.5F, 0.8F);
		                	shop.setStoredMoney((int) (shop.getStoredMoney() - newAmount));
							pp.addToBalance(newAmount, "Withdraw from Shop ID: " + shop.getShopId());
							refreshShopViewers();
		                } catch (RuntimeException ex) {
		                	p.sendActionBar(Component.text("\u00a7cThat's not a valid amount!"));
		                	p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5F, 0.8F);
		                }
		            	
		                Bukkit.getScheduler().runTaskLater(plugin, () -> new BeanGuiShopOwner(p, shop).openInventory(), 1L);
		                return true;
		            });
					menuuu.open(p);
				}
			}
			break;
		case 44: // Shop Delete
			if ((pp.hasPermission("bean.shop.override") || shop.getOwnerId() == pp.getId())) {
				BeanGuiConfirm confirm = new BeanGuiConfirm(p, Arrays.asList(
						Component.text("\u00a77Confirming will drop \u00a7c" + shop.getItemQuantity() + " Items"),
						Component.text("\u00a77at your feet immediately. Make sure there"),
						Component.text("\u00a77is no one around to steal your stuff!"),
						Component.empty(),
						Component.text("\u00a77All \u00a76Shop Funds\u00a77 will be instantly"),
						Component.text((shop.getOwnerId()!=pp.getId() ? "\u00a77returned to the Owner's wallet." : "\u00a77returned to your wallet.")))) {
					@Override
					public void onAccept() {
						p.closeInventory();
						
						while (shop.getItemQuantity() > 0) {
							ItemStack i = shop.getItemStack().clone();
							i.setAmount(Math.min(i.getMaxStackSize(), shop.getItemQuantity()));
							shop.setItemQuantity(shop.getItemQuantity() - i.getAmount());
							p.getWorld().dropItem(p.getLocation(), i);
						}
						shop.delete(pp.getId());
						kickShopViewers();
						p.getWorld().dropItem(p.getLocation(), BeanItem.SHOP_STAND.getOriginalStack());
					}

					@Override
					public void onDecline() {
						new BeanGuiShopOwner(p, shop).openInventory();
					}
				};
				confirm.openInventory();
			}
			break;
		default:
			if (slot >= e.getInventory().getSize()) {
				// Add a similar item to the stock
				if (shop.getItemStack() != null && isSimilar(item, shop.getItemStack())) {
					if (tpp.onCdElseAdd("shop_itemAdd", 500, true))
						return;
					
					int addAmt = Math.min(shop.getMaxItemQuantity()-shop.getItemQuantity(), item.getAmount());
					if (addAmt < 1) {
						pp.addCooldown("shop_itemAdd", 3000);
						p.sendActionBar(Component.text("\u00a7cThe shop is full!"));
					} else {
						if (item.getAmount()-addAmt < 0)
							e.getClickedInventory().setItem(e.getSlot(), null);
						else
							item.setAmount(item.getAmount()-addAmt);
						
						p.updateInventory();
						shop.setItemQuantity(shop.getItemQuantity() + addAmt);
						refreshShopViewers();
						p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_GOLD, 0.2F, 0.8F);
					}
				// Replace the current display item
				} else if (pp.onCdElseAdd("shop_itemReplace", 2000, true)) {
					p.sendActionBar(Component.text("\u00a7cPlease slow down!"));
				} else if (shop.getItemQuantity() > 0) {
					p.sendMessage(Component.text("\u00a7cPlease remove all of your \u00a76Shop Stock\u00a7c before replacing the \u00a7fDisplay Item\u00a7c!"));
				} else {
					shop.setItemStack(item);
					shop.setBuyPrice(0);
					shop.setSellPrice(0);
					refreshShopViewers();
					p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 0.2F, 0.8F);
				}
			}
			break;
		}
		
		onInventoryOpened();
	}

	@Override
	public void onInventoryOpened() {
		ItemStack[] contents = i.getContents();
		
		final ItemStack item = shop.getItemStack();
		final int stock = shop.getItemQuantity();
		final boolean isSelling = shop.getSellPrice() > 0 && stock > 0;
		final boolean isBuying = shop.getBuyPrice() > 0 && shop.getStoredMoney() >= shop.getBuyPrice() && stock < shop.getMaxItemQuantity();
		
		contents[8] = pp.hasPermission("bean.shop.override") ? adminBtn : blank;
		
		if (!isSelling) {
			for (int x = 1; x < 4; x++)
				contents[x * 9] = blankNS;
		}
		
		if (!isBuying) {
			for (int x = 1; x < 4; x++)
				contents[x * 9 + 8] = blankNB;
		}
		
		contents[13] = item;
		contents[30] = newItem(shop_itemStorage, Component.text("\u00a76Shop Stock"), "", "\u00a77There are \u00a7e"+stock+"\u00a77 items in stock", "\u00a77out of a maximum of \u00a76" + shop.getMaxItemQuantity());
		
		if (shop.getStoredMoney() > 0) {
			 ItemStack i = newItem(new ItemStack(Material.RAW_GOLD), Component.text("\u00a76Shop Bank"), "", "\u00a77This shop is holding \u00a7e" + df.format(shop.getStoredMoney()) + " Coins", "", "\u00a78\u00a7oLeft Click to \u00a74\u00a7odeposit \u00a78\u00a7osome \u00a76\u00a7oCoins", "\u00a78\u00a7oRight Click to \u00a72\u00a7owithdraw \u00a78\u00a7osome \u00a76\u00a7oCoins", "\u00a78\u00a7o(\u00a79\u00a7oShift\u00a78\u00a7o to do deposit/withdraw all coins)");
			 i.addUnsafeEnchantment(Enchantment.ARROW_DAMAGE, 1);
			 contents[32] = i;
		}
		
		if (shop.getSellPrice() > 0) {
			ItemStack i = newItem(new ItemStack(Material.WARPED_SIGN, 1), Component.text("\u00a7aSell Price: \u00a7e"+df.format(shop.getSellPrice())+" Coins"), "", "\u00a78\u00a7oThis is the amount of coins Players", "\u00a78\u00a7opay in order to purchase 1x of", "\u00a78\u00a7oof your displayed item.", "", "\u00a77You recieve \u00a7e97%\u00a77 of this amount.");
			i.addUnsafeEnchantment(Enchantment.ARROW_DAMAGE, 1);
			contents[19] = i;
		}
		
		if (shop.getBuyPrice() > 0) {
			ItemStack i = newItem(new ItemStack(Material.CRIMSON_SIGN, 1), Component.text("\u00a7cBuy Price: \u00a7e"+df.format(shop.getBuyPrice())+" Coins"), "", "\u00a78\u00a7oThis is how much your shop pays to", "\u00a78\u00a7oPlayers when they sell you 1x of the", "\u00a78\u00a7odisplayed item.", "", "\u00a77Sellers recieve \u00a7e97%\u00a77 of this amount.");
			i.addUnsafeEnchantment(Enchantment.ARROW_DAMAGE, 1);
			contents[25] = i;
		}
		
		if (pp.hasPermission("bean.shop.override") || pp.getId() == shop.getOwnerId())
			contents[44] = shop_delete;
		
		i.setContents(contents);
	}
	
	private void resetShopStack() {
		shop.setItemStack(null);
		shop.setBuyPrice(0);
		shop.setSellPrice(0);
		refreshShopViewers();
		p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 0.2F, 0.8F);
	}
	
}
