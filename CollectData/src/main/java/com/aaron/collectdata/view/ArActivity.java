/*
 * Copyright 2017 Google LLC
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

package com.aaron.collectdata.view;

import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.PointF;
import android.media.Image;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.aaron.baselibs.base.BaseActivity;
import com.aaron.collectdata.R;
import com.aaron.collectdata.ar.utils.BackgroundRenderer;
import com.aaron.collectdata.ar.utils.CameraPermissionHelper;
import com.aaron.collectdata.ar.utils.DepthSettings;
import com.aaron.collectdata.ar.utils.DisplayRotationHelper;
import com.aaron.collectdata.ar.utils.Framebuffer;
import com.aaron.collectdata.ar.utils.FullScreenHelper;
import com.aaron.collectdata.ar.utils.GLError;
import com.aaron.collectdata.ar.utils.InstantPlacementSettings;
import com.aaron.collectdata.ar.utils.Mesh;
import com.aaron.collectdata.ar.utils.PlaneRenderer;
import com.aaron.collectdata.ar.utils.SampleRender;
import com.aaron.collectdata.ar.utils.Shader;
import com.aaron.collectdata.ar.utils.SpecularCubemapFilter;
import com.aaron.collectdata.ar.utils.TapHelper;
import com.aaron.collectdata.ar.utils.Texture;
import com.aaron.collectdata.ar.utils.TrackingStateHelper;
import com.aaron.collectdata.ar.utils.VertexBuffer;
import com.aaron.collectdata.ar.utils.WrappedAnchor;
import com.aaron.collectdata.bean.AccelerometerBean;
import com.aaron.collectdata.bean.GPSCollectData;
import com.aaron.collectdata.databinding.ActivityArBinding;
import com.aaron.collectdata.utils.CollectUtils;
import com.google.ar.core.Anchor;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.ArCoreApk.Availability;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Config.InstantPlacementMode;
import com.google.ar.core.DepthPoint;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.InstantPlacementPoint;
import com.google.ar.core.LightEstimate;
import com.google.ar.core.Plane;
import com.google.ar.core.Point;
import com.google.ar.core.Point.OrientationMode;
import com.google.ar.core.PointCloud;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingFailureReason;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.NotYetAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.math.Vector3;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * This is a simple example that shows how to create an augmented reality (AR) application using the
 * ARCore API. The application will display any detected planes and will allow the user to tap on a
 * plane to place a 3D model.
 */
public class ArActivity extends AppCompatActivity implements SampleRender.Renderer {

  private static final String TAG = ArActivity.class.getSimpleName();

  private static final String SEARCHING_PLANE_MESSAGE = "Searching for surfaces...";
  private static final String WAITING_FOR_TAP_MESSAGE = "Tap on a surface to place an object.";

  // See the definition of updateSphericalHarmonicsCoefficients for an explanation of these
  // constants.
  private static final float[] sphericalHarmonicFactors = {
    0.282095f,
    -0.325735f,
    0.325735f,
    -0.325735f,
    0.273137f,
    -0.273137f,
    0.078848f,
    -0.273137f,
    0.136569f,
  };

  private static final float Z_NEAR = 0.1f;
  private static final float Z_FAR = 100f;

  private PointF centerPoint;

  private Disposable disposable = null;

  private static Pose rootPose = null;
  private static double distance = 0.0D;

  private static final int CUBEMAP_RESOLUTION = 16;
  private static final int CUBEMAP_NUMBER_OF_IMPORTANCE_SAMPLES = 32;

  // Rendering. The Renderers are created here, and initialized when the GL surface is created.
  private GLSurfaceView surfaceView;
  private TextView collectTips;
  //位移
  private TextView wy;
  //经度
  private TextView jd;
  //纬度
  private TextView wd;
  //加速度 x
  private TextView jsdX;
  //加速度 y
  private TextView jsdY;
  //加速度 z
  private TextView jsdZ;

  //陀螺仪 x
  private TextView tlyX;
  //陀螺仪 y
  private TextView tlyY;
  //陀螺仪 z
  private TextView tlyZ;

  //磁力计 x
  private TextView cljX;
  //磁力计 y
  private TextView cljY;
  //磁力计 z
  private TextView cljZ;

  //罗盘
  private TextView luopan;

  private boolean installRequested;

  private Session session;
  private DisplayRotationHelper displayRotationHelper;
  private final TrackingStateHelper trackingStateHelper = new TrackingStateHelper(this);
  //private TapHelper tapHelper;
  private SampleRender render;

  private boolean isCollecting = false;

  private PlaneRenderer planeRenderer;
  private BackgroundRenderer backgroundRenderer;
  private Framebuffer virtualSceneFramebuffer;
  private boolean hasSetTextureNames = false;

  private final DepthSettings depthSettings = new DepthSettings();
  private boolean[] depthSettingsMenuDialogCheckboxes = new boolean[2];

  private final InstantPlacementSettings instantPlacementSettings = new InstantPlacementSettings();
  private boolean[] instantPlacementSettingsMenuDialogCheckboxes = new boolean[1];
  // Assumed distance from the device camera to the surface on which user will try to place objects.
  // This value affects the apparent scale of objects while the tracking method of the
  // Instant Placement point is SCREENSPACE_WITH_APPROXIMATE_DISTANCE.
  // Values in the [0.2, 2.0] meter range are a good choice for most AR experiences. Use lower
  // values for AR experiences where users are expected to place objects on surfaces close to the
  // camera. Use larger values for experiences where the user will likely be standing and trying to
  // place an object on the ground or floor in front of them.
  private static final float APPROXIMATE_DISTANCE_METERS = 2.0f;

