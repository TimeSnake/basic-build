package de.timesnake.extension.build.server;

import de.timesnake.basic.bukkit.util.ServerManager;
import de.timesnake.library.basic.util.Status;

public class BuildServerManager extends ServerManager {

    public static BuildServerManager getInstance() {
        return (BuildServerManager) ServerManager.getInstance();
    }

    public void onBuildEnable() {
    }

    @Override
    public void loaded() {
        this.setStatus(Status.Server.SERVICE);
    }
}
