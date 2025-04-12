package com.candyspawners.komutlar;

import com.candyspawners.CandySpawners;
import com.candyspawners.cagiricilar.CagiriciOlustur;
import com.candyspawners.dosyalar.CagiriciVeriYukleyici;
import com.candyspawners.oyun.OyuncuGUI;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class anakomutlar implements CommandExecutor, TabCompleter {
    String prefix = CandySpawners.getInstance().getPrefix();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            String defaultMessage = prefix + "§aYardım Sayfası Sayfa (0 - 1)\n" +
                    "§7- §b/candyspawners info §7 Plugin Hakkında\n" +
                    "§7- §b/candyspawners gui §7 Üzerinize kayıtlı spawnerları görüntüler\n";
            
            String adminMessage = prefix + "§cYetkili Komutları\n" +
                    "§7- §c/candyspawners give <Oyuncu> <spawner_turu> <miktar> §7 Belirtilen oyuncuya belirtilen miktar spawner verir\n" +
                    "§7- §c/candyspawners list §7 Mevcut spawner türlerini listeler\n" +
                    "§7- §c/candyspawners gui <Oyuncu> §7 Belirtilen oyuncunun üzerine kayıtlı spawnerları görüntüler\n";
            if (sender.hasPermission("candyspawners.admin")) {
                sender.sendMessage(defaultMessage + adminMessage);
            } else {
                sender.sendMessage(defaultMessage);
            }
            
            return true;
        }
        // /CANDYSPAWNERS LİST KOMUTU KODLARI
        if (args[0].equalsIgnoreCase("list")) {
            Set<String> spawnerler = CagiriciVeriYukleyici.getSpawnerTurleri();
            
            if (spawnerler.isEmpty()) {
                sender.sendMessage(prefix+"§cHiç kayıtlı spawner türü bulunamadı!");
                return true;
            }
            String spawnerListesi = String.join(", ", spawnerler);
            sender.sendMessage(prefix+"§aMevcut spawner türleri: §e" + spawnerListesi);
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("gui")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;

                if (args.length == 2) {
                    // Admin yetkisi kontrolü
                    if (player.hasPermission("candyspawners.admin")) {
                        String targetPlayerName = args[1];
                        // Hedef oyuncunun verilerini açmak için GUI açılıyor
                        new OyuncuGUI().guiAc(player, targetPlayerName);
                        player.sendMessage(prefix+"§a" + targetPlayerName + " verileri yükleniyor...");
                    } else {
                        // Admin olmayan oyunculara izin verilmiyor
                        player.sendMessage(prefix+"§cSadece kendi bilgilerinizi görebilirsiniz.");
                    }
                } else {
                    // Sadece 'gui' komutunu yazarsa, kendi verilerini gösterir
                    new OyuncuGUI().guiAc(player, player.getName()); 
                }
            } else {
                sender.sendMessage(prefix+"§cBu komutu yalnızca oyuncular kullanabilir.");
            }
            return true;
        }


        if (args[0].equalsIgnoreCase("info")) {
            sender.sendMessage(prefix+"§b CandySpawners Plugin §3v1.3");
            sender.sendMessage("§7</> §aDev By RianMC, Berk AYDIN");
            return true;
        }

        if (args[0].equalsIgnoreCase("give")) {
        	
            if (!sender.hasPermission("candyspawners.admin")) {
                sender.sendMessage(prefix + "§cBu komutu kullanma yetkin yok!");
                return true;
            }

            if (args.length < 4) {
                sender.sendMessage(prefix + "§cKullanım: /candyspawners give <oyuncu> <spawner_turu> <miktar>");
                return true;
            }

            Player hedefOyuncu = Bukkit.getPlayer(args[1]);
            String spawnerTuru = args[2].toLowerCase();

            if (hedefOyuncu == null) {
                sender.sendMessage(prefix + "§cOyuncu bulunamadı!");
                return true;
            }

            int miktar;
            try {
                miktar = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                sender.sendMessage(prefix + "§cGeçersiz miktar! Lütfen geçerli bir sayı girin.");
                return true;
            }

            if (CagiriciVeriYukleyici.getIsim(spawnerTuru).equals("Bilinmeyen Spawner")) {
                sender.sendMessage(prefix + "§cGeçersiz spawner türü! Mevcut türleri görmek için cagiricilar.yml dosyanızı kontrol edin.");
                return true;
            }

            for (int i = 0; i < miktar; i++) {
                ItemStack cagiriSandik = CagiriciOlustur.sandikOlustur(spawnerTuru);
                hedefOyuncu.getInventory().addItem(cagiriSandik);
            }

            sender.sendMessage(prefix + "§a" + hedefOyuncu.getName() + " adlı oyuncuya §e[" + spawnerTuru + " spawner "+miktar+"x] §7 §averildi!");
            hedefOyuncu.sendMessage(prefix + "§aSana§b [" + spawnerTuru + " spawner "+miktar+"x] §7 §averildi!");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> tamamlamalar = new ArrayList<>();
        if (args.length == 1) {
            tamamlamalar.add("info");
            tamamlamalar.add("give");
            tamamlamalar.add("list");
            tamamlamalar.add("gui");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                tamamlamalar.add(p.getName());
            }
        }
        return tamamlamalar;
    }
}
