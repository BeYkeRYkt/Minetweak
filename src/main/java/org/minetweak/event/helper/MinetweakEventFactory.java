package org.minetweak.event.helper;

import net.minecraft.server.MinecraftServer;
import org.minetweak.Minetweak;
import org.minetweak.block.IBlock;
import org.minetweak.block.IBlockState;
import org.minetweak.entity.Player;
import org.minetweak.event.block.BlockIgniteEvent;
import org.minetweak.event.block.BlockPlaceEvent;
import org.minetweak.world.World;

/**
 * Is our call event helper
 */
public class MinetweakEventFactory {

    private static boolean canBuild(World world, Player player, int x, int z) {
        net.minecraft.src.WorldServer worldServer = world.getWorldServer();
        int spawnSize = MinecraftServer.getServer().getSpawnProtectionSize();

        if (world.getWorldServer().provider.dimensionId != 0) return true;
        if (spawnSize <= 0) return true;
        if (player.isOperator()) return true;

        net.minecraft.src.ChunkCoordinates chunkcoordinates = worldServer.getSpawnPoint();

        int distanceFromSpawn = Math.max(Math.abs(x - chunkcoordinates.posX), Math.abs(z - chunkcoordinates.posZ));
        return distanceFromSpawn >= spawnSize;
    }

    /**
     * TweakBlock place methods
     */
    public static BlockPlaceEvent callBlockPlaceEvent(net.minecraft.src.World world, net.minecraft.src.EntityPlayer who, IBlockState replacedBlockState, int clickedX, int clickedY, int clickedZ) {
        World craftWorld = world.getWorld();

        Player player = (who == null) ? null : (Player) Minetweak.getPlayerByName(who.getEntityName());

        IBlock blockClicked = craftWorld.getBlockAt(clickedX, clickedY, clickedZ);
        IBlock placedBlock = replacedBlockState.getBlock();

        boolean canBuild = canBuild(craftWorld, player, placedBlock.getX(), placedBlock.getZ());

        BlockPlaceEvent event = new BlockPlaceEvent(placedBlock, replacedBlockState, blockClicked, player.getItemInHand(), player, canBuild);
        Minetweak.getEventBus().post(event);

        return event;
    }

    public static BlockIgniteEvent callBlockIgniteEvent(net.minecraft.src.World world, int x, int y, int z, int igniterX, int igniterY, int igniterZ) {
        org.minetweak.world.World bukkitWorld = world.getWorld();
        IBlock igniter = bukkitWorld.getBlockAt(igniterX, igniterY, igniterZ);
        BlockIgniteEvent.IgniteCause cause;
        switch (igniter.getType()) {
            case LAVA:
            case STATIONARY_LAVA:
                cause = BlockIgniteEvent.IgniteCause.LAVA;
                break;
            case FIRE: // Fire or any other unknown block counts as SPREAD.
            default:
                cause = BlockIgniteEvent.IgniteCause.SPREAD;
        }

        BlockIgniteEvent event = new BlockIgniteEvent(bukkitWorld.getBlockAt(x, y, z), cause, igniter);
        Minetweak.getEventBus().post(event);
        return event;
    }

    public static BlockIgniteEvent callBlockIgniteEvent(net.minecraft.src.World world, int x, int y, int z, BlockIgniteEvent.IgniteCause cause, net.minecraft.src.EntityPlayer igniter) {
        BlockIgniteEvent event = new BlockIgniteEvent(world.getWorld().getBlockAt(x, y, z), cause, Minetweak.getPlayerByName(igniter.getEntityName()));
        Minetweak.getEventBus().post(event);
        return event;
    }
}


