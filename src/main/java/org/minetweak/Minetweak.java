package org.minetweak;

import com.google.common.eventbus.EventBus;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.Block;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import org.minetweak.command.*;
import org.minetweak.entity.Player;
import org.minetweak.plugins.PluginLoader;
import org.minetweak.recipe.RecipeManager;

import java.util.HashMap;

/**
 * Main entrypoint for Minetweak, basically defines fields
 * for use throughout the internal server, and even the API
 * itself. It gives most of the basic methods that you will
 * need to create a plugin, like registering a command,
 * or an event.
 */
public class Minetweak {

    /**
     * This is the Minecraft version we are currently running on.
     */
    private static final String minecraftVersion = "1.5.2";
    /**
     * This is the Minetweak version we are currently running on.
     */
    private static final String serverVersion = "0.0.3";

    /**
     * This boolean will return true if the server has finished loading, will
     * be replaced by a ServerFinishedStartupEvent possibly.
     */
    private static boolean isServerDoneLoading = false;

    /**
     * This boolean will return true if the server is currently under lockdown,
     * and non-op players are kicked out.
     */
    private static boolean lockdownEnabled = false;

    /**
     * This is where we store the player's information at, inside of the Player
     * class contains their NetServerHandler and EntityPlayerMP.
     */
    private static HashMap<String, Player> players = new HashMap<String, Player>();

    /**
     * This is where we store the command executors for our base commands, and even
     * for other plugins. It will need some work later on for duplicates, etc.
     */
    private static HashMap<String, CommandExecutor> commandExecutors = new HashMap<String, CommandExecutor>();

    /**
     * The Guava EventBus that allows us to create our own events. This is basically
     * the main part of our event system, allowing us to register/unregister listeners
     * and tell the EventBus that an event needs to be fired.
     */
    private static EventBus eventBus = new EventBus();

    public static void main(String[] args) {
        System.out.println("Success is very tasty.");
        System.out.println("Minetweak v" + getServerVersion() + " using Minecraft v" + getMinecraftVersion());

        registerCommand("help", new CommandHelp());
        registerCommand("stop", new CommandStop());
        registerCommand("kick", new CommandKick());
        registerCommand("op", new CommandOp());
        registerCommand("deop", new CommandDeop());
        registerCommand("kill", new CommandKill());
        registerCommand("players", new CommandListPlayers());
        registerListener(RecipeManager.getInstance());

        PluginLoader.initialize();

        ramCheck();

        MinecraftServer.main(args);
    }

    private static void ramCheck() {
        if (Runtime.getRuntime().maxMemory() / 1024L / 1024L < 512L) {
            System.out.println("To start the server with more ram, launch it as \"java -Xmx1024M -Xms1024M -jar minecraft_server.jar\"");
        } else {
            System.out.println("Good, you are using plenty of RAM to run Minetweak, I believe it is thanking you already!");
        }
    }

    /**
     * What Minecraft version are we running?
     * @return Minecraft Version
     */
    public static String getMinecraftVersion() {
        return minecraftVersion;
    }

    /**
     * What Minetweak server version are we running?
     * @return Minetweak Server Version
     */
    public static String getServerVersion() {
        return serverVersion;
    }

    /**
     * Is the server done loading?
     * @return Server done loading
     */
    public static boolean isServerDoneLoading() {
        return isServerDoneLoading;
    }

    /**
     * Tell Minetweak that the Minecraft server itself has finished loading
     */
    public static void setServerDoneLoading() {
        isServerDoneLoading = true;
    }

    /**
     * Check to see if the server is in "lockdown" mode
     * @return Lockdown status
     */
    public static boolean isServerLockedDown() {
        return lockdownEnabled;
    }

    /**
     * Register a player into Minetweak
     * @param playerUsername Player name we are registering
     */
    public static boolean registerPlayer(String playerUsername) {
        Player targetPlayerInstance = new Player(playerUsername);
        if (isServerLockedDown()) {
            targetPlayerInstance.kickPlayer("This server is currently under lockdown.");
            return false;
        } else {
            if (players.containsKey(playerUsername)) {
                if (isPlayerOnline(playerUsername)) {
                    targetPlayerInstance.kickPlayer("There was a problem connecting you to the server");
                    return false;
                }
            } else {
                players.put(playerUsername, targetPlayerInstance);
            }
            targetPlayerInstance.sendMessage("You were registered within Minetweak. Please check within the console for errors.");
            if (targetPlayerInstance.isOperator()) targetPlayerInstance.sendMessage("You are an op.");
            return true;
        }
    }

    /**
     * Take the target player and unregister them
     * @param playerUsername Player name we marking as offline
     */
    public static void unregisterPlayer(String playerUsername) {
        players.remove(playerUsername);
    }

    /**
     * Get a specific player by their username, either online or offline, if they are online
     * @param playerName Player's username
     * @return Instance of player
     */
    public static Player getPlayerByName(String playerName) {
       return players.get(playerName);
    }

    /**
     * Check to ensure that a command exists.
     * @param command Target command label
     * @return True if the command does exist
     */
    public static boolean doesCommandExist(String command) {
        return commandExecutors.containsKey(command);
    }

    /**
     * Get the class to the corresponding command label specified. Return null if no command exists with that label.
     * @param commandLabel Command label to get
     * @return CommandExecutor for specified command label
     */
    public static CommandExecutor getCommandByName(String commandLabel) {
        if (commandExecutors.containsKey(commandLabel)) return commandExecutors.get(commandLabel);
        else return null;
    }

    /**
     * Register a command within Minetweak
     * @param commandLabel Label that the command uses
     * @param commandExecutor CommandExecutor class that we will use to execute the command
     */
    public static void registerCommand(String commandLabel, CommandExecutor commandExecutor) {
        commandExecutors.put(commandLabel, commandExecutor);
    }

    /**
     * Get the EventBus that handles all events that go on in the server
     * @return Server EventBus
     */
    public static EventBus getEventBus() {
        return eventBus;
    }

    /**
     .* Registers a Guava Event Listener
     * @param object the instance of the listener
     */
    public static void registerListener(Object object) {
        eventBus.register(object);
    }

    /**
     *
     * @param playerUsername The players username
     * @return if the player is online
     */
    public static boolean isPlayerOnline(String playerUsername) {
        return players.containsKey(playerUsername);
    }

    /**
     * Log Info to Console
     * @param line line to log
     */
    public static void info(String line) {
        MinecraftServer.getServer().logInfo(line);
    }
}