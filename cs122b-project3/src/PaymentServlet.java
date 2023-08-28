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
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.*;

@WebServlet(name = "PaymentServlet", urlPatterns = "/api/payment")
public class PaymentServlet extends HttpServlet {
    /**
     * @see HttpServlet#doPost(HttpServletRequest, HttpServletResponse)
     */
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
//        HttpSession session = request.getSession();
        PrintWriter out = response.getWriter();
        JsonArray jsonArray = new JsonArray();
        out.write(jsonArray.toString());
        response.setStatus(200);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();

        String id = request.getParameter("cardnumber");
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String expDate = request.getParameter("expDate");
//        System.out.println(id);
//        System.out.println(firstName);
//        System.out.println(lastName);
//        System.out.println(expDate);
        System.out.println("movie list");
        JsonObject responseJsonObject = new JsonObject();

        String correctFirstName = "";
        String correctLastName = "";
        String correctExpDate = "";
        try (Connection conn = dataSource.getConnection()) {
            String query = "SELECT * from creditcards where id = ? ";
            //System.out.println(query);
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, id);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                id = rs.getString("id");
                correctFirstName = rs.getString("firstName");
                correctLastName = rs.getString("lastName");
                correctExpDate = rs.getString("expiration");
            }


            System.out.println();

            if (firstName.equals(correctFirstName) && !correctFirstName.equals("") && lastName.equals(correctLastName) &&
                    expDate.equals(correctExpDate)) {
                System.out.println("Success");
                User user = (User) session.getAttribute("user");
                String customerId = user.getId(); // This will call the overridden toString() method in the User class
                String movieList = request.getParameter("movie");
                String[] movieL = movieList.split(",");

                Set<String> uniqueMovieIds = new HashSet<>();
                for (int i = 1; i < movieL.length; i += 2) {
                    uniqueMovieIds.add(movieL[i].replace("/", ""));
                }
                Map<String,Integer> dict = new HashMap<>();
                for(String movieId : uniqueMovieIds) {
                    String insertQuery = "INSERT INTO sales (customerId, movieId, saleDate) VALUES (?,?,?)";
                    PreparedStatement insertStatement = conn.prepareStatement(insertQuery, new String[]{"salesid"});
                    insertStatement.setString(1, customerId);
                    insertStatement.setString(2, movieId);
                    Date currentDate = new Date(System.currentTimeMillis());
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                    String formattedDate = formatter.format(currentDate);
                    insertStatement.setString(3, formattedDate);
                    insertStatement.executeUpdate();

                    // get the automatically generated salesId
                    ResultSet generatedId = insertStatement.getGeneratedKeys();
                    if(generatedId.next())
                    {
                        int saleId = generatedId.getInt(1);
                        dict.put(movieId,saleId);
                    }
                        //System.out.println("Insert Completed");
                    responseJsonObject.addProperty("status", "success");
                    responseJsonObject.addProperty("message", "success");
                    response.setStatus(200);
                    insertStatement.close();
                }
                System.out.println(dict);
                session.setAttribute("salesId", dict);

            }
             else{
            System.out.println("Failed");
            //System.out.println("no");
            responseJsonObject.addProperty("status", "fail");
            // Log to localhost log
            request.getServletContext().log("Payment failed");
            // sample error messages. in practice, it is not a good idea to tell user which one is incorrect/not exist.
            if (correctExpDate == "") {
                System.out.println("nf");
                responseJsonObject.addProperty("message", "Credit Card " + id + " doesn't exist");
            } else if (!firstName.equals(correctFirstName) || !lastName.equals(correctLastName)) {
                System.out.println("wr");
                responseJsonObject.addProperty("message", "incorrect name");
            } else if (!expDate.equals(correctExpDate)) {
                System.out.println("wd");
                responseJsonObject.addProperty("message", "wrong exp date");
            }
        }
        rs.close();
             statement.close();
    } catch(Exception e)
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

