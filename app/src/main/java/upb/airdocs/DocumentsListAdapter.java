package upb.airdocs;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

public class DocumentsListAdapter extends BaseAdapter {
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

        final Document currentItem = (Document) getItem(position);
        viewHolder.itemName.setText(currentItem.getItemName());
        viewHolder.itemDescription.setText(currentItem.getItemDescription());
        String imageString = currentItem.getImageString();
        if (imageString != null){
            Bitmap imageBitmap = convertStringToBitmap(imageString);
            final Uri imageUri = getImageUri(context, imageBitmap);
            Picasso.get()
                    .load(imageUri)
                    .resize(240, 240)
                    .centerCrop()
                    .into(viewHolder.imgThumbnail);

            viewHolder.imgThumbnail.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setDataAndType(imageUri, "image/*");
                    context.startActivity(intent);
                }
            });
        }
        else
        if (URLUtil.isValidUrl(currentItem.getItemName())) {
            Picasso.get()
                    .load(currentItem.getItemName())
                    .resize(240, 240)
                    .centerCrop()
                    .into(viewHolder.imgThumbnail);

            viewHolder.imgThumbnail.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    openURL(currentItem.getItemName());
                }
            });
            viewHolder.itemName.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    openURL(currentItem.getItemName());
                }
            });
        }

        return convertView;
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

        public ViewHolder(View view) {
            itemName = (TextView)view.findViewById(R.id.doc_name);
            itemDescription = (TextView) view.findViewById(R.id.doc_description);
            imgThumbnail = (ImageView) view.findViewById(R.id.img_thumbnail);
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
            e.getMessage();
            return null;
        }
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }


}
