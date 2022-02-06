package de.timesnake.extension.build.cmd;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.Argument;
import de.timesnake.basic.bukkit.util.chat.ChatColor;
import de.timesnake.basic.bukkit.util.chat.CommandListener;
import de.timesnake.basic.bukkit.util.chat.Sender;
import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.library.basic.util.cmd.Arguments;
import de.timesnake.library.basic.util.cmd.ExCommand;

import java.util.List;

public class PvPCmd implements CommandListener {

    @Override
    public void onCommand(Sender sender, ExCommand<Sender, Argument> cmd, Arguments<Argument> args) {
        if (!sender.hasPermission("exbuild.pvp", 2417)) {
            return;
        }

        if (args.isLengthEquals(1, false)) {
            if (!args.get(0).isWorldName(true)) {
                return;
            }

            ExWorld world = args.get(0).toWorld();
            world.setPVP(!world.getPVP());

            sender.sendPluginMessage(ChatColor.PERSONAL + "PvP " + (world.getPVP() ? "enabled" : "disabled"));
            return;
        }

        if (!sender.isPlayer(true)) {
            return;
        }

        ExWorld world = sender.getUser().getExWorld();
        world.setPVP(!world.getPVP());

        sender.sendPluginMessage(ChatColor.PERSONAL + "PvP " + (world.getPVP() ? "enabled" : "disabled"));
    }

    @Override
    public List<String> getTabCompletion(ExCommand<Sender, Argument> cmd, Arguments<Argument> args) {
        if (args.length() == 1) {
            return Server.getCommandManager().getTabCompleter().getWorldNames();
        }
        return List.of();
    }
}
