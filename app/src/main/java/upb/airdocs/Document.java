package upb.airdocs;

public class Document {
    private String itemName = null;
    private String itemDescription = null;
    private String fileString = null;
    private String fileType = null;

    public Document(String name, String description) {
        this.itemName = name;
        this.itemDescription = description;
    }

    public Document(String name, String description, String fileString, String fileType) {
        this.itemName = name;
        this.itemDescription = description;
        this.fileString = fileString;
        this.fileType = fileType;
    }

    public String getItemName() {
        return this.itemName;
    }

    public String getItemDescription() {
        return this.itemDescription;
    }

    public String getFileString() {
        return fileString;
    }

    public String getFileType() {
        return fileType;
    }
}
