package tablas;

import java.util.Date;

public class Team {
    private int teamId;
    private String teamName;
    private Date dateCreated;
    private Date dateDisbanded;
    private int idCoach;
    private String createdByPlayer;

    public Team(int teamId, String teamName, Date dateCreated, Date dateDisbanded, int idCoach, String createdByPlayer) {
        this.teamId = teamId;
        this.teamName = teamName;
        this.dateCreated = dateCreated;
        this.dateDisbanded = dateDisbanded;
        this.idCoach = idCoach;
        this.createdByPlayer = createdByPlayer;
    }

    // Getters y setters
    public int getTeamId() { return teamId; }
    public void setTeamId(int teamId) { this.teamId = teamId; }

    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }

    public Date getDateCreated() { return dateCreated; }
    public void setDateCreated(Date dateCreated) { this.dateCreated = dateCreated; }

    public Date getDateDisbanded() { return dateDisbanded; }
    public void setDateDisbanded(Date dateDisbanded) { this.dateDisbanded = dateDisbanded; }

    public int getIdCoach() { return idCoach; }
    public void setIdCoach(int idCoach) { this.idCoach = idCoach; }

    public String getCreatedByPlayer() { return createdByPlayer; }
    public void setCreatedByPlayer(String createdByPlayer) { this.createdByPlayer = createdByPlayer; }

    @Override
    public String toString() {
        return "Team{" +
                "teamId=" + teamId +
                ", teamName='" + teamName + '\'' +
                ", dateCreated=" + dateCreated +
                ", dateDisbanded=" + dateDisbanded +
                ", idCoach=" + idCoach +
                ", createdByPlayer=" + createdByPlayer +
                '}';
    }
}