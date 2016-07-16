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
public class TrailerResponse {

    private static String LOG_TAG = TrailerResponse.class.getSimpleName();

    @SerializedName("results")
    private List<Trailer> results;

    public TrailerResponse() {
        results = new ArrayList();
    }

    public List<Trailer> getTrailers() {
        return results;
    }

    public String toString() {
        String result = "";
        for (Trailer trailer : results) {
            result += trailer.toString();
        }
        return result;
    }

    public static TrailerResponse parseJSON(String response) {

        Log.d(LOG_TAG, "in parseJSON");
        Gson gson = new GsonBuilder().create();

        try
        {
            TrailerResponse trailerResponse = gson.fromJson(response, TrailerResponse.class);
            return trailerResponse;

        }
        catch (IllegalStateException | JsonSyntaxException exception) {
            Log.e(MovieResponse.class.getSimpleName(), exception.getMessage());
            return new TrailerResponse();
        }
    }
}
