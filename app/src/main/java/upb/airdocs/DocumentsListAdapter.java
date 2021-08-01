package upb.airdocs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

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

        Document currentItem = (Document) getItem(position);
        viewHolder.itemName.setText(currentItem.getItemName());
        viewHolder.itemDescription.setText(currentItem.getItemDescription());

        if (URLUtil.isValidUrl(currentItem.getItemName())) {
            Picasso.get()
                    .load(currentItem.getItemName())
                    .resize(240, 240)
                    .centerCrop()
                    .into(viewHolder.imgThumbnail);
        }

        return convertView;
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


}
