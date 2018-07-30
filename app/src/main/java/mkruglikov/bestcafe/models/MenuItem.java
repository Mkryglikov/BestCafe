package mkruglikov.bestcafe.models;

import org.parceler.Parcel;

@Parcel
public class MenuItem {

    private String id;
    private String name;
    private String category;
    private String description;
    private long price;

    public MenuItem(String id, String name, String category, String description, long price) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.description = description;
        this.price = price;
    }

    public MenuItem() {

    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public long getPrice() {
        return price;
    }

}
