package tablas;

import java.util.Date;

public class League {
    private int leagueId;
    private String leagueName;
    private String leagueDetails;
    private String gameName;
    private int prizePool;
    private String rules;
    private Date startedDate;
    private Date endDate;

    public League(int leagueId, String leagueName, String leagueDetails, String gameName, int prizePool, String rules, Date startedDate, Date endDate) {
        this.leagueId = leagueId;
        this.leagueName = leagueName;
        this.leagueDetails = leagueDetails;
        this.gameName = gameName;
        this.prizePool = prizePool;
        this.rules = rules;
        this.startedDate = startedDate;
        this.endDate = endDate;
    }

    // Getters y setters
    public int getLeagueId() { return leagueId; }
    public void setLeagueId(int leagueId) { this.leagueId = leagueId; }

    public String getLeagueName() { return leagueName; }
    public void setLeagueName(String leagueName) { this.leagueName = leagueName; }

    public String getLeagueDetails() { return leagueDetails; }
    public void setLeagueDetails(String leagueDetails) { this.leagueDetails = leagueDetails; }

    public String getGameName() { return gameName; }
    public void setGameName(String gameName) { this.gameName = gameName; }

    public int getPrizePool() { return prizePool; }
    public void setPrizePool(int prizePool) { this.prizePool = prizePool; }

    public String getRules() { return rules; }
    public void setRules(String rules) { this.rules = rules; }

    public Date getStartedDate() { return startedDate; }
    public void setStartedDate(Date startedDate) { this.startedDate = startedDate; }

    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }

    @Override
    public String toString() {
        return "League{" +
                "leagueId=" + leagueId +
                ", leagueName='" + leagueName + '\'' +
                ", gameName='" + gameName + '\'' +
                '}';
    }
}