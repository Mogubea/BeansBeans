package me.playground.regions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockVector;

import me.playground.data.Datasource;
import me.playground.data.Dirty;
import me.playground.playerprofile.PlayerProfile;
import me.playground.playerprofile.ProfileStore;
import me.playground.regions.flags.Flags;
import me.playground.regions.flags.MemberLevel;
import me.playground.utils.BeanColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

public class Region extends RegionBase implements Dirty {
	
	final protected RegionManager rm;
	final private int creatorId;
	protected BlockVector min, max;
	
	private String name;
	private int parentId;
	private boolean dirty;
	
	private final LinkedHashMap<Integer, MemberLevel> members = new LinkedHashMap<Integer, MemberLevel>(); // Member ID, Permission Level
	private final List<Integer> regionKeys = new ArrayList<Integer>(); // Region Keys, to remove upon deletion
	
	public Region(RegionManager rm, int id, int creator, int priority,  String name, World world, int mx, int my, int mz, int max, int may, int maz) {
		this(rm, id, creator, priority, name, world, new BlockVector(mx,my,mz), new BlockVector(max,may,maz));
	}
	
	public Region(RegionManager rm, int id, int creator, int priority, int parent, String name, World world, BlockVector min, BlockVector max) {
		this(rm, id, creator, priority, name, world, min, max);
		this.parentId = parent;
	}
	
	public Region(RegionManager rm, int id, int creator, int priority, String name, World world, BlockVector min, BlockVector max) {
		super(id, world);
		this.rm = rm;
		this.name = name;
		this.creatorId = creator;
		this.priority = priority;
		this.min = min;
		this.max = max;
		rm.registerRegion(this);
	}
	
	public Region(RegionManager rm, int id, World w) {
		this(rm, id, 0, 0, w.getName(), w, null, null);
		// World Region defaults
		this.setFlag(Flags.BUILD_ACCESS, MemberLevel.NONE, false);
		this.setFlag(Flags.CONTAINER_ACCESS, MemberLevel.NONE, false);
		this.setFlag(Flags.DOOR_ACCESS, MemberLevel.NONE, false);
		this.setFlag(Flags.PROTECT_ANIMALS, false, false);
	}
	
	public BlockVector getMinimumPoint() {
		return min;
	}
	
	public BlockVector getMaximumPoint() {
		return max;
	}
	
	public boolean isInsideRegion(int x, int y, int z) {
		return new BlockVector(x, y, z).isInAABB(min, max);
	}
	
	public Region setMinimumPoint(int x, int y, int z) {
		this.min = new BlockVector(x, y, z);
		this.setDirty(true);
		return this;
	}
	
	public Region setMaximumPoint(int x, int y, int z) {
		this.max = new BlockVector(x, y, z);
		this.setDirty(true);
		return this;
	}
	
	public int getCreatorId() {
		return creatorId;
	}
	
	public Component getCreatorName() {
		return ProfileStore.from(creatorId, false).getColouredName();
	}
	
	public int getRegionId() {
		return regionId;
	}
	
	public boolean isMember(Player p) {
		return isMember(PlayerProfile.getDBID(p));
	}
	
	public boolean isMember(int memberId) {
		return members.containsKey(memberId);
	}
	
	public Region addMember(Player p, MemberLevel level) {
		return addMember(PlayerProfile.getDBID(p), level, true);
	}
	
	public Region addMember(int playerId, MemberLevel level) {
		return addMember(playerId, level, true);
	}
	
	public Region addMember(int playerId, MemberLevel level, boolean save) {
		if (save)
			Datasource.setRegionMember(regionId, playerId, level);
		members.put(playerId, level);
		return this;
	}
	
	public void removeMember(int playerId) {
		Datasource.removeRegionMember(regionId, playerId);
		members.remove(playerId);
	}
	
	public MemberLevel getMember(int memberId) {
		return this.members.getOrDefault(memberId, MemberLevel.NONE);
	}
	
	public MemberLevel getMember(Player p) {
		if (p.hasPermission("bean.region.override"))
			return MemberLevel.MASTER;
		
		return this.members.getOrDefault(PlayerProfile.getDBID(p), MemberLevel.NONE);
	}
	
	public LinkedHashMap<Integer, MemberLevel> getMembers() {
		return this.members;
	}
	
	public Collection<Integer> getMembersOf(MemberLevel level) {
		ArrayList<Integer> members = new ArrayList<Integer>();
		for (Entry<Integer, MemberLevel> member : getMembers().entrySet()) {
			if (member.getValue() == level)
				members.add(member.getKey());
		}
		return members;
	}
	
	public void addRegionKey(int key) {
		this.regionKeys.add(key);
	}
	
	public List<Integer> getRegionKeys() {
		return this.regionKeys;
	}
	
	public void setName(String name) {
		rm.renameRegion(this.name, name);
		this.name = name;
		setDirty(true);
	}
	
	public String getName() {
		return this.name;
	}
	
	public TextComponent getColouredName() {
		return Component.text(this.name).color(this.isWorldRegion() ? BeanColor.REGION_WORLD : BeanColor.REGION);
	}
	
	public boolean isWorldRegion() {
		return this.getRegionId() < 0;
	}
	
	public boolean isDirty() {
		return dirty;
	}
	
	public int setPriority(int val) {
		setDirty(true);
		return priority = val;
	}
	
	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}
	
	public Region getParent() {
		return rm.getRegion(parentId);
	}
	
	public boolean hasParent() {
		return parentId > 0;
	}
	
	public int getParentId() {
		return parentId;
	}
	
	public Region setParent(int id) {
		Region r = rm.getRegion(id);
		if (r != null) {
			this.parentId = id;
			setDirty(true);
		}
		return r;
	}
	
	public Location getRegionCenter() {
		if (this.isWorldRegion())
			return world.getSpawnLocation();
		
		return new Location(world, min.getX() + (max.getX()-min.getX()) / 2, min.getY() + (max.getY()-min.getY()) / 2, min.getZ() + (max.getZ()-min.getZ()) / 2);
	}
	
	public Component getComponentMembers() {
		Component text = Component.empty();
		MemberLevel[] seek = {MemberLevel.OWNER, MemberLevel.OFFICER, MemberLevel.TRUSTED, MemberLevel.MEMBER, MemberLevel.VISITOR };
		
		for (MemberLevel level : seek) {
			ArrayList<Integer> members = (ArrayList<Integer>) getMembersOf(level);
			if (members.size() > 0) {
				text = text.append(Component.text("\n\u00a77" + level.toString() + "s: "));
				for (int x = 0; x < Math.min(5, members.size()); x++)
					text = text.append(PlayerProfile.getDisplayName(members.get((int)x)).append(Component.text("\u00a78" + (x+1<5? ", " : "..."))));
			}
		}
		return text;
	}
	
	public Region update() {
		rm.refreshRegion(this);
		return this;
	}
	
	/*public <T extends Flag<V>, V> boolean can(Player p, T flag) {
		if (flag instanceof FlagMember) maybe?
			return !this.getEffectiveFlag((FlagMember)flag).higherThan(this.getMember(p));
		return true;
	}*/
	
}
