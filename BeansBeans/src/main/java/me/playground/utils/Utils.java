package me.playground.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;

import me.playground.currency.Currency;
import me.playground.ranks.Rank;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class Utils {

	public static DecimalFormat numberFormat = new DecimalFormat("#,###");

	public static String toString(Object[] array, boolean forceLower, String seperator) {
		String newString = "";
		int remaining = array.length;
		for (Object o : array) {
			newString += (forceLower ? o.toString().toLowerCase() : o.toString()) + (remaining != 1 ? seperator : "");
			remaining--;
		}
		return newString;
	}

	public static String toString(List<?> arraylist, boolean forceLower, String seperator) {
		String newString = "";
		int remaining = arraylist.size();
		for (Object o : arraylist) {
			newString += (forceLower ? o.toString().toLowerCase() : o.toString()) + (remaining != 1 ? seperator : "");
			remaining--;
		}
		return newString;
	}
	
	public static String toString(Set<?> arraylist, boolean forceLower, String seperator) {
		String newString = "";
		int remaining = arraylist.size();
		for (Object o : arraylist) {
			newString += (forceLower ? o.toString().toLowerCase() : o.toString()) + (remaining != 1 ? seperator : "");
			remaining--;
		}
		return newString;
	}

	public static String firstCharUpper(String str) {
		return (str.charAt(0) + "").toUpperCase() + str.substring(1).toLowerCase();
	}

	public static String currencyString(Currency type, long amount) {
		return type.getColour() + numberFormat.format(amount) + " " + type.getFriendlyString();
	}

	public static void setPacketValue(Object obj, String name, Object value) {
		try {
			Field field = obj.getClass().getDeclaredField(name);
			field.setAccessible(true);
			field.set(obj, value);
		} catch (Exception e) {

		}
	}
	
	public static String toBase64(Object obj) {
		try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            
            dataOutput.writeObject(obj);
            dataOutput.close();
            
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to compress object.", e);
        }
	}
	
	public static Object fromBase64(String base64) {
		try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(base64));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            Object item = dataInput.readObject();
            
            dataInput.close();
            return item;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to decompress object.", e);
        }
	}
	
	
	public static ItemStack itemStackFromBase64(String base64) {
		try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(base64));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack item = (ItemStack) dataInput.readObject();
            
            dataInput.close();
            return item;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to decompress itemstack.", e);
        }
	}
	
	public static String itemStackArrayToBase64(ItemStack[] items) throws IllegalStateException {
    	try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            
            // Write the size of the inventory
            dataOutput.writeInt(items.length);
            
            // Save every element in the list
            for (int i = 0; i < items.length; i++) {
                dataOutput.writeObject(items[i]);
            }
            
            // Serialize that array
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save item stacks.", e);
        }
    }
	
	public static ItemStack[] itemStackArrayFromBase64(String data) {
    	try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack[] items = new ItemStack[dataInput.readInt()];
    
            // Read the serialized inventory
            for (int i = 0; i < items.length; i++) {
            	items[i] = (ItemStack) dataInput.readObject();
            }
            
            dataInput.close();
            return items;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to decode class type.", e);
        }
    }
	
	public static ItemStack getSkullWithCustomSkin(String base64) {
		ItemStack i = new ItemStack(Material.PLAYER_HEAD,1);
		SkullMeta meta = (SkullMeta) i.getItemMeta();
		PlayerProfile ack = Bukkit.createProfile(UUID.randomUUID());
		ack.getProperties().add(new ProfileProperty("textures", base64));
		meta.setPlayerProfile(ack);
		i.setItemMeta(meta);
		return i;
	}
	
	/**
	 * For a skull that'll likely be used as an itemstack, using GameProfile's rather than PlayerProfile's for valid consistency.
	 */
	public static ItemStack getSkullWithCustomSkin(UUID uuid, String base64) {
		ItemStack i = new ItemStack(Material.PLAYER_HEAD,1);
		SkullMeta meta = (SkullMeta) i.getItemMeta();
		PlayerProfile ack = Bukkit.createProfile(uuid, null);
		ack.getProperties().add(new ProfileProperty("textures", base64));
		meta.setPlayerProfile(ack);
		i.setItemMeta(meta);
		return i;
	}
	
	public static ItemStack getDyedLeather(Material mat, int hex) {
		ItemStack leather = new ItemStack(mat);
		ItemMeta meta = leather.getItemMeta();
		if (meta instanceof LeatherArmorMeta) {
			LeatherArmorMeta lam = (LeatherArmorMeta) meta;
			lam.setColor(Color.fromRGB(hex));
			leather.setItemMeta(lam);
		}
		return leather;
	}
	
	public static ItemStack getSkullFromPlayer(OfflinePlayer p) {
		ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
		SkullMeta meta = (SkullMeta) skull.getItemMeta();
		meta.setOwningPlayer(p);
		skull.setItemMeta(meta);
		
		return skull;
	}
	
	public static ItemStack getSkullFromPlayer(UUID uuid) {
		ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
		SkullMeta meta = (SkullMeta) skull.getItemMeta();
		meta.setPlayerProfile(Bukkit.createProfile(uuid));
		skull.setItemMeta(meta);
		
		return skull;
	}
	
	public static void spawnParticleCircle(Location location1, Particle particle, int particles, float radius) {
		for (int i = 0; i < particles; i++) {
			double angle, x, z;
			angle = 2 * Math.PI * i / particles;
			x = Math.cos(angle) * radius;
			z = Math.sin(angle) * radius;
			location1.add(x, 0, z);
			location1.getWorld().spawnParticle(particle, location1, i);
			location1.subtract(x, 0, z);
		}
	}
	
	public static Component getProgressBar(char symbol, int amount, long value, long maxvalue, int baseColor, int filledColor) {
		Component component = Component.empty();
		int todo = (int)(((float)value/(float)maxvalue) * (float)amount);
		
		for (int c = -1; ++c < 2;) {
			String sb = "";
			
			for (int x = -1; ++x < todo;)
				sb += symbol;
			component = component.append(Component.text(sb, TextColor.color(c == 0 ? filledColor : baseColor)));
			todo = amount - todo;
		}
		
		return component.decoration(TextDecoration.ITALIC, false);
	}
	
	/**
	 * Get an online player whether it's by nickname or username.
	 * 
	 * @param name - Search
	 * @return Player
	 */
	@SuppressWarnings("unchecked")
	public static Player playerPartialMatch(String name) {
		final Collection<Player> online = (Collection<Player>) Bukkit.getServer().getOnlinePlayers();
		List<Player> targets = new ArrayList<Player>();
		
		targets.addAll(online);

		for (final Player p : targets) {
			//if (p.getCustomName().equalsIgnoreCase(name))
			//	return p;
			if (p.getName().equalsIgnoreCase(name))
				return p;
		}

		String lowerName = name.toLowerCase();
		if (lowerName.length() >= 3) {
			for (final Player p : targets) {
				//if (p.getCustomName().toLowerCase().contains(lowerName))
				//	return p;
				if (p.getName().toLowerCase().contains(lowerName))
					return p;
			}
		}
		return null;
	}
	
	public static void sendActionBar(Rank rank, Component message) {
		Bukkit.getOnlinePlayers().forEach((player) -> {
			me.playground.playerprofile.PlayerProfile pp = me.playground.playerprofile.PlayerProfile.from(player);
			if (!pp.isRank(rank)) return;
			player.sendActionBar(message);
		});
	}
	
	public static String timeStringFromNow(long timeInMillis) {
		long cur = System.currentTimeMillis();
		long secs = (timeInMillis > cur ? timeInMillis - cur : cur - timeInMillis) / 1000;
		boolean tooShort = secs < 60;
		long mins = secs / 60;
		secs -= mins*60;
		long hours = mins / 60;
		mins -= hours*60;
		long days = hours / 24;
		hours -= days*24;
		long weeks = days / 7;
		days -= weeks*7;
		
		if (tooShort) 
			return "a few seconds";
		if (weeks > 0)
			return weeks + (weeks > 1 ? " Weeks" : " Week") + (days > 0 ? " and " + days + (days > 1 ? " Days" : " Day") : "");
		if (days > 0)
			return days + (days > 1 ? " Days" : " Day") + (hours > 0 ? " and " + hours + (hours > 1 ? " Hours" : " Hour") : "");
		if (hours > 0)
			return hours + (hours > 1 ? " Hours" : " Hour") + (mins > 0 ? " and " + mins + (mins > 1 ? " Minutes" : " Minute") : "");
		
		return mins + (mins > 1 ? " Minutes" : " Minute");
	}
	
	public static String timeStringFromMillis(long timeInMillis) {
		long cur = System.currentTimeMillis();
		return timeStringFromNow(timeInMillis + cur);
	}
	
	private final static TreeMap<Integer, String> map = new TreeMap<Integer, String>();

    static {

        map.put(1000, "M");
        map.put(900, "CM");
        map.put(500, "D");
        map.put(400, "CD");
        map.put(100, "C");
        map.put(90, "XC");
        map.put(50, "L");
        map.put(40, "XL");
        map.put(10, "X");
        map.put(9, "IX");
        map.put(5, "V");
        map.put(4, "IV");
        map.put(1, "I");

    }

    public final static String toRoman(int number) {
        int l =  map.floorKey(number);
        if ( number == l ) {
            return map.get(number);
        }
        return map.get(l) + toRoman(number-l);
    }
}
