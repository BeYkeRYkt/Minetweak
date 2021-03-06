package net.minecraft.command;

import net.minecraft.crash.exception.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.utils.chat.ChatMessageComponent;

import java.util.List;

public class CommandServerSay extends CommandBase {
    public String getCommandName() {
        return "say";
    }

    /**
     * Return the required permission level for this command.
     */
    public int getRequiredPermissionLevel() {
        return 1;
    }

    public String getCommandUsage(ICommandSender par1ICommandSender) {
        return "commands.say.usage";
    }

    public void processCommand(ICommandSender par1ICommandSender, String[] par2ArrayOfStr) {
        if (par2ArrayOfStr.length > 0 && par2ArrayOfStr[0].length() > 0) {
            String var3 = func_82361_a(par1ICommandSender, par2ArrayOfStr, 0, true);
            MinecraftServer.getServer().getConfigurationManager().sendChatMessageToAll(ChatMessageComponent.createWithType("chat.type.announcement", par1ICommandSender.getCommandSenderName(), var3));
        } else {
            throw new WrongUsageException("commands.say.usage");
        }
    }

    /**
     * Adds the strings available in this command to the given list of tab completion options.
     */
    public List addTabCompletionOptions(ICommandSender par1ICommandSender, String[] par2ArrayOfStr) {
        return par2ArrayOfStr.length >= 1 ? getListOfStringsMatchingLastWord(par2ArrayOfStr, MinecraftServer.getServer().getAllUsernames()) : null;
    }
}
