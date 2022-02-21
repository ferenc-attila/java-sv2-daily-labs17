package day01;

import day01.Movie;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MoviesRepository {

    private DataSource dataSource;

    public MoviesRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public long saveMovie(String title, LocalDate releaseDate) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("INSERT INTO movies(title, release_date, avg_rating) VALUES (?,?,?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, title);
            statement.setDate(2, Date.valueOf(releaseDate));
            statement.setDouble(3, 0.0);
            statement.executeUpdate();
            return getMovieIdAfterInsert(statement);
        } catch (SQLException sqle) {
            throw new IllegalStateException("Cannot connect!", sqle);
        }
    }

    private long getMovieIdAfterInsert(PreparedStatement statement) throws SQLException {
        try (ResultSet resultSet = statement.getGeneratedKeys()) {
            if (resultSet.next()) {
                return resultSet.getLong(1);
            }
            throw new IllegalStateException("Cannot insert: " + statement);
        }
    }

    public Optional<Movie> findMovieByTitle(String title) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM movies WHERE title=?")) {
            statement.setString(1, title);
            return getMovie(statement);
        } catch (SQLException sqle) {
            throw new IllegalStateException("Cannot connect!", sqle);
        }
    }

    private Optional<Movie> getMovie(PreparedStatement statement) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                return Optional.of(new Movie(resultSet.getLong("id"), resultSet.getString("title"), resultSet.getDate("release_date").toLocalDate(), resultSet.getDouble("avg_rating")));
            }
            return Optional.empty();
        }
    }

    public List<Movie> findAllMovies() {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM movies");
             ResultSet resultSet = statement.executeQuery()) {
            return processListOfMoviesByResultSet(resultSet);
        } catch (SQLException sqle) {
            throw new IllegalStateException("Cannot query!", sqle);
        }
    }

    private List<Movie> processListOfMoviesByResultSet(ResultSet resultSet) throws SQLException {
        List<Movie> movies = new ArrayList<>();
        while (resultSet.next()) {
            movies.add(new Movie(resultSet.getLong("id"), resultSet.getString("title"), resultSet.getDate("release_date").toLocalDate(), resultSet.getDouble("avg_rating")));
        }
        return movies;
    }

    public void updateAverageRatingByMovieTitle(String movieTitle, double averageRating) {
        long movieId = findMovieByTitle(movieTitle).orElseThrow(() -> new IllegalArgumentException("No such movie")).getId();
        updateAverageRatingByMovieId(movieId, averageRating);
    }

    public void updateAverageRatingByMovieId(long movieId, double averageRating) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE movies SET avg_rating = ? WHERE id = ?")) {
            statement.setDouble(1, averageRating);
            statement.setLong(2, movieId);
            statement.executeUpdate();
        } catch (SQLException sqle) {
            throw new IllegalStateException("Cannot update!");
        }

    }
}
