package day01;

import com.mysql.cj.jdbc.MysqlDataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ActorsMoviesRepositoryTest {

    ActorsMoviesRepository actorsMoviesRepository;
    MysqlDataSource dataSource;

    @BeforeEach
    void init() {
        dataSource = new MysqlDataSource();
        dataSource.setUrl("jdbc:mysql://localhost:3306/movies-actors-test?useUnicode=true");
        dataSource.setUser("employees");
        dataSource.setPassword("employees");

        Flyway flyway = Flyway.configure().dataSource(dataSource).load();
        flyway.clean();
        flyway.migrate();

        actorsMoviesRepository = new ActorsMoviesRepository(dataSource);
    }

    @Test
    void insertActorAndMovieIdTest() throws SQLException {
        actorsMoviesRepository.insertActorAndMovieId(2, 3);
        List<Long> resultIds = new ArrayList<>();
        try (Connection connection = actorsMoviesRepository.getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM actors_movies WHERE actor_id = ? ORDER BY id")
        ) {
            statement.setLong(1, 2);
            addResultIds(resultIds, statement);
        }
        assertEquals(1L, resultIds.get(0));
        assertEquals(2L, resultIds.get(1));
        assertEquals(3L, resultIds.get(2));
    }

    @Test
    void getMoviesByActorTest() {
        MoviesRepository moviesRepository = new MoviesRepository(dataSource);
        ActorsRepository actorsRepository = new ActorsRepository(dataSource);
        ActorsMoviesService actorsMoviesService = new ActorsMoviesService(actorsRepository, moviesRepository, actorsMoviesRepository);
        actorsMoviesService.insertMovieWithActors("Hyppolit a lakáj", LocalDate.of(1931, 11,27), Arrays.asList("Csortos Gyula", "Kabos Gyula", "Jávor Pál"));
        actorsMoviesService.insertMovieWithActors("Meseautó", LocalDate.of(1934, 12,14), Arrays.asList("Perczel Zita", "Kabos Gyula", "Törzs Jenő"));
        actorsMoviesService.insertMovieWithActors("Fizessen, nagysád!", LocalDate.of(1937, 3,11), Arrays.asList("Kabos Gyula", "Jávor Pál", "Muráti Lili"));

        List<Movie> expected = Arrays.asList(
          new Movie(3L, "Fizessen, nagysád!", LocalDate.of(1937, 3,11), 0.0),
          new Movie(1L, "Hyppolit a lakáj", LocalDate.of(1931, 11,27), 0.0),
          new Movie(2L, "Meseautó", LocalDate.of(1934, 12,14), 0.0)
        );

        List<Movie> movies = actorsMoviesRepository.getMoviesByActor("Kabos Gyula");

        assertEquals(expected, movies);
    }

    @Test
    void getActorsByMovie() {
        MoviesRepository moviesRepository = new MoviesRepository(dataSource);
        ActorsRepository actorsRepository = new ActorsRepository(dataSource);
        ActorsMoviesService actorsMoviesService = new ActorsMoviesService(actorsRepository, moviesRepository, actorsMoviesRepository);
        actorsMoviesService.insertMovieWithActors("Hyppolit a lakáj", LocalDate.of(1931, 11,27), Arrays.asList("Csortos Gyula", "Kabos Gyula", "Jávor Pál"));
        List<Actor> expected = (Arrays.asList(
         new Actor(1L, "Csortos Gyula"),
         new Actor(3L, "Jávor Pál"),
         new Actor(2L, "Kabos Gyula")
        ));

        List<Actor> actors = actorsMoviesRepository.getActorsByMovie("Hyppolit a lakáj");

        assertEquals(expected, actors);
    }

    @Test
    void invalidDataSourceTest() {
        MysqlDataSource invalidDataSource = new MysqlDataSource();
        invalidDataSource.setUrl("jdbc:mysql://localhost:3306/movies-actors-invalid-test?useUnicode=true");
        invalidDataSource.setUser("employees");
        invalidDataSource.setPassword("employees");

        ActorsMoviesRepository invalidRepository = new ActorsMoviesRepository(invalidDataSource);

        IllegalStateException ise = assertThrows(IllegalStateException.class, () -> invalidRepository.insertActorAndMovieId(1,1));
        assertEquals("Cannot insert", ise.getMessage());
        assertEquals("Unknown database 'movies-actors-invalid-test'", ise.getCause().getMessage());
        assertEquals(SQLSyntaxErrorException.class, ise.getCause().getClass());
    }

    @Test
    void invalidDatabaseUsernameTest() {
        MysqlDataSource invalidDataSource = new MysqlDataSource();
        invalidDataSource.setUrl("jdbc:mysql://localhost:3306/movies-actors-test?useUnicode=true");
        invalidDataSource.setUser("emp");
        invalidDataSource.setPassword("employees");

        ActorsMoviesRepository invalidRepository = new ActorsMoviesRepository(invalidDataSource);

        IllegalStateException ise = assertThrows(IllegalStateException.class, () -> invalidRepository.insertActorAndMovieId(1,1));
        assertEquals("Cannot insert", ise.getMessage());
        assertTrue(ise.getCause().getMessage().startsWith("Access denied for user 'emp'"));
        assertEquals(SQLException.class, ise.getCause().getClass());
    }

    @Test
    void invalidDatabasePasswordTest() {
        MysqlDataSource invalidDataSource = new MysqlDataSource();
        invalidDataSource.setUrl("jdbc:mysql://localhost:3306/movies-actors-test?useUnicode=true");
        invalidDataSource.setUser("employees");
        invalidDataSource.setPassword("emp");

        ActorsMoviesRepository invalidRepository = new ActorsMoviesRepository(invalidDataSource);

        IllegalStateException ise = assertThrows(IllegalStateException.class, () -> invalidRepository.insertActorAndMovieId(1,1));
        assertEquals("Cannot insert", ise.getMessage());
        assertTrue(ise.getCause().getMessage().startsWith("Access denied for user 'employees'"));
        assertEquals(SQLException.class, ise.getCause().getClass());
    }

    private void addResultIds(List<Long> resultIds, PreparedStatement statement) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                resultIds.add(resultSet.getLong("id"));
                resultIds.add(resultSet.getLong("actor_id"));
                resultIds.add(resultSet.getLong("movie_id"));
            }
        }
    }
}