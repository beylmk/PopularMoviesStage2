package practice.maddie.popularmoviesstage2.Model;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rfl518 on 7/13/16.
 */
public class MovieResponse {

    private static String LOG_TAG = MovieResponse.class.getSimpleName();

    @SerializedName("results")
    private List<Movie> results;

    public MovieResponse() {
        results = new ArrayList();
    }

    public List<Movie> getMovies() {
        return results;
    }

    public String toString() {
        String result = "";
        for (Movie movie : results) {
            result += movie.toString();
        }
        return result;
    }

    public static MovieResponse parseJSON(String response) {

        Log.d(LOG_TAG, "in parseJSON");
        Gson gson = new GsonBuilder().create();

        try
        {
            MovieResponse movieResponse = gson.fromJson(response, MovieResponse.class);
            return movieResponse;

        }
        catch (IllegalStateException | JsonSyntaxException exception) {
            Log.e(MovieResponse.class.getSimpleName(), exception.getMessage());
            return new MovieResponse();
        }
    }
}
