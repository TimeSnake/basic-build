/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.build.server;

import de.timesnake.basic.bukkit.core.world.WorldManager;
import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.basic.bukkit.util.world.ExWorldType;
import de.timesnake.database.util.server.DbBuildServer;
import de.timesnake.library.network.NetworkUtils;
import de.timesnake.library.network.WorldSyncResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.GameRule;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;

public class BuildWorldManager extends WorldManager {

  private final Logger logger = LogManager.getLogger("build.world-manager");

  @Override
  public ExWorld createWorld(String name, ExWorldType type, boolean temporary) {
    ExWorld world = super.createWorld(name, type, temporary);

    if (world == null || world.getName().equals("world")) {
      return world;
    }

    String worldName = world.getName();

    if (!Files.isSymbolicLink(world.getWorldFolder().toPath())) {
      this.unloadWorld(world, false);
      WorldSyncResult result = Server.getNetwork().exportAndSyncWorld(Server.getName(), worldName, Path.of("build"));

      if (!result.isSuccessful()) {
        this.logger.warn("Error while exporting world '{}': {}", worldName, ((WorldSyncResult.Fail) result).getReason());
        return null;
      }

      world = this.createWorldFromFile(worldName);

      this.logger.info("Exported world " + worldName);
    }

    if (world == null) {
      return null;
    }

    this.setBuildRules(world);
    ((DbBuildServer) Server.getDatabase()).addWorld(worldName);
    return world;
  }

  @Override
  public @Nullable ExWorld cloneWorld(String name, ExWorld exWorld) {
    ExWorld clonedWorld = super.cloneWorld(name, exWorld);

    if (clonedWorld == null || clonedWorld.getName().equals("world")) {
      return clonedWorld;
    }

    String worldName = clonedWorld.getName();

    if (!Files.isSymbolicLink(clonedWorld.getWorldFolder().toPath())) {
      this.unloadWorld(clonedWorld, false);
      WorldSyncResult result = Server.getNetwork()
          .exportAndSyncWorld(Server.getName(),
              worldName, Path.of("build", NetworkUtils.DEFAULT_DIRECTORY));

      if (!result.isSuccessful()) {
        this.logger.warn("Error while exporting world {}: {}", worldName, ((WorldSyncResult.Fail) result).getReason());
        return null;
      }

      clonedWorld = this.createWorldFromFile(worldName);

      this.logger.info("Exported world '{}'", worldName);
    }

    if (clonedWorld == null) {
      return null;
    }

    this.setBuildRules(clonedWorld);
    ((DbBuildServer) Server.getDatabase()).addWorld(worldName);
    return clonedWorld;
  }

  @Override
  public boolean deleteWorld(ExWorld world, boolean deleteFiles) {
    boolean result = super.deleteWorld(world, deleteFiles);
    if (result) {
      ((DbBuildServer) Server.getDatabase()).removeWorld(world.getName());
    }
    return result;
  }

  @Override
  public boolean unloadWorld(ExWorld world, boolean save) {
    boolean result = super.unloadWorld(world, save);
    if (result) {
      ((DbBuildServer) Server.getDatabase()).removeWorld(world.getName());
    }
    return result;
  }

  public void setBuildRules(ExWorld world) {
    world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
    world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
    world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
    world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
    world.setGameRule(GameRule.DISABLE_RAIDS, true);
    world.setGameRule(GameRule.RANDOM_TICK_SPEED, 0);
  }
}
