package org.minetweak.command;

import org.minetweak.Minetweak;
import org.minetweak.chat.TabCompletion;
import org.minetweak.chat.TextColor;
import org.minetweak.console.Console;
import org.minetweak.entity.Player;
import org.minetweak.server.GameMode;
import org.minetweak.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;

public class CommandGamemode extends CommandExecutor {

    @Override
    public void executeCommand(CommandSender sender, String overallCommand, String[] args) {
        if (args.length != 1 && args.length != 2) {
            sender.sendMessage("Usage: /gamemode <gamemode> [player]");
            return;
        } else if (!sender.hasPermission("minetweak.command.gamemode")) {
            noPermission(sender, "change gamemodes");
            return;
        }

        int gamemode = -1;

        if (!StringUtils.isInteger(args[0])) {
            if (args[0].toLowerCase().equals("survival")) gamemode = 0;
            if (args[0].toLowerCase().equals("creative")) gamemode = 1;
            if (args[0].toLowerCase().equals("adventure")) gamemode = 2;
        } else {
            if (Integer.parseInt(args[0]) == 0) gamemode = 0;
            if (Integer.parseInt(args[0]) == 1) gamemode = 1;
            if (Integer.parseInt(args[0]) == 2) gamemode = 2;
        }

        if (args.length == 1) {
            if (sender instanceof Console) {
                sender.sendMessage("Consoles can not run this command.");
                return;
            }

            Player player = (Player) sender;
            setPlayerGamemode(player, gamemode);
        } else {
            Player player = Minetweak.getPlayerByName(args[1].toLowerCase());
            if (player == null) {
                sender.sendMessage(TextColor.RED + "Player does not exist.");
                return;
            }
            setPlayerGamemode(player, gamemode);
        }
    }

    @Override
    public String getHelpInfo() {
        return "Set player's gamemode";
    }

    @Override
    public void getTabCompletion(CommandSender sender, String input, ArrayList<String> completions) {
        String[] split = input.split(" ");
        if (split.length == 0 || !split[0].equals("")) {
            completions.addAll(Arrays.asList("survival", "creative", "adventure"));
        } else if (split.length == 1) {
            completions.addAll(Minetweak.getPlayers().keySet());
        } else if (split.length == 2) {
            completions.addAll(TabCompletion.getPlayersMatching(split[1]));
        }
    }

    public static void setPlayerGamemode(Player player, int gamemode) {
        if (gamemode == 0) player.setGameMode(GameMode.SURVIVAL);
        if (gamemode == 1) player.setGameMode(GameMode.CREATIVE);
        if (gamemode == 2) player.setGameMode(GameMode.ADVENTURE);
    }
}
