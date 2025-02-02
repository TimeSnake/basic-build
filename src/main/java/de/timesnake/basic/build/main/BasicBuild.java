/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.build.main;

import de.timesnake.basic.build.cmd.MapCmd;
import de.timesnake.basic.build.cmd.PvPCmd;
import de.timesnake.basic.build.server.BuildServerManager;
import de.timesnake.basic.build.server.EventManager;
import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.ServerManager;
import de.timesnake.database.util.Database;
import de.timesnake.library.chat.Plugin;
import de.timesnake.library.waitinggames.WaitingGameCreateCmd;
import org.bukkit.plugin.java.JavaPlugin;

public class BasicBuild extends JavaPlugin {

  public static BasicBuild getPlugin() {
    return plugin;
  }

  private static BasicBuild plugin;

  @Override
  public void onLoad() {
    ServerManager.setInstance(new BuildServerManager());
  }

  @Override
  public void onEnable() {
    plugin = this;

    Server.getCommandManager().addCommand(this, "map",
        new MapCmd(Database.getNetwork().getNetworkFile("templates").getFile()), BuildServerManager.PLUGIN);
    Server.getCommandManager().addCommand(this, "pvp", new PvPCmd(), Plugin.SERVER);

    new EventManager();

    BuildServerManager.getInstance().onBuildEnable();

    Server.getCommandManager().addCommand(this, "wgc",
        new WaitingGameCreateCmd(BuildServerManager.getInstance().getWaitingGameManager()), Plugin.SERVER);
  }

  @Override
  public void onDisable() {
    BuildServerManager.getInstance().onBuildDisable();
  }
}
