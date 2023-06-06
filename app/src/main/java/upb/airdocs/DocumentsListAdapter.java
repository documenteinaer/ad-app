package upb.airdocs;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.squareup.picasso.Picasso;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class DocumentsListAdapter extends BaseAdapter {
    private static final String LOG_TAG = "DocumentsListAdapter";
    private Context context;
    private ArrayList<Document> items;

    public DocumentsListAdapter(Context context, ArrayList<Document> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size(); //returns total item in the list
    }

    @Override
    public Object getItem(int position) {
        return items.get(position); //returns the item at the specified position
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.layout_document_item, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.imgThumbnail.setImageBitmap(null);

        final Document currentItem = (Document) getItem(position);
        String docName = currentItem.getItemName();
        final String docDescription = currentItem.getItemDescription();
        final String docSimilarity = currentItem.getSimilarity();
        if (!docName.equals("-")) {
            viewHolder.itemName.setText(docName);
        }else{
            viewHolder.itemName.setVisibility(View.GONE);
        }
        viewHolder.itemDescription.setText(docDescription + " -> " + docSimilarity);
        String fileString = currentItem.getFileString();
        String fileType = currentItem.getFileType();
        if (fileString != null){
            if (fileType.equals("image/*") && fileString != null) {
                Bitmap imageBitmap = convertStringToBitmap(fileString);
                final Uri imageUri = getImageUri(context, imageBitmap, docName);
                Picasso.get()
                        .load(imageUri)
                        .resize(240, 240)
                        .centerCrop()
                        .into(viewHolder.imgThumbnail);

                viewHolder.imgThumbnail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setDataAndType(imageUri, fileType);
                        context.startActivity(intent);
                    }
                });
            } else if (FileTypes.isAcceptedType(fileType) && fileString != null){
                final File file = convertStringToFile(fileString, docName);
                if (file != null) {
                    viewHolder.itemName.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //Log.d(LOG_TAG, "trying to open file: " + fileUri.getPath());
                            Uri fileUri = openFile(file);
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_VIEW);
                            intent.setDataAndType(fileUri, fileType);
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                            context.startActivity(intent);
                        }
                    });
                }
            }
        }

        if (URLUtil.isValidUrl(docDescription)) {
            /*Picasso.get()
                    .load(currentItem.getItemDescription())
                    .resize(240, 240)
                    .centerCrop()
                    .into(viewHolder.imgThumbnail);*/

            /*viewHolder.imgThumbnail.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    openURL(docDescription);
                }
            });*/
            viewHolder.itemDescription.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    openURL(docDescription);
                }
            });
        }

        String id = currentItem.getId();
        viewHolder.deleteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                saveDelId(id);
                if (context instanceof SearchDocumentActivity) {
                    ((SearchDocumentActivity)context).deleteDocumentFromServer();
                }

                items.remove(position);
                notifyDataSetChanged();
                Toast.makeText(context, "Deleting file from server", Toast.LENGTH_LONG).show();
            }
        });

        return convertView;
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

    //ViewHolder inner class
    private class ViewHolder {
        TextView itemName;
        TextView itemDescription;
        ImageView imgThumbnail;
        ImageView deleteButton;

        public ViewHolder(View view) {
            itemName = (TextView)view.findViewById(R.id.doc_name);
            itemDescription = (TextView) view.findViewById(R.id.doc_description);
            imgThumbnail = (ImageView) view.findViewById(R.id.img_thumbnail);
            deleteButton = (ImageView) view.findViewById(R.id.delete_button);
        }
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
