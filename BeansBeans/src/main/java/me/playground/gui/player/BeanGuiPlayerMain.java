package me.playground.gui.player;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import me.playground.celestia.logging.Celestia;
import me.playground.playerprofile.ProfileModifyRequest;
import me.playground.playerprofile.ProfileModifyRequest.ModifyType;
import me.playground.playerprofile.ProfileStore;
import me.playground.ranks.Permission;
import me.playground.ranks.Rank;
import me.playground.utils.SignMenuFactory;
import me.playground.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;

public class BeanGuiPlayerMain extends BeanGuiPlayer {
	
	private final ItemStack skullDiscord = Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGQ0MjMzN2JlMGJkY2EyMTI4MDk3ZjFjNWJiMTEwOWU1YzYzM2MxNzkyNmFmNWZiNmZjMjAwMDAwMTFhZWI1MyJ9fX0=");
	
	public BeanGuiPlayerMain(Player p) {
		super(p);
		
		setName(pp.isOverridingProfile() ? p.getName() + "'s Menu" : "Your Menu");
		this.presetSize = 54;
		this.presetInv = new ItemStack[] {
				blank,blank,bBlank,bBlank,null,bBlank,bBlank,blank,blank,
				blank,null,null,null,null,null,null,null,blank,
				bBlank,null,null,null,null,null,null,null,bBlank,
				bBlank,null,null,null,null,null,null,null,bBlank,
				bBlank,null,null,null,null,null,null,null,bBlank,
				blank,blank,blank,blank,goBack,blank,blank,blank,blank
		};
	}

	@Override
	public void onInventoryClosed(InventoryCloseEvent e) {
	}
	
