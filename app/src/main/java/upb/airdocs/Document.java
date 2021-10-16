package upb.airdocs;

public class Document {
    private String itemName = null;
    private String itemDescription = null;
    private String fileString = null;
    private String fileType = null;
    private String id = null;

    public Document(String name, String description, String fileType, String id) {
        this.itemName = name;
        this.itemDescription = description;
        this.fileType = fileType;
        this.id = id;
    }

    public Document(String name, String description, String fileString, String fileType, String id) {
        this.itemName = name;
        this.itemDescription = description;
        this.fileString = fileString;
        this.fileType = fileType;
        this.id = id;
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

    public String getId(){ return id;}
}
