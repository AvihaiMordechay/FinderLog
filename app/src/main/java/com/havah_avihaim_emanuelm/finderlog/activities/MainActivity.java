package com.havah_avihaim_emanuelm.finderlog.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.content.pm.PackageManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.core.view.WindowCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.havah_avihaim_emanuelm.finderlog.R;
import com.havah_avihaim_emanuelm.finderlog.adapters.Repositories;
import com.havah_avihaim_emanuelm.finderlog.firebase.firestore.FoundItem;
import com.havah_avihaim_emanuelm.finderlog.firebase.firestore.LostItem;
import com.havah_avihaim_emanuelm.finderlog.camera.CameraHelper;
import com.havah_avihaim_emanuelm.finderlog.camera.GalleryHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class MainActivity extends BaseActivity {
    private DrawerLayout drawerLayout;
    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private static final String[] REQUIRED_PERMISSIONS = new String[]{
            Manifest.permission.CAMERA
    };
    private CameraHelper cameraHelper;
    private PreviewView previewView;

    private GalleryHelper galleryHelper;
    private Bitmap bitmapToProcess;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Variables Start:
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationViewReports);
        setupBottomNavigation(bottomNavigationView, R.id.nav_reports);
        drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.navigationView);
        Toolbar toolbar = findViewById(R.id.toolbar);
        MaterialCardView uploadButton=findViewById(R.id.cardUploadImage);
        previewView = findViewById(R.id.previewView);
        MaterialCardView btnOpenCamera=findViewById(R.id.cardOpenCamera);
        Button btnCapture = findViewById(R.id.btnCapture);
        ImageButton btnCloseCamera = findViewById(R.id.btnCloseCamera);
        MaterialCardView btnAddReport = findViewById(R.id.cardAddReport);
        Button btnSubmitLostItem = findViewById(R.id.btnSubmitLostItem);
        Button btnCancelLostItem =  findViewById(R.id.btnCancelLostItem);
        ScrollView lostItemForm = findViewById(R.id.lostItemForm);
        ImageView imagePreview = findViewById(R.id.imagePreview);
        LinearLayout cameraPreviewButtons = findViewById(R.id.cameraPreviewButtons);
        Button btnRetake = findViewById(R.id.btnRetake);
        Button btnSaveFromCamera = findViewById(R.id.btnSaveFromCamera);
        galleryHelper = new GalleryHelper(this, storageService, firestoreService, machineLearningService);
        cameraHelper = new CameraHelper(this, previewView, storageService, firestoreService, machineLearningService);
        Button saveImageFromGallery= findViewById(R.id.saveImageFromGallery);
        Button cancelImageFromGallery= findViewById(R.id.cancelImageFromGallery);
        LinearLayout galleryPreviewButtons= findViewById(R.id.galleryPreviewButtons);
        // Variables End.

        if (!Repositories.getFoundRepo().isLoaded()) {
            Log.d("YourTag", "Your message here found repo");
            firestoreService.getItems(FoundItem.class, Repositories.getFoundRepo()::setItems);
        }
        if (!Repositories.getLostRepo().isLoaded()) {
            firestoreService.getItems(LostItem.class, Repositories.getLostRepo()::setItems);
        }

        setSupportActionBar(toolbar);
        Window window = getWindow();
        WindowCompat.setDecorFitsSystemWindows(window, false);
        window.setStatusBarColor(Color.TRANSPARENT);
        toolbar.setNavigationIcon(R.drawable.dots);
        toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // Drawer item handling
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_about) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("About the App");


                String message = "App Name: FinderLog\n" +
                        "Package: " + getPackageName() + "\n\n" +
                        "Android Version: " + android.os.Build.VERSION.RELEASE + "\n" +
                        "SDK: " + android.os.Build.VERSION.SDK_INT + "\n" +
                        "Device: " + android.os.Build.MODEL + "\n\n" +
                        "Submitted by:\n" +
                        "Hava Haviv\n" +
                        "Emanuel Melloul\n" +
                        "Avihai Mordechay\n\n" +
                        "Submission Date: May 21, 2025";

                builder.setMessage(message);

                builder.setPositiveButton("OK", null);
                AlertDialog dialog = builder.create();
                dialog.show();

                // About action
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
            } else if (id == R.id.nav_exit) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Exit");
                builder.setMessage("Are you sure you want to exit?");
                builder.setPositiveButton("OK", (dialog, which) -> {
                    finishAffinity();
                });
                builder.setNegativeButton("Cancel", null);

                builder.show();

            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // Upload button click
        uploadButton.setOnClickListener(v -> uploadImageFromGallery());

