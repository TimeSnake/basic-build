/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.build.server;

import de.timesnake.basic.build.main.BasicBuild;
import de.timesnake.basic.bukkit.util.Server;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class EventManager implements Listener {

    public EventManager() {
        Server.registerListener(this, BasicBuild.getPlugin());
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
