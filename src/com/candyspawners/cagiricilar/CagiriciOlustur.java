package com.candyspawners.cagiricilar;

import com.candyspawners.CandySpawners;
import com.candyspawners.dosyalar.CagiriciVeriYukleyici;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.UUID;

public class CagiriciOlustur {

    public static ItemStack sandikOlustur(String spawnerTuru) {
        // YAML dosyasından verileri çekelim
        int cc = CagiriciVeriYukleyici.getCC(spawnerTuru);
        String isim = CagiriciVeriYukleyici.getIsim(spawnerTuru);
        List<String> lore = CagiriciVeriYukleyici.getLore(spawnerTuru);

        // Eğer bilinmeyen bir spawner türü girdiysek, varsayılan bir sandık döndürelim
        if (isim.equals("Bilinmeyen Spawner")) {
            return new ItemStack(Material.CHEST);
        }

        // Yeni sandık oluştur
        ItemStack sandik = new ItemStack(Material.CHEST, 1);
        ItemMeta meta = sandik.getItemMeta();

        // Sandığa özel isim ve açıklamalar ekleyelim
        meta.setDisplayName(isim);
        meta.setLore(lore);

        // UUID oluştur
        UUID newUUID = UUID.randomUUID();

        // PersistentDataContainer ile NBT benzeri veri ekleyelim
        PersistentDataContainer data = meta.getPersistentDataContainer();
        NamespacedKey ccKey = new NamespacedKey(CandySpawners.getInstance(), "CC");
        NamespacedKey spawnerKey = new NamespacedKey(CandySpawners.getInstance(), "spawner_turu");
        NamespacedKey uuidKey = new NamespacedKey(CandySpawners.getInstance(), "uuid");

        // UUID, CC ve spawner türünü NBT'ye ekleyelim
        data.set(ccKey, PersistentDataType.INTEGER, cc);
        data.set(spawnerKey, PersistentDataType.STRING, spawnerTuru);
        data.set(uuidKey, PersistentDataType.STRING, newUUID.toString());

        // Güncellenmiş metayı sandığa kaydedelim
        sandik.setItemMeta(meta);

        return sandik;
    }
}
