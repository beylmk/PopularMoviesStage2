package practice.maddie.popularmoviesstage2;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;

import practice.maddie.popularmoviesstage2.Model.Movie;
import practice.maddie.popularmoviesstage2.Model.MovieResponse;
import practice.maddie.popularmoviesstage2.Model.Movies;
import practice.maddie.popularmoviesstage2.Model.Trailer;
import practice.maddie.popularmoviesstage2.Model.TrailerResponse;

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

        setUpFavoriteButton();

    }

    private void setUpFavoriteButton() {
        favoriteButton = (Button) rootView.findViewById(R.id.favorite_button);
        favoriteButton.setText(mMovie.getIsFavorite() ? getString(R.string.unfavorite) : getString(R.string.favorite));
        favoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "button listener", Toast.LENGTH_SHORT);
            }
        });
    }

    private class TrailersAdapter extends BaseAdapter {

        private Context context;

        private TrailerResponse trailers;

        TrailersAdapter(Context context, TrailerResponse response) {
            this.context = context;
            trailers = response;
        }

        @Override
        public int getCount() {
            return trailers.getTrailers().size();
        }

        @Override
        public Trailer getItem(int position) {
            return trailers.getTrailers().get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View gridItem;

            Trailer tempTrailer = trailers.getTrailers().get(position);

            if (convertView == null) {
                gridItem = LayoutInflater.from(context).inflate(R.layout.list_item_trailer, parent, false);
            } else {
                gridItem = convertView;
            }

            TextView trailerTitle = (TextView) convertView.findViewById(R.id.trailer_title);
            trailerTitle.setText("Trailer " + position);

            return gridItem;
        }

        public void clear() {
            trailers.getTrailers().clear();
        }

        public void add(Trailer trailer) {
            trailers.getTrailers().add(trailer);
        }

        public void addAll(TrailerResponse inTrailers) {
            for (Trailer trailer : inTrailers.getTrailers()) {
                add(trailer);
            }
        }

    }


}
