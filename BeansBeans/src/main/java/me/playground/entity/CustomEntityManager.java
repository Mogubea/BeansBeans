package me.playground.entity;

import me.playground.main.Main;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class CustomEntityManager {

    protected final NamespacedKey KEY_ENTITY_TYPE;

    public CustomEntityManager(Main plugin) {
        KEY_ENTITY_TYPE = plugin.getKey("CUSTOM_ENTITY_ID");
    }

    protected void tryToConvert(Entity oldEntity) {
        PersistentDataContainer container = oldEntity.getPersistentDataContainer();
        String identifier = container.get(KEY_ENTITY_TYPE, PersistentDataType.STRING);
        if (identifier == null) return;

        CustomEntityType.fromIdentifier(identifier).replace(oldEntity);
    }

}
