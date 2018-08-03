package mkruglikov.things;

class MenuItem {

    private final String id;
    private final String name;
    private final String category;
    private final String description;
    private final long price;

    MenuItem(String id, String name, String category, String description, long price) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.description = description;
        this.price = price;
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
