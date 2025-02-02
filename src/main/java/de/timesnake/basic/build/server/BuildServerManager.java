/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.build.server;

import de.timesnake.basic.bukkit.util.ServerManager;
import de.timesnake.basic.bukkit.util.world.WorldManager;
import de.timesnake.database.util.server.DbBuildServer;
import de.timesnake.library.basic.util.Status;
import de.timesnake.library.chat.Plugin;
import de.timesnake.library.waitinggames.WaitingGameManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;

public class BuildServerManager extends ServerManager {

  public static final Plugin PLUGIN = new de.timesnake.library.chat.Plugin("Build", "XBL");

  public static BuildServerManager getInstance() {
    return (BuildServerManager) ServerManager.getInstance();
  }

  private WaitingGameManager waitingGameManager;

  public void onBuildEnable() {
    Bukkit.setDefaultGameMode(GameMode.CREATIVE);

    this.waitingGameManager = new WaitingGameManager();
  }

  public void onBuildDisable() {
    ((DbBuildServer) this.getDatabase()).clearWorlds();
    this.waitingGameManager.onDisable();
  }

  @Override
  protected WorldManager initWorldManager() {
    return new BuildWorldManager();
  }

  @Override
  public void loaded() {
    this.setStatus(Status.Server.SERVICE);
  }

  public WaitingGameManager getWaitingGameManager() {
    return waitingGameManager;
  }
}
