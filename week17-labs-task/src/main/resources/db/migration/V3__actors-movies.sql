CREATE TABLE actors_movies(
id bigint auto_increment,
actor_id bigint,
movie_id bigint,
CONSTRAINT pk_movies_actors PRIMARY KEY(id));