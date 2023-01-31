package io.github.levtey.Compressors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;
import redempt.redlib.config.ConfigManager;
import redempt.redlib.itemutils.ItemBuilder;

public class Compressors extends JavaPlugin {
	protected ConfigManager config;
	public NamespacedKey compressorKey = new NamespacedKey(this, "compressor");
	
	public void onEnable() {
		reloadConfigs();
		new CompressorsCommand(this);
		new Listeners(this);
	}
	
	public void reloadConfigs() {
        config = ConfigManager.create(this).target(Config.class).saveDefaults().load();
	}
	
	public ItemStack compressor() {
		return new ItemBuilder(Material.DROPPER)
				.setName(makeReadable(Config.itemName))
				.setLore(Config.lore.stream().map(Compressors::makeReadable).toArray(String[]::new))
				.addPersistentTag(compressorKey, PersistentDataType.BYTE, (byte) 1);
	}
	
	public static String makeReadable(String input) {
		return ChatColor.translateAlternateColorCodes('&', input);
	}

}
