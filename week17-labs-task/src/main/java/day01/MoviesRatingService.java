package day01;

import java.util.Arrays;
import java.util.Optional;

public class MoviesRatingService {

    private MoviesRepository moviesRepository;
    private RatingsRepository ratingsRepository;

    public MoviesRatingService(MoviesRepository moviesRepository, RatingsRepository ratingsRepository) {
        this.moviesRepository = moviesRepository;
        this.ratingsRepository = ratingsRepository;
    }

    public void insertRatings(String title, Integer... ratings) {
        Optional<Movie> actual = moviesRepository.findMovieByTitle(title);
        if (actual.isPresent()) {
            ratingsRepository.insertRatings(actual.get().getId(), Arrays.asList(ratings));
        } else {
            throw new IllegalArgumentException("Cannot find movie: " + title);
        }
    }

    public double getAverageRatingByMovieTitle(String movieTitle) {
        return ratingsRepository.getRatingsByMovieTitle(movieTitle).stream()
                .mapToLong(Long::longValue)
                .average().orElse(0.0);
    }
}
