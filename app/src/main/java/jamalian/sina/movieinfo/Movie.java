package jamalian.sina.movieinfo;

/**
 * Movie object consists of the movie title, its release date, its rating, the movie overview,
 * and the mini poster icon URL,
 */
public class Movie {
    private String title;
    private String releaseDate;
    private String rating;
    private String overview;
    private String posterURL;

    public Movie(String title, String releaseDate, String rating, String overview, String posterURL) {
        this.title = title;
        this.releaseDate = releaseDate;
        this.rating = rating;
        this.overview = overview;
        this.posterURL = posterURL;
    }

    public String getTitle() {
        return title;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public String getRating() {
        return rating;
    }

    public String getOverview() {
        return overview;
    }

    public String getPosterURL() {
        return posterURL;
    }
}
