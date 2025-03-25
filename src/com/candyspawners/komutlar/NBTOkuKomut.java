package com.candyspawners.komutlar;

import com.candyspawners.CandySpawners;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class NBTOkuKomut implements CommandExecutor {

    private final NamespacedKey ccKey;
    private final NamespacedKey spawnerKey;
    private final NamespacedKey uuidKey;
    String prefix = CandySpawners.getInstance().getPrefix();

    public NBTOkuKomut(CandySpawners plugin) {
        this.ccKey = new NamespacedKey(plugin, "CC");
        this.spawnerKey = new NamespacedKey(plugin, "spawner_turu");
        this.uuidKey = new NamespacedKey(plugin, "uuid");  // Yeni UUID anahtarını ekliyoruz
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(prefix + "Bu komut sadece oyuncular tarafından kullanılabilir.");
            return true;
        }

        Player oyuncu = (Player) sender;
        ItemStack item = oyuncu.getInventory().getItemInMainHand();

        if (item == null || !item.hasItemMeta()) {
            oyuncu.sendMessage(prefix + "§cElinde bir CandySpawner tutmalısın. Bu komut geliştiricinin debugging komutudur.");
            return true;
        }

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer data = meta.getPersistentDataContainer();

        // CC ve spawner_turu verilerini kontrol ediyoruz, ayrıca UUID'yi de kontrol ediyoruz
        if (!data.has(ccKey, PersistentDataType.INTEGER) || !data.has(spawnerKey, PersistentDataType.STRING) || !data.has(uuidKey, PersistentDataType.STRING)) {
            oyuncu.sendMessage(prefix + "§cBu eşyanın özel NBT verisi yok!");
            return true;
        }

        int cc = data.get(ccKey, PersistentDataType.INTEGER);
        String spawnerTuru = data.get(spawnerKey, PersistentDataType.STRING);
        String uuid = data.get(uuidKey, PersistentDataType.STRING);  // UUID'yi okuyoruz

        oyuncu.sendMessage("§aNBT Verileri:");
        oyuncu.sendMessage("§eCC: §f" + cc);
        oyuncu.sendMessage("§eSpawner Türü: §f" + spawnerTuru);
        oyuncu.sendMessage("§eUUID: §f" + uuid);  // UUID'yi de mesaj olarak gönderiyoruz

        return true;
    }
}
