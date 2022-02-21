package day01;

import com.mysql.cj.jdbc.MysqlDataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MoviesRatingServiceTest {

    RatingsRepository ratingsRepository;
    MoviesRepository moviesRepository;
    MoviesRatingService moviesRatingService;

    @BeforeEach
    void init() {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setUrl("jdbc:mysql://localhost:3306/movies-actors-test?useUnicode=true");
        dataSource.setUser("employees");
        dataSource.setPassword("employees");

        Flyway flyway = Flyway.configure().dataSource(dataSource).load();
        flyway.clean();
        flyway.migrate();

        moviesRepository = new MoviesRepository(dataSource);
        ratingsRepository = new RatingsRepository(dataSource);
        moviesRatingService = new MoviesRatingService(moviesRepository, ratingsRepository);
    }

    @Test
    void insertRatingsTest() {
        moviesRepository.saveMovie("Lord Of The Rings", LocalDate.of(2000, 12, 5));
        moviesRatingService.insertRatings("Lord Of The Rings", 1,5,4,2,3);
        List<Long> expected = Arrays.asList(1L, 2L, 3L, 4L, 5L);

        assertEquals(expected, ratingsRepository.getRatingsByMovieTitle("Lord Of The Rings"));
    }

    @Test
    void getAverageRatingByMovieTitleTest() {
        moviesRepository.saveMovie("Lord Of The Rings", LocalDate.of(2000, 12, 5));
        moviesRatingService.insertRatings("Lord Of The Rings", 1,5,4,2,3);

        assertEquals(3.0, moviesRatingService.getAverageRatingByMovieTitle("Lord Of The Rings"), 1);
    }
}