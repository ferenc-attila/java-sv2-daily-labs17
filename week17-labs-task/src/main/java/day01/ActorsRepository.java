package day01;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ActorsRepository {

    private DataSource dataSource;

    public ActorsRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Long saveActorAndGetGeneratedKey(String name) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("insert into actors(actor_name) values(?)",
                     Statement.RETURN_GENERATED_KEYS)
        ) {
            statement.setString(1, name);
            statement.executeUpdate();
            return executeAndGetGeneratedKey(statement);
        } catch (SQLException sqle) {
            throw new IllegalStateException("Cannot update", sqle);
        }
    }

    private long executeAndGetGeneratedKey(PreparedStatement statement) {
        try (ResultSet resultSet = statement.getGeneratedKeys()) {
            if (resultSet.next()) {
                return resultSet.getLong(1);
            }
            throw new SQLException("No keys heas generated!");
        } catch (SQLException sqle) {
            throw new IllegalArgumentException("Error by insert", sqle);
        }
    }

    public Optional<Actor> findActorByName(String name) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM actors WHERE actor_name=?")) {
            statement.setString(1, name);
            return getActor(statement);
        } catch (SQLException sqle) {
            throw new IllegalStateException("Cannot connect!");
        }
    }

    private Optional<Actor> getActor(PreparedStatement statement) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                return Optional.of(new Actor(resultSet.getLong("id"), resultSet.getString("actor_name")));
            }
            return Optional.empty();
        }
    }

    public List<String> findActorsWithPrefix(String prefix) {
        List<String> result = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT actor_name FROM actors WHERE actor_name LIKE ?")) {
            statement.setString(1, prefix + "%");

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String actorName = resultSet.getString("actor_name");
                    result.add(actorName);
                }
            }
        } catch (SQLException sqle) {
            throw new IllegalStateException("Cannot query!", sqle);
        }
        return result;
    }
}
