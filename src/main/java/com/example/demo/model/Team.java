package com.example.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "teams")
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String teamId; // Format like T001, T002, etc.

    @Column(nullable = false)
    private String name; // Official team name

    private String captainName;
    private String contact;

    @Column(nullable = false)
    private String captainUsername; // Links to User

    @Column(name = "id_cup")
    private Long tournamentId; // Tournament association

    public Team() {}

    public Team(String teamId, String name, String captainName, String contact, String captainUsername, Long tournamentId) {
        this.teamId = teamId;
        this.name = name;
        this.captainName = captainName;
        this.contact = contact;
        this.captainUsername = captainUsername;
        this.tournamentId = tournamentId;
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

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCaptainName() {
        return captainName;
    }

    public void setCaptainName(String captainName) {
        this.captainName = captainName;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getCaptainUsername() {
        return captainUsername;
    }

    public void setCaptainUsername(String captainUsername) {
        this.captainUsername = captainUsername;
    }
}
