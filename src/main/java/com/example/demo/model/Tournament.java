package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "tournaments")
public class Tournament {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // Competition name

    @Column(nullable = false)
    private String game; // Game name, e.g., Valorant Pro Series

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private String status; // "BERJALAN" or "SELESAI"

    private String championTeamName;

    public Tournament() {}

    public Tournament(String name, String game, LocalDate startDate, String status) {
        this.name = name;
        this.game = game;
        this.startDate = startDate;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGame() {
        return game;
    }

    public void setGame(String game) {
        this.game = game;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getChampionTeamName() {
        return championTeamName;
    }

    public void setChampionTeamName(String championTeamName) {
        this.championTeamName = championTeamName;
    }
}
