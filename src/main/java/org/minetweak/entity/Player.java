package org.minetweak.entity;

import net.minecraft.server.MinecraftServer;
import net.minecraft.src.*;
import org.minetweak.command.CommandSender;
import org.minetweak.permissions.PermissionNode;

import java.util.ArrayList;

/**
 * This class represents the player, and allows you to
 * call many API methods that are buried deep in
 * vanilla code. It extends CommandSender, which allows
 * the player to send commands. It also retrieves the
 * EntityPlayerMP and NetServerHandler for the player
 * when a instance is created for him/her. This gives
 * complete control over the methods inside of both
 * of those classes.
 */
public class Player implements CommandSender {

    private String playerDisplayName;
    private ArrayList<PermissionNode> playerPermissions = new ArrayList<PermissionNode>();

    private EntityPlayerMP entityPlayerMP;

    /**
     * Initialize a player
     * @param playerDisplayName Player's username
     */
    public Player(String playerDisplayName) {
        this.playerDisplayName = playerDisplayName;
        entityPlayerMP = MinecraftServer.getServer().getConfigurationManager().getPlayerEntity(playerDisplayName);
    }

    /**
     * Retrieve the player's "display name", which is the username of the player.
     * @return Player's display name
     */
    public String getDisplayName() {
        return playerDisplayName;
    }

    /**
     * Kick the player from the server
     */
    public void kickPlayer() {
        kickPlayer("You have been kicked from the server.");
    }

    /**
     * Kick the player from the server, including a kick reason
     * @param kickReason Reason why the player was kicked(will show up on their disconnected screen)
     */
    public void kickPlayer(String kickReason) {
        getNetServerHandler().kickPlayer(kickReason);
    }

    /**
     * Ban the player from the server
     */
    public void banPlayer() {
        banPlayer("You have been banned from the server.");
    }

    /**
     * Ban the player from the server, including a ban reason
     * @param banReason Reason why the player was banned(will show up on their disconnected screen)
     */
    public void banPlayer(String banReason) {
        BanEntry banEntry = new BanEntry(playerDisplayName);
        MinecraftServer.getServer().getConfigurationManager().getBannedPlayers().put(banEntry);
        getNetServerHandler().kickPlayer(banReason);
    }

    /**
     * Give permission to the player
     * @param permissionNode Node of the permission that you want to give the player
     * @return True if the permission was successfully added, and that the permission did not already exist
     */
    protected boolean givePermission(String permissionNode) {
        if (!playerPermissions.contains(permissionNode)) {
            playerPermissions.add(new PermissionNode(permissionNode));
            return true;
        }
        return false;
    }

    /**
     * Get the player permissions
     * @return Object array including the player's permissions
     */
    public Object[] getPlayerPermissions() {
        return playerPermissions.toArray();
    }

    /**
     * Check to see if this player has a specific permission node
     * @param permissionNode Target permission node
     * @return True if the player has the permission
     */
    public boolean hasPermission(String permissionNode) {
        for (PermissionNode p : playerPermissions) {
            if (p.getPermissionNode() == permissionNode) {
                return true;
            }
        }
        return false;
    }

    /**
     * Send the player a message
     * @param message Target message towards the player
     */
    @Override
    public void sendMessage(String message) {
        getPlayerMP().sendChatToPlayer(message);
    }

    /**
     * Get the player's name, basically is just a method wrap to getDisplayName()
     * @return The player's display name/username
     */
    @Override
    public String getName() {
        return getDisplayName();
    }

    /**
     * Can we kick the player? We sure can.
     * @return True, if the player can be kickable
     */
    @Override
    public boolean isKickable() {
        return true;
    }

    /**
     * Get the instance of EntityPlayerMP for this player
     * @return EntityPlayerMP corresponding class
     */
    public EntityPlayerMP getPlayerMP() {
        return entityPlayerMP;
    }

    /**
     * Get the NetServerHandler for this player
     * @return NetServerHandler corresponding class
     */
    public NetServerHandler getNetServerHandler() {
        return entityPlayerMP.playerNetServerHandler;
    }

    /**
     * Check to see if this player has operator powers
     * @return True if the player is an op
     */
    public boolean isOperator() {
        return MinecraftServer.getServer().getConfigurationManager().getOps().contains(playerDisplayName);
    }

    /**
     * Op the player
     */
    public void opPlayer() {
        MinecraftServer.getServer().getConfigurationManager().addOp(playerDisplayName);
    }

    /**
     * Deop the player
     */
    public void deopPlayer() {
        MinecraftServer.getServer().getConfigurationManager().removeOp(playerDisplayName);
    }

    /**
     * Kill the player
     */
    public void killPlayer() {
        getPlayerMP().attackEntityFrom(DamageSource.outOfWorld, 5000);
    }

    public boolean isPlayerSleeping() {
        return getNetServerHandler().playerEntity.isPlayerSleeping();
    }

    public void setGameMode(EnumGameType gameType) {
        getPlayerMP().setGameType(gameType);
    }

}
