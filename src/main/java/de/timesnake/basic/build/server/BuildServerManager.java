/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.build.server;

import de.timesnake.basic.bukkit.util.ServerManager;
import de.timesnake.basic.bukkit.util.world.WorldManager;
import de.timesnake.database.util.server.DbBuildServer;
import de.timesnake.library.basic.util.Status;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;

public class BuildServerManager extends ServerManager {

  public static BuildServerManager getInstance() {
    return (BuildServerManager) ServerManager.getInstance();
  }

  public void onBuildEnable() {
    Bukkit.setDefaultGameMode(GameMode.CREATIVE);
  }

  public void onBuildDisable() {
    ((DbBuildServer) this.getDatabase()).clearWorlds();
  }

  @Override
  protected WorldManager initWorldManager() {
    return new BuildWorldManager();
  }

  @Override
  public void loaded() {
    this.setStatus(Status.Server.SERVICE);
  }
}
