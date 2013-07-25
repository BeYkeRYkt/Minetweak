package net.minecraft.command;

import net.minecraft.crash.exception.WrongUsageException;
import net.minecraft.entity.EntityPlayer;
import net.minecraft.entity.EntityPlayerMP;
import net.minecraft.player.PlayerNotFoundException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.utils.chat.ChatMessageComponent;
import net.minecraft.utils.enums.EnumChatFormatting;

import java.util.Arrays;
import java.util.List;

public class CommandServerMessage extends CommandBase {
    public List getCommandAliases() {
        return Arrays.asList(new String[]{"w", "msg"});
    }

    public String getCommandName() {
        return "tell";
    }

    /**
     * Return the required permission level for this command.
     */
    public int getRequiredPermissionLevel() {
        return 0;
    }

    public String getCommandUsage(ICommandSender par1ICommandSender) {
        return "commands.message.usage";
    }

    public void processCommand(ICommandSender par1ICommandSender, String[] par2ArrayOfStr) {
        if (par2ArrayOfStr.length < 2) {
            throw new WrongUsageException("commands.message.usage", new Object[0]);
        } else {
            EntityPlayerMP var3 = func_82359_c(par1ICommandSender, par2ArrayOfStr[0]);

            if (var3 == null) {
                throw new PlayerNotFoundException();
            } else if (var3 == par1ICommandSender) {
                throw new PlayerNotFoundException("commands.message.sameTarget", new Object[0]);
            } else {
                String var4 = func_82361_a(par1ICommandSender, par2ArrayOfStr, 1, !(par1ICommandSender instanceof EntityPlayer));
                var3.func_110122_a(ChatMessageComponent.func_111082_b("commands.message.display.incoming", new Object[]{par1ICommandSender.getCommandSenderName(), var4}).func_111059_a(EnumChatFormatting.GRAY).func_111063_b(Boolean.valueOf(true)));
                par1ICommandSender.func_110122_a(ChatMessageComponent.func_111082_b("commands.message.display.outgoing", new Object[]{var3.getCommandSenderName(), var4}).func_111059_a(EnumChatFormatting.GRAY).func_111063_b(Boolean.valueOf(true)));
            }
        }
    }

    /**
     * Adds the strings available in this command to the given list of tab completion options.
     */
    public List addTabCompletionOptions(ICommandSender par1ICommandSender, String[] par2ArrayOfStr) {
        return getListOfStringsMatchingLastWord(par2ArrayOfStr, MinecraftServer.getServer().getAllUsernames());
    }

    /**
     * Return whether the specified command parameter index is a username parameter.
     */
    public boolean isUsernameIndex(String[] par1ArrayOfStr, int par2) {
        return par2 == 0;
    }
}
