package practice.maddie.popularmoviesstage2;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;

import practice.maddie.popularmoviesstage2.Data.FavoriteMovieContract;
import practice.maddie.popularmoviesstage2.Data.FavoriteMovieDBHelper;
import practice.maddie.popularmoviesstage2.Data.FavoriteMovieProvider;
import practice.maddie.popularmoviesstage2.Model.Movie;
import practice.maddie.popularmoviesstage2.Model.MovieResponse;
import practice.maddie.popularmoviesstage2.Model.Movies;
import practice.maddie.popularmoviesstage2.Model.Review;
import practice.maddie.popularmoviesstage2.Model.ReviewResponse;
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

    private TextView reviewSectionLabel;

    private Button favoriteButton;

    private RecyclerView trailerRecyclerView;

    private TrailersAdapter trailersAdapter;

    private RecyclerView reviewsRecyclerView;

    private ReviewsAdapter reviewsAdapter;

    private boolean isFavorite = false;

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

        reviewSectionLabel = (TextView) rootView.findViewById(R.id.review_section_label);

        setUpFavoriteButton(getActivity());
        setUpTrailers();
        setUpReviews();

    }

    private void setUpReviews() {
        reviewsRecyclerView = (RecyclerView) rootView.findViewById(R.id.reviews_recycler_view);
        reviewsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        sendReviewsRequest();
    }

    private void setUpFavoriteButton(Context context) {
        favoriteButton = (Button) rootView.findViewById(R.id.favorite_button);

        Cursor movieCursor = context.getContentResolver().query(
            FavoriteMovieContract.FavoritesEntry.buildFavoriteMovieUri(mMovie.getId()),
            new String[]{FavoriteMovieContract.FavoritesEntry.COLUMN_ID},
            FavoriteMovieContract.FavoritesEntry.COLUMN_ID + " = ?",
            new String[]{Long.toString(mMovie.getId())},
            null);

        if (movieCursor != null && movieCursor.moveToFirst()) {
            isFavorite = true;
        }

        favoriteButton.setText(isFavorite ? getString(R.string.unfavorite) : getString(R.string.favorite));
        favoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleMovieFavorite();
            }
        });
    }

    private void setUpTrailers() {
        trailerRecyclerView = (RecyclerView) rootView.findViewById(R.id.trailer_recycler_view);
        trailerRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        sendTrailersRequest();
    }

    private void toggleMovieFavorite() {
        if (isFavorite) {
            removeFavoriteMovie(mMovie.getId(), getActivity());
            isFavorite = false;
            favoriteButton.setText(R.string.favorite);
        } else {
            long locationID = addFavoriteMovie(mMovie.getId(), getActivity());
            String message = "";
            if (locationID == -1) {
                message = getString(R.string.error_adding_favorite) + " " + mMovie.getTitle();
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            } else {
                favoriteButton.setText(R.string.unfavorite);
                isFavorite = true;
            }
        }
    }

    public static long addFavoriteMovie(long movieId, Context context) {

        Movie movie = Movies.getById(movieId);
        long locationId;

        // First, check if the location with this city name exists in the db
        Cursor movieCursor = context.getContentResolver().query(
            FavoriteMovieContract.FavoritesEntry.buildFavoriteMovieUri(movieId),
            new String[]{FavoriteMovieContract.FavoritesEntry.COLUMN_ID},
            FavoriteMovieContract.FavoritesEntry.COLUMN_ID + " = ?",
            new String[]{Long.toString(movieId)},
            null);

        if (movieCursor != null && movieCursor.moveToFirst()) {
            int locationIdIndex = movieCursor.getColumnIndex(FavoriteMovieContract.FavoritesEntry.COLUMN_ID);
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
            FavoriteMovieContract.FavoritesEntry.buildFavoriteMovieUri(movieId),
            new String[]{FavoriteMovieContract.FavoritesEntry.COLUMN_ID},
            FavoriteMovieContract.FavoritesEntry.COLUMN_ID + " = ?",
            new String[]{Long.toString(movieId)},
            null);

        if (movieCursor != null && movieCursor.moveToFirst()) {

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

        } else {
            return 0;
        }

        movieCursor.close();
        // Wait, that worked?  Yes!
        return numDeleted;
    }

    private void sendReviewsRequest() {
        Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

        long movieId = mMovie.getId();

        ReviewsEndpointInterface endpoints = retrofit.create(ReviewsEndpointInterface.class);
        Call<ReviewResponse> call = endpoints.getReviews(movieId);

        call.enqueue(new Callback<ReviewResponse>() {

            @Override
            public void onResponse(Response response) {

                if (response == null && response.isSuccess()) {
                    return;
                }

                ReviewResponse reviewResponse = (ReviewResponse) response.body();

                if (reviewResponse == null || reviewResponse.getReviews() == null || reviewResponse.getReviews().size() == 0) {
                    reviewSectionLabel.setText(getString(R.string.no_reviews));
                    return;
                }

                reviewSectionLabel.setText(getString(R.string.reviews_header));
                reviewsAdapter = new ReviewsAdapter(reviewResponse);
                reviewsRecyclerView.setAdapter(reviewsAdapter);

            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(getContext(), R.string.connection_error, Toast.LENGTH_LONG).show();
//                mPageLoading.setVisibility(View.GONE);
                Log.e(LOG_TAG, t.getMessage() + t.getCause());
            }
        });
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

                if (trailerResponse == null || trailerResponse.getTrailers() == null) {
                    return;
                }

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

    private class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ReviewViewHolder> {

        private ReviewResponse reviews;

        ReviewsAdapter(ReviewResponse response) {
            reviews = response;
        }

        /**
         * Cache of the children views for a forecast list item.
         */
        public class ReviewViewHolder extends RecyclerView.ViewHolder {

            public final ImageButton goToReviewButton;

            public final TextView reviewContent;

            public final TextView reviewAuthor;

            public ReviewViewHolder(View itemView) {
                super(itemView);
                reviewAuthor = (TextView) itemView.findViewById(R.id.review_author);
                reviewContent = (TextView) itemView.findViewById(R.id.review_content);
                goToReviewButton = (ImageButton) itemView.findViewById(R.id.go_to_review_button);
            }
        }

        @Override
        public ReviewViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
            View view = inflater.inflate(R.layout.list_item_review, viewGroup, false);
            return new ReviewViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ReviewViewHolder holder, int position) {
            final Review review = reviews.getReviews().get(position);
            TextView reviewAuthor = holder.reviewAuthor;
            TextView reviewContent = holder.reviewContent;
            ImageButton goToReviewButton = holder.goToReviewButton;

            reviewAuthor.setText(getString(R.string.by) + " " + review.getAuthor());
            reviewContent.setText(review.getContent());
            goToReviewButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(review.getUrl()));
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return reviews.getReviews().size();
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


    public interface ReviewsEndpointInterface {
        @GET(Constants.REVIEWS_URL)
        Call<ReviewResponse> getReviews(@Path("id") long movieId);
    }

}
