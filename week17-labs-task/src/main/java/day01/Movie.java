package day01;

import java.time.LocalDate;
import java.util.Objects;

public class Movie {

    private Long id;
    private String title;
    private LocalDate localDate;
    private double averageRating;

    public Movie(Long id, String title, LocalDate localDate, double averageRating) {
        this.id = id;
        this.title = title;
        this.localDate = localDate;
        this.averageRating = averageRating;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Movie movie = (Movie) o;
        return title.equals(movie.title) && localDate.equals(movie.localDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, localDate);
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
