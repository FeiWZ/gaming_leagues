package org.example.tablas;

import java.util.Date;

public class TeamPlayer {
    private int teamId;
    private int playerId;
    private Date dateFrom;
    private Date dateTo;
    private String role;

    public TeamPlayer(int teamId, int playerId, Date dateFrom, Date dateTo, String role) {
        this.teamId = teamId;
        this.playerId = playerId;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.role = role;
    }

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
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
                "teamId=" + teamId +
                ", playerId=" + playerId +
                ", role='" + role + '\'' +
                '}';
    }
}