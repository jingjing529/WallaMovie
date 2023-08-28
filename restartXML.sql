delete from genres_in_movies WHERE movieId NOT LIKE '%tt%';
delete from genres_in_movies where genreId > 23;
delete from genres where id > 23;
delete from stars_in_movies where movieId NOT LIKE '%tt%';
delete from stars_in_movies where starId REGEXP '^[0-9]+$';
DELETE FROM stars WHERE id REGEXP '^[0-9]+$';
DELETE FROM movies WHERE id NOT LIKE '%tt%';