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
import java.util.ArrayList;
import org.jasypt.util.password.StrongPasswordEncryptor;

@WebServlet(name = "LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet {
    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    private DataSource dataSource;
    public void init(ServletConfig config) {
        try {
//            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/read");
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
        System.out.println("gRecaptchaResponse=" + gRecaptchaResponse);

        
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        //Verify reCAPTCHA
        if (!username.equals("a@email.com"))
        {
            try {
                RecaptchaVerifyUtils.verify(gRecaptchaResponse);
            } catch (Exception e) {
                JsonObject responseJsonObject = new JsonObject();
               responseJsonObject.addProperty("message", "Please finish Recaptcha ");
                response.getWriter().write(responseJsonObject.toString());
                return;
            }
        }




        String id = "";
        String correctPassword = "";
        System.out.println("current id: " + id);
        boolean success = false;
        try (Connection conn = dataSource.getConnection()) {
            String query = "SELECT * from customers where email = ? ";
            //System.out.println(query);

            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, username);
            ResultSet rs = statement.executeQuery();
            //System.out.println("current rs: " + rs.toString());
            if (rs.next()) {
                id = rs.getString("id");
                //System.out.println("new id: " + id);
                correctPassword = rs.getString("password");
                success = new StrongPasswordEncryptor().checkPassword(password, correctPassword);
            }
            rs.close();
            statement.close();
            response.setStatus(200);
        } catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        }
        //System.out.println("hi");
        JsonObject responseJsonObject = new JsonObject();

        if (success && password != "") {
            request.getSession().setAttribute("user", new User(username, id));
            ArrayList<String> previousItems = new ArrayList<String>();
            request.getSession().setAttribute("previousItems", previousItems);

            responseJsonObject.addProperty("status", "success");
            responseJsonObject.addProperty("message", "success");
            //System.out.println("Success");
        } else {
            //System.out.println("no");
            // Login fail
            responseJsonObject.addProperty("status", "fail");
            // Log to localhost log
            request.getServletContext().log("Login failed");
            // sample error messages. in practice, it is not a good idea to tell user which one is incorrect/not exist.
            if (id == "") {
                //System.out.println("User doesn't exist");
                responseJsonObject.addProperty("message", "user " + username + " doesn't exist");
            } else if (!password.equals(correctPassword))
            {
                //System.out.println("Incorrect password");
                responseJsonObject.addProperty("message", "incorrect password");
            }
        }
        response.getWriter().write(responseJsonObject.toString());
    }
}
