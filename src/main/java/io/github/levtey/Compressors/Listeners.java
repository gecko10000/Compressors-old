package io.github.levtey.Compressors;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Dropper;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import redempt.redlib.itemutils.ItemUtils;
import redempt.redlib.misc.Task;

public class Listeners implements Listener {
	
	private Compressors plugin;
	
	public Listeners(Compressors plugin) {
		this.plugin = plugin;
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler (ignoreCancelled = true)
	public void onPlace(BlockPlaceEvent evt) {
		if (evt.getItemInHand().getType() == Material.AIR) return;
		if (!evt.getItemInHand().getItemMeta().getPersistentDataContainer().has(plugin.compressorKey, PersistentDataType.BYTE)) return;
		Block block = evt.getBlock();
		if (block.getType() != Material.DROPPER) return;
		if (!evt.getPlayer().hasPermission("compressors.place")) {
			evt.setCancelled(true);
			return;
		}
		Dropper dropper = (Dropper) block.getState();
		dropper.getPersistentDataContainer().set(plugin.compressorKey, PersistentDataType.BYTE, (byte) 1);
		dropper.update(true);
	}
	
	@EventHandler (ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void onBreak(BlockDropItemEvent evt) {
		if (!(evt.getBlockState() instanceof Dropper)) return;
		Dropper dropper = (Dropper) evt.getBlockState();
		Location location = dropper.getLocation();
		if (!dropper.getPersistentDataContainer().has(plugin.compressorKey, PersistentDataType.BYTE)) return;
		if (evt.getItems().isEmpty()) return;
		evt.getItems().remove(evt.getItems().size() - 1);
		location.getWorld().dropItemNaturally(location, plugin.compressor());
	}
	
	@SuppressWarnings("unchecked")
	@EventHandler (ignoreCancelled = true)
	public void onDispense(BlockDispenseEvent evt) {
		Block block = evt.getBlock();
		if (block.getType() != Material.DROPPER) return;
        Dropper dropper = (Dropper) block.getState();
		if (!dropper.getPersistentDataContainer().has(plugin.compressorKey, PersistentDataType.BYTE)) return;
		String selectedRecipe = null;
		ConfigurationSection recipeSection = plugin.config.getConfig().getConfigurationSection("recipes");
		if (recipeSection == null) return;
		boolean hasDispensedItem = false;
		ItemStack dispensedItem = evt.getItem();
		for (String key : recipeSection.getKeys(false)) {
			List<ItemStack> requiredItems = (List<ItemStack>) recipeSection.getList(key + ".from");
			boolean valid = true;
			hasDispensedItem = false;
			for (ItemStack requiredItem : requiredItems) {
                // subtracting is required because the inventory technically has the dispensed item missing
				if (ItemUtils.count(dropper.getInventory(), requiredItem) < requiredItem.getAmount() + (requiredItem.isSimilar(dispensedItem) ? -1 : 0)) {
					valid = false;
					break;
				}
				if (requiredItem.isSimilar(evt.getItem())) hasDispensedItem = true;
			}
			if (!valid || !hasDispensedItem) continue;
			selectedRecipe = key;
			break;
		}
		if (!hasDispensedItem) {
			//evt.setCancelled(true);
			return;
		}
		if (selectedRecipe == null) {
			evt.setCancelled(true);
			return;
		}
		ItemStack to = recipeSection.getItemStack(selectedRecipe + ".to");
		Material type = to.getType();
		evt.setItem(to);
		String selectedCopy = selectedRecipe;
        boolean removedOriginal = false;
		Task.syncDelayed(() -> {
			for (ItemStack item : (List<ItemStack>) recipeSection.getList(selectedCopy + ".from")) {
                int amount = item.getAmount();
                if (!removedOriginal && item.isSimilar(dispensedItem)) amount--;
				ItemUtils.remove(dropper.getInventory(), item, amount);
			}
		});
	}

}
