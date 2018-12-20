package com.originalstocks.qrscanner;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.originalstocks.qrscanner.Adapters.RecyclerAdapter;
import com.originalstocks.qrscanner.Database.Note;
import com.originalstocks.qrscanner.Database.NoteViewModel;
import com.originalstocks.qrscanner.Helper.GraphicOverlay;
import com.originalstocks.qrscanner.Helper.RectangleOverlay;
import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

public class MainActivity extends AppCompatActivity{

    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private CameraView mCameraView;
    private Button mDetectButton, showSavedInfoButton;
    private BottomSheetBehavior mSheetBehaviour;
    private GraphicOverlay graphicOverlay;
    private RelativeLayout mRelativeLayout, mProgressLayout;
    private TextView resultTextView, simpleTextView;
    private Bitmap mBitmap;
    private String ImagePath = null;

    // Database
    private NoteViewModel noteViewModel;
    private static final int NEW_NOTE_ACTIVITY_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //Check real_time permission if run higher API 23
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, REQUEST_CAMERA_PERMISSION);
            return;
        }

        mCameraView = findViewById(R.id.camera_view);
        mDetectButton = findViewById(R.id.buttonDetect);
        graphicOverlay = (GraphicOverlay) findViewById(R.id.graphic_overlay);
        resultTextView = findViewById(R.id.result_textView);
        simpleTextView = findViewById(R.id.simple_text);
        mProgressLayout = findViewById(R.id.progress_layout);
        showSavedInfoButton = findViewById(R.id.showSavedButton);
        mRelativeLayout = findViewById(R.id.layout_container);

        noteViewModel = ViewModelProviders.of(this).get(NoteViewModel.class);

        mDetectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCameraView.start();
                mCameraView.captureImage();
                graphicOverlay.clear();
            }
        });

        mCameraView.addCameraKitListener(new CameraKitEventListener() {
            @Override
            public void onEvent(CameraKitEvent cameraKitEvent) {

            }

            @Override
            public void onError(CameraKitError cameraKitError) {
                Log.i("CameraKitError", "onError: " + cameraKitError.getMessage());
            }

            @Override
            public void onImage(final CameraKitImage cameraKitImage) {
                // show progress
                mProgressLayout.setVisibility(View.VISIBLE);
                mBitmap = cameraKitImage.getBitmap();
                mBitmap = Bitmap.createScaledBitmap(mBitmap, mCameraView.getWidth(), mCameraView.getHeight(), false);
                mCameraView.stop();

                if (mBitmap != null) {
                    // get photo
                    File directory = new File(Environment.getExternalStorageDirectory() + File.separator + "qr_scanner");
                    if (!directory.exists()) {
                        directory.mkdirs();
                    }

                    String name = "Image_" + UUID.randomUUID().toString() + ".jpg";
                    File pictureFile = new File(directory, name);
                    try {
                        pictureFile.createNewFile();
                        FileOutputStream outputStream = new FileOutputStream(pictureFile);
                        outputStream.write(cameraKitImage.getJpeg());
                        mBitmap.compress(Bitmap.CompressFormat.JPEG, 20, outputStream);
                        getResizedBitmap(mBitmap, 80);

                        outputStream.close();
                        Toast.makeText(MainActivity.this, "Photo Path: " + pictureFile, Toast.LENGTH_LONG).show();

                        ImagePath = pictureFile.toString();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    Toast.makeText(MainActivity.this, "Empty bitmap", Toast.LENGTH_SHORT).show();
                }


                // Detecting the captured Image
                detectImageByMLkit(mBitmap);
            }

            @Override
            public void onVideo(CameraKitVideo cameraKitVideo) {

            }
        });

        View nestedScrollView = (View) findViewById(R.id.nestedScrollView2);
        mSheetBehaviour = BottomSheetBehavior.from(nestedScrollView);

        mSheetBehaviour.setState(BottomSheetBehavior.STATE_SETTLING);

        mSheetBehaviour.setPeekHeight(260);    //Set the peek height

        mSheetBehaviour.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int newState) {
                String state = "";

                switch (newState) {
                    case BottomSheetBehavior.STATE_DRAGGING: {
                        simpleTextView.setVisibility(View.GONE);
                        state = "DRAGGING";
                        break;
                    }
                    case BottomSheetBehavior.STATE_SETTLING: {
                        simpleTextView.setVisibility(View.VISIBLE);

                        state = "SETTLING";
                        break;
                    }
                    case BottomSheetBehavior.STATE_EXPANDED: {
                        simpleTextView.setVisibility(View.GONE);
                        state = "EXPANDED";
                        break;
                    }
                    case BottomSheetBehavior.STATE_COLLAPSED: {
                        simpleTextView.setVisibility(View.VISIBLE);

                        state = "COLLAPSED";
                        break;
                    }
                    case BottomSheetBehavior.STATE_HIDDEN: {
                        simpleTextView.setVisibility(View.GONE);

                        if (mSheetBehaviour.getState() == BottomSheetBehavior.STATE_HIDDEN) {
                            mSheetBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        }
                        state = "HIDDEN";
                        break;
                    }
                }
            }

            @Override
            public void onSlide(@NonNull View view, float v) {

            }
        });

        showSavedInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCameraView.stop();
                startActivity(new Intent(MainActivity.this, SavedQRActivity.class));
            }
        });

    }//onCreate closes


    private void detectImageByMLkit(final Bitmap bitmap) {
        // get image to firebase
        final FirebaseVisionImage visionImage = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionBarcodeDetectorOptions options = new FirebaseVisionBarcodeDetectorOptions.Builder()
                .setBarcodeFormats(
                        FirebaseVisionBarcode.FORMAT_QR_CODE,
                        FirebaseVisionBarcode.FORMAT_ALL_FORMATS,
                        FirebaseVisionBarcode.FORMAT_AZTEC)
                .build();


        FirebaseVisionBarcodeDetector barcodeDetector = FirebaseVision.getInstance().getVisionBarcodeDetector(options);

        barcodeDetector.detectInImage(visionImage).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
            @Override
            public void onSuccess(List<FirebaseVisionBarcode> firebaseVisionBarcodes) {
                //getting results...
                mProgressLayout.setVisibility(View.GONE);

                fetchTheResults(firebaseVisionBarcodes);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                mProgressLayout.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Wait for the camera to assign focus & try again...", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void fetchTheResults(List<FirebaseVisionBarcode> firebaseVisionBarcodes) {
        for (FirebaseVisionBarcode barcode : firebaseVisionBarcodes) {

            // Drawing the rectangle bounds :
            Rect rectBounds = barcode.getBoundingBox();
            RectangleOverlay overlay = new RectangleOverlay(graphicOverlay, rectBounds);
            graphicOverlay.add(overlay);

            int valueType = barcode.getValueType();


            if (barcode.getRawValue() == null) {

                mRelativeLayout.setVisibility(View.VISIBLE);

            } else {
                final String note_id = UUID.randomUUID().toString();
                switch (valueType) {

                    case FirebaseVisionBarcode.TYPE_TEXT:
                        // open bottom sheet to show the results
                        mSheetBehaviour.setState(BottomSheetBehavior.STATE_EXPANDED);
                        String rawValue = barcode.getRawValue();
                        mRelativeLayout.setVisibility(View.GONE);
                        String text = barcode.getDisplayValue();

                        resultTextView.setText("Detected text: " + "\n" + text);

                        Note note_raw = new Note(note_id, rawValue, ImagePath);
                        noteViewModel.insert(note_raw);

                        Toast.makeText(this, "Results : " + rawValue , Toast.LENGTH_LONG).show();

                        break;

                    case FirebaseVisionBarcode.TYPE_WIFI:

                        mSheetBehaviour.setState(BottomSheetBehavior.STATE_EXPANDED);
                        mRelativeLayout.setVisibility(View.GONE);
                        String wifiDetails = new StringBuilder("SSID: ").append(barcode.getWifi().getSsid()).append("\n")
                                .append("Password: ").append(barcode.getWifi().getPassword()).append("\n")
                                .append("Signal type: ").append(barcode.getWifi().getEncryptionType()).toString();

                        resultTextView.setText("Detected WiFi Details: " + "\n" + wifiDetails);

                        Note note_wifi = new Note(note_id, wifiDetails, ImagePath);
                        noteViewModel.insert(note_wifi);
                        Toast.makeText(this, "Results : " + wifiDetails, Toast.LENGTH_LONG).show();

                        break;

                    case FirebaseVisionBarcode.TYPE_URL:
                        mSheetBehaviour.setState(BottomSheetBehavior.STATE_EXPANDED);

                        String url = new StringBuilder("URL Tittle: ")
                                .append(barcode.getUrl().getTitle()).append("\n")
                                .append("Url: ").append(barcode.getUrl().getUrl()).toString();

                        resultTextView.setText("Detected URL: " + "\n" + url);

                        Note note_url = new Note(note_id, url, ImagePath);
                        noteViewModel.insert(note_url);

                        Toast.makeText(this, "result: " + url, Toast.LENGTH_SHORT).show();
                        break;

                    case FirebaseVisionBarcode.TYPE_CONTACT_INFO:
                        mSheetBehaviour.setState(BottomSheetBehavior.STATE_EXPANDED);
                        mRelativeLayout.setVisibility(View.GONE);
                        String contactInfo = new StringBuilder("Name: ")
                                .append(barcode.getContactInfo().getName().getFormattedName())
                                .append("\n")
                                .append("Address: ")
                                .append(barcode.getContactInfo().getAddresses().get(0).getAddressLines())
                                .append("\n")
                                .append("Email: ")
                                .append("\n").append(barcode.getContactInfo().getEmails().get(0).getAddress()).toString();

                        resultTextView.setText("Detected Contact: " + "\n" + contactInfo);
                        Note note_contact = new Note(note_id, contactInfo, ImagePath);
                        noteViewModel.insert(note_contact);

                        Toast.makeText(this, "Results : " + contactInfo, Toast.LENGTH_LONG).show();
                    default:
                        break;

                }
            }
        }
        mProgressLayout.setVisibility(View.GONE);

    }

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    @Override
    protected void onStart() {
        mCameraView.start();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mCameraView.stop();
        super.onStop();
    }

    @Override
    protected void onResume() {
        mCameraView.start();
        super.onResume();
    }

    @Override
    protected void onPause() {
        mCameraView.stop();
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        mSheetBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
        mCameraView.start();
        graphicOverlay.clear();
        if (mSheetBehaviour.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            finish();
        }
    }

}
