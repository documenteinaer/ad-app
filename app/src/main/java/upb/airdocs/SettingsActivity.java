package upb.airdocs;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SettingsActivity extends Activity {
    private static final String LOG_TAG = "SettingsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        restoreFields();

        final Button saveButton = (Button) findViewById(R.id.save_settings);
        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                saveFields();
                finish();
            }
        });

    }

    private void saveFields(){
        final EditText noscansEditText = (EditText) findViewById(R.id.no_scans_user);
        final EditText addressEditText = (EditText) findViewById(R.id.address_user);
        final EditText portEditText = (EditText) findViewById(R.id.port_user);

        Context context = getApplicationContext();
        SharedPreferences sharedPref = context.getSharedPreferences(getString(R.string.preference_file), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("user_scan_no", Integer. parseInt(noscansEditText.getText().toString()));
        editor.putString("ip", addressEditText.getText().toString());
        editor.putString("port", portEditText.getText().toString());
        editor.apply();
    }

    private void restoreFields(){
        final EditText noscansEditText = (EditText) findViewById(R.id.no_scans_user);
        final EditText addressEditText = (EditText) findViewById(R.id.address_user);
        final EditText portEditText = (EditText) findViewById(R.id.port_user);

        Context context = getApplicationContext();
        SharedPreferences sharedPref = context.getSharedPreferences(getString(R.string.preference_file), Context.MODE_PRIVATE);
        int noscans = sharedPref.getInt("user_scan_no", 1);
        Log.d(LOG_TAG, "no scans = " + noscans);
        noscansEditText.setText(String.valueOf(noscans));
        String address = sharedPref.getString("ip", "192.168.142.123");
        addressEditText.setText(address);
        String port = sharedPref.getString("port", "8001");
        portEditText.setText(port);

    }

}
