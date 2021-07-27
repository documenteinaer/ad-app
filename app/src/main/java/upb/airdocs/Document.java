package upb.airdocs;

public class Document {
    private String itemName;
    private String itemDescription;

    public Document(String name, String description) {
        this.itemName = name;
        this.itemDescription = description;
    }

    public String getItemName() {
        return this.itemName;
    }

    public String getItemDescription() {
        return this.itemDescription;
    }
}
