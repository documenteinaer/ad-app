package upb.airdocs;

import java.util.ArrayList;
import java.util.Arrays;

public class FileTypes {
    private static ArrayList<Object> types = new ArrayList<>(
            Arrays.asList("application/pdf",
                    "application/msword",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    "application/vnd.ms-excel",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    "application/vnd.ms-powerpoint",
                    "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                    "audio/*",
                    "video/*",
                    "text/plain"));

    public static boolean isAcceptedType(String fileType){
        return types.contains(fileType);
    };
}