//        showImageFromUrl("https://firebasestorage.googleapis.com/v0/b/finderlog-1f757.firebasestorage.app/o/uploads%2Fd64ab902-af62-4059-a434-d1718be44717.?alt=media&token=b3d43cff-2ef1-499a-b4c4-e82e1ac638f0");

        // camera opening code:
        previewView.setVisibility(View.GONE);
        btnCapture.setVisibility(View.GONE);

        btnOpenCamera.setOnClickListener(v -> {
            // Request permissions if not granted
            if (allPermissionsGranted()) {
                cameraHelper.startCamera();
                previewView.setVisibility(View.VISIBLE);
                btnCapture.setVisibility(View.VISIBLE);
                btnCloseCamera.setVisibility(View.VISIBLE);
                btnAddReport.setVisibility(View.GONE);
                btnOpenCamera.setVisibility(View.GONE);
                uploadButton.setVisibility(View.GONE);
            } else {
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
            }
        });

        // Capture button click listener
        btnCapture.setOnClickListener(v -> cameraHelper.takePhoto(() -> {}));

        btnCloseCamera.setOnClickListener(v -> {
            previewView.setVisibility(View.GONE);
            btnCapture.setVisibility(View.GONE);
            btnCloseCamera.setVisibility(View.GONE);
            btnAddReport.setVisibility(View.VISIBLE);
            btnOpenCamera.setVisibility(View.VISIBLE);
            uploadButton.setVisibility(View.VISIBLE);
        });

        // report button + Report Form implementation
        btnAddReport.setOnClickListener(v -> {
            // Hide the buttons:
            btnAddReport.setVisibility(View.GONE);
            btnOpenCamera.setVisibility(View.GONE);
            uploadButton.setVisibility(View.GONE);
            // Show the Form:
            lostItemForm.setVisibility(View.VISIBLE);
        });
        btnCancelLostItem.setOnClickListener(v -> {
            // Close Form
            lostItemForm.setVisibility(View.GONE);
            // Show buttons:
            uploadButton.setVisibility(View.VISIBLE);
            btnAddReport.setVisibility(View.VISIBLE);
            btnOpenCamera.setVisibility(View.VISIBLE);
        });

        btnSubmitLostItem.setOnClickListener(v -> {
            submitLostItemReport();
        });

        btnRetake.setOnClickListener(v -> {
            imagePreview.setVisibility(View.GONE);
            imagePreview.setImageDrawable(null);
            this.bitmapToProcess.recycle(); // deletes the bitmap from memory
            cameraHelper.clearPendingImage();
            this.bitmapToProcess = null;
            cameraPreviewButtons.setVisibility(View.GONE);
            cameraHelper.startCamera();
            previewView.setVisibility(View.VISIBLE);
            btnCapture.setVisibility(View.VISIBLE);
            btnCloseCamera.setVisibility(View.VISIBLE);
        });
        btnSaveFromCamera.setOnClickListener(v -> {
            cameraHelper.confirmAndUploadImage("test2");
            btnSaveFromCamera.setVisibility(View.GONE);
            cameraPreviewButtons.setVisibility(View.GONE);
            imagePreview.setVisibility(View.GONE);
            findViewById(R.id.cardUploadImage).setVisibility(View.VISIBLE);
            findViewById(R.id.cardOpenCamera).setVisibility(View.VISIBLE);
            findViewById(R.id.cardAddReport).setVisibility(View.VISIBLE);
            bitmapToProcess = null;
        });
        saveImageFromGallery.setOnClickListener(v -> {
            galleryHelper.confirmAndUploadImage("TEST1");
            imagePreview.setVisibility(View.GONE);
            imagePreview.setImageDrawable(null);
            galleryPreviewButtons.setVisibility(View.GONE);
            findViewById(R.id.cardUploadImage).setVisibility(View.VISIBLE);
            findViewById(R.id.cardOpenCamera).setVisibility(View.VISIBLE);
            findViewById(R.id.cardAddReport).setVisibility(View.VISIBLE);
        });
        cancelImageFromGallery.setOnClickListener(v -> {
            galleryHelper.clearPendingImage();
            imagePreview.setVisibility(View.GONE);
            imagePreview.setImageDrawable(null);
            galleryPreviewButtons.setVisibility(View.GONE);
            findViewById(R.id.cardUploadImage).setVisibility(View.VISIBLE);
            findViewById(R.id.cardOpenCamera).setVisibility(View.VISIBLE);
            findViewById(R.id.cardAddReport).setVisibility(View.VISIBLE);
        });

    }

    /**
     * Launches gallery image picker using the new ActivityResult API.
     */
    private void uploadImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }
    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        galleryHelper.handleSelectedImage(imageUri, () -> {
                            ImageView imagePreview = findViewById(R.id.imagePreview);
                            imagePreview.setImageURI(imageUri);
                            imagePreview.setVisibility(View.VISIBLE);
                            findViewById(R.id.galleryPreviewButtons).setVisibility(View.VISIBLE);
                            findViewById(R.id.cardUploadImage).setVisibility(View.GONE);
                            findViewById(R.id.cardOpenCamera).setVisibility(View.GONE);
                            findViewById(R.id.cardAddReport).setVisibility(View.GONE);
                        });
                    }
                }
            });

    // opening the camera code:
    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
                return false;
        }
        return true;
    }
    public void showCameraImagePreview(Bitmap bitmap) {
        findViewById(R.id.btnCapture).setVisibility(View.GONE);
        findViewById(R.id.btnCloseCamera).setVisibility(View.GONE);
        findViewById(R.id.previewView).setVisibility(View.GONE);
        findViewById(R.id.btnRetake).setVisibility(View.VISIBLE);
        findViewById(R.id.btnSaveFromCamera).setVisibility(View.VISIBLE);
        ImageView imagePreview = findViewById(R.id.imagePreview);
        LinearLayout cameraPreviewButtons = findViewById(R.id.cameraPreviewButtons);

        imagePreview.setImageBitmap(bitmap);
        imagePreview.setVisibility(View.VISIBLE);
        cameraPreviewButtons.setVisibility(View.VISIBLE);

        findViewById(R.id.cardUploadImage).setVisibility(View.GONE);
        findViewById(R.id.cardOpenCamera).setVisibility(View.GONE);
        findViewById(R.id.cardAddReport).setVisibility(View.GONE);
        this.bitmapToProcess = bitmap;
    }
    private void submitLostItemReport() {
        EditText etTitle = findViewById(R.id.etTitle);
        EditText etClientName = findViewById(R.id.etClientName);
        EditText etClientPhone = findViewById(R.id.etClientPhone);
        EditText etDescription = findViewById(R.id.etDescription);
        EditText etLostDate = findViewById(R.id.etLostDate);

        String title = etTitle.getText().toString().trim();
        String clientName = etClientName.getText().toString().trim();
        String clientPhone = etClientPhone.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String lostDateStr = etLostDate.getText().toString().trim();

        if (title.isEmpty() || clientName.isEmpty() || clientPhone.isEmpty() || description.isEmpty() || lostDateStr.isEmpty()) {
            View rootView = findViewById(android.R.id.content);
            Snackbar snackbar = Snackbar.make(rootView, "Please fill all fields", Snackbar.LENGTH_SHORT);
            snackbar.show();
            return;
        }

        Date lostDate;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            lostDate = sdf.parse(lostDateStr);
        } catch (Exception e) {
            View rootView = findViewById(android.R.id.content);
            Snackbar snackbar = Snackbar.make(rootView, "Invalid date format. Use yyyy-MM-dd", Snackbar.LENGTH_SHORT);
            snackbar.show();
            return;
        }
        // Create LostItem object and add to FireStore
        LostItem lostItem = new LostItem(clientName, clientPhone, description, "open",title, lostDate, new Date());
        firestoreService.addItem(lostItem);
        // Add the item to the repository
        Repositories.getLostRepo().addItem(lostItem);
        // Show popup message
        View rootView = findViewById(android.R.id.content);
        Snackbar snackbar = Snackbar.make(rootView, "Report submitted!", Snackbar.LENGTH_SHORT);
        snackbar.show();

        etTitle.setText("");
        etClientName.setText("");
        etClientPhone.setText("");
        etDescription.setText("");
        etLostDate.setText("");

        ScrollView lostItemForm = findViewById(R.id.lostItemForm);
        lostItemForm.setVisibility(View.GONE);

        findViewById(R.id.cardAddReport).setVisibility(View.VISIBLE);
        findViewById(R.id.cardOpenCamera).setVisibility(View.VISIBLE);
        findViewById(R.id.cardUploadImage).setVisibility(View.VISIBLE);
    }


}