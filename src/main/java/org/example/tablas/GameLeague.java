package org.example.tablas;

public class GameLeague {
    private int leagueId;
    private String gameCode;

    public GameLeague (int leagueId, String gameCode) {
        this.leagueId = leagueId;
        this.gameCode = gameCode;
    }

    public int getLeagueId() {
        return leagueId;
    }

    public void setLeagueId(int leagueId) {
        this.leagueId = leagueId;
    }

    public String getGameCode() {
        return gameCode;
    }

    public void setGameCode(String gameCode) {
        this.gameCode = gameCode;
    }

    @Override
    public String toString() {
        return "LeaguesGames{" +
                "leagueId=" + leagueId +
                ", gameCode='" + gameCode + '\'' +
                '}';
    }
}