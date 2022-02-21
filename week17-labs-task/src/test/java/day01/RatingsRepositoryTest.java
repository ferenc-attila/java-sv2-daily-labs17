package day01;

import com.mysql.cj.jdbc.MysqlDataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RatingsRepositoryTest {

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
    }

    @Test
    void insertRatingsTest() {
        moviesRepository.saveMovie("Lord Of The Rings", LocalDate.of(2000, 12, 5));
        ratingsRepository.insertRatings(1, Arrays.asList(1,4,3,5,2));
        List<Long> expected = Arrays.asList(1L, 2L, 3L, 4L, 5L);

        assertEquals(expected, ratingsRepository.getRatingsByMovieTitle("Lord Of The Rings"));
    }

    @Test
    void insertInvalidRatingsTest() {
        moviesRepository.saveMovie("Lord Of The Rings", LocalDate.of(2000, 12, 5));

        IllegalArgumentException iae = assertThrows(IllegalArgumentException.class, () -> ratingsRepository.insertRatings(1, Arrays.asList(1,4,3,6,2)));
        assertEquals("Invalid rating value in the list: 6", iae.getMessage());
        assertTrue(ratingsRepository.getRatingsByMovieTitle("Lord Of The Rings").isEmpty());
    }

    @Test
    void invalidDataSourceTest() {
        MysqlDataSource invalidDataSource = new MysqlDataSource();
        invalidDataSource.setUrl("jdbc:mysql://localhost:3306/movies-actors-invalid-test?useUnicode=true");
        invalidDataSource.setUser("employees");
        invalidDataSource.setPassword("employees");

        RatingsRepository invalidRepository = new RatingsRepository(invalidDataSource);

        IllegalStateException ise = assertThrows(IllegalStateException.class, () -> invalidRepository.insertRatings(1, Arrays.asList(1,2,3)));
        assertEquals("Cannot insert!", ise.getMessage());
        assertEquals("Unknown database 'movies-actors-invalid-test'", ise.getCause().getMessage());
        assertEquals(SQLSyntaxErrorException.class, ise.getCause().getClass());
    }

    @Test
    void invalidDatabaseUsernameTest() {
        MysqlDataSource invalidDataSource = new MysqlDataSource();
        invalidDataSource.setUrl("jdbc:mysql://localhost:3306/movies-actors-test?useUnicode=true");
        invalidDataSource.setUser("emp");
        invalidDataSource.setPassword("employees");

        RatingsRepository invalidRepository = new RatingsRepository(invalidDataSource);

        IllegalStateException ise = assertThrows(IllegalStateException.class, () -> invalidRepository.insertRatings(1, Arrays.asList(1,2,3)));
        assertEquals("Cannot insert!", ise.getMessage());
        assertTrue(ise.getCause().getMessage().startsWith("Access denied for user 'emp'"));
        assertEquals(SQLException.class, ise.getCause().getClass());
    }

    @Test
    void invalidDatabasePasswordTest() {
        MysqlDataSource invalidDataSource = new MysqlDataSource();
        invalidDataSource.setUrl("jdbc:mysql://localhost:3306/movies-actors-test?useUnicode=true");
        invalidDataSource.setUser("employees");
        invalidDataSource.setPassword("emp");

        RatingsRepository invalidRepository = new RatingsRepository(invalidDataSource);

        IllegalStateException ise = assertThrows(IllegalStateException.class, () -> invalidRepository.insertRatings(1, Arrays.asList(1,2,3)));
        assertEquals("Cannot insert!", ise.getMessage());
        assertTrue(ise.getCause().getMessage().startsWith("Access denied for user 'employees'"));
        assertEquals(SQLException.class, ise.getCause().getClass());
    }
}