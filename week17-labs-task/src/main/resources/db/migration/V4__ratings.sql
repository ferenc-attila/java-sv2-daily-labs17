CREATE TABLE ratings (
id BIGINT AUTO_INCREMENT,
movie_id BIGINT,
rating BIGINT,
CONSTRAINT pk_ratings PRIMARY KEY(id)
);