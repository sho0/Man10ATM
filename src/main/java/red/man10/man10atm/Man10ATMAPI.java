package red.man10.man10atm;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;

/**
 * Created by sho on 2017/08/12.
 */
public class Man10ATMAPI {

    public Man10ATMAPI(){
    }

    static HashMap<ItemMeta,Double> itemMeta = new HashMap<>();

    public double getPrice(ItemStack item){
        if(!itemMeta.containsKey(item.getItemMeta())){
            return 0;
        }
        return itemMeta.get(item.getItemMeta());
    }
}
