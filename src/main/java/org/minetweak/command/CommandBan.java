package org.minetweak.command;

import net.minecraft.src.EnumChatFormatting;
import org.minetweak.Minetweak;
import org.minetweak.Server;
import org.minetweak.entity.Player;
import org.minetweak.util.StringUtils;

public class CommandBan extends CommandExecutor {

    @Override
    public void executeCommand(CommandSender sender, String overallCommand, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(EnumChatFormatting.AQUA + "Usage: /ban <player> [reason]");
            return;
        }
        Player targetPlayer = Minetweak.getPlayerByName(args[0]);
        if (args.length == 1) {
            targetPlayer.banPlayer();
        } else if (args.length >= 2) {
            args = StringUtils.dropFirstString(args);
            String reason = mergeArgs(args);
            targetPlayer.banPlayer(reason);
        }
    }

}
