package upb.airdocs;


public class AuxObj {
    String comment="-";
    String map="-";
    float x=-1;
    float y=-1;
    int noScans = 1;

    public AuxObj(String comment, String map, float x, float y, int noScans) {
        this.comment = comment;
        this.map = map;
        this.x = x;
        this.y = y;
        this.noScans = noScans;
    }
}
