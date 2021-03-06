package net.minecraft.server.network.packet;

import org.minetweak.network.INetworkHandler;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class Packet204ClientInfo extends Packet {
    private String language;
    private int renderDistance;
    private int chatVisisble;
    private boolean chatColours;
    private int gameDifficulty;
    private boolean showCape;

    /**
     * Abstract. Reads the raw packet data from the data stream.
     */
    @Override
    public void readPacketData(DataInput par1DataInput) throws IOException {
        this.language = readString(par1DataInput, 7);
        this.renderDistance = par1DataInput.readByte();
        byte var2 = par1DataInput.readByte();
        this.chatVisisble = var2 & 7;
        this.chatColours = (var2 & 8) == 8;
        this.gameDifficulty = par1DataInput.readByte();
        this.showCape = par1DataInput.readBoolean();
    }

    /**
     * Abstract. Writes the raw packet data to the data stream.
     */
    @Override
    public void writePacketData(DataOutput par1DataOutput) throws IOException {
        writeString(this.language, par1DataOutput);
        par1DataOutput.writeByte(this.renderDistance);
        par1DataOutput.writeByte(this.chatVisisble | (this.chatColours ? 1 : 0) << 3);
        par1DataOutput.writeByte(this.gameDifficulty);
        par1DataOutput.writeBoolean(this.showCape);
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    @Override
    public void processPacket(INetworkHandler par1NetHandler) {
        par1NetHandler.handleClientInfo(this);
    }

    /**
     * Abstract. Return the size of the packet (not counting the header).
     */
    @Override
    public int getPacketSize() {
        return 7;
    }

    public String getLanguage() {
        return this.language;
    }

    public int getRenderDistance() {
        return this.renderDistance;
    }

    public int getChatVisibility() {
        return this.chatVisisble;
    }

    public boolean getChatColours() {
        return this.chatColours;
    }

    public int getDifficulty() {
        return this.gameDifficulty;
    }

    public boolean getShowCape() {
        return this.showCape;
    }

    /**
     * only false for the abstract Packet class, all real packets return true
     */
    @Override
    public boolean isRealPacket() {
        return true;
    }

    /**
     * eg return packet30entity.entityId == entityId; WARNING : will throw if you compare a packet to a different packet
     * class
     */
    @Override
    public boolean containsSameEntityIDAs(Packet par1Packet) {
        return true;
    }
}
