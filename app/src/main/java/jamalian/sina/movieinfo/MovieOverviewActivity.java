package jamalian.sina.movieinfo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class MovieOverviewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_overview);

        // Creates the arrow to return to MovieListActivity.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            // Get selected movie's details.
            String title = extras.getString(MovieListActivity.TITLE_KEY);
            String releaseDate = extras.getString(MovieListActivity.RELEASE_DATE_KEY);
            String rating = extras.getString(MovieListActivity.RATING_KEY);
            String overview = extras.getString(MovieListActivity.OVERVIEW_KEY);
            String posterURL = extras.getString(MovieListActivity.POSTER_PATH_KEY);

            ImageView iconView = (ImageView)findViewById(R.id.iconView);
            TextView releaseRatingView = (TextView)findViewById(R.id.releaseRatingView);
            TextView overviewView = (TextView)findViewById(R.id.overviewView);

            // Display movie details.
            if (!posterURL.equals("")) {
                Picasso.with(this).load(posterURL).resize(400, 600).into(iconView);
            } else {
                iconView.setImageResource(R.drawable.no_poster);
            }

            releaseRatingView.setText(releaseDate + "  |  " + rating);
            overviewView.setText(overview);
            setTitle(title);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Returns to movie list if back arrow is pressed.
        switch(item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
