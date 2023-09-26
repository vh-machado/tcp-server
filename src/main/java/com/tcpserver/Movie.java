package com.tcpserver;

public class Movie {
    private String title;
    private String posterPath;
    private String releaseDate;
    // Outras propriedades do filme, se necessário

    // Construtor
    public Movie(String title, String posterPath, String releaseDate) {
        this.title = title;
        this.posterPath = posterPath;
        this.releaseDate = releaseDate;
        // Inicialize outras propriedades, se necessário
    }

    // Getters e Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    // Outros getters e
}