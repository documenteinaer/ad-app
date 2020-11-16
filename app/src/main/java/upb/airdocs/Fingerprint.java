package upb.airdocs;

import android.util.Log;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

public class Fingerprint extends Hashtable<String,ArrayList<ScanItem>>{
    private static final String LOG_TAG = "Fingerprint";


    void printToLogFingerprint(){
        Log.d(LOG_TAG, "Fingerprint: ");
        Enumeration<String> addresses = keys();
        while (addresses.hasMoreElements()) {
            String address = (String) addresses.nextElement();
            Log.d(LOG_TAG, (address + " : " + get(address).toString()));
        }
    }
}
