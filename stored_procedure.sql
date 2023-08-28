delimiter $$

CREATE PROCEDURE add_movie
(
    IN movie_title VARCHAR(100),
    IN movie_director VARCHAR(100),
    IN movie_year INT,
    IN genre VARCHAR(32),
    IN star VARCHAR(100),
    OUT message VARCHAR(100)
)
BEGIN
    -- Check if the movie already exists in the database
    DECLARE movie_id VARCHAR(10);
    DECLARE genre_id INT;
    DECLARE star_id VARCHAR(10);
    -- DECLARE message VARCHAR(100);
    
    SELECT id into movie_id
    FROM movies
    WHERE title = movie_title AND director = movie_director AND year = movie_year;
    
    IF movie_id IS NOT NULL
    THEN 
    SET message = 'Error: Movie already exists in database. Please try again.';

    ELSE
		-- Generate a new id for the movie using the next available id in the movies table
        SELECT CONCAT('tt0', SUBSTRING(MAX(id), 4)+1) INTO movie_id
        FROM movies
		where length(id)=9 and Substring(id,1,3) = "tt0";
        
        -- Insert the new movie into the movies table
        INSERT INTO movies (id, title, director, year)
        VALUES (movie_id, movie_title, movie_director, movie_year);
        
        -- Check if the genre already exists in the database
        SELECT id INTO genre_id
        FROM genres
        WHERE name = genre;
        
        IF genre_id IS NULL THEN
            -- If the genre doesn't exist, create a new row in the genre table with the next available id
            SELECT MAX(id) + 1 INTO genre_id
            FROM genres;
            
            INSERT INTO genres (id, name)
            VALUES (genre_id, genre);
        END IF;
        
        -- Map the new movie to the genre in the genre_in_movies table
        INSERT INTO genres_in_movies (genreId, movieId)
        VALUES (genre_id, movie_id);
        
        -- Check if the star already exists in the database
        SELECT id INTO star_id
        FROM stars
        WHERE name = star;
        
        IF star_id IS NULL THEN
            -- If the star doesn't exist, create a new row in the stars table with the next available id
            SELECT CONCAT('nm', SUBSTRING(MAX(id), 3) + 1) INTO star_id
            FROM stars;
            
            INSERT INTO stars (id, name, birthYear)
            VALUES (star_id, star, null);
        END IF;
        
        -- Map the new movie to the star in the star_in_movies table
        INSERT INTO stars_in_movies (starId, movieId)
        VALUES (star_id, movie_id);
        
        SET message = CONCAT(CONCAT(CONCAT(CONCAT(CONCAT(CONCAT(CONCAT('Success! Movie ', movie_title), ' has been added! Movie id: '), movie_id),' Star id: '), star_id), ' Genre id: '), genre_id);
    END IF;
END $$;

delimiter ;

