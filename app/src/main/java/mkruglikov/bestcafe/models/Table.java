package mkruglikov.bestcafe.models;

public class Table {
    private String id;
    private String name;

    public Table(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

}
