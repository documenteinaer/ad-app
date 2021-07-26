package upb.airdocs;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class TestingSettingsActivity extends Activity {

    private static final String LOG_TAG = "TestingSettingsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testing_settings);
        restoreFields();

        final Button saveButton = (Button) findViewById(R.id.save_testing_settings);
        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                saveFields();
                finish();
            }
        });

    }
private void saveFields(){
        final EditText noscansEditText = (EditText) findViewById(R.id.no_scans_testing);
        final EditText addressEditText = (EditText) findViewById(R.id.address_testing);
        final EditText portEditText = (EditText) findViewById(R.id.port_testing);

        String address = addressEditText.getText().toString();
        String port = portEditText.getText().toString();
        int scan_no = Integer.parseInt(noscansEditText.getText().toString());

        Log.d(LOG_TAG, "address=" + address + " port=" + port + " scan_no=" + scan_no);

        Context context = getApplicationContext();
        SharedPreferences sharedPref = context.getSharedPreferences(getString(R.string.preference_file), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("scan_no", scan_no);
        editor.putString("ip", address);
        editor.putString("port", port);
        editor.commit();
    }

    private void restoreFields(){
        final EditText noscansEditText = (EditText) findViewById(R.id.no_scans_testing);
        final EditText addressEditText = (EditText) findViewById(R.id.address_testing);
        final EditText portEditText = (EditText) findViewById(R.id.port_testing);

        Context context = getApplicationContext();
        SharedPreferences sharedPref = context.getSharedPreferences(getString(R.string.preference_file), Context.MODE_PRIVATE);
        int scan_no = sharedPref.getInt("scan_no", 1);
        noscansEditText.setText(String.valueOf(scan_no));
        String address = sharedPref.getString("ip", "192.168.142.123");
        addressEditText.setText(address);
        String port = sharedPref.getString("port", "8001");
        portEditText.setText(port);

        Log.d(LOG_TAG, "address=" + address + " port=" + port + " scan_no=" + scan_no);
    }

}
