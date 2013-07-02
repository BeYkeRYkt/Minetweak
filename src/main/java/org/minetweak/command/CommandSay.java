package org.minetweak.command;

import net.minecraft.src.EnumChatFormatting;
import org.minetweak.Minetweak;
import org.minetweak.util.StringUtils;

public class CommandSay extends CommandExecutor {
    @Override
    public void executeCommand(CommandSender sender, String overallCommand, String[] args) {
        if (args.length<2) {
            sender.sendMessage(EnumChatFormatting.AQUA + "Usage: /say <user> <message>");
        }
        if (Minetweak.isPlayerOnline(args[1])) {
            String message = StringUtils.toString(StringUtils.dropFirstString(args));
            Minetweak.getPlayerByName(args[1]).sendMessage(EnumChatFormatting.ITALIC + "Console: " + EnumChatFormatting.RESET  + message);
        }
    }
}