  // Point Cloud
  private VertexBuffer pointCloudVertexBuffer;
  private Mesh pointCloudMesh;
  private Shader pointCloudShader;
  // Keep track of the last point cloud rendered to avoid updating the VBO if point cloud
  // was not changed.  Do this using the timestamp since we can't compare PointCloud objects.
  private long lastPointCloudTimestamp = 0;

  // Virtual object (ARCore pawn)
  private Mesh virtualObjectMesh;
  private Shader virtualObjectShader;
  private Texture virtualObjectAlbedoTexture;
  private Texture virtualObjectAlbedoInstantPlacementTexture;

  private final List<WrappedAnchor> wrappedAnchors = new ArrayList<>(1);

  // Environmental HDR
  private Texture dfgTexture;
  private SpecularCubemapFilter cubemapFilter;

  // Temporary matrix allocated here to reduce number of allocations for each frame.
  private final float[] modelMatrix = new float[16];
  private final float[] viewMatrix = new float[16];
  private final float[] projectionMatrix = new float[16];
  private final float[] modelViewMatrix = new float[16]; // view x model
  private final float[] modelViewProjectionMatrix = new float[16]; // projection x view x model
  private final float[] sphericalHarmonicsCoefficients = new float[9 * 3];
  private final float[] viewInverseMatrix = new float[16];
  private final float[] worldLightDirection = {0.0f, 0.0f, 0.0f, 0.0f};
  private final float[] viewLightDirection = new float[4]; // view x world light direction

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_ar);
    surfaceView = findViewById(R.id.arSurfaceView);
    collectTips = findViewById(R.id.collectTips);
    wy = findViewById(R.id.wy);
    jd = findViewById(R.id.jd);
    wd = findViewById(R.id.wd);
    jsdX = findViewById(R.id.jsdX);
    jsdY = findViewById(R.id.jsdY);
    jsdZ = findViewById(R.id.jsdZ);

    tlyX = findViewById(R.id.tlyX);
    tlyY = findViewById(R.id.tlyY);
    tlyZ = findViewById(R.id.tlyZ);

    cljX = findViewById(R.id.cljX);
    cljY = findViewById(R.id.cljY);
    cljZ = findViewById(R.id.cljZ);

    luopan = findViewById(R.id.luopan);

    displayRotationHelper = new DisplayRotationHelper(/* context= */ this);

    // Set up touch listener.
    //tapHelper = new TapHelper(/* context= */ this);
    //surfaceView.setOnTouchListener(tapHelper);

    // Set up renderer.
    render = new SampleRender(surfaceView, this, getAssets());

    installRequested = false;

    //获取屏幕中心点
    DisplayMetrics displayMetrics = new DisplayMetrics();
    getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

    centerPoint = new PointF(displayMetrics.widthPixels / 2.0F, displayMetrics.heightPixels / 2.0F);

    depthSettings.onCreate(this);
    instantPlacementSettings.onCreate(this);
