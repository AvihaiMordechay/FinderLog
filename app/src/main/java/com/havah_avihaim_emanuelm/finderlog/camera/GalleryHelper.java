package com.havah_avihaim_emanuelm.finderlog.camera;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.havah_avihaim_emanuelm.finderlog.matches.MatchAlgorithm;
import com.havah_avihaim_emanuelm.finderlog.firebase.MachineLearningService;
import com.havah_avihaim_emanuelm.finderlog.firebase.StorageService;
import com.havah_avihaim_emanuelm.finderlog.utils.NetworkAwareDataLoader;

public class GalleryHelper {

    private final Context context;
    private final StorageService storageService = StorageService.getSharedInstance();

    private Uri pendingImageUri;

    public GalleryHelper(Context context) {
        this.context = context;
    }
    // A function to handle the selected image
    public void handleSelectedImage(Uri imageUri, Runnable onReadyToDisplay) {
        // Check the size of the image
        try (Cursor cursor = context.getContentResolver().query(
                imageUri,
                new String[]{
                        MediaStore.Images.Media.SIZE,
                        MediaStore.Images.Media.MIME_TYPE
                },
                null, null, null
        )) {
            if (cursor != null && cursor.moveToFirst()) {
                // Get the size of the image
                long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE));
                // Check if the size is less than 3MB and the MIME type is an image
                if (size < 3000000) {
                    this.pendingImageUri = imageUri;
                    // Trigger callback to show preview and Save/Cancel options
                    onReadyToDisplay.run();
                } else {
                    Toast.makeText(context, "Image is too large", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Log.e("ImageUtils", "Failed to read image info", e);
            Toast.makeText(context, "Failed to read image info", Toast.LENGTH_SHORT).show();
        }
    }
    // A function to confirm and upload the image
    public void confirmAndUploadImage(String imageTitle) {
        if (pendingImageUri == null) {
            Log.e("CameraX", "No photo to upload:");
            return;
        }
        // Check for internet connection
        if (!NetworkAwareDataLoader.isNetworkAvailable(context)) {
            new AlertDialog.Builder(context)
                    .setTitle("No internet connection")
                    .setMessage("Failed to upload image. \nPlease check your internet connection and try again.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }
        // Upload the image to Firebase Storage
        storageService.uploadFile(pendingImageUri, storagePath -> {
            if (storagePath != null) {
                // Start the Machine Learning service
                new MatchAlgorithm(context, this::clearPendingImage, imageTitle);
                Intent intent = new Intent(context, MachineLearningService.class);
                // Send the image path to the service
                intent.setAction(MachineLearningService.ACTION_ANALYZE_IMAGE);
                intent.putExtra(MachineLearningService.EXTRA_IMAGE_URI, storagePath);
                // Start the service
                context.startService(intent);
            } else {
                Log.e("CameraX", "Upload failed:");
            }
        });
    }
    // A function to clear the pending image URI
    public void clearPendingImage() {
        pendingImageUri = null;
    }

}
