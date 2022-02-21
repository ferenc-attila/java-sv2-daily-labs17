package day01;

import com.mysql.cj.jdbc.MysqlDataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ActorsMoviesRepositoryTest {

    ActorsMoviesRepository actorsMoviesRepository;

    @BeforeEach
    void init() {
        MysqlDataSource dataSource = new MysqlDataSource();
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
        List<Long> resultIds = new ArrayList();
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