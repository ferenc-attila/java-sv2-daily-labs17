package day01;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RatingsRepository {

    DataSource dataSource;

    public RatingsRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void insertRatings(long movieId, List<Integer> ratings) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            runInsertRatingStatement(movieId, ratings, connection);
            double averageRating = getRatingsByMovieId(movieId).stream().mapToLong(Long::longValue).average().orElse(0.0);
            new MoviesRepository(dataSource).updateAverageRatingByMovieId(movieId, averageRating);
        } catch (SQLException sqle) {
            throw new IllegalStateException("Cannot insert!", sqle);
        }
    }

    public List<Long> getRatingsByMovieTitle(String movieTitle) {
        Long movieId = getMovieIdByTitle(movieTitle);
        return getRatingsByMovieId(movieId);
    }

    public List<Long> getRatingsByMovieId(long movieId) {
        List<Long> ratings = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT rating FROM ratings WHERE movie_id = ? ORDER BY rating");
        ) {
            statement.setLong(1, movieId);
            createListOfRatings(ratings, statement);
            return ratings;
        } catch (SQLException sqle) {
            throw new IllegalStateException("Cannot connect!");
        }
    }

    private void runInsertRatingStatement(long movieId, List<Integer> ratings, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("insert into ratings(movie_id,rating) VALUES (?,?)")) {
            for (Integer actual : ratings) {
                rollbackIfInvalidRating(connection, actual);
                executeInsert(movieId, statement, actual);
            }
            connection.commit();
        }
    }

    private void rollbackIfInvalidRating(Connection connection, Integer actual) throws SQLException{
        if (actual < 1 || actual > 5) {
            connection.rollback();
            throw new IllegalArgumentException("Invalid rating value in the list: " + actual);
        }
    }

    private void executeInsert(long movieId, PreparedStatement statement, Integer actual) throws SQLException {
        statement.setLong(1, movieId);
        statement.setLong(2, actual);
        statement.executeUpdate();
    }

    private void createListOfRatings(List<Long> ratings, PreparedStatement statement) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                ratings.add(resultSet.getLong(1));
            }
        }
    }

    private Long getMovieIdByTitle(String movieTitle) {
        MoviesRepository moviesRepository = new MoviesRepository(dataSource);
        return moviesRepository.findMovieByTitle(movieTitle).orElseThrow(() -> new IllegalArgumentException("Cannot find movie by title: " + movieTitle)).getId();
    }
}
