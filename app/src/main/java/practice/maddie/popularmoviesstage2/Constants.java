package practice.maddie.popularmoviesstage2;

import practice.maddie.popularmoviesstage2.BuildConfig;

/**
 * Created by rfl518 on 7/13/16.
 */
public class Constants {

    public static String BASE_URL = "https://api.themoviedb.org";

    public static final String MOVIE_DB_API_KEY = "api_key=" + BuildConfig.MY_MOVIE_DB_API_KEY; // must be set for every request

    public static final String API_VERSION = "/3";

    public static final String MOVIES_URL = API_VERSION + "/discover/movie?" + MOVIE_DB_API_KEY;

}
