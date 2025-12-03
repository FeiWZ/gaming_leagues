
package tablas;

import java.sql.Timestamp;

public class Match {
    private int matchId;
    private Timestamp matchDate;
    private String result;
    private String matchType;
    private String player1;
    private String player2;
    private String gameCode;

    public Match(int matchId, Timestamp matchDate, String result, String matchType, String player1, String player2, String gameCode) {
        this.matchId = matchId;
        this.matchDate = matchDate;
        this.result = result;
        this.matchType = matchType;
        this.player1 = player1;
        this.player2 = player2;
        this.gameCode = gameCode;
    }

    public int getMatchId() { return matchId; }
    public void setMatchId(int matchId) { this.matchId = matchId; }

    public Timestamp getMatchDate() { return matchDate; }
    public void setMatchDate(Timestamp matchDate) { this.matchDate = matchDate; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public String getMatchType() { return matchType; }
    public void setMatchType(String matchType) { this.matchType = matchType; }

    public String getPlayer1() { return player1; }
    public void setPlayer1(String playerId1) { this.player1 = player1; }

    public String getPlayer2() { return player2; }
    public void setPlayer2(String player2) { this.player2 = player2; }

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
