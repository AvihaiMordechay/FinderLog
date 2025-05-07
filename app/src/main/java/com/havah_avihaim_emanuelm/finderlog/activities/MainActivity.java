package com.havah_avihaim_emanuelm.finderlog.activities;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.core.view.WindowCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.havah_avihaim_emanuelm.finderlog.R;
import com.havah_avihaim_emanuelm.finderlog.adapters.Repositories;
import com.havah_avihaim_emanuelm.finderlog.firebase.firestore.FirestoreService;
import com.havah_avihaim_emanuelm.finderlog.firebase.firestore.FoundItem;
import com.havah_avihaim_emanuelm.finderlog.firebase.firestore.LostItem;

public class MainActivity extends BaseActivity {
    private DrawerLayout drawerLayout;

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

        showImageFromUrl("https://firebasestorage.googleapis.com/v0/b/finderlog-1f757.firebasestorage.app/o/uploads%2Fd64ab902-af62-4059-a434-d1718be44717.?alt=media&token=b3d43cff-2ef1-499a-b4c4-e82e1ac638f0");
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
                        handleSelectedImage(imageUri);
                    }
                }
            });

    /**
     * Reads image metadata from selected URI using content provider.
     */
    private void handleSelectedImage(Uri imageUri) {
        try (Cursor cursor = getContentResolver().query(
                imageUri,
                new String[]{
                        MediaStore.Images.Media.SIZE,
                        MediaStore.Images.Media.MIME_TYPE,
                        MediaStore.Images.Media.WIDTH,
                        MediaStore.Images.Media.HEIGHT,
                },
                null, null, null
        )) {
            if (cursor != null && cursor.moveToFirst()) {
                // For checking the img size before uploading the image to the cloud. (limit to 3 mb?)
                long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE));
                // For saving the image in the cloud.
                String mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE));

                if (size < 3000000) {
                    storageService.uploadFile(imageUri, storagePath -> {
                        if (storagePath != null) {
                            // TODO: SERVICE MACHINE LEARNING IMPLEMENTATION
                            Log.d("storagePath", storagePath);
                            FoundItem foundItem = new FoundItem("test", storagePath, mimeType);
                            firestoreService.addItem(foundItem);
                        } else {
                            Log.e("Upload", "Upload failed");
                        }
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to read image info", Toast.LENGTH_SHORT).show();
        }
    }

    // Example
    private void showImageFromUrl(String imageUrl) {
        ImageView imageView = findViewById(R.id.imageViewPreview);
        Glide.with(this)
                .load(imageUrl)
//                .placeholder(R.drawable.placeholder) // Show temp image until the loading finish (need to save image as placeholder.png)
//                .error(R.drawable.error_image)       // In case that we have error with the image (need to save image as error_image.png)
                .into(imageView);
    }


}