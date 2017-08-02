package red.man10.man10atm;

import man10vaultapi.vaultapi.Man10Vault;
import man10vaultapi.vaultapi.VaultAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Skull;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class Man10ATM extends JavaPlugin implements Listener {

    private MenuFunctions menuFunctions = null;
    @Override
    public void onEnable() {
        // Plugin startup logic
        Bukkit.getServer().getPluginManager().registerEvents(this,this);
        this.saveDefaultConfig();
        menuFunctions = new MenuFunctions(this);
        vault = new VaultAPI();
        boot();
    }

    VaultAPI vault = null;

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    String prefix = "§e§l[§1§lMan10ATM§e§l]§f§l";

    void boot(){
        Set<String> keys = getConfig().getConfigurationSection("money.types").getKeys(false);
        for(int i = 0;i < keys.toArray().length;i++){
            itemPrice.put(getConfig().getItemStack("money.types." + keys.toArray()[i]),Double.parseDouble(String.valueOf(keys.toArray()[i])));
            priceItem.put(Double.parseDouble(String.valueOf(keys.toArray()[i])),getConfig().getItemStack("money.types." + keys.toArray()[i]));
        }
        for(int i = 0;i < keys.size();i++){
            prices.add(Double.parseDouble(String.valueOf(keys.toArray()[i])));
        }
        for(int i = 0;i < priceItem.keySet().size();i++){
            itemMeta.put(priceItem.get(prices.get(i)).getItemMeta(),prices.get(i));
        }

        menuFunctions.tenKeyNum.put(46,0);
        menuFunctions.tenKeyNum.put(37,1);
        menuFunctions.tenKeyNum.put(38,2);
        menuFunctions.tenKeyNum.put(39,3);
        menuFunctions.tenKeyNum.put(28,4);
        menuFunctions.tenKeyNum.put(29,5);
        menuFunctions.tenKeyNum.put(30,6);
        menuFunctions.tenKeyNum.put(19,7);
        menuFunctions.tenKeyNum.put(20,8);
        menuFunctions.tenKeyNum.put(21,9);
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
        ItemStack dot = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/733aa24916c88696ee71db7ac8cd306ad73096b5b6ffd868e1c384b1d62cfb3c").build();
        ItemStack e = new SkullMaker().withSkinUrl("http://textures.minecraft.net/texture/dbb2737ecbf910efe3b267db7d4b327f360abc732c77bd0e4eff1d510cdef").build();
        menuFunctions.itemHead.put("0".charAt(0),i0);
        menuFunctions.itemHead.put("1".charAt(0),i1);
        menuFunctions.itemHead.put("2".charAt(0),i2);
        menuFunctions.itemHead.put("3".charAt(0),i3);
        menuFunctions.itemHead.put("4".charAt(0),i4);
        menuFunctions.itemHead.put("5".charAt(0),i5);
        menuFunctions.itemHead.put("6".charAt(0),i6);
        menuFunctions.itemHead.put("7".charAt(0),i7);
        menuFunctions.itemHead.put("8".charAt(0),i8);
        menuFunctions.itemHead.put("9".charAt(0),i9);
        menuFunctions.itemHead.put(".".charAt(0),dot);
        menuFunctions.itemHead.put("E".charAt(0),e);
    }

    List<Double> prices = new ArrayList<>();
    HashMap<ItemMeta,Double> itemMeta = new HashMap<>();
    HashMap<ItemStack,Double> itemPrice = new HashMap<>();
    HashMap<Double,ItemStack> priceItem = new HashMap<>();
    HashMap<UUID,String> menu = new HashMap<>();
    HashMap<UUID,Double> calcPrice = new HashMap<>();
    HashMap<UUID,ATMSetting> atmSettings = new HashMap<>();


    @EventHandler
    public void onClick(InventoryClickEvent e){
        Bukkit.broadcastMessage(menu.get(e.getWhoClicked().getUniqueId()));
        if(menu.isEmpty()){
            return;
        }
        if(e.getEventName().equalsIgnoreCase("InventoryCreativeEvent")){
            return;
        }
        if(!menu.containsKey(e.getWhoClicked().getUniqueId())){
            return;
        }
        if(menu.get(e.getWhoClicked().getUniqueId()).equals("main")){
            e.setCancelled(true);
            menuFunctions.mainMenuFunction(e);
            return;
        }
        if(menu.get(e.getWhoClicked().getUniqueId()).equals("deposit")){
            menuFunctions.depositInventoryFunction(e);
            return;
        }
        if(menu.get(e.getWhoClicked().getUniqueId()).equals("withdraw")){
            menuFunctions.withDrawMenuFunction(e);
            return;
        }
        if(menu.get(e.getWhoClicked().getUniqueId()).equals("currency")){
            menuFunctions.currencySettingFunction(e);
            return;
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e){
        if(menu.isEmpty()){
            return;
        }
        if(!menu.containsKey(e.getPlayer().getUniqueId())){
            return;
        }
        if(menu.get(e.getPlayer().getUniqueId()).equals("deposit")){
            if(e.getInventory().getContents().length != 0) {
                double d = 0;
                for (int ii = 0; ii < e.getInventory().getContents().length; ii++) {
                    if (e.getInventory().getContents()[ii] != null && itemMeta.get(e.getInventory().getContents()[ii].getItemMeta()) != null) {
                        d = d + itemMeta.get(e.getInventory().getContents()[ii].getItemMeta()) * e.getInventory().getContents()[ii].getAmount();
                    }
                }
                e.getPlayer().sendMessage(prefix + d + "円振り込みました。");
                vault.silentDeposit(e.getPlayer().getUniqueId(),d);
            }
        }
        calcPrice.remove(e.getPlayer().getUniqueId());
        menu.remove(e.getPlayer().getUniqueId());
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("atm")) {
            Player p = (Player) sender;
            if(args.length == 0){
                menu.put(p.getUniqueId(),"main");
                p.openInventory(createMainMenu());
            }
            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("setting")) {
                    p.openInventory(preivew());
                }
            }
        }
        return false;
    }

    public Inventory preivew(){
        Inventory inv = Bukkit.createInventory(null,9,"a");
        for(int i = 0;i < itemPrice.size();i++){
            inv.setItem(i,priceItem.get(prices.get(i)));
        }
        return inv;
    }

    public Inventory createMainMenu(){
        Inventory inv = Bukkit.createInventory(null,27,"§1§lman10銀行ATM");
        int[] blue = {0,1,2,3,4,5,6,7,8,9,13,17,18,19,20,21,22,23,24,25,26};
        int[] chest = {10,11,12};
        int[] dropper = {14,15,16};
        ItemStack blueGlass = new ItemStack(Material.STAINED_GLASS_PANE,1,(short) 11);
        ItemMeta itemMeta = blueGlass.getItemMeta();
        itemMeta.setDisplayName(" ");
        blueGlass.setItemMeta(itemMeta);

        ItemStack chestItem = new ItemStack(Material.CHEST);
        ItemMeta itemMeta1 = chestItem.getItemMeta();
        itemMeta1.setDisplayName("§6§l§nお預入れ");
        chestItem.setItemMeta(itemMeta1);

        ItemStack dispenserItem = new ItemStack(Material.DISPENSER);
        ItemMeta itemMeta2 = dispenserItem.getItemMeta();
        itemMeta2.setDisplayName("§6§l§nお引き出し");
        dispenserItem.setItemMeta(itemMeta2);

        for(int i = 0;i < blue.length;i++){
            inv.setItem(blue[i],blueGlass);
        }
        for(int i = 0;i < chest.length;i++){
            inv.setItem(chest[i],chestItem);
            inv.setItem(dropper[i],dispenserItem);
        }
        return inv;
    }
}
