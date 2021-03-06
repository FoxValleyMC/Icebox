package Icebox;

import Icebox.Events.*;
import NukkitDB.Provider.MongoDB;
import cn.nukkit.Player;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.Item;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Binary;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class Main extends PluginBase {

    private static Main instance;

    public static Main getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();

        if (!getConfig().getBoolean("use-MongoDB") || getConfig().getString("collection").isEmpty()) {
            getLogger().error("Please edit config");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getServer().getPluginManager().registerEvents(new JoinEvent(this), this);
        getServer().getPluginManager().registerEvents(new QuitEvent(this), this);

        instance = this;

    }

    public void saveInventory(Player player) {
        String collection = getConfig().getString("collection");
        String uuid = player.getUniqueId().toString();
        PlayerInventory inventory = player.getInventory();
        Map<Integer, Item> contents = inventory.getContents();
        List<Object> itemList = new ArrayList<>();
        for (Map.Entry<Integer, Item> entry : contents.entrySet()) {
            Item item = entry.getValue();
            String itemString = item.toString();
            String slot = entry.getKey().toString();
            String name = StringUtils.substringBefore(itemString, "(").replaceAll("Item ", "").trim();
            String id = StringUtils.substringBetween(itemString, "(", ")");
            String count = item.hasCompoundTag() ? StringUtils.substringBetween(itemString, ")x", " tags:0x") : StringUtils.substringAfter(itemString, ")x");
            String tag = item.hasCompoundTag() ? Binary.bytesToHexString(item.getCompoundTag()) : "null";
            String itemData = name+":"+slot+":"+id+":"+count+":"+tag;
            itemList.add(itemData);
        }
        MongoDB.updateOne(
                MongoDB.getCollection(collection), "uuid", uuid, "inventory", itemList
        );
    }

    public void restoreInventory(Player player) {
        String collection = getConfig().getString("collection");
        String uuid = player.getUniqueId().toString();
        Map<String, Object> query = MongoDB.getDocument(MongoDB.getCollection(collection), "uuid", uuid);
        Map<Integer, Item> inventoryContents = new HashMap<>();
        List<String> stringList = (List<String>) query.get("inventory");
        for (String string : stringList) {
            String[] itemData = string.split(":");
            String name = itemData[0];
            int slot = Integer.parseInt(itemData[1]);
            int id = Integer.parseInt(itemData[2]);
            int meta = Integer.parseInt(itemData[3]);
            int count = Integer.parseInt(itemData[4]);
            String hexString = itemData[5];
            byte[] tags = Binary.hexStringToBytes(hexString);
            Item item = Item.get(id, meta, count);
            if (!hexString.equals("null")) {
                item.setCompoundTag(tags);
            }
            inventoryContents.put(slot, item);
        }
        player.getInventory().setContents(inventoryContents);
    }
}