	@Override
	public void onInventoryClicked(InventoryClickEvent e) {
		final int slot = e.getRawSlot();
		
		switch(slot) {
		case 20: // Name Change
			if (!pp.isOverridingProfile()) {
				if (tpp.onCooldown("nicknameRequest")) {
					p.sendActionBar(Component.text("\u00a7cPlease wait before applying for another nickname!"));
					return;
				} else if (!tpp.hasPermission(Permission.NICKNAME_APPLY) && !tpp.hasPermission(Permission.NICKNAME_OVERRIDE)) {
					p.sendActionBar(Component.text("\u00a7cYou don't have permission to apply for a nickname!"));
					return;
				}
			}
			
			close();
			
			SignMenuFactory.Menu menu = plugin.getSignMenuFactory().newMenu(Arrays.asList(
					Component.empty(), Component.text("\u00a78^^^^^^^^^^"), Component.text("\u00a7fEnter " + (pp.isOverridingProfile() ? tpp.getDisplayName() + "'s" : "your") + " new"), Component.text("\u00a7fnickname above!")), Material.OAK_WALL_SIGN)
            .reopenIfFail(true)
            .response((player, strings) -> {
                try {
                	String newName = strings[0];
                	if (newName == null || newName.length() < 4 || newName.length() > 16)
                		throw new RuntimeException("Invalid name length (4 - 16)!");
                	
                	if (!newName.matches("^[a-zA-Z0-9_]+$"))
                		throw new RuntimeException("Nicknames must be alphanumeric (Underscores are allowed)!");
                	
                	if (ProfileStore.from(newName, true) != null)
                		throw new RuntimeException("That name is already taken!");
                	
                	// Don't bother with applying if you're viewing a profile or have the override permission
                	if (pp.isOverridingProfile() || tpp.hasPermission(Permission.NICKNAME_OVERRIDE)) {
                		tpp.setNickname(newName);
                		Celestia.logModify(pp.getId(), "Changed %ID"+tpp.getId()+"'s Name to " + newName);
                	} else {
                    	if (!ProfileModifyRequest.newRequest(tpp.getId(), ModifyType.NAME_CHANGE, newName))
                    		throw new RuntimeException("There was an error applying for a nickname...");
                    	tpp.addCooldown("nicknameRequest", 1000 * 60 * 60 * 24 * 7); // 7 day cooldown
                	}
                	
                	p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.5F, 0.8F);
                } catch (RuntimeException ex) {
                	if (ex.getMessage() != null)
                		p.sendActionBar(Component.text("\u00a7c" + ex.getMessage()));
                	p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5F, 0.8F);
                }
            	
                Bukkit.getScheduler().runTaskLater(plugin, () -> new BeanGuiPlayerMain(p).openInventory(), 1L);
                return true;
            });
			menu.open(p);
			return;
		case 21:
			new BeanGuiPlayerNameColour(p).openInventory();
			return;
		/*case 23: case 25:
			final boolean in = slot == 23;
			final String msg = in ? "login" : "logout";
			
			if (!pp.isOverridingProfile()) {
				if (tpp.onCooldown("nicknameRequest")) {
					p.sendActionBar(Component.text("\u00a7cPlease wait before applying for this!"));
					return;
				} else if (!tpp.hasPermission(Permission.CONNECTMSG_APPLY) && !tpp.hasPermission(Permission.CONNECTMSG_OVERRIDE)) {
					p.sendActionBar(Component.text("\u00a7cYou don't have permission to apply for this!"));
					return;
				}
			}
			
			close();
			
			SignMenuFactory.Menu menuu = plugin.getSignMenuFactory().newMenu(Arrays.asList(
					"","","\u00a78^^^^^^^^^^", "\u00a7fEnter " + (pp.isOverridingProfile() ? tpp.getDisplayName() + "'s" : "your") + " new " + msg + " message above!"), Material.OAK_WALL_SIGN)
            .reopenIfFail(true)
            .response((player, strings) -> {
                try {
                	String newMsg = strings[0] + strings[1];
                	if (newMsg == null || newMsg.length() < 4 || newMsg.length() > 64)
                		throw new RuntimeException("Invalid message length (4 - 64)!");
                	
                	// Don't bother with applying if you're viewing a profile or have the override permission
                	if (pp.isOverridingProfile() || tpp.hasPermission(Permission.CONNECTMSG_OVERRIDE)) {
                		// TODO
                	} else {
                    	if (!ProfileModifyRequest.newRequest(tpp.getId(), in ? ModifyType.LOGIN_MESSAGE : ModifyType.LOGOUT_MESSAGE, newMsg))
                    		throw new RuntimeException("There was an error applying for a " + msg + " message...");
                    	tpp.addCooldown(msg+"Request", 1000 * 60 * 60 * 24 * 7); // 7 day cooldown
                	}
                	
                	p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.5F, 0.8F);
                } catch (RuntimeException ex) {
                	if (ex.getMessage() != null)
                		p.sendActionBar(Component.text("\u00a7c" + ex.getMessage()));
                	p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5F, 0.8F);
                }
            	
                Bukkit.getScheduler().runTaskLater(plugin, () -> new BeanGuiPlayerMain(p).openInventory(), 1L);
                return true;
            });
			menuu.open(p);
			return;*/
		case 23:
			if (this.isOverrideView() || getPlugin().getDiscord().isLinked(tpp.getId())) return;
			long code = getPlugin().getDiscord().generateLinkCode(tpp.getId());
			p.sendMessage(Component.text("\u00a77Your Discord Link Code is \u00a7f\u00a7l" + code));
			p.sendMessage(Component.text("\u00a77Use the \u00a7b/link " + code + "\u00a77 command in the Bean's Beans Discord to link your accounts!"));
			p.closeInventory();
			return;
		case 24:
			new BeanGuiPlayerSettings(p).openInventory();
		}
	}
	
	@Override
	public void onInventoryOpened() {
		final ItemStack[] contents = presetInv.clone();
		contents[4] = newItem(tpp.getSkull(), tpp.getColouredName());
		
		boolean canNick = pp.isOverridingProfile() || tpp.hasPermission(Permission.NICKNAME_APPLY) || tpp.hasPermission(Permission.NICKNAME_OVERRIDE);
		
		contents[20] = newItem(new ItemStack(Material.NAME_TAG), Component.text("Nickname", tpp.getNameColour()).append(Component.text(canNick ? "" : "\u00a77 (Unavailable)")),
				Component.text(tpp.hasNickname() ? "\u00a77Current Nickname: \u00a7f"+tpp.getNickname() : "\u00a77You have no nickname set."),
				Component.empty(),
				canNick ? 
						(!tpp.onCooldown("nicknameRequest") ? Component.text(pp.isOverridingProfile() || tpp.hasPermission(Permission.NICKNAME_OVERRIDE) ? "\u00a76» \u00a7eClick to modify!" 
								: "\u00a76» \u00a7eClick to apply!") 
								: Component.text("\u00a7cCooldown: " + Utils.timeStringFromNow(tpp.getCooldown("nicknameRequest")))) 
						: Component.text("\u00a78» Requires the " + Rank.PATRICIAN.getNiceName() + " rank."));
		
		// Name Colour Icon
		ItemStack chestCol = new ItemStack(Material.LEATHER_CHESTPLATE);
		LeatherArmorMeta meta = (LeatherArmorMeta) chestCol.getItemMeta();
		meta.displayName(Component.text("Name Colour", tpp.getNameColour()).decoration(TextDecoration.ITALIC, false));
		final ArrayList<Component> lore = new ArrayList<>();
		lore.add(Component.text("\u00a77Current Colour: #").append(Component.text(Long.toHexString(tpp.getNameColour().value()), tpp.getNameColour()).decoration(TextDecoration.ITALIC, false)));
		lore.add(Component.empty());
		lore.add(Component.text("\u00a76» \u00a7eClick to modify!"));
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES,ItemFlag.HIDE_DYE);
		meta.lore(lore);
		meta.setColor(Color.fromRGB(tpp.getNameColour().value()));
		chestCol.setItemMeta(meta);
		
		contents[21] = chestCol;
		
		// Rank Icon
		ItemStack rankIcon = newItem(new ItemStack(Material.CLOCK), Component.text("Rank", tpp.getNameColour()));
		Rank playtimeRank = tpp.getPlaytimeRank();
		lore.clear();
		lore.add(Component.text("\u00a77Rank: ").append(tpp.getHighestRank().toComponent()));
		if (tpp.isRank(Rank.MODERATOR))
			lore.add(Component.text("\u00a77Playtime Rank: ").append(playtimeRank.toComponent()));
		lore.add(Component.text("\u00a77Playtime: \u00a7f" + Utils.timeStringFromMillis(tpp.getPlaytime() * 1000)));
		if (tpp.getPlaytimeRank() != Rank.EXALTED) {
			Rank next = Rank.values()[tpp.getPlaytimeRank().ordinal()+1];
			lore.add(playtimeRank.toComponent().append(Component.text(" ")).append(Utils.getProgressBar('-', 16, tpp.getPlaytime(), next.getPlaytimeRequirement(), 0x444444, playtimeRank.getRankColour().value())).append(Component.text(" ")).append(next.toComponent()));
		}
		
		if (tpp.getDonorRank() != null) {
			lore.add(Component.empty());
			lore.add(Component.text("\u00a77Supporter Rank: ").append(tpp.getDonorRank().toComponent()));
		}
		rankIcon.editMeta((metaa) -> metaa.lore(lore));
		
		contents[22] = rankIcon;
		
		// Discord Icon
		ItemStack discordIcon = newItem(skullDiscord, Component.text("Discord", tpp.getNameColour()));
		lore.clear();
		if (getPlugin().getDiscord().isLinked(tpp.getId())) {
			lore.add(Component.text("\u00a77Status: \u00a79Linked"));
			lore.add(Component.text("\u00a77Account: \u00a7b" + getPlugin().getDiscord().getDiscordAccount(tpp.getId()).getUser().getAsTag()));
		} else {
			lore.add(Component.text("\u00a77Status: \u00a7cNot Linked"));
			lore.add(Component.empty());
			lore.add(Component.text("\u00a76» \u00a7eClick to obtain Link Code!"));
		}
		discordIcon.editMeta((metaa) -> metaa.lore(lore));
		
		contents[23] = discordIcon;
		contents[24] = newItem(new ItemStack(Material.REDSTONE_TORCH), Component.text("Settings", tpp.getNameColour()), Component.empty(), Component.text("\u00a76» \u00a7eClick to modify!"));
		
		i.setContents(contents);
	}
	
}
