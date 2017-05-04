package practice.maddie.popularmoviesstage2;

import android.content.Intent;
import android.content.res.Configuration;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.facebook.stetho.Stetho;

import practice.maddie.popularmoviesstage2.Model.Movie;
import practice.maddie.popularmoviesstage2.Model.MovieResponse;
import practice.maddie.popularmoviesstage2.Model.Movies;
import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

public class MainActivity extends AppCompatActivity implements OnMovieClickListener {

    private MoviesFragment moviesFragment;

    private MovieDetailFragment movieDetailFragment;

    private long selectedMovie = 0;

    private static boolean isTablet = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Stetho.initializeWithDefaults(this);

        isTablet = (getResources().getConfiguration().screenLayout
            & Configuration.SCREENLAYOUT_SIZE_MASK)
            >= Configuration.SCREENLAYOUT_SIZE_LARGE;

        setContentView(R.layout.activity_main);

        if (Movies.getMovies().size() == 0) {
            getMovies();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        String currentSelection = Utility.getPreferredSortOrderLabel(this);

        String itemTitle = item.getTitle().toString();

        if (TextUtils.equals(currentSelection, itemTitle)) {
            Toast.makeText(this, getString(R.string.same_selection), Toast.LENGTH_LONG).show();
            return false;
        }

        String sortOrderValue = "";
        switch (item.getItemId()) {
            case R.id.action_sort_by_popularity:
                sortOrderValue = getString(R.string.pref_sort_popularity);
                break;
            case R.id.action_sort_by_rating:
                sortOrderValue = getString(R.string.pref_sort_rating);
                break;
            case R.id.action_show_favorites:
                sortOrderValue = getString(R.string.pref_show_favorites);
            default:
                break;
        }

        Utility.setPreferredSortOrder(this, sortOrderValue);
        getMovies();

        return true;
    }

    public void getMovies() {

        String sortPref = Utility.getPreferredSortOrder(this);

        if (sortPref != getString(R.string.pref_show_favorites)) {

            Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

            MoviesFragment.MovieEndpointInterface endpoints = retrofit.create(MoviesFragment.MovieEndpointInterface.class);
            Call<MovieResponse> call = endpoints.getMovies(sortPref);

            call.enqueue(new Callback<MovieResponse>() {

                @Override
                public void onResponse(Response response) {

                    if (response == null && response.isSuccess()) {
                        return;
                    }

                    MovieResponse movieResponse = (MovieResponse) response.body();

                    Movies.clear(); // clear before populating

                    if (movieResponse == null)
                        return;

                    Movies.addAll(movieResponse);
                    selectedMovie = Movies.get(0).getId();
                    reloadMovieFragment();
                }

                @Override
                public void onFailure(Throwable t) {
                    Toast.makeText(MainActivity.this, getString(R.string.connection_error), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            Movies.setMovies(Utility.getFavorites(this));
            selectedMovie = Movies.get(0).getId();
            if (Movies.getMovies().size() == 0 ) {
                Toast.makeText(this, getString(R.string.no_favorited_movies), Toast.LENGTH_LONG).show();
            }
            reloadMovieFragment();
        }

    }

    private void reloadMovieFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        moviesFragment = MoviesFragment.newInstance();
        fragmentManager.beginTransaction().replace(R.id.movies_fragment, moviesFragment).commit();

        if (isTablet) {
            loadMovieDetailFragment(selectedMovie != 0 ? selectedMovie : Movies.get(0).getId());
        }
    }

        private void loadMovieDetailFragment(long movieId) {
        if (movieId != 0) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            movieDetailFragment = MovieDetailFragment.newInstance(Movies.getById(movieId));
            fragmentManager.beginTransaction().replace(R.id.movie_detail_fragment, movieDetailFragment).commit();
        }
    }

    public static boolean getIsTablet() {
        return isTablet;
    }

    @Override
    public void onMovieClick(long id) {
        selectedMovie = id;

        if (isTablet) {
            loadMovieDetailFragment(id);
        } else {
            Intent intent = new Intent(this, MovieDetailActivity.class);
            intent.putExtra(Constants.MOVIE_ID_EXTRA_KEY, id);
            startActivity(intent);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle onSaveInstanceState) {
        super.onSaveInstanceState(onSaveInstanceState);
        onSaveInstanceState.putLong(Constants.SELECTED_MOVIE_ID_KEY, selectedMovie);
    }

    @Override
    public void onRestoreInstanceState(Bundle onSaveInstanceState) {
        super.onRestoreInstanceState(onSaveInstanceState);
        selectedMovie = onSaveInstanceState.getLong(Constants.SELECTED_MOVIE_ID_KEY);
    }

}

