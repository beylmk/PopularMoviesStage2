package practice.maddie.popularmoviesstage2.Model;

/**
 * Created by rfl518 on 7/15/16.
 */
public class Review {

    private String id;
    private String key;

    public String getKey() { return key; }

    public void setKey(String key) { this.key = key; }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String toString() {
        return "trailer" + getId() + " " + getKey();
    }
}
