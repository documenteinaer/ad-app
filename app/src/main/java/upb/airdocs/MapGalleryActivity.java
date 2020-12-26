package upb.airdocs;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import upb.airdocs.R;

public class MapGalleryActivity extends Activity
{
    //---the images to display---
    Integer[] imageIDs = {
            R.drawable.precis_subsol,
            R.drawable.precis_parter,
            R.drawable.precis_etaj1,
            R.drawable.precis_etaj2,
            R.drawable.precis_etaj3,
            R.drawable.precis_etaj4,
            R.drawable.precis_etaj5,
            R.drawable.precis_etaj6,
            R.drawable.precis_etaj7,
            R.drawable.precis_terasa
    };

    String[] names = {
            "precis_subsol.png",
            "precis_parter.png",
            "precis_etaj1.png",
            "precis_etaj2.png",
            "precis_etaj3.png",
            "precis_etaj4.png",
            "precis_etaj5.png",
            "precis_etaj6.png",
            "precis_etaj7.png",
            "precis_terasa.png",
    };

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_gallery_layout);

        Gallery gallery = (Gallery) findViewById(R.id.gallery1);

        gallery.setAdapter(new ImageAdapter(this));
        gallery.setOnItemClickListener(new OnItemClickListener()
        {
            public void onItemClick(AdapterView parent,
                                    View v, int position, long id)
            {
                ImageView imageView = (ImageView) findViewById(R.id.image1);
                imageView.setImageResource(imageIDs[position]);
                Toast.makeText(getBaseContext(),
                        "Map " + names[position] + " selected",
                        Toast.LENGTH_SHORT).show();

                MainActivity.selectedMap = names[position];
            }
        });

        final Button backButton = (Button) findViewById(R.id.back_from_gallery);
        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
    }

    public class ImageAdapter extends BaseAdapter
    {
        private Context context;
        private int itemBackground;

        public ImageAdapter(Context c)
        {
            context = c;
            //---setting the style---
            TypedArray a = obtainStyledAttributes(R.styleable.Gallery1);
            itemBackground = a.getResourceId(
                    R.styleable.Gallery1_android_galleryItemBackground, 0);
            a.recycle();
        }

        //---returns the number of images---
        public int getCount() {
            return imageIDs.length;
        }

        //---returns the ID of an item--- 
        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        //---returns an ImageView view---
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView = new ImageView(context);
            imageView.setImageResource(imageIDs[position]);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            imageView.setLayoutParams(new Gallery.LayoutParams(750, 600));
            imageView.setBackgroundResource(itemBackground);
            return imageView;
        }
    }
}