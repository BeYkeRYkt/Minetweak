package org.minetweak.event.player;

public class PlayerDeathEvent {

    private String playerUsername;
    private boolean isHardcoreMode;

    public PlayerDeathEvent(String playerUsername) {
        this.playerUsername = playerUsername;
        isHardcoreMode = false;
    }

    public PlayerDeathEvent(String playerUsername, boolean isHardcore) {
        this.playerUsername = playerUsername;
        this.isHardcoreMode = isHardcore;
    }

    public String getPlayerUsername() {
        return playerUsername;
    }

    public boolean isHardcoreMode() {
        return isHardcoreMode;
    }

}
