package de.timesnake.extension.build.game;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.Argument;
import de.timesnake.basic.bukkit.util.chat.ChatColor;
import de.timesnake.basic.bukkit.util.chat.CommandListener;
import de.timesnake.basic.bukkit.util.chat.Sender;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.database.util.Database;
import de.timesnake.database.util.game.DbGame;
import de.timesnake.database.util.game.DbMap;
import de.timesnake.database.util.object.DbLocation;
import de.timesnake.database.util.server.DbTempGameServer;
import de.timesnake.library.basic.util.Status;
import de.timesnake.library.extension.util.cmd.Arguments;
import de.timesnake.library.extension.util.cmd.ExCommand;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class MapCmd implements CommandListener {

    enum Type {
        BLOCK("block"), MIDDLE("middle"), EXACT("exact"), BLOCK_FACING("block_facing"), MIDDLE_FACING("middle_facing"), EXACT_FACING("exact_facing"), EXACT_EXACT_FACING("exact_exact_facing");

        private final String name;

        Type(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

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
    }


    @Override
    public void onCommand(Sender sender, ExCommand<Sender, Argument> cmd, Arguments<Argument> args) {

        if (!sender.isPlayer(true)) {
            return;
        }

        User user = sender.getUser();

        if (!sender.hasPermission("exbuild.map", 2401)) {
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
            sender.sendMessageNotExist(mapName, 2501, "Map");
            return;
        }

        DbMap map = game.getMap(mapName);

        if (!args.isLengthHigherEquals(3, true)) {
            return;
        }

        switch (args.getString(2).toLowerCase()) {
            case "add", "set" -> this.handleLocationCmd(sender, user, args, game, map);
            case "update" -> this.handleUpdateCmd(sender, user, game, map);
        }

    }

    private void handleUpdateCmd(Sender sender, User user, DbGame game, DbMap map) {
        String worldName = map.getWorldName();
        ExWorld world = Server.getWorld(worldName);

        if (world == null) {
            sender.sendPluginMessage(ChatColor.WARNING + "World " + ChatColor.VALUE + worldName + ChatColor.WARNING + " not found");
            return;
        }

        File worldFolder = world.getWorldFolder();

        Collection<DbTempGameServer> servers = Database.getServers().getServers(de.timesnake.database.util.object.Type.Server.TEMP_GAME, game.getName());
        List<File> serverWorldFolders = new LinkedList<>();

        for (DbTempGameServer server : servers) {
            if (!server.getStatus().equals(Status.Server.OFFLINE)) {
                sender.sendPluginMessage(ChatColor.WARNING + "Stop all game servers before copying");
                return;
            }

            serverWorldFolders.add(new File(server.getFolderPath() + File.separator + worldName));
        }

        Server.getWorldManager().unloadWorld(world, true);

        for (File serverFolder : serverWorldFolders) {
            Server.getWorldManager().copyWorldFolderFiles(worldFolder, serverFolder);
        }

        Server.getWorldManager().createWorld(worldName);

        sender.sendPluginMessage(ChatColor.PERSONAL + "Updated world " + ChatColor.VALUE + worldName + ChatColor.PERSONAL + " for map " + ChatColor.VALUE + map.getName());

    }

    private void handleLocationCmd(Sender sender, User user, Arguments<Argument> args, DbGame game, DbMap map) {
        if (!args.isLengthHigherEquals(5, true)) {
            return;
        }

        String typeName = args.getString(3);

        Type type = Type.fromString(typeName);

        if (!args.get(4).isInt(true)) {
            return;
        }

        Integer number = args.get(4).toInt();

        switch (args.getString(2).toLowerCase()) {
            case "add":
                if (map.containsLocation(number)) {
                    sender.sendMessageAlreadyExist(String.valueOf(number), 2502, "Location");
                    return;
                }
            case "set":
                ExLocation loc = user.getExLocation();

                DbLocation dbLoc = switch (type) {
                    case BLOCK -> Server.getDbLocationFromLocation(loc.zeroBlock().zeroFacing());
                    case BLOCK_FACING -> Server.getDbLocationFromLocation(user.getExLocation().zeroBlock().roundFacing());
                    case EXACT -> Server.getDbLocationFromLocation(user.getExLocation().zeroFacing());
                    case EXACT_FACING -> Server.getDbLocationFromLocation(user.getExLocation().roundFacing());
                    case EXACT_EXACT_FACING -> user.getDbLocation();
                    case MIDDLE -> Server.getDbLocationFromLocation(loc.middleBlock().zeroFacing());
                    case MIDDLE_FACING -> Server.getDbLocationFromLocation(loc.middleBlock().roundFacing());
                };

                map.addLocation(number, dbLoc);
                sender.sendPluginMessage(ChatColor.PERSONAL + "Added location " + number + " to map " + map.getName());

                break;
        }
    }

    @Override
    public List<String> getTabCompletion(ExCommand<Sender, Argument> cmd, Arguments<Argument> args) {
        if (args.getLength() == 4) {
            return Type.getNames();
        } else if (args.getLength() == 3) {
            return List.of("add", "set", "update");
        } else if (args.getLength() == 2) {
            return Server.getCommandManager().getTabCompleter().getMapNames(args.getString(0));
        } else if (args.getLength() == 1) {
            return Server.getCommandManager().getTabCompleter().getGameNames();
        } else if (args.getLength() == 5) {
            return List.of("0", "1", "2", "3");
        }
        return List.of();
    }
}
