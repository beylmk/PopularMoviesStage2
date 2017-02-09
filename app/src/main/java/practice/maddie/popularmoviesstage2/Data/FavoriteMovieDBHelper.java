package practice.maddie.popularmoviesstage2.Data;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.text.TextUtils;

import practice.maddie.popularmoviesstage2.Model.Movie;
import practice.maddie.popularmoviesstage2.Model.Movies;

/**
 * Created by rfl518 on 8/13/16.
 */
public class FavoriteMovieDBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "favorites.db";

    public FavoriteMovieDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public static long addFavoriteMovie(long movieId, Context context) {

        Movie movie = Movies.getById(movieId);
        long locationId;

        // First, check if the location with this city name exists in the db
        Cursor movieCursor = context.getContentResolver().query(
                FavoriteMovieContract.FavoritesEntry.CONTENT_URI,
                new String[]{FavoriteMovieContract.FavoritesEntry._ID},
                FavoriteMovieContract.FavoritesEntry.COLUMN_ID + " = ?",
                new String[]{Long.toString(movieId)},
                null);

        if (movieCursor != null && movieCursor.moveToFirst()) {
            int locationIdIndex = movieCursor.getColumnIndex(FavoriteMovieContract.FavoritesEntry._ID);
            locationId = movieCursor.getLong(locationIdIndex);
        } else {
            // Now that the content provider is set up, inserting rows of data is pretty simple.
            // First create a ContentValues object to hold the data you want to insert.
            ContentValues movieValues = new ContentValues();

            // Then add the data, along with the corresponding name of the data type,
            // so the content provider knows what kind of value is being inserted.
            movieValues.put(FavoriteMovieContract.FavoritesEntry.COLUMN_ID, movieId);
            movieValues.put(FavoriteMovieContract.FavoritesEntry.COLUMN_TITLE, movie.getTitle());
            movieValues.put(FavoriteMovieContract.FavoritesEntry.COLUMN_POSTER_PATH, movie.getPosterPath());
            movieValues.put(FavoriteMovieContract.FavoritesEntry.COLUMN_RATING, movie.getVoteAverage());
            movieValues.put(FavoriteMovieContract.FavoritesEntry.COLUMN_RELEASE_DATE, movie.getReleaseDate());
            movieValues.put(FavoriteMovieContract.FavoritesEntry.COLUMN_SYNOPSIS, movie.getOverview());

            // Finally, insert location data into the database.
            Uri insertedUri = context.getContentResolver().insert(
                    FavoriteMovieContract.FavoritesEntry.CONTENT_URI,
                    movieValues
            );

            // The resulting URI contains the ID for the row.  Extract the locationId from the Uri.
            locationId = ContentUris.parseId(insertedUri);
        }

        movieCursor.close();
        // Wait, that worked?  Yes!
        return locationId;
    }

    public static long removeFavoriteMovie(long movieId, Context context) {

        long locationId;
        int numDeleted = 0;

        // First, check if the location with this city name exists in the db
        Cursor movieCursor = context.getContentResolver().query(
                FavoriteMovieContract.FavoritesEntry.CONTENT_URI,
                new String[]{FavoriteMovieContract.FavoritesEntry._ID},
                FavoriteMovieContract.FavoritesEntry.COLUMN_ID + " = ?",
                new String[]{Long.toString(movieId)},
                null);

        if (movieCursor != null && movieCursor.moveToFirst()) {
            int locationIdIndex = movieCursor.getColumnIndex(FavoriteMovieContract.FavoritesEntry._ID);
            locationId = movieCursor.getLong(locationIdIndex);
        } else {

            String[] selectionArgs = new String[]{};
            String selection = "";

            if (!TextUtils.isEmpty(Long.toString(movieId))) {
                selection = FavoriteMovieProvider.sMovieSelection;
                selectionArgs = new String[]{Long.toString(movieId)};
            }

            numDeleted = context.getContentResolver().delete(
                    FavoriteMovieContract.FavoritesEntry.CONTENT_URI,
                    selection,
                    selectionArgs
            );

        }

        movieCursor.close();
        // Wait, that worked?  Yes!
        return numDeleted;
    }

}
