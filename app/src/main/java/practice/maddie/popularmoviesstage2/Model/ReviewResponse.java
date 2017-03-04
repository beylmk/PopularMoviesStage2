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
public class ReviewResponse {

    private static String LOG_TAG = ReviewResponse.class.getSimpleName();

    @SerializedName("results")
    private List<Review> results;

    public ReviewResponse() {
        results = new ArrayList();
    }

    public List<Review> getReviews() {
        return results;
    }

    public String toString() {
        String result = "";
        for (Review review : results) {
            result += review.toString();
        }
        return result;
    }

    public static ReviewResponse parseJSON(String response) {

        Log.d(LOG_TAG, "in parseJSON");
        Gson gson = new GsonBuilder().create();

        try
        {
            ReviewResponse reviewResponse = gson.fromJson(response, ReviewResponse.class);
            return reviewResponse;

        }
        catch (IllegalStateException | JsonSyntaxException exception) {
            Log.e(MovieResponse.class.getSimpleName(), exception.getMessage());
            return new ReviewResponse();
        }
    }
}
