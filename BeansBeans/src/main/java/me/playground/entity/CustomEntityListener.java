package me.playground.entity;

import me.playground.listeners.EventListener;
import me.playground.main.Main;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.world.EntitiesLoadEvent;

public class CustomEntityListener extends EventListener {

    private final CustomEntityManager manager;

    public CustomEntityListener(Main plugin, CustomEntityManager manager) {
        super(plugin);
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChunkLoad(EntitiesLoadEvent e) {
        for (Entity ent : e.getEntities()) {
            if (ent.getPersistentDataContainer().isEmpty()) continue;
            manager.tryToConvert(ent);
        }
    }

}
