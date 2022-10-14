/*
 * extension-build.main
 * Copyright (C) 2022 timesnake
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; If not, see <http://www.gnu.org/licenses/>.
 */

package de.timesnake.extension.build.server;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.ServerManager;
import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.basic.bukkit.util.world.ExWorldType;
import de.timesnake.basic.bukkit.util.world.WorldManager;
import de.timesnake.database.util.server.DbBuildServer;
import de.timesnake.extension.build.chat.Plugin;
import de.timesnake.library.basic.util.Status;
import de.timesnake.library.network.NetworkUtils;
import de.timesnake.library.network.WorldSyncResult;
import org.bukkit.GameRule;

import java.nio.file.Files;
import java.nio.file.Path;

public class BuildServerManager extends ServerManager {

    public static BuildServerManager getInstance() {
        return (BuildServerManager) ServerManager.getInstance();
    }

    public void onBuildEnable() {
    }

    public void onBuildDisable() {
        ((DbBuildServer) this.getDatabase()).clearWorlds();
    }

    @Override
    protected WorldManager initWorldManager() {
        return new de.timesnake.basic.bukkit.core.world.WorldManager() {
            @Override
            public ExWorld createWorld(String name, ExWorldType type) {
                ExWorld world = super.createWorld(name, type);

                if (world == null || world.getName().equals("world")) return world;

                String worldName = world.getName();

                if (!Files.isSymbolicLink(world.getWorldFolder().toPath())) {
                    this.unloadWorld(world, false);
                    WorldSyncResult result = Server.getNetwork().exportAndSyncWorld(Server.getName(),
                            worldName, Path.of("build", NetworkUtils.DEFAULT_DIRECTORY));

                    if (!result.isSuccessful()) {
                        Server.printWarning(Plugin.BUILD, "Error while exporting world " + worldName + ", " +
                                ((WorldSyncResult.Fail) result).getReason());
                        return null;
                    }

                    world = this.createWorldFromFile(worldName);
                }

                if (world == null) return world;

                this.setBuildRules(world);
                ((DbBuildServer) Server.getDatabase()).addWorld(worldName);
                return world;
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
        };
    }

    @Override
    public void loaded() {
        this.setStatus(Status.Server.SERVICE);
    }
}
