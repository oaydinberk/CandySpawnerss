package com.candyspawners.oyun;

import org.bukkit.Bukkit;

import com.candyspawners.CandySpawners;
import com.candyspawners.dosyalar.CagiriciVeriYukleyici;
import com.candyspawners.dosyalar.SandikVeriYukleyici;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.block.TileState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Particle;
import org.bukkit.Sound;
public class OzelSandikListener implements Listener {

    private final CandySpawners plugin;
    private final NamespacedKey ccKey, spawnerKey, uuidKey;
    private final boolean holographicDisplaysYuklu;
    public OzelSandikListener(CandySpawners plugin) {
        this.plugin = plugin;
        this.ccKey = new NamespacedKey(plugin, "CC");
        this.spawnerKey = new NamespacedKey(plugin, "spawner_turu");
		this.uuidKey = new NamespacedKey(plugin, "uuid");
		this.holographicDisplaysYuklu = Bukkit.getPluginManager().getPlugin("HolographicDisplays") != null;
    }
    String prefix = CandySpawners.getInstance().getPrefix();

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player SandikAcanOyuncu = event.getPlayer();

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || !SandikAcanOyuncu.isSneaking()) {
            return;
        }

        Block block = event.getClickedBlock();
        
        if (block == null || block.getType() != Material.CHEST || !(block.getState() instanceof TileState)) {
            return;
        }

        TileState tile = (TileState) block.getState();
        PersistentDataContainer blockData = tile.getPersistentDataContainer();
        
        if (!blockData.has(ccKey, PersistentDataType.INTEGER) || !blockData.has(spawnerKey, PersistentDataType.STRING)) {
            return;
        }
        String spawnerTuru = blockData.get(spawnerKey, PersistentDataType.STRING);
        int sure = CagiriciVeriYukleyici.getSure(spawnerTuru);
        String isim = CagiriciVeriYukleyici.getIsim(spawnerTuru);
        String lore = CagiriciVeriYukleyici.getLore(spawnerTuru).get(0);

        String spawnerInfo = "┭━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┭\n"
                + "§eSpawner: " + isim + "\n"
                + lore + "\n"
                + "§aHer " + sure + " saniyede bir eşya veriyor!§f\n"
                + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━";
        
        SandikAcanOyuncu.sendMessage(spawnerInfo); 
    }


    @EventHandler
    public void sandikYerlesince(BlockPlaceEvent event) {
        Block block = event.getBlockPlaced();
        Player oyuncu = event.getPlayer();
        ItemStack item = event.getItemInHand();

        if (item.getType() == Material.CHEST && item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer itemData = meta.getPersistentDataContainer();

            if (itemData.has(ccKey, PersistentDataType.INTEGER) && itemData.has(spawnerKey, PersistentDataType.STRING)) {
                String spawnerTuru = itemData.get(spawnerKey, PersistentDataType.STRING);
                int cc = itemData.get(ccKey, PersistentDataType.INTEGER);
                String uuid = itemData.get(uuidKey, PersistentDataType.STRING); // UUID'yi alıyoruz

                // UUID'yi kontrol et
                if (SandikVeriYukleyici.uuidKontrol(oyuncu.getUniqueId(), uuid)) {
                    oyuncu.sendMessage(prefix + "§cBu UUID zaten başka bir spawner tarafından kullanılıyor.");
                    event.setCancelled(true); // Sandığın yerleştirilmesini engelliyoruz
                    return;
                }

                if (block.getState() instanceof TileState) {
                    TileState tile = (TileState) block.getState();
                    PersistentDataContainer blockData = tile.getPersistentDataContainer();
                    String isim = CagiriciVeriYukleyici.getIsim(spawnerTuru);

                    // Blok verilerini yerleştir
                    blockData.set(ccKey, PersistentDataType.INTEGER, cc);
                    blockData.set(spawnerKey, PersistentDataType.STRING, spawnerTuru);
                    blockData.set(uuidKey, PersistentDataType.STRING, uuid);
                    blockData.set(new NamespacedKey(plugin, "sahip"), PersistentDataType.STRING, oyuncu.getName());
                    tile.update();

                    // ✅ Yeni sistemle kayıt
                    SandikVeriYukleyici.sandikKaydet(
                        oyuncu.getUniqueId(), // UUID
                        block.getLocation(),
                        spawnerTuru,
                        oyuncu.getName(),
                        cc,
                        uuid,
                        isim
                    );

                    // Hologram ekleme
                    if (holographicDisplaysYuklu) {
                        Location holoLoc = block.getLocation().clone().add(0.5, 1.5, 0.5);
                        Hologram hologram = HologramsAPI.createHologram(plugin, holoLoc);
                        hologram.appendTextLine("§eSpawner: " + isim);
                        hologram.appendTextLine("§6Sahibi: §e" + oyuncu.getName());
                    }

                    // Aktifleştirme ve efektler
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        SpawnerSandikManager.sandikAktifEt(block);
                    }, 20L);

                    block.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, block.getLocation().clone().add(0.5, 1, 0.5), 50);
                    oyuncu.playSound(oyuncu.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_PLACE, 1.0f, 1.0f);
                    oyuncu.sendMessage(prefix + "§aBir §e" + isim + " §asandığı yerleştirdiniz!");
                }
            }
        }
    }



    
    @EventHandler
    public void sandikKirinca(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player oyuncu = event.getPlayer();

        // Event cancel edilmemişse devam et
        if (event.isCancelled()) {
        	// Bukkit.getLogger().info("[DEBUG] BlockBreakEvent iptal edilmiş.");
            return;
        }

        if (block.getType() == Material.CHEST && block.getState() instanceof TileState) {
            TileState tile = (TileState) block.getState();
            PersistentDataContainer blockData = tile.getPersistentDataContainer();

            if (blockData.has(ccKey, PersistentDataType.INTEGER) && blockData.has(spawnerKey, PersistentDataType.STRING)) {
                int cc = blockData.get(ccKey, PersistentDataType.INTEGER);
                String spawnerTuru = blockData.get(spawnerKey, PersistentDataType.STRING);
                String sahipIsmi = blockData.get(new NamespacedKey(plugin, "sahip"), PersistentDataType.STRING);
                String uuid = blockData.get(uuidKey, PersistentDataType.STRING);

                // Bukkit.getLogger().info("[DEBUG] Spawner kırıldı! Sahip: " + sahipIsmi + " | Tür: " + spawnerTuru);

                UUID sahipUUID = Bukkit.getOfflinePlayer(sahipIsmi).getUniqueId();
                UUID kiriciUUID = oyuncu.getUniqueId();
                boolean adminMi = oyuncu.hasPermission("candyspawners.admin");

                Location loc = block.getLocation();
                String key = loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();

                // Bukkit.getLogger().info("[DEBUG] Lokasyon anahtarı: " + key);

                if (adminMi || oyuncu.getName().equals(sahipIsmi)) {
                	event.setDropItems(false);
                	if (block.getState() instanceof Container) {
                	    Container container = (Container) block.getState();
                	    Inventory inv = container.getInventory();

                	    for (ItemStack item : inv.getContents()) {
                	        if (item != null && item.getType() != Material.AIR) {
                	            block.getWorld().dropItemNaturally(block.getLocation(), item);
                	        }
                	    }
                	    inv.clear();
                	}

                    // Item verisi oluştur
                    String isim = CagiriciVeriYukleyici.getIsim(spawnerTuru);
                    List<String> lore = CagiriciVeriYukleyici.getLore(spawnerTuru);

                    ItemStack yeniSandik = new ItemStack(Material.CHEST, 1);
                    ItemMeta meta = yeniSandik.getItemMeta();
                    PersistentDataContainer itemData = meta.getPersistentDataContainer();

                    meta.setDisplayName(isim);
                    meta.setLore(lore);

                    itemData.set(ccKey, PersistentDataType.INTEGER, cc);
                    itemData.set(spawnerKey, PersistentDataType.STRING, spawnerTuru);
                    itemData.set(new NamespacedKey(plugin, "sahip"), PersistentDataType.STRING, sahipIsmi);
                    itemData.set(uuidKey, PersistentDataType.STRING, uuid);

                    yeniSandik.setItemMeta(meta);

                    block.getWorld().dropItemNaturally(loc, yeniSandik);
                    // Bukkit.getLogger().info("[DEBUG] Spawner itemi yere atıldı.");

                 // Hologram varsa sil
                    if (holographicDisplaysYuklu) {
                    	// Bukkit.getLogger().info("[DEBUG] Hologram silme işlemi başlatılıyor...");
                        HologramsAPI.getHolograms(plugin).stream()
                            .filter(h -> {
                                Location hologramLoc = h.getLocation();
                                Location targetLoc = loc.clone().add(0.5, 1.5, 0.5);
                                if (!hologramLoc.getWorld().equals(targetLoc.getWorld())) {
                                    return false;
                                }
                                return hologramLoc.distance(targetLoc) < 1;
                            })
                            .forEach(h -> {
                            	// Bukkit.getLogger().info("[DEBUG] Hologram silindi");
                                h.delete(); // Hologramı sil
                            });
                    } else {
                    	// Bukkit.getLogger().warning("[DEBUG] HolographicDisplays yüklü değil! Hologram silinmedi.");
                    }

                    // Veritabanından sil (her zaman sahip UUID'si ile sil)
                    // Bukkit.getLogger().info("[DEBUG] Veritabanından veri siliniyor... UUID: " + sahipUUID);
                    SandikVeriYukleyici.sandikSil(sahipUUID, loc);

                    // Sandık işlemini durdur
                    // Bukkit.getLogger().info("[DEBUG] SpawnerSandikManager.durdurSandik çağrılıyor.");
                    SpawnerSandikManager.durdurSandik(block);
                } else {
                    // Yetkisi yoksa işlemi iptal et
                	// Bukkit.getLogger().info("[DEBUG] Oyuncunun bu sandığı kırma yetkisi yok!");
                    event.setCancelled(true);
                    oyuncu.sendMessage(prefix + "§cBu spawnerin sahibi siz değilsiniz!");
                }
            } else {
            	// Bukkit.getLogger().info("[DEBUG] Blokta geçerli spawner verisi yok.");
            }
        } else {
        	// Bukkit.getLogger().info("[DEBUG] Kırılan blok sandık değil veya TileState değil.");
        }
    }




    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId(); // Değiştirildi

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Map<Location, Map<String, String>> oyuncuSandiklari = SandikVeriYukleyici.getOyuncuSandiklari(uuid);

            Bukkit.getScheduler().runTask(plugin, () -> {
                for (Map.Entry<Location, Map<String, String>> entry : oyuncuSandiklari.entrySet()) {
                    Location loc = entry.getKey();
                    Map<String, String> veri = entry.getValue();
                    String spawnerTuru = veri.get("spawnerTuru");
                    String spawnerAdi = veri.get("spawnerAdi");

                    Block block = loc.getBlock();
                    if (block.getType() != Material.CHEST || !(block.getState() instanceof TileState)) continue;

                    if (holographicDisplaysYuklu) {
                        Hologram hologram = HologramsAPI.createHologram(plugin, loc.clone().add(0.5, 1.5, 0.5));
                        hologram.appendTextLine("§eSpawner: " + spawnerAdi);
                        hologram.appendTextLine("§6Sahibi: §e" + player.getName());
                    }

                    TileState tile = (TileState) block.getState();
                    PersistentDataContainer blockData = tile.getPersistentDataContainer();
                    int cc = CagiriciVeriYukleyici.getCC(spawnerTuru);

                    blockData.set(ccKey, PersistentDataType.INTEGER, cc);
                    blockData.set(spawnerKey, PersistentDataType.STRING, spawnerTuru);
                    tile.update();

                    SpawnerSandikManager.sandikAktifEt(block);
                }
            });
        });
    }


    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        Map<Location, Map<String, String>> oyuncuSandiklari = SandikVeriYukleyici.getOyuncuSandiklari(uuid);

        for (Location loc : oyuncuSandiklari.keySet()) {
            Block block = loc.getBlock();

            SpawnerSandikManager.durdurSandik(block);

            if (holographicDisplaysYuklu) {
                HologramsAPI.getHolograms(plugin).stream()
                    .filter(h -> {
                        Location hologramLoc = h.getLocation();
                        Location targetLoc = loc.clone().add(0.5, 1.5, 0.5);
                        
                        if (!hologramLoc.getWorld().equals(targetLoc.getWorld())) {
                            return false;
                        }
                        return hologramLoc.distance(targetLoc) < 1;
                    })
                    .forEach(h -> {
                        // Bukkit.getLogger().info("[DEBUG] " + player.getName() + " çıkış yaptı. Hologram silindi.");
                        h.delete(); // Hologramı sil
                    });
            }
        }
    }





}
