package upb.airdocs;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;


public class UserActivity  extends AppCompatActivity {
    private static final String LOG_TAG = "UserActivity";
    Button postDocButton;
    Button searchDocButton;
    int scan_no = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        restoreAllFields();


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

    @Override
    protected void onStart() {
        restoreAllFields();
        super.onStart();
    }

    @Override
    protected void onStop() {
        saveAllFields();
        super.onStop();
    }

    @Override
    protected void onRestart() {
        invalidateOptionsMenu();
        super.onRestart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.user_menu, menu);
        restoreFieldScanNo();
        switch (scan_no){
            case 1:
                MenuItem item1 = menu.findItem(R.id.scan1);
                item1.setChecked(true);
                return true;
            case 2:
                MenuItem item2 = menu.findItem(R.id.scan2);
                item2.setChecked(true);
                return true;
            case 4:
                MenuItem item4 = menu.findItem(R.id.scan4);
                item4.setChecked(true);
                return true;
            default:
                return true;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.scan_no:
                return true;
            case R.id.scan1:
                item.setChecked(true);
                scan_no = 1;
                saveFieldScanNo();
                return true;
            case R.id.scan2:
                item.setChecked(true);
                scan_no = 2;
                saveFieldScanNo();
                return true;
            case R.id.scan4:
                item.setChecked(true);
                scan_no = 4;
                saveFieldScanNo();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void saveAllFields(){
        Context context = getApplicationContext();
        SharedPreferences sharedPref = context.getSharedPreferences(getString(R.string.preference_file), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("user_scan_no", scan_no);
        Log.d(LOG_TAG, "Saved user scan no");
        editor.apply();
    }

    private void restoreAllFields(){
        Context context = getApplicationContext();
        SharedPreferences sharedPref = context.getSharedPreferences(getString(R.string.preference_file), Context.MODE_PRIVATE);
        scan_no = sharedPref.getInt("user_scan_no", 1);
    }

    private void saveFieldScanNo(){
        Context context = getApplicationContext();
        SharedPreferences sharedPref = context.getSharedPreferences(getString(R.string.preference_file), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("user_scan_no", scan_no);
        Log.d(LOG_TAG, "Saved user scan no");
        editor.apply();
    }

    private void restoreFieldScanNo(){
        Context context = getApplicationContext();
        SharedPreferences sharedPref = context.getSharedPreferences(getString(R.string.preference_file), Context.MODE_PRIVATE);
        scan_no = sharedPref.getInt("user_scan_no", 1);
        Log.d(LOG_TAG, "Restore user scan no");
    }
}
