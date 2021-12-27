package me.playground.shop;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.World;
import org.dynmap.markers.Marker;

import me.playground.data.DynmapDatasource;
import me.playground.main.Main;
import me.playground.utils.Utils;

/**
 * An instance of {@link DynmapDatasource} for {@link Shop} data management.
 * @author Mogubean
 */
public class ShopDatasource extends DynmapDatasource<Shop> {
	private final ShopManager manager;
	
	public ShopDatasource(Main plugin, ShopManager manager) {
		super(plugin, "Shops");
		this.manager = manager;
	}
	
	/**
	 * Load all shops
	 */
	@Override
	public void loadAll() {
		final long then = System.currentTimeMillis();
			
		try(Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("SELECT * FROM shops"); ResultSet r = s.executeQuery()) {
			while(r.next()) {
				final World w = getWorld(r.getShort("world"));
				if (w == null) continue;
				
				final int id = r.getInt("id");
				final String item = r.getString("compressedItem");
				
				new Shop(manager,
						id, 
						r.getInt("ownerId"), 
						new Location(w, r.getShort("x")+0.5F, r.getShort("y")-0.38F, r.getShort("z")+0.5F, r.getShort("yaw"), 0), 
						r.getInt("maxStorage"),
						r.getInt("itemsStored"),
						(item == null ? null : Utils.itemStackFromBase64(item)), 
						r.getInt("storedMoney"), 
						r.getInt("totalMoneyEarned"), 
						r.getInt("totalMoneyTaxed"),
						r.getInt("sellPrice"),
						r.getInt("buyPrice"));
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
		Main.getInstance().getLogger().info("Loaded " + manager.countShops() + " Shops in " + (System.currentTimeMillis()-then) + "ms");
	}

	/**
	 * Save all dirty shops
	 */
	@Override
	public void saveAll() {
		manager.getShops().values().forEach(shop -> {
			if (!shop.isDirty()) return;
			saveDirtyShop(shop);
		});
	}
	
	/**
	 * Attempt to delete the specified {@link Shop} from the database.
	 * @return whether the deletion was successful or not.
	 */
	protected boolean deleteShop(Shop shop) {
		try(Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("DELETE FROM shops WHERE id = ?")) {
			s.setInt(1, shop.getShopId());
			s.executeUpdate();
			removeMarker(shop);
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Attempt to create a new {@link Shop}.
	 * @return the newly created {@link Shop}, or {@code null} if unsuccessful.
	 */
	public Shop createShop(int creatorId, Location loc) {
		Shop sh = null;
		
		try(Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("INSERT INTO shops (ownerId,world,x,y,z,yaw) VALUES (?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
			s.setInt(1, creatorId);
			s.setInt(2, getWorldId(loc.getWorld()));
			s.setFloat(3, (int)loc.getX());
			s.setFloat(4, (int)loc.getY());
			s.setFloat(5, (int)loc.getZ());
			s.setFloat(6, (int)loc.getYaw());
			s.executeUpdate();
			
			ResultSet r = s.getGeneratedKeys();
			r.next();
			sh = new Shop(manager, r.getInt(1), creatorId, loc.add(0.5, -0.38, 0.5), 1728);
			r.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return sh;
	}
	
	private void saveDirtyShop(Shop shop) {
		try(Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("UPDATE shops SET maxStorage = ?, itemsStored = ?, compressedItem = ?, storedMoney = ?, totalMoneyEarned = ?, totalMoneyTaxed = ?, sellPrice = ?, buyPrice = ?, ownerId = ? WHERE id = ?")) {
			s.setInt(1, shop.getMaxItemQuantity());
			s.setInt(2, shop.getItemQuantity());
			s.setString(3, shop.getItemStack() == null ? null : Utils.toBase64(shop.getItemStack()));
			s.setInt(4, shop.getStoredMoney());
			s.setInt(5, shop.getTotalMoneyEarned());
			s.setInt(6, shop.getTotalMoneyTaxed());
			s.setInt(7, shop.getSellPrice());
			s.setInt(8, shop.getBuyPrice());
			s.setInt(9, shop.getOwnerId());
			s.setInt(10, shop.getShopId());
			s.executeUpdate();
			updateMarker(shop);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	public boolean logShopAction(int shopId, int playerId, String comment, String data) {
		try(Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("INSERT INTO shops_log (shopId,playerId,comment,data) VALUES (?,?,?,?)")) {
			s.setInt(1, shopId);
			s.setInt(2, playerId);
			s.setString(3, comment);
			s.setString(4, data);
			s.executeUpdate();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public ArrayList<ShopLog> loadShopLogs(Integer shopId) {
		final ArrayList<ShopLog> logList = new ArrayList<ShopLog>();
		
		try(Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("SELECT time,playerId,comment,data FROM shops_log WHERE shopId = ?")){
			s.setInt(1, shopId);
			ResultSet r = s.executeQuery();
			
			while(r.next()) {
				final ShopLog sl = new ShopLog(r.getTimestamp("time"), r.getInt("playerId"), r.getString("comment"), r.getString("data"));
				logList.add(sl);
			}
			r.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return logList;
	}
	
	// XXX: Markers
	
	@Override
	public void updateMarker(Shop s) {
		if (!isDynmapEnabled()) return;
		if (s.getItemStack() == null || (s.getBuyPrice() == 0 && s.getSellPrice() == 0))
			return;
		
		Location l = s.getLocation();
		
		getMarkerSet().createMarker("shop."+s.getShopId(), "Player Shop", false, l.getWorld().getName(), l.getX(), l.getY()+1, l.getZ(), getMarkerAPI().getMarkerIcon("diamond"), false);
	}
	
	@Override
	public void removeMarker(Shop s) {
		if (!isDynmapEnabled()) return;
		
		Marker m = getMarkerSet().findMarker("shop."+s.getShopId());
		if (m != null) m.deleteMarker();
	}
}
