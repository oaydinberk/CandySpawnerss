package com.candyspawners.oyun;

import com.candyspawners.CandySpawners;
import com.candyspawners.dosyalar.CagiriciVeriYukleyici;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.TileState;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class SpawnerSandikManager {

    // Aktif sandıkları ve zamanlayıcıları tutan HashMap'ler
    private static final ConcurrentHashMap<Location, BukkitTask> sandikZamanlayicilar = new ConcurrentHashMap<>();
    private static final NamespacedKey spawnerKey = new NamespacedKey(CandySpawners.getInstance(), "spawner_turu");

    public static void sandikAktifEt(Block block) {
        if (!(block.getState() instanceof Chest)) return;

        Location loc = block.getLocation();
        Chest sandik = (Chest) block.getState();
        PersistentDataContainer data = ((TileState) sandik).getPersistentDataContainer();

        if (!data.has(spawnerKey, PersistentDataType.STRING)) {
            Bukkit.getLogger().warning(" Oyunda Hatalı Bir Spawner var NBT'sinde spawner_turu bilgisi yok! Konum: " + loc);
            return;
        }
        String spawnerTuru = data.get(spawnerKey, PersistentDataType.STRING);

        if (spawnerTuru == null) {
            Bukkit.getLogger().warning(" Oyundaki bir spawnerin türü NBT'den çekilemedi! Konum: " + loc);
            return;
        }

        int sure = CagiriciVeriYukleyici.getSure(spawnerTuru);

        // Zamanlayıcıyı yalnızca bir kez başlat
        if (sandikZamanlayicilar.containsKey(loc)) {
        	// Bukkit.getLogger().info("[DEBUG] Zamanlayıcı zaten mevcut, güncelleniyor.");
            return; // Zamanlayıcı zaten varsa, yeni bir tane başlatma
        }

        // Her sandık için bağımsız bir zamanlayıcı oluştur
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(CandySpawners.getInstance(), () -> {
            Block sandikBlock = loc.getBlock();

            if (!(sandikBlock.getState() instanceof Chest)) {
                durdurSandik(sandikBlock);
                return;
            }

            Chest sandikGuncel = (Chest) sandikBlock.getState();
            Inventory inv = sandikGuncel.getBlockInventory();
            List<Material> itemList = CagiriciVeriYukleyici.getItems(spawnerTuru);

            if (itemList.isEmpty()) {
                Bukkit.getLogger().warning("HATA: " + spawnerTuru + " türü için cagiricilar.yml'den item bilgileri alınamadı!");
                return;
            }

            for (Material item : itemList) {
                ItemStack stack = new ItemStack(item, 1);

                if (inv.firstEmpty() == -1) {
                    sandikBlock.getWorld().dropItemNaturally(sandikBlock.getLocation(), stack);
                } else {
                    inv.addItem(stack);
                }
            }

        }, sure * 20L, sure * 20L); // Sandığın süresine göre çalıştır

        sandikZamanlayicilar.put(loc, task); // Zamanlayıcıyı kaydet
    }


    public static void durdurSandik(Block block) {
        Location loc = block.getLocation();

        if (!sandikZamanlayicilar.containsKey(loc)) return;

        // Sandık zamanlayıcısını iptal et
        BukkitTask task = sandikZamanlayicilar.remove(loc);
        if (task != null) {
            task.cancel();
            // Bukkit.getLogger().info("[DEBUG] Zamanlayıcı durduruldu: " + loc);
        }
    }

}