//    ImageButton settingsButton = findViewById(R.id.settings_button);
//    settingsButton.setOnClickListener(
//            new View.OnClickListener() {
//              @Override
//              public void onClick(View v) {
//                PopupMenu popup = new PopupMenu(HelloArActivity.this, v);
//                popup.setOnMenuItemClickListener(HelloArActivity.this::settingsMenuClick);
//                popup.inflate(R.menu.settings_menu);
//                popup.show();
//              }
//            });
  }

  @Override
  protected void onDestroy() {
    if (session != null) {
      // Explicitly close ARCore Session to release native resources.
      // Review the API reference for important considerations before calling close() in apps with
      // more complicated lifecycle requirements:
      // https://developers.google.com/ar/reference/java/arcore/reference/com/google/ar/core/Session#close()
      session.close();
      session = null;
    }

    if(null!=disposable){
      disposable.dispose();
      disposable = null;
    }

    CollectUtils.getInstance(this).stopAcceData();
    CollectUtils.getInstance(this).stopGyroscopeData();
    CollectUtils.getInstance(this).stopMagnetometerData();
    CollectUtils.getInstance(this).stopGPS();
    CollectUtils.getInstance(this).stopCompassData();

    isCollecting = false;

    rootPose = null;
    distance = 0.0D;

    super.onDestroy();
  }

  @Override
  protected void onResume() {
    super.onResume();

    if (session == null) {
      Exception exception = null;
      String message = null;
      try {
        // Always check the latest availability.
        Availability availability = ArCoreApk.getInstance().checkAvailability(this);

        // In all other cases, try to install ARCore and handle installation failures.
        if (availability != Availability.SUPPORTED_INSTALLED) {
          switch (ArCoreApk.getInstance().requestInstall(this, !installRequested)) {
            case INSTALL_REQUESTED:
              installRequested = true;
              return;
            case INSTALLED:
              break;
          }
        }

        // ARCore requires camera permissions to operate. If we did not yet obtain runtime
        // permission on Android M and above, now is a good time to ask the user for it.
        if (!CameraPermissionHelper.hasCameraPermission(this)) {
          CameraPermissionHelper.requestCameraPermission(this);
          return;
        }

        // Create the session.
        session = new Session(/* context= */ this);
      } catch (UnavailableArcoreNotInstalledException
          | UnavailableUserDeclinedInstallationException e) {
        message = "Please install ARCore";
        exception = e;
      } catch (UnavailableApkTooOldException e) {
        message = "Please update ARCore";
        exception = e;
      } catch (UnavailableSdkTooOldException e) {
        message = "Please update this app";
        exception = e;
      } catch (UnavailableDeviceNotCompatibleException e) {
        message = "This device does not support AR";
        exception = e;
      } catch (Exception e) {
        message = "Failed to create AR session";
        exception = e;
      }

      if (message != null) {
        //Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Log.e(TAG, "Exception creating session", exception);
        return;
      }
    }

    // Note that order matters - see the note in onPause(), the reverse applies here.
    try {
      configureSession();
      // To record a live camera session for later playback, call
      // `session.startRecording(recordingConfig)` at anytime. To playback a previously recorded AR
      // session instead of using the live camera feed, call
      // `session.setPlaybackDatasetUri(Uri)` before calling `session.resume()`. To
      // learn more about recording and playback, see:
      // https://developers.google.com/ar/develop/java/recording-and-playback
      session.resume();
    } catch (CameraNotAvailableException e) {
      //Toast.makeText(this, "Camera not available. Try restarting the app.", Toast.LENGTH_LONG).show();
      //messageSnackbarHelper.showError(this, "Camera not available. Try restarting the app.");
      session = null;
      return;
    }

    surfaceView.onResume();
    displayRotationHelper.onResume();

    disposable = Observable.interval(1, 1, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Consumer<Long>() {
              @Override
              public void accept(Long aLong) throws Exception {

                try {

                  if(hasTrackingPlane()){

                    if(!isCollecting){

                      //设置文件路径
                      CollectUtils.getInstance(ArActivity.this).setAcceFilePath("");
                      CollectUtils.getInstance(ArActivity.this).setGyroscopeFilePath("");
                      CollectUtils.getInstance(ArActivity.this).setMagnetometerFilePath("");
                      CollectUtils.getInstance(ArActivity.this).setGpsFilePath("");
                      CollectUtils.getInstance(ArActivity.this).setCompassFilePath("");

                      //采集
                      CollectUtils.getInstance(ArActivity.this).collectAcceData();
                      CollectUtils.getInstance(ArActivity.this).collectGyroscopeData();
                      CollectUtils.getInstance(ArActivity.this).collectMagnetometerData();
                      CollectUtils.getInstance(ArActivity.this).collectGPS();
                      CollectUtils.getInstance(ArActivity.this).collectCompass();

                      isCollecting = true;
                    } else {


                      if(null!=wrappedAnchors && !wrappedAnchors.isEmpty()){

                        //开始检测数据

                        collectTips.setText(getString(R.string.label_38));

                        if(distance != 0.0D){


                          wy.setText(getString(R.string.label_40)+ String.format("%.2f", distance)+getString(R.string.label_41));

                          float data = CollectUtils.getInstance(ArActivity.this).getCompassData();
                          luopan.setText(getString(R.string.label_42)+data);

                          StringBuilder lpBuilder = new StringBuilder();
                          lpBuilder.append(getString(R.string.label_05)+(aLong+1)+getString(R.string.label_43));
                          lpBuilder.append("\n");
                          lpBuilder.append(getString(R.string.label_12)+data);
                          lpBuilder.append("\n");
                          lpBuilder.append(getString(R.string.label_40)+ String.format("%.2f", distance)+getString(R.string.label_41));
                          lpBuilder.append("\n\r");

                          CollectUtils.getInstance(ArActivity.this).savedata2File(CollectUtils.getInstance(ArActivity.this).getCompassFilePath(),
                                  lpBuilder.toString());

                        }

                        GPSCollectData gps = CollectUtils.getInstance(ArActivity.this).getGpsData();
                        if(null!=gps){

                          if(gps.getLatitude() != 0.0 && gps.getLongitude() != 0.0){

                            jd.setText(getString(R.string.label_32)+gps.getLongitude());
                            wd.setText(getString(R.string.label_31)+gps.getLatitude());

                          } else {

                            CollectUtils.getInstance(ArActivity.this).collectGPS();
                          }

                          StringBuilder gpsBuilder = new StringBuilder();
                          gpsBuilder.append(getString(R.string.label_05)+(aLong+1)+getString(R.string.label_44));
                          gpsBuilder.append("\n");
                          gpsBuilder.append(getString(R.string.label_32)+gps.getLongitude());
                          gpsBuilder.append("\n");
                          gpsBuilder.append(getString(R.string.label_31)+gps.getLatitude());
                          gpsBuilder.append("\n\r");

                          CollectUtils.getInstance(ArActivity.this).savedata2File(CollectUtils.getInstance(ArActivity.this).getGpsFilePath(),
                                  gpsBuilder.toString());

                        }

                        AccelerometerBean acce = CollectUtils.getInstance(ArActivity.this).getAcceData();
                        jsdX.setText(getString(R.string.label_45)+acce.getX());
                        jsdY.setText(getString(R.string.label_46)+acce.getY());
                        jsdZ.setText(getString(R.string.label_47)+acce.getZ());

                        StringBuilder jsdBuilder = new StringBuilder();
                        jsdBuilder.append(getString(R.string.label_05)+(aLong+1)+getString(R.string.label_48));
                        jsdBuilder.append("\n");
                        jsdBuilder.append(getString(R.string.label_45)+acce.getX());
                        jsdBuilder.append("\n");
                        jsdBuilder.append(getString(R.string.label_46)+acce.getY());
                        jsdBuilder.append("\n");
                        jsdBuilder.append(getString(R.string.label_47)+acce.getZ());
                        jsdBuilder.append("\n\r");

                        CollectUtils.getInstance(ArActivity.this).savedata2File(CollectUtils.getInstance(ArActivity.this).getAcceFilePath(),
                                jsdBuilder.toString());



                        AccelerometerBean gyroscope = CollectUtils.getInstance(ArActivity.this).getGyroscopeData();
                        tlyX.setText(getString(R.string.label_50)+gyroscope.getX());
                        tlyY.setText(getString(R.string.label_51)+gyroscope.getY());
                        tlyZ.setText(getString(R.string.label_52)+gyroscope.getZ());

                        StringBuilder tlyBuilder = new StringBuilder();
                        tlyBuilder.append(getString(R.string.label_05)+(aLong+1)+getString(R.string.label_49));
                        tlyBuilder.append("\n");
                        tlyBuilder.append(getString(R.string.label_50)+gyroscope.getX());
                        tlyBuilder.append("\n");
                        tlyBuilder.append(getString(R.string.label_51)+gyroscope.getY());
                        tlyBuilder.append("\n");
                        tlyBuilder.append(getString(R.string.label_52)+gyroscope.getZ());
                        tlyBuilder.append("\n\r");

                        CollectUtils.getInstance(ArActivity.this).savedata2File(CollectUtils.getInstance(ArActivity.this).getGyroscopeFilePath(),
                                tlyBuilder.toString());

                        AccelerometerBean magnetometer = CollectUtils.getInstance(ArActivity.this).getMagnetometerData();

                        cljX.setText(getString(R.string.label_54)+magnetometer.getX());
                        cljY.setText(getString(R.string.label_55)+magnetometer.getY());
                        cljZ.setText(getString(R.string.label_56)+magnetometer.getZ());

                        StringBuilder cljBuilder = new StringBuilder();
                        cljBuilder.append(getString(R.string.label_05)+(aLong+1)+getString(R.string.label_53));
                        cljBuilder.append("\n");
                        cljBuilder.append(getString(R.string.label_54)+magnetometer.getX());
                        cljBuilder.append("\n");
                        cljBuilder.append(getString(R.string.label_55)+magnetometer.getY());
                        cljBuilder.append("\n");
                        cljBuilder.append(getString(R.string.label_56)+magnetometer.getZ());
                        cljBuilder.append("\n\r");

                        CollectUtils.getInstance(ArActivity.this).savedata2File(CollectUtils.getInstance(ArActivity.this).getMagnetometerFilePath(),
                                cljBuilder.toString());


                      } else {

                        //Toast.makeText(ArActivity.this, "等待ar 机器人采集数据", Toast.LENGTH_SHORT).show();
                        collectTips.setText(getString(R.string.label_39));
                      }

                    }
                  } else {

                    //Toast.makeText(ArActivity.this, "等待ar 机器人采集数据", Toast.LENGTH_SHORT).show();
                    collectTips.setText(getString(R.string.label_39));
                  }

                }catch (Exception e){
                  e.printStackTrace();
                }

              }
            }, new Consumer<Throwable>() {
              @Override
              public void accept(Throwable throwable) throws Exception {

                //
              }
            });
  }

  @Override
  public void onPause() {
    super.onPause();
    if (session != null) {
      // Note that the order matters - GLSurfaceView is paused first so that it does not try
      // to query the session. If Session is paused before GLSurfaceView, GLSurfaceView may
      // still call session.update() and get a SessionPausedException.
      displayRotationHelper.onPause();
      surfaceView.onPause();
      session.pause();
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
    super.onRequestPermissionsResult(requestCode, permissions, results);
    if (!CameraPermissionHelper.hasCameraPermission(this)) {
      // Use toast instead of snackbar here since the activity will exit.
      //Toast.makeText(this, "Camera permission is needed to run this application", Toast.LENGTH_LONG).show();
      if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(this)) {
        // Permission denied with checking "Do not ask again".
        CameraPermissionHelper.launchPermissionSettings(this);
      }
      finish();
    }
  }

  @Override
  public void onWindowFocusChanged(boolean hasFocus) {
    super.onWindowFocusChanged(hasFocus);
    FullScreenHelper.setFullScreenOnWindowFocusChanged(this, hasFocus);
  }

  @Override
  public void onSurfaceCreated(SampleRender render) {
    // Prepare the rendering objects. This involves reading shaders and 3D model files, so may throw
    // an IOException.
    try {
      planeRenderer = new PlaneRenderer(render);
      backgroundRenderer = new BackgroundRenderer(render);
      virtualSceneFramebuffer = new Framebuffer(render, /* width= */ 1, /* height= */ 1);

      cubemapFilter =
          new SpecularCubemapFilter(
              render, CUBEMAP_RESOLUTION, CUBEMAP_NUMBER_OF_IMPORTANCE_SAMPLES);
      // Load DFG lookup table for environmental lighting
      dfgTexture =
          new Texture(
              render,
              Texture.Target.TEXTURE_2D,
              Texture.WrapMode.CLAMP_TO_EDGE,
              /* useMipmaps= */ false);
      // The dfg.raw file is a raw half-float texture with two channels.
      final int dfgResolution = 64;
      final int dfgChannels = 2;
      final int halfFloatSize = 2;

      ByteBuffer buffer =
          ByteBuffer.allocateDirect(dfgResolution * dfgResolution * dfgChannels * halfFloatSize);
      try (InputStream is = getAssets().open("models/dfg.raw")) {
        is.read(buffer.array());
      }
      // SampleRender abstraction leaks here.
      GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, dfgTexture.getTextureId());
      GLError.maybeThrowGLException("Failed to bind DFG texture", "glBindTexture");
      GLES30.glTexImage2D(
          GLES30.GL_TEXTURE_2D,
          /* level= */ 0,
          GLES30.GL_RG16F,
          /* width= */ dfgResolution,
          /* height= */ dfgResolution,
          /* border= */ 0,
          GLES30.GL_RG,
          GLES30.GL_HALF_FLOAT,
          buffer);
      GLError.maybeThrowGLException("Failed to populate DFG texture", "glTexImage2D");

      // Point cloud
      pointCloudShader =
          Shader.createFromAssets(
                  render,
                  "shaders/point_cloud.vert",
                  "shaders/point_cloud.frag",
                  /* defines= */ null)
              .setVec4(
                  "u_Color", new float[] {31.0f / 255.0f, 188.0f / 255.0f, 210.0f / 255.0f, 1.0f})
              .setFloat("u_PointSize", 5.0f);
      // four entries per vertex: X, Y, Z, confidence
      pointCloudVertexBuffer =
          new VertexBuffer(render, /* numberOfEntriesPerVertex= */ 4, /* entries= */ null);
      final VertexBuffer[] pointCloudVertexBuffers = {pointCloudVertexBuffer};
      pointCloudMesh =
          new Mesh(
              render, Mesh.PrimitiveMode.POINTS, /* indexBuffer= */ null, pointCloudVertexBuffers);

      // Virtual object to render (ARCore pawn)
      virtualObjectAlbedoTexture =
          Texture.createFromAsset(
              render,
              "models/andy.png",
              Texture.WrapMode.CLAMP_TO_EDGE,
              Texture.ColorFormat.SRGB);
      virtualObjectAlbedoInstantPlacementTexture =
          Texture.createFromAsset(
              render,
              "models/andy.png",
              Texture.WrapMode.CLAMP_TO_EDGE,
              Texture.ColorFormat.SRGB);
      Texture virtualObjectPbrTexture =
          Texture.createFromAsset(
              render,
              "models/andy.png",
              Texture.WrapMode.CLAMP_TO_EDGE,
              Texture.ColorFormat.LINEAR);

      virtualObjectMesh = Mesh.createFromAsset(render, "models/andy.obj");
      virtualObjectShader =
          Shader.createFromAssets(
                  render,
                  "shaders/environmental_hdr.vert",
                  "shaders/environmental_hdr.frag",
                  /* defines= */ new HashMap<String, String>() {
                    {
                      put(
                          "NUMBER_OF_MIPMAP_LEVELS",
                          Integer.toString(cubemapFilter.getNumberOfMipmapLevels()));
                    }
                  })
              .setTexture("u_AlbedoTexture", virtualObjectAlbedoTexture)
              .setTexture("u_RoughnessMetallicAmbientOcclusionTexture", virtualObjectPbrTexture)
              .setTexture("u_Cubemap", cubemapFilter.getFilteredCubemapTexture())
              .setTexture("u_DfgTexture", dfgTexture);
    } catch (IOException e) {
      Log.e(TAG, "Failed to read a required asset file", e);
      //Toast.makeText(this, "Failed to read a required asset file: " + e, Toast.LENGTH_LONG).show();

    }
  }

  @Override
  public void onSurfaceChanged(SampleRender render, int width, int height) {
    displayRotationHelper.onSurfaceChanged(width, height);
    virtualSceneFramebuffer.resize(width, height);
  }

  @Override
  public void onDrawFrame(SampleRender render) {
    if (session == null) {
      return;
    }

    // Texture names should only be set once on a GL thread unless they change. This is done during
    // onDrawFrame rather than onSurfaceCreated since the session is not guaranteed to have been
    // initialized during the execution of onSurfaceCreated.
    if (!hasSetTextureNames) {
      session.setCameraTextureNames(
          new int[] {backgroundRenderer.getCameraColorTexture().getTextureId()});
      hasSetTextureNames = true;
    }

    // -- Update per-frame state

    // Notify ARCore session that the view size changed so that the perspective matrix and
    // the video background can be properly adjusted.
    displayRotationHelper.updateSessionIfNeeded(session);

    // Obtain the current frame from the AR Session. When the configuration is set to
    // UpdateMode.BLOCKING (it is by default), this will throttle the rendering to the
    // camera framerate.
    Frame frame;
    try {
      frame = session.update();
    } catch (CameraNotAvailableException e) {
      Log.e(TAG, "Camera not available during onDrawFrame", e);
      //Toast.makeText(this, "Camera not available. Try restarting the app.", Toast.LENGTH_LONG).show();
      return;
    }
    Camera camera = frame.getCamera();

    // Update BackgroundRenderer state to match the depth settings.
    try {
      backgroundRenderer.setUseDepthVisualization(
          render, depthSettings.depthColorVisualizationEnabled());
      backgroundRenderer.setUseOcclusion(render, depthSettings.useDepthForOcclusion());
    } catch (IOException e) {
      Log.e(TAG, "Failed to read a required asset file", e);
      //Toast.makeText(this, "Failed to read a required asset file: ", Toast.LENGTH_LONG).show();
      return;
    }
    // BackgroundRenderer.updateDisplayGeometry must be called every frame to update the coordinates
    // used to draw the background camera image.
    backgroundRenderer.updateDisplayGeometry(frame);

    if (camera.getTrackingState() == TrackingState.TRACKING
        && (depthSettings.useDepthForOcclusion()
            || depthSettings.depthColorVisualizationEnabled())) {
      try (Image depthImage = frame.acquireDepthImage16Bits()) {
        backgroundRenderer.updateCameraDepthTexture(depthImage);
      } catch (NotYetAvailableException e) {
        // This normally means that depth data is not available yet. This is normal so we will not
        // spam the logcat with this.
      }
    }

    // Handle one tap per frame.
    handleTap(frame, camera);

    // Keep the screen unlocked while tracking, but allow it to lock when tracking stops.
    trackingStateHelper.updateKeepScreenOnFlag(camera.getTrackingState());

    // Show a message based on whether tracking has failed, if planes are detected, and if the user
    // has placed any objects.
    String message = null;
    if (camera.getTrackingState() == TrackingState.PAUSED) {
      if (camera.getTrackingFailureReason() == TrackingFailureReason.NONE) {
        message = SEARCHING_PLANE_MESSAGE;
      } else {
        message = TrackingStateHelper.getTrackingFailureReasonString(camera);
      }
    } else if (hasTrackingPlane()) {
      if (wrappedAnchors.isEmpty()) {
        message = WAITING_FOR_TAP_MESSAGE;
      }
    } else {
      message = SEARCHING_PLANE_MESSAGE;
    }
    if (message != null) {
      //Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    // -- Draw background

    if (frame.getTimestamp() != 0) {
      // Suppress rendering if the camera did not produce the first frame yet. This is to avoid
      // drawing possible leftover data from previous sessions if the texture is reused.
      backgroundRenderer.drawBackground(render);
    }

    // If not tracking, don't draw 3D objects.
    if (camera.getTrackingState() == TrackingState.PAUSED) {
      return;
    }

    // -- Draw non-occluded virtual objects (planes, point cloud)

    // Get projection matrix.
    camera.getProjectionMatrix(projectionMatrix, 0, Z_NEAR, Z_FAR);

    // Get camera matrix and draw.
    camera.getViewMatrix(viewMatrix, 0);

    // Visualize tracked points.
    // Use try-with-resources to automatically release the point cloud.
    try (PointCloud pointCloud = frame.acquirePointCloud()) {
      if (pointCloud.getTimestamp() > lastPointCloudTimestamp) {
        pointCloudVertexBuffer.set(pointCloud.getPoints());
        lastPointCloudTimestamp = pointCloud.getTimestamp();
      }
      Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
      pointCloudShader.setMat4("u_ModelViewProjection", modelViewProjectionMatrix);
      render.draw(pointCloudMesh, pointCloudShader);
    }

    // Visualize planes.
    planeRenderer.drawPlanes(
        render,
        session.getAllTrackables(Plane.class),
        camera.getDisplayOrientedPose(),
        projectionMatrix);

    // -- Draw occluded virtual objects

    // Update lighting parameters in the shader
    updateLightEstimation(frame.getLightEstimate(), viewMatrix);

    // Visualize anchors created by touch.
    render.clear(virtualSceneFramebuffer, 0f, 0f, 0f, 0f);
    for (WrappedAnchor wrappedAnchor : wrappedAnchors) {
      Anchor anchor = wrappedAnchor.getAnchor();
      Trackable trackable = wrappedAnchor.getTrackable();
      if (anchor.getTrackingState() != TrackingState.TRACKING) {
        continue;
      }

      // Get the current pose of an Anchor in world space. The Anchor pose is updated
      // during calls to session.update() as ARCore refines its estimate of the world.
      anchor.getPose().toMatrix(modelMatrix, 0);

      // Calculate model/view/projection matrices
      Matrix.multiplyMM(modelViewMatrix, 0, viewMatrix, 0, modelMatrix, 0);
      Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, modelViewMatrix, 0);

      // Update shader properties and draw
      virtualObjectShader.setMat4("u_ModelView", modelViewMatrix);
      virtualObjectShader.setMat4("u_ModelViewProjection", modelViewProjectionMatrix);

      if (trackable instanceof InstantPlacementPoint
          && ((InstantPlacementPoint) trackable).getTrackingMethod()
              == InstantPlacementPoint.TrackingMethod.SCREENSPACE_WITH_APPROXIMATE_DISTANCE) {
        virtualObjectShader.setTexture(
            "u_AlbedoTexture", virtualObjectAlbedoInstantPlacementTexture);
      } else {
        virtualObjectShader.setTexture("u_AlbedoTexture", virtualObjectAlbedoTexture);
      }

      render.draw(virtualObjectMesh, virtualObjectShader, virtualSceneFramebuffer);
    }

    // Compose the virtual scene with the background.
    backgroundRenderer.drawVirtualScene(render, virtualSceneFramebuffer, Z_NEAR, Z_FAR);
  }

  // Handle only one tap per frame, as taps are usually low frequency compared to frame rate.
  private void handleTap(Frame frame, Camera camera) {

    try{

      //MotionEvent tap = tapHelper.poll();
      MotionEvent tap = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, centerPoint.x, centerPoint.y, 0);
      if (camera.getTrackingState() == TrackingState.TRACKING) {
        List<HitResult> hitResultList;
        if (instantPlacementSettings.isInstantPlacementEnabled()) {
          hitResultList =
                  frame.hitTestInstantPlacement(tap.getX(), tap.getY(), APPROXIMATE_DISTANCE_METERS);
        } else {
          hitResultList = frame.hitTest(tap);
        }
        for (HitResult hit : hitResultList) {
          // If any plane, Oriented Point, or Instant Placement Point was hit, create an anchor.
          Trackable trackable = hit.getTrackable();
          // If a plane was hit, check that it was hit inside the plane polygon.
          // DepthPoints are only returned if Config.DepthMode is set to AUTOMATIC.
          if ((trackable instanceof Plane
                  && ((Plane) trackable).isPoseInPolygon(hit.getHitPose())
                  && (PlaneRenderer.calculateDistanceToPlane(hit.getHitPose(), camera.getPose()) > 0))
                  || (trackable instanceof Point
                  && ((Point) trackable).getOrientationMode()
                  == OrientationMode.ESTIMATED_SURFACE_NORMAL)
                  || (trackable instanceof InstantPlacementPoint)
                  || (trackable instanceof DepthPoint)) {
            // Cap the number of objects created. This avoids overloading both the
            // rendering system and ARCore.
            if (wrappedAnchors.size() >= 1) {
              wrappedAnchors.get(0).getAnchor().detach();
              wrappedAnchors.remove(0);
            }

            // Adding an Anchor tells ARCore that it should track this position in
            // space. This anchor is created on the Plane to place the 3D model
            // in the correct position relative both to the world and to the plane.
            Anchor anchor = hit.createAnchor();

            //封装
            if(null == rootPose){

              rootPose = anchor.getPose();

            } else {


              //计算位移
              Pose curPose = anchor.getPose();

              float difX = curPose.tx() - rootPose.tx();
              float difY = curPose.ty() - rootPose.ty();
              float difZ = curPose.tz() - rootPose.tz();
              distance = Math.sqrt((double) (difX * difX + difY * difY + difZ * difZ));
            }



            wrappedAnchors.add(new WrappedAnchor(anchor, trackable));
            // For devices that support the Depth API, shows a dialog to suggest enabling
            // depth-based occlusion. This dialog needs to be spawned on the UI thread.
            this.runOnUiThread(this::showOcclusionDialogIfNeeded);

            // Hits are sorted by depth. Consider only closest hit on a plane, Oriented Point, or
            // Instant Placement Point.
            break;
          }
        }
      }

    }catch (Exception e){
      e.printStackTrace();
    }
  }

  /**
   * Shows a pop-up dialog on the first call, determining whether the user wants to enable
   * depth-based occlusion. The result of this dialog can be retrieved with useDepthForOcclusion().
   */
  private void showOcclusionDialogIfNeeded() {
//    boolean isDepthSupported = session.isDepthModeSupported(Config.DepthMode.AUTOMATIC);
//    if (!depthSettings.shouldShowDepthEnableDialog() || !isDepthSupported) {
//      return; // Don't need to show dialog.
//    }
//
//    // Asks the user whether they want to use depth-based occlusion.
//    new AlertDialog.Builder(this)
//        .setTitle(R.string.options_title_with_depth)
//        .setMessage(R.string.depth_use_explanation)
//        .setPositiveButton(
//            R.string.button_text_enable_depth,
//            (DialogInterface dialog, int which) -> {
//              depthSettings.setUseDepthForOcclusion(true);
//            })
//        .setNegativeButton(
//            R.string.button_text_disable_depth,
//            (DialogInterface dialog, int which) -> {
//              depthSettings.setUseDepthForOcclusion(false);
//            })
//        .show();
  }

  private void launchInstantPlacementSettingsMenuDialog() {
//    resetSettingsMenuDialogCheckboxes();
//    Resources resources = getResources();
//    new AlertDialog.Builder(this)
//        .setTitle(R.string.options_title_instant_placement)
//        .setMultiChoiceItems(
//            resources.getStringArray(R.array.instant_placement_options_array),
//            instantPlacementSettingsMenuDialogCheckboxes,
//            (DialogInterface dialog, int which, boolean isChecked) ->
//                instantPlacementSettingsMenuDialogCheckboxes[which] = isChecked)
//        .setPositiveButton(
//            R.string.done,
//            (DialogInterface dialogInterface, int which) -> applySettingsMenuDialogCheckboxes())
//        .setNegativeButton(
//            android.R.string.cancel,
//            (DialogInterface dialog, int which) -> resetSettingsMenuDialogCheckboxes())
//        .show();
  }

  /** Shows checkboxes to the user to facilitate toggling of depth-based effects. */
  private void launchDepthSettingsMenuDialog() {
    // Retrieves the current settings to show in the checkboxes.
//    resetSettingsMenuDialogCheckboxes();
//
//    // Shows the dialog to the user.
//    Resources resources = getResources();
//    if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
//      // With depth support, the user can select visualization options.
//      new AlertDialog.Builder(this)
//          .setTitle(R.string.options_title_with_depth)
//          .setMultiChoiceItems(
//              resources.getStringArray(R.array.depth_options_array),
//              depthSettingsMenuDialogCheckboxes,
//              (DialogInterface dialog, int which, boolean isChecked) ->
//                  depthSettingsMenuDialogCheckboxes[which] = isChecked)
//          .setPositiveButton(
//              R.string.done,
//              (DialogInterface dialogInterface, int which) -> applySettingsMenuDialogCheckboxes())
//          .setNegativeButton(
//              android.R.string.cancel,
//              (DialogInterface dialog, int which) -> resetSettingsMenuDialogCheckboxes())
//          .show();
//    } else {
//      // Without depth support, no settings are available.
//      new AlertDialog.Builder(this)
//          .setTitle(R.string.options_title_without_depth)
//          .setPositiveButton(
//              R.string.done,
//              (DialogInterface dialogInterface, int which) -> applySettingsMenuDialogCheckboxes())
//          .show();
//    }
  }

  private void applySettingsMenuDialogCheckboxes() {
    depthSettings.setUseDepthForOcclusion(depthSettingsMenuDialogCheckboxes[0]);
    depthSettings.setDepthColorVisualizationEnabled(depthSettingsMenuDialogCheckboxes[1]);
    instantPlacementSettings.setInstantPlacementEnabled(
        instantPlacementSettingsMenuDialogCheckboxes[0]);
    configureSession();
  }

  private void resetSettingsMenuDialogCheckboxes() {
    depthSettingsMenuDialogCheckboxes[0] = depthSettings.useDepthForOcclusion();
    depthSettingsMenuDialogCheckboxes[1] = depthSettings.depthColorVisualizationEnabled();
    instantPlacementSettingsMenuDialogCheckboxes[0] =
        instantPlacementSettings.isInstantPlacementEnabled();
  }

  /** Checks if we detected at least one plane. */
  private boolean hasTrackingPlane() {
    for (Plane plane : session.getAllTrackables(Plane.class)) {
      if (plane.getTrackingState() == TrackingState.TRACKING) {
        return true;
      }
    }
    return false;
  }

  /** Update state based on the current frame's light estimation. */
  private void updateLightEstimation(LightEstimate lightEstimate, float[] viewMatrix) {
    if (lightEstimate.getState() != LightEstimate.State.VALID) {
      virtualObjectShader.setBool("u_LightEstimateIsValid", false);
      return;
    }
    virtualObjectShader.setBool("u_LightEstimateIsValid", true);

    Matrix.invertM(viewInverseMatrix, 0, viewMatrix, 0);
    virtualObjectShader.setMat4("u_ViewInverse", viewInverseMatrix);

    updateMainLight(
        lightEstimate.getEnvironmentalHdrMainLightDirection(),
        lightEstimate.getEnvironmentalHdrMainLightIntensity(),
        viewMatrix);
    updateSphericalHarmonicsCoefficients(
        lightEstimate.getEnvironmentalHdrAmbientSphericalHarmonics());
    cubemapFilter.update(lightEstimate.acquireEnvironmentalHdrCubeMap());
  }

  private void updateMainLight(float[] direction, float[] intensity, float[] viewMatrix) {
    // We need the direction in a vec4 with 0.0 as the final component to transform it to view space
    worldLightDirection[0] = direction[0];
    worldLightDirection[1] = direction[1];
    worldLightDirection[2] = direction[2];
    Matrix.multiplyMV(viewLightDirection, 0, viewMatrix, 0, worldLightDirection, 0);
    virtualObjectShader.setVec4("u_ViewLightDirection", viewLightDirection);
    virtualObjectShader.setVec3("u_LightIntensity", intensity);
  }

  private void updateSphericalHarmonicsCoefficients(float[] coefficients) {
    // Pre-multiply the spherical harmonics coefficients before passing them to the shader. The
    // constants in sphericalHarmonicFactors were derived from three terms:
    //
    // 1. The normalized spherical harmonics basis functions (y_lm)
    //
    // 2. The lambertian diffuse BRDF factor (1/pi)
    //
    // 3. A <cos> convolution. This is done to so that the resulting function outputs the irradiance
    // of all incoming light over a hemisphere for a given surface normal, which is what the shader
    // (environmental_hdr.frag) expects.
    //
    // You can read more details about the math here:
    // https://google.github.io/filament/Filament.html#annex/sphericalharmonics

    if (coefficients.length != 9 * 3) {
      throw new IllegalArgumentException(
          "The given coefficients array must be of length 27 (3 components per 9 coefficients");
    }

    // Apply each factor to every component of each coefficient
    for (int i = 0; i < 9 * 3; ++i) {
      sphericalHarmonicsCoefficients[i] = coefficients[i] * sphericalHarmonicFactors[i / 3];
    }
    virtualObjectShader.setVec3Array(
        "u_SphericalHarmonicsCoefficients", sphericalHarmonicsCoefficients);
  }

  /** Configures the session with feature settings. */
  private void configureSession() {
    Config config = session.getConfig();
    config.setLightEstimationMode(Config.LightEstimationMode.ENVIRONMENTAL_HDR);
    if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
      config.setDepthMode(Config.DepthMode.AUTOMATIC);
    } else {
      config.setDepthMode(Config.DepthMode.DISABLED);
    }
    if (instantPlacementSettings.isInstantPlacementEnabled()) {
      config.setInstantPlacementMode(InstantPlacementMode.LOCAL_Y_UP);
    } else {
      config.setInstantPlacementMode(InstantPlacementMode.DISABLED);
    }
    session.configure(config);
  }
}

