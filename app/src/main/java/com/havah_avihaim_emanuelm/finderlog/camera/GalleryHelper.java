package com.havah_avihaim_emanuelm.finderlog.camera;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import com.havah_avihaim_emanuelm.finderlog.firebase.firestore.FirestoreService;
import com.havah_avihaim_emanuelm.finderlog.firebase.ml_kit.MachineLearningService;
import com.havah_avihaim_emanuelm.finderlog.firebase.storage.StorageService;

public class GalleryHelper {

    private Context context;
    private StorageService storageService;
    private FirestoreService firestoreService;
    private MachineLearningService machineLearningService;

    public GalleryHelper(Context context, StorageService storageService, FirestoreService firestoreService, MachineLearningService machineLearningService) {
        this.context = context;
        this.storageService = storageService;
        this.firestoreService = firestoreService;
        this.machineLearningService = machineLearningService;
    }

    public void handleSelectedImage(Uri imageUri) {
        try (Cursor cursor = context.getContentResolver().query(
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
                long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE));
                String mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE));

                if (size < 3000000) {
                    storageService.uploadFile(imageUri, storagePath -> {
                        if (storagePath != null) {
                            // Save to Firestore or other actions
                            // TODO: SERVICE MACHINE LEARNING IMPLEMENTATION
                            machineLearningService.analyzeImageFromFirebaseStorage(storagePath);
                        } else {
                            Toast.makeText(context, "Upload failed", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(context, "Image is too large", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Failed to read image info", Toast.LENGTH_SHORT).show();
        }
    }
}

