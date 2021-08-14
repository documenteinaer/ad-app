package upb.airdocs;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class PostDocumentActivity extends AppCompatActivity {
    private static final String LOG_TAG = "SendDocumentActivity";
    final private static int MY_PERMISSIONS_REQUEST = 126;
    // Flag indicating whether we have called bind on the service.
    private boolean mBound = false;
    private boolean scanActive = false;
    private boolean permissionGranted = false;
    private boolean send = false;

    //  Messenger for communicating with the service.
    Messenger mMessenger = null;

    Button scanSendDocButton;
    EditText postDocumentURL;
    EditText postDocumentDescription;
    TextView scanSendStatus;
    ImageView imageThumbnail;

    String address;
    String port;
    int scan_no;
    String comment;
    String URL;
    String postImageString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_doc);

        requestAllPermissions();

        bindScanService();

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        scanSendStatus = (TextView) findViewById(R.id.scan_send_status);
        scanSendStatus.setText("");

        postDocumentURL = (EditText) findViewById(R.id.post_document_url);
        postDocumentDescription = (EditText) findViewById(R.id.post_document_description);

        imageThumbnail = (ImageView) findViewById(R.id.shared_img_thumbnail);

        restoreAllFields();

        scanSendDocButton = (Button) findViewById(R.id.scan_send_doc);
        scanSendDocButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (scanActive == false) {
                    if (permissionGranted == true) {
                        scanSendStatus.setText("");
                        onStartScanSendDoc();
                        scanActive = true;
                        scanSendDocButton.setEnabled(false);
                        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    } else {
                        Toast.makeText(getApplicationContext(), "Permissions have not been granted", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                handleSendText(intent); // Handle text being sent
            } else if (type.startsWith("image/")) {
                handleSendImage(intent); // Handle single image being sent
            }
        } else {
            // Handle other intents, such as being started from the home screen
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        restoreAllFields();
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, new IntentFilter("msg"));
    }

    @Override
    protected void onPause() {
        saveFields();
        // Unregister since the activity is not visible
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        unbindScanService();
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        //saveAllFields();
        super.onStop();
    }

    @Override
    protected void onRestart() {
        restoreAllFields();
        //invalidateOptionsMenu();
        super.onRestart();
    }


    private void requestAllPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.READ_PHONE_STATE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.RECORD_AUDIO}, MY_PERMISSIONS_REQUEST);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(LOG_TAG, "Permission granted");
                    //ScanService.startService(getApplicationContext());
                    permissionGranted = true;

                } else {
                    Log.d(LOG_TAG, "Permission not granted");
                    //Do smth
                    Toast.makeText(getApplicationContext(), "Permissions have not been granted", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    public void bindScanService() {
        //Intent serviceIntent = new Intent(this, ScanService.class);
        //ContextCompat.startForegroundService(this, serviceIntent);
        bindService(new Intent(this, ScanService.class), mConnection, Context.BIND_AUTO_CREATE);
        Log.d(LOG_TAG, "Bind Scan Service");
    }

    public void unbindScanService() {
        if (mBound) {
            unbindService(mConnection);
            Log.d(LOG_TAG, "Unbind Scan Service");
        }
        //Intent serviceIntent = new Intent(this, ScanService.class);
        //stopService(serviceIntent);
    }

    // Class for interacting with the main interface of the service.
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder iBinder) {
            // This is called when the connection with the iBinder has been established, giving us the object we can use
            // to interact with the iBinder.  We are communicating with the iBinder using a Messenger, so here we get a
            // client-side representation of that from the raw IBinder object.
            mMessenger = new Messenger(iBinder);
            mBound = true;
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been unexpectedly disconnected -- that is,
            // its process crashed.
            mMessenger = null;
            mBound = false;
        }
    };


    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Extract data included in the Intent
            int msg = intent.getIntExtra("message", -1/*default value*/);
            if (msg == ScanService.ACT_STOP_SCAN) {
                scanActive = false;
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                Log.d(LOG_TAG, "In broadcast receiver");
                if (send == true){
                    scanSendStatus.setText("Scan complete");
                    sendDocumentToServer();
                }
            }
            else if (msg == ScanService.MSG_SEND_DONE){
                if (send == true){
                    scanSendDocButton.setEnabled(true);
                    scanSendStatus.setText("Sent successfuly");
                    send = false;
                    clearFields();
                }
            }
            else if (msg == ScanService.UPDATE_SEND_STATUS){
                if (send == true) {
                    scanSendDocButton.setEnabled(true);
                    scanSendStatus.setText("Something went wrong");
                    send = false;
                }
            }
        }
    };

    private void onStartScanSendDoc() {
        //String documentDescription = postDocumentDescription.getText().toString();
        saveFields();
        if (mBound) {
            Message msg = Message.obtain(null, ScanService.MSG_SCAN_TO_POST_DOC, 0, 0);
            try {
                mMessenger.send(msg);
                send = true;
            } catch (RemoteException e) {
                Log.e(LOG_TAG, e.getMessage());
            }
        }
        else{
            Log.d(LOG_TAG, "Not bound to the service");
        }
    }

    private void sendDocumentToServer() {
        if (mBound) {
            // Create and send a message to the service, using a supported 'what' value
            Message msg = Message.obtain(null, ScanService.MSG_ACTUAL_SEND_DOC, 0, 0);
            try {
                mMessenger.send(msg);
            } catch (RemoteException e) {
                Log.e(LOG_TAG, e.getMessage());
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.user_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.user_settings:
                //Go to settings activity
                intent = new Intent(getBaseContext(), SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.testing_activity:
                //Go to testing activity
                intent = new Intent(getBaseContext(), TestingActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void restoreAllFields(){
        Context context = getApplicationContext();
        SharedPreferences sharedPref = context.getSharedPreferences(getString(R.string.preference_file), Context.MODE_PRIVATE);
        if (address == null) {
            address = sharedPref.getString("ip", "192.168.142.105");
            port = sharedPref.getString("port", "8001");
        }
        scan_no = sharedPref.getInt("scan_no", 1);
        comment = sharedPref.getString("comment", "-");
        URL = sharedPref.getString("URL", "-");

        //final EditText commentEditText = (EditText) findViewById(R.id.post_document_description);
        postDocumentDescription.setText(comment);
        //final EditText urlEditText = (EditText) findViewById(R.id.post_document_url);
        postDocumentURL.setText(URL);
    }

    private void saveFields(){
        //final EditText commentEditText = (EditText) findViewById(R.id.post_document_description);
        //final EditText urlEditText = (EditText) findViewById(R.id.post_document_url);

        Context context = getApplicationContext();
        SharedPreferences sharedPref = context.getSharedPreferences(getString(R.string.preference_file), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putString("comment", postDocumentDescription.getText().toString());
        editor.putString("URL", postDocumentURL.getText().toString());
        editor.putString("image", postImageString);
        editor.apply();
    }

    private void handleSendText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            // Update UI to reflect text being shared
            Log.d(LOG_TAG, "received text: " + sharedText);
            postDocumentURL.setText("Announcement");
            postDocumentDescription.setText(sharedText);
            saveFields();
        }
    }

    private void handleSendImage(Intent intent) {
        Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {
            // Update UI to reflect image being shared
            String imgName = imageUri.getLastPathSegment();
            Log.d(LOG_TAG, "received image: " + imgName);
            postDocumentURL.setText(imgName);
            postDocumentDescription.setText("");
            Picasso.get()
                    .load(imageUri)
                    .resize(240, 240)
                    .centerCrop()
                    .into(imageThumbnail);
            String imageString = convertImageToString(imageUri);
            if (imageString != null){
                postImageString = imageString;
            }
            saveFields();

        }
    }

    private String convertImageToString(Uri imageUri){
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            //Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.logo);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageBytes = baos.toByteArray();
            String imageString = Base64.encodeToString(imageBytes, Base64.DEFAULT);
            return imageString;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void clearFields(){
        postDocumentURL = (EditText) findViewById(R.id.post_document_url);
        postDocumentDescription = (EditText) findViewById(R.id.post_document_description);
        imageThumbnail = (ImageView) findViewById(R.id.shared_img_thumbnail);

        postDocumentURL.setText("");
        postDocumentDescription.setText("");
        imageThumbnail.setImageDrawable(null);

        comment = null;
        URL = null;
        postImageString = null;

        Context context = getApplicationContext();
        SharedPreferences sharedPref = context.getSharedPreferences(getString(R.string.preference_file), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putString("comment", null);
        editor.putString("URL", null);
        editor.putString("image", null);
        editor.apply();

    }
}