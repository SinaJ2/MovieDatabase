package jamalian.sina.movieinfo;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.HeaderViewListAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MovieListActivity extends AppCompatActivity {

    // For storing movie details.
    private ArrayList<Movie> movies;

    // For displaying movie details.
    private ListView myListView;
    private View footerView;
    private ArrayAdapter myMovieAdapter;

    // Flag to stop trying to load more movies when scrolled all the way down after
    // a failed connection (most likely because every existing page has already been looked at)
    private boolean scrollingEnabled;

    // Flag to prevent executing multiple network tasks with a single scroll event.
    private boolean loadingMore;

    private String setSortBy;
    private String currentSort;

    private int selectIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_list);

        Bundle extras = getIntent().getExtras();

        // If user changes sort criteria, a new activity starts with that information
        // passed along.
        if (extras != null) {
            setSortBy = extras.getString("selection");
            selectIndex = extras.getInt("index");
        }
        else {
            setSortBy = "popularity.desc";
            selectIndex = -1;
        }
        currentSort = setSortBy;

        // Initialize Movie ArrayList containing details about the movies.
        movies = new ArrayList<Movie>();

        // Setup interface for movie list.
        myListView = (ListView)findViewById(R.id.myListView);

        // Setup the movie list adapter.
        myMovieAdapter = new MovieAdapter(MovieListActivity.this, R.layout.row, movies);

        if (myListView != null && myMovieAdapter != null) {
            myListView.setAdapter(myMovieAdapter);
        }

        // Adds the text "Loading..." as a footer to the ListView to tell the user that
        // there are more movie listings being loaded if they have scrolled to the bottom
        // of the current listings.
        footerView = ((LayoutInflater)MovieListActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.footer, null, false);
        myListView.addFooterView(footerView);

        // Flags to handle displaying more movies.
        scrollingEnabled = true;
        loadingMore = false;

        myListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            // Setup URL query string components.
            private String apiKey = "c69c4ae8012e85b7870ae0a54f00d53b";
            private String sortBy = setSortBy;
            private int pageNo = 1;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // Unused
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int lastVisibleItem = firstVisibleItem + visibleItemCount;

                if (scrollingEnabled && !loadingMore && (lastVisibleItem == totalItemCount)) {
                    loadingMore = true;

                    // Connects to themoviedb.org to retrieve movie info.
                    // Need to do this in a separate background thread.
                    new NetworkTask().execute(apiKey, sortBy, Integer.toString(pageNo));

                    // The first GET request receives information on movies found on the first page.
                    // We want to keep loading movies on subsequent pages as the user continues to
                    // scroll down.
                    pageNo++;
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_layout, menu);

        // Create a Spinner object to display the sort by options.
        MenuItem item = menu.findItem(R.id.spinner);
        Spinner spinner = (Spinner) MenuItemCompat.getActionView(item);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sort_options, R.layout.custom_spinner);
        adapter.setDropDownViewResource(R.layout.custom_spinner);

        spinner.setAdapter(adapter);

        // If a selection resulted in a new activity, set the starting selection to the option
        // the user selected. Otherwise it will default to the first option, popularity.desc,
        // which will result in another intent to a new activity as another option change will
        // falsely be detected.
        if (selectIndex != -1) {
            spinner.setSelection(selectIndex);
        }

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Let the user choose the sort_by value of the URL query String based on how
                // they want the movies to be sorted.
                switch (position) {
                    case 0:
                        setSortBy = "popularity.desc";
                        break;
                    case 1:
                        setSortBy = "popularity.asc";
                        break;
                    case 2:
                        setSortBy = "vote_average.desc";
                        break;
                    case 3:
                        setSortBy = "vote_average.asc";
                        break;
                    case 4:
                        setSortBy = "original_title.asc";
                        break;
                    case 5:
                        setSortBy = "original_title.desc";
                        break;
                    case 6:
                        setSortBy = "release_date.desc";
                        break;
                    case 7:
                        setSortBy = "release_date.asc";
                        break;
                    default:
                        setSortBy = "popularity.desc";
                        break;
                }

                // Sorting option has not been changed.
                // This is to prevent creating a new activity non-stop even with no change
                // in option.
                if (setSortBy.equals(currentSort)) {
                    return;
                }

                // Start a new MovieListActivity to clear movies already loaded and start loading
                // again based on the sorting option.
                Intent intent = new Intent(MovieListActivity.this, MovieListActivity.class);
                intent.putExtra("selection", setSortBy);
                intent.putExtra("index", position);
                startActivity(intent);
                finish();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Unused
            }
        });

        return true;
    }


    /**
     * Does all the networking on a separate background thread.
     * Extends AsyncTask<param1, param2, param3> where parameters represent:
     * param1: Params - type of parameters sent to task upon execution
     * param2: Progress - type of progress units published during background computation
     * param3: Result - the type of the result of the background computation
     *
     * We will get the URL to connect to from Params.
     * Set Progress to Void as we're not using it.
     * The Result parameter will give us a String containing the response from our HTTP GET request.
     *
     * doInBackground connects to themoviedb.org and gets the movie information, returned as Result.
     *
     * onPostExecute takes Result as its String parameter and parses and uses that information.
     *
     */
    private class NetworkTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            // Get the passed in paramaters for the api_key, the sort_by option, and the page number.
            String apiKey = params[0];
            String sortBy = params[1];
            String pageNo = params[2];
            String urlString = "https://api.themoviedb.org/3/discover/movie?api_key=" + apiKey + "&language=en-US&sort_by=" + sortBy + "&include_adult=false&include_video=false&page=" + pageNo;

            HttpURLConnection urlConnection = null;
            InputStream in;
            String data;

            try {
                // Establish a connection with themoviedb.org.
                URL url = new URL(urlString);
                urlConnection = (HttpURLConnection)url.openConnection();
                urlConnection.connect();

                // Reads input stream in bytes and decodes into characters.
                in = new BufferedInputStream(urlConnection.getInputStream());
                data = readStream(in);

                // Result is the returned value, which is onPostExecute's parameter.
                return data;
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } finally {
                // Closes the connection.
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }

                loadingMore = false;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            // Connection was not established successfully, most likely due to reaching a page that doesn't exist.
            // Put this so movie list isn't populated further and prevent having the app crash.
            if (s == null) {
                scrollingEnabled = false;

                if (myListView != null && footerView != null) {
                    myListView.removeFooterView(footerView);
                }

                return;
            }

            try {
                // Turns the input String into a JSON object.
                JSONObject rootJSON = new JSONObject(s);

                // If page exceeds totalPages, no more movies left to parse.
                int page = rootJSON.getInt("page");
                int totalPages = rootJSON.getInt("total_pages");

                if (page > totalPages) {
                    scrollingEnabled = false;

                    if (myListView != null && footerView != null) {
                        myListView.removeFooterView(footerView);
                    }

                    return;
                }

                // Gets the array of movies and their attributes.
                JSONArray resultsJSON = rootJSON.getJSONArray("results");
                int noOfMovies = resultsJSON.length();

                // Extracts value of attribute for each desired attribute.
                for (int i = 0; i < noOfMovies; i++) {
                    JSONObject jsonObject = resultsJSON.getJSONObject(i);

                    String title = jsonObject.getString("original_title");
                    String releaseDate = "Released: " + jsonObject.getString("release_date");
                    String rating = "Rating: " + jsonObject.getString("vote_average") + "/10";
                    String overview = jsonObject.getString("overview");
                    String posterPath = jsonObject.getString("poster_path");
                    String posterURL = "https://image.tmdb.org/t/p/w640" + posterPath;

                    movies.add(new Movie(title, releaseDate, rating, overview, posterURL));
                }

                // Displays movie details onto the ListView.
                populateListView();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private String readStream(InputStream in) throws IOException {
            // Converts byte stream into character stream.
            BufferedReader r = new BufferedReader(new InputStreamReader(in));

            StringBuilder sb = new StringBuilder();
            String line;

            // Reads every line and stores them in sb.
            while((line = r.readLine()) != null) {
                sb.append(line);
            }

            // Closes the input stream.
            in.close();

            return sb.toString();
        }

        private void populateListView() {
            // Update the adapter after more movies were retrieved.
            // Because we have a footer, our MovieAdapter is wrapped as a HeaderViewListAdapter.
            // We want to get our adapter and call notifyDataSetChanged() to update the movie list.
            ((MovieAdapter)((HeaderViewListAdapter)myListView.getAdapter()).getWrappedAdapter()).notifyDataSetChanged();

            // Check if the user selected a specific movie and start a new activity with only that movie's information.
            myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Movie selectedMovie = movies.get(position);

                    Intent intent = new Intent(MovieListActivity.this, MovieOverviewActivity.class);
                    intent.putExtra("title", selectedMovie.getTitle());
                    intent.putExtra("releaseDate", selectedMovie.getReleaseDate());
                    intent.putExtra("rating", selectedMovie.getRating());
                    intent.putExtra("overview", selectedMovie.getOverview());
                    intent.putExtra("posterURL", selectedMovie.getPosterURL());
                    startActivity(intent);
                }
            });
        }
    }
}
