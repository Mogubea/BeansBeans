package me.playground.main;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

import me.playground.playerprofile.PlayerProfile;
import me.playground.ranks.Rank;

public class PermissionManager {
	
	final private Main plugin;
	
	final private HashMap<UUID, PermissionAttachment> permissions = new HashMap<UUID, PermissionAttachment>();
	final private HashMap<UUID, List<Rank>> rankPreviewers = new HashMap<UUID, List<Rank>>();
	
	public PermissionManager(Main plugin) {
		this.plugin = plugin;
	}
	
	public PermissionAttachment getPlayerAttachment(Player p) {
		return permissions.get(p.getUniqueId());
	}
	
	public Main getPlugin() {
		return plugin;
	}
	
	public void updatePlayerPermissions(Player p) {
		PermissionAttachment attachment = permissions.getOrDefault(p.getUniqueId(), p.addAttachment(plugin));
		// Clear all existing permissions
		for (Entry<String, Boolean> perm : attachment.getPermissions().entrySet())
			attachment.unsetPermission(perm.getKey());
		
		final PlayerProfile pp = PlayerProfile.from(p);
		final boolean isTrueOwner = pp.getHighestRank() == Rank.OWNER && p.getUniqueId().equals(UUID.fromString("158f33a1-37d7-45d1-86bf-ed7f82a716b1"));
		p.setOp(isTrueOwner);
		
		for (String s : pp.getPermissions())
			attachment.setPermission(s, true);
		
		if (isPreviewing(p)) // Override for previewing, we need access to the /op command.
			attachment.setPermission("bean.cmd.op", true);
		
		permissions.put(p.getUniqueId(), attachment);
		p.updateCommands();
	}
	
	public void clearPlayerPermissions(Player p) {
		PermissionAttachment attachment = permissions.get(p.getUniqueId());
		if (attachment != null)
			p.removeAttachment(attachment);
		permissions.remove(p.getUniqueId());
		p.updateCommands();
	}
	
	// /op previewrank
	
	public void previewRankFor(Player p, Rank rank) {
		final ArrayList<Rank> preview = new ArrayList<>();
		preview.add(rank);
		
		final PlayerProfile pp = PlayerProfile.from(p);
		if (!rankPreviewers.containsKey(p.getUniqueId())) // Don't override
			this.rankPreviewers.put(p.getUniqueId(), new ArrayList<>(pp.getRanks()));
		pp.setRanks(preview);
	}
	
	public boolean isPreviewing(Player p) {
		return rankPreviewers.containsKey(p.getUniqueId());
	}
	
	public Collection<UUID> getRankPreviewers() {
		return rankPreviewers.keySet();
	}
	
	public boolean stopPreviewingRank(Player p) {
		return stopPreviewingRank(PlayerProfile.from(p));
	}
	
	public boolean stopPreviewingRank(PlayerProfile pp) {
		if (this.rankPreviewers.containsKey(pp.getUniqueId())) {
			pp.setRanks(this.rankPreviewers.get(pp.getUniqueId()));
			this.rankPreviewers.remove(pp.getUniqueId());
			return true;
		}
		return false;
	}
	
}
