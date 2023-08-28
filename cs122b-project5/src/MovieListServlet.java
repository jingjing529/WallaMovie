import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mysql.cj.x.protobuf.MysqlxPrepare;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@WebServlet(name = "MovieListServlet", urlPatterns = "/api/movie-list")
public class MovieListServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/read");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        long servletStartTime = System.currentTimeMillis(); // Start time for TS

        String Url = request.getQueryString();
        if (!Objects.equals(Url, "num=null&page=null&sort=null&input=null")){
            session.setAttribute("movie_page", Url);
        }
        System.out.println(session.getAttribute("previousItems"));

        response.setContentType("application/json"); // Response mime type
        PrintWriter out = response.getWriter();

        List<List<String>> res = new ArrayList<>();

        String pageNumber = request.getParameter("page");
        String numberPerPage = request.getParameter("num");
        String sort = request.getParameter("sort");
        String input = request.getParameter("input");
        String sortOrder = "";
        if (sort.substring(0,2).equals("t0")) {
            sortOrder += "m.title DESC";
        }
        else if (sort.substring(0,2).equals("t1")) {
            sortOrder += "m.title";
        }
        else if (sort.substring(0,2).equals("r0")) {
            sortOrder += "r.rating DESC";
        }
        else if (sort.substring(0,2).equals("r1")) {
            sortOrder += "r.rating";
        }
        sortOrder += " , ";
        if (sort.substring(2).equals("t0")) {
            sortOrder += "m.title DESC";
        }
        else if (sort.substring(2).equals("t1")) {
            sortOrder += "m.title";
        }
        else if (sort.substring(2).equals("r0")) {
            sortOrder += "r.rating DESC";
        }
        else if (sort.substring(2).equals("r1")) {
            sortOrder += "r.rating";
        }

        String inputQuery = "";
        if (input != null) {
            String[] inputArr = input.trim().split(":");
            if (inputArr[0].equals("genre")) {
                inputQuery += "g.name = '" + inputArr[1] + "'";
            }
            else if (inputArr[0].equals("alpha")) {
                if (inputArr[1].equals("*")) {
                    inputQuery += "m.title REGEXP '^[^a-zA-Z0-9]'";
                }
                else {
                    inputQuery += "m.title LIKE '" + inputArr[1] + "%' OR m.title LIKE '" + inputArr[1].toLowerCase() + "%' ";
                }
            }
            else {
                for (int i = 0; i < inputArr.length; i+=2) {
                    if (i != 0 && !inputArr[i].equals(" ")) {
                        inputQuery += " AND ";
                    }
                    if (inputArr[i].equals("title")) {
                        String temp = "+" + String.join("*+", inputArr[i+1].split(" ")) + "*";

                        inputQuery += "MATCH(m.title) AGAINST ('" + temp + "' IN BOOLEAN MODE) " ;
                        inputQuery += "OR " + "m.title LIKE '%" + inputArr[i+1] + "%' COLLATE utf8mb4_general_ci ";
                        inputQuery += " OR " + "edth(title, '" + inputArr[i+1] +"', 2) =1 ";
                    }
                    else if (inputArr[i].equals("year")) {
                        inputQuery += "m.year ='" + inputArr[i+1] + "' ";
                    }
                    else if (inputArr[i].equals("director")) {
                        inputQuery += "m.director LIKE '%" + inputArr[i+1] + "%' COLLATE utf8mb4_general_ci ";
                    }
                    else if (inputArr[i].equals("name")) {
                        inputQuery += "s.name LIKE '%" + inputArr[i+1] + "%' COLLATE utf8mb4_general_ci ";
                    }
                }
            }
        }

        FileWriter writer = null;
        try (Connection conn = dataSource.getConnection()) {
            long jdbcStartTime = System.currentTimeMillis(); // Start time for TJ

            String query = "WITH temp(id) AS (\n" +
                    "SELECT m.id\n" +
                    "FROM movies m LEFT JOIN ratings r ON m.id = r.movieId " +
                    "JOIN genres_in_movies gim ON m.id = gim.movieId " +
                    "JOIN genres g ON gim.genreId = g.id " +
                    "JOIN stars_in_movies sim ON m.id = sim.movieId\n" +
                    "JOIN stars s ON sim.starId = s.id\n" +
                    "WHERE " + inputQuery +
                    "ORDER BY " + sortOrder + ")\n" +
                    "SELECT m.id as movieId, m.title, m.year, m.director, GROUP_CONCAT(distinct g.name SEPARATOR ',') AS genres, r.rating\n" +
                    "FROM movies m\n" +
                    "JOIN genres_in_movies gim ON m.id = gim.movieId\n" +
                    "JOIN genres g ON gim.genreId = g.id\n" +
                    "JOIN temp ON m.id = temp.id\n" +
                    "LEFT JOIN ratings r ON r.movieId = m.id\n" +
                    "GROUP BY m.id, m.title, m.year, m.director,  r.rating\n" +
                    "ORDER BY " + sortOrder +
                    " LIMIT " + numberPerPage + " OFFSET " + Integer.toString((Integer.parseInt(pageNumber)-1) * Integer.parseInt(numberPerPage));

            PreparedStatement statement1 = conn.prepareStatement(query);
            ResultSet rs = statement1.executeQuery(query);
            
            while (rs.next()) {
                List<String> temp = new ArrayList<>();
                String id = rs.getString("movieId");
                String title = rs.getString("title");
                String year = rs.getString("year");
                String director = rs.getString("director");
                String rating = rs.getString("rating");
                String genre = rs.getString("genres");
                String[] genres = genre.split(",");

                temp.add("id");
                temp.add(id);
                temp.add("title");
                temp.add(title);
                temp.add("year");
                temp.add(year);
                temp.add("director");
                temp.add(director);
                temp.add("rating");
                temp.add(rating);

                String genre1 = "";
                String genre2 = "";
                String genre3 = "";
                if (genres.length >= 1) {
                    genre1 = genres[0];
                }
                if (genres.length >= 2) {
                    genre2 = genres[1];
                }
                if (genres.length >= 3) {
                    genre3 = genres[2];
                }
                temp.add("genre1");
                temp.add(genre1);
                temp.add("genre2");
                temp.add(genre2);
                temp.add("genre3");
                temp.add(genre3);

                String query2 = "WITH temp(id,name, dob) AS (\n" +
                        "SELECT stars.id, stars.name, stars.birthYear\n" +
                        "FROM stars\n" +
                        "JOIN stars_in_movies ON stars.id = stars_in_movies.starId\n" +
                        "WHERE stars_in_movies.movieId = ?)\n" +
                        "\n" +
                        "SELECT temp.name, temp.id, temp.dob, COUNT(stars_in_movies.movieId) as count \n" +
                        "FROM temp JOIN stars_in_movies ON temp.id = stars_in_movies.starId\n" +
                        "GROUP BY temp.name, temp.id, temp.dob\n" +
                        "ORDER by COUNT(stars_in_movies.movieId) DESC, temp.name;";

                PreparedStatement statement2 = conn.prepareStatement(query2);
                statement2.setString(1, id);
                ResultSet rs2 = statement2.executeQuery();
                int counter = 1;
                while (rs2.next() && counter <= 3) {
                    String starId = rs2.getString("id");
                    String starName = rs2.getString("name");
                    String starDob = rs2.getString("dob");
                    String count = rs2.getString("count");

                    temp.add("star"+counter);
                    temp.add(starName + "(" + count + ")");
                    temp.add("starId"+counter);
                    temp.add(starId);
                    if (starDob == null) {
                        starDob = "N/A";
                    }
                    counter += 1;
                }
                rs2.close();
                statement2.close();
                res.add(temp);
            }

            rs.close();
            statement1.close();
            JsonArray jsonArray = new JsonArray();
            for(int i = 0; i < res.size(); i++) {
                JsonObject jsonObject = new JsonObject();
                for (int j = 0; j < res.get(i).size(); j += 2) {
                    jsonObject.addProperty(res.get(i).get(j), res.get(i).get(j + 1));
                }
                jsonArray.add(jsonObject);
            }

            request.getServletContext().log("getting " + jsonArray.size() + " results");
            out.write(jsonArray.toString());
            response.setStatus(200);

            long jdbcEndTime = System.currentTimeMillis(); // End time for TJ
            long jdbcDuration = jdbcEndTime - jdbcStartTime; // Compute elapsed time for TJ

            long servletEndTime = System.currentTimeMillis(); // End time for TS
            long servletDuration = servletEndTime - servletStartTime; // Compute elapsed time for TS

            String logMessage = input + " TS: " + servletDuration + " milliseconds, TJ: " + jdbcDuration + " milliseconds\n";
            System.out.println(logMessage);
            try {
                String contextPath = request.getServletContext().getRealPath("/");

                String xmlFilePath=contextPath+"/80test";

                System.out.println(xmlFilePath);

                File file = new File(xmlFilePath);



                if (file.exists()) {
                    // Append to the existing file
                    writer = new FileWriter(file, true);
                } else {
                    // Create a new file

                    writer = new FileWriter(file);
                }

                writer.write(logMessage);
                writer.close();
            }
            catch (Exception e) {
                System.out.println(e);
            }
        }
        catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());
            response.setStatus(500);
        }
        finally {
            out.close();
        }
    }
}
