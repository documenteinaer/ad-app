package upb.airdocs;

public class ServerAddress {
    private String address;
    private String port;
    private String documentURL;

    public ServerAddress(String address, String port, String documentURL) {
        this.address = address;
        this.port = port;
        this.documentURL = documentURL;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getDocumentURL() {
        return documentURL;
    }

    public void setDocumentURL(String documentURL) {
        this.documentURL = documentURL;
    }
}
