package day01;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class ActorsMoviesService {

    private ActorsRepository actorsRepository;
    private MoviesRepository moviesRepository;
    private ActorsMoviesRepository actorsMoviesRepository;

    public ActorsMoviesService(ActorsRepository actorsRepository, MoviesRepository moviesRepository, ActorsMoviesRepository actorsMoviesRepository) {
        this.actorsRepository = actorsRepository;
        this.moviesRepository = moviesRepository;
        this.actorsMoviesRepository = actorsMoviesRepository;
    }

    public void insertMovieWithActors(String title, LocalDate releaseDate, List<String> actorNames) {
        long movieId = moviesRepository.saveMovie(title, releaseDate);
        for (String actual : actorNames) {
            long actorId;
            Optional<Actor> founded = actorsRepository.findActorByName(actual);
            if (founded.isPresent()) {
                actorId = founded.get().getId();
            } else {
                actorId = actorsRepository.saveActorAndGetGeneratedKey(actual);
            }
            actorsMoviesRepository.insertActorAndMovieId(actorId, movieId);
        }
    }

    public ActorsRepository getActorsRepository() {
        return actorsRepository;
    }

    public MoviesRepository getMoviesRepository() {
        return moviesRepository;
    }

    public ActorsMoviesRepository getActorsMoviesRepository() {
        return actorsMoviesRepository;
    }
}
