package rps.client;

import java.util.List;
import rps.common.*;

public interface IClientEvents {
    void onClientId(long id);
    void onClientConnect(long clientId, String clientName);
    void onClientDisconnect(long clientId, String clientName);
    void onMessageReceived(long clientId, String message);
    void onSyncClient(long clientId, String clientName);
    void onResetUserList();
    void onPoints(long clientId, String clientName, int points);
    void onPhase(String phase);
    void onRoundTimer(int seconds);
    void onRoomList(List<RoomPayload.RoomInfo> rooms);
    void onLeaderboard(List<LeaderboardPayload.LeaderboardEntry> entries);
}
