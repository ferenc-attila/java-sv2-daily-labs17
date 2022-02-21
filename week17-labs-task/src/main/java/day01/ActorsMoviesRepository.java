package day01;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ActorsMoviesRepository {

    private DataSource dataSource;

    public ActorsMoviesRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void insertActorAndMovieId(long actorId, long movieId) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("INSERT INTO actors_movies(actor_id,movie_id) VALUES (?,?)")) {
            statement.setLong(1, actorId);
            statement.setLong(2, movieId);
            statement.executeUpdate();
        } catch (SQLException sqle) {
            throw new IllegalStateException("Cannot insert", sqle);
        }
    }

    public List<Movie> getMoviesByActor(String actorName) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT movies.id, title, release_date, avg_rating, actors_movies.movie_id, actors.actor_name FROM movies LEFT OUTER JOIN actors_movies ON movies.id = actors_movies.movie_id LEFT OUTER JOIN actors ON actor_id = actors.id HAVING actors.actor_name = ? ORDER BY title;")) {
            statement.setString(1, actorName);
            return processListOfMoviesByResultSet(statement);
        } catch (SQLException sqle) {
            throw new IllegalStateException("Cannot connect", sqle);
        }
    }

    private List<Movie> processListOfMoviesByResultSet(PreparedStatement statement) {
        List<Movie> movies = new ArrayList<>();
        try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                movies.add(new Movie(resultSet.getLong("movie_id"), resultSet.getString("title"), resultSet.getDate("release_date").toLocalDate(), resultSet.getDouble("avg_rating")));
            }
        } catch (SQLException sqle) {
            throw new IllegalStateException("Cannot query: " + statement, sqle);
        }
        return movies;
    }

    public List<Actor> getActorsByMovie(String movieTitle) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT * FROM actors JOIN actors_movies ON actors.id = actors_movies.actor_id JOIN movies ON actors_movies.movie_id = movies.id HAVING movies.title = ? ORDER BY actor_name;")) {
            statement.setString(1, movieTitle);
            return processListOfActorsByResultSet(statement);
        } catch (SQLException sqle) {
            throw new IllegalStateException("Cannot connect", sqle);
        }
    }

    private List<Actor> processListOfActorsByResultSet(PreparedStatement statement) {
        List<Actor> actors = new ArrayList<>();
        try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                actors.add(new Actor(resultSet.getLong("actor_id"), resultSet.getString("actor_name")));
            }
        } catch (SQLException sqle) {
            throw new IllegalStateException("Cannot query: " + statement, sqle);
        }
        return actors;
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}
