package com.candyspawners.oyun;

import com.candyspawners.dosyalar.SandikVeriYukleyici;
import com.candyspawners.dosyalar.CagiriciVeriYukleyici;
import com.candyspawners.CandySpawners; // Prefix için import
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OyuncuGUI {
    public void guiAc(Player oyuncu, String targetPlayerName) {
        Inventory gui;
        gui = Bukkit.createInventory(null, 27, "§8§nSpawner Bilgileri");

        // Eğer targetPlayerName boş değilse, hedef oyuncuyu bulalım
        Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
        if (targetPlayer == null) {
            // Eğer hedef oyuncu çevrimiçi değilse, hata mesajı verelim
            oyuncu.sendMessage(CandySpawners.getInstance().getPrefix() + "§cBelirtilen oyuncu çevrimiçi değil.");
            return;
        }

        // Spawner türlerini al
        Set<String> spawnerTurleri = CagiriciVeriYukleyici.getSpawnerTurleri();
        
        // Sandık verilerini yükleyip, hedef oyuncunun (veya kendi) sahip olduğu spawner türlerine göre sayıları alalım
        Map<Location, Map<String, String>> oyuncuSandiklari = SandikVeriYukleyici.getOyuncuSandiklari(targetPlayer.getUniqueId());

        // Spawner türlerinin sayısını hesaplayalım
        Map<String, Integer> spawnerSayilari = new HashMap<>();
        int toplamSpawnerSayisi = 0;  // Oyuncunun sahip olduğu toplam spawner sayısı

        for (Map.Entry<Location, Map<String, String>> entry : oyuncuSandiklari.entrySet()) {
            String spawnerTuru = entry.getValue().get("spawnerTuru");
            spawnerSayilari.put(spawnerTuru, spawnerSayilari.getOrDefault(spawnerTuru, 0) + 1);
            toplamSpawnerSayisi++;  // Toplam spawner sayısını artır
        }

        // 4. slot'a hedef oyuncunun kafasını ekle
        ItemStack kafa = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta kafaMeta = kafa.getItemMeta();
        kafaMeta.setDisplayName(targetPlayer.getName());

        List<String> kafaLore = new ArrayList<>();
        kafaLore.add("§7Özel CandySpawnerler hakkında");
        kafaLore.add("§7daha fazla bilgi aşağıdadır:");
        kafaLore.add(""); // Boş satır ekleyelim
        kafaLore.add("§dToplam spawner sayısı: §e" + toplamSpawnerSayisi + " Adet"); // Toplam spawner sayısını buraya ekliyoruz
        kafaLore.add("");  // Boş bir satır

        // Spawner türlerini listele
        for (String tur : spawnerTurleri) {
            int mevcutSpawnerSayisi = spawnerSayilari.getOrDefault(tur, 0);
            if (mevcutSpawnerSayisi > 0) {
                String spawnerIsmi = CagiriciVeriYukleyici.getIsim(tur);
                kafaLore.add(spawnerIsmi + " §e" + mevcutSpawnerSayisi + " Adet");
            }
        }

        kafaMeta.setLore(kafaLore);

        // Hedef oyuncunun kafasını ayarla
        if (kafaMeta instanceof org.bukkit.inventory.meta.SkullMeta) {
            org.bukkit.inventory.meta.SkullMeta skullMeta = (org.bukkit.inventory.meta.SkullMeta) kafaMeta;
            skullMeta.setOwningPlayer(targetPlayer);
            kafa.setItemMeta(skullMeta);
        }

        gui.setItem(13, kafa); // 13. slot'a yerleştiriyoruz (GUI'nin ortasına)

        // Admin değilse 'Spawner Satın Al' kitabını göster
        if (!oyuncu.hasPermission("candyspawners.admin")) {
            ItemStack book = new ItemStack(Material.BOOK);
            ItemMeta bookMeta = book.getItemMeta();
            bookMeta.setDisplayName("§6Spawner Satın Al");

            List<String> bookLore = new ArrayList<>();
            bookLore.add("§7Daha fazla spawner için sitemiz:");
            bookLore.add("§6www.candycraft.net");
            bookMeta.setLore(bookLore);

            book.setItemMeta(bookMeta);

            gui.setItem(26, book);
        }

        // Oyuncuya GUI'yi aç
        oyuncu.openInventory(gui);
    }
}
