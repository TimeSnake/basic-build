/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.build.cmd;

import de.timesnake.basic.build.chat.Plugin;
import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.cmd.Argument;
import de.timesnake.basic.bukkit.util.chat.cmd.CommandListener;
import de.timesnake.basic.bukkit.util.chat.cmd.Completion;
import de.timesnake.basic.bukkit.util.chat.cmd.Sender;
import de.timesnake.basic.bukkit.util.exception.WorldNotExistException;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.basic.bukkit.util.world.entity.HoloDisplay;
import de.timesnake.database.util.Database;
import de.timesnake.database.util.game.DbGame;
import de.timesnake.database.util.game.DbMap;
import de.timesnake.database.util.game.DbTmpGame;
import de.timesnake.database.util.object.DbLocation;
import de.timesnake.database.util.user.DbUser;
import de.timesnake.library.chat.ExTextColor;
import de.timesnake.library.commands.PluginCommand;
import de.timesnake.library.commands.simple.Arguments;
import de.timesnake.library.extension.util.chat.Code;
import net.kyori.adventure.text.Component;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class MapCmd implements CommandListener {

  private final Map<ExWorld, List<HoloDisplay>> mapLocDisplays = new HashMap<>();

  private final File templateWorldDir;

  private final Code mapPerm = Plugin.BUILD.createPermssionCode("exbuild.map");
  private final Code mapNotExists = Plugin.BUILD.createHelpCode("Map not exists");
  private final Code locationAlreadyExists = Plugin.BUILD.createHelpCode("Location already exists");

  public MapCmd(File templateDir) {
    this.templateWorldDir = new File(templateDir.getAbsolutePath() + File.separator + "worlds");
  }

  @Override
  public void onCommand(Sender sender, PluginCommand cmd,
      Arguments<Argument> args) {

    if (!sender.isPlayer(true)) {
      return;
    }

    User user = sender.getUser();

    if (!sender.hasPermission(this.mapPerm)) {
      return;
    }

    if (!args.isLengthHigherEquals(1, true)) {
      return;
    }

    String gameName = args.getString(0).toLowerCase();

    if (!Database.getGames().getGamesName().contains(gameName)) {
      sender.sendMessageGameNotExist(gameName);
      return;
    }

    DbGame game = Database.getGames().getGame(gameName);

    if (!args.isLengthHigherEquals(2, true)) {
      return;
    }

    String mapName = args.getString(1);

    if (!game.containsMap(mapName)) {
      sender.sendMessageNotExist(mapName, this.mapNotExists, "Map");
      return;
    }

    DbMap map = game.getMap(mapName);

    if (!args.isLengthHigherEquals(3, true)) {
      return;
    }

    switch (args.getString(2).toLowerCase()) {
      case "add", "set" -> this.handleLocationCmd(sender, user, args, map);
      case "update" -> this.handleUpdateCmd(sender, game, map);
      case "author" -> this.handleAuthorCmd(sender, args, map);
      case "show_loc" -> this.handleShowLocCmd(sender, args, map);
    }

  }

  private void handleShowLocCmd(Sender sender, Arguments<Argument> args, DbMap map) {
    ExWorld world = Server.getWorldManager().getWorld(map.getWorldName());

    if (world == null) {
      sender.sendPluginTDMessage("§wWorld §v" + map.getWorldName() + "§w not found");
      return;
    }

    if (this.mapLocDisplays.get(world) == null) {
      List<HoloDisplay> displays = new LinkedList<>();
      for (Map.Entry<Integer, DbLocation> entry : map.getMapLocations().entrySet()) {
        try {
          HoloDisplay display = new HoloDisplay(Server.getExLocationFromDbLocation(entry.getValue()),
              List.of(String.valueOf(entry.getKey())));
          displays.add(display);
          Server.getEntityManager().registerEntity(display);
        } catch (WorldNotExistException e) {
          sender.sendPluginTDMessage("§wWorld §v" + map.getWorldName() + "§w not found for location §v" + entry.getKey());
        }
      }
      this.mapLocDisplays.put(world, displays);
      sender.sendPluginTDMessage("§sShowing locations of map §v" + map.getName());
    } else {
      this.mapLocDisplays.remove(world).forEach(d -> Server.getEntityManager().unregisterEntity(d));
      sender.sendPluginTDMessage("§sHiding locations of map §v" + map.getName());
    }
  }

  private void handleUpdateCmd(Sender sender, DbGame game, DbMap map) {
    String worldName = map.getWorldName();
    ExWorld world = Server.getWorld(worldName);

    if (world == null) {
      sender.sendPluginTDMessage("§wWorld §v" + map.getWorldName() + "§w not found");
      return;
    }

    File worldDirectory = world.getWorldFolder();

    Server.getWorldManager().unloadWorld(world, true);

    String gameType;
    String gameName = game.getInfo().getName();

    if (game instanceof DbTmpGame) {
      gameType = "tempgame";
    } else {
      gameType = "game";
    }

    File worldTemplateDirectory = new File(
        this.templateWorldDir.getAbsolutePath() + File.separator + gameType +
            File.separator + gameName + File.separator + worldName);

    try {
      FileUtils.deleteDirectory(worldTemplateDirectory);
      Server.getWorldManager()
          .copyWorldFolderFiles(worldDirectory.toPath().toRealPath().toFile(),
              worldTemplateDirectory);
    } catch (IOException e) {
      e.printStackTrace();
      sender.sendPluginMessage(
          Component.text("Error while updating world ", ExTextColor.WARNING)
              .append(Component.text(worldName, ExTextColor.VALUE)));
      Server.getWorldManager().createWorld(worldName);
      return;
    }

    Server.getWorldManager().createWorld(worldName);

    sender.sendPluginMessage(Component.text("Updated world ", ExTextColor.PERSONAL)
        .append(Component.text(worldName, ExTextColor.VALUE))
        .append(Component.text(" for map ", ExTextColor.PERSONAL))
        .append(Component.text(map.getName(), ExTextColor.VALUE)));

  }

  private void handleLocationCmd(Sender sender, User user, Arguments<Argument> args, DbMap map) {
    if (!args.isLengthHigherEquals(5, true)) {
      return;
    }

    String typeName = args.getString(3);

    Type type = Type.fromString(typeName);

    if (!args.get(4).isInt(true)) {
      return;
    }

    Integer number = args.get(4).toInt();

    ExLocation loc = user.getExLocation();

    DbLocation dbLoc = switch (type) {
      case BLOCK -> Server.getDbLocationFromLocation(loc.zeroBlock().zeroFacing());
      case BLOCK_FACING -> Server.getDbLocationFromLocation(
          user.getExLocation().zeroBlock().roundFacing());
      case EXACT -> Server.getDbLocationFromLocation(user.getExLocation().zeroFacing());
      case EXACT_FACING -> Server.getDbLocationFromLocation(user.getExLocation().roundFacing());
      case EXACT_EXACT_FACING -> user.getDbLocation();
      case MIDDLE -> Server.getDbLocationFromLocation(loc.middleHorizontalBlock().zeroFacing());
      case MIDDLE_FACING -> Server.getDbLocationFromLocation(loc.middleHorizontalBlock().roundFacing());
    };

    switch (args.getString(2).toLowerCase()) {
      case "add" -> {
        if (map.containsLocation(number)) {
          sender.sendMessageAlreadyExist(String.valueOf(number), this.locationAlreadyExists, "Location");
          return;
        }
        map.setLocation(number, dbLoc);
        sender.sendPluginMessage(Component.text("Added location ", ExTextColor.PERSONAL)
            .append(Component.text(number, ExTextColor.VALUE))
            .append(Component.text(" to map ", ExTextColor.PERSONAL))
            .append(Component.text(map.getName(), ExTextColor.VALUE)));
      }
      case "set" -> {
        map.setLocation(number, dbLoc);
        sender.sendPluginMessage(Component.text("Updated location ", ExTextColor.PERSONAL)
            .append(Component.text(number, ExTextColor.VALUE))
            .append(Component.text(" to map ", ExTextColor.PERSONAL))
            .append(Component.text(map.getName(), ExTextColor.VALUE)));
      }
    }
  }

  private void handleAuthorCmd(Sender sender, Arguments<Argument> args, DbMap map) {
    if (!args.get(4).isPlayerDatabaseName(true)) {
      return;
    }

    DbUser author = args.get(4).toDbUser();

    switch (args.get(3).toLowerCase()) {
      case "add" -> {
        map.addAuthor(author.getUniqueId());
        sender.sendPluginMessage(Component.text("Added author ", ExTextColor.PERSONAL)
            .append(Component.text(author.getName(), ExTextColor.VALUE))
            .append(Component.text("to map ", ExTextColor.PERSONAL))
            .append(Component.text(map.getName(), ExTextColor.VALUE)));
      }
      case "remove" -> {
        map.removeAuthor(author.getUniqueId());
        sender.sendPluginMessage(Component.text("Removed author ", ExTextColor.PERSONAL)
            .append(Component.text(author.getName(), ExTextColor.VALUE))
            .append(Component.text("from map ", ExTextColor.PERSONAL))
            .append(Component.text(map.getName(), ExTextColor.VALUE)));
      }
    }
  }

  @Override
  public Completion getTabCompletion() {
    return new Completion(this.mapPerm)
        .addArgument(Completion.ofGameNames()
            .addArgument(new Completion(((sender, cmd, args) -> Completion.ofMapNames(args.getString(0))
                .complete(sender, new PluginCommand(cmd, sender.getPlugin()), args, args.length())))
                .addArgument(new Completion("update", "show_loc"))
                .addArgument(new Completion("add", "set")
                    .addArgument(new Completion(Type.getNames())))
                .addArgument(new Completion("author")
                    .addArgument(new Completion("add", "remove")
                        .addArgument(Completion.ofPlayerNames())))));
  }

  @Override
  public String getPermission() {
    return this.mapPerm.getPermission();
  }

  enum Type {
    BLOCK("block"),
    MIDDLE("middle"),
    EXACT("exact"),
    BLOCK_FACING("block_facing"),
    MIDDLE_FACING("middle_facing"),
    EXACT_FACING("exact_facing"),
    EXACT_EXACT_FACING("exact_exact_facing");

    public static Type fromString(String typeString) {
      for (Type type : Type.values()) {
        if (type.getName().equalsIgnoreCase(typeString)) {
          return type;
        }
      }
      return null;
    }

    public static List<String> getNames() {
      List<String> names = new ArrayList<>();
      for (Type type : Type.values()) {
        names.add(type.getName());
      }
      return names;
    }

    private final String name;

    Type(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }
  }
}
