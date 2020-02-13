package ducview;

import java.io.Serializable;

public class SCU implements Serializable {
    private static int nextAvailableId = 1;
    private int id;
    private String label;
    private String comment = "";

    public SCU(String label) {
        this.id = (nextAvailableId++);
        this.label = label;
    }

    public SCU(int id, String label) {
        this(id, label, "");
    }

    public SCU(int id, String label, String comment) {
        this.id = id;
        if (id >= nextAvailableId)
            nextAvailableId = id + 1;
        this.label = label;
        this.comment = comment;
    }

    public String toString() {
        return this.label;
    }

    public String getComment() {
        return this.comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getId() {
        return this.id;
    }
}