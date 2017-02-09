package practice.maddie.popularmoviesstage2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;

import practice.maddie.popularmoviesstage2.Data.FavoriteMovieDBHelper;
import practice.maddie.popularmoviesstage2.Data.FavoriteMovieProvider;
import practice.maddie.popularmoviesstage2.Model.Movie;
import practice.maddie.popularmoviesstage2.Model.MovieResponse;
import practice.maddie.popularmoviesstage2.Model.Movies;
import practice.maddie.popularmoviesstage2.Model.Trailer;
import practice.maddie.popularmoviesstage2.Model.TrailerResponse;
import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

public class MovieDetailFragment extends Fragment {
    private final String LOG_TAG = MovieDetailActivity.class.getSimpleName();

    private Movie mMovie;
    private View rootView;
    private ImageView moviePoster;
    private TextView movieRating;
    private TextView movieReleaseDate;
    private TextView movieSynopsis;
    private Button favoriteButton;
    private RecyclerView trailerRecyclerView;
    private TrailersAdapter trailersAdapter;

    public MovieDetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);

        Intent intent = getActivity().getIntent();
        long defaultId = 0;
        if (intent != null && intent.hasExtra("movie Id")) {
            Long movieId = intent.getLongExtra("movie Id", defaultId);
            mMovie = Movies.getById(movieId);
            setUpDetailsUI();
        }

        return rootView;
    }

    private void setUpDetailsUI() {

        getActivity().setTitle(mMovie.getTitle() + " " + getString(R.string.details));

        movieSynopsis = (TextView) rootView.findViewById(R.id.movie_details_synopsis_textview);
        movieSynopsis.setText(mMovie.getOverview());

        moviePoster = (ImageView) rootView.findViewById(R.id.movie_details_poster_imageview);
        Picasso.with(getContext()).load("http://image.tmdb.org/t/p/w185" + mMovie.getPosterPath()).into(moviePoster);

        movieRating = (TextView) rootView.findViewById(R.id.movie_details_vote_average_textview);
        movieRating.setText(Double.toString(mMovie.getVoteAverage()));

        movieReleaseDate = (TextView) rootView.findViewById(R.id.movie_details_release_date_textview);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy");
        movieReleaseDate.setText(mMovie.getReleaseDate());

        trailerRecyclerView = (RecyclerView) rootView.findViewById(R.id.trailer_recycler_view);
        trailerRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        setUpFavoriteButton();
        setUpTrailers();

    }

    private void setUpFavoriteButton() {
        favoriteButton = (Button) rootView.findViewById(R.id.favorite_button);
        favoriteButton.setText(mMovie.getIsFavorite(getActivity()) ? getString(R.string.unfavorite) : getString(R.string.favorite));
        favoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleMovieFavorite(mMovie.getIsFavorite(getActivity()) ? true : false);
            }
        });
    }

    private void setUpTrailers() {
        sendTrailersRequest();
    }

    private void toggleMovieFavorite(boolean isFavorite) {
        if (isFavorite) {
            FavoriteMovieDBHelper.removeFavoriteMovie(mMovie.getId(), getActivity());
        } else {
            FavoriteMovieDBHelper.addFavoriteMovie(mMovie.getId(), getActivity());
        }
    }

    private void sendTrailersRequest() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        long movieId = mMovie.getId();

        TrailerEndpointInterface endpoints = retrofit.create(TrailerEndpointInterface.class);
        Call<TrailerResponse> call = endpoints.getTrailers(movieId);

        call.enqueue(new Callback<TrailerResponse>() {

            @Override
            public void onResponse(Response response) {

                if (response == null && response.isSuccess()) {
                    return;
                }

                TrailerResponse trailerResponse = (TrailerResponse) response.body();

                if (trailerResponse.getTrailers() == null) return;

                trailersAdapter = new TrailersAdapter(trailerResponse);
                trailerRecyclerView.setAdapter(trailersAdapter);

            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(getContext(), R.string.connection_error, Toast.LENGTH_LONG).show();
//                mPageLoading.setVisibility(View.GONE);
                Log.e(LOG_TAG, t.getMessage() + t.getCause());
            }
        });
    }

    private class TrailersAdapter extends RecyclerView.Adapter<TrailersAdapter.TrailerViewHolder> {

        private TrailerResponse trailers;

        TrailersAdapter(TrailerResponse response) {
            trailers = response;
        }

        /**
         * Cache of the children views for a forecast list item.
         */
        public class TrailerViewHolder extends RecyclerView.ViewHolder {

            public final TextView trailerNumber;

            public final Button launchButton;

            public TrailerViewHolder(View itemView) {
                super(itemView);
                trailerNumber = (TextView) itemView.findViewById(R.id.trailer_number);
                launchButton = (Button) itemView.findViewById(R.id.launch_button);
            }
        }

        @Override
        public TrailerViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                View view = inflater.inflate(R.layout.list_item_trailer, viewGroup, false);
                return new TrailerViewHolder(view);
        }

        @Override
        public void onBindViewHolder(TrailerViewHolder holder, int position) {
            final Trailer trailer = trailers.getTrailers().get(position);
            TextView trailerNumber = holder.trailerNumber;
            Button launchButton = holder.launchButton;

            trailerNumber.setText("Trailer " + (position + 1));
            launchButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.YOUTUBE_URL + trailer.getKey()));
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return trailers.getTrailers().size();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }


    }

    public interface TrailerEndpointInterface {

        @GET(Constants.TRAILERS_URL)
        Call<TrailerResponse> getTrailers(@Path("id") long movieId);

    }

}
