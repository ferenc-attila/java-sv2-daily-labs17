package day01;

import com.mysql.cj.jdbc.MysqlDataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ActorsRepositoryTest {

    ActorsRepository actorsRepository;

    @BeforeEach
    void init() {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setUrl("jdbc:mysql://localhost:3306/movies-actors-test?useUnicode=true");
        dataSource.setUser("employees");
        dataSource.setPassword("employees");

        Flyway flyway = Flyway.configure().dataSource(dataSource).load();
        flyway.clean();
        flyway.migrate();

        actorsRepository = new ActorsRepository(dataSource);
    }

    @Test
    void insertTest() {
        Long id = actorsRepository.saveActorAndGetGeneratedKey("Jack Doe");
        assertEquals(1, id);
        assertEquals("Jack Doe", actorsRepository.findActorByName("Jack Doe").get().getName());
        Long anotherId = actorsRepository.saveActorAndGetGeneratedKey("John Doe");
        assertEquals(2, anotherId);
        assertEquals(2, actorsRepository.findActorsWithPrefix("J").size());
    }

    @Test
    void findActorByNameTest() {
        actorsRepository.saveActorAndGetGeneratedKey("Jack Doe");
        assertEquals("Jack Doe", actorsRepository.findActorByName("Jack Doe").get().getName());
    }

    @Test
    void findActorByInvalidNameTest() {
        actorsRepository.saveActorAndGetGeneratedKey("Jack Doe");
        assertEquals(Optional.empty(), actorsRepository.findActorByName("J. Doe"));
    }

    @Test
    void findActorsWithPrefixTest() {
        actorsRepository.saveActorAndGetGeneratedKey("Jack Doe");
        actorsRepository.saveActorAndGetGeneratedKey("John Doe");
        actorsRepository.saveActorAndGetGeneratedKey("Jack Smith");
        assertEquals(3, actorsRepository.findActorsWithPrefix("j").size());
        assertEquals(2, actorsRepository.findActorsWithPrefix("Jack").size());
        assertEquals(0, actorsRepository.findActorsWithPrefix("Mark").size());
    }

    @Test
    void invalidDataSourceTest() {
        MysqlDataSource invalidDataSource = new MysqlDataSource();
        invalidDataSource.setUrl("jdbc:mysql://localhost:3306/movies-actors-invalid-test?useUnicode=true");
        invalidDataSource.setUser("employees");
        invalidDataSource.setPassword("employees");

        ActorsRepository invalidRepository = new ActorsRepository(invalidDataSource);

        IllegalStateException ise = assertThrows(IllegalStateException.class, () -> invalidRepository.findActorByName("Csortos Gyula"));
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

        ActorsRepository invalidRepository = new ActorsRepository(invalidDataSource);

        IllegalStateException ise = assertThrows(IllegalStateException.class, () -> invalidRepository.saveActorAndGetGeneratedKey("Csortos Gyula"));
        assertEquals("Cannot update", ise.getMessage());
        assertTrue(ise.getCause().getMessage().startsWith("Access denied for user 'emp'"));
        assertEquals(SQLException.class, ise.getCause().getClass());
    }

    @Test
    void invalidDatabasePasswordTest() {
        MysqlDataSource invalidDataSource = new MysqlDataSource();
        invalidDataSource.setUrl("jdbc:mysql://localhost:3306/movies-actors-test?useUnicode=true");
        invalidDataSource.setUser("employees");
        invalidDataSource.setPassword("emp");

        ActorsRepository invalidRepository = new ActorsRepository(invalidDataSource);

        IllegalStateException ise = assertThrows(IllegalStateException.class, () -> invalidRepository.findActorsWithPrefix("K"));
        assertEquals("Cannot query!", ise.getMessage());
        assertTrue(ise.getCause().getMessage().startsWith("Access denied for user 'employees'"));
        assertEquals(SQLException.class, ise.getCause().getClass());
    }
}