package com.havah_avihaim_emanuelm.finderlog.activities;

import static com.havah_avihaim_emanuelm.finderlog.repositories.Repositories.getLostRepo;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.core.view.WindowCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.havah_avihaim_emanuelm.finderlog.utils.NetworkAwareDataLoader;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.havah_avihaim_emanuelm.finderlog.matches.MatchAlgorithm;
import com.havah_avihaim_emanuelm.finderlog.R;
import com.havah_avihaim_emanuelm.finderlog.items.LostItem;
import com.havah_avihaim_emanuelm.finderlog.camera.CameraHelper;
import com.havah_avihaim_emanuelm.finderlog.camera.GalleryHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends BaseActivity {
    public static final String CHANNEL_ID = "finderlog_channel";
    private static final CharSequence CHANNEL_NAME = "FinderLog Notifications";
    private static final String CHANNEL_DESC = "Notifications for found/lost item updates";
    private DrawerLayout drawerLayout;
    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private CameraHelper cameraHelper;
    private PreviewView previewView;
    private GalleryHelper galleryHelper;
    private SimpleDateFormat sdf;
    private static final String[] REQUIRED_PERMISSIONS = new String[]{
            Manifest.permission.CAMERA
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        // Image Helpers:
        previewView= findViewById(R.id.previewView);
        galleryHelper = new GalleryHelper(this);
        cameraHelper = new CameraHelper(this, previewView);
        // Variables Start:
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationViewReports);
        setupBottomNavigation(bottomNavigationView, R.id.nav_reports);
        drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.navigationView);
        Toolbar toolbar = findViewById(R.id.toolbar);
        // report activity main cards:
        MaterialCardView uploadButton = findViewById(R.id.cardUploadImage);
        MaterialCardView btnAddReport = findViewById(R.id.cardAddReport);
        MaterialCardView btnOpenCamera = findViewById(R.id.cardOpenCamera);
        // open camera page buttons:
        Button btnCapture = findViewById(R.id.btnCapture);
        ImageButton btnCloseCamera = findViewById(R.id.btnCloseCamera);
        Button btnSaveFromCamera = findViewById(R.id.btnSaveFromCamera);
        LinearLayout cameraPreviewButtons = findViewById(R.id.cameraPreviewButtons);
        // camera image preview:
        ImageView imagePreview = findViewById(R.id.imagePreview);
        Button btnRetake = findViewById(R.id.btnRetake);
        EditText etCameraImageTitle = findViewById(R.id.etCameraImageTitle);
        // gallery image preview:
        Button saveImageFromGallery = findViewById(R.id.saveImageFromGallery);
        Button cancelImageFromGallery = findViewById(R.id.cancelImageFromGallery);
        LinearLayout galleryPreviewButtons = findViewById(R.id.galleryPreviewButtons);
        EditText etGalleryImageTitle = findViewById(R.id.etGalleryImageTitle);
        // report activity main buttons:
        Button btnSubmitLostItem = findViewById(R.id.btnSubmitLostItem);
        Button btnCancelLostItem = findViewById(R.id.btnCancelLostItem);
        ScrollView lostItemForm = findViewById(R.id.lostItemForm);
        ImageButton btnTogglePersonal = findViewById(R.id.btnTogglePersonal);
        ImageButton btnToggleClothing = findViewById(R.id.btnToggleClothing);
        ImageButton btnToggleTech = findViewById(R.id.btnToggleTech);
        ImageButton btnToggleOther = findViewById(R.id.btnToggleOther);
        HorizontalScrollView personalScroll = findViewById(R.id.personalItemsScroll);
        HorizontalScrollView clothingScroll = findViewById(R.id.clothingDetailsScroll);
        HorizontalScrollView techScroll = findViewById(R.id.techItemsScroll);
        HorizontalScrollView otherScroll = findViewById(R.id.otherItemsScroll);
        // Variables End.

        // Toolbar handling:
        setSupportActionBar(toolbar);
        Window window = getWindow();
        WindowCompat.setDecorFitsSystemWindows(window, false);
        window.setStatusBarColor(Color.TRANSPARENT);
        toolbar.setNavigationIcon(R.drawable.dots);
        toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        // date picker:
        TextView tvLostDate = findViewById(R.id.etLostDate);
        String today = sdf.format(new Date());
        tvLostDate.post(() -> tvLostDate.setText(today));

        tvLostDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    MainActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        Calendar pickedCal = Calendar.getInstance();
                        pickedCal.set(selectedYear, selectedMonth, selectedDay);
                        String formattedDate = sdf.format(pickedCal.getTime());
                        tvLostDate.setText(formattedDate);
                    },
                    year, month, day
            );
            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
            datePickerDialog.show();
        });



        // Drawer item handling
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_about) {
                buildAboutDialog(this).create().show();
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
            } else if (id == R.id.nav_exit) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Exit");
                builder.setMessage("Are you sure you want to exit?");
                builder.setPositiveButton("OK", (dialog, which) -> finishAffinity());
                builder.setNegativeButton("Cancel", null);

                builder.show();

            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // Upload button click
        uploadButton.setOnClickListener(v -> uploadImageFromGallery());

        // camera opening code:
        previewView.setVisibility(View.GONE);
        btnCapture.setVisibility(View.GONE);
        // open camera button click and permissions check
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
                // Request permissions if not granted
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
            }
        });

        // Capture button click listener
        btnCapture.setOnClickListener(v -> cameraHelper.takePhoto(() -> {
        }));
        // Close button click listener
        btnCloseCamera.setOnClickListener(v -> {
            // Hide the camera preview and buttons
            previewView.setVisibility(View.GONE);
            btnCapture.setVisibility(View.GONE);
            btnCloseCamera.setVisibility(View.GONE);
            btnAddReport.setVisibility(View.VISIBLE);
            btnOpenCamera.setVisibility(View.VISIBLE);
            uploadButton.setVisibility(View.VISIBLE);
            cameraHelper.closeCamera();
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
        // Cancel button click listener
        btnCancelLostItem.setOnClickListener(v -> {
            // clean the form:
            ((EditText) findViewById(R.id.etTitle)).setText("");
            ((EditText) findViewById(R.id.etClientName)).setText("");
            ((EditText) findViewById(R.id.etClientPhone)).setText("");
            ((TextView) findViewById(R.id.etLostDate)).setText(getString(R.string.no_date_selected));
            clearAllCheckboxes();
            closeAllCategoryScrolls();
            // Close Form
            lostItemForm.setVisibility(View.GONE);

            // Show buttons:
            uploadButton.setVisibility(View.VISIBLE);
            btnAddReport.setVisibility(View.VISIBLE);
            btnOpenCamera.setVisibility(View.VISIBLE);
        });
        // Submit button click listener
        btnSubmitLostItem.setOnClickListener(v -> submitLostItemReport());
        // Category buttons click listeners
        btnTogglePersonal.setOnClickListener(v ->
                toggleScrollAndIcon(personalScroll, btnTogglePersonal, R.drawable.switch_on, R.drawable.switch_off));
        // Category buttons click listeners
        btnToggleClothing.setOnClickListener(v -> toggleScrollAndIcon(clothingScroll, btnToggleClothing, R.drawable.switch_on, R.drawable.switch_off));
        btnToggleTech.setOnClickListener(v -> toggleScrollAndIcon(techScroll, btnToggleTech, R.drawable.switch_on, R.drawable.switch_off));
        btnToggleOther.setOnClickListener(v -> toggleScrollAndIcon(otherScroll, btnToggleOther, R.drawable.switch_on, R.drawable.switch_off));
        // Retake button click listener
        btnRetake.setOnClickListener(v -> {
            // Show the camera preview and buttons and clear the last image
            imagePreview.setVisibility(View.GONE);
            imagePreview.setImageDrawable(null);
            etCameraImageTitle.setVisibility(View.GONE);
            etCameraImageTitle.setText("");
            //clear all image instances
            cameraHelper.clearPendingImage();
            cameraPreviewButtons.setVisibility(View.GONE);
            cameraHelper.startCamera();
            previewView.setVisibility(View.VISIBLE);
            btnCapture.setVisibility(View.VISIBLE);
            btnCloseCamera.setVisibility(View.VISIBLE);
        });
        // Save button click listener
        btnSaveFromCamera.setOnClickListener(v -> {
            // Hide the buttons:
            String imageTitle = etCameraImageTitle.getText().toString().trim();
            if (imageTitle.isEmpty()) {
                Toast.makeText(this, "Please enter a title for the image", Toast.LENGTH_SHORT).show();
                return;
            }
            cameraHelper.confirmAndUploadImage(imageTitle);
            btnSaveFromCamera.setVisibility(View.GONE);
            cameraPreviewButtons.setVisibility(View.GONE);
            etCameraImageTitle.setVisibility(View.GONE);
            etCameraImageTitle.setText("");
            imagePreview.setVisibility(View.GONE);
            // Show report page buttons:
            findViewById(R.id.cardUploadImage).setVisibility(View.VISIBLE);
            findViewById(R.id.cardOpenCamera).setVisibility(View.VISIBLE);
            findViewById(R.id.cardAddReport).setVisibility(View.VISIBLE);
        });
        //
        saveImageFromGallery.setOnClickListener(v -> {
            // Hide the buttons:
            String imageTitle = etGalleryImageTitle.getText().toString().trim();
            if (imageTitle.isEmpty()) {
                Toast.makeText(this, "Please enter a title for the image", Toast.LENGTH_SHORT).show();
                return;
            }
            galleryHelper.confirmAndUploadImage(imageTitle);
            imagePreview.setVisibility(View.GONE);
            imagePreview.setImageDrawable(null);
            galleryPreviewButtons.setVisibility(View.GONE);
            etGalleryImageTitle.setVisibility(View.GONE);
            etGalleryImageTitle.setText("");
            // Show report page buttons:
            findViewById(R.id.cardUploadImage).setVisibility(View.VISIBLE);
            findViewById(R.id.cardOpenCamera).setVisibility(View.VISIBLE);
            findViewById(R.id.cardAddReport).setVisibility(View.VISIBLE);
        });
        // Cancel button click listener
        cancelImageFromGallery.setOnClickListener(v -> {
            // Hide the buttons:
            galleryHelper.clearPendingImage();
            imagePreview.setVisibility(View.GONE);
            imagePreview.setImageDrawable(null);
            galleryPreviewButtons.setVisibility(View.GONE);
            etGalleryImageTitle.setVisibility(View.GONE);
            etGalleryImageTitle.setText("");
            // Show report page buttons:
            findViewById(R.id.cardUploadImage).setVisibility(View.VISIBLE);
            findViewById(R.id.cardOpenCamera).setVisibility(View.VISIBLE);
            findViewById(R.id.cardAddReport).setVisibility(View.VISIBLE);
        });
        // Create notification channel
        createNotificationChannel();
    }
    // Handle permission request result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Check if the camera permission is granted
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            // Check if all permissions are granted
            if (allPermissionsGranted()) {
                // start camera
                cameraHelper.startCamera();
                // Show the camera preview and buttons
                previewView.setVisibility(View.VISIBLE);
                findViewById(R.id.btnCapture).setVisibility(View.VISIBLE);
                findViewById(R.id.btnCloseCamera).setVisibility(View.VISIBLE);
                findViewById(R.id.cardAddReport).setVisibility(View.GONE);
                findViewById(R.id.cardOpenCamera).setVisibility(View.GONE);
                findViewById(R.id.cardUploadImage).setVisibility(View.GONE);
            } else {
                // Show a dialog to explain why the permission is required
                for (String permission : REQUIRED_PERMISSIONS) {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                        showPermissionDeniedDialog();
                        return;
                    }
                }
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show();
            }
        }
    }
    // Show a dialog to explain why the permission is required
    private void showPermissionDeniedDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Permission Required")
                .setMessage("Camera permission was denied permanently. Please enable it manually in settings.")
                .setPositiveButton("OK", null)
                .show();
    }
    // About dialog builder
    private AlertDialog.Builder buildAboutDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("About the App");
        // Set the app name and version
        String message = "App Name: FinderLog\n" +
                "Package: " + context.getPackageName() + "\n\n" +
                "Android Version: " + android.os.Build.VERSION.RELEASE + "\n" +
                "API: " + android.os.Build.VERSION.SDK_INT + "\n" +
                "Device: " + android.os.Build.MODEL + "\n\n" +
                "Submitted by:\n" +
                "Hava Haviv\n" +
                "Emanuel Malloul\n" +
                "Avihai Mordechay\n\n" +
                "Submission Date: 29.06.2025";

        builder.setMessage(message);
        builder.setPositiveButton("OK", null);

        return builder;
    }

    /**
     * Launches gallery image picker using the new ActivityResult API.
     */
    private void uploadImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }
    // Image picker launcher
    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                // Handle the result of the image picker
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    // Get the selected image URI
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        // Handle the selected image
                        galleryHelper.handleSelectedImage(imageUri, () -> {
                            ImageView imagePreview = findViewById(R.id.imagePreview);
                            EditText etGalleryImageTitle = findViewById(R.id.etGalleryImageTitle);

                            imagePreview.setImageURI(imageUri);
                            imagePreview.setVisibility(View.VISIBLE);
                            etGalleryImageTitle.setVisibility(View.VISIBLE);
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
        // Hide the buttons:
        findViewById(R.id.btnCapture).setVisibility(View.GONE);
        findViewById(R.id.btnCloseCamera).setVisibility(View.GONE);
        findViewById(R.id.previewView).setVisibility(View.GONE);
        // Show the camera preview and buttons
        findViewById(R.id.btnRetake).setVisibility(View.VISIBLE);
        findViewById(R.id.btnSaveFromCamera).setVisibility(View.VISIBLE);
        ImageView imagePreview = findViewById(R.id.imagePreview);
        LinearLayout cameraPreviewButtons = findViewById(R.id.cameraPreviewButtons);
        EditText etCameraImageTitle = findViewById(R.id.etCameraImageTitle);
        // Show the camera preview and buttons
        imagePreview.setImageBitmap(bitmap);
        imagePreview.setVisibility(View.VISIBLE);
        cameraPreviewButtons.setVisibility(View.VISIBLE);
        etCameraImageTitle.setVisibility(View.VISIBLE);
        // Show report page buttons:
        findViewById(R.id.cardUploadImage).setVisibility(View.GONE);
        findViewById(R.id.cardOpenCamera).setVisibility(View.GONE);
        findViewById(R.id.cardAddReport).setVisibility(View.GONE);
    }
    // Submit lost item report function:
    private void submitLostItemReport() {
        EditText etTitle = findViewById(R.id.etTitle);
        EditText etClientName = findViewById(R.id.etClientName);
        EditText etClientPhone = findViewById(R.id.etClientPhone);
        TextView etLostDate = findViewById(R.id.etLostDate);
        // Get the report data
        String title = etTitle.getText().toString().trim();
        String clientName = etClientName.getText().toString().trim();
        String clientPhone = etClientPhone.getText().toString().trim();
        String lostDateStr = etLostDate.getText().toString().trim();
        String description = getSelectedItemsDescription();
        // Validate the report data
        if (title.isEmpty() || clientName.isEmpty() || clientPhone.isEmpty() || description.isEmpty() || lostDateStr.isEmpty()) {
            View rootView = findViewById(android.R.id.content);
            Snackbar snackbar = Snackbar.make(rootView, "Please fill all fields", Snackbar.LENGTH_SHORT);
            snackbar.show();
            return;
        }
        // Convert the date string to a Date object
        Date lostDate;
        try {
            lostDate = sdf.parse(lostDateStr);
        } catch (Exception e) {
            return;
        }
        // Create LostItem object and add to FireStore
        LostItem lostItem = new LostItem(clientName, clientPhone, description, "open", title, lostDate, new Date());
        if (NetworkAwareDataLoader.isNetworkAvailable(this)) {
            firestoreService.addItem(lostItem, item -> {
                getLostRepo().addItem(item);
                MatchAlgorithm matchAlgorithm = new MatchAlgorithm(this);
                // Show popup message
                View rootView = findViewById(android.R.id.content);
                Snackbar snackbar = Snackbar.make(rootView, "Report submitted!", Snackbar.LENGTH_SHORT);
                snackbar.show();
                Log.d("submitLostItemReport", "before startMatchingThread.");
                new Thread(() -> matchAlgorithm.startMatchingThread(matchAlgorithm.convertToList(description), lostItem)).start();
            });
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("No internet connection")
                    .setMessage("Failed to upload report. \nPlease check your internet connection and try again.")
                    .setPositiveButton("OK", null)
                    .show();
        }
        // Clear the form
        etTitle.setText("");
        etClientName.setText("");
        etClientPhone.setText("");
        etLostDate.setText("");
        // Hide the buttons:
        ScrollView lostItemForm = findViewById(R.id.lostItemForm);
        lostItemForm.setVisibility(View.GONE);
        // Clear and close the form
        clearAllCheckboxes();
        closeAllCategoryScrolls();
        // Show report page buttons:
        findViewById(R.id.cardAddReport).setVisibility(View.VISIBLE);
        findViewById(R.id.cardOpenCamera).setVisibility(View.VISIBLE);
        findViewById(R.id.cardUploadImage).setVisibility(View.VISIBLE);
    }
    // Close all Forms category scrolls
    private void closeAllCategoryScrolls() {
        findViewById(R.id.personalItemsScroll).setVisibility(View.GONE);
        findViewById(R.id.clothingDetailsScroll).setVisibility(View.GONE);
        findViewById(R.id.techItemsScroll).setVisibility(View.GONE);
        findViewById(R.id.otherItemsScroll).setVisibility(View.GONE);

        ((ImageButton) findViewById(R.id.btnTogglePersonal)).setImageResource(R.drawable.switch_off);
        ((ImageButton) findViewById(R.id.btnToggleClothing)).setImageResource(R.drawable.switch_off);
        ((ImageButton) findViewById(R.id.btnToggleTech)).setImageResource(R.drawable.switch_off);
        ((ImageButton) findViewById(R.id.btnToggleOther)).setImageResource(R.drawable.switch_off);
    }
    // A function to get the selected items description
    private String getSelectedItemsDescription() {
        StringBuilder selectedItems = new StringBuilder();

        int[] categoryContainerIds = new int[]{
                R.id.personalItemsContainer,
                R.id.techItemsContainer,
                R.id.otherItemsContainer,
                R.id.clothingDetailsContainer
        };
        // Iterate through each category container
        for (int containerId : categoryContainerIds) {
            LinearLayout container = findViewById(containerId);
            // Iterate through each checkbox in the container
            if (container != null) {
                int childCount = container.getChildCount();
                // Iterate through each checkbox in the container
                for (int i = 0; i < childCount; i++) {
                    View child = container.getChildAt(i);
                    // Check if the child is a checkbox
                    if (child instanceof CheckBox) {
                        CheckBox checkBox = (CheckBox) child;
                        // Check if the checkbox is checked
                        if (checkBox.isChecked()) {
                            if (selectedItems.length() > 0) {
                                selectedItems.append(", ");
                            }
                            // Append the checkbox text to the selected items description
                            selectedItems.append(checkBox.getText().toString());
                        }
                    }
                }
            }
        }
        // Return the selected items description
        return selectedItems.toString();
    }
    // A function that clears all checkboxes
    private void clearAllCheckboxes() {
        int[] categoryContainerIds = new int[]{
                R.id.personalItemsContainer,
                R.id.techItemsContainer,
                R.id.otherItemsContainer,
                R.id.clothingDetailsContainer
        };
        // Iterate through each category container
        for (int containerId : categoryContainerIds) {
            LinearLayout container = findViewById(containerId);
            if (container != null) {
                int childCount = container.getChildCount();
                // Iterate through each checkbox in the container
                for (int i = 0; i < childCount; i++) {
                    View child = container.getChildAt(i);
                    if (child instanceof CheckBox) {
                        ((CheckBox) child).setChecked(false);
                    }
                }
            }
        }
    }
    // A function that toggles the scroll view and icon
    private void toggleScrollAndIcon(View scrollView, ImageButton button, int iconOnResId, int iconOffResId) {
        boolean isVisible = scrollView.getVisibility() == View.VISIBLE;
        if (isVisible) {
            scrollView.setVisibility(View.GONE);
            button.setImageResource(iconOffResId);
        } else {
            scrollView.setVisibility(View.VISIBLE);
            button.setImageResource(iconOnResId);
        }
    }
    // A function that creates the notification channel
    private void createNotificationChannel() {
        // Create the NotificationChannel
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        // Configure the channel
        channel.setDescription(CHANNEL_DESC);
        channel.enableLights(true);
        channel.enableVibration(true);
        // Register the channel with the system
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
    }

}
