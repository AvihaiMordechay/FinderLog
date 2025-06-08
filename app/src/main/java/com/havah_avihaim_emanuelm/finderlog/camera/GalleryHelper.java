package com.havah_avihaim_emanuelm.finderlog.camera;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
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

    public void handleSelectedImage(Uri imageUri, Runnable onReadyToDisplay) {
        try (Cursor cursor = context.getContentResolver().query(
                imageUri,
                new String[]{
                        MediaStore.Images.Media.SIZE,
                        MediaStore.Images.Media.MIME_TYPE
                },
                null, null, null
        )) {
            if (cursor != null && cursor.moveToFirst()) {
                long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE));

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

    public void confirmAndUploadImage(String imageTitle) {
        if (pendingImageUri == null) {
            Log.e("CameraX", "No photo to upload:");
            return;
        }
        if (!NetworkAwareDataLoader.isNetworkAvailable(context)) {
            Toast toast = Toast.makeText(context, "No internet connection. Please connect to upload the image.", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);  // X=0, Y=0
            toast.show();
            return;
        }

        storageService.uploadFile(pendingImageUri, storagePath -> {
            if (storagePath != null) {
                new MatchAlgorithm(context, this::clearPendingImage, imageTitle);
                Intent intent = new Intent(context, MachineLearningService.class);
                intent.setAction(MachineLearningService.ACTION_ANALYZE_IMAGE);
                intent.putExtra(MachineLearningService.EXTRA_IMAGE_URI, storagePath);
                context.startService(intent);
            } else {
                Log.e("CameraX", "Upload failed:");
            }
        });
    }

    public void clearPendingImage() {
        pendingImageUri = null;
    }

}
