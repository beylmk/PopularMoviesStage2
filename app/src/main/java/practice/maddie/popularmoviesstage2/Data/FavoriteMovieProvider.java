package practice.maddie.popularmoviesstage2.Data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import practice.maddie.popularmoviesstage2.Model.Movie;
import practice.maddie.popularmoviesstage2.Model.Movies;

/**
 * Created by rfl518 on 8/13/16.
 */
public class FavoriteMovieProvider extends ContentProvider {

    public final String TAG = this.getClass().getSimpleName();

    static final int FAVORITES = 100;
    static final int FAVORITE_MOVIE = 101;

    private FavoriteMovieDBHelper mHelper;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private static final SQLiteQueryBuilder favoriteMoviesQueryBuilder;

    static {
        favoriteMoviesQueryBuilder = new SQLiteQueryBuilder();

        favoriteMoviesQueryBuilder.setTables(
                FavoriteMovieContract.FavoritesEntry.TABLE_NAME);
    }

    public static final String sMovieSelection =
            FavoriteMovieContract.FavoritesEntry.TABLE_NAME +
                    "." + FavoriteMovieContract.FavoritesEntry.COLUMN_ID + " = ? ";

    @Override
    public boolean onCreate() {
        mHelper = new FavoriteMovieDBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "favorites"
            case FAVORITES:
            {
                retCursor = getFavorites(uri, projection, sortOrder);
                break;
            }
            // "favorites/*"
            case FAVORITE_MOVIE: {

                retCursor = getFavoriteMovie(uri, projection, sortOrder);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case FAVORITES:
                return FavoriteMovieContract.FavoritesEntry.CONTENT_TYPE;
            case FAVORITE_MOVIE:
                return FavoriteMovieContract.FavoritesEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {

        final SQLiteDatabase db = mHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case FAVORITES: {

                Log.v(TAG, "inserting favorites database with content provider");

                long _id = db.insert(FavoriteMovieContract.FavoritesEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = FavoriteMovieContract.FavoritesEntry.buildFavoritesUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        final SQLiteDatabase db = mHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if ( null == selection ) selection = "1";
        switch (match) {
            case FAVORITES:

                Log.v(TAG, "deleting from favorites database with content provider");

                rowsDeleted = db.delete(
                        FavoriteMovieContract.FavoritesEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case FAVORITES:

                Log.v(TAG, "updating favorites database with content provider");
                rowsUpdated = db.update(FavoriteMovieContract.FavoritesEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    static UriMatcher buildUriMatcher() {
        // I know what you're thinking.  Why create a UriMatcher when you can use regular
        // expressions instead?  Because you're not crazy, that's why.

        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = FavoriteMovieContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, FavoriteMovieContract.PATH_FAVORITE_MOVIES, FAVORITES);
        matcher.addURI(authority, FavoriteMovieContract.PATH_FAVORITE_MOVIES + "/*", FAVORITE_MOVIE);

        return matcher;
    }

    private Cursor getFavorites(
            Uri uri, String[] projection, String sortOrder) {

        Log.v(TAG, "getting favorites from provider");

        return favoriteMoviesQueryBuilder.query(mHelper.getReadableDatabase(),
                projection,
                null,
                null,
                null,
                null,
                sortOrder
        );

    }

    private Cursor getFavoriteMovie(
            Uri uri, String[] projection, String sortOrder) {

        Log.v(TAG, "getting favorite movie from provider");

        String id = FavoriteMovieContract.FavoritesEntry.getIdFromUri(uri);
        String[] selectionArgs = new String[]{};
        String selection = "";

        if (!TextUtils.isEmpty(id)) {
            selection = sMovieSelection;
            selectionArgs = new String[]{id};
        }

        return favoriteMoviesQueryBuilder.query(mHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

}
