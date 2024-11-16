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
import de.timesnake.library.basic.util.ServerType;
import de.timesnake.library.basic.util.Tuple;
import de.timesnake.library.chat.Code;
import de.timesnake.library.chat.ExTextColor;
import de.timesnake.library.commands.PluginCommand;
import de.timesnake.library.commands.simple.Arguments;
import net.kyori.adventure.text.Component;
import org.apache.commons.io.FileUtils;
import org.bukkit.Material;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class MapCmd implements CommandListener {

  private final Map<ExWorld, List<HoloDisplay>> mapLocDisplays = new HashMap<>();

  private final File templateWorldDir;

  private final Code perm = Plugin.BUILD.createPermssionCode("exbuild.map");
  private final Code locationsPerm = Plugin.BUILD.createPermssionCode("exbuild.map.locations");
  private final Code updatePerm = Plugin.BUILD.createPermssionCode("exbuild.map.update");
  private final Code authorPerm = Plugin.BUILD.createPermssionCode("exbuild.map.author");
  private final Code showLocPerm = Plugin.BUILD.createPermssionCode("exbuild.map.showloc");
  private final Code propertyPerm = Plugin.BUILD.createPermssionCode("exbuild.map.property");
  private final Code namePerm = Plugin.BUILD.createPermssionCode("exbuild.map.name");
  private final Code playersPerm = Plugin.BUILD.createPermssionCode("exbuild.map.players");
  private final Code itemPerm = Plugin.BUILD.createPermssionCode("exbuild.map.item");
  private final Code descriptionPerm = Plugin.BUILD.createPermssionCode("exbuild.map.description");

  private final Code mapNotExists = Plugin.BUILD.createHelpCode("Map not exists");
  private final Code locationAlreadyExists = Plugin.BUILD.createHelpCode("Location already exists");

  public MapCmd(File templateDir) {
    this.templateWorldDir = new File(templateDir.getAbsolutePath() + File.separator + "worlds");
  }

  @Override
  public void onCommand(Sender sender, PluginCommand cmd, Arguments<Argument> args) {
    sender.isPlayerElseExit(true);
    sender.hasPermissionElseExit(this.perm);
    args.isLengthHigherEqualsElseExit(1, true);

    String gameName = args.getString(0).toLowerCase();
    if (!Database.getGames().getGamesName().contains(gameName)) {
      sender.sendMessageGameNotExist(gameName);
      return;
    }

    DbGame game = Database.getGames().getGame(gameName);
    if (game == null) {
      sender.sendMessageGameNotExist(gameName);
      return;
    }

    args.isLengthHigherEqualsElseExit(2, true);

    String mapName = args.getString(1);
    if (!game.containsMap(mapName)) {
      if (args.isLengthHigherEquals(3, false) && args.get(2).equalsIgnoreCase("create")) {
        this.handleCreateCmd(sender, game, args, mapName);
        return;
      }

      sender.sendMessageNotExist(mapName, this.mapNotExists, "Map");
      return;
    }

    DbMap map = game.getMap(mapName);
    if (map == null) {
      sender.sendMessageNotExist(mapName, this.mapNotExists, "Map");
      return;
    }

    args.isLengthHigherEqualsElseExit(3, true);

    User user = sender.getUser();

    switch (args.getString(2).toLowerCase()) {
      case "add", "set" -> {
        sender.hasPermissionElseExit(this.locationsPerm);
        this.handleLocationCmd(sender, user, args, map);
      }
      case "update" -> {
        sender.hasPermissionElseExit(this.updatePerm);
        this.handleUpdateCmd(sender, game, map);
      }
      case "author" -> {
        sender.hasPermissionElseExit(this.authorPerm);
        this.handleAuthorCmd(sender, args, map);
      }
      case "show_loc" -> {
        sender.hasPermissionElseExit(this.showLocPerm);
        this.handleShowLocCmd(sender, args, map);
      }
      case "property" -> {
        sender.hasPermissionElseExit(this.propertyPerm);
        this.handlePropertyCmd(sender, args, map);
      }
      case "name" -> {
        sender.hasPermissionElseExit(this.namePerm);
        this.handNameCmd(sender, args, map);
      }
      case "players" -> {
        sender.hasPermissionElseExit(this.playersPerm);
        this.handPlayersCmd(sender, args, map);
      }
      case "item" -> {
        sender.hasPermissionElseExit(this.itemPerm);
        this.handleItemCmd(sender, args, map);
      }
      case "description" -> {
        sender.hasPermissionElseExit(this.descriptionPerm);
        this.handleDescriptionCmd(sender, args, map);
      }
    }

  }

  private void handleDescriptionCmd(Sender sender, Arguments<Argument> args, DbMap map) {
    sender.sendPluginTDMessage("§wNot implemented yet");
    // TODO set map description
  }

  private void handleItemCmd(Sender sender, Arguments<Argument> args, DbMap map) {
    args.isLengthEqualsElseExit(4, true);

    Material material = args.get(3).toMaterialOrExit(true);
    map.setItemName(material.name());

    sender.sendPluginTDMessage("§sUpdated item of map §v" + map.getName() + "§s to §v" + material.name());
  }

  private void handPlayersCmd(Sender sender, Arguments<Argument> args, DbMap map) {
    args.isLengthEqualsElseExit(5, true);

    int minPlayers = args.get(3).toBoundedIntOrExit(0, 64, true);
    int maxPlayers = args.get(4).toBoundedIntOrExit(0, 64, true);


    map.setMinPlayers(minPlayers);
    map.setMaxPlayers(maxPlayers);

    sender.sendPluginTDMessage("§sUpdated players of map §v" + map.getName() + "§s to §v" + minPlayers + " - " + maxPlayers);
  }

  private void handleCreateCmd(Sender sender, DbGame game, Arguments<Argument> args, String mapName) {
    game.addMap(mapName);
    sender.sendPluginTDMessage("§sCreated map §v" + mapName);
  }

  private void handNameCmd(Sender sender, Arguments<Argument> args, DbMap map) {
    args.isLengthHigherEqualsElseExit(4, true);

    Tuple<String, Integer> collapsed = args.collapse(3);
    String name = collapsed.getA();

    map.setDisplayName(name);
    sender.sendPluginTDMessage("§pUpdated name of map §v" + map.getName() + "§s to §v" + name);
  }

  private void handlePropertyCmd(Sender sender, Arguments<Argument> args, DbMap map) {
    args.isLengthEqualsElseExit(5, true);

    String key = args.getString(3).toLowerCase();
    String value = args.getString(4);

    map.setProperty(key, value);
    sender.sendPluginTDMessage("§sUpdated property §v" + key + "§s of map §v" + map.getName() + "§s to §v" + value);
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
        String text = String.valueOf(entry.getKey());
        try {
          ExLocation location = Server.getExLocationFromDbLocation(entry.getValue());
          Optional<HoloDisplay> displayAtSameLoc = displays.stream()
              .filter(d -> d.getLocation().equals(location))
              .findFirst();

          if (displayAtSameLoc.isPresent()) {
            HoloDisplay display = displayAtSameLoc.get();
            displays.remove(display);
            Server.getEntityManager().unregisterEntity(display);
            text = display.getText().get(0) + ", " + text;
          }

          HoloDisplay display = new HoloDisplay(location, List.of(text));
          displays.add(display);
          Server.getEntityManager().registerEntity(display);
        } catch (WorldNotExistException e) {
          sender.sendPluginTDMessage("§wWorld §v" + map.getWorldName() + "§w not found for location §v" + text);
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

    String gameName = game.getInfo().getName();

    String gameType;
    if (game instanceof DbTmpGame) {
      gameType = ServerType.TEMP_GAME.getShortName();
    } else {
      gameType = ServerType.GAME.getShortName();
    }

    File worldTemplateDirectory = new File(this.templateWorldDir.getAbsolutePath()
                                           + File.separator + gameType
                                           + File.separator + gameName
                                           + File.separator + worldName);

    try {
      FileUtils.deleteDirectory(worldTemplateDirectory);
      boolean result = Server.getWorldManager().copyWorldFolderFiles(worldDirectory.toPath().toRealPath().toFile(),
          worldTemplateDirectory);

      if (!result) {
        sender.sendPluginTDMessage("§wFailed to update world §v" + worldName);
        return;
      }
    } catch (IOException e) {
      sender.sendPluginTDMessage("§wError while updating world §v" + worldName);
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

    if (type == null) {
      sender.sendPluginTDMessage("§wType not exists");
      return;
    }

    if (!args.get(4).isInt(true)) {
      return;
    }

    Integer number = args.get(4).toInt();

    ExLocation loc = user.getExLocation();

    DbLocation dbLoc = switch (type) {
      case BLOCK -> Server.getDbLocationFromLocation(loc.zeroBlock().zeroFacing());
      case BLOCK_FACING -> Server.getDbLocationFromLocation(user.getExLocation().zeroBlock().roundFacing());
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
        sender.sendPluginTDMessage("§sAdded location §v" + number + "§s to map §v" + map.getName());
      }
      case "set" -> {
        map.setLocation(number, dbLoc);
        sender.sendPluginTDMessage("§sUpdated location §v" + number + "§s to map §v" + map.getName());
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
        sender.sendPluginTDMessage("§sAdded author §v" + author.getName() + "§s to map §v" + map.getName());
      }
      case "remove" -> {
        map.removeAuthor(author.getUniqueId());
        sender.sendPluginTDMessage("§sRemoved author §v" + author.getName() + "§s from map §v" + map.getName());
      }
    }
  }

  @Override
  public Completion getTabCompletion() {
    return new Completion(this.perm)
        .addArgument(Completion.ofGameNames()
            .addArgument(new Completion(((sender, cmd, args) -> Completion.ofMapNames(args.getString(0))
                .completeFirst(sender, cmd, args)))
                .addArgument(new Completion(this.showLocPerm, "show_loc"))
                .addArgument(new Completion(this.updatePerm, "update"))
                .addArgument(new Completion(this.locationsPerm, "add", "set")
                    .addArgument(new Completion(Type.getNames())))
                .addArgument(new Completion(this.authorPerm, "author")
                    .addArgument(new Completion("add", "remove")
                        .addArgument(Completion.ofPlayerNames())))
                .addArgument(new Completion(this.namePerm, "name")
                    .addArgument(new Completion("<name>")))
                .addArgument(new Completion(this.descriptionPerm, "description")
                    .addArgument(new Completion("<description>")))
                .addArgument(new Completion(this.itemPerm, "item")
                    .addArgument(new Completion("<material>")))
                .addArgument(new Completion(this.playersPerm, "players")
                    .addArgument(new Completion("<min>")
                        .addArgument(new Completion("<max>"))))
                .addArgument(new Completion(this.propertyPerm, "property")
                    .addArgument(new Completion("<key>")
                        .addArgument(new Completion("<value>"))))));
  }

  @Override
  public String getPermission() {
    return this.perm.getPermission();
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
