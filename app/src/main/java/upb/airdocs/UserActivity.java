package upb.airdocs;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class UserActivity  extends Activity {
    private static final String LOG_TAG = "UserActivity";
    Button postDocButton;
    Button searchDocButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        postDocButton = (Button) findViewById(R.id.post_doc_button);
        postDocButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), PostDocumentActivity.class);
                startActivity(intent);
            }
        });

        searchDocButton = (Button) findViewById(R.id.search_doc_button);
        searchDocButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), SearchDocumentActivity.class);
                startActivity(intent);
            }
        });
    }
}
