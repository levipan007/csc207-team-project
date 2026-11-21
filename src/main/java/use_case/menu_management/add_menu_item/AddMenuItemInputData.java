package use_case.menu_management.add_menu_item;

/** Values supplied when the user requests this action. */
public final class AddMenuItemInputData {
    private final String truckId;
    private final String name;
    private final String description;
    private final String price;
    private final String imageReference;

    public AddMenuItemInputData(String truckId, String name, String description, String price, String imageReference) {
        this.truckId = truckId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageReference = imageReference;
    }

    public String getTruckId() { return truckId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getPrice() { return price; }
    public String getImageReference() { return imageReference; }
}
