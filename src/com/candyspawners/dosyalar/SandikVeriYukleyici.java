package com.candyspawners.dosyalar;

import com.candyspawners.CandySpawners;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class SandikVeriYukleyici {

    private static CandySpawners plugin;

    public static void yukle(CandySpawners pl) {
        plugin = pl;

        // playerdatas klasörü yoksa oluştur
        File klasor = new File(plugin.getDataFolder(), "playerdatas");
        if (!klasor.exists()) {
            klasor.mkdirs();
        }
    }

    public static void sandikKaydet(UUID oyuncuUUID, Location loc, String spawnerTuru, String oyuncuAdi, int cc, String uuid, String isim) {
        File dosya = new File(plugin.getDataFolder() + "/playerdatas", oyuncuUUID.toString() + ".yml");
        FileConfiguration data = YamlConfiguration.loadConfiguration(dosya);

        String key = loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();

        data.set("player", oyuncuAdi);
        data.set("playerUUID", oyuncuUUID.toString());

        data.set("sandiklar." + key + ".spawnerTuru", spawnerTuru);
        data.set("sandiklar." + key + ".oyuncu", oyuncuAdi);
        data.set("sandiklar." + key + ".cc", cc);
        data.set("sandiklar." + key + ".uuid", uuid);
        data.set("sandiklar." + key + ".spawnerAdi", isim);

        try {
            data.save(dosya);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sandikSil(UUID oyuncuUUID, Location loc) {
        File dosya = new File(plugin.getDataFolder() + "/playerdatas", oyuncuUUID.toString() + ".yml");

        // Bukkit.getLogger().info("[DEBUG] Veri silme dosya yolu: " + dosya.getAbsolutePath());

        if (!dosya.exists()) {
        	// Bukkit.getLogger().warning("[DEBUG] Veri dosyası bulunamadı, silme işlemi iptal.");
            return;
        }

        FileConfiguration data = YamlConfiguration.loadConfiguration(dosya);
        String key = loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();

        data.set("sandiklar." + key, null);
        // Bukkit.getLogger().info("[DEBUG] Veri null olarak işaretlendi: " + key);

        try {
            data.save(dosya);
            // Bukkit.getLogger().info("[DEBUG] Dosya başarıyla kaydedildi.");
        } catch (IOException e) {
        	Bukkit.getLogger().severe("CandySpawners | [ERROR] Dosya kaydedilirken hata oluştu!");
            e.printStackTrace();
        }
    }


    public static boolean uuidKontrol(UUID oyuncuUUID, String hedefUUID) {
        File dosya = new File(plugin.getDataFolder() + "/playerdatas", oyuncuUUID.toString() + ".yml");
        if (!dosya.exists()) return false;

        FileConfiguration data = YamlConfiguration.loadConfiguration(dosya);
        ConfigurationSection sandiklar = data.getConfigurationSection("sandiklar");
        if (sandiklar == null) return false;

        for (String key : sandiklar.getKeys(false)) {
            String storedUuid = sandiklar.getString(key + ".uuid");
            if (storedUuid != null && storedUuid.equalsIgnoreCase(hedefUUID)) {
                return true;
            }
        }
        return false;
    }

    public static Map<Location, Map<String, String>> getOyuncuSandiklari(UUID oyuncuUUID) {
        Map<Location, Map<String, String>> oyuncuSandiklar = new HashMap<>();

        File dosya = new File(plugin.getDataFolder() + "/playerdatas", oyuncuUUID.toString() + ".yml");
        if (!dosya.exists()) return oyuncuSandiklar;

        FileConfiguration data = YamlConfiguration.loadConfiguration(dosya);
        ConfigurationSection sandiklar = data.getConfigurationSection("sandiklar");
        if (sandiklar == null) return oyuncuSandiklar;

        for (String key : sandiklar.getKeys(false)) {
            String[] parts = key.split(",");
            if (parts.length != 4) continue;

            World world = Bukkit.getWorld(parts[0]);
            if (world == null) continue;

            try {
                int x = Integer.parseInt(parts[1]);
                int y = Integer.parseInt(parts[2]);
                int z = Integer.parseInt(parts[3]);
                Location loc = new Location(world, x, y, z);

                String spawnerTuru = sandiklar.getString(key + ".spawnerTuru");
                String spawnerAdi = sandiklar.getString(key + ".spawnerAdi");
                String oyuncuIsmi = sandiklar.getString(key + ".oyuncu");
                String uuid = sandiklar.getString(key + ".uuid");
                String cc = sandiklar.getString(key + ".cc");

                Map<String, String> sandikBilgi = new HashMap<>();
                sandikBilgi.put("spawnerTuru", spawnerTuru);
                sandikBilgi.put("spawnerAdi", spawnerAdi);
                sandikBilgi.put("oyuncu", oyuncuIsmi);
                sandikBilgi.put("uuid", uuid);
                sandikBilgi.put("cc", cc);

                oyuncuSandiklar.put(loc, sandikBilgi);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        return oyuncuSandiklar;
    }
}
