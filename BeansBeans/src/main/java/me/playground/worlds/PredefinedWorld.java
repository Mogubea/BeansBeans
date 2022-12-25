package me.playground.worlds;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import me.playground.worlds.generation.TestChunkGenerator;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Main;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.ai.village.VillageSiege;
import net.minecraft.world.entity.npc.CatSpawner;
import net.minecraft.world.entity.npc.WanderingTraderSpawner;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.WorldData;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_19_R2.CraftServer;
import org.bukkit.craftbukkit.v1_19_R2.generator.CraftWorldInfo;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PredefinedWorld {

    private static final Map<String, PredefinedWorld> byIdentifier = new HashMap<>();

    static {
        new PredefinedWorld("MINING", OptionalLong.of(18000L), false, true, false, false, 4D, false, false, false, false, -256, 384, 384, 0.0F)
                .setChunkGenerator(new TestChunkGenerator());
    }

    private final OptionalLong fixedTime;
    private final boolean hasSkylight;
    private final boolean hasCeiling;
    private final boolean ultraWarm;
    private final boolean natural;
    private final double coordinateScaling;
    private final boolean piglinSafe;
    private final boolean bedWorks;
    private final boolean respawnAnchorWorks;
    private final boolean hasRaids;
    private final int minY;
    private final int maxY;
    private final int logicalY;
    private final float ambientLight;

    private Difficulty difficulty = Difficulty.NORMAL;
    private ChunkGenerator chunkGenerator;
    private BiomeProvider biomeProvider;
    private WorldBorder worldBorder;

    /**
     * @param identifier The identifier of this predefined world.
     * @param fixedTime If given a long value, that will be the time for this world, forever.
     * @param hasSkylight Whether the sky of this world emits light.
     * @param hasCeiling Whether this world has a ceiling or not. This does NOT create a ceiling on its own.
     * @param ultraWarm Determines if water can be placed and if lava flow is accelerated.
     * @param natural Does many things, including determining if a clock will work in that dimension.
     * @param coordinateScaling How distance travelled in this world is reflected in the base world.
     * @param piglinSafe Whether this world is safe for Piglins.
     * @param bedWorks Whether beds work or explode in this world.
     * @param respawnAnchorWorks Whether respawn anchors work or explode in this world.
     * @param hasRaids Whether raids are enabled in this world.
     * @param minY The minimum Y of this world. <b>Must be a multiple of 16 and below maxY</b>.
     * @param maxY The maximum Y of this world. <b>Must be a multiple of 16 and above 0 and minY</b>.
     * @param logicalY The logical Y axis which determines the maximum spawn height for various things such as portals and entities. <b>Must be equal to or below maxY</b>
     * @param ambientLight This affects how bright lighting shadows are.
     */
    protected PredefinedWorld(@NotNull String identifier, OptionalLong fixedTime, boolean hasSkylight, boolean hasCeiling, boolean ultraWarm, boolean natural, double coordinateScaling, boolean piglinSafe, boolean bedWorks, boolean respawnAnchorWorks, boolean hasRaids, int minY, int maxY, int logicalY, float ambientLight) {
        this.fixedTime = fixedTime;
        this.hasSkylight = hasSkylight;
        this.hasCeiling = hasCeiling;
        this.ultraWarm = ultraWarm;
        this.natural = natural;
        this.coordinateScaling = coordinateScaling;
        this.piglinSafe = piglinSafe;
        this.bedWorks = bedWorks;
        this.respawnAnchorWorks = respawnAnchorWorks;
        this.hasRaids = hasRaids;
        this.minY = minY;
        this.maxY = maxY;
        this.logicalY = logicalY;
        this.ambientLight = ambientLight;

        byIdentifier.put(identifier, this);
    }

    @Nullable
    @Contract("null -> null")
    public static PredefinedWorld getPredefinedWorld(String identifier) {
        return byIdentifier.get(identifier);
    }

    protected PredefinedWorld setWorldBorder(@NotNull WorldBorder border) {
        if (worldBorder != null) throw new IllegalArgumentException("Default WorldBorder is already set.");
        worldBorder = border;
        return this;
    }

    protected PredefinedWorld setChunkGenerator(@NotNull ChunkGenerator generator) {
        if (chunkGenerator != null) throw new IllegalArgumentException("Default ChunkGenerator is already set.");
        chunkGenerator = generator;
        return this;
    }

    protected PredefinedWorld setDifficulty(@NotNull Difficulty difficulty) {
        this.difficulty = difficulty;
        return this;
    }

    /**
     * Fires once the world is successfully created.
     */
    protected void postWorldCreation(World world) {

    }

    /**
     * Essentially an override allowing for custom {@link DimensionType} injection.
     * @see CraftServer#createWorld(WorldCreator)
     * @return the created {@link World}.
     */
    protected World createWorld(WorldCreator creator) {
        String name = creator.name();
        File folder = new File(Bukkit.getWorldContainer(), name);
        World world = Bukkit.getWorld(name);
        if (world != null) {
            return world;
        } else if (folder.exists() && !folder.isDirectory()) {
            throw new IllegalArgumentException("File exists with the name '" + name + "' and isn't a folder");
        } else {

            // Null check
            if (chunkGenerator == null)
                chunkGenerator = ((CraftServer)Bukkit.getServer()).getGenerator(name);
            if (biomeProvider == null)
                biomeProvider = ((CraftServer)Bukkit.getServer()).getBiomeProvider(name);

            TagKey<Block> infiniburnTag = BlockTags.INFINIBURN_OVERWORLD;
            ResourceKey actualDimension = LevelStem.OVERWORLD;
            ResourceLocation dimensionEffects = BuiltinDimensionTypes.OVERWORLD_EFFECTS;

            switch (creator.environment()) {
                case NETHER -> { actualDimension = LevelStem.NETHER; infiniburnTag = BlockTags.INFINIBURN_NETHER; dimensionEffects = BuiltinDimensionTypes.NETHER_EFFECTS; }
                case THE_END -> { actualDimension = LevelStem.END; infiniburnTag = BlockTags.INFINIBURN_END; dimensionEffects = BuiltinDimensionTypes.END_EFFECTS; }
            }

            LevelStorageSource.LevelStorageAccess worldSession;
            try {
                worldSession = LevelStorageSource.createDefault((Bukkit.getServer().getWorldContainer().toPath())).createAccess(name, actualDimension);
            } catch (IOException var23) {
                throw new RuntimeException(var23);
            }

            DedicatedServer console = ((CraftServer)Bukkit.getServer()).getServer();

            boolean hardcore = creator.hardcore();

            WorldLoader.DataLoadContext worldloader_a = console.worldLoader;
            net.minecraft.core.Registry<LevelStem> iregistry = worldloader_a.datapackDimensions().registryOrThrow(Registries.LEVEL_STEM);
            DynamicOps<Tag> dynamicops = RegistryOps.create(NbtOps.INSTANCE, worldloader_a.datapackWorldgen());
            Pair<WorldData, WorldDimensions.Complete> pair = worldSession.getDataTag(dynamicops, worldloader_a.dataConfiguration(), iregistry, worldloader_a.datapackWorldgen().allRegistriesLifecycle());
            PrimaryLevelData worlddata;
            if (pair != null) {
                worlddata = (PrimaryLevelData)pair.getFirst();
                iregistry = pair.getSecond().dimensions();
            } else {
                WorldOptions worldoptions = new WorldOptions(creator.seed(), creator.generateStructures(), false);
                DedicatedServerProperties.WorldDimensionData properties = new DedicatedServerProperties.WorldDimensionData(GsonHelper.parse(creator.generatorSettings().isEmpty() ? "{}" : creator.generatorSettings()), creator.type().name().toLowerCase(Locale.ROOT));
                LevelSettings worldsettings = new LevelSettings(name, GameType.byId(Bukkit.getDefaultGameMode().getValue()), hardcore, Difficulty.EASY, false, new GameRules(), worldloader_a.dataConfiguration());
                WorldDimensions worlddimensions = properties.create(worldloader_a.datapackWorldgen());
                WorldDimensions.Complete worlddimensions_b = worlddimensions.bake(iregistry);
                Lifecycle lifecycle = worlddimensions_b.lifecycle().add(worldloader_a.datapackWorldgen().allRegistriesLifecycle());
                worlddata = new PrimaryLevelData(worldsettings, worldoptions, worlddimensions_b.specialWorldProperty(), lifecycle);
                iregistry = worlddimensions_b.dimensions();
            }

            worlddata.customDimensions = iregistry;
            worlddata.checkName(name);
            worlddata.setModdedInfo(console.getServerModName(), console.getModdedStatus().shouldReportAsModified());

            if (console.options.has("forceUpgrade"))
                net.minecraft.server.Main.forceUpgrade(worldSession, DataFixers.getDataFixer(), console.options.has("eraseCache"), () -> true, iregistry);

            long j = BiomeManager.obfuscateSeed(creator.seed());
            List<CustomSpawner> list = ImmutableList.of(new PhantomSpawner(), new PatrolSpawner(), new CatSpawner(), new VillageSiege(), new WanderingTraderSpawner(worlddata));
            LevelStem worldDimension = iregistry.get(actualDimension);

            // Set the DimensionType Holder
            DimensionType type = new DimensionType(fixedTime, hasSkylight, hasCeiling, ultraWarm, natural, coordinateScaling, bedWorks, respawnAnchorWorks,
                   minY, maxY, logicalY, infiniburnTag, dimensionEffects, ambientLight, new DimensionType.MonsterSettings(piglinSafe, hasRaids, UniformInt.of(0, 7), 0));
            Holder<DimensionType> holder = Holder.direct(type);
            worldDimension = new LevelStem(holder, worldDimension.generator());

            // World Info, the thing before the World.
            WorldInfo worldInfo = new CraftWorldInfo(worlddata, worldSession, creator.environment(), holder.value());
            if (biomeProvider == null && chunkGenerator != null)
                biomeProvider = chunkGenerator.getDefaultBiomeProvider(worldInfo);

            // Silly Bukkit level name stuff.
            String levelName = console.getProperties().levelName;
            ResourceKey worldKey;
            if (name.equals(levelName + "_nether")) {
                worldKey = net.minecraft.world.level.Level.NETHER;
            } else if (name.equals(levelName + "_the_end")) {
                worldKey = net.minecraft.world.level.Level.END;
            } else {
                worldKey = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(name.toLowerCase(Locale.ENGLISH)));
            }

            // Create the NMS World
            ServerLevel internal = new ServerLevel(console, console.executor, worldSession, worlddata, worldKey, worldDimension, console.progressListenerFactory.create(11), worlddata.isDebugWorld(), j, creator.environment() == World.Environment.NORMAL ? list : ImmutableList.of(), true, creator.environment(), chunkGenerator, biomeProvider);

            console.initWorld(internal, worlddata, worlddata, worlddata.worldGenOptions());
            internal.setSpawnSettings(true, true);
            console.addLevel(internal);
            console.prepareLevels(internal.getChunkSource().chunkMap.progressListener, internal);
            internal.entityManager.tick();
            Bukkit.getPluginManager().callEvent(new WorldLoadEvent(internal.getWorld()));

            //
            if (worldBorder != null)
                postWorldSetBorder(internal.getWorld());

            postWorldCreation(internal.getWorld());
            //

            return internal.getWorld();
        }
    }

    /**
     * Updates the world border to what we want it to be.
     */
    private void postWorldSetBorder(World world) {
        WorldBorder border = world.getWorldBorder();
        border.setSize(worldBorder.getSize());
        border.setDamageBuffer(worldBorder.getDamageBuffer());
        border.setDamageAmount(worldBorder.getDamageAmount());
        border.setWarningTime(worldBorder.getWarningTime());
        border.setWarningDistance(worldBorder.getWarningDistance());
    }

}
