package upb.airdocs;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

public class TestingSettingsActivity extends Activity {

    private static final String LOG_TAG = "TestingSettingsActivity";
    boolean ble = true;

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

        final TextView versionText = (TextView) findViewById(R.id.testing_version_name);
        String versionTextString = "Application version: "+ BuildConfig.VERSION_NAME;
        versionText.setText(versionTextString);

    }
private void saveFields(){
        final EditText noscansEditText = (EditText) findViewById(R.id.no_scans_testing);
        final EditText addressEditText = (EditText) findViewById(R.id.address_testing);
        final EditText portEditText = (EditText) findViewById(R.id.port_testing);
        final Switch bleSwitch = (Switch) findViewById(R.id.ble_switch_testing);
        final Switch cellularSwitch = (Switch) findViewById(R.id.cellular_switch_testing);
        final Switch gpsSwitch = (Switch) findViewById(R.id.gps_switch_testing);
        final Switch audioSwitch = (Switch) findViewById(R.id.audio_switch_testing);
        final Switch magneticSwitch = (Switch) findViewById(R.id.magnetic_switch_testing);

        String address = addressEditText.getText().toString();
        String port = portEditText.getText().toString();
        int scan_no = Integer.parseInt(noscansEditText.getText().toString());
        boolean ble = bleSwitch.isChecked();
        boolean cellular = cellularSwitch.isChecked();
        boolean gps = gpsSwitch.isChecked();
        boolean audio = audioSwitch.isChecked();
        boolean magnetic = magneticSwitch.isChecked();

        Log.d(LOG_TAG, "address=" + address + " port=" + port + " scan_no=" + scan_no);

        Context context = getApplicationContext();
        SharedPreferences sharedPref = context.getSharedPreferences(getString(R.string.preference_file), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("scan_no", scan_no);
        editor.putString("ip", address);
        editor.putString("port", port);
        editor.putBoolean("ble", ble);
        editor.putBoolean("cellular", cellular);
        editor.putBoolean("gps", gps);
        editor.putBoolean("audio", audio);
        editor.putBoolean("magnetic", magnetic);
        editor.commit();
    }

    private void restoreFields(){
        final EditText noscansEditText = (EditText) findViewById(R.id.no_scans_testing);
        final EditText addressEditText = (EditText) findViewById(R.id.address_testing);
        final EditText portEditText = (EditText) findViewById(R.id.port_testing);
        final Switch bleSwitch = (Switch) findViewById(R.id.ble_switch_testing);
        final Switch cellularSwitch = (Switch) findViewById(R.id.cellular_switch_testing);
        final Switch gpsSwitch = (Switch) findViewById(R.id.gps_switch_testing);
        final Switch audioSwitch = (Switch) findViewById(R.id.audio_switch_testing);
        final Switch magneticSwitch = (Switch) findViewById(R.id.magnetic_switch_testing);

        Context context = getApplicationContext();
        SharedPreferences sharedPref = context.getSharedPreferences(getString(R.string.preference_file), Context.MODE_PRIVATE);
        int scan_no = sharedPref.getInt("scan_no", 1);
        noscansEditText.setText(String.valueOf(scan_no));
        String address = sharedPref.getString("ip", "192.168.142.123");
        addressEditText.setText(address);
        String port = sharedPref.getString("port", "8001");
        portEditText.setText(port);
        boolean ble = sharedPref.getBoolean("ble", true);
        bleSwitch.setChecked(ble);
        boolean cellular = sharedPref.getBoolean("cellular", true);
        cellularSwitch.setChecked(cellular);
        boolean gps = sharedPref.getBoolean("gps", true);
        gpsSwitch.setChecked(gps);
        boolean audio = sharedPref.getBoolean("audio", true);
        audioSwitch.setChecked(audio);
        boolean magnetic = sharedPref.getBoolean("magnetic", true);
        magneticSwitch.setChecked(magnetic);


        Log.d(LOG_TAG, "address=" + address + " port=" + port + " scan_no=" + scan_no);
    }

}
