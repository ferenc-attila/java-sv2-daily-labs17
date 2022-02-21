package day01;

import com.mysql.cj.jdbc.MysqlDataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class MoviesRepositoryTest {

    MoviesRepository moviesRepository;

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
    }

    @Test
    void insertThanQueryTest() {
        moviesRepository.saveMovie("Lord Of The Rings", LocalDate.of(2000, 12, 5));
        List<Movie> movies = moviesRepository.findAllMovies();
        assertEquals(1, movies.size());
        assertEquals("Lord Of The Rings", movies.get(0).getTitle());
    }

    @Test
    void insertTwoTest() {
        moviesRepository.saveMovie("Lord Of The Rings", LocalDate.of(2000, 12, 5));
        moviesRepository.saveMovie("Kill Bill", LocalDate.of(2003, 2, 15));
        List<Movie> movies = moviesRepository.findAllMovies();
        assertEquals(2, movies.size());
        assertEquals("Kill Bill", movies.get(1).getTitle());
        assertEquals(LocalDate.of(2000, 12, 5), movies.get(0).getLocalDate());
    }

    @Test
    void findMovieByTitle() {
        moviesRepository.saveMovie("Lord Of The Rings", LocalDate.of(2000, 12, 5));
        moviesRepository.saveMovie("Kill Bill", LocalDate.of(2003, 2, 15));
        assertEquals("Kill Bill", moviesRepository.findMovieByTitle("Kill Bill").get().getTitle());
        assertEquals(LocalDate.parse("2000-12-05"), moviesRepository.findMovieByTitle("Lord Of The Rings").get().getLocalDate());
        assertEquals(Optional.empty(), moviesRepository.findMovieByTitle("Star Wars"));
    }

    @Test
    void findAllMoviesTest() {
        moviesRepository.saveMovie("Lord Of The Rings", LocalDate.of(2000, 12, 5));
        moviesRepository.saveMovie("Kill Bill", LocalDate.of(2003, 2, 15));
        List<Movie> movies = moviesRepository.findAllMovies();
        assertEquals(2, movies.size());
        assertEquals("Lord Of The Rings", movies.get(0).getTitle());
        assertEquals(LocalDate.parse("2003-02-15"), movies.get(1).getLocalDate());
    }

    @Test
    void invalidDataSourceTest() {
        MysqlDataSource invalidDataSource = new MysqlDataSource();
        invalidDataSource.setUrl("jdbc:mysql://localhost:3306/movies-actors-invalid-test?useUnicode=true");
        invalidDataSource.setUser("employees");
        invalidDataSource.setPassword("employees");

        MoviesRepository invalidRepository = new MoviesRepository(invalidDataSource);

        IllegalStateException ise = assertThrows(IllegalStateException.class, () -> invalidRepository.saveMovie("Meseautó", LocalDate.of(1934, 12,14)));
        assertEquals("Cannot connect!", ise.getMessage());
        assertEquals("Unknown database 'movies-actors-invalid-test'", ise.getCause().getMessage());
        assertEquals(SQLSyntaxErrorException.class, ise.getCause().getClass());
    }

    @Test
    void invalidDatabaseUsernameTest() {
        MysqlDataSource invalidDataSource = new MysqlDataSource();
        invalidDataSource.setUrl("jdbc:mysql://localhost:3306/movies-actors-test?useUnicode=true");
        invalidDataSource.setUser("emp");
        invalidDataSource.setPassword("employees");

        MoviesRepository invalidRepository = new MoviesRepository(invalidDataSource);

        IllegalStateException ise = assertThrows(IllegalStateException.class, () -> invalidRepository.findMovieByTitle("Meseautó"));
        assertEquals("Cannot connect!", ise.getMessage());
        assertTrue(ise.getCause().getMessage().startsWith("Access denied for user 'emp'"));
        assertEquals(SQLException.class, ise.getCause().getClass());
    }

    @Test
    void invalidDatabasePasswordTest() {
        MysqlDataSource invalidDataSource = new MysqlDataSource();
        invalidDataSource.setUrl("jdbc:mysql://localhost:3306/movies-actors-test?useUnicode=true");
        invalidDataSource.setUser("employees");
        invalidDataSource.setPassword("emp");

        MoviesRepository invalidRepository = new MoviesRepository(invalidDataSource);

        IllegalStateException ise = assertThrows(IllegalStateException.class, invalidRepository::findAllMovies);
        assertEquals("Cannot query!", ise.getMessage());
        assertTrue(ise.getCause().getMessage().startsWith("Access denied for user 'employees'"));
        assertEquals(SQLException.class, ise.getCause().getClass());
    }
}