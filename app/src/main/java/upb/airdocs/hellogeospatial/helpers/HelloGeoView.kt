/*
 * Copyright 2022 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package upb.airdocs.hellogeospatial.helpers

import android.content.*
import android.opengl.GLSurfaceView
import android.os.*
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.ar.core.Earth
import com.google.ar.core.GeospatialPose
import com.indooratlas.android.sdk.*
import upb.airdocs.Document
import upb.airdocs.PostDocumentActivity
import upb.airdocs.R
import upb.airdocs.ScanService
import upb.airdocs.common.helpers.SnackbarHelper
import upb.airdocs.hellogeospatial.HelloGeoActivity
import upb.airdocs.hellogeospatial.HelloGeoRenderer

/** Contains UI elements for Hello Geo. */
class HelloGeoView(val activity: HelloGeoActivity) : DefaultLifecycleObserver, IALocationListener,
    IARegion.Listener {
  val root = View.inflate(activity, R.layout.activity_aractivity, null)
  val surfaceView = root.findViewById<GLSurfaceView>(R.id.surfaceview)
  val getARDocs = root.findViewById <Button>(R.id.getARDocs)
  val postARDoc = root.findViewById <Button>(R.id.postARDoc)
  val viewARDoc = root.findViewById <Button>(R.id.viewARDoc)

  val session
    get() = activity.arCoreSessionHelper.session

  val snackbarHelper = SnackbarHelper()

  var mapView: MapView? = null
    var documents: ArrayList<Document>? = null
    private val mIALocationManager: IALocationManager? = null
    private val mIALocationListener: IALocationListener? = null
    var mManager: IALocationManager? = null
    var mCurrentVenue: IARegion? = null
    var mCurrentFloorPlan: IARegion? = null
    var mCurrentFloorLevel: Int? = null
    var mCurrentCertainty: Float? = null
    var currPOIs: ArrayList<IAPOI>? = null
    var iaLatitude: Double? = null
    var iaLongitude: Double? = null
    var scanServiceUtils = ScanServiceUtils(surfaceView.context)


    val statusText = root.findViewById<TextView>(R.id.statusText)
    val iatlasText = root.findViewById<TextView>(R.id.iatlasText)


    private var mBound = false
    private var scanActive = false
    private val permissionGranted = false
    private val send = false
    private var search = false
    private val LOG_TAG = "VLAD"


    //  Messenger for communicating with the service.
    var mMessenger: Messenger? = null



  fun updateStatusText(earth: Earth, cameraGeospatialPose: GeospatialPose?, vpsString: String) {
    activity.runOnUiThread {
      val poseText = if (cameraGeospatialPose == null) "" else
        activity.getString(R.string.geospatial_pose,
                           cameraGeospatialPose.latitude,
                           cameraGeospatialPose.longitude,
                           cameraGeospatialPose.horizontalAccuracy,
                           cameraGeospatialPose.altitude,
                           cameraGeospatialPose.verticalAccuracy,
                           cameraGeospatialPose.heading,
                           cameraGeospatialPose.orientationYawAccuracy)
    val geospatialText = activity.resources.getString(R.string.earth_state,
                                                     earth.earthState.toString(),
                                                     earth.trackingState.toString(),
                                                     vpsString,
                                                     poseText)

    statusText.text = String.format("ARCore Geospatial\n%s\n", geospatialText)
    iatlasText.text = String.format("IndoorAtlas\nPLAN:%s\nLAT/LNG: %.6f˚, %.6f˚\nFLOOR: %d\n\t\t\tCERTAINTY: %.1f\n",
        mCurrentFloorPlan?.floorPlan?.name, iaLatitude, iaLongitude, mCurrentFloorLevel, mCurrentCertainty)

    }
  }


  fun updateViewDocument(document: Document) {
    activity.runOnUiThread {
        activity.view.viewARDoc.visibility = View.VISIBLE
        activity.view.viewARDoc.text = "View doc: ${document.itemDescription}"
        activity.view.viewARDoc.setOnClickListener {
          Toast.makeText(it.context, "Viewing doc ${document.itemDescription}!", Toast.LENGTH_SHORT).show()
            scanServiceUtils.ViewDocument(document)
        }
    }
  }

   fun clearViewDocument() {
    activity.runOnUiThread {
        activity.view.viewARDoc.visibility = View.GONE
    }
  }

    override fun onCreate(owner: LifecycleOwner) {
        mManager = IALocationManager.create(this.activity.baseContext)
        mManager?.registerRegionListener(this)
        mManager?.requestLocationUpdates(IALocationRequest.create(), this)

        // get reference to button
        println("VLAD ON CREATE")
        println("VLAD Searching docs...")

        println("VLAD latitude: ${HelloGeoRenderer.staticLatitude}")
        println("VLAD longitude: ${HelloGeoRenderer.staticLongitude}")
        Toast.makeText(activity, "Waiting Geospatial positioning", Toast.LENGTH_LONG).show();


        //Intent serviceIntent = new Intent(this, ScanService.class);
        //ContextCompat.startForegroundService(this, serviceIntent);

        surfaceView.context.bindService(Intent(surfaceView.context, ScanService::class.java), mConnection, Context.BIND_AUTO_CREATE)

        getARDocs.setOnClickListener {
            // your code to perform when the user clicks on the button
            Toast.makeText(it.context, "Getting AR Docs!", Toast.LENGTH_SHORT).show()

            if (!scanActive) {
                onStartScanSearchDoc()
                scanServiceUtils.javaFunc()
                scanActive = true
                getARDocs.setEnabled(false)
            }

        }
        postARDoc.setOnClickListener {
            // your code to perform when the user clicks on the button
            Toast.makeText(it.context, "Posting AR Doc!", Toast.LENGTH_SHORT).show()
            val intent = Intent(it.context, PostDocumentActivity::class.java)
            it.context.startActivity(intent)
        }
    }

    // Class for interacting with the main interface of the service.
    private val mConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, iBinder: IBinder) {
            // This is called when the connection with the iBinder has been established, giving us the object we can use
            // to interact with the iBinder.  We are communicating with the iBinder using a Messenger, so here we get a
            // client-side representation of that from the raw IBinder object.
            mMessenger = Messenger(iBinder)
            mBound = true
        }

        override fun onServiceDisconnected(className: ComponentName) {
            // This is called when the connection with the service has been unexpectedly disconnected -- that is,
            // its process crashed.
            mMessenger = null
            mBound = false
        }
    }

    private val messageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Extract data included in the Intent
            val msg = intent.getIntExtra("message", -1 /*default value*/)
            if (msg == ScanService.ACT_STOP_SCAN) {
                scanActive = false
                if (search == true) {
                    searchDocumentOnServer()
                }
            } else if (msg == ScanService.MSG_SEND_DONE) {
                if (search == true) {
                    getARDocs.isEnabled = true
                    val jsonString = intent.getStringExtra("json")
                    Log.d(LOG_TAG, "Msg=$jsonString")
                    search = false
                    documents = scanServiceUtils.generateItemsList(jsonString)
                }
            } else if (msg == ScanService.UPDATE_SEND_STATUS) {
                if (search == true) {
                    getARDocs.isEnabled = true
                    search = false
                }
            } else if (msg == ScanService.ACT_STOP_SCAN_FAILED) {
                scanActive = false
                Log.d(LOG_TAG, "In broadcast receiver - scan failed")
                if (search == true) {
                    getARDocs.isEnabled = true
                    search = false
                }
            }
        }
    }

    private fun onStartScanSearchDoc() {
        if (mBound) {
            val msg = Message.obtain(null, ScanService.MSG_SCAN_TO_SEARCH_DOC, 0, 0)
            try {
                mMessenger?.send(msg)
                search = true
            } catch (e: RemoteException) {
                Log.e(LOG_TAG, e.message!!)
            }
        }
    }

    private fun searchDocumentOnServer() {
        if (mBound) {
            // Create and send a message to the service, using a supported 'what' value
            val msg = Message.obtain(null, ScanService.MSG_ACTUAL_SEARCH_DOC, 0, 0)
            try {
                mMessenger?.send(msg)
            } catch (e: RemoteException) {
                Log.e(LOG_TAG, e.message!!)
            }
        }
    }

  override fun onResume(owner: LifecycleOwner) {
    surfaceView.onResume()
      LocalBroadcastManager.getInstance(surfaceView.context).registerReceiver(messageReceiver, IntentFilter("msg"))
  }

  override fun onPause(owner: LifecycleOwner) {

      // Unregister since the activity is not visible
      LocalBroadcastManager.getInstance(surfaceView.context).unregisterReceiver(messageReceiver)
    surfaceView.onPause()
  }

    override fun onLocationChanged(iaLocation: IALocation?) {
//        println("VLAD: onLocationChanged $iaLocation")
        mCurrentFloorLevel = iaLocation?.floorLevel
        mCurrentCertainty = iaLocation?.floorCertainty
        iaLatitude = iaLocation?.latitude
        iaLongitude = iaLocation?.longitude

        val foundPois = ArrayList<IAPOI>()
        if (mCurrentVenue != null) {
//            println("VLAD:NOT NULL!")
            val pois = mCurrentVenue?.venue?.poIs
//            println("VLAD: POIS: $pois")
            for (iapoi in mCurrentVenue!!.venue.poIs) {
                if (iapoi.floor != mCurrentFloorLevel) {
                    continue
                }
                foundPois.add(iapoi)
            }
        }
        currPOIs = foundPois
    }

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
    }

    override fun onEnterRegion(iaRegion: IARegion?) {
        println("VLAD onEnterRegion $iaRegion")
        if (iaRegion?.type == IARegion.TYPE_VENUE) {
            mCurrentVenue = iaRegion
        } else if (iaRegion?.type == IARegion.TYPE_FLOOR_PLAN) {
            mCurrentFloorPlan = iaRegion
        }
    }

    override fun onExitRegion(iaRegion: IARegion?) {
        if (iaRegion?.type == IARegion.TYPE_VENUE) {
            mCurrentVenue = iaRegion
        } else if (iaRegion?.type == IARegion.TYPE_FLOOR_PLAN) {
            mCurrentFloorPlan = iaRegion
        }
    }

}

