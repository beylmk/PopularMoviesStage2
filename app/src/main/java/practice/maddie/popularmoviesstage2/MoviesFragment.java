package practice.maddie.popularmoviesstage2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.lb.material_preferences_library.custom_preferences.ListPreference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import practice.maddie.popularmoviesstage2.Data.FavoriteMovieProvider;
import practice.maddie.popularmoviesstage2.Model.Movie;
import practice.maddie.popularmoviesstage2.Model.MovieResponse;
import practice.maddie.popularmoviesstage2.Model.Movies;
import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Fragment holding GridView of movies returned from The Movie Database API
 */
public class MoviesFragment extends Fragment {

    private final String LOG_TAG = MoviesFragment.class.getSimpleName();

    private MoviesAdapter mAdapter;

    private GridView mMovieGrid;

    private OnMovieClickListener listener;

    public MoviesFragment() {
        mAdapter = new MoviesAdapter();
    }

    public static MoviesFragment newInstance() {
        MoviesFragment fragment = new MoviesFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_movies, container, false);

        if (!MainActivity.getIsTablet()) {
            getActivity().setTitle(getString(R.string.popular_movies));
        }

        mMovieGrid = (GridView) rootView.findViewById(R.id.gridview_movies);
        mMovieGrid.setAdapter(mAdapter);
        mMovieGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Movie movie = mAdapter.getItem(position);

                //load movie detail fragment
                listener.onMovieClick(movie.getId());

            }
        });

        listener = (OnMovieClickListener) getActivity();

        return rootView;
    }

    private class MoviesAdapter extends BaseAdapter {

        List<Movie> adapterMovies;

        MoviesAdapter() {
            adapterMovies = Movies.getMovies();
        }

        @Override
        public int getCount() {
            return adapterMovies.size();
        }

        @Override
        public Movie getItem(int position) {
            return adapterMovies.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View gridItem;

            Movie tempMovie = Movies.getMovies().get(position);

            if (convertView == null) {
                gridItem = LayoutInflater.from(getActivity()).inflate(R.layout.grid_item_movie, parent, false);
            } else {
                gridItem = convertView;
            }
            ImageView moviePoster = (ImageView) gridItem.findViewById(R.id.grid_item_movie_poster);

            int width = mMovieGrid.getMeasuredWidth() / 3;
            moviePoster.setScaleType(ImageView.ScaleType.FIT_CENTER);
            moviePoster.setMinimumWidth(width);
            moviePoster.setMinimumHeight(width);

            if(tempMovie.getPosterPath() != null) {
                Picasso.with(getActivity()).load("http://image.tmdb.org/t/p/w185" + tempMovie.getPosterPath()).into(moviePoster);
            } else {
                Bitmap noPoster = BitmapFactory.decodeResource(getResources(), R.drawable.no_poster);
                //TODO: get this to look cleaner
                // moviePoster.setImageBitmap(noPoster);
            }

            return gridItem;
        }

        public void clear() {
            adapterMovies.clear();
        }

        public void add(Movie movie) {
            adapterMovies.add(movie);
        }

        public void addAll(List<Movie> inMovies) {
            for (Movie movie : inMovies) {
                add(movie);
            }
        }

    }

    public interface MovieEndpointInterface {

        @GET(Constants.MOVIES_URL)
        Call<MovieResponse> getMovies(@Query("sort_by") String sort);
    }

}