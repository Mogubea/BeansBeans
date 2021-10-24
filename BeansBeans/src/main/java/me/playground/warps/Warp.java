package me.playground.warps;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

import me.playground.data.Dirty;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;

public class Warp implements Dirty {
	
	// Constant values
	private final int warpId, creatorId;
	
	// Modifiable values
	private String name; // Name of the warp (Used in commands and warping to).
	private int ownerId; // Current Owner of the warp.
	private Material item; // Display item of the warp.
	private WarpType warpType = WarpType.PLAYER; // Type of Warp, default: PLAYER
	private String description; // The description of the warp. unused.
	private boolean isPublic; // Whether the warp is publically available or not.
	private boolean isLocked; // Whether the warp can be updated/deleted or not.
	private int useCount; // How many times the warp has been used.
	private List<Integer> invitedIds = new ArrayList<Integer>(); // Players that can access the warp regardless of it being private.
	private List<Integer> bannedIds = new ArrayList<Integer>(); // Players that can't access the warp regardless of it being public.
	private Location location; // Location of the warp.
	
	private boolean dirty = false;
	
	public Warp(int warpId, int ownerId, int creatorId, String name, Material material, String desc, boolean isPublic, boolean isLocked, int useCount, List<Integer> invited, List<Integer> banned, Location location) {
		this.warpId = warpId;
		this.creatorId = creatorId;
		this.ownerId = ownerId;
		this.name = name;
		this.item = material;
		this.description = desc;
		this.isPublic = isPublic;
		this.isLocked = isLocked;
		this.useCount = useCount;
		this.invitedIds = invited;
		this.bannedIds = banned;
		this.location = location;
	}

	public void warp(Player p) {
		if (getLocation() != null && getLocation().getWorld() != null) {
			if (p.teleport(getLocation())) {
				p.closeInventory();
				this.useCount++;
				setDirty(true);
				p.sendMessage(Component.text("\u00a77Successfully warped to ").append(toComponent()));
			}
		} else {
			p.sendMessage("\u00a7cLooks like this warp's location has been obstructed, likely due to the location no longer existing. It is advisable to update or delete the warp.");
		}
	}
	
	public boolean canPlayerWarp(Player p) {
		PlayerProfile pp = PlayerProfile.from(p);
		return isOwner(pp.getId()) || p.hasPermission("bean.cmd.warp.others") || (!isPublic && invitedIds.contains(pp.getId()) || (isPublic && !bannedIds.contains(pp.getId())));
	}
	
	public boolean isOwner(Player p) {
		return isOwner(PlayerProfile.from(p).getId());
	}
	
	public boolean isOwner(int playerId) {
		return playerId == this.ownerId;
	}
	
	public void invitePlayer(Player p) {
		invitePlayer(PlayerProfile.from(p).getId());
	}
	
	public void invitePlayer(int playerId) {
		if (invitedIds.contains(playerId))
			return;
		
		PlayerProfile pp = PlayerProfile.fromIfExists(playerId);
		if (pp.isOnline())
			pp.getPlayer().sendMessage(Component.text("\u00a77You were invited to ").append(toComponent()));
		
		invitedIds.add(playerId);
		setDirty(true);
	}
	
	public boolean isInvited(Player p) {
		return isInvited(PlayerProfile.from(p).getId());
	}
	
	public boolean isInvited(int playerId) {
		return invitedIds.contains(playerId);
	}
	
	public boolean uninvitePlayer(int playerId) {
		if (invitedIds.contains(playerId)) {
			PlayerProfile pp = PlayerProfile.fromIfExists(playerId);
			if (pp.isOnline())
				pp.getPlayer().sendMessage(Component.text("\u00a77You've been uninvited from ").append(toComponent()));
		}
		
		return invitedIds.remove((Integer)playerId);
	}
	
	public boolean isPublic() {
		return isPublic;
	}

	public void setPublic(boolean isPublic) {
		this.isPublic = isPublic;
		setDirty(true);
	}

	public boolean isLocked() {
		return isLocked;
	}

	public void setLocked(boolean isLocked) {
		this.isLocked = isLocked;
		setDirty(true);
	}

	public int getUseCount() {
		return useCount;
	}

	public List<Integer> getInvitedIds() {
		return invitedIds;
	}

	public void setInvitedIds(ArrayList<Integer> invitedIds) {
		this.invitedIds = invitedIds;
		setDirty(true);
	}

	public List<Integer> getBannedIds() {
		return bannedIds;
	}

	public void setBannedIds(ArrayList<Integer> bannedIds) {
		this.bannedIds = bannedIds;
		setDirty(true);
	}

	public Location getLocation() {
		return location;
	}
	
	public World getWorld() {
		return location.getWorld();
	}

	public void setLocation(Location location) {
		this.location = location;
		setDirty(true);
	}

	public int getWarpId() {
		return warpId;
	}

	public int getCreatorId() {
		return creatorId;
	}

	public String getName() {
		return name;
	}
	
	public void rename(String s) {
		Main.getWarpManager().renameWarp(this, s);
		name = s;
		setDirty(true);
	}

	public int getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(int ownerId) {
		this.ownerId = ownerId;
		setDirty(true);
	}

	public Material getItem() {
		return item;
	}

	public void setItem(@Nullable Material item) {
		this.item = item;
		setDirty(true);
	}
	
	public void setType(@Nullable WarpType type) {
		warpType = type;
		if (type != WarpType.PLAYER)
			setPublic(true);
		if (type == WarpType.SERVER)
			setOwnerId(0);
	}
	
	public WarpType getType() {
		return warpType;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
		setDirty(true);
	}
	
	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}
	
	public boolean isDirty() {
		return dirty;
	}
	
	public boolean delete() {
		return Main.getWarpManager().deleteWarp(this);
	}
	
	public Component toComponent() {
		return Component.empty().append(Component.text(getName()).color(getType().getColor())
				.hoverEvent(HoverEvent.showText(Component.text("\u00a77A " + warpType.getName() + " \u00a77owned by ").append(PlayerProfile.getDisplayName(getOwnerId()))))
				.clickEvent(ClickEvent.suggestCommand("/warp " + getName())));
	}
	
}
