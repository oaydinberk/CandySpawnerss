package com.candyspawners.dosyalar;

import com.candyspawners.CandySpawners;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SandikVeriYukleyici {

    private static File dosya;
    private static FileConfiguration config;

    public static void yukle(CandySpawners plugin) {
        dosya = new File(plugin.getDataFolder(), "sandiklar.yml");

        if (!dosya.exists()) {
            try {
                dosya.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        config = YamlConfiguration.loadConfiguration(dosya);
    }

    public static void sandikKaydet(Location loc, String spawnerTuru, String oyuncuAdi, int cc, String uuid,String isim) {
        String key = loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();

        // UUID'yi kaydediyoruz
        config.set("sandiklar." + key + ".spawnerTuru", spawnerTuru);
        config.set("sandiklar." + key + ".oyuncu", oyuncuAdi);
        config.set("sandiklar." + key + ".cc", cc); // cc bilgisini kaydediyoruz
        config.set("sandiklar." + key + ".uuid", uuid); // UUID bilgisini kaydediyoruz
        config.set("sandiklar." + key + ".spawnerAdi", isim);
        kaydet();
    }

    
    
    // UUID kontrolü ekliyoruz
    public static boolean uuidKontrol(String uuid) {
        Set<String> keys = config.getConfigurationSection("sandiklar") != null ? 
                config.getConfigurationSection("sandiklar").getKeys(false) : null;

        if (keys != null) {
            for (String key : keys) {
                String storedUuid = config.getString("sandiklar." + key + ".uuid");
                if (storedUuid != null && storedUuid.equals(uuid)) {
                    return true; // UUID zaten var
                }
            }
        }
        return false; // UUID bulunamadı
    }
    public static void sandikSil(Location loc) {
        String key = loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
        config.set("sandiklar." + key, null);
        kaydet();
    }

    public static Map<Location, Map<String, String>> getOyuncuSandiklari(String oyuncuAdi) {
        Map<Location, Map<String, String>> oyuncuSandiklar = new HashMap<>();

        // Tüm kayıtlı sandıkları al
        Set<String> keys = config.getConfigurationSection("sandiklar") != null ? 
                config.getConfigurationSection("sandiklar").getKeys(false) : null;

        if (keys != null) {
            for (String key : keys) {
                String kayitliOyuncu = config.getString("sandiklar." + key + ".oyuncu");

                // Oyuncunun sandıklarını al
                if (kayitliOyuncu != null && kayitliOyuncu.equalsIgnoreCase(oyuncuAdi)) {
                    String[] parts = key.split(",");
                    Location loc = new Location(Bukkit.getWorld(parts[0]), Integer.parseInt(parts[1]),
                            Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));

                    String spawnerTuru = config.getString("sandiklar." + key + ".spawnerTuru");
                    String spawnerAdi = config.getString("sandiklar." + key + ".spawnerAdi");

                    Map<String, String> sandikBilgi = new HashMap<>();
                    sandikBilgi.put("spawnerTuru", spawnerTuru);
                    sandikBilgi.put("spawnerAdi", spawnerAdi);

                    oyuncuSandiklar.put(loc, sandikBilgi);
                }
            }
        }
        return oyuncuSandiklar;
    }    


    private static void kaydet() {
        try {
            config.save(dosya);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
