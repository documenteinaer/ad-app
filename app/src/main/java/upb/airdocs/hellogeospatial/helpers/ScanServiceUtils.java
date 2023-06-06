package upb.airdocs.hellogeospatial.helpers;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import upb.airdocs.Document;
import upb.airdocs.DocumentsListAdapter;
import upb.airdocs.FileTypes;
import upb.airdocs.R;
import upb.airdocs.SearchDocumentActivity;
import upb.airdocs.hellogeospatial.HelloGeoRenderer;

public class ScanServiceUtils {
    private Context context;
    private String LOG_TAG = "VLAD";
    public ScanServiceUtils() {

    }
    public ScanServiceUtils(Context context) {
        this.context = context;
    }

    public void javaFunc() {
        System.out.println("VLAD: IT WORKS");
    }

    /**
     * Util function to generate list of items
     *
     * @return ArrayList
     */
    public ArrayList<Document> generateItemsList(String jsonString) {
        ArrayList<Document> list = new ArrayList<>();

        try {
            JSONArray jsonArray = new JSONArray(jsonString);

            Location currLocation = new Location("");
            currLocation.setLatitude(HelloGeoRenderer.Companion.getStaticLatitude());
            currLocation.setLongitude(HelloGeoRenderer.Companion.getStaticLongitude());
            currLocation.setAltitude(HelloGeoRenderer.Companion.getStaticAltitude());


            for(int i=0; i < jsonArray.length(); i++) {
                JSONObject docInfo = (JSONObject)jsonArray.get(i);
                System.out.println("docInfo: " +docInfo);
                String docName = (String)docInfo.get("document");
                String docDescription = (String)docInfo.get("description");
                Double docLatitude = (Double)docInfo.get("latitude");
                Double docLongitude = (Double)docInfo.get("longitude");
                Double docAltitude = (Double)docInfo.get("altitude");
                String fileType = (String)docInfo.get("filetype");
                String id = (String)docInfo.get("id");
                System.out.println("docLatitude" + docLatitude);
                System.out.println("docLongitude" + docLongitude);

                Location poiLocation = new Location("");
                poiLocation.setLatitude(docLatitude);
                poiLocation.setLongitude(docLongitude);
                System.out.println("WORKS dist to poi " + docName + " -> "  + currLocation.distanceTo(poiLocation) + " m");
//                docDescription += " <> " + currLocation.distanceTo(poiLocation) + " m";

                if (docInfo.has("file")){
                    String fileString = (String)docInfo.get("file");
                    list.add(new Document(docName, docDescription, fileString, fileType, id, docLatitude, docLongitude, docAltitude));
                }
                else {
                    list.add(new Document(docName, docDescription, fileType, id, docLatitude, docLongitude, docAltitude));
                }
            }


        } catch (Throwable t) {
            Log.e("VLAD", "Could not parse malformed JSON: \"" + jsonString + "\"");
        }

        return list;
    }

    public void ViewDocument(Document currentItem) {

        System.out.println("VLAD: viewing doc: " + currentItem);

        String docName = currentItem.getItemName();
        final String docDescription = currentItem.getItemDescription();

        String fileString = currentItem.getFileString();
        String fileType = currentItem.getFileType();
        if (fileString != null){
            if (fileType.equals("image/*") && fileString != null) {
                Bitmap imageBitmap = convertStringToBitmap(fileString);
                final Uri imageUri = getImageUri(context, imageBitmap, docName);

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(imageUri, fileType);
                context.startActivity(intent);


            } else if (FileTypes.isAcceptedType(fileType) && fileString != null){
                final File file = convertStringToFile(fileString, docName);
                if (file != null) {
                    //Log.d(LOG_TAG, "trying to open file: " + fileUri.getPath());
                    Uri fileUri = openFile(file);
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setDataAndType(fileUri, fileType);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    context.startActivity(intent);
                }
            }
        }

        if (URLUtil.isValidUrl(docDescription)) {
            openURL(docDescription);
        }

//        String id = currentItem.getId();
//        viewHolder.deleteButton.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                saveDelId(id);
//                if (context instanceof SearchDocumentActivity) {
//                    ((SearchDocumentActivity)context).deleteDocumentFromServer();
//                }
//
//                items.remove(position);
//                notifyDataSetChanged();
//                Toast.makeText(context, "Deleting file from server", Toast.LENGTH_LONG).show();
//            }
//        });

    }



    private Uri openFile(File file){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(context,  context.getPackageName()+ ".fileprovider", file);

            List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                context.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            Log.d(LOG_TAG, "found file: "+ uri.getPath());
        }else {
            uri = Uri.fromFile(file);
        }

        return uri;
    }

    private void openURL(String url){
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        context.startActivity(i);
    }

    private Bitmap convertStringToBitmap(String imageString){
        Bitmap bitmap = null;

        try{
            byte [] encodeByte= Base64.decode(imageString,Base64.DEFAULT);
            InputStream inputStream  = new ByteArrayInputStream(encodeByte);
            bitmap  = BitmapFactory.decodeStream(inputStream);
            return bitmap;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public Uri getImageUri(Context inContext, Bitmap inImage, String name) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, name, null);
        if (path != null) {
            return Uri.parse(path);
        }
        return null;
    }

    private File convertStringToFile(String fileString, String filename){
        try{
            byte [] encodeByte= Base64.decode(fileString,Base64.DEFAULT);
            InputStream inputStream  = new ByteArrayInputStream(encodeByte);
            File sdCard = Environment.getExternalStorageDirectory();
            String path = sdCard.getAbsolutePath()  + "/Documents/" + filename;
            Log.d(LOG_TAG, "path:" + path);
            File file = new File(path);
            file.createNewFile();
            if(file.exists()) {
                Log.d(LOG_TAG, "created file: "+file.getAbsolutePath());
                copyInputStreamToFile(inputStream, file);
                return file;
            }
            else{
                return null;
            }
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    private void copyInputStreamToFile(InputStream in, File file) {
        OutputStream out = null;

        try {
            out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while((len=in.read(buf))>0){
                out.write(buf,0,len);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            // Ensure that the InputStreams are closed even if there's an exception.
            try {
                if ( out != null ) {
                    out.close();
                }

                // If you want to close the "in" InputStream yourself then remove this
                // from here but ensure that you close it yourself eventually.
                in.close();
            }
            catch (IOException e ) {
                e.printStackTrace();
            }
        }
    }

    private void saveDelId(String id){
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putString("delId", id);
        editor.apply();
    }

}
