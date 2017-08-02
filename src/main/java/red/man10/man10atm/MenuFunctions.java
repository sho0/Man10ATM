package red.man10.man10atm;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
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

    static HashMap<Character,ItemStack> itemHead = new HashMap<>();
    static HashMap<Integer,Integer> tenKeyNum = new HashMap<>();



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
            } else if (e.getSlot() == withDraw[i]) {
                e.getWhoClicked().openInventory(withDrawInventory());
                plugin.menu.put(e.getWhoClicked().getUniqueId(), "withdraw");
                plugin.atmSettings.put(e.getWhoClicked().getUniqueId(), new ATMSetting());
                plugin.calcPrice.put(e.getWhoClicked().getUniqueId(), 0D);
            }
        }
    }

    void currencySettingFunction(InventoryClickEvent e){
        e.setCancelled(true);
        int s = e.getSlot();
        if(s == 20){
            ItemMeta itemMeta = e.getCurrentItem().getItemMeta();
            itemMeta.setDisplayName("§c§l§n通貨の最小単位は無効にできません");
            e.getCurrentItem().setItemMeta(itemMeta);

            new BukkitRunnable(){

                @Override
                public void run() {
                    ItemMeta itemMeta = e.getCurrentItem().getItemMeta();
                    itemMeta.setDisplayName("§a§l許可/ALLOWED");
                    e.getCurrentItem().setItemMeta(itemMeta);
                }
            }.runTaskLater(plugin,15);
            return;
        }
        if(s == 47 || s == 46 || s == 38 || s == 37){
            boolean tenThousand = true;
            boolean hundredThousand = true;
            boolean million = true;
            boolean tenMillion = true;
            boolean hundredMillion = true;
            ATMSetting atm = new ATMSetting();
            for(int i = 0;i < 6;i++){
                ItemStack item = e.getInventory().getItem(i + 2);
                if(item.getDurability() == 14){

                }
            }
        }
        if(e.getCurrentItem().getType() == Material.WOOL && e.getCurrentItem().getDurability() == 5){
            ItemStack no = new ItemStack(Material.WOOL,1,(short) 14);
            ItemMeta noMeta = no.getItemMeta();
            noMeta.setDisplayName("§c§l無効/DENIED");
            no.setItemMeta(noMeta);
            e.getInventory().setItem(s,no);
            return;
        }
        if(e.getCurrentItem().getType() == Material.WOOL && e.getCurrentItem().getDurability() == 14){
            ItemStack ok = new ItemStack(Material.WOOL,1,(short) 5);
            ItemMeta okMeta = ok.getItemMeta();
            okMeta.setDisplayName("§a§l有効/ALLOWED");
            ok.setItemMeta(okMeta);
            e.getInventory().setItem(s,ok);
            return;
        }
    }

    Inventory currencySettingMenu(){
        Inventory inv = Bukkit.createInventory(null,54,"§1§l§n引き出し通貨種類設定");
        ItemStack blueGlass = new ItemStack(Material.STAINED_GLASS_PANE,1,(short) 11);
        ItemMeta itemMeta = blueGlass.getItemMeta();
        itemMeta.setDisplayName(" ");
        blueGlass.setItemMeta(itemMeta);
        int[] blueGlassLoc = {0,1,2,3,4,5,6,7,8,9,10,17,18,19,25,26,27,16,28,29,30,31,32,33,34,35,36,39,40,41,44,45,48,49,50,53};
        for(int i = 0;i < blueGlassLoc.length;i++){
            inv.setItem(blueGlassLoc[i],blueGlass);
        }
        ItemStack cancel = new ItemStack(Material.REDSTONE_BLOCK, 1);
        ItemStack Accept = new ItemStack(Material.EMERALD_BLOCK, 1);
        ItemMeta am = Accept.getItemMeta();
        ItemMeta cm = cancel.getItemMeta();
        am.setDisplayName("§a§l確認");
        cm.setDisplayName("§c§lキャンセル");
        Accept.setItemMeta(am);
        cancel.setItemMeta(cm);
        int[] acc = {37,38,46,47};
        int[] can = {42,43,51,52};
        for(int i = 0;i < acc.length;i++){
            inv.setItem(acc[i],Accept);
            inv.setItem(can[i],cancel);
        }
        List<Double> list = plugin.prices;

        for(int i = 0;i < list.size();i++){
            inv.setItem(i + 11,plugin.priceItem.get(list.get(i)));
        }

        ItemStack ok = new ItemStack(Material.WOOL,1,(short) 5);
        ItemMeta okMeta = ok.getItemMeta();
        okMeta.setDisplayName("§a§l有効/ALLOWED");
        ok.setItemMeta(okMeta);
        ItemStack no = new ItemStack(Material.WOOL,1,(short) 14);
        ItemMeta noMeta = no.getItemMeta();
        noMeta.setDisplayName("§c§l無効/DENIED");
        no.setItemMeta(noMeta);

        int[] nos = {20,21,22,23,24};
        for(int i = 0;i < nos.length;i++){
            inv.setItem(nos[i],ok);
        }
        return inv;
    }

    void withDrawMenuFunction(InventoryClickEvent e){
        e.setCancelled(true);
        int s = e.getSlot();
        if(s == 42 || s == 43 || s == 51 || s == 52){
            e.getWhoClicked().closeInventory();
            return;
        }
        if(s == 47){
            e.getWhoClicked().openInventory(currencySettingMenu());
            plugin.menu.put(e.getWhoClicked().getUniqueId(),"currency");
            return;
        }
        getNumberAddToCalUser(e.getWhoClicked().getUniqueId(),s);
        renderInventoryNumber(e.getWhoClicked().getUniqueId(),e.getInventory());
    }


    void getNumberAddToCalUser(UUID uuid,int slot){
        if(slot == 48){
            plugin.calcPrice.put(uuid,0D);
            return;
        }
        double d = plugin.calcPrice.get(uuid) / 10000;
        long l = (long) d;
        String lString = String.valueOf(l);
        lString = lString + tenKeyNum.get(slot).toString();
        d = Double.parseDouble(lString);
        if(d * 10000 > plugin.vault.getBalance(uuid)){
            long ll = (long) plugin.vault.getBalance(uuid);
            d = ll / 10000;
        }
        //Bukkit.broadcastMessage(String.valueOf(d * 10000));
        plugin.calcPrice.put(uuid,d * 10000);
    }

    void renderInventoryNumber(UUID uuid,Inventory inv){
        if(!plugin.calcPrice.containsKey(uuid)){
            return;
        }
        boolean below = false;
        Double d = plugin.calcPrice.get(uuid);
        if(d < 1E7){
            below = true;
        }
        String dString = d.toString();
        char[] dCharArray = dString.toCharArray();
        int total;
        if(below){
            total = dCharArray.length - 2;
        }else{
            total = dCharArray.length;
        }
        int start = 9 - total;
        for(int i = 0;i < 8;i++){
            inv.setItem(i,new ItemStack(Material.AIR));
        }
        for(int i = 0;i < total;i++){
            inv.setItem(i + start,itemHead.get(dCharArray[i]));
        }
        ItemStack Accept = new ItemStack(Material.EMERALD_BLOCK, 1);
        int[] accept = {40,41,49,50};
        ItemMeta am = Accept.getItemMeta();
        am.setDisplayName("§a§l確認");
        java.util.List<String> list = new ArrayList<>();
        double dd = plugin.calcPrice.get(uuid);
        String addition = "";
        String form = "万";
        long val = (long) dd;
        long man = val/10000;
        if(val >= 100000000){
            man = val/100000000;
            form = "億";
            long mann = (val - man * 100000000) / 10000;
            addition = mann + "万";
        }
        long balAfter = (long) (plugin.vault.getBalance(uuid) - val);
        list.add("§e§l引き出し額：" + val + "円");
        list.add("§e§l      (" + man + form + addition + ")");
        list.add("§d§l=========================");
        long balance = (long) plugin.vault.getBalance(uuid);
        list.add("§e§l現在の所持金:" + balance + "円");
        list.add("§e§lお引き出し後:" + balAfter + "円");
        am.setLore(list);
        Accept.setItemMeta(am);
        for(int i = 0;i < accept.length;i++){
            inv.setItem(accept[i],Accept);
        }
    }

    Inventory withDrawInventory(){
        Inventory inv = Bukkit.createInventory(null,54,"§1§l§nお引き出し金額を入力してください");
        ItemStack i0 = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/0ebe7e5215169a699acc6cefa7b73fdb108db87bb6dae2849fbe24714b27").build();
        ItemStack i1 = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/71bc2bcfb2bd3759e6b1e86fc7a79585e1127dd357fc202893f9de241bc9e530").build();
        ItemStack i2 = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/4cd9eeee883468881d83848a46bf3012485c23f75753b8fbe8487341419847").build();
        ItemStack i3 = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/1d4eae13933860a6df5e8e955693b95a8c3b15c36b8b587532ac0996bc37e5").build();
        ItemStack i4 = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/d2e78fb22424232dc27b81fbcb47fd24c1acf76098753f2d9c28598287db5").build();
        ItemStack i5 = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/6d57e3bc88a65730e31a14e3f41e038a5ecf0891a6c243643b8e5476ae2").build();
        ItemStack i6 = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/334b36de7d679b8bbc725499adaef24dc518f5ae23e716981e1dcc6b2720ab").build();
        ItemStack i7 = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/6db6eb25d1faabe30cf444dc633b5832475e38096b7e2402a3ec476dd7b9").build();
        ItemStack i8 = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/59194973a3f17bda9978ed6273383997222774b454386c8319c04f1f4f74c2b5").build();
        ItemStack i9 = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/e67caf7591b38e125a8017d58cfc6433bfaf84cd499d794f41d10bff2e5b840").build();
        ItemStack cancel = new ItemStack(Material.REDSTONE_BLOCK, 1);
        ItemStack Accept = new ItemStack(Material.EMERALD_BLOCK, 1);
        ItemStack clear = new ItemStack(Material.TNT, 1);
        ItemMeta clearm = clear.getItemMeta();
        clearm.setDisplayName("§c§lクリア");
        clear.setItemMeta(clearm);
        ArrayList<String> a = new ArrayList<String>();
        ItemMeta am = Accept.getItemMeta();
        ItemMeta cm = cancel.getItemMeta();
        a.add("§d§l掛け金");
        am.setDisplayName("§a§l確認");
        cm.setDisplayName("§c§lキャンセル");
        Accept.setItemMeta(am);
        cancel.setItemMeta(cm);
        ItemMeta i0m = i0.getItemMeta();
        ItemMeta i1m = i1.getItemMeta();
        ItemMeta i2m = i2.getItemMeta();
        ItemMeta i3m = i3.getItemMeta();
        ItemMeta i4m = i4.getItemMeta();
        ItemMeta i5m = i5.getItemMeta();
        ItemMeta i6m = i6.getItemMeta();
        ItemMeta i7m = i7.getItemMeta();
        ItemMeta i8m = i8.getItemMeta();
        ItemMeta i9m = i9.getItemMeta();
        i0m.setDisplayName("§7§l0");
        i1m.setDisplayName("§7§l1");
        i2m.setDisplayName("§7§l2");
        i3m.setDisplayName("§7§l3");
        i4m.setDisplayName("§7§l4");
        i5m.setDisplayName("§7§l5");
        i6m.setDisplayName("§7§l6");
        i7m.setDisplayName("§7§l7");
        i8m.setDisplayName("§7§l8");
        i9m.setDisplayName("§7§l9");
        i0.setItemMeta(i0m);
        i1.setItemMeta(i1m);
        i2.setItemMeta(i2m);
        i3.setItemMeta(i3m);
        i4.setItemMeta(i4m);
        i5.setItemMeta(i5m);
        i6.setItemMeta(i6m);
        i7.setItemMeta(i7m);
        i8.setItemMeta(i8m);
        i9.setItemMeta(i9m);

        ItemStack blueGlass = new ItemStack(Material.STAINED_GLASS_PANE,1,(short) 11);
        ItemMeta itemMeta = blueGlass.getItemMeta();
        itemMeta.setDisplayName(" ");
        blueGlass.setItemMeta(itemMeta);

        int[] bGlass = {9,10,11,12,13,14,15,16,17,18,22,23,24,25,26,27,31,32,33,34,35,36,44,45,53};
        for(int i = 0;i < bGlass.length;i++){
            inv.setItem(bGlass[i],blueGlass);
        }
        ItemStack B = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/3d43e5b3e8d14ab8f9d2318e56de4aa026e3241112426c5edd5015e6b9a6b71").withName("§1§l§nBANK").build();
        ItemStack A = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/adb5f1a9f58c852b473b3855dce27f8bf40db7e4bd2951e62f28d61c3694ff").withName("§1§l§nBANK").build();
        ItemStack N = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/785b8c8ae5eae18fa5fcae88d5bca351c93144384f9c4a22f75cd642d5796").withName("§1§l§nBANK").build();
        ItemStack K = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/b331cc913f191ae9bda4ce98d05929a6fcc41622eaa8a7ed52c6c724919b31").withName("§1§l§nBANK").build();
        inv.setItem(8,i0);
        inv.setItem(46,i0);
        inv.setItem(37,i1);
        inv.setItem(38,i2);
        inv.setItem(39,i3);
        inv.setItem(28,i4);
        inv.setItem(29,i5);
        inv.setItem(30,i6);
        inv.setItem(19,i7);
        inv.setItem(20,i8);
        inv.setItem(21,i9);
        inv.setItem(23,B);
        inv.setItem(24,A);
        inv.setItem(25,N);
        inv.setItem(26,K);
        inv.setItem(48,clear);

        int[] accept = {40,41,49,50};
        int[] cancell = {42,43,51,52};
        for(int i = 0;i < accept.length;i++){
            inv.setItem(accept[i],Accept);
            inv.setItem(cancell[i],cancel);
        }
        return inv;
    }

    void depositInventoryFunction(InventoryClickEvent e){
        int[] ee = {50,49,48};
        new BukkitRunnable() {
            @Override
            public void run() {
                double d = 0;
                for (int i = 0; i < e.getInventory().getContents().length; i++) {
                    if (e.getInventory().getContents()[i] != null && plugin.itemMeta.get(e.getInventory().getContents()[i].getItemMeta()) != null) {
                        d = d + plugin.itemMeta.get(e.getInventory().getContents()[i].getItemMeta()) * e.getInventory().getContents()[i].getAmount();
                    }
                }
                for (int i = 0; i < ee.length; i++) {
                    ItemStack item = e.getInventory().getItem(ee[i]);
                    ItemMeta itemMeta = item.getItemMeta();
                    itemMeta.setDisplayName("§e現在: " + d + "円");
                    item.setItemMeta(itemMeta);
                }
            }
        }.runTaskLater(plugin, 1);
        //Bukkit.broadcastMessage(String.valueOf(e.getSlot()));
        if(e.getSlot() == 50 || e.getSlot() == 49 || e.getSlot() == 48){
            double d = 0;
            for(int ii = 0;ii < e.getInventory().getContents().length;ii++){
                if(e.getInventory().getContents()[ii] != null && plugin.itemMeta.get(e.getInventory().getContents()[ii].getItemMeta()) != null) {
                    d = d + plugin.itemMeta.get(e.getInventory().getContents()[ii].getItemMeta()) * e.getInventory().getContents()[ii].getAmount();
                }
            }
            e.setCancelled(true);
            plugin.menu.put(e.getWhoClicked().getUniqueId(),"out");
            e.getWhoClicked().closeInventory();
            e.getWhoClicked().sendMessage(plugin.prefix + d + "円振り込みました。");
            plugin.vault.silentDeposit(e.getWhoClicked().getUniqueId(),d);
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

    Inventory depositInventory(){
        Inventory inv = Bukkit.createInventory(null,54,"§1§l§nお金を投入してください");
        ItemStack blueGlass = new ItemStack(Material.STAINED_GLASS_PANE,1,(short) 11);
        ItemMeta itemMeta = blueGlass.getItemMeta();
        itemMeta.setDisplayName(" ");
        blueGlass.setItemMeta(itemMeta);

        ItemStack emerald = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta itemMeta1 = emerald.getItemMeta();
        itemMeta1.setDisplayName("§e§l§n現在:0円");
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
