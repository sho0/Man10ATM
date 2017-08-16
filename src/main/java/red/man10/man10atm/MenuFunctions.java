package red.man10.man10atm;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Created by sho on 2017/07/29.
 */
public class MenuFunctions {
    private final Man10ATM plugin;



    public MenuFunctions(Man10ATM plugin) {
        this.plugin = plugin;
    }

    void mainMenuFunction(InventoryClickEvent e) {
        int[] deposit = {10, 11, 12};
        int[] withDraw = {14, 15, 16};
        for (int i = 0; i < deposit.length; i++) {
            if (e.getSlot() == deposit[i]) {
                e.getWhoClicked().openInventory(depositInventory());
                plugin.menu.put(e.getWhoClicked().getUniqueId(), "deposit");
                plugin.atmLog.put(e.getWhoClicked().getUniqueId(),new ATMLog());
            } else if (e.getSlot() == withDraw[i]) {
                e.getWhoClicked().openInventory(withDrawInventory(e.getWhoClicked().getUniqueId()));
                plugin.menu.put(e.getWhoClicked().getUniqueId(), "withdraw");
                plugin.atmLog.put(e.getWhoClicked().getUniqueId(),new ATMLog());
            }
        }
    }

    Inventory withDrawInventory(UUID uuid){
        Inventory inv = Bukkit.createInventory(null,45,"§1§l§n引き出す通貨をクリックしてください");
        ItemStack blueGlass = new ItemStack(Material.STAINED_GLASS_PANE,1,(short) 11);
        ItemMeta itemMeta = blueGlass.getItemMeta();
        itemMeta.setDisplayName(" ");
        blueGlass.setItemMeta(itemMeta);
        int[] blueGlassLoc = {0,1,2,3,4,5,6,7,8,9,10,17,18,19,25,26,27,16,28,20,21,22,23,24,29,30,31,32,33,34,35,36,39,40,41,44,43,42,38,37};
        for(int i = 0;i < blueGlassLoc.length;i++){
            inv.setItem(blueGlassLoc[i],blueGlass);
        }
        ItemStack cancel = new ItemStack(Material.STAINED_GLASS_PANE, 1,(short) 14);
        ItemMeta cm = cancel.getItemMeta();
        cm.setDisplayName("§c§l閉じる");
        cancel.setItemMeta(cm);
        List<Double> list = plugin.prices;
        inv.setItem(39,cancel);
        inv.setItem(40,cancel);
        inv.setItem(41,cancel);
        for(int i = 0;i < list.size();i++){
            ItemStack item = plugin.priceItemGetItem(list.get(i));
            inv.setItem(i + 11,item);
        }
        for(int i = 0;i < 5;i++){
            ItemStack item = inv.getItem(i + 11);
            ItemMeta itemMeta1 = item.getItemMeta();
            List<String> lore = itemMeta1.getLore();
            lore.add("§d§l===================================");
            lore.add("§e§l現在の所持金        :" + jpnBalForm((long) plugin.vault.getBalance(uuid)) + "円");
            lore.add("§e§lお引き出し後の所持金:" + jpnBalForm((long) ((long) plugin.vault.getBalance(uuid) - plugin.itemMeta.get(item.getItemMeta()))) + "円");
            itemMeta1.setLore(lore);
            item.setItemMeta(itemMeta1);
        }
        return inv;
    }

