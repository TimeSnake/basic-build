package de.timesnake.extension.build.main;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.extension.build.chat.Plugin;
import de.timesnake.extension.build.cmd.PvPCmd;
import de.timesnake.extension.build.game.MapLocationCmd;
import org.bukkit.plugin.java.JavaPlugin;

public class ExBuild extends JavaPlugin {

    private static ExBuild plugin;

    public void onEnable() {
        plugin = this;

        Server.getCommandManager().addCommand(this, "map", new MapLocationCmd(), Plugin.BUILD);
        Server.getCommandManager().addCommand(this, "pvp", new PvPCmd(), Plugin.BUKKIT);
    }

    public static ExBuild getPlugin() {
        return plugin;
    }
}
