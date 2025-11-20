package tablas;

import java.sql.Timestamp;

public class Match {
    private int matchId;
    private Timestamp matchDate;
    private String result;
    private String matchType;
    private int playerId1;
    private int playerId2;
    private String gameCode;

    public Match(int matchId, Timestamp matchDate, String result, String matchType, int playerId1, int playerId2, String gameCode) {
        this.matchId = matchId;
        this.matchDate = matchDate;
        this.result = result;
        this.matchType = matchType;
        this.playerId1 = playerId1;
        this.playerId2 = playerId2;
        this.gameCode = gameCode;
    }

    // Getters y setters
    public int getMatchId() { return matchId; }
    public void setMatchId(int matchId) { this.matchId = matchId; }

    public Timestamp getMatchDate() { return matchDate; }
    public void setMatchDate(Timestamp matchDate) { this.matchDate = matchDate; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public String getMatchType() { return matchType; }
    public void setMatchType(String matchType) { this.matchType = matchType; }

    public int getPlayerId1() { return playerId1; }
    public void setPlayerId1(int playerId1) { this.playerId1 = playerId1; }

    public int getPlayerId2() { return playerId2; }
    public void setPlayerId2(int playerId2) { this.playerId2 = playerId2; }

    public String getGameCode() { return gameCode; }
    public void setGameCode(String gameCode) { this.gameCode = gameCode; }

    @Override
    public String toString() {
        return "Matches{" +
                "matchId=" + matchId +
                ", result='" + result + '\'' +
                '}';
    }
}