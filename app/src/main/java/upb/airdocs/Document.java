package upb.airdocs;

public class Document {
    private String itemName = null;
    private String itemDescription = null;
    private String fileString = null;
    private String fileType = null;
    private String id = null;
    public Double latitude = 0.0;
    public Double longitude = 0.0;
    public Double altitude = 0.0;
    private String similarity = null;

    public Document(String name, String description, String fileType, String id, String similarity) {
        this.itemName = name;
        this.itemDescription = description;
        this.fileType = fileType;
        this.id = id;
        this.similarity = similarity;
    }

    public Document(String name, String description, String fileType, String id, Double latitude, Double longitude, Double altitude) {
        this.itemName = name;
        this.itemDescription = description;
        this.fileType = fileType;
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
    }

    public Document(String name, String description, String fileString, String fileType, String id, String similarity) {
        this.itemName = name;
        this.itemDescription = description;
        this.fileString = fileString;
        this.fileType = fileType;
        this.id = id;
        this.similarity = similarity;
    }

    public Document(String name, String description, String fileString, String fileType, String id, Double latitude, Double longitude, Double altitude) {
        this.itemName = name;
        this.itemDescription = description;
        this.fileString = fileString;
        this.fileType = fileType;
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;

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

    public String getSimilarity() {
        return similarity;
    }
}
