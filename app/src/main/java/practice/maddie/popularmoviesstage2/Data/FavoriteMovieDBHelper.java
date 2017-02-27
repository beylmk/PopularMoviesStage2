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
import practice.maddie.popularmoviesstage2.Data.FavoriteMovieContract.FavoritesEntry;

/**
 * Created by rfl518 on 8/13/16.
 */
public class FavoriteMovieDBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    public static final String DATABASE_NAME = "favorites.db";

    public FavoriteMovieDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a table to hold locations.  A location consists of the string supplied in the
        // location setting, the city name, and the latitude and longitude

        db.execSQL("CREATE TABLE " + FavoritesEntry.TABLE_NAME + " (" + FavoritesEntry.COLUMN_ID + " INTEGER PRIMARY KEY , " +
            FavoritesEntry.COLUMN_TITLE + " TEXT, " + FavoritesEntry.COLUMN_POSTER_PATH + " TEXT, " +
            FavoritesEntry.COLUMN_RATING + " TEXT,  " + FavoritesEntry.COLUMN_RELEASE_DATE + " TEXT, " + FavoritesEntry.COLUMN_SYNOPSIS +
            " TEXT) ");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + FavoritesEntry.TABLE_NAME);
        onCreate(db);
    }

    public boolean contains(Movie movie) {
        return this.contains(movie.getId());
    }

    public boolean contains(long movieId) {
        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();

        // 2. build query
        Cursor cursor = db.rawQuery("SELECT * FROM " + FavoritesEntry.TABLE_NAME + " WHERE " +
            FavoritesEntry.COLUMN_ID + " = " + movieId, null);

        db.close();

        if (cursor != null && cursor.moveToFirst()) {
            return true;
        }

        return false;
    }

}
