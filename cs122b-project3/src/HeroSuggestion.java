import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.http.HttpSession;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

@WebServlet("/hero-suggestion")
public class HeroSuggestion extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private DataSource dataSource;
	public void init(ServletConfig config) {
		try {
			dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
		} catch (NamingException e)
		{
			e.printStackTrace();
		}
	}

    /*
     * 
     * Match the query against superheroes and return a JSON response.
     * 
     * For example, if the query is "super":
     * The JSON response look like this:
     * [
     * 	{ "value": "Superman", "data": { "heroID": 101 } },
     * 	{ "value": "Supergirl", "data": { "heroID": 113 } }
     * ]
     * 
     * The format is like this because it can be directly used by the 
     *   JSON auto complete library this example is using. So that you don't have to convert the format.
     *   
     * The response contains a list of suggestions.
     * In each suggestion object, the "value" is the item string shown in the dropdown list,
     *   the "data" object can contain any additional information.
     * 
     * 
     */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		try {
			// setup the response json arrray
			JsonArray jsonArray = new JsonArray();

			// get the query string from parameter
			String query = request.getParameter("query").trim();
			//System.out.println(query);
			// return the empty json array if query is null or empty
			if (query == null || query.trim().isEmpty()) {
				response.getWriter().write(jsonArray.toString());
				return;
			}
			//System.out.println("Hi");
			Connection conn = dataSource.getConnection();
			//System.out.println(query);
			String SQLQuery = "SELECT * FROM movies WHERE MATCH(title) AGAINST (? IN BOOLEAN MODE)"
					+ "OR title LIKE ? COLLATE utf8mb4_general_ci "
					+ " OR edth(title, ?, 2) =1 limit 10";
			PreparedStatement statement = conn.prepareStatement(SQLQuery);
			String temp = "+" + String.join("*+", query.split(" ")) + "*";
			statement.setString(1, temp);
			String temp2 = "%" + query + "%";
			statement.setString(2,temp2);
			statement.setString(3, query);
			ResultSet rs = statement.executeQuery();

			while (rs.next())
				{
					String id = rs.getString("id");
					String title = rs.getString("title");
					jsonArray.add(generateJsonObject(id, title));
				}
				rs.close();
				statement.close();
				conn.close();
			//System.out.println(query);
			response.getWriter().write(jsonArray.toString());
		} catch (Exception e) {
			System.out.println(e);
			response.sendError(500, e.getMessage());
		}

	}
	
	/*
	 * Generate the JSON Object from hero to be like this format:
	 * {
	 *   "value": "Iron Man",
	 *   "data": { "heroID": 11 }
	 * }
	 * 
	 */
	private static JsonObject generateJsonObject(String heroID, String heroName) {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("value", heroName);
		
		JsonObject additionalDataJsonObject = new JsonObject();
		additionalDataJsonObject.addProperty("heroID", heroID);
		
		jsonObject.add("data", additionalDataJsonObject);
		return jsonObject;
	}
}
