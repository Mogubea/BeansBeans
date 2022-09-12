package me.playground.regions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.annotation.Nonnull;

import me.playground.entity.EntityRegionCrystal;
import me.playground.main.TeamManager;
import me.playground.regions.flags.*;
import net.kyori.adventure.text.format.TextColor;
import net.minecraft.world.entity.Entity;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockVector;

import me.playground.celestia.logging.Celestia;
import me.playground.data.Dirty;
import me.playground.playerprofile.PlayerProfile;
import me.playground.playerprofile.ProfileStore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

public class Region extends RegionBase implements Dirty, Comparable<Region> {
	
	final private int creatorId;
	private BoundingBox box;
	protected BlockVector min, max;
	
	private String name;
	private int parentId;
	private boolean dirty;

	private final List<EntityRegionCrystal> crystals = new ArrayList<>();

	protected Component componentName;
	protected TextColor colour;
	
	private final LinkedHashMap<Integer, MemberLevel> members = new LinkedHashMap<>(); // Member ID, Permission Level
	private final List<Integer> regionKeys = new ArrayList<>(); // Region Keys, to remove upon deletion
	
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
		this.creatorId = creator;
		this.priority = priority;
		this.min = min;
		this.max = max;

		colour = TextColor.color(getFlag(Flags.NAME_COLOUR));
		componentName = Component.text(name, getColour());

		if (min != null && max != null)
			this.box = BoundingBox.of(min, max);
		rm.registerRegion(this);
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

	// TODO: There is likely a better algorithm for this.
	public boolean isByRegionBoundary(int x, int y, int z, int distance) {
		return Math.abs(max.getX() - x) <= distance || Math.abs(x - min.getX()) <= distance
				|| Math.abs(max.getY() - y) <= distance || Math.abs(y - min.getY()) <= distance
				|| Math.abs(max.getZ() - z) <= distance || Math.abs(z - min.getZ()) <= distance;
	}
	
	public Region setMinimumPoint(int x, int y, int z) {
		this.min = new BlockVector(x, y, z);
		this.box.resize(min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ());
		this.setDirty(true);
		return this;
	}
	
	public Region setMaximumPoint(int x, int y, int z) {
		this.max = new BlockVector(x, y, z);
		this.box.resize(min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ());
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

	public MemberLevel getTrueMemberLevel(int id) {
		return this.members.getOrDefault(id, MemberLevel.NONE);
	}
	
	public LinkedHashMap<Integer, MemberLevel> getMembers() {
		return this.members;
	}
	
	public Collection<Integer> getMembersOf(MemberLevel level) {
		ArrayList<Integer> members = new ArrayList<>();
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

	public String getDisplayName() {
		return this.name;
	}

	public void updateColouredName() {
		colour = TextColor.color(getFlag(Flags.NAME_COLOUR));
		componentName = Component.text(name, getColour());
		getPlayersInRegion().forEach(player -> PlayerProfile.from(player).flagScoreboardUpdate(TeamManager.ScoreboardFlag.REGION));
		crystals.forEach(crystal -> crystal.getBukkitEntity().customName(componentName));
	}
	
	public Component getColouredName() {
		return componentName;
	}

	public TextColor getColour() {
		return colour;
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

	@NotNull
	public BoundingBox getBoundingBox() {
		return box;
	}

	public Location getRegionCenter() {
		return new Location(world, min.getX() + (max.getX()-min.getX()) / 2, min.getY() + (max.getY()-min.getY()) / 2, min.getZ() + (max.getZ()-min.getZ()) / 2);
	}

	@NotNull
	protected Collection<Player> getPlayersInRegion() {
		List<Player> players = new ArrayList<>();
		getWorld().getNearbyEntities(box, (entity) -> entity instanceof Player).forEach((player) -> players.add((Player) player));
		return players;
	}
	
	Component getComponentMembers() {
		Component text = Component.empty();
		MemberLevel[] seek = {MemberLevel.OWNER, MemberLevel.OFFICER, MemberLevel.TRUSTED, MemberLevel.MEMBER, MemberLevel.VISITOR };
		
		for (MemberLevel level : seek) {
			ArrayList<Integer> members = (ArrayList<Integer>) getMembersOf(level);
			if (members.size() > 0) {
				text = text.append(Component.text("\n\u00a77" + level + "s: "));
				for (int x = -1; ++x < Math.min(5, members.size());)
					text = text.append(PlayerProfile.getDisplayName(members.get(x)).append(Component.text("\u00a78" + (x+1<5? ", " : "..."))));
			}
		}
		return text;
	}
	
	protected Component component;

	@NotNull
	public Component toComponent() {
		if (component != null) return component;
		Component text = Component.text(
					"\u00a7r" + getDisplayName() +
					"\n\u00a7e(\u00a78" + getMinimumPoint().toString() + "\u00a77 - \u00a78" + getMaximumPoint() + "\u00a7e)" +
					"\n\u00a77Priority: \u00a7b" + getPriority())
					.append(getComponentMembers());
		text = text.colorIfAbsent(getColour());
		Component done = getColouredName().hoverEvent(HoverEvent.showText(text));
		component = Component.empty().append(done.clickEvent(ClickEvent.suggestCommand("/region warpto " + getName())));
		return component;
	}
	
	public void updateMapEntry() {
		rm.refreshRegion(this);
	}
	
	public void delete() {
		rm.removeRegion(this);
		List<EntityRegionCrystal> concurrentSafe = new ArrayList<>(crystals);
		concurrentSafe.forEach(crystal -> crystal.remove(Entity.RemovalReason.DISCARDED));
	}
	
	public boolean canModify(@NotNull Player p) {
		return p.hasPermission("bean.region.modifyothers") || getMember(p).higherThan(MemberLevel.OFFICER);
	}

	@Override
	public int compareTo(Region otherRegion) {
		return Integer.compare(otherRegion.getPriority(), getPriority());
	}

	public boolean doesPlayerBypass(Player p, Flag<?> flag) {
		MemberLevel level = getMember(p);

		if (flag instanceof FlagMember mFlag)
			return getEffectiveFlag(mFlag).lowerOrEqTo(level);
		if (flag instanceof FlagMemberBoolean mbFlag)
			return level.higherThan(MemberLevel.VISITOR) || getEffectiveFlag(mbFlag);
		if (flag instanceof FlagBoolean bFlag)
			return getEffectiveFlag(bFlag);
		return false;
	}

	@Override
	@NotNull
	protected RegionType getType() {
		return RegionType.DEFINED;
	}

	public void addCrystal(EntityRegionCrystal crystal) {
		this.crystals.add(crystal);
	}

	public int getCrystalCount() {
		return crystals.size();
	}

	public int getMaxCrystals() {
		return 1;
	}

	public void removeCrystal(EntityRegionCrystal crystal) {
		this.crystals.remove(crystal);
	}
	
}
