package edu.uci.ics.fabflixmobile.ui.singlemovie;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.util.Log;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import edu.uci.ics.fabflixmobile.R;
import edu.uci.ics.fabflixmobile.data.NetworkManager;
import edu.uci.ics.fabflixmobile.databinding.ActivitySinglemovieBinding;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class SingleMovieActivity extends AppCompatActivity {
    private TextView movieTitle;
    private TextView content;
    private TextView stars;

    private final String host = "44.209.21.156";
    private final String port = "8443";
    private final String domain = "cs122b";
    private final String baseURL = "https://" + host + ":" + port + "/" + domain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movielist);
        ActivitySinglemovieBinding binding = ActivitySinglemovieBinding.inflate(getLayoutInflater());
        // upon creation, inflate and initialize the layout
        setContentView(binding.getRoot());

        movieTitle = binding.movieTitle;
        content = binding.content;
        stars = binding.stars;


        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        // request type is POST
        Intent intent = getIntent();
        String movieId = intent.getStringExtra("id");
        @SuppressLint("SetTextI18n") final StringRequest singleMovieRequest = new StringRequest(
                Request.Method.GET,
                baseURL + "/api/single-movie?id=" + movieId,
                response -> {
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        JSONObject jsonResponse = jsonArray.getJSONObject(0);
                        Log.d("single-movie.success", response);
                        movieTitle.setText(jsonResponse.getString("movie_title"));
                        content.setText("Year: " + jsonResponse.getString("movie_year") + "\nDirector: " + jsonResponse.getString("movie_director") + "\nGenres: " + jsonResponse.getString("genre"));
                        stars.setText("Stars: ");
                        stars.append(jsonResponse.getString("star_name"));
                        for (int i = 1; i < jsonArray.length()-1;i++) {
                            stars.append(", ");
                            stars.append(jsonArray.getJSONObject(i).getString("star_name"));}
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }},
                error -> {
                    // error
                    Log.d("single-movie.error", error.toString());
                });
        queue.add(singleMovieRequest);
    }

}