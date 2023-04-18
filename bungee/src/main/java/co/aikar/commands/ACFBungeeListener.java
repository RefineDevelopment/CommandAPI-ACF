package co.aikar.commands;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.util.concurrent.TimeUnit;

public class ACFBungeeListener implements Listener {

    private final BungeeCommandManager manager;
    private final Plugin plugin;

    public ACFBungeeListener(BungeeCommandManager manager, Plugin plugin) {
        this.manager = manager;
        this.plugin = plugin;
    }
}
