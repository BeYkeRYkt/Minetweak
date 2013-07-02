package org.minetweak.load;

import org.minetweak.Minetweak;
import org.minetweak.command.*;
import org.minetweak.plugins.PluginLoader;
import org.minetweak.recipe.RecipeManager;

public class StartupThread extends Thread {
    @Override
    public void run() {
        Minetweak.ramCheck();
        Minetweak.registerCommand("help", new CommandHelp());
        Minetweak.registerCommand("stop", new CommandStop());
        Minetweak.registerCommand("kick", new CommandKick());
        Minetweak.registerCommand("op", new CommandOp());
        Minetweak.registerCommand("deop", new CommandDeop());
        Minetweak.registerCommand("kill", new CommandKill());
        Minetweak.registerCommand("players", new CommandListPlayers());
        Minetweak.registerListener(RecipeManager.getInstance());
        PluginLoader.initialize();
    }
}
