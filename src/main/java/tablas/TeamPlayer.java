package tablas;

import java.util.Date;

public class TeamPlayer {
    private String teamName;
    private String playerName;
    private Date dateFrom;
    private Date dateTo;
    private String role;

    public TeamPlayer(String teamName, String playerName, Date dateFrom, Date dateTo, String role) {
        this.teamName = teamName;
        this.playerName = playerName;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.role = role;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) { this.playerName = playerName;
    }

    public Date getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(Date dateFrom) {
        this.dateFrom = dateFrom;
    }

    public Date getDateTo() {
        return dateTo;
    }

    public void setDateTo(Date dateTo) {
        this.dateTo = dateTo;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "TeamPlayers{" +
                "teamName=" + teamName +
                ", playerName=" + playerName +
                ", role='" + role + '\'' +
                '}';
    }
}