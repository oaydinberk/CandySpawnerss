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


    // Sandık Yerleştirildiğinde Kaydedelim
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
                if (SandikVeriYukleyici.uuidKontrol(uuid)) {
                    oyuncu.sendMessage(prefix + "§cBu UUID zaten başka bir spawner tarafından kullanılıyor. Bu spawner illegal bir spawner veya kopyalanmış sahte bir spawner. Lütfen yeni UUID'ye sahip spawner almak için komutları kullanınız");
                    event.setCancelled(true); // Sandığın yerleştirilmesini engelliyoruz
                    return;
                }

                if (block.getState() instanceof TileState) {
                    TileState tile = (TileState) block.getState();
                    PersistentDataContainer blockData = tile.getPersistentDataContainer();
                    String isim = CagiriciVeriYukleyici.getIsim(spawnerTuru);

                    // Koyan oyuncunun adı
                    blockData.set(ccKey, PersistentDataType.INTEGER, cc);
                    blockData.set(spawnerKey, PersistentDataType.STRING, spawnerTuru);
                    blockData.set(uuidKey, PersistentDataType.STRING, uuid); // UUID ekleme
                    blockData.set(new NamespacedKey(plugin, "sahip"), PersistentDataType.STRING, oyuncu.getName()); // Sahip ekleme
                    tile.update();

                    // Sandığı sandiklar.yml içine kaydedelim (Oyuncu adıyla)
                    // **Hologram ekleme (Eğer eklenti yüklü ise)**
                    if (holographicDisplaysYuklu) {
                        Location holoLoc = block.getLocation().add(0.5, 1.5, 0.5);
                        Hologram hologram = HologramsAPI.createHologram(plugin, holoLoc);
                        hologram.appendTextLine("§eSpawner: " + isim);
                        hologram.appendTextLine("§6Sahibi: §e" + oyuncu.getName());
                        SandikVeriYukleyici.sandikKaydet(block.getLocation(), spawnerTuru, oyuncu.getName(), cc, uuid, isim);
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            SpawnerSandikManager.sandikAktifEt(block);
                        }, 20L);

                        // Sandık yerleştirme efektleri
                        block.getWorld().spawnParticle(Particle.FIREWORKS_SPARK,event.getBlockPlaced().getLocation().add(0.5, 1, 0.5), 50);
                        
                        oyuncu.playSound(oyuncu.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_PLACE, 1.0f, 1.0f);
                        oyuncu.sendMessage(prefix + "§aBir §e" + isim + " §asandığı yerleştirdiniz!");
                    }else {
                    	 oyuncu.sendMessage(prefix + "§aHolograms API Yüklenemedi! Sunucu Sahibine Bildiriniz");
                    	 return;
                    }
                }
            }
        }
    }


    
    
    @EventHandler
    public void sandikKirinca(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player oyuncu = event.getPlayer();

        if (block.getType() == Material.CHEST && block.getState() instanceof TileState) {
            TileState tile = (TileState) block.getState();
            PersistentDataContainer blockData = tile.getPersistentDataContainer();

            if (blockData.has(ccKey, PersistentDataType.INTEGER) && blockData.has(spawnerKey, PersistentDataType.STRING)) {
                int cc = blockData.get(ccKey, PersistentDataType.INTEGER);
                String spawnerTuru = blockData.get(spawnerKey, PersistentDataType.STRING);

                // Sahip ismini verilerden al
                String sahip = blockData.get(new NamespacedKey(plugin, "sahip"), PersistentDataType.STRING);

                // UUID'yi blok veri container'ından al
                String uuid = blockData.get(uuidKey, PersistentDataType.STRING); // UUID'yi alıyoruz

                if (oyuncu.getName().equals(sahip) || oyuncu.hasPermission("candyspawners.admin")) {
                    String isim = CagiriciVeriYukleyici.getIsim(spawnerTuru);
                    List<String> lore = CagiriciVeriYukleyici.getLore(spawnerTuru);

                    // Yeni item stack oluştur
                    ItemStack yeniSandik = new ItemStack(Material.CHEST, 1);
                    ItemMeta meta = yeniSandik.getItemMeta();
                    PersistentDataContainer itemData = meta.getPersistentDataContainer();

                    meta.setDisplayName(isim);
                    meta.setLore(lore);

                    itemData.set(ccKey, PersistentDataType.INTEGER, cc);
                    itemData.set(spawnerKey, PersistentDataType.STRING, spawnerTuru);
                    itemData.set(new NamespacedKey(plugin, "sahip"), PersistentDataType.STRING, sahip); // Sahip ekleme
                    itemData.set(uuidKey, PersistentDataType.STRING, uuid); // UUID ekleme

                    yeniSandik.setItemMeta(meta);

                    // **Kırılan sandığın içindeki itemleri yere bırak**
                    Inventory inventory = ((Chest) block.getState()).getInventory();
                    ItemStack[] items = inventory.getContents();
                    for (ItemStack item : items) {
                        if (item != null) {
                            block.getWorld().dropItemNaturally(block.getLocation(), item);
                        }
                    }

                    // Kırılan sandığın kendisini düşürmemek için
                    event.setDropItems(false);

                    // Sandığı envantere ekle
                    if (oyuncu.getInventory().firstEmpty() != -1) {
                        oyuncu.getInventory().addItem(yeniSandik);
                        oyuncu.playSound(oyuncu.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_HIT, 1.0f, 1.0f);
                        oyuncu.sendMessage(prefix + "§cBir §e" + isim + " §csandığını kaldırdınız!");
                    } else {
                        oyuncu.sendMessage(prefix + "§cEnvanteriniz dolu! Bu spawneri almak için önce envanterinizde yer açın.");
                        return;
                    }

                    // Hologramı sil (Opsiyonel)
                    if (holographicDisplaysYuklu) {
                        HologramsAPI.getHolograms(plugin).stream()
                                .filter(h -> h.getLocation().distance(block.getLocation().add(0.5, 1.5, 0.5)) < 1)
                                .forEach(Hologram::delete);
                    }

                    // Sandığı veritabanından sil
                    SandikVeriYukleyici.sandikSil(block.getLocation());
                    SpawnerSandikManager.durdurSandik(block);
                } else {
                    // Eğer oyuncu sahip değilse ve admin değilse, kırma işlemi engellenir
                    event.setCancelled(true);
                    oyuncu.sendMessage(prefix + "§cBu spawnerin sahibi siz değilsiniz!");
                }
            }
        }
    }


    // Oyuncu Oyuna Girince Sandıklarını Çalıştır
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String oyuncuAdi = player.getName();
        // Oyuncunun sandıklarını asenkron olarak yükle
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Map<Location, Map<String, String>> oyuncuSandiklari = SandikVeriYukleyici.getOyuncuSandiklari(oyuncuAdi);

            // Ana thread'de sandıkları aktif et ve hologramları oluştur
            Bukkit.getScheduler().runTask(plugin, () -> {
                for (Map.Entry<Location, Map<String, String>> entry : oyuncuSandiklari.entrySet()) {
                    Location loc = entry.getKey();
                    String spawnerTuru = entry.getValue().get("spawnerTuru");
                    String spawnerAdi = entry.getValue().get("spawnerAdi");

                    Block block = loc.getBlock();
                    if (block.getType() != Material.CHEST || !(block.getState() instanceof TileState)) {
                        continue; // Blok uygun değilse geç
                    }

                    // **Hologram oluşturma (eğer eklenti yüklü ise)**
                    if (holographicDisplaysYuklu) {
                        Hologram hologram = HologramsAPI.createHologram(plugin, loc.clone().add(0.5, 1.5, 0.5));
                        hologram.appendTextLine("§eSpawner: " + spawnerAdi);
                        hologram.appendTextLine("§6Sahibi: §e" + oyuncuAdi);
                    } else {
                        Bukkit.getLogger().warning("Hologram API yüklü değil, sandık hologramları oluşturulamadı.");
                    }

                    // **PersistentData ekleme**
                    TileState tile = (TileState) block.getState();
                    PersistentDataContainer blockData = tile.getPersistentDataContainer();

                    int cc = CagiriciVeriYukleyici.getCC(spawnerTuru);
                    blockData.set(ccKey, PersistentDataType.INTEGER, cc);
                    blockData.set(spawnerKey, PersistentDataType.STRING, spawnerTuru);
                    tile.update();

                    // **Sandığı aktif hale getir**
                    SpawnerSandikManager.sandikAktifEt(block);

                }
            });
        });
    }

    // Oyuncu Oyundan Çıkınca Sandıklarını Durdur
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String oyuncuAdi = player.getName();

        // Oyuncunun sandıklarını getir
        Map<Location, Map<String, String>> oyuncuSandiklari = SandikVeriYukleyici.getOyuncuSandiklari(oyuncuAdi);

        // Sandıkları durdur
        for (Location loc : oyuncuSandiklari.keySet()) {
            Block block = loc.getBlock();
            SpawnerSandikManager.durdurSandik(block);
            if (holographicDisplaysYuklu) {
                HologramsAPI.getHolograms(plugin).stream()
                        .filter(h -> h.getLocation().distance(block.getLocation().add(0.5, 1.5, 0.5)) < 1)
                        .forEach(Hologram::delete);
            }

        }
    }



}
