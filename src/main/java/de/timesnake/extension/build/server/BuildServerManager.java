package de.timesnake.extension.build.server;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.ServerManager;
import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.database.util.Database;
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

    private NetworkUtils networkUtils;

    public void onBuildEnable() {
        this.initNetworkUtils();
    }

    public void initNetworkUtils() {
        this.networkUtils = new NetworkUtils(Database.getNetwork().getNetworkFile("network").getFile().toPath());
    }

    @Override
    protected void initWorldManager() {
        if (this.networkUtils == null) {
            this.initNetworkUtils();
        }

        this.worldManager = new de.timesnake.basic.bukkit.core.world.WorldManager() {
            @Override
            public ExWorld createWorld(String name, Type type) {
                ExWorld world = super.createWorld(name, type);

                if (world == null || world.getName().equals("world")) return world;

                String worldName = world.getName();

                if (!Files.isSymbolicLink(world.getWorldFolder().toPath())) {
                    this.unloadWorld(world, false);
                    WorldSyncResult result = BuildServerManager.this.networkUtils.exportAndSyncWorld(Server.getName(),
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
                return world;
            }

            @Override
            public ExWorld cloneWorld(String name, ExWorld exWorld) {
                ExWorld world = super.cloneWorld(name, exWorld);
                if (world == null) return world;
                this.setBuildRules(world);
                return world;
            }

            public void setBuildRules(ExWorld world) {
                world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
                world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
                world.setGameRule(GameRule.DO_PATROL_SPAWNING, false);
                world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
                world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
                world.setGameRule(GameRule.DO_WARDEN_SPAWNING, false);
            }
        };
    }

    @Override
    public void loaded() {
        this.setStatus(Status.Server.SERVICE);
    }
}
