package practice.maddie.popularmoviesstage2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

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

    private View mPageLoading;

    private String mSortPreference;

    public MoviesFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mSortPreference = Utility.getPreferredSortOrder(getActivity());

        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(
                new SharedPreferences
                        .OnSharedPreferenceChangeListener() {
                    @Override
                    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                        Movies.sortMovies(mSortPreference);
                    }
                });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_movies, container, false);

        mPageLoading = rootView.findViewById(R.id.page_loading);
        mPageLoading.bringToFront();

        mMovieGrid = (GridView) rootView.findViewById(R.id.gridview_movies);
        mMovieGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Movie movie = mAdapter.getItem(position);
                Intent intent = new Intent(getActivity(), MovieDetailActivity.class)
                        .putExtra("movie Id", movie.getId());
                startActivity(intent);
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        String currentPref = Utility.getPreferredSortOrder(getActivity());
        if (!TextUtils.isEmpty(currentPref) && !TextUtils.equals(currentPref, mSortPreference) & mAdapter != null) {
            Movies.sortMovies(currentPref);
            mAdapter.notifyDataSetChanged();
            mSortPreference = currentPref;
        }
        //TODO implement loaders and get rid of this
        updateMovies();
        super.onResume();
    }

    public void updateMovies() {

        mPageLoading.setVisibility(View.VISIBLE);
        mMovieGrid.setClickable(false);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortPref = prefs.getString(getString(R.string.pref_sort_key),
                getString(R.string.pref_sort_default));

        MovieEndpointInterface endpoints = retrofit.create(MovieEndpointInterface.class);
        Call<MovieResponse> call = endpoints.getMovies(sortPref);

        call.enqueue(new Callback<MovieResponse>() {

            @Override
            public void onResponse(Response response) {

                if (response == null && response.isSuccess()) {
                    return;
                }

                MovieResponse movieResponse = (MovieResponse) response.body();

                Movies.clear(); // clear before populating

                if (movieResponse == null) return;

                Movies.addAll(movieResponse);

                mAdapter = new MoviesAdapter(getContext(), movieResponse);
                mMovieGrid.setAdapter(mAdapter);

                mPageLoading.setVisibility(View.GONE);
                mMovieGrid.setClickable(true);

            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(getContext(), R.string.connection_error, Toast.LENGTH_LONG).show();
                mPageLoading.setVisibility(View.GONE);
                Log.e(LOG_TAG, t.getMessage() + t.getCause());
            }
        });
    }

    private class MoviesAdapter extends BaseAdapter {

        private Context context;

        private MovieResponse adapterMovies;

        MoviesAdapter(Context context, MovieResponse response) {
            this.context = context;
            adapterMovies = response;
        }

        @Override
        public int getCount() {
            return adapterMovies.getMovies().size();
        }

        @Override
        public Movie getItem(int position) {
            return adapterMovies.getMovies().get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View gridItem;

            Movie tempMovie = adapterMovies.getMovies().get(position);

            if (convertView == null) {
                gridItem = LayoutInflater.from(context).inflate(R.layout.grid_item_movie, parent, false);
            } else {
                gridItem = convertView;
            }
            ImageView moviePoster = (ImageView) gridItem.findViewById(R.id.grid_item_movie_poster);

            int width = mMovieGrid.getMeasuredWidth() / 3;
            moviePoster.setScaleType(ImageView.ScaleType.FIT_CENTER);
            moviePoster.setMinimumWidth(width);
            moviePoster.setMinimumHeight(width);

            if(tempMovie.getPosterPath() != null) {
                Picasso.with(context).load("http://image.tmdb.org/t/p/w185" + tempMovie.getPosterPath()).into(moviePoster);
            } else {
                Bitmap noPoster = BitmapFactory.decodeResource(getResources(), R.drawable.no_poster);
                //TODO: get this to look cleaner
                // moviePoster.setImageBitmap(noPoster);
            }

            return gridItem;
        }

        public void clear() {
            adapterMovies.getMovies().clear();
        }

        public void add(Movie movie) {
            adapterMovies.getMovies().add(movie);
        }

        public void addAll(MovieResponse inMovies) {
            for (Movie movie : inMovies.getMovies()) {
                add(movie);
            }
        }

    }

    public interface MovieEndpointInterface {

        @GET(Constants.MOVIES_URL)
        Call<MovieResponse> getMovies(@Query("sort_by") String sort);
    }

}