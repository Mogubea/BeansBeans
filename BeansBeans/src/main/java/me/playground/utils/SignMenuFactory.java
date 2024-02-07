package me.playground.utils;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

import me.playground.gui.BeanGui;
import me.playground.playerprofile.PlayerProfile;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;

import net.kyori.adventure.text.Component;

public final class SignMenuFactory {
	
    private final Plugin plugin;
    private final Map<Player, Menu> inputs;

    public SignMenuFactory(Plugin plugin) {
        this.plugin = plugin;
        this.inputs = new HashMap<>();
        this.listen();
    }

    public Menu newMenu(List<Component> text) {
        return new Menu(text);
    }
    
    public Menu newMenu(List<Component> text, Material sign) {
        return new Menu(sign, text);
    }

    private void listen() {
    	ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(this.plugin, PacketType.Play.Client.UPDATE_SIGN) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                Player player = event.getPlayer();
                Menu menu = inputs.remove(player);

                if (menu == null) return;
                
                event.setCancelled(true);

                boolean success = menu.response.test(player, event.getPacket().getStringArrays().read(0));

                if (!success && menu.reopenIfFail && !menu.forceClose) {
                    Bukkit.getScheduler().runTaskLater(plugin, () -> menu.open(player), 2L);
                }
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (player.isOnline()) {
                        player.sendBlockChange(menu.location, menu.location.getBlock().getBlockData());
                    }
                }, 2L);
            }
        });
    }

    public void requestSignResponse(Player p, Material signType, Consumer<String[]> consumer, boolean reopenGui, String...text) {
        BeanGui beanGui = PlayerProfile.from(p).getBeanGui();
        p.closeInventory();

        List<Component> preText = new ArrayList<>();
        preText.add(Component.empty());
        if (text.length < 2)
            preText.add(Component.empty());
        preText.add(Component.text("\u00a78^^^^^^^^^^"));
        if (text.length > 0)
            preText.add(Component.text(text[0]));
        if (text.length > 1)
            preText.add(Component.text(text[1]));

        SignMenuFactory.Menu menu = newMenu(preText, signType)
                .reopenIfFail(true)
                .response((player, strings) -> {
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        try {
                            consumer.accept(strings);
                            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.5F, 0.8F);
                        } catch (RuntimeException ex) {
                            p.sendActionBar(Component.text(ex.getMessage(), NamedTextColor.RED));
                            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5F, 0.8F);
                        }

                        if (reopenGui && beanGui != null)
                            beanGui.openInventory();
                    }, 1L);
                    return true;
                });
        menu.open(p);
    }

    public final class Menu {

        private final List<Component> text;
        private final Material signMat;

        private BiPredicate<Player, String[]> response;
        private boolean reopenIfFail;

        private Location location;

        private boolean forceClose;

        Menu(List<Component> text) {
            this(Material.OAK_SIGN, text);
        }
        
        Menu(Material sign, List<Component> text) {
        	this.text = text;
        	this.signMat = sign;
        }

        public Menu reopenIfFail(boolean value) {
            this.reopenIfFail = value;
            return this;
        }

        public Menu response(BiPredicate<Player, String[]> response) {
            this.response = response;
            return this;
        }

		public void open(Player player) {
        	Objects.requireNonNull(player, "player");
            if (!player.isOnline()) {
                return;
            }
            location = player.getLocation();
            location.setY(location.getBlockY() - 4);
            
            player.sendBlockChange(location, signMat.createBlockData());
            player.sendSignChange(location, text);
            
            PacketContainer openSign = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.OPEN_SIGN_EDITOR);
            BlockPosition position = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
            openSign.getBlockPositionModifier().write(0, position);
            player.closeInventory();
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, openSign);

            inputs.put(player, this);
        }

        /**
         * closes the menu. if force is true, the menu will close and will ignore the reopen
         * functionality. false by default.
         *
         * @param player the player
         * @param force decides whether or not it will reopen if reopen is enabled
         */
        public void close(Player player, boolean force) {
            this.forceClose = force;
            if (player.isOnline()) {
                player.closeInventory();
            }
        }

        public void close(Player player) {
            close(player, false);
        }
    }
}