package me.playground.regions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.annotation.Nonnull;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockVector;

import me.playground.celestia.logging.Celestia;
import me.playground.data.Dirty;
import me.playground.playerprofile.PlayerProfile;
import me.playground.playerprofile.ProfileStore;
import me.playground.regions.flags.Flag;
import me.playground.regions.flags.FlagBoolean;
import me.playground.regions.flags.FlagMember;
import me.playground.regions.flags.MemberLevel;
import me.playground.utils.BeanColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;

public class Region extends RegionBase implements Dirty, Comparable<Region> {
	
	final private int creatorId;
	protected BlockVector min, max;
	
	private String name;
	private int parentId;
	private boolean dirty;
	
	private Component componentName;
	
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
		super(rm, id, world);
		this.name = name;
		updateColouredName();
		this.creatorId = creator;
		this.priority = priority;
		this.min = min;
		this.max = max;
		rm.registerRegion(this);
	}
	
	public Region(RegionManager rm, int id, World w) {
		this(rm, id, 0, 0, w.getName(), w, null, null);
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
			rm.getDatasource().setRegionMember(regionId, playerId, level);
		members.put(playerId, level);
		return this;
	}
	
	public void removeMember(int playerId) {
		rm.getDatasource().removeRegionMember(regionId, playerId);
		members.remove(playerId);
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
	
	/**
	 * Player parameter is required for {@link Celestia}
	 * @param p
	 * @param name
	 */
	public void setName(@Nonnull Player p, @Nonnull String name) {
		rm.renameRegion(this.name, name);
		Celestia.logRegionChange(p, "Renamed from " + this.name + " to " + name + ".");
		this.name = name;
		updateColouredName();
		setDirty(true);
	}
	
	public String getName() {
		return this.name;
	}
	
	private final void updateColouredName() {
		componentName = Component.text(name, getColour());
	}
	
	public Component getColouredName() {
		return componentName;
	}
	
	public boolean isWorldRegion() {
		return this.getRegionId() < 0;
	}
	
	public BeanColor getColour() {
		return this.isWorldRegion() ? BeanColor.REGION_WORLD : BeanColor.REGION;
	}
	
	public boolean isDirty() {
		return dirty;
	}
	
	public int setPriority(Player p, int val) {
		setDirty(true);
		Celestia.logRegionChange(p, "Priority changed from " + priority + " to " + val + ".");
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
	
	private Component getComponentMembers() {
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
	
	private Component component;
	public Component toComponent() {
		if (component != null) return component;
		Component text;
		
		if (isWorldRegion()) {
			text = Component.text(
					"\u00a7rWorld Region" + 
					"\n\u00a7e(\u00a78World: " + getWorld().getName() + "\u00a7e)");
		} else {
			text = Component.text(
					"\u00a7rPlayer Region" +
					"\n\u00a7e(\u00a78" + getMinimumPoint().toString() + "\u00a77 - \u00a78" + getMaximumPoint() + "\u00a7e)" +
					"\n\u00a77Priority: \u00a7b" + getPriority())
					.append(getComponentMembers());
		}
		text = text.colorIfAbsent(isWorldRegion() ? BeanColor.REGION_WORLD : BeanColor.REGION);
		Component done = getColouredName().hoverEvent(HoverEvent.showText(text));
		component = Component.empty().append(done.clickEvent(ClickEvent.suggestCommand("/region warpto " + getName())));
		return component;
	}
	
	public Region update() {
		rm.refreshRegion(this);
		return this;
	}
	
	public void delete() {
		if (this.isWorldRegion()) return; // Precaution.
		rm.removeRegion(this);
	}
	
	public boolean canModify(Player p) {
		if (this.isWorldRegion()) return p.hasPermission("bean.region.override");
		return p.hasPermission("bean.region.modifyothers") || getMember(p).higherThan(MemberLevel.OFFICER);
	}

	@Override
	public int compareTo(Region otherRegion) {
		return Integer.compare(otherRegion.getPriority(), getPriority());
	}
	
	public boolean can(Player p, Flag<?> flag) {
		MemberLevel level = getMember(p);
		
		if (flag instanceof FlagMember)
			return getEffectiveFlag((FlagMember)flag).lowerOrEqTo(level);
		if (flag instanceof FlagBoolean)
			return getEffectiveFlag((FlagBoolean)flag).booleanValue() ? level.higherThan(MemberLevel.VISITOR) : true;
		return false;
	}
	
}
