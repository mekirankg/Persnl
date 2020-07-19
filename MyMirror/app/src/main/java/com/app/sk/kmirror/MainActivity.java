package com.app.sk.kmirror;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuCompat;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.app.sk.kmirror.utils.Util;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.LargestFaceFocusingProcessor;

import java.io.IOException;
import java.util.ArrayList;

import static android.Manifest.permission.CAMERA;

public class MainActivity extends AppCompatActivity {

    private PopupWindow popUp;
    private LinearLayout myMirror;
    private LinearLayout mainLayout;
    private Button cameraButton;
    private SurfaceView surfaceView;
    private FaceDetector detector;
    private String[] neededPermissions = new String[]{CAMERA};
    private CameraSource cameraSource;
    private SurfaceHolder surfaceHolder;

    private TextView nightMode;

    private boolean mIsNightMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myMirror = findViewById(R.id.my_mirror);
        cameraButton = findViewById(R.id.camera_button);
        surfaceView = findViewById(R.id.surfaceView);



        popUp = new PopupWindow(this);
        this.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.custom_action_bar);
        //getSupportActionBar().setElevation(0);
        View view = getSupportActionBar().getCustomView();
        TextView name = view.findViewById(R.id.name);
        name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "You have clicked tittle", Toast.LENGTH_LONG).show();
            }
        });

        detector = new FaceDetector.Builder(this)
                .setProminentFaceOnly(true) // optimize for single, relatively large face
                .setTrackingEnabled(true) // enable face tracking
                .setClassificationType(/* eyes open and smile */ FaceDetector.ALL_CLASSIFICATIONS)
                .setMode(FaceDetector.FAST_MODE) // for one face this is OK
                .build();

// Check if detector has been initialized properly

        if (!detector.isOperational()) {

            Log.w("MainActivity", "Detector Dependencies are not yet available");

        } else {

            Log.w("MainActivity", "Detector Dependencies are available");
            if (surfaceView != null) {
                boolean result = checkPermission();
                if (result) {
                    setViewVisibility(R.id.tv_capture);
                    setViewVisibility(R.id.surfaceView);
                    setupSurfaceHolder();
                }
            }
        }
        nightMode = findViewById(R.id.tv_capture);
        nightMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickImage();
            }
        });
    }

    private void clickImage() {
        if(!mIsNightMode) {
            myMirror.setBackgroundColor(getColor(R.color.colorPrimaryWhite));
            int pxValue = (int)Util.convertDpToPixel(50, getApplicationContext());
            myMirror.setPadding(pxValue,pxValue,pxValue,pxValue);
            nightMode.setText("NightMode ON");
        } else {
            myMirror.setBackgroundColor(getColor(R.color.colorPrimaryDark));
            int pxValue = (int)Util.convertDpToPixel(5, getApplicationContext());
            myMirror.setPadding(pxValue,pxValue,pxValue,pxValue);
            nightMode.setText("NightMode OFF");

        }
        mIsNightMode= !mIsNightMode;
    /*    if (cameraSource != null) {
            cameraSource.takePicture(*//*shutterCallback*//*null, new CameraSource.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] bytes) {

                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    ((ImageView) findViewById(R.id.iv_picture)).setImageBitmap(bitmap);
                    setViewVisibility(R.id.iv_picture);
                    findViewById(R.id.surfaceView).setVisibility(View.GONE);
                    findViewById(R.id.tv_capture).setVisibility(View.GONE);
                }
            });
        }*/


    }

    public void cameraClicked(View view) {
    /*    Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra("android.intent.extras.CAMERA_FACING", 1);
        startActivity(intent);*/
        cameraButton.setVisibility(View.INVISIBLE);
        myMirror.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        MenuCompat.setGroupDividerEnabled(menu, true);
        inflater.inflate(R.menu.menu_item, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_about:
                // Toast.makeText(MainActivity.this, "About clicked", Toast.LENGTH_LONG).show();
                showAboutWindow();
                return (true);
         /*   case R.id.menu_version:
                Toast.makeText(MainActivity.this, R.string.app_version, Toast.LENGTH_LONG).show();
                return (true);*/
            default:

        }
        return (super.onOptionsItemSelected(item));
    }

    private void showAboutWindow() {
        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.about_popup, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window tolken
        popupWindow.showAtLocation(getWindow().getDecorView().getRootView(), Gravity.CENTER, 0, 0);
        Button bt = popupView.findViewById(R.id.ok_button);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
        // dismiss the popup window when touched
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();
                return true;
            }
        });
    }

    private boolean checkPermission() {
        ArrayList<String> permissionsNotGranted = new ArrayList<>();
        for (String permission : neededPermissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsNotGranted.add(permission);
            }
        }
        if (!permissionsNotGranted.isEmpty()) {
            boolean shouldShowAlert = false;
            for (String permission : permissionsNotGranted) {
                shouldShowAlert = ActivityCompat.shouldShowRequestPermissionRationale(this, permission);
            }
            if (shouldShowAlert) {
                showPermissionAlert(permissionsNotGranted.toArray(new String[0]));
            } else {
                requestPermissions(permissionsNotGranted.toArray(new String[0]));
            }
            return false;
        }
        return true;
    }

    private void showPermissionAlert(final String[] permissions) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle("Permission Required");
        alertBuilder.setMessage("Camea permission is required to move forward.");
        alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                requestPermissions(permissions);
            }
        });
        AlertDialog alert = alertBuilder.create();
        alert.show();
    }

    private void requestPermissions(String[] permissions) {
        ActivityCompat.requestPermissions(MainActivity.this, permissions, 1001);
    }

    @Override

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1001) {
            for (int result : grantResults) {
                if (result == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(MainActivity.this, "This permission is required", Toast.LENGTH_LONG).show();
                    checkPermission();
                    return;
                }
            }
            /* Code after permission granted */
            setViewVisibility(R.id.surfaceView);
            setupSurfaceHolder();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void setupSurfaceHolder() {
        cameraSource = new CameraSource.Builder(this, detector)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedFps(2.0f)
                .setAutoFocusEnabled(true)
                .build();

        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    cameraSource.start(surfaceHolder);
                    detector.setProcessor(new LargestFaceFocusingProcessor(detector,
                            new Tracker<Face>()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }

        });
    }

    private void setViewVisibility(int id) {
        View view = findViewById(id);
        if (view != null) {
            view.setVisibility(View.VISIBLE);
        }
    }
}
