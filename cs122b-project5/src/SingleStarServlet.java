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

@WebServlet(name = "SingleStarServlet", urlPatterns = "/api/single-star")

public class SingleStarServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/read");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json"); // Response mime type
        String id = request.getParameter("id");
        request.getServletContext().log("getting id: " + id);
        HttpSession session = request.getSession();
        PrintWriter out = response.getWriter();
        try (Connection conn = dataSource.getConnection()) {
            String query = "SELECT * from stars as s, stars_in_movies as sim, movies as m " +
                    "where m.id = sim.movieId and sim.starId = s.id and s.id = ?" +
                    "ORDER BY m.year desc, m.title";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, id);
            ResultSet rs = statement.executeQuery();
            JsonArray jsonArray = new JsonArray();
            while (rs.next()) {
                String starId = rs.getString("starId");
                String starName = rs.getString("name");
                String starDob = rs.getString("birthYear");
                if(starDob == null)
                {
                    starDob = "N/A";
                }

                String movieId = rs.getString("movieId");
                String movieTitle = rs.getString("title");
                String movieYear = rs.getString("year");
                String movieDirector = rs.getString("director");

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("star_id", starId);
                jsonObject.addProperty("star_name", starName);
                jsonObject.addProperty("star_dob", starDob);
                jsonObject.addProperty("movie_id", movieId);
                jsonObject.addProperty("movie_title", movieTitle);
                jsonObject.addProperty("movie_year", movieYear);
                jsonObject.addProperty("movie_director", movieDirector);

                jsonArray.add(jsonObject);
            }

            JsonObject UrljsonObject = new JsonObject();
            String url = (String) session.getAttribute("movie_page");
            UrljsonObject.addProperty("movie_page", url);
            System.out.println(UrljsonObject);
            jsonArray.add(UrljsonObject);

            rs.close();
            statement.close();
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
}
