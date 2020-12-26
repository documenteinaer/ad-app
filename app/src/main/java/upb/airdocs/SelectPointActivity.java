package upb.airdocs;


import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.lang.reflect.Field;

public class SelectPointActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_point_layout);

        ImageView imageView = (ImageView) findViewById(R.id.selected_map);
        imageView.setImageResource(MainActivity.selectedMapID);

        final Button backButton = (Button) findViewById(R.id.back_from_select_point);
        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
    }

}
