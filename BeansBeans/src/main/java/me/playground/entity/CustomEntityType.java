package me.playground.entity;

import me.playground.main.Main;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R3.CraftWorld;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class CustomEntityType<T extends Entity> {

    private static final Map<String, CustomEntityType<?>> byIdentifier = new HashMap<>();
    private static final CustomEntityManager manager = Main.getInstance().getCustomEntityManager();

    public static final CustomEntityType<EntityRegionCrystal> REGION_CRYSTAL;
    public static final CustomEntityType<EntityHologram> HOLOGRAM;
    public static final CustomEntityType<EntityWitherShulker> WITHER_SHULKER;

    static {
        REGION_CRYSTAL = register("REGION_CRYSTAL", EntityRegionCrystal::new);
        HOLOGRAM = register("HOLOGRAM", EntityHologram::new);
        WITHER_SHULKER = register("WITHER_SHULKER", EntityWitherShulker::new);
    }

    private final String identifier;
    private final EntityFactory<T> factory;

    private CustomEntityType(String identifier, EntityFactory<T> factory) {
        this.identifier = identifier.toUpperCase();
        this.factory = factory;
    }

    @NotNull
    private static <T extends Entity> CustomEntityType<T> register(String identifier, EntityFactory<T> test) {
        CustomEntityType<T> type = new CustomEntityType<>(identifier, test);
        byIdentifier.put(identifier, type);
        return type;
    }

    @Contract("null -> null")
    public static CustomEntityType<?> fromIdentifier(String identifier) {
        return byIdentifier.get(identifier);
    }

    @NotNull
    public T spawn(Location location) {
        if (factory == null) throw new UnsupportedOperationException("Spawning this entity via CustomEntityType#spawn isn't possible.");

        ServerLevel level = ((CraftWorld)location.getWorld()).getHandle();
        T newEntity = factory.create(location);
        org.bukkit.entity.Entity bukkitEnt = newEntity.getBukkitEntity();
        bukkitEnt.getPersistentDataContainer().set(manager.KEY_ENTITY_TYPE, PersistentDataType.STRING, identifier);

        IBeanEntity beEnt = (IBeanEntity) newEntity;
        beEnt.postCreation();

        level.addFreshEntity(newEntity, CreatureSpawnEvent.SpawnReason.CUSTOM);
        return newEntity;
    }

    public T replace(org.bukkit.entity.Entity oldEntity) {
        if (factory == null) throw new UnsupportedOperationException("Replacing this entity via CustomEntityType#spawn isn't possible.");

        ServerLevel level = ((CraftWorld)oldEntity.getWorld()).getHandle();
        T newEntity = factory.create(oldEntity.getLocation());
        org.bukkit.entity.Entity bukkitEnt = newEntity.getBukkitEntity();
        bukkitEnt.getPersistentDataContainer().set(manager.KEY_ENTITY_TYPE, PersistentDataType.STRING, identifier);

        IBeanEntity beEnt = (IBeanEntity) newEntity;
        beEnt.transferData(oldEntity);
        beEnt.postCreation();
        oldEntity.remove();

        level.addFreshEntity(newEntity, CreatureSpawnEvent.SpawnReason.CUSTOM);
        return newEntity;
    }

    private interface EntityFactory<T extends Entity> {
        T create(Location location);
    }
}
