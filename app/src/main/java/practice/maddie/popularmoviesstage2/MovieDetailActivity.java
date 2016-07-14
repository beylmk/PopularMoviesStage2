package practice.maddie.popularmoviesstage2;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;

public class MovieDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.movies_fragment_container, new MovieDetailFragment())
                    .commit();
        }
    }

    public static class MovieDetailFragment extends Fragment {

        private final String LOG_TAG = MovieDetailActivity.class.getSimpleName();

        private Movie mMovie;
        private View rootView;
        private ImageView moviePoster;
        private TextView movieRating;
        private TextView movieReleaseDate;
        private TextView movieSynopsis;

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

        public void setUpDetailsUI() {

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

        }



    }

}
