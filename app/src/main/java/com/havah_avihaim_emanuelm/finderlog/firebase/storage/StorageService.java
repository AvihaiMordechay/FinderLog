package com.havah_avihaim_emanuelm.finderlog.firebase.storage;

import static com.google.common.io.Files.getFileExtension;

import android.net.Uri;
import android.util.Log;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

public class StorageService {

    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final StorageReference rootRef = storage.getReference();

    // Upload a file with a random generated path (e.g. uploads/uuid.jpg)
    public void uploadFile(Uri fileUri, UploadCallback callback) {
        String extension = getFileExtension(String.valueOf(fileUri));
        String randomPath = "uploads/" + UUID.randomUUID().toString() + "." + extension;
        StorageReference fileRef = rootRef.child(randomPath);

        fileRef.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot ->
                        fileRef.getDownloadUrl()
                                .addOnSuccessListener(uri -> callback.onComplete(uri.toString()))
                                .addOnFailureListener(e -> callback.onComplete(null)))
                .addOnFailureListener(e -> callback.onComplete(null));
    }


    // Delete a file at a given path
    public void deleteFile(String storagePath, DeleteCallback callback) {
        StorageReference fileRef = rootRef.child(storagePath);

        fileRef.delete()
                .addOnSuccessListener(aVoid -> callback.onComplete(true))
                .addOnFailureListener(e -> callback.onComplete(false));
    }


    public interface UploadCallback {
        void onComplete(String downloadUrl);
    }
    public interface DeleteCallback {
        void onComplete(boolean success);
    }
}

