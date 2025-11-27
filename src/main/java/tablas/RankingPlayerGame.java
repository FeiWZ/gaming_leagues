package tablas;

public class RankingPlayerGame {
    private int playerId;
    private String playerName;
    private String gameName;
    private Integer ranking;
    private int wins;
    private int losses;

    public RankingPlayerGame(int playerId, String playerName, String gameName, Integer ranking, int wins, int losses) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.gameName = gameName;
        this.ranking = ranking;
        this.wins = wins;
        this.losses = losses;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String gameCode) {
        this.playerName = playerName;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameCode) {
        this.gameName = gameName;
    }

    public Integer getRanking() {
        return ranking;
    }

    public void setRanking(Integer ranking) {
        this.ranking = ranking;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public int getLosses() {
        return losses;
    }

    public void setLosses(int losses) {
        this.losses = losses;
    }

    @Override
    public String toString() {
        return "PlayersGamesRanking{" +
                "playerId=" + playerId +
                ", playerName='" + playerName + '\'' +
                ", gameName='" + gameName + '\'' +
                ", ranking=" + ranking +
                ", wins=" + wins +
                ", losses=" + losses +
                '}';
    }
}