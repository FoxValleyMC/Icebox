package Icebox;

import Icebox.Events.*;
import cn.nukkit.Player;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.Item;
import cn.nukkit.plugin.PluginBase;

import java.util.*;

public class Main extends PluginBase {

    private static Main instance;

    public static Main getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();

        try {
            new NukkitDB.Main();
        } catch (NoClassDefFoundError error) {
            getLogger().warning("Please download and install plugin 'NukkitDB': https://nukkitx.com/resources/nukkitdb.364/");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (getConfig().getString("database").isEmpty() || getConfig().getString("collection").isEmpty()) {
            getLogger().warning("Please edit your config!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getServer().getPluginManager().registerEvents(new JoinEvent(this), this);
        getServer().getPluginManager().registerEvents(new QuitEvent(this), this);

        instance = this;

    }

    public void saveInventory(Player player) {
        PlayerInventory inventory = player.getInventory();
        Object[] objectArray = inventory.slots.entrySet().toArray();
        List<Object> stringList = new ArrayList<>();
        for (Object object: objectArray) {
            String data = object.toString().replaceAll("[^0-9:=)]", "");
            String regex = data.replaceAll("[=)]", ":");
            stringList.add(regex);
        }
        DatabaseHandler.update(player.getUniqueId().toString(), "inventory", stringList);
    }

    public void restoreInventory(Player player) {
        player.getInventory().clearAll();
        Map<String, Object> query = DatabaseHandler.query(player.getUniqueId().toString(), "uuid");
        Map<Integer, Item> inventoryContents = new HashMap<>();
        List<String> stringList = (List<String>) query.get("inventory");
        for (String string : stringList) {
            String[] itemData = string.split(":");
            Item item = Item.get(Integer.parseInt(itemData[1]), Integer.parseInt(itemData[2]), Integer.parseInt(itemData[3]));
            inventoryContents.put(Integer.parseInt(itemData[0]), item);
        }
        player.getInventory().setContents(inventoryContents);
    }
}
