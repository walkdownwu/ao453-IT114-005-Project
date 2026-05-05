package rps.common;

import java.util.List;

public class RoomPayload extends Payload {
    private static final long serialVersionUID = 1L;
    private boolean isPrivate;
    private List<RoomInfo> rooms;

    public boolean isPrivate() { return isPrivate; }
    public void setPrivate(boolean isPrivate) { this.isPrivate = isPrivate; }

    public List<RoomInfo> getRooms() { return rooms; }
    public void setRooms(List<RoomInfo> rooms) { this.rooms = rooms; }

    public static class RoomInfo implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        private String name;
        private int playerCount;
        private boolean isPrivate;
        private Phase phase;

        public RoomInfo(String name, int playerCount, boolean isPrivate, Phase phase) {
            this.name = name;
            this.playerCount = playerCount;
            this.isPrivate = isPrivate;
            this.phase = phase;
        }

        public String getName() { return name; }
        public int getPlayerCount() { return playerCount; }
        public boolean isPrivate() { return isPrivate; }
        public Phase getPhase() { return phase; }
    }
}
