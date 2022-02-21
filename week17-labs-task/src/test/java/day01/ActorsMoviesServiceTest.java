package day01;

import com.mysql.cj.jdbc.MysqlDataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ActorsMoviesServiceTest {

    ActorsMoviesService actorsMoviesService;

    @BeforeEach
    void init() {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setUrl("jdbc:mysql://localhost:3306/movies-actors-test?useUnicode=true");
        dataSource.setUser("employees");
        dataSource.setPassword("employees");

        Flyway flyway = Flyway.configure().dataSource(dataSource).load();
        flyway.clean();
        flyway.migrate();

        ActorsRepository actorsRepository = new ActorsRepository(dataSource);
        MoviesRepository moviesRepository = new MoviesRepository(dataSource);
        ActorsMoviesRepository actorsMoviesRepository = new ActorsMoviesRepository(dataSource);

        actorsMoviesService = new ActorsMoviesService(actorsRepository, moviesRepository, actorsMoviesRepository);
    }

    @Test
    void insertMovieWithActorsTest() {
        actorsMoviesService.insertMovieWithActors("Hyppolit a lakáj", LocalDate.of(1931, 11,27), Arrays.asList("Csortos Gyula", "Kabos Gyula", "Jávor Pál"));
        actorsMoviesService.insertMovieWithActors("Meseautó", LocalDate.of(1934, 12,14), Arrays.asList("Perczel Zita", "Kabos Gyula", "Törzs Jenő"));
        actorsMoviesService.insertMovieWithActors("Fizessen, nagysád!", LocalDate.of(1937, 3,11), Arrays.asList("Kabos Gyula", "Jávor Pál", "Muráti Lili"));

        assertEquals(3, actorsMoviesService.getMoviesRepository().findAllMovies().size());
        assertTrue(actorsMoviesService.getActorsRepository().findActorByName("Kabos Gyula").isPresent());
        assertTrue(actorsMoviesService.getMoviesRepository().findMovieByTitle("Meseautó").isPresent());
        assertEquals(1, actorsMoviesService.getActorsRepository().findActorsWithPrefix("K").size());
    }
}