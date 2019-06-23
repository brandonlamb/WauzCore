package eu.wauz.wauzcore.menu;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import eu.wauz.wauzcore.data.players.PlayerConfigurator;
import eu.wauz.wauzcore.events.WauzPlayerEventCharacterDelete;
import eu.wauz.wauzcore.menu.util.MenuUtils;
import eu.wauz.wauzcore.menu.util.WauzInventory;
import eu.wauz.wauzcore.menu.util.WauzInventoryHolder;
import eu.wauz.wauzcore.players.CharacterManager;
import eu.wauz.wauzcore.players.WauzPlayerData;
import eu.wauz.wauzcore.players.WauzPlayerDataPool;
import eu.wauz.wauzcore.system.util.WauzDateUtils;
import eu.wauz.wauzcore.system.util.WauzMode;
import net.md_5.bungee.api.ChatColor;

public class CharacterSlotMenu implements WauzInventory {
	
	public static void open(PlayerInteractEntityEvent event, WauzMode wauzMode) {
		WauzInventoryHolder holder = new WauzInventoryHolder(new CharacterSlotMenu());
		Inventory menu = Bukkit.createInventory(holder, 9, ChatColor.BLACK + "" + ChatColor.BOLD + "Choose your Character!");
		
		Player player = event.getPlayer();
		event.setCancelled(true);
		
		if(wauzMode.equals(WauzMode.MMORPG)) {
			for(int slotId = 1; slotId <= 3; slotId++)
				menu.setItem(slotId * 2, getCharacterSlot(player, slotId, true));
		}
		else if(wauzMode.equals(WauzMode.SURVIVAL)) {
			menu.setItem(4, getCharacterSlot(player, WauzDateUtils.getSurvivalSeasonInteger(), true));
		}
		
		MenuUtils.setBorders(menu);
		player.openInventory(menu);
	}
	
	private static ItemStack getCharacterSlot(Player player, int slotId, boolean deletable) {
		boolean characterExists = PlayerConfigurator.doesCharacterExist(player, slotId);
		ItemStack slot = new ItemStack(Material.TOTEM_OF_UNDYING);
		ItemMeta im = slot.getItemMeta();
		im.setDisplayName((characterExists ? ChatColor.GOLD : ChatColor.RED) + "Slot " + slotId);
		List<String> lores = new ArrayList<String>();
		
		if(characterExists) {
			lores.add(ChatColor.WHITE 
					+ PlayerConfigurator.getRaceString(player, slotId) + ", "
					+ PlayerConfigurator.getWorldString(player, slotId) + ", "
					+ PlayerConfigurator.getLevelString(player, slotId));
			lores.add("");
			lores.add(ChatColor.GRAY + "Last Played: " + ChatColor.GREEN
					+ PlayerConfigurator.getLastCharacterLogin(player, slotId) + " ago");
			if(slotId > 20000)
				lores.add(ChatColor.GRAY + "End of Season: " + ChatColor.RED
						+ WauzDateUtils.getTimeTillNextSeason());
			if(deletable)
				lores.add(ChatColor.GRAY + "Right Click to Delete");
		}
		else
			lores.add(ChatColor.GRAY + "Empty");
		
		im.setLore(lores);
		slot.setItemMeta(im);
		return slot;
	}
	
	public void selectMenuPoint(InventoryClickEvent event) {
		event.setCancelled(true);
		ItemStack clicked = event.getCurrentItem();
		final Player player = (Player) event.getWhoClicked();
		WauzPlayerData pd = WauzPlayerDataPool.getPlayer(player);
		
		if(pd == null)
			return;
		
		if(clicked == null || (!clicked.getType().equals(Material.TOTEM_OF_UNDYING) && !clicked.getType().equals(Material.BARRIER)))
			return;

		int slotId = Integer.parseInt(clicked.getItemMeta().getDisplayName().split(" ")[1]);
		pd.setSelectedCharacterSlot("char" + slotId);		
		
		String clickedName = clicked.getItemMeta().getDisplayName();
		if(clickedName.contains("" + ChatColor.RED))
			if(slotId > 20000) {
				pd.setSelectedCharacterWorld("Survival");
				pd.setSelectedCharacterRace(WauzDateUtils.getSurvivalSeason());
				CharacterManager.createCharacter(player, WauzMode.SURVIVAL);
			}
			else
				CharacterWorldMenu.open(player);
		else if(event.getClick().toString().contains("RIGHT")) {
			String characterSlot = pd.getSelectedCharacterSlot();
			String characterSlotNumber = characterSlot.substring(4, 5);
			pd.setWauzPlayerEventName("Delete Char " + characterSlotNumber);
			pd.setWauzPlayerEvent(new WauzPlayerEventCharacterDelete());
			WauzDialog.open(player, getCharacterSlot(player, event.getSlot() / 2, false));
		}
		else
			CharacterManager.loginCharacter(player, WauzMode.getMode(PlayerConfigurator.getCharacterWorldString(player)));
	}

}