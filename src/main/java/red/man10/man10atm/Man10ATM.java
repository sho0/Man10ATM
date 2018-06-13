package red.man10.man10atm;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Skull;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import red.man10.man10mysqlapi.MySQLAPI;
import red.man10.man10vaultapiplus.Man10VaultAPI;
import red.man10.man10vaultapiplus.enums.TransactionType;

import javax.swing.*;
import java.text.SimpleDateFormat;
import java.util.*;

public final class Man10ATM extends JavaPlugin implements Listener {

    HashMap<Integer,ItemStack> withDrawItemStack = new HashMap<>();
    Man10ATMAPI api = new Man10ATMAPI();

    String createAtmLogTable = "CREATE TABLE `man10_atm_log` (\n" +
            "\t`id` INT NOT NULL AUTO_INCREMENT,\n" +
            "\t`name` VARCHAR(16) NULL DEFAULT '0',\n" +
            "\t`uuid` VARCHAR(64) NULL DEFAULT '0',\n" +
            "\t`action` VARCHAR(64) NULL DEFAULT '0',\n" +
            "\t`ten_thousand` BIGINT NULL DEFAULT '0',\n" +
            "\t`hundred_thousand` BIGINT NULL DEFAULT '0',\n" +
            "\t`million` BIGINT NULL DEFAULT '0',\n" +
            "\t`ten_million` BIGINT NULL DEFAULT '0',\n" +
            "\t`hundred_million` BIGINT NULL DEFAULT '0',\n" +
            "\t`value` BIGINT NULL DEFAULT '0',\n" +
            "\t`world` VARCHAR(64) NULL DEFAULT '0',\n" +
            "\t`x` DOUBLE NULL DEFAULT '0',\n" +
            "\t`y` DOUBLE NULL DEFAULT '0',\n" +
            "\t`z` DOUBLE NULL DEFAULT '0',\n" +
            "\t`pitch` DOUBLE NULL DEFAULT '0',\n" +
            "\t`yaw` DOUBLE NULL DEFAULT '0',\n" +
            "\t`date_time` DATETIME NULL DEFAULT NULL,\n" +
            "\t`time` BIGINT NULL DEFAULT '0',\n" +
            "\t PRIMARY KEY (`id`)\n" +
            ")\n" +
            "COLLATE='utf8_general_ci'\n" +
            "ENGINE=InnoDB\n" +
            ";\n";

    private MenuFunctions menuFunctions = null;
    @Override
    public void onEnable() {
        // Plugin startup logic
        Bukkit.getServer().getPluginManager().registerEvents(this,this);
        this.saveDefaultConfig();
        menuFunctions = new MenuFunctions(this);
        vault = new Man10VaultAPI("Man10ATM");
        mysql = new MySQLAPI(this,"man10ATM");
        mysql.execute(createAtmLogTable);
        boot();
    }

    HashMap<UUID,Long> cancel = new HashMap<>();
    static HashMap<Integer,Double> withdrawPrice = new HashMap<>();

