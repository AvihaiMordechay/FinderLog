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
    private Bitmap bitmapToProcess;
    private SimpleDateFormat sdf;
    private static final String[] REQUIRED_PERMISSIONS = new String[]{
            Manifest.permission.CAMERA
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        // Variables Start:
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationViewReports);
        setupBottomNavigation(bottomNavigationView, R.id.nav_reports);
        drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.navigationView);
        Toolbar toolbar = findViewById(R.id.toolbar);
        MaterialCardView uploadButton = findViewById(R.id.cardUploadImage);
        previewView = findViewById(R.id.previewView);
        MaterialCardView btnOpenCamera = findViewById(R.id.cardOpenCamera);
        Button btnCapture = findViewById(R.id.btnCapture);
        ImageButton btnCloseCamera = findViewById(R.id.btnCloseCamera);
        MaterialCardView btnAddReport = findViewById(R.id.cardAddReport);
        Button btnSubmitLostItem = findViewById(R.id.btnSubmitLostItem);
        Button btnCancelLostItem = findViewById(R.id.btnCancelLostItem);
        ScrollView lostItemForm = findViewById(R.id.lostItemForm);
        ImageView imagePreview = findViewById(R.id.imagePreview);
        LinearLayout cameraPreviewButtons = findViewById(R.id.cameraPreviewButtons);
        Button btnRetake = findViewById(R.id.btnRetake);
        Button btnSaveFromCamera = findViewById(R.id.btnSaveFromCamera);
        galleryHelper = new GalleryHelper(this);
        cameraHelper = new CameraHelper(this, previewView);
        Button saveImageFromGallery = findViewById(R.id.saveImageFromGallery);
        Button cancelImageFromGallery = findViewById(R.id.cancelImageFromGallery);
        LinearLayout galleryPreviewButtons = findViewById(R.id.galleryPreviewButtons);
        ImageButton btnTogglePersonal = findViewById(R.id.btnTogglePersonal);
        ImageButton btnToggleClothing = findViewById(R.id.btnToggleClothing);
        ImageButton btnToggleTech = findViewById(R.id.btnToggleTech);
        ImageButton btnToggleOther = findViewById(R.id.btnToggleOther);
        EditText etCameraImageTitle = findViewById(R.id.etCameraImageTitle);
        EditText etGalleryImageTitle = findViewById(R.id.etGalleryImageTitle);


        HorizontalScrollView personalScroll = findViewById(R.id.personalItemsScroll);
        HorizontalScrollView clothingScroll = findViewById(R.id.clothingDetailsScroll);
        HorizontalScrollView techScroll = findViewById(R.id.techItemsScroll);
        HorizontalScrollView otherScroll = findViewById(R.id.otherItemsScroll);
        // Variables End.

        setSupportActionBar(toolbar);
        Window window = getWindow();
        WindowCompat.setDecorFitsSystemWindows(window, false);
        window.setStatusBarColor(Color.TRANSPARENT);
        toolbar.setNavigationIcon(R.drawable.dots);
        toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        TextView tvLostDate = findViewById(R.id.etLostDate);
        String today = sdf.format(new Date());
        tvLostDate.setText(today);

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
        btnCapture.setOnClickListener(v -> cameraHelper.takePhoto(() -> {
        }));

        btnCloseCamera.setOnClickListener(v -> {
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

        btnSubmitLostItem.setOnClickListener(v -> submitLostItemReport());
        btnTogglePersonal.setOnClickListener(v ->
                toggleScrollAndIcon(personalScroll, btnTogglePersonal, R.drawable.switch_on, R.drawable.switch_off));

        btnToggleClothing.setOnClickListener(v -> toggleScrollAndIcon(clothingScroll, btnToggleClothing, R.drawable.switch_on, R.drawable.switch_off));

        btnToggleTech.setOnClickListener(v -> toggleScrollAndIcon(techScroll, btnToggleTech, R.drawable.switch_on, R.drawable.switch_off));

        btnToggleOther.setOnClickListener(v -> toggleScrollAndIcon(otherScroll, btnToggleOther, R.drawable.switch_on, R.drawable.switch_off));
        btnRetake.setOnClickListener(v -> {
            imagePreview.setVisibility(View.GONE);
            imagePreview.setImageDrawable(null);
            etCameraImageTitle.setVisibility(View.GONE);
            etCameraImageTitle.setText("");
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
            String imageTitle = etCameraImageTitle.getText().toString().trim();
            cameraHelper.confirmAndUploadImage(imageTitle);
            btnSaveFromCamera.setVisibility(View.GONE);
            cameraPreviewButtons.setVisibility(View.GONE);
            etCameraImageTitle.setVisibility(View.GONE);
            etCameraImageTitle.setText("");
            imagePreview.setVisibility(View.GONE);
            findViewById(R.id.cardUploadImage).setVisibility(View.VISIBLE);
            findViewById(R.id.cardOpenCamera).setVisibility(View.VISIBLE);
            findViewById(R.id.cardAddReport).setVisibility(View.VISIBLE);
            bitmapToProcess = null;
        });
        saveImageFromGallery.setOnClickListener(v -> {
            String imageTitle = etGalleryImageTitle.getText().toString().trim();
            galleryHelper.confirmAndUploadImage(imageTitle);
            imagePreview.setVisibility(View.GONE);
            imagePreview.setImageDrawable(null);
            galleryPreviewButtons.setVisibility(View.GONE);
            etGalleryImageTitle.setVisibility(View.GONE);
            etGalleryImageTitle.setText("");
            findViewById(R.id.cardUploadImage).setVisibility(View.VISIBLE);
            findViewById(R.id.cardOpenCamera).setVisibility(View.VISIBLE);
            findViewById(R.id.cardAddReport).setVisibility(View.VISIBLE);
        });
        cancelImageFromGallery.setOnClickListener(v -> {
            galleryHelper.clearPendingImage();
            imagePreview.setVisibility(View.GONE);
            imagePreview.setImageDrawable(null);
            galleryPreviewButtons.setVisibility(View.GONE);
            etGalleryImageTitle.setVisibility(View.GONE);
            etGalleryImageTitle.setText("");
            findViewById(R.id.cardUploadImage).setVisibility(View.VISIBLE);
            findViewById(R.id.cardOpenCamera).setVisibility(View.VISIBLE);
            findViewById(R.id.cardAddReport).setVisibility(View.VISIBLE);
        });
        createNotificationChannel();
        NetworkAwareDataLoader.loadData(this, firestoreService);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                cameraHelper.startCamera();
                previewView.setVisibility(View.VISIBLE);
                findViewById(R.id.btnCapture).setVisibility(View.VISIBLE);
                findViewById(R.id.btnCloseCamera).setVisibility(View.VISIBLE);
                findViewById(R.id.cardAddReport).setVisibility(View.GONE);
                findViewById(R.id.cardOpenCamera).setVisibility(View.GONE);
                findViewById(R.id.cardUploadImage).setVisibility(View.GONE);
            } else {
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
    private void showPermissionDeniedDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Permission Required")
                .setMessage("Camera permission was denied permanently. Please enable it manually in settings.")
                .setPositiveButton("OK", null)
                .show();
    }


    private AlertDialog.Builder buildAboutDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("About the App");

        String message = "App Name: FinderLog\n" +
                "Package: " + context.getPackageName() + "\n\n" +
                "Android Version: " + android.os.Build.VERSION.RELEASE + "\n" +
                "API: " + android.os.Build.VERSION.SDK_INT + "\n" +
                "Device: " + android.os.Build.MODEL + "\n\n" +
                "Submitted by:\n" +
                "Hava Haviv\n" +
                "Emanuel Malloul\n" +
                "Avihai Mordechay\n\n" +
                "Submission Date: May 21, 2025";

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

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
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
        findViewById(R.id.btnCapture).setVisibility(View.GONE);
        findViewById(R.id.btnCloseCamera).setVisibility(View.GONE);
        findViewById(R.id.previewView).setVisibility(View.GONE);
        findViewById(R.id.btnRetake).setVisibility(View.VISIBLE);
        findViewById(R.id.btnSaveFromCamera).setVisibility(View.VISIBLE);
        ImageView imagePreview = findViewById(R.id.imagePreview);
        LinearLayout cameraPreviewButtons = findViewById(R.id.cameraPreviewButtons);
        EditText etCameraImageTitle = findViewById(R.id.etCameraImageTitle);

        imagePreview.setImageBitmap(bitmap);
        imagePreview.setVisibility(View.VISIBLE);
        cameraPreviewButtons.setVisibility(View.VISIBLE);
        etCameraImageTitle.setVisibility(View.VISIBLE);

        findViewById(R.id.cardUploadImage).setVisibility(View.GONE);
        findViewById(R.id.cardOpenCamera).setVisibility(View.GONE);
        findViewById(R.id.cardAddReport).setVisibility(View.GONE);
        this.bitmapToProcess = bitmap;
    }

    private void submitLostItemReport() {
        EditText etTitle = findViewById(R.id.etTitle);
        EditText etClientName = findViewById(R.id.etClientName);
        EditText etClientPhone = findViewById(R.id.etClientPhone);
        TextView etLostDate = findViewById(R.id.etLostDate);

        String title = etTitle.getText().toString().trim();
        String clientName = etClientName.getText().toString().trim();
        String clientPhone = etClientPhone.getText().toString().trim();
        String lostDateStr = etLostDate.getText().toString().trim();
        String description = getSelectedItemsDescription();

        if (title.isEmpty() || clientName.isEmpty() || clientPhone.isEmpty() || description.isEmpty() || lostDateStr.isEmpty()) {
            View rootView = findViewById(android.R.id.content);
            Snackbar snackbar = Snackbar.make(rootView, "Please fill all fields", Snackbar.LENGTH_SHORT);
            snackbar.show();
            return;
        }


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
                new Thread(() -> matchAlgorithm.startMatchingThread(matchAlgorithm.convertToList(description), lostItem)).start();
            });
            // Show popup message
            View rootView = findViewById(android.R.id.content);
            Snackbar snackbar = Snackbar.make(rootView, "Report submitted!", Snackbar.LENGTH_SHORT);
            snackbar.show();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("No internet connection")
                    .setMessage("Failed to upload report. \nPlease check your internet connection and try again.")
                    .setPositiveButton("OK", null)
                    .show();
        }


        etTitle.setText("");
        etClientName.setText("");
        etClientPhone.setText("");
        etLostDate.setText("");

        ScrollView lostItemForm = findViewById(R.id.lostItemForm);
        lostItemForm.setVisibility(View.GONE);

        clearAllCheckboxes();
        closeAllCategoryScrolls();

        findViewById(R.id.cardAddReport).setVisibility(View.VISIBLE);
        findViewById(R.id.cardOpenCamera).setVisibility(View.VISIBLE);
        findViewById(R.id.cardUploadImage).setVisibility(View.VISIBLE);
    }

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

    private String getSelectedItemsDescription() {
        StringBuilder selectedItems = new StringBuilder();

        int[] categoryContainerIds = new int[]{
                R.id.personalItemsContainer,
                R.id.techItemsContainer,
                R.id.otherItemsContainer,
                R.id.clothingDetailsContainer
        };

        for (int containerId : categoryContainerIds) {
            LinearLayout container = findViewById(containerId);
            if (container != null) {
                int childCount = container.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    View child = container.getChildAt(i);
                    if (child instanceof CheckBox) {
                        CheckBox checkBox = (CheckBox) child;
                        if (checkBox.isChecked()) {
                            if (selectedItems.length() > 0) {
                                selectedItems.append(", ");
                            }
                            selectedItems.append(checkBox.getText().toString());
                        }
                    }
                }
            }
        }

        return selectedItems.toString();
    }

    private void clearAllCheckboxes() {
        int[] categoryContainerIds = new int[]{
                R.id.personalItemsContainer,
                R.id.techItemsContainer,
                R.id.otherItemsContainer,
                R.id.clothingDetailsContainer
        };

        for (int containerId : categoryContainerIds) {
            LinearLayout container = findViewById(containerId);
            if (container != null) {
                int childCount = container.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    View child = container.getChildAt(i);
                    if (child instanceof CheckBox) {
                        ((CheckBox) child).setChecked(false);
                    }
                }
            }
        }
    }

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

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription(CHANNEL_DESC);
        channel.enableLights(true);
        channel.enableVibration(true);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
    }

}
