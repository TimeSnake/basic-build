/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.extension.build.server;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.extension.build.main.ExBuild;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class EventManager implements Listener {

    public EventManager() {
        Server.registerListener(this, ExBuild.getPlugin());
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent e) {
        e.setYield(0);
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent e) {
        e.setYield(0);
    }

}
