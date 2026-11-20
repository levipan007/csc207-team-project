package use_case.edit_menu_item;

/** Values supplied when the user requests this action. */
public final class EditMenuItemInputData {
    private final String truckId;
    private final String itemId;
    private final String name;
    private final String description;
    private final String price;
    private final String imageReference;

    public EditMenuItemInputData(String truckId, String itemId, String name, String description, String price, String imageReference) {
        this.truckId = truckId;
        this.itemId = itemId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageReference = imageReference;
    }

    public String getTruckId() { return truckId; }
    public String getItemId() { return itemId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getPrice() { return price; }
    public String getImageReference() { return imageReference; }
}
