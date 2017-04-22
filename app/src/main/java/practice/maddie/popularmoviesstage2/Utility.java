package practice.maddie.popularmoviesstage2;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import practice.maddie.popularmoviesstage2.Data.FavoriteMovieContract;
import practice.maddie.popularmoviesstage2.Data.FavoriteMovieProvider;
import practice.maddie.popularmoviesstage2.Model.Movie;

/**
 * Created by rfl518 on 7/14/16.
 */
public class Utility {

    public static String getPreferredSortOrder(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_sort_key),
            context.getString(R.string.pref_sort_default));
    }

    public static String getPreferredSortOrderLabel(Context context) {
        String sortOrder = getPreferredSortOrder(context);
        List<String> sortOrderValues = Arrays.asList(context.getResources().getStringArray(R.array.sort_order_values));
        int sortIndex = sortOrderValues.indexOf(sortOrder);
        return (context.getResources().getStringArray(R.array.sort_order_label_values))[sortIndex];

    }

    public static void setPreferredSortOrder(Context context, String mSortPreference) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putString(context.getString(R.string.pref_sort_key), mSortPreference).commit();
    }

    public static List<Movie> getFavorites(Context context) {
        Cursor cursor = FavoriteMovieProvider.getFavorites(FavoriteMovieContract.FavoritesEntry.CONTENT_URI,
            null, getPreferredSortOrder(context));

        List<Movie> favoriteMovies = new ArrayList<>();

        if (cursor != null && cursor.moveToFirst()) {
            Movie currentMovie;
            do {
                currentMovie = new Movie();
                currentMovie.setId(Integer.parseInt(cursor.getString(0)));
                currentMovie.setTitle(cursor.getString(1));
                currentMovie.setPosterPath(cursor.getString(2));
                currentMovie.setVoteAverage(Float.parseFloat(cursor.getString(3)));
                currentMovie.setReleaseDate(cursor.getString(4));
                currentMovie.setOverview(cursor.getString(5));

                favoriteMovies.add(currentMovie);
            } while (cursor.moveToNext());
        }

        return favoriteMovies;

    }
}
