package jamalian.sina.movieinfo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Used to populate ListView with movie details.
 */
public class MovieAdapter extends ArrayAdapter<Movie> {

    private Context context;
    private int layoutResourceId;
    private ArrayList<Movie> movies;

    public MovieAdapter(Context context, int layoutResourceId, ArrayList<Movie> movies) {
        super(context, layoutResourceId, movies);
        this.context = context;
        this.layoutResourceId = layoutResourceId;
        this.movies = movies;
    }

    @Override
    public Movie getItem(int position) {
        return super.getItem(position);
    }

    /**
     * ListView calls getView every time it needs to populate a new row.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // A placeholder for the view.
        // Use the View Holder pattern to improve performance by reducing the number of calls
        // to findViewById.
        ViewHolder viewHolder = null;

        // Currently no row View to reuse.
        // convertView contains previously used Views.
        if (convertView == null) {
            // Create a new View.
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(layoutResourceId, parent, false);

            viewHolder = new ViewHolder();

            // Save inflated elements into ViewHolder references.
            viewHolder.iconView = (ImageView)convertView.findViewById(R.id.iconView);
            viewHolder.titleView = (TextView)convertView.findViewById(R.id.titleView);
            viewHolder.releaseRatingView = (TextView)convertView.findViewById(R.id.releaseRatingView);

            // Stores the references.
            convertView.setTag(viewHolder);
        } else {
            // Use an existing View.
            viewHolder = (ViewHolder)convertView.getTag();
        }

        // Get the movie from the movies array.
        Movie movie = movies.get(position);

        // Set text for titleView and releaseRatingView.
        viewHolder.titleView.setText(movie.getTitle());
        viewHolder.releaseRatingView.setText(movie.getReleaseDate() + "  |  " + movie.getRating());

        // Set movie poster icon for iconView.
        Picasso.with(context).load(movie.getPosterURL()).resize(100,150).into(viewHolder.iconView);

        return convertView;
    }

    private static class ViewHolder {
        ImageView iconView;
        TextView titleView;
        TextView releaseRatingView;
    }
}
