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
		
		if (isPreviewing(p))
			attachment.setPermission("bean.cmd.op", true);
		
		final PlayerProfile pp = PlayerProfile.from(p);
		final boolean isTrueOwner = pp.getHighestRank() == Rank.OWNER && p.getUniqueId().equals(UUID.fromString("158f33a1-37d7-45d1-86bf-ed7f82a716b1"));
		p.setOp(isTrueOwner);
		
		for (Rank rank : Rank.values()) {
			if (!isTrueOwner) {
				if (rank.isDonorRank()) {
					if (pp.getDonorRank() != null && pp.getDonorRank().power() < rank.power()) continue;
				} else if (pp.getHighestRank().power() < rank.power()) break; // Don't bother continuing...
			}
			
			attachment.setPermission("bean.rank."+rank.lowerName(), true);
			for (String rankPerm : rank.getPermissions())
				attachment.setPermission(rankPerm, true);
		}
			
		permissions.put(p.getUniqueId(), attachment);
	}
	
	public void clearPlayerPermissions(Player p) {
		PermissionAttachment attachment = permissions.get(p.getUniqueId());
		p.removeAttachment(attachment);
		permissions.remove(p.getUniqueId());
	}
	
	// /op previewrank
	
	public void previewRankFor(Player p, Rank rank) {
		final ArrayList<Rank> preview = new ArrayList<Rank>();
		preview.add(rank);
		
		final PlayerProfile pp = PlayerProfile.from(p);
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
