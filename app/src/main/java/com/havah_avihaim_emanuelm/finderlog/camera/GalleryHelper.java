package com.havah_avihaim_emanuelm.finderlog.camera;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import com.havah_avihaim_emanuelm.finderlog.firebase.firestore.FirestoreService;
import com.havah_avihaim_emanuelm.finderlog.firebase.ml_kit.MachineLearningService;
import com.havah_avihaim_emanuelm.finderlog.firebase.storage.StorageService;
import com.havah_avihaim_emanuelm.finderlog.firebase.firestore.FoundItem;
public class GalleryHelper {

    private  Context context;
    private  StorageService storageService;
    private  FirestoreService firestoreService;
    private  MachineLearningService machineLearningService;

    private Uri pendingImageUri;
    private String pendingMimeType;

    public GalleryHelper(Context context,
                         StorageService storageService,
                         FirestoreService firestoreService,
                         MachineLearningService machineLearningService) {
        this.context = context;
        this.storageService = storageService;
        this.firestoreService = firestoreService;
        this.machineLearningService = machineLearningService;
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
                String mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE));

                if (size < 3000000) {
                    this.pendingImageUri = imageUri;
                    this.pendingMimeType = mimeType;
                    // Trigger callback to show preview and Save/Cancel options
                    onReadyToDisplay.run();
                } else {
                    Toast.makeText(context, "Image is too large", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Failed to read image info", Toast.LENGTH_SHORT).show();
        }
    }

    public Uri getPendingImageUri() {
        return pendingImageUri;
    }

    public void confirmAndUploadImage(String title) {
        if (pendingImageUri == null) {
            Toast.makeText(context, "No image to upload", Toast.LENGTH_SHORT).show();
            return;
        }

        storageService.uploadFile(pendingImageUri, storagePath -> {
            if (storagePath != null) {
                machineLearningService.analyzeImageFromFirebaseStorage(storagePath, labels -> {
                    firestoreService.addItem(new FoundItem(title, storagePath, pendingMimeType, labels));
                    Toast.makeText(context, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                    clearPendingImage();
                });
            } else {
                Toast.makeText(context, "Upload failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void clearPendingImage() {
        pendingImageUri = null;
        pendingMimeType = null;
    }
}
