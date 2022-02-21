package day01;

import java.time.LocalDate;

public class Movie {

    private Long id;
    private String title;
    private LocalDate localDate;
    private double averageRating;

    public Movie(Long id, String title, LocalDate localDate, double averageRating) {
        this.id = id;
        this.title = title;
        this.localDate = localDate;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public LocalDate getLocalDate() {
        return localDate;
    }

    public double getAverageRating() {
        return averageRating;
    }
}
