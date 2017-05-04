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

import practice.maddie.popularmoviesstage2.Model.Movie;
import practice.maddie.popularmoviesstage2.Model.Movies;

public class MovieDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        Long movieId = getIntent().getLongExtra(Constants.MOVIE_ID_EXTRA_KEY, 0);
        if (savedInstanceState == null && movieId != 0) {
            Movie movie = Movies.getById(movieId);
            getSupportFragmentManager().beginTransaction()
                .add(R.id.movie_detail_fragment_container, MovieDetailFragment.newInstance(movie))
                .commit();
        }
    }

}