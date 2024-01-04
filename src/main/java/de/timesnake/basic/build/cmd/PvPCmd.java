/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.build.cmd;

import de.timesnake.basic.bukkit.util.chat.cmd.Argument;
import de.timesnake.basic.bukkit.util.chat.cmd.CommandListener;
import de.timesnake.basic.bukkit.util.chat.cmd.Completion;
import de.timesnake.basic.bukkit.util.chat.cmd.Sender;
import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.library.chat.Code;
import de.timesnake.library.chat.Plugin;
import de.timesnake.library.commands.PluginCommand;
import de.timesnake.library.commands.simple.Arguments;

public class PvPCmd implements CommandListener {

  private final Code pvpPerm = Plugin.SERVER.createPermssionCode("exbuild.pvp");

  @Override
  public void onCommand(Sender sender, PluginCommand cmd, Arguments<Argument> args) {
    sender.hasPermissionElseExit(this.pvpPerm);

    if (args.isLengthEquals(1, false)) {
      if (!args.get(0).isWorldName(true)) {
        return;
      }

      ExWorld world = args.get(0).toWorld();
      world.setPVP(!world.getPVP());

      sender.sendPluginTDMessage("§sPvP " + (world.getPVP() ? "enabled" : "disabled"));
      return;
    }

    if (!sender.isPlayer(true)) {
      return;
    }

    ExWorld world = sender.getUser().getExWorld();
    world.setPVP(!world.getPVP());

    sender.sendPluginTDMessage("§sPvP " + (world.getPVP() ? "enabled" : "disabled"));
  }

  @Override
  public Completion getTabCompletion() {
    return new Completion(this.pvpPerm)
        .addArgument(Completion.ofWorldNames());
  }

  @Override
  public String getPermission() {
    return this.pvpPerm.getPermission();
  }
}
