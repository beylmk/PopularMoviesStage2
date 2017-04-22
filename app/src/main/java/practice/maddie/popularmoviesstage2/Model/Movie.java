package practice.maddie.popularmoviesstage2.Model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.squareup.okhttp.internal.Util;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import practice.maddie.popularmoviesstage2.Utility;

/**
 * Created by rfl518 on 7/13/16.
 */
public class Movie {

    private final String LOG_TAG = Movie.class.getSimpleName();

    private long id;
    private String title;
    private String poster_path;
    private double vote_average;
    private double popularity;
    private String overview;
    private String release_date;
    private Bitmap bitmap;
    private boolean isFavorite;

    public Movie() {}

    public Movie(long id, String title, String poster_path, double vote_average, long popularity, String overview, String release_date) {
        this.id = id;
        this.title = title;
        this.poster_path = poster_path;
        this.vote_average = vote_average;
        this.popularity = popularity;
        this.overview = overview;
        this.release_date = release_date;
        this.bitmap = getPosterBitmapFromString(poster_path);
        this.isFavorite = false;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPosterPath() {
        return poster_path;
    }

    public void setPosterPath(String poster_path) {
        this.poster_path = poster_path;
    }

    public double getVoteAverage() {
        return vote_average;
    }

    public void setVoteAverage(double vote_average) {
        this.vote_average = vote_average;
    }

    public double getPopularity() {
        return popularity;
    }

    public void setPopularity(long popularity) {
        this.popularity = popularity;
    }

    public String getReleaseDate() {
        if (release_date.isEmpty()) {
            return null;
        }

        try {
            return new SimpleDateFormat("MMMM dd, yyyy", Locale.US)
                    .format(new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(release_date));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void setReleaseDate(String release_date) {
        this.release_date = release_date;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public Bitmap getPosterBitmapFromString(String path){

        if(path == null) {
            return null;
        } else {
            String urlString = "https://image.tmdb.org/t/p/w185" + path;
            Bitmap poster = null;

            try {
                URL url = new URL(urlString);
                HttpURLConnection connection  = (HttpURLConnection) url.openConnection();
                InputStream is = connection.getInputStream();
                poster = BitmapFactory.decodeStream(is);
            } catch (Exception e) {
                Log.e(LOG_TAG, e.toString());
            }

            return poster;
        }
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public String toString() {
        return getTitle() + " " + getVoteAverage() + " " + getPopularity();
    }
}

