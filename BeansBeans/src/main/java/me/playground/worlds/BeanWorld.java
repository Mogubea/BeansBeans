package me.playground.worlds;

import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

public class BeanWorld {

    private final World world;
    private WorldLogic logic;

    protected BeanWorld(@NotNull World world) {
        this.world = world;
        this.logic = new WorldLogic(this);
    }

    @NotNull
    public World getBukkitWorld() {
        return world;
    }

    @NotNull
    protected WorldLogic getWorldLogic() {
        return logic;
    }

    protected void setWorldLogic(@NotNull WorldLogic logic) {
        this.logic = logic;
    }

}
