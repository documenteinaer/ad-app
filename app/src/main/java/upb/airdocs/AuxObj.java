package upb.airdocs;


public class AuxObj {
    String comment="-";
    String map="-";
    float x_p=-1;
    float y_p=-1;
    float x = -1;
    float y = -1;
    float z = -1;

    public AuxObj(String comment, String map, float x_p, float y_p, float x, float y, float z) {
        this.comment = comment;
        this.map = map;
        this.x_p = x_p;
        this.y_p = y_p;
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
