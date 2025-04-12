package com.candyspawners;

// 🔹 KOMUTLAR
import com.candyspawners.komutlar.anakomutlar;
import com.candyspawners.komutlar.NBTOkuKomut;
import com.candyspawners.oyun.OyuncuGUIListener;
// 🔹 LİSTENERLAR
import com.candyspawners.oyun.OzelSandikListener;
// 🔹 DOSYA YÜKLEYİCİLER
import com.candyspawners.dosyalar.SandikVeriYukleyici;
import com.candyspawners.dosyalar.CagiriciVeriYukleyici;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;


public final class CandySpawners extends JavaPlugin {

    private static CandySpawners instance;
    private final String prefix = "§x§F§F§B§9§0§0C§x§F§F§A§A§1§4a§x§F§F§9§A§2§7n§x§F§F§8§B§3§Bd§x§F§F§7§B§4§Ey§x§F§F§6§C§6§2S§x§F§F§5§D§7§5p§x§F§F§4§D§8§9a§x§F§F§3§E§9§Cw§x§F§F§2§E§B§0n§x§F§F§1§F§C§3e§x§F§F§0§F§D§7r§x§F§F§0§0§E§As §7| ";  // Prefix'i burada tanımlıyoruz
    private boolean holographicDisplaysYuklu = false;
    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("\u001B[32mCandySpawners eklentisi aktif edildi!\u001B[0m");

        // **Dosya Yükleyicileri Başlat**
        CagiriciVeriYukleyici.yukle(this);
        SandikVeriYukleyici.yukle(this);
        OzelSandikListener sandikListener = new OzelSandikListener(this);
        // **HolographicDisplays kontrolü**
        if (Bukkit.getPluginManager().getPlugin("HolographicDisplays") != null) {
            holographicDisplaysYuklu = true;
            getLogger().info("\u001B[36mHolographicDisplays bulundu, hologramlar aktif edilecek.\u001B[0m");
        } else {
            getLogger().warning("\u001B[33mHolographicDisplays bulunamadı! Hologramlar devre dışı bırakıldı.\u001B[0m");
        }
        
        
        
        // **Eventleri Kaydet**
        getServer().getPluginManager().registerEvents(sandikListener, this);
        getServer().getPluginManager().registerEvents(new OyuncuGUIListener(), this);
        // **Komutları Kaydet**
        if (getCommand("candyspawners") != null) {
            getCommand("candyspawners").setExecutor(new anakomutlar());
        }
        if (getCommand("nbtoku") != null) {
            getCommand("nbtoku").setExecutor(new NBTOkuKomut(this));
        }

    }

    
    // PLUGİN KAPATILINCA
    @Override
    public void onDisable() {
        getLogger().info("\u001B[31mCandySpawners eklentisi devre dışı bırakıldı\u001B[0m");
    }
    public boolean isHolographicDisplaysYuklu() {
        return holographicDisplaysYuklu;
    }
    public static CandySpawners getInstance() {
        return instance;
    }
    
    public String getPrefix() {
        return prefix;
    }
}
