package org.minetweak.command

import net.minecraft.server.MinecraftServer

class CommandDebug extends CommandExecutor {

    static def mb = 1024 * 1024

    @Override
    void executeCommand(CommandSender sender, String overallCommand, String[] args) {
        if (!sender.hasPermission("minetweak.command.debug")) { noPermission(sender, "debug the server") ; return}
        sender.sendMessage("TPS: ${CommandTps.getTPS(MinecraftServer.getServer().tickTimeArray)}", "Total Memory: ${Runtime.getRuntime().totalMemory() / mb}mb", "Free Memory: ${Runtime.getRuntime().freeMemory() / mb}mb", "Max Memory: ${Runtime.getRuntime().maxMemory() / mb}mb")
    }
}
