package com.havah_avihaim_emanuelm.finderlog.activities;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.content.pm.PackageManager;
import android.widget.Button;
import android.widget.ImageButton;

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
import com.google.android.material.navigation.NavigationView;
import com.havah_avihaim_emanuelm.finderlog.R;
import com.havah_avihaim_emanuelm.finderlog.adapters.Repositories;
import com.havah_avihaim_emanuelm.finderlog.firebase.firestore.FirestoreService;
import com.havah_avihaim_emanuelm.finderlog.firebase.firestore.FoundItem;
import com.havah_avihaim_emanuelm.finderlog.firebase.firestore.LostItem;
import com.havah_avihaim_emanuelm.finderlog.camera.CameraHelper;
import com.havah_avihaim_emanuelm.finderlog.camera.GalleryHelper;



public class MainActivity extends BaseActivity {
    private DrawerLayout drawerLayout;
    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private static final String[] REQUIRED_PERMISSIONS = new String[]{
            Manifest.permission.CAMERA
    };
    private CameraHelper cameraHelper;
    private PreviewView previewView;

    private GalleryHelper galleryHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!Repositories.getFoundRepo().isLoaded()) {
            Log.d("YourTag", "Your message here found repo");
            new FirestoreService().getItems(FoundItem.class, Repositories.getFoundRepo()::setItems);
        }
        if (!Repositories.getLostRepo().isLoaded()) {
            new FirestoreService().getItems(LostItem.class, Repositories.getLostRepo()::setItems);
        }
        galleryHelper = new GalleryHelper(this,storageService);
        // UI setup
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationViewReports);
        setupBottomNavigation(bottomNavigationView, R.id.nav_reports);
        drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.navigationView);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Window window = getWindow();
        WindowCompat.setDecorFitsSystemWindows(window, false);
        window.setStatusBarColor(Color.TRANSPARENT);

//        WindowInsetsControllerCompat insetsController =
//                WindowCompat.getInsetsController(window, window.getDecorView());
//        insetsController.setAppearanceLightStatusBars(true);

        toolbar.setNavigationIcon(R.drawable.dots);
        toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // Drawer item handling
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if(id == R.id.nav_about) {
                // About action
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
            } else if (id == R.id.nav_exit) {
                // Exit action
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // Upload button click
        View uploadButton = findViewById(R.id.uploadButton);
        uploadButton.setOnClickListener(v -> uploadImageFromGallery());

//        showImageFromUrl("https://firebasestorage.googleapis.com/v0/b/finderlog-1f757.firebasestorage.app/o/uploads%2Fd64ab902-af62-4059-a434-d1718be44717.?alt=media&token=b3d43cff-2ef1-499a-b4c4-e82e1ac638f0");

        // camera opening code:
        previewView = findViewById(R.id.previewView);
        // Initialize CameraHelper
        cameraHelper = new CameraHelper(this, previewView);

        Button btnOpenCamera = findViewById(R.id.btnOpenCamera);
        Button btnCapture = findViewById(R.id.btnCapture);
        ImageButton btnCloseCamera = findViewById(R.id.btnCloseCamera);
        // Initially hide preview and capture button
        previewView.setVisibility(View.GONE);
        btnCapture.setVisibility(View.GONE);

        btnOpenCamera.setOnClickListener(v -> {
            // Request permissions if not granted
            if (allPermissionsGranted()) {
                cameraHelper.startCamera();
                previewView.setVisibility(View.VISIBLE);
                btnCapture.setVisibility(View.VISIBLE);
                btnCloseCamera.setVisibility(View.VISIBLE);
                btnOpenCamera.setVisibility(View.GONE);
                uploadButton.setVisibility(View.GONE);
            } else {
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
            }
        });

        // Capture button click listener
        btnCapture.setOnClickListener(v -> cameraHelper.takePhoto());


        btnCloseCamera.setOnClickListener(v -> {
//            cameraHelper.stopCamera();
            previewView.setVisibility(View.GONE);
            btnCapture.setVisibility(View.GONE);
            btnCloseCamera.setVisibility(View.GONE);
            btnOpenCamera.setVisibility(View.VISIBLE);
            uploadButton.setVisibility(View.VISIBLE);
        });
        // end

    }

    /**
     * Launches gallery image picker using the new ActivityResult API.
     */
    private void uploadImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    /**
     * Modern image picker result handler
     */
    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        galleryHelper.handleSelectedImage(imageUri);
                    }
                }
            });

    // Example
//    private void showImageFromUrl(String imageUrl) {
//        ImageView imageView = findViewById(R.id.imageViewPreview);
//        Glide.with(this)
//                .load(imageUrl)
////                .placeholder(R.drawable.placeholder) // Show temp image until the loading finish (need to save image as placeholder.png)
////                .error(R.drawable.error_image)       // In case that we have error with the image (need to save image as error_image.png)
//                .into(imageView);
//    }

    // opening the camera code:
    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
                return false;
        }
        return true;
    }


}