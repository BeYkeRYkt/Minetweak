package net.minecraft.server;

import net.minecraft.entity.EntityPlayerMP;
import net.minecraft.player.score.*;
import net.minecraft.server.network.packet.*;

import java.util.*;

public class ServerScoreboard extends Scoreboard {
    private final MinecraftServer minecraftServer;
    private final Set<ScoreObjective> objectives = new HashSet<ScoreObjective>();
    private ScoreboardSaveData field_96554_c;

    public ServerScoreboard(MinecraftServer par1MinecraftServer) {
        this.minecraftServer = par1MinecraftServer;
    }

    @Override
    public void func_96536_a(Score par1Score) {
        super.func_96536_a(par1Score);

        if (this.objectives.contains(par1Score.func_96645_d())) {
            this.minecraftServer.getConfigurationManager().sendPacketToAllPlayers(new Packet207SetScore(par1Score, 0));
        }

        this.func_96551_b();
    }

    @Override
    public void func_96516_a(String par1Str) {
        super.func_96516_a(par1Str);
        this.minecraftServer.getConfigurationManager().sendPacketToAllPlayers(new Packet207SetScore(par1Str));
        this.func_96551_b();
    }

    @Override
    public void func_96530_a(int par1, ScoreObjective par2ScoreObjective) {
        ScoreObjective var3 = this.func_96539_a(par1);
        super.func_96530_a(par1, par2ScoreObjective);

        if (var3 != par2ScoreObjective && var3 != null) {
            if (this.indexOf(var3) > 0) {
                this.minecraftServer.getConfigurationManager().sendPacketToAllPlayers(new Packet208SetDisplayObjective(par1, par2ScoreObjective));
            } else {
                this.func_96546_g(var3);
            }
        }

        if (par2ScoreObjective != null) {
            if (this.objectives.contains(par2ScoreObjective)) {
                this.minecraftServer.getConfigurationManager().sendPacketToAllPlayers(new Packet208SetDisplayObjective(par1, par2ScoreObjective));
            } else {
                this.addObjective(par2ScoreObjective);
            }
        }

        this.func_96551_b();
    }

    @Override
    public void func_96521_a(String par1Str, ScorePlayerTeam par2ScorePlayerTeam) {
        super.func_96521_a(par1Str, par2ScorePlayerTeam);
        this.minecraftServer.getConfigurationManager().sendPacketToAllPlayers(new Packet209SetPlayerTeam(par2ScorePlayerTeam, Collections.singletonList(par1Str), 3));
        this.func_96551_b();
    }

    /**
     * Removes the given username from the given ScorePlayerTeam. If the player is not on the team then an
     * IllegalStateException is thrown.
     */
    @Override
    public void removePlayerFromTeam(String par1Str, ScorePlayerTeam par2ScorePlayerTeam) {
        super.removePlayerFromTeam(par1Str, par2ScorePlayerTeam);
        this.minecraftServer.getConfigurationManager().sendPacketToAllPlayers(new Packet209SetPlayerTeam(par2ScorePlayerTeam, Collections.singletonList(par1Str), 4));
        this.func_96551_b();
    }

    @Override
    public void func_96522_a(ScoreObjective par1ScoreObjective) {
        super.func_96522_a(par1ScoreObjective);
        this.func_96551_b();
    }

    @Override
    public void func_96532_b(ScoreObjective par1ScoreObjective) {
        super.func_96532_b(par1ScoreObjective);

        if (this.objectives.contains(par1ScoreObjective)) {
            this.minecraftServer.getConfigurationManager().sendPacketToAllPlayers(new Packet206SetObjective(par1ScoreObjective, 2));
        }

        this.func_96551_b();
    }

    @Override
    public void func_96533_c(ScoreObjective par1ScoreObjective) {
        super.func_96533_c(par1ScoreObjective);

        if (this.objectives.contains(par1ScoreObjective)) {
            this.func_96546_g(par1ScoreObjective);
        }

        this.func_96551_b();
    }

    @Override
    public void func_96523_a(ScorePlayerTeam par1ScorePlayerTeam) {
        super.func_96523_a(par1ScorePlayerTeam);
        this.minecraftServer.getConfigurationManager().sendPacketToAllPlayers(new Packet209SetPlayerTeam(par1ScorePlayerTeam, 0));
        this.func_96551_b();
    }

    @Override
    public void func_96538_b(ScorePlayerTeam par1ScorePlayerTeam) {
        super.func_96538_b(par1ScorePlayerTeam);
        this.minecraftServer.getConfigurationManager().sendPacketToAllPlayers(new Packet209SetPlayerTeam(par1ScorePlayerTeam, 2));
        this.func_96551_b();
    }

    @Override
    public void func_96513_c(ScorePlayerTeam par1ScorePlayerTeam) {
        super.func_96513_c(par1ScorePlayerTeam);
        this.minecraftServer.getConfigurationManager().sendPacketToAllPlayers(new Packet209SetPlayerTeam(par1ScorePlayerTeam, 1));
        this.func_96551_b();
    }

    public void func_96547_a(ScoreboardSaveData par1ScoreboardSaveData) {
        this.field_96554_c = par1ScoreboardSaveData;
    }

    protected void func_96551_b() {
        if (this.field_96554_c != null) {
            this.field_96554_c.markDirty();
        }
    }

    public List<Packet> func_96550_d(ScoreObjective par1ScoreObjective) {
        ArrayList<Packet> var2 = new ArrayList<Packet>();
        var2.add(new Packet206SetObjective(par1ScoreObjective, 0));

        for (int var3 = 0; var3 < 3; ++var3) {
            if (this.func_96539_a(var3) == par1ScoreObjective) {
                var2.add(new Packet208SetDisplayObjective(var3, par1ScoreObjective));
            }
        }

        for (Object o : this.func_96534_i(par1ScoreObjective)) {
            Score var4 = (Score) o;
            var2.add(new Packet207SetScore(var4, 0));
        }

        return var2;
    }

    public void addObjective(ScoreObjective par1ScoreObjective) {
        List<Packet> var2 = this.func_96550_d(par1ScoreObjective);

        for (EntityPlayerMP var4 : this.minecraftServer.getConfigurationManager().playerEntityList) {

            for (Packet var6 : var2) {
                var4.playerNetServerHandler.sendPacket(var6);
            }
        }

        this.objectives.add(par1ScoreObjective);
    }

    public List<Packet> func_96548_f(ScoreObjective par1ScoreObjective) {
        ArrayList<Packet> var2 = new ArrayList<Packet>();
        var2.add(new Packet206SetObjective(par1ScoreObjective, 1));

        for (int var3 = 0; var3 < 3; ++var3) {
            if (this.func_96539_a(var3) == par1ScoreObjective) {
                var2.add(new Packet208SetDisplayObjective(var3, par1ScoreObjective));
            }
        }

        return var2;
    }

    public void func_96546_g(ScoreObjective par1ScoreObjective) {
        List<Packet> var2 = this.func_96548_f(par1ScoreObjective);

        for (EntityPlayerMP var4 : this.minecraftServer.getConfigurationManager().playerEntityList) {
            for (Packet var6 : var2) {
                var4.playerNetServerHandler.sendPacket(var6);
            }
        }

        this.objectives.remove(par1ScoreObjective);
    }

    public int indexOf(ScoreObjective par1ScoreObjective) {
        int var2 = 0;

        for (int var3 = 0; var3 < 3; ++var3) {
            if (this.func_96539_a(var3) == par1ScoreObjective) {
                ++var2;
            }
        }

        return var2;
    }
}
