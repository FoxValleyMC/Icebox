package Icebox.Events;

import Icebox.Main;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerQuitEvent;

public class QuitEvent implements Listener {

    private Main plugin;

    public QuitEvent(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler()
    public void onQuit(PlayerQuitEvent event) {
        plugin.saveInventory(event.getPlayer());
    }

}
