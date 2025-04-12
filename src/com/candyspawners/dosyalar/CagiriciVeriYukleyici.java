package com.candyspawners.dosyalar;

import com.candyspawners.CandySpawners;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CagiriciVeriYukleyici {

    private static File dosya;
    private static FileConfiguration config;

    public static void yukle(CandySpawners plugin) {
        dosya = new File(plugin.getDataFolder(), "cagiricilar.yml");

        if (!dosya.exists()) {
            plugin.saveResource("cagiricilar.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(dosya);
    }

    public static Set<String> getSpawnerTurleri() {
        if (config.contains("spawnerler")) {
            return config.getConfigurationSection("spawnerler").getKeys(false);
        }
        return new HashSet<>();
    }

    public static String getIsim(String tur) {
        return config.getString("spawnerler." + tur + ".ismi", "Bilinmeyen Spawner");
    }

    public static List<String> getLore(String tur) {
        return config.getStringList("spawnerler." + tur + ".lore");
    }

    public static int getCC(String tur) {
        return config.getInt("spawnerler." + tur + ".cc", 0);
    }

    public static int getSure(String tur) {
        return config.getInt("spawnerler." + tur + ".sure", 5); // Varsayılan 5 saniye
    }

    public static List<Material> getItems(String tur) {
        return config.getStringList("spawnerler." + tur + ".items").stream()
                .map(Material::matchMaterial)
                .collect(Collectors.toList());
    }
}
