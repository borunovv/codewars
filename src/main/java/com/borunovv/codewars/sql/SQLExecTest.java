package z_codewars.sql;

import java.util.HashMap;
import java.util.Map;

/**
 * @author borunovv
 */
public class SQLExecTest {

    private static String query = "SELECT movies.title, actors.name, movies.cert\n" +
            "FROM movies\n" +
            "JOIN actors_in_movies ON actors_in_movies.movieID = movies.ID\n" +
            "JOIN actors ON actors_in_movies.actorID = actors.ID\n" +
            "WHERE movies.cert <= 15";

    public static void main(String[] args) {
        Map<String, SQLExec.DataSet> db = new HashMap<>();
        SQLExec.DataSet movies = new SQLExec.DataSet();
        movies.addRow().with("id", 1).with("title", "The Matrix").with("cert", 15);
        movies.addRow().with("id", 2).with("title", "Titanic").with("cert", 12);
        movies.addRow().with("id", 3).with("title", "Alien").with("cert", 18);
        db.put("movies", movies);
        System.out.println(movies);

        SQLExec.DataSet actors_in_movies = new SQLExec.DataSet();
        actors_in_movies.addRow().with("actorID", 1).with("movieID", 1);
        actors_in_movies.addRow().with("actorID", 2).with("movieID", 1);
        actors_in_movies.addRow().with("actorID", 3).with("movieID", 2);
        actors_in_movies.addRow().with("actorID", 4).with("movieID", 3);
        db.put("actors_in_movies", actors_in_movies);
        System.out.println(actors_in_movies);
        SQLExec.DataSet actors = new SQLExec.DataSet();
        actors.addRow().with("id", 1).with("name", "Keanu Reeves");
        actors.addRow().with("id", 2).with("name", "Carrie-Anne Moss");
        actors.addRow().with("id", 4).with("name", "Sigourney Weaver");
        db.put("actors", actors);
        System.out.println(actors);

        System.out.println("Query:\n" + query + "\n");

        System.out.println("Result:\n");
        SQLExec.DataSet ds = SQLExec.exec(db, query);
        System.out.println(ds);
    }
}
