package org.minetweak.command;

public abstract class CommandExecutor {

    public abstract void executeCommand(CommandSender sender, String overallCommand, String[] args);

}
