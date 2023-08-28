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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
@WebServlet(name = "ShoppingCartServlet", urlPatterns = "/api/shopping-cart")
public class ShoppingCartServlet extends HttpServlet {

    private static final long serialVersionUID = 2L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json"); // Response mime type
        HttpSession session = request.getSession();
        PrintWriter out = response.getWriter();
//        int number = Integer.parseInt(request.getParameter("number"));
//        session.setAttribute("count", number);
//
//        ArrayList<String> items = (ArrayList<String>) session.getAttribute("previousItems");
//        Collections.sort(items);
//        int count = 1;
        JsonArray jsonArray = new JsonArray();
////        System.out.println(items);
//        for (int i= 0; i < items.size() - 1; i++) {
//            if (Objects.equals(items.get(i), items.get(i + 1))) {
//                count ++;
//            }
//            else {
//                JsonObject jsonObject = new JsonObject();
//                jsonObject.addProperty("movie_title", items.get(i));
//                jsonObject.addProperty("count", count);
//                count = 1;
//                jsonArray.add(jsonObject);
//            }
//        }
//        JsonObject jsonObject = new JsonObject();
//        jsonObject.addProperty("movie_title", items.get(items.size()-1));
//        jsonObject.addProperty("count", count);
//        jsonArray.add(jsonObject);
//        int total_price = 10 * items.size();
//        session.setAttribute("total", total_price);
////
////        JsonObject pricejsonObject = new JsonObject();
//        pricejsonObject.addProperty("totalPrice", total_price);
//        System.out.println(pricejsonObject);
//        jsonArray.add(pricejsonObject);

        JsonObject UrljsonObject = new JsonObject();
        String url = (String) session.getAttribute("movie_page");
        UrljsonObject.addProperty("movie_page", url);
        System.out.println(UrljsonObject);
        jsonArray.add(UrljsonObject);


        out.write(jsonArray.toString());
        response.setStatus(200);
    }

}
