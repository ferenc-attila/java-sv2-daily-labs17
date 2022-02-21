package day01;

import com.mysql.cj.jdbc.MysqlDataSource;
import org.flywaydb.core.Flyway;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setUrl("jdbc:mysql://localhost:3306/movies-actors?useUnicode=true");
        dataSource.setUser("employees");
        dataSource.setPassword("employees");

        Flyway flyway = Flyway.configure().dataSource(dataSource).load();
        flyway.migrate();

        ActorsRepository actorsRepository = new ActorsRepository(dataSource);
        MoviesRepository moviesRepository = new MoviesRepository(dataSource);
        RatingsRepository ratingsRepository = new RatingsRepository(dataSource);
        ActorsMoviesRepository actorsMoviesRepository = new ActorsMoviesRepository(dataSource);
        ActorsMoviesService actorsMoviesService = new ActorsMoviesService(actorsRepository, moviesRepository, actorsMoviesRepository);
        MoviesRatingService moviesRatingService = new MoviesRatingService(moviesRepository, ratingsRepository);

//        actorsMoviesService.insertMovieWithActors("Titanic", LocalDate.of(1997, 12, 11), List.of("Leonardo DiCaprio","Kate Winslet"));
//        actorsMoviesService.insertMovieWithActors("Great Gatsby", LocalDate.of(1997, 12, 11), List.of("Leonardo DiCaprio", "Toby"));
        List<Movie> movies = moviesRepository.findAllMovies();
        System.out.println(movies.size());
//        moviesRatingService.insertRatings("Titanic", 5,3,2);
        moviesRatingService.insertRatings("Great Gatsby", 1, 3, 6, 2);
    }
}
