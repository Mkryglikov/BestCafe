package mkruglikov.things;

public class MenuItem {

    private String id;
    private String name;
    private String category;
    private String description;
    private long price;
    private boolean isAvailable;

    MenuItem(String id, String name, String category, String description, long price, boolean isAvailable) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.description = description;
        this.price = price;
        this.isAvailable = isAvailable;
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

    public boolean getIsAvailable() {
        return isAvailable;
    }
}
