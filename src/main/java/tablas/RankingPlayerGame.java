package tablas;

public class RankingPlayerGame {
    private int playerId;
    private String gameCode;
    private Integer ranking;
    private int wins;
    private int losses;

    public RankingPlayerGame(int playerId, String gameCode, Integer ranking, int wins, int losses) {
        this.playerId = playerId;
        this.gameCode = gameCode;
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

    public String getGameCode() {
        return gameCode;
    }

    public void setGameCode(String gameCode) {
        this.gameCode = gameCode;
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
                ", gameCode='" + gameCode + '\'' +
                ", ranking=" + ranking +
                ", wins=" + wins +
                ", losses=" + losses +
                '}';
    }
}