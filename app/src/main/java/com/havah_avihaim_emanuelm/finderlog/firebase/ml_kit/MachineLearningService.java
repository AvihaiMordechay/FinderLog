package com.havah_avihaim_emanuelm.finderlog.firebase.ml_kit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

import java.util.List;

public class MachineLearningService {
    private static final String TAG = "MLService";

    /**
     * Downloads an image from Firebase Storage using its Uri,
     * then processes it through ML Kit to extract image labels.
     *
     * @param imageUri Firebase Storage URI (e.g., gs://... or https://...)
     */
    public void analyzeImageFromFirebaseStorage(String imageUri) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl(imageUri);

        // Download the image as bytes (limit to 5MB for safety)
        storageRef.getBytes(5 * 1024 * 1024)
                .addOnSuccessListener(bytes -> {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    runImageLabeling(bitmap);
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to download image: " + e.getMessage()));
    }

    /**
     * Runs ML Kit image labeling on a given bitmap.
     *
     * @param bitmap the image to analyze
     */
    private void runImageLabeling(Bitmap bitmap) {
        try {
            // Convert Bitmap to ML Kit's InputImage
            InputImage image = InputImage.fromBitmap(bitmap, 0);

            // Create the image labeler with default options
            ImageLabeler labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);

            // Process the image and handle the results
            labeler.process(image)
                    .addOnSuccessListener(labels -> {
                        for (ImageLabel label : labels) {
                            String text = label.getText();
                            float confidence = label.getConfidence();
                            int index = label.getIndex();
                            Log.d(TAG, "Label: " + text + ", Confidence: " + confidence);
                        }
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Image labeling failed: " + e.getMessage()));

        } catch (Exception e) {
            Log.e(TAG, "Failed to create InputImage: " + e.getMessage());
        }
    }
}
