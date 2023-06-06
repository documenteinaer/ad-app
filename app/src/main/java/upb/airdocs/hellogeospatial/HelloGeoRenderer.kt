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
package upb.airdocs.hellogeospatial

import android.location.Location
import android.opengl.Matrix
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.maps.model.LatLng
import com.google.ar.core.*
import com.google.ar.core.exceptions.CameraNotAvailableException
import upb.airdocs.Document
import upb.airdocs.common.helpers.DisplayRotationHelper
import upb.airdocs.common.helpers.TrackingStateHelper
import upb.airdocs.common.samplerender.*
import upb.airdocs.common.samplerender.Mesh
import upb.airdocs.common.samplerender.arcore.BackgroundRenderer
import java.io.IOException
import kotlin.math.abs
import kotlin.math.absoluteValue


class HelloGeoRenderer(val activity: HelloGeoActivity) :
  SampleRender.Renderer, DefaultLifecycleObserver {
  //<editor-fold desc="ARCore initialization" defaultstate="collapsed">
  companion object {
    val TAG = "HelloGeoRenderer"

    private val Z_NEAR = 0.1f
    private val Z_FAR = 1000f
    var staticLatitude = 0.0;
    var staticLongitude = 0.0;
    var staticAltitude = 0.0;
    var future : VpsAvailabilityFuture? = null
    var vpsDone = false;
    var vpsString = "";

  }

  lateinit var backgroundRenderer: BackgroundRenderer
  lateinit var virtualSceneFramebuffer: Framebuffer
  var hasSetTextureNames = false
  private val earthAnchors = ArrayList<Anchor>()

  // Virtual object (ARCore pawn)
  lateinit var virtualObjectMesh: Mesh
  lateinit var virtualObjectShader: Shader
  lateinit var virtualObjectTexture: Texture

  // Temporary matrix allocated here to reduce number of allocations for each frame.
  val modelMatrix = FloatArray(16)
  val viewMatrix = FloatArray(16)
  val projectionMatrix = FloatArray(16)
  val modelViewMatrix = FloatArray(16) // view x model


  val modelViewProjectionMatrix = FloatArray(16) // projection x view x model

  val session
    get() = activity.arCoreSessionHelper.session

  val displayRotationHelper = DisplayRotationHelper(activity)
  val trackingStateHelper = TrackingStateHelper(activity)

  override fun onResume(owner: LifecycleOwner) {
    displayRotationHelper.onResume()
    hasSetTextureNames = false
  }

  override fun onPause(owner: LifecycleOwner) {
    displayRotationHelper.onPause()
  }

  override fun onSurfaceCreated(render: SampleRender) {
    println("onSurfaceCreated")
    // Prepare the rendering objects.
    // This involves reading shaders and 3D model files, so may throw an IOException.
    try {
      backgroundRenderer = BackgroundRenderer(render)
      virtualSceneFramebuffer = Framebuffer(render, /*width=*/ 1, /*height=*/ 1)

      // Virtual object to render (Geospatial Marker)
      virtualObjectTexture =
        Texture.createFromAsset(
          render,
          "models/spatial_marker_baked.png",
          Texture.WrapMode.CLAMP_TO_EDGE,
          Texture.ColorFormat.SRGB
        )

      virtualObjectMesh = Mesh.createFromAsset(render, "models/geospatial_marker.obj");
      virtualObjectShader =
        Shader.createFromAssets(
          render,
          "shaders/ar_unlit_object.vert",
          "shaders/ar_unlit_object.frag",
          /*defines=*/ null)
          .setTexture("u_Texture", virtualObjectTexture)

      backgroundRenderer.setUseDepthVisualization(render, false) // TODO??
      backgroundRenderer.setUseOcclusion(render, false) // TODO set to true for occlusion
    } catch (e: IOException) {
      Log.e(TAG, "Failed to read a required asset file", e)
      showError("Failed to read a required asset file: $e")
    }
//    onMapClick(LatLng(44.434958, 26.047685))
  }

  override fun onSurfaceChanged(render: SampleRender, width: Int, height: Int) {
    displayRotationHelper.onSurfaceChanged(width, height)
    virtualSceneFramebuffer.resize(width, height)
  }
  //</editor-fold>


  override fun onDrawFrame(render: SampleRender) {
    val session = session ?: return





    //<editor-fold desc="ARCore frame boilerplate" defaultstate="collapsed">
    // Texture names should only be set once on a GL thread unless they change. This is done during
    // onDrawFrame rather than onSurfaceCreated since the session is not guaranteed to have been
    // initialized during the execution of onSurfaceCreated.
    if (!hasSetTextureNames) {
      session.setCameraTextureNames(intArrayOf(backgroundRenderer.cameraColorTexture.textureId))
      hasSetTextureNames = true
    }

    // -- Update per-frame state

    // Notify ARCore session that the view size changed so that the perspective matrix and
    // the video background can be properly adjusted.
    displayRotationHelper.updateSessionIfNeeded(session)

    // Obtain the current frame from ARSession. When the configuration is set to
    // UpdateMode.BLOCKING (it is by default), this will throttle the rendering to the
    // camera framerate.
    val frame =
      try {
        session.update()
      } catch (e: CameraNotAvailableException) {
        Log.e(TAG, "Camera not available during onDrawFrame", e)
        showError("Camera not available. Try restarting the app.")
        return
      }

    val camera = frame.camera

    // BackgroundRenderer.updateDisplayGeometry must be called every frame to update the coordinates
    // used to draw the background camera image.
    backgroundRenderer.updateDisplayGeometry(frame)

    // Keep the screen unlocked while tracking, but allow it to lock when tracking stops.
    trackingStateHelper.updateKeepScreenOnFlag(camera.trackingState)

    // -- Draw background
    if (frame.timestamp != 0L) {
      // Suppress rendering if the camera did not produce the first frame yet. This is to avoid
      // drawing possible leftover data from previous sessions if the texture is reused.
      backgroundRenderer.drawBackground(render)
    }

    // If not tracking, don't draw 3D objects.
    if (camera.trackingState == TrackingState.PAUSED) {
      return
    }

    // Get projection matrix.
    camera.getProjectionMatrix(projectionMatrix, 0, Z_NEAR, Z_FAR)

    // Get camera matrix and draw.
    camera.getViewMatrix(viewMatrix, 0)

    render.clear(virtualSceneFramebuffer, 0f, 0f, 0f, 0f)
    //</editor-fold>



    // TODO: Obtain Geospatial information and display it on the map.
    val earth = session.earth

    if (earth?.trackingState != TrackingState.TRACKING) {
      println("VLAD Earth not tracking: ${earth?.trackingState}")
      return
    }
    // TODO: the Earth object may be used here.
    val cameraGeospatialPose = earth.cameraGeospatialPose
    activity.view.mapView?.updateMapPosition(
      latitude = cameraGeospatialPose.latitude,
      longitude = cameraGeospatialPose.longitude,
      heading = cameraGeospatialPose.heading
    )

    staticLatitude = cameraGeospatialPose.latitude
    staticLongitude = cameraGeospatialPose.longitude
    staticAltitude = cameraGeospatialPose.altitude
    activity.view.updateStatusText(earth, earth.cameraGeospatialPose, vpsString)


    // Obtain a VpsAvailabilityFuture and store it somewhere.
    if (future == null) {
      future = session.checkVpsAvailabilityAsync(staticLatitude, staticLongitude, null)
    }
//
    // Poll VpsAvailabilityFuture later, for example, in a render loop.
    if (future!!.state != FutureState.DONE && !vpsDone) {
      println("VLAD: NOT DONE. ${future!!.state} Returning...")
      return
    }
    if (!vpsDone) {
      when (future!!.result) {
        VpsAvailability.AVAILABLE -> {
          // VPS is available at this location.
          println("VLAD: VPS available")
          activity.runOnUiThread { Toast.makeText(activity, "VPS Available", Toast.LENGTH_SHORT).show() }
          vpsString = "Available"
        }
        VpsAvailability.UNAVAILABLE -> {
          // VPS is unavailable at this location.
          activity.runOnUiThread { Toast.makeText(activity, "VPS Unavailable", Toast.LENGTH_SHORT).show() }
          println("VLAD: VPS unavailable")
          vpsString = "Unavailable"
        }
        VpsAvailability.ERROR_NETWORK_CONNECTION -> {
          // The external service could not be reached due to a network connection error.
          activity.runOnUiThread { Toast.makeText(activity, "VPS ERROR_NETWORK_CONNECTION", Toast.LENGTH_SHORT).show() }
          println("VLAD: error network conn")
          vpsString = "Error network connection"
        }
        else -> {
          activity.runOnUiThread { Toast.makeText(activity, "VPS ELSE???", Toast.LENGTH_SHORT).show() }
          println("VLAD: else??")
          vpsString = "Error"
        }
      }
      vpsDone = true
    }

    var toViewDoc: Document? = null;
    activity.view.documents?.forEach { document ->
//      TODO: document.altitude
      val anchor = onMapClick(LatLng(document.latitude, document.longitude), document.altitude) ?: return
      render.renderCompassAtAnchor(anchor)

      val currLocation = Location("")
      currLocation.latitude = staticLatitude
      currLocation.longitude = staticLongitude

      val anchorLocation = Location("")
      anchorLocation.latitude = document.latitude
      anchorLocation.longitude = document.longitude

      if (currLocation.distanceTo(anchorLocation) < 5) {
        toViewDoc = document
      }
    }

    if (toViewDoc != null) {
      activity.view.updateViewDocument(toViewDoc!!)
    } else {
      activity.view.clearViewDocument()
    }

    // Compose the virtual scene with the background.
    backgroundRenderer.drawVirtualScene(render, virtualSceneFramebuffer, Z_NEAR, Z_FAR)
  }


  fun onMapClick(latLng: LatLng, alt: Double): Anchor? {
    // TODO: place an anchor at the given position.
    val earth = session?.earth ?: return null
    if (earth.trackingState != TrackingState.TRACKING) {
      return null
    }
//    earthAnchor?.detach()
    // Place the earth anchor at the same altitude as that of the camera to make it easier to view.
//    TODO: if altitude within [-3,+3m] -> then move closer

    var altitude = alt
//    if (abs(earth.cameraGeospatialPose.altitude - alt) < 3) {
//      altitude = earth.cameraGeospatialPose.altitude
//    }

    var quats = earth.cameraGeospatialPose.eastUpSouthQuaternion
    // The rotation quaternion of the anchor in the East-Up-South (EUS) coordinate system.
    val qx = 0f
    val qy = 0f
    val qz = 0f
    val qw = 1f
    activity.view.mapView?.earthMarker?.apply {
      position = latLng
      isVisible = true
    }
    return earth.createAnchor(latLng.latitude, latLng.longitude, altitude, quats[0], quats[1], quats[2], quats[3])
  }

  private fun SampleRender.renderCompassAtAnchor(anchor: Anchor) {
    // Get the current pose of the Anchor in world space. The Anchor pose is updated
    // during calls to session.update() as ARCore refines its estimate of the world.
    anchor.pose.toMatrix(modelMatrix, 0)

    // Calculate model/view/projection matrices
    Matrix.multiplyMM(modelViewMatrix, 0, viewMatrix, 0, modelMatrix, 0)
    Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, modelViewMatrix, 0)

    // Update shader properties and draw
    virtualObjectShader.setMat4("u_ModelViewProjection", modelViewProjectionMatrix)
    // actual shader
    draw(virtualObjectMesh, virtualObjectShader, virtualSceneFramebuffer)
  }

  private fun showError(errorMessage: String) =
    activity.view.snackbarHelper.showError(activity, errorMessage)
}