    Man10VaultAPI vault = null;
    MySQLAPI mysql = null;

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        for(Player pp : Bukkit.getOnlinePlayers()){
            if(menu.containsKey(pp.getUniqueId())){
                long price = getInvPrice(pp.getOpenInventory().getTopInventory(),pp.getUniqueId());
                vault.givePlayerMoney(pp.getUniqueId(), price, TransactionType.DEPOSIT, "Server ShutDown AutoPay In Menu");
                pp.sendMessage(prefix + menuFunctions.jpnBalForm(price)+ "円預入れました。");
                pp.sendMessage(prefix + "現在の所持金は" + (long) vault.getBalance(pp.getUniqueId()) + "円です");
                pp.sendMessage(prefix + "              (" + menuFunctions.jpnBalForm((long) vault.getBalance(pp.getUniqueId())) + ")");
                ATMLog atm = atmLog.get(pp.getUniqueId());
                createAtmLog(pp.getName(),pp.getUniqueId(),"Deposit",atm.tenThousand,atm.hundredThousand,atm.million,atm.tenMillion,atm.hundredMillion,pp.getLocation());
                pp.closeInventory();
            }
        }
    }

    String prefix = "§e§l[§1Man10ATM§e§l]§f§l";

    void createAtmLog(String name, UUID uuid, String action, long tenThousand, long hundredThousand, long million, long tenMillion, long hundredMillion, Location l){
        long value = (tenThousand * 10000) + (hundredThousand * 100000) + (million * 1000000) + (tenMillion * 10000000) + (hundredMillion * 100000000);
        if(value == 0){
            return;
        }
        mysql.execute("INSERT INTO man10_atm_log VALUES('0','" + name + "','" + uuid +"','" + action + "','" + tenThousand + "','" + hundredThousand + "','" + million +"','" + tenMillion + "','" + hundredMillion + "','" + value + "','" + l.getWorld().getName() + "','" + l.getX() + "','" + l.getY() + "','" + l.getZ() + "','" + l.getPitch() + "','" + l.getYaw() + "','" +  currentTimeNoBracket() + "','" + System.currentTimeMillis()/1000 + "');");
    }
    public String currentTimeNoBracket(){
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy'-'MM'-'dd' 'HH':'mm':'ss");
        Bukkit.getLogger().info("datetime ");
        return sdf.format(date);
    }

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
            api.itemMeta.put(priceItem.get(prices.get(i)).getItemMeta(),prices.get(i));
        }
        for(int i = 0;i < prices.size();i++){
            withDrawItemStack.put(i + 11,priceItem.get(prices.get(i)));
        }
        withdrawPrice.put(11,prices.get(0));
        withdrawPrice.put(12,prices.get(1));
        withdrawPrice.put(13,prices.get(2));
        withdrawPrice.put(14,prices.get(3));
        withdrawPrice.put(15,prices.get(4));
        withdrawPrice.put(15,prices.get(4));
    }

    List<Double> prices = new ArrayList<>();
    HashMap<ItemMeta,Double> itemMeta = new HashMap<>();
    HashMap<ItemStack,Double> itemPrice = new HashMap<>();
    HashMap<Double,ItemStack> priceItem = new HashMap<>();
    HashMap<UUID,String> menu = new HashMap<>();
    HashMap<UUID,Double> calcPrice = new HashMap<>();

    HashMap<UUID,ATMLog> atmLog = new HashMap<>();

    ArrayList<UUID> inDeposit = new ArrayList<>();

    boolean locked = false;

    ItemStack priceItemGetItem(Double d ){
        return priceItem.get(d);
    }

    public long getInvPrice(Inventory inv,UUID uuid){
        double d = 0;
        if(inv.getContents().length != 0) {
            for (int ii = 0; ii < inv.getContents().length; ii++) {
                if (inv.getContents()[ii] != null && itemMeta.get(inv.getContents()[ii].getItemMeta()) != null) {
                    d = d + itemMeta.get(inv.getContents()[ii].getItemMeta()) * inv.getContents()[ii].getAmount();
                    Double s = itemMeta.get(inv.getContents()[ii].getItemMeta());
                    ATMLog atm = atmLog.get(uuid);
                    if (s == 10000) {
                        atm.tenThousand = atm.tenThousand + inv.getContents()[ii].getAmount();
                        atmLog.put(uuid, atm);
                    } else if (s == 100000) {
                        atm.hundredThousand = atm.hundredThousand + inv.getContents()[ii].getAmount();
                        atmLog.put(uuid, atm);
                    } else if (s == 1000000) {
                        atm.million = atm.million + inv.getContents()[ii].getAmount();
                        atmLog.put(uuid, atm);
                    } else if (s == 10000000) {
                        atm.tenMillion = atm.tenMillion + inv.getContents()[ii].getAmount();
                        atmLog.put(uuid, atm);
                    } else if (s == 100000000) {
                        atm.hundredMillion = atm.hundredMillion + inv.getContents()[ii].getAmount();
                        atmLog.put(uuid, atm);
                    }
                }
            }
        }
        return (long) d;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e){
        try {
            if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (e.getPlayer().getInventory().getItemInMainHand().getItemMeta() != null) {
                    if (itemMeta.get(e.getItem().getItemMeta()) != null) {
                        if (e.getPlayer().hasPermission("man10.atm.item")) {
                            Bukkit.dispatchCommand(e.getPlayer(), "atm");
                        }
                    } else {
                    }
                } else {
                }
            }
        }catch (NullPointerException ee){
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent e){
        if(menu.isEmpty()){
            return;
        }
        if(menu.get(e.getWhoClicked().getUniqueId()).equals("deposit")){
            menuFunctions.depositInventoryFunction(e);
            return;
        }
        if(e.getInventory() == null || e.getCurrentItem() == null){
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
        if(menu.get(e.getWhoClicked().getUniqueId()).equals("withdraw")){
            menuFunctions.withDrawMenuFunction(e);
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
        if(menu.get(e.getPlayer().getUniqueId()).equals("withdraw")){
            ATMLog atm = atmLog.get(e.getPlayer().getUniqueId());
            createAtmLog(e.getPlayer().getName(),e.getPlayer().getUniqueId(),"Withdraw",atm.tenThousand,atm.hundredThousand,atm.million,atm.tenMillion,atm.hundredMillion,e.getPlayer().getLocation());
            e.getPlayer().sendMessage(prefix + "現在の所持金は" + (long) vault.getBalance(e.getPlayer().getUniqueId()) + "円です");
            e.getPlayer().sendMessage(prefix + "              (" + menuFunctions.jpnBalForm((long) vault.getBalance(e.getPlayer().getUniqueId())) + ")");
        }
        if(menu.get(e.getPlayer().getUniqueId()).equals("deposit")){
            if(e.getInventory().getContents().length != 0) {
                double d = 0;
                for (int ii = 0; ii < e.getInventory().getContents().length; ii++) {
                    if (e.getInventory().getContents()[ii] != null && itemMeta.get(e.getInventory().getContents()[ii].getItemMeta()) != null) {
                        d = d + itemMeta.get(e.getInventory().getContents()[ii].getItemMeta()) * e.getInventory().getContents()[ii].getAmount();
                        Double s =  itemMeta.get(e.getInventory().getContents()[ii].getItemMeta());
                        ATMLog atm = atmLog.get(e.getPlayer().getUniqueId());
                        if(s == 10000){
                            atm.tenThousand = atm.tenThousand + e.getInventory().getContents()[ii].getAmount();
                            atmLog.put(e.getPlayer().getUniqueId(),atm);
                        }else if(s == 100000){
                            atm.hundredThousand = atm.hundredThousand + e.getInventory().getContents()[ii].getAmount();
                            atmLog.put(e.getPlayer().getUniqueId(),atm);
                        }else if(s == 1000000){
                            atm.million = atm.million + e.getInventory().getContents()[ii].getAmount();
                            atmLog.put(e.getPlayer().getUniqueId(),atm);
                        }else if(s == 10000000){
                            atm.tenMillion = atm.tenMillion + e.getInventory().getContents()[ii].getAmount();
                            atmLog.put(e.getPlayer().getUniqueId(),atm);
                        }else if(s == 100000000){
                            atm.hundredMillion = atm.hundredMillion + e.getInventory().getContents()[ii].getAmount();
                            atmLog.put(e.getPlayer().getUniqueId(),atm);
                        }
                    }
                }
                if(d == 0){
                    atmLog.remove(e.getPlayer().getUniqueId());
                    calcPrice.remove(e.getPlayer().getUniqueId());
                    menu.remove(e.getPlayer().getUniqueId());
                    return;
                }
                vault.givePlayerMoney(e.getPlayer().getUniqueId(), d, TransactionType.DEPOSIT, "Normal Deposit");
                e.getPlayer().sendMessage(prefix + menuFunctions.jpnBalForm((long)d)+ "円預入れました。");
                e.getPlayer().sendMessage(prefix + "現在の所持金は" + (long) vault.getBalance(e.getPlayer().getUniqueId()) + "円です");
                e.getPlayer().sendMessage(prefix + "              (" + menuFunctions.jpnBalForm((long) vault.getBalance(e.getPlayer().getUniqueId())) + ")");
                ATMLog atm = atmLog.get(e.getPlayer().getUniqueId());
                createAtmLog(e.getPlayer().getName(),e.getPlayer().getUniqueId(),"Deposit",atm.tenThousand,atm.hundredThousand,atm.million,atm.tenMillion,atm.hundredMillion,e.getPlayer().getLocation());
            }
        }
        atmLog.remove(e.getPlayer().getUniqueId());
        calcPrice.remove(e.getPlayer().getUniqueId());
        menu.remove(e.getPlayer().getUniqueId());
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("atm")) {
            Player p = (Player) sender;
            if(args.length == 0){
                if(!p.hasPermission("man10.atm")){
                    p.sendMessage(prefix + "あなたには権限がありません");
                    return false;
                }
                if(locked){
                    p.sendMessage(prefix + "ATMはロックされています");
                    return false;
                }
                ((Player) sender).closeInventory();
                p.sendMessage(prefix + "現在の所持金は" + (long) vault.getBalance(p.getUniqueId()) + "円です");
                p.sendMessage(prefix + "              (" + menuFunctions.jpnBalForm((long) vault.getBalance(p.getUniqueId())) + ")");
                menu.put(p.getUniqueId(),"main");
                p.openInventory(createMainMenu());
            }
            if(args.length == 1){
                if(args[0].equals("lock")){
                    if(!sender.hasPermission("man10.atm.lock")){
                        sender.sendMessage(prefix + "あなたには権限がありません");
                        return false;
                    }
                    if(locked){
                        locked = false;
                        sender.sendMessage(prefix + "ATMのロックを解除しました");
                        return false;
                    }
                    locked = true;
                    sender.sendMessage(prefix + "ATMをロックしました");
                    for(Player pp : Bukkit.getOnlinePlayers()){
                        if(menu.containsKey(pp.getUniqueId())){
                            pp.closeInventory();
                        }
                    }
                    return false;
                }
                if(args[0].equals("help")){
                    sender.sendMessage("§f§l==========" + prefix + "==========");
                    sender.sendMessage("§b/atm atmを開く");
                    sender.sendMessage("§b/atm help atmのコマンド一覧を見る");
                    sender.sendMessage("§b/atm lock atmをロックする");
                    sender.sendMessage("§f============================");
                    sender.sendMessage("§d§lCreated By Sho0");
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
