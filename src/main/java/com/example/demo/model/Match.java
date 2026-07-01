package com.example.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "tr_match")
public class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long tournamentId;

    private String team1Id;
    private String team2Id;

    private String team1Name;
    private String team2Name;

    private Integer team1Score;
    private Integer team2Score;

    private String winnerTeamId;

    @Column(nullable = false)
    private Integer round; // 1 = Round of 8, 2 = Semifinals, 3 = Finals, etc.

    @Column(nullable = false)
    private Integer matchIndex; // Order index within the round

    private String matchDate; // e.g. "2026-06-20" or "Saturday"
    private String matchTime; // e.g. "19:00"

    @Column(name = "jadwal_tanding")
    private String jadwalTanding;

    @Column(nullable = false)
    private String status; // "SCHEDULED", "FINISHED", "BYE"

    public Match() {}

    public Match(Long tournamentId, String team1Id, String team2Id, String team1Name, String team2Name, Integer round, Integer matchIndex, String status) {
        this.tournamentId = tournamentId;
        this.team1Id = team1Id;
        this.team2Id = team2Id;
        this.team1Name = team1Name;
        this.team2Name = team2Name;
        this.round = round;
        this.matchIndex = matchIndex;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(Long tournamentId) {
        this.tournamentId = tournamentId;
    }

    public String getTeam1Id() {
        return team1Id;
    }

    public void setTeam1Id(String team1Id) {
        this.team1Id = team1Id;
    }

    public String getTeam2Id() {
        return team2Id;
    }

    public void setTeam2Id(String team2Id) {
        this.team2Id = team2Id;
    }

    public String getTeam1Name() {
        return team1Name;
    }

    public void setTeam1Name(String team1Name) {
        this.team1Name = team1Name;
    }

    public String getTeam2Name() {
        return team2Name;
    }

    public void setTeam2Name(String team2Name) {
        this.team2Name = team2Name;
    }

    public Integer getTeam1Score() {
        return team1Score;
    }

    public void setTeam1Score(Integer team1Score) {
        this.team1Score = team1Score;
    }

    public Integer getTeam2Score() {
        return team2Score;
    }

    public void setTeam2Score(Integer team2Score) {
        this.team2Score = team2Score;
    }

    public String getWinnerTeamId() {
        return winnerTeamId;
    }

    public void setWinnerTeamId(String winnerTeamId) {
        this.winnerTeamId = winnerTeamId;
    }

    public Integer getRound() {
        return round;
    }

    public void setRound(Integer round) {
        this.round = round;
    }

    public Integer getMatchIndex() {
        return matchIndex;
    }

    public void setMatchIndex(Integer matchIndex) {
        this.matchIndex = matchIndex;
    }

    public String getMatchDate() {
        return matchDate;
    }

    public void setMatchDate(String matchDate) {
        this.matchDate = matchDate;
    }

    public String getMatchTime() {
        return matchTime;
    }

    public void setMatchTime(String matchTime) {
        this.matchTime = matchTime;
    }

    public String getJadwalTanding() {
        return jadwalTanding;
    }

    public void setJadwalTanding(String jadwalTanding) {
        this.jadwalTanding = jadwalTanding;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
