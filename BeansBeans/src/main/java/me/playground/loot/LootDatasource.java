package me.playground.loot;

import me.playground.data.PrivateDatasource;
import me.playground.items.BeanItem;
import me.playground.main.Main;
import me.playground.utils.Utils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.json.JSONObject;

import java.sql.*;
import java.util.ArrayList;

public class LootDatasource extends PrivateDatasource {

    private final LootManager manager;

    protected LootDatasource(Main plugin, LootManager manager) {
        super(plugin);
        this.manager = manager;
    }

    @Override
    public void loadAll() {
        try(Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("SELECT * FROM loot"); ResultSet r = s.executeQuery()) {
            while(r.next()) {
                final String itemType = r.getString("itemType");
                final String compressedStack = r.getString("compressedStack");
                final LootTable table = manager.getOrCreateTable(r.getString("tableName"));
                ItemStack i;

                if (compressedStack != null) {
                    i = Utils.itemStackFromBase64(compressedStack);
                } else {
                    try {
                        i = new ItemStack(Material.valueOf(itemType));
                    } catch (Exception e) {
                        BeanItem bi = BeanItem.from(itemType);
                        if (bi == null)
                            i = new ItemStack(Material.DIAMOND);
                        else
                            i = bi.getItemStack();
                    }
                }

                String data = r.getString("data");
                Material cookedType = null;
                try {
                    cookedType = Material.valueOf(r.getString("cookedType"));
                } catch (Exception ignored) {
                }

                table.addEntry(new LootEntry(r.getInt("id"), table, i, cookedType, r.getInt("minStack"), r.getInt("maxStack"), r.getFloat("chance"), r.getFloat("chanceLuckChange"), data == null ? null : new JSONObject(data)).setFlags(r.getByte("flags"))
                        .setRequiresPlayer(r.getBoolean("requiresPlayer"), false));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveAll() throws Exception {
        final ArrayList<LootEntry> entries = manager.getAllEntries();
        final int size = entries.size();

        for (int x = -1; ++x < size;) {
            LootEntry entry = entries.get(x);
            if (!entry.isDirty()) continue;
            updateLootEntry(entry);
            entry.setDirty(false);
        }
    }

    protected void updateLootEntry(LootEntry entry) {
        try(Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("UPDATE loot SET tableName = ?, chance = ?, chanceLuckChange = ?, minStack = ?, maxStack = ?, itemType = ?, flags = ?, compressedStack = ?, data = ?, cookedType = ?, requiresPlayer = ? WHERE id = ?")) {
            int idx = 0;

            s.setString(++idx, entry.getTable().getName());
            s.setFloat(++idx, entry.getChance());
            s.setFloat(++idx, entry.getLuckEffectiveness());
            s.setInt(++idx, entry.getMinStackSize());
            s.setInt(++idx, entry.getMaxStackSize());

            String identifier = BeanItem.getIdentifier(entry.getDisplayStack());

            s.setString(++idx, identifier);
            s.setByte(++idx, entry.getFlags());

            s.setString(++idx, entry.shouldCompress() ? Utils.toBase64(entry.getDisplayStack()) : null);

            JSONObject data = entry.getJsonData(); // Additional json information such as enchantments, levels and chances.
            s.setString(++idx, data != null ? data.toString() : null);
            s.setString(++idx, entry.getCookedMaterial().name());

            s.setBoolean(++idx, entry.requiresPlayer());

            s.setInt(++idx, entry.getId());
            s.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected LootEntry registerLootEntry(LootTable table, ItemStack itemStack, int min, int max, int chance, int luckEffectiveness, byte flags) throws SQLException {
        ResultSet r = null;
        try(Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("INSERT INTO loot (tableName, chance, chanceLuckChange, minStack, maxStack, itemType, flags) VALUES (?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
            s.setString(1, table.getName());
            s.setFloat(2, chance);
            s.setFloat(3, luckEffectiveness);
            s.setInt(4, min);
            s.setInt(5, max);

            String identifier = BeanItem.getIdentifier(itemStack);

            s.setString(6, identifier);
            s.setByte(7, flags);

            s.executeUpdate();
            r = s.getGeneratedKeys();
            if (r.next())
                return new LootEntry(r.getInt(1), table, itemStack, min, max, chance, luckEffectiveness).setFlags(flags);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            r.close();
        }
        return null;
    }

}
