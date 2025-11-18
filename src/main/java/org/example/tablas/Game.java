package org.example.tablas;

public class Game {
    private String gameCode;
    private String gameName;
    private String gameDescription;
    private String gameGenres;

    public Game(String gameCode, String gameName, String gameDescription, String gameGenres) {
        this.gameCode = gameCode;
        this.gameName = gameName;
        this.gameDescription = gameDescription;
        this.gameGenres = gameGenres;
    }

    // Getters y setters
    public String getGameCode() { return gameCode; }
    public void setGameCode(String gameCode) { this.gameCode = gameCode; }

    public String getGameName() { return gameName; }
    public void setGameName(String gameName) { this.gameName = gameName; }

    public String getGameDescription() { return gameDescription; }
    public void setGameDescription(String gameDescription) { this.gameDescription = gameDescription; }

    public String getGameGenres() { return gameGenres; }
    public void setGameGenres(String gameGenres) { this.gameGenres = gameGenres; }

    @Override
    public String toString() {
        return "Game{" +
                "gameCode='" + gameCode + '\'' +
                ", gameName='" + gameName + '\'' +
                '}';
    }
}