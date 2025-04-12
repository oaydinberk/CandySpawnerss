package com.candyspawners.oyun;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class OyuncuGUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack clickedItem = event.getCurrentItem(); 
        if (clickedItem != null && event.getView().getTitle().equals("§8§nSpawner Bilgileri")) {
            event.setCancelled(true);
        }
    }
}
