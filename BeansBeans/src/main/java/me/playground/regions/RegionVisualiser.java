package me.playground.regions;

import me.playground.playerprofile.PlayerProfile;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

public class RegionVisualiser {

    public static final int INTERVAL = 16;

    private final PlayerProfile profile;
    private final Player player;
    private final Region region;
    private int ticks;
//    private int viewDistance = 64;

    private double volume;
    private int gridSize = 4;
    private int orangeSize = 16;
    private int step = 1;

    /**
     * @param ticks -1 ticks is infinite
     */
    public RegionVisualiser(PlayerProfile profile, Region region, int ticks) {
        if (!profile.isOnline()) throw new UnsupportedOperationException("Player must be online in order to visualise a Region.");

        this.profile = profile;
        this.player = profile.getPlayer();
        this.region = region;
        this.ticks = ticks;

        this.volume = region.getBoundingBox().getVolume();

        // To reduce the amount of particles for larger regions in an attempt to limit the amount of particle packets being sent to players.
        if (volume >= 2000000) { step = 3; }
        else if (volume >= 750000) { gridSize = 16; step = 2; orangeSize = 64; }
        else if (volume >= 125000) { gridSize = 8; step = 2; }
    }

//    protected void setViewDistance(int blocks) {
//        this.viewDistance = blocks;
//    }

    protected void tick() {
        // If the player isn't online or isn't in the same world; just stop bothering to show.
        if (player == null || player.getWorld() != region.getWorld()) {
            profile.unvisualiseRegion(region);
            return;
        }

        final BoundingBox box = region.getBoundingBox();
        final Particle particle = Particle.SOUL_FIRE_FLAME;
        final Particle chunkBound = Particle.FLAME; // Every gridSize * 4, use regular flames to mark chunk boundaries

        int maxX = (int) (box.getMaxX() + 1), maxY = (int) (box.getMaxY() + 1), maxZ = (int) (box.getMaxZ() + 1);

        for (int x = (int) (box.getMinX() - 1); ++x < (maxX + 1);) {
            int xBounds = (x == box.getMinX() || x == maxX) ? 1 : 0;
            if (step > 1 && xBounds == 0 && x % step != 0) continue; // Trim

            int xA = x % gridSize == 0 && xBounds == 0 ? 1 : 0; // If this x is divisible by gridSize, exclude x boundaries
            int x16 = x % orangeSize == 0 && xBounds == 0 ? 1 : 0; // If this x is divisible by orangeSize, exclude x boundaries

            for (int y = (int) (box.getMinY() - 1); ++y < (maxY + 1);) {
                int yBounds = y == box.getMinY() || y == maxY ? 1 : 0;
                if (step > 1 && yBounds == 0 && y % step != 0) continue; // Trim

                int yA = y % gridSize == 0 && yBounds == 0 ? 1 : 0; // If this y is divisible by gridSize, exclude y boundaries
                int y16 = y % orangeSize == 0 && yBounds == 0 ? 1 : 0; // If this y is divisible by orangeSize, exclude y boundaries

                for (int z = (int) (box.getMinZ() - 1); ++z < (maxZ + 1);) {
                    int zBounds = z == box.getMinZ() || z == maxZ ? 1 : 0;
                    if (step > 1 && zBounds == 0 && z % step != 0) continue; // Trim

                    int outerBounds = xBounds + yBounds + zBounds; // Out of 3 on boundaries
                    int distanceA = xA + yA + (z % gridSize == 0 && zBounds == 0 ? 1 : 0); // Out of 3 divisible by gridSize, exclude z bounds
                    int distance16 = x16 + y16 + (z % orangeSize == 0 && zBounds == 0 ? 1 : 0); // Out of 3 divisible by orangeSize, exclude z bounds

                    switch (outerBounds) {
                        case 1 -> { if (distanceA < 1) continue; player.spawnParticle(distance16 > 0 ? chunkBound : particle, x, y, z, 1, 0, 0, 0, 0); } // Creates a grid-like visual
                        case 2, 3 -> player.spawnParticle(particle, x, y, z, 1, 0, 0, 0, 0); // Particle every block for the edges
                    }
                }
            }
        }

        if (ticks != -1 && (ticks-=INTERVAL) <= 0)
            profile.unvisualiseRegion(region);
    }

    public int getTicksRemaining() {
        return ticks;
    }
}
