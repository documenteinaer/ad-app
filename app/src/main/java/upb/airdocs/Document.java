package upb.airdocs;

public class Document {
    private String itemName = null;
    private String itemDescription = null;
    private String imageString = null;

    public Document(String name, String description) {
        this.itemName = name;
        this.itemDescription = description;
    }

    public Document(String name, String description, String imageString) {
        this.itemName = name;
        this.itemDescription = description;
        this.imageString = imageString;
    }

    public String getItemName() {
        return this.itemName;
    }

    public String getItemDescription() {
        return this.itemDescription;
    }

    public String getImageString() {
        return imageString;
    }
}
