package day01;

import com.mysql.cj.jdbc.MysqlDataSource;
import day01.Movie;
import day01.MoviesRepository;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

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
}