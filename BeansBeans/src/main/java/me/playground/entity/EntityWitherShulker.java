package me.playground.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.item.DyeColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R2.CraftWorld;
import org.bukkit.entity.Entity;

public class EntityWitherShulker extends Shulker implements IBeanEntity {

	protected EntityWitherShulker(Location location) {
		super(EntityType.SHULKER, ((CraftWorld)location.getWorld()).getHandle());
		this.entityData.set(DATA_COLOR_ID, (byte)DyeColor.BLACK.getId());
	}

	@Override
	public void postCreation() {

	}

	@Override
	public void transferData(Entity oldEntity) {

	}
}
