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
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Objects;

@WebServlet(name = "DashboardServlet", urlPatterns = "/_dashboard/api/dashboard")

public class DashboardServlet extends HttpServlet {
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
//        String genre = request.getParameter("genre");
//        request.getServletContext().log("getting genre: " + genre);
        PrintWriter out = response.getWriter();
        try (Connection conn = dataSource.getConnection()) {
            String all_table_query ="SELECT \n" +
                    "DISTINCT\n" +
                    "TABLE_NAME\n" +
                    "FROM INFORMATION_SCHEMA.COLUMNS\n" +
                    "WHERE TABLE_SCHEMA = \"moviedb\"";
            PreparedStatement query_statement = conn.prepareStatement(all_table_query);
            ResultSet all_table_query_rs = query_statement.executeQuery();
            JsonArray table_jsonArray = new JsonArray();
            while (all_table_query_rs.next()) {
                String TABLE_NAME = all_table_query_rs.getString("TABLE_NAME");
                String query = "SELECT \n" +
                        "COLUMN_NAME, \n" +
                        "DATA_TYPE \n" +
                        "FROM INFORMATION_SCHEMA.COLUMNS\n" +
                        "where TABLE_NAME = ? and TABLE_SCHEMA = \"moviedb\";";
                PreparedStatement statement = conn.prepareStatement(query);
                statement.setString(1, TABLE_NAME);
                ResultSet rs = statement.executeQuery();
                JsonArray jsonArray = new JsonArray();
                while (rs.next()) {
                    JsonObject jsonObject_data = new JsonObject();
                    jsonObject_data.addProperty("TABLE_NAME", TABLE_NAME);
                    String COLUMN_NAME = rs.getString("COLUMN_NAME");
                    String DATA_TYPE = rs.getString("DATA_TYPE");

                    jsonObject_data.addProperty("COLUMN_NAME", COLUMN_NAME);
                    jsonObject_data.addProperty("DATA_TYPE", DATA_TYPE);

                    jsonArray.add(jsonObject_data);
                }
                rs.close();
                statement.close();
                table_jsonArray.add(jsonArray);
            }
//            System.out.println(table_jsonArray.toString());
            all_table_query_rs.close();
            query_statement.close();
            out.write(table_jsonArray.toString());
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
        JsonObject responseJsonObject = new JsonObject();
        String star_name = request.getParameter("star_name");
        String star_year = request.getParameter("star_year");
        String movie_title = request.getParameter("movie_title");
        String movie_year = request.getParameter("movie_year");
        String movie_director = request.getParameter("movie_director");
        String movie_star = request.getParameter("movie_star");
        String movie_genre = request.getParameter("movie_genre");
        try (Connection conn = dataSource.getConnection()) {
//            System.out.println(new_ID);
            if (star_name != null) {
                String query = "SELECT max(id) as maxID from stars";
//                System.out.println(query);
                PreparedStatement statement = conn.prepareStatement(query);
                ResultSet rs = statement.executeQuery();
                rs.next();
                String maxID = rs.getString(1);
                int maxIDnum = Integer.parseInt(maxID.replace("nm", ""));
                String new_ID = "nm" + (maxIDnum + 1);
//                System.out.println(new_ID);
                rs.close();
                statement.close();
                String StarQuery = "INSERT INTO stars (id, name, birthYear) VALUES (?,?,?)";
                PreparedStatement insertStatement = conn.prepareStatement(StarQuery);
                insertStatement.setString(1, new_ID);
                insertStatement.setString(2, star_name);
                if (star_year.length()<1){
                    insertStatement.setString(3, null);
                }
                else{
                insertStatement.setString(3, star_year);}
//                System.out.println(StarQuery);
                responseJsonObject.addProperty("status", "success");
                responseJsonObject.addProperty("message", "successfully added " + star_name + ", star id: " + new_ID);
                insertStatement.executeUpdate();
                insertStatement.close();
            }
            else if (movie_title != null){
                System.out.println("movie");
                System.out.println(conn);
                CallableStatement CallableStatement = conn.prepareCall("{CALL add_movie(?, ?, ?, ?, ?, ?)}");
                System.out.println(CallableStatement);
                CallableStatement.setString(1, movie_title);
                CallableStatement.setString(2, movie_director);
                CallableStatement.setString(3, movie_year);
                CallableStatement.setString(4, movie_genre);
                CallableStatement.setString(5, movie_star);
                System.out.println(movie_title);
                CallableStatement.registerOutParameter(6, Types.VARCHAR);
                CallableStatement.execute();
                String message = CallableStatement.getString(6);
                System.out.println(message);
                CallableStatement.close();
                responseJsonObject.addProperty("message", message);
            }
            conn.close();
            response.setStatus(200);
        }

        catch(Exception e)
        {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        }
        response.getWriter().write(responseJsonObject.toString());

    }
}