    void withDrawMenuFunction(InventoryClickEvent e){
        e.setCancelled(true);
        int s = e.getSlot();
        if(s == 39 || s == 40 || s == 41){
            e.getWhoClicked().closeInventory();
            return;
        }
        if(e.getCurrentItem().getType() != Material.STAINED_GLASS_PANE){
            if(e.getClickedInventory().getType() == InventoryType.PLAYER){
                return;
            }
            double balance = plugin.vault.getBalance(e.getWhoClicked().getUniqueId());
            double required = plugin.withdrawPrice.get(e.getSlot());
            if(balance < required){
                e.getWhoClicked().sendMessage(plugin.prefix + "お金が足りません");
                return;
            }
            if(e.getWhoClicked().getInventory().firstEmpty() == -1){
                if(full(e.getWhoClicked().getInventory(),e.getCurrentItem().getItemMeta())){
                e.getWhoClicked().sendMessage(plugin.prefix + "スペースが足りません");
                return;
                }
            }
            ATMLog atm = plugin.atmLog.get(e.getWhoClicked().getUniqueId());
            plugin.vault.silentWithdraw(e.getWhoClicked().getUniqueId(),required);
            e.getWhoClicked().getInventory().addItem(plugin.priceItem.get(plugin.withdrawPrice.get(e.getSlot())));
            Inventory inv = e.getInventory();
            UUID uuid = e.getWhoClicked().getUniqueId();
            for(int i = 0;i < 5;i++){
                ItemStack item = inv.getItem(i + 11);
                ItemStack loreItem = plugin.priceItem.get(plugin.withdrawPrice.get(i + 11));
                ItemMeta loreItemMeta = loreItem.getItemMeta();
                ItemMeta itemMeta1 = item.getItemMeta();
                List<String> lore = loreItemMeta.getLore();
                lore.add("§d§l===================================");
                long val = (long) (plugin.vault.getBalance(uuid) - plugin.withdrawPrice.get(i + 11));
                if(val < 0){
                    lore.add("§c§l§n所持金が足りないためお引き出しできません");
                }else{
                    lore.add("§e§l現在の所持金        :" + jpnBalForm((long) plugin.vault.getBalance(uuid)) + "円");
                    lore.add("§e§lお引き出し後の所持金:" + jpnBalForm(val)  + "円");
                }
                itemMeta1.setLore(lore);
                item.setItemMeta(itemMeta1);
            }
            if(s == 11){
                atm.tenThousand = atm.tenThousand + 1;
                plugin.atmLog.put(e.getWhoClicked().getUniqueId(),atm);
            }else if(s == 12){
                atm.hundredThousand = atm.hundredThousand + 1;
                plugin.atmLog.put(e.getWhoClicked().getUniqueId(),atm);
            }else if(s == 13){
                atm.million = atm.million + 1;
                plugin.atmLog.put(e.getWhoClicked().getUniqueId(),atm);
            }else if(s == 14){
                atm.tenMillion = atm.tenMillion + 1;
                plugin.atmLog.put(e.getWhoClicked().getUniqueId(),atm);
            }else if(s == 15){
                atm.hundredMillion = atm.hundredMillion + 1;
                plugin.atmLog.put(e.getWhoClicked().getUniqueId(),atm);
            }
        }
    }

    boolean full(Inventory inv,ItemMeta item){
        for(int i = 0;i < inv.getContents().length;i++){
            if(inv.getContents()[i] != null){
                if(inv.getContents()[i].getItemMeta().equals(item)){
                    if(inv.getContents()[i].getAmount() < 64) {
                        Bukkit.broadcastMessage(String.valueOf(inv.getContents()[i].getAmount()));
                        return false;
                    }
                }
            }
        }
        return true;
    }

    void depositInventoryFunction(InventoryClickEvent e){
        int[] ee = {50,49,48};
        if(e.getInventory() == null || e.getCurrentItem() == null){
            return;
        }
        //Bukkit.broadcastMessage(String.valueOf(e.getSlot()));
        if(e.getSlot() == 50 || e.getSlot() == 49 || e.getSlot() == 48){
            double d = 0;
            if(d == 0) {
                e.setCancelled(true);
                e.getWhoClicked().closeInventory();
                return;
            }
            e.setCancelled(true);
            e.getWhoClicked().closeInventory();
            return;
        }
        if(e.getCurrentItem().getType() == Material.AIR){
            return;
        }
        if(!plugin.itemMeta.containsKey(e.getCurrentItem().getItemMeta())){
            e.setCancelled(true);
            return;
        }
            return;
    }

    String jpnBalForm(long val){
        String addition = "";
        String form = "万";
        long man = val/10000;
        if(val >= 100000000){
            man = val/100000000;
            form = "億";
            long mann = (val - man * 100000000) / 10000;
            addition = mann + "万";
        }
        return man + form + addition;
    }

    Inventory depositInventory(){
        Inventory inv = Bukkit.createInventory(null,54,"§1§l§nお金を投入してください");
        ItemStack blueGlass = new ItemStack(Material.STAINED_GLASS_PANE,1,(short) 11);
        ItemMeta itemMeta = blueGlass.getItemMeta();
        itemMeta.setDisplayName(" ");
        blueGlass.setItemMeta(itemMeta);

        ItemStack emerald = new ItemStack(Material.STAINED_GLASS_PANE,1,(short) 14);
        ItemMeta itemMeta1 = emerald.getItemMeta();
        itemMeta1.setDisplayName("§c§l閉じる");
        emerald.setItemMeta(itemMeta1);

        int[] blue = {53,52,51,47,46,45};
        int[] e = {50,49,48};
        for(int i = 0;i < blue.length;i++){
            inv.setItem(blue[i],blueGlass);
        }
        for(int i = 0;i < e.length;i++){
            inv.setItem(e[i],emerald);
        }
        return inv;
    }
}
