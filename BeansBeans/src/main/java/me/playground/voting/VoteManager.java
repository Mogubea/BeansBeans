package me.playground.voting;

import java.io.File;
import java.security.KeyPair;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import me.playground.data.Datasource;
import me.playground.main.IPluginRef;
import me.playground.main.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;

public class VoteManager implements IPluginRef {
	
	// Will contain the list of valid services that we receive votes from.
	// This will be displayed in the Support Us! GUI and recorded in player stats.
	final private HashMap<String, VoteService> validServices = Datasource.getValidVoteServices();
	final private ItemStack voteBook = new ItemStack(Material.WRITTEN_BOOK);
	
	final private Main plugin;
	final private VoteRSA rsaManager;
	private VoteReceiver voteReceiver;
	private KeyPair keyPair;
	
	public VoteManager(Main plugin) {
		updateVoteBook();
		
		this.rsaManager = new VoteRSA();
		this.plugin = plugin;
		
		if (!plugin.getDataFolder().exists())
			plugin.getDataFolder().mkdir();
		
		File voteConfig = new File(plugin.getDataFolder() + "/voteConfiguration.yml");
		YamlConfiguration cfg = YamlConfiguration.loadConfiguration(voteConfig);
		File voteRSA = new File(plugin.getDataFolder() + "/rsa");
		
		String hostAddr = Bukkit.getServer().getIp();
		
		if (!voteConfig.exists()) {
			try {
				plugin.getLogger().warning("Couldn't find voteConfiguration.yml, creating a new one..");
				voteConfig.createNewFile();
				cfg.set("host", hostAddr);
				cfg.set("port", 8192);
				cfg.save(voteConfig);
			} catch (Exception e) {
				plugin.getLogger().severe("There was a problem creating a new voteConfiguration.yml.");
				e.printStackTrace();
			}
		}
		
		try {
			if (!voteRSA.exists()) {
				voteRSA.mkdir();
				keyPair = rsaManager.generate(2048);
				rsaManager.save(voteRSA, keyPair);
			} else {
				keyPair = rsaManager.load(voteRSA);
			}
		} catch (Exception e) {
			plugin.getLogger().severe("Failure reading configuration file or RSA keys.");
			e.printStackTrace();
			return;
		}
		
		String host = cfg.getString("host", hostAddr);
		int port = cfg.getInt("port", 8192);
		
		try {
			voteReceiver = new VoteReceiver(this, host, port);
			voteReceiver.start();
			plugin.getLogger().info("Now listening for votes..");
		} catch (Exception e) {
			plugin.getLogger().severe("Error beginning vote listening thread..");
			e.printStackTrace();
		}
	}
	
	public void shutdown() {
		if (voteReceiver == null) return;
		voteReceiver.shutdown();
		plugin.getLogger().info("No longer listening for votes..");
	}
	
	public KeyPair getKeyPair() {
		return keyPair;
	}
	
	public VoteRSA getRSAManager() {
		return rsaManager;
	}

	@Override
	public Main getPlugin() {
		return plugin;
	}
	
	public VoteService getService(String service) {
		return this.validServices.get(service);
	}
	
	public void reloadServiceList() {
		this.validServices.clear();
		this.validServices.putAll(Datasource.getValidVoteServices());
	}
	
	public ItemStack getVoteBook() {
		return voteBook;
	}
	
	private void updateVoteBook() {
		BookMeta bmeta = (BookMeta) voteBook.getItemMeta();
		bmeta.author(Component.text("Server"));
		bmeta.title(Component.text("Votelist"));
		Component comp = Component.text("\u00a7lSites:");
		for (VoteService vs : validServices.values())
			comp = comp.append(Component.text("\n" + vs.getDisplayName() + "\n\u00a71" + vs.getServiceName()).clickEvent(ClickEvent.openUrl(vs.getServiceName())));
		
		bmeta.pages(comp);
		voteBook.setItemMeta(bmeta);
	}
	
}
