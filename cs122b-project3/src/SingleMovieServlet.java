import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@WebServlet(name = "SingleMovieServlet", urlPatterns = "/api/single-movie")
public class SingleMovieServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;
    private DataSource dataSource;
    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json"); // Response mime type
        HttpSession session = request.getSession();

        String id = request.getParameter("id");
        request.getServletContext().log("getting id: " + id);
        PrintWriter out = response.getWriter();
        List<List<String>> res = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()) {

           String query =
            "SELECT m.id as movieId, m.title, m.year, m.director, GROUP_CONCAT(g.name ORDER BY g.name ASC SEPARATOR ', ') AS genre, r.rating\n" +
            "FROM movies m\n" +
            "JOIN genres_in_movies gim ON m.id = gim.movieId\n" +
            "JOIN genres g ON gim.genreId = g.id\n" +
            "LEFT JOIN ratings r ON r.movieId = m.id\n" +
            "WHERE m.id = ?\n" +
            "GROUP BY m.id, m.title, m.year, m.director, r.rating;";


            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, id);
            ResultSet rs = statement.executeQuery();
            List<String> movieInfo = new ArrayList<>();
            while (rs.next()) {
                String movieId = rs.getString("movieId");
                String movieTitle = rs.getString("title");
                String movieYear = rs.getString("year");
                String movieDirector = rs.getString("director");

                String genre = rs.getString("genre");
                String rating = rs.getString("rating");
                if(rating == null)
                {
                    rating = "N/A";
                }

                movieInfo.add("movie_id");
                movieInfo.add(movieId);
                movieInfo.add("movie_title");
                movieInfo.add(movieTitle);
                //jsonObject.addProperty("movie_title", movieTitle);
                movieInfo.add("movie_year");
                movieInfo.add(movieYear);
                movieInfo.add("movie_director");
                //jsonObject.addProperty("movie_year", movieYear);
                movieInfo.add(movieDirector);
                movieInfo.add("rating");
                //jsonObject.addProperty("movie_director", movieDirector);
                movieInfo.add(rating);
                movieInfo.add("genre");
                //jsonObject.addProperty("rating", rating);
                movieInfo.add(genre);
                //jsonObject.addProperty("genre",genre);
                //jsonArray.add(jsonObject);

            }
            rs.close();
            statement.close();
            // This query sorts the name by number of movies played
            query = "WITH temp(id,name, dob) AS (\n" +
                            "SELECT stars.id, stars.name, stars.birthYear\n" +
                            "FROM stars\n" +
                            "JOIN stars_in_movies ON stars.id = stars_in_movies.starId\n" +
                            "WHERE stars_in_movies.movieId = ?)\n" +
                            "\n" +
                            "SELECT temp.name, temp.id, temp.dob, COUNT(stars_in_movies.movieId) as count \n" +
                            "FROM temp JOIN stars_in_movies ON temp.id = stars_in_movies.starId\n" +
                            "GROUP BY temp.name, temp.id, temp.dob\n" +
                            "ORDER by COUNT(stars_in_movies.movieId) DESC, temp.name;";

            statement = conn.prepareStatement(query);
            statement.setString(1, id);

            ResultSet rs2 = statement.executeQuery();
            while (rs2.next()) {
                List<String> temp = new ArrayList<>(movieInfo);
                String starId = rs2.getString("id");
                String starName = rs2.getString("name");
                String starDob = rs2.getString("dob");
                String count = rs2.getString("count");

                temp.add("star_id");
                temp.add(starId);
                temp.add("star_name");
                temp.add(starName + "(" + count + ")");
                if (starDob == null) {
                    starDob = "N/A";
                }
                temp.add("star_dob");
                temp.add(starDob);
                res.add(temp);
            }

            JsonArray jsonArray = new JsonArray();
            for(int i = 0; i < res.size(); i++) {
                JsonObject jsonObject = new JsonObject();
                for (int j = 0; j < res.get(i).size(); j += 2) {
                    //System.out.print((res.get(i).get(j)) + " : " + (res.get(i).get(j+1)) + " ");
                    jsonObject.addProperty(res.get(i).get(j), res.get(i).get(j + 1));
                }
                //System.out.println();
                jsonArray.add(jsonObject);
            }

            JsonObject UrljsonObject = new JsonObject();
            String url = (String) session.getAttribute("movie_page");
            UrljsonObject.addProperty("movie_page", url);
            System.out.println(UrljsonObject);
            jsonArray.add(UrljsonObject);


            out.write(jsonArray.toString());
            response.setStatus(200);
        } catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }
    }

//    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
//        HttpSession session = request.getSession();
//        ArrayList<String> previousItems = (ArrayList<String>) session.getAttribute("previousItems");
//        JsonObject responseJsonObject = new JsonObject();
//
//        responseJsonObject.addProperty("add", "success");
//        request.getServletContext().log("getting " + previousItems.size() + " items");
//
//        String newItem = request.getParameter("movie-title"); // Get parameter that sent by GET request url
////        String newTitle = request.getParameter("movie-title");
////        String newMoney = request.getParameter("movie-money");
////        String newItem = newId + ", " + newTitle + ", " + newMoney;
//        responseJsonObject.addProperty("newItem", newItem);
//        System.out.println("newItem");
//        System.out.println(newItem);
//
//        synchronized (previousItems) {
//            if (newItem != null) {
//                previousItems.add(newItem);
//                // Add the new item to the previousItems ArrayList
//            }
//        }
//        System.out.println(previousItems);
//        response.getWriter().write(responseJsonObject.toString());
//    }
}
