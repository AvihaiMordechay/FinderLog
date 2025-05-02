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
    public void uploadFile(Uri fileUri) {
        String extension = getFileExtension(String.valueOf(fileUri));
        String randomPath = "uploads/" + UUID.randomUUID().toString() + "." + extension;
        StorageReference fileRef = rootRef.child(randomPath);

        UploadTask uploadTask = fileRef.putFile(fileUri);
        uploadTask
                .addOnSuccessListener(taskSnapshot ->
                        fileRef.getDownloadUrl()
                                .addOnSuccessListener(uri -> onUploadSuccess(uri.toString(), randomPath))
                                .addOnFailureListener(this::onUploadFailure))
                .addOnFailureListener(this::onUploadFailure);
    }


    // Delete a file at a given path
    public void deleteFile(String storagePath) {
        StorageReference fileRef = rootRef.child(storagePath);

        fileRef.delete()
                .addOnSuccessListener(aVoid -> onDeleteSuccess("File deleted: " + storagePath))
                .addOnFailureListener(this::onDeleteFailure);
    }

    // Called when upload succeeds
    private void onUploadSuccess(String downloadUrl, String storagePath) {
        Log.d("StorageService", "Upload successful: " + storagePath + " | URL: " + downloadUrl);
    }

    // Called when upload fails
    private void onUploadFailure(Exception e) {
        Log.e("StorageService", "Upload failed", e);
    }

    // Called when deletion succeeds
    private void onDeleteSuccess(String message) {
        Log.d("StorageService", message);
    }

    // Called when deletion fails
    private void onDeleteFailure(Exception e) {
        Log.e("StorageService", "Deletion failed", e);
    }
}
