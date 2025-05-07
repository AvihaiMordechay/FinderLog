package com.havah_avihaim_emanuelm.finderlog.camera;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import com.havah_avihaim_emanuelm.finderlog.firebase.storage.StorageService;

public class GalleryHelper {

    private Context context;
    private StorageService storageService;
    public GalleryHelper(Context context ,StorageService storageService) {
        this.context = context;
        this.storageService=storageService;
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

