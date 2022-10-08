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

package de.timesnake.extension.build.main;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.ServerManager;
import de.timesnake.database.util.Database;
import de.timesnake.extension.build.chat.Plugin;
import de.timesnake.extension.build.cmd.MapCmd;
import de.timesnake.extension.build.cmd.PvPCmd;
import de.timesnake.extension.build.server.BuildServerManager;
import de.timesnake.extension.build.server.EventManager;
import org.bukkit.plugin.java.JavaPlugin;

public class ExBuild extends JavaPlugin {

    public static ExBuild getPlugin() {
        return plugin;
    }

    private static ExBuild plugin;

    @Override
    public void onLoad() {
        ServerManager.setInstance(new BuildServerManager());
    }

    @Override
    public void onEnable() {
        plugin = this;

        Server.getCommandManager().addCommand(this, "map",
                new MapCmd(Database.getNetwork().getNetworkFile("templates").getFile()), Plugin.BUILD);
        Server.getCommandManager().addCommand(this, "pvp", new PvPCmd(), Plugin.BUKKIT);

        new EventManager();

        BuildServerManager.getInstance().onBuildEnable();
    }

    @Override
    public void onDisable() {
        BuildServerManager.getInstance().onBuildDisable();
    }
}
