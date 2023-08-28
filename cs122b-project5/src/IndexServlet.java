import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet(name = "IndexServlet", urlPatterns = "/api/index")

public class IndexServlet extends HttpServlet {
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
//        String genre = request.getParameter("genre");
//        request.getServletContext().log("getting genre: " + genre);
        PrintWriter out = response.getWriter();
        try (Connection conn = dataSource.getConnection()) {
            String query = "SELECT distinct name from genres order by name asc";
            PreparedStatement statement = conn.prepareStatement(query);
//            statement.setString(1, genre);
            ResultSet rs = statement.executeQuery();
            JsonArray jsonArray = new JsonArray();
            while (rs.next()) {
                String name = rs.getString("name");

                JsonObject jsonObject_genre = new JsonObject();
                jsonObject_genre.addProperty("genre", name);

                jsonArray.add(jsonObject_genre);
            }
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

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String sort_title = request.getParameter("sort_title");
        System.out.println(sort_title);
        String sort_year = request.getParameter("sort_year");
        String sort_director = request.getParameter("sort_director");
        String sort_name = request.getParameter("sort_name");
        JsonObject JsonObject = new JsonObject();
        JsonObject.addProperty("sort_title", sort_title);
        JsonObject.addProperty("sort_year", sort_year);
        JsonObject.addProperty("sort_director", sort_director);
        JsonObject.addProperty("sort_name", sort_name);
        System.out.println(JsonObject.toString());
        response.getWriter().write(JsonObject.toString());
    }
}
