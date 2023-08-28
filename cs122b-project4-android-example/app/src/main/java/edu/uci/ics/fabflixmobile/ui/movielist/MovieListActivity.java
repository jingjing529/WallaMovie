package edu.uci.ics.fabflixmobile.ui.movielist;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import edu.uci.ics.fabflixmobile.R;
import edu.uci.ics.fabflixmobile.data.NetworkManager;
import edu.uci.ics.fabflixmobile.data.model.Movie;
import edu.uci.ics.fabflixmobile.databinding.ActivityLoginBinding;
import edu.uci.ics.fabflixmobile.databinding.ActivityMovielistBinding;
import edu.uci.ics.fabflixmobile.ui.login.LoginActivity;
import edu.uci.ics.fabflixmobile.ui.search.SearchActivity;
import edu.uci.ics.fabflixmobile.ui.singlemovie.SingleMovieActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MovieListActivity extends AppCompatActivity {
    private Button prev;
    private Button next;
    private static int page = 1;

    private final String host = "44.209.21.156";
    private final String port = "8443";
    private final String domain = "cs122b";
    private final String baseURL = "https://" + host + ":" + port + "/" + domain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movielist);
        ActivityMovielistBinding binding = ActivityMovielistBinding.inflate(getLayoutInflater());
        // upon creation, inflate and initialize the layout
        setContentView(binding.getRoot());

        prev = binding.prev;
        next = binding.next;

        //assign a listener to call a function to handle the user request when clicking a button
        prev.setOnClickListener(view -> updatePage(-1));
        next.setOnClickListener(view -> updatePage(1));

        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        // request type is POST
        Intent intent = getIntent();
        String movieTitle = intent.getStringExtra("sort_title");
        prev.setEnabled(page > 1);
        final StringRequest movieListRequest = new StringRequest(
                Request.Method.GET,
                baseURL + "/api/movie-list?num=20&page="+ page +"&sort=r0t1&input=title:" + movieTitle,
                response -> {
                    try {
                        JSONArray jsonResponse = new JSONArray(response);
                        next.setEnabled(jsonResponse.length() >= 20);
                        Log.d("movie-list.success", String.valueOf(response));
                            final ArrayList<Movie> movies = new ArrayList<>();
                            for (int i = 0; i < jsonResponse.length();i++) {
                                JSONObject object = jsonResponse.getJSONObject(i);
                                String[] Genres = {object.getString("genre1"), object.getString("genre2"), object.getString("genre3")};
                                String[] Stars = {object.getString("star1")};
                                if (object.length()>12){
                                    Stars = new String[]{object.getString("star1"), object.getString("star2"), object.getString("star3")};
                                }
                                else if (object.length()>10){
                                    Stars = new String[]{object.getString("star1"), object.getString("star2")};
                                }
                                movies.add(new Movie(object.getString("id"), object.getString("title"), (short) object.getInt("year"), object.getString("director"), Genres, Stars));
                            }
                        MovieListViewAdapter adapter = new MovieListViewAdapter(this, movies);
                            ListView listView = findViewById(R.id.list);
                            listView.setAdapter(adapter);
                            listView.setOnItemClickListener((parent, view, position, id) -> {
                                Movie movie = movies.get(position);
                            Intent singleMoviePage = new Intent(MovieListActivity.this, SingleMovieActivity.class);
                            // activate the list page.
                                singleMoviePage.putExtra("id", movie.getId());
                            startActivity(singleMoviePage);});
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }},
    error -> {
        // error
        Log.d("movie-list.error", error.toString());
    });
        queue.add(movieListRequest);
}

    void updatePage(int pageValue) {
        page += pageValue;
        finish();
        startActivity(getIntent());
        // The page will reload, so there is no need to call any function to update
    }

}