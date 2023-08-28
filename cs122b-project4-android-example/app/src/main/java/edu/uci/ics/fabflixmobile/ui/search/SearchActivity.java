package edu.uci.ics.fabflixmobile.ui.search;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import edu.uci.ics.fabflixmobile.data.NetworkManager;
import edu.uci.ics.fabflixmobile.databinding.ActivityLoginBinding;
import edu.uci.ics.fabflixmobile.databinding.ActivitySearchBinding;
import edu.uci.ics.fabflixmobile.ui.movielist.MovieListActivity;
import edu.uci.ics.fabflixmobile.ui.search.SearchActivity;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SearchActivity extends AppCompatActivity {

    private EditText search_bar;
    private Button search_button;

    /*
      In Android, localhost is the address of the device or the emulator.
      To connect to your machine, you need to use the below IP address
     */
    private final String host = "44.209.21.156";
    private final String port = "8443";
    private final String domain = "cs122b";
    private final String baseURL = "https://" + host + ":" + port + "/" + domain;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivitySearchBinding binding = ActivitySearchBinding.inflate(getLayoutInflater());
        // upon creation, inflate and initialize the layout
        setContentView(binding.getRoot());

        search_bar = binding.searchBar;
        search_button = binding.searchButton;

        //assign a listener to call a function to handle the user request when clicking a button
        search_button.setOnClickListener(view -> search());
    }

    @SuppressLint("SetTextI18n")
    public void search() {
        // use the same network queue across our application
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        // request type is POST
        final StringRequest loginRequest = new StringRequest(
                Request.Method.POST,
                baseURL + "/api/index",
                response -> {
                    try {
                            JSONObject jsonResponse = new JSONObject(response);
                            Log.d("search.success", response);
                            //Complete and destroy login activity once successful
                            // initialize the activity(page)/destination
                            Intent MoviePage = new Intent(SearchActivity.this, MovieListActivity.class);
                            // activate the list page.
                            MoviePage.putExtra("sort_title", jsonResponse.getString("sort_title"));
                            startActivity(MoviePage);
                            // Login failed, display an error message
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                },
                error -> {
                    // error
                    Log.d("search.error", error.toString());
                }) {
            @Override
            protected Map<String, String> getParams() {
                // POST request form data
                final Map<String, String> params = new HashMap<>();
                params.put("sort_title", search_bar.getText().toString());
                return params;
            }
        };
        // important: queue.add is where the login request is actually sent
        queue.add(loginRequest);
    }
}