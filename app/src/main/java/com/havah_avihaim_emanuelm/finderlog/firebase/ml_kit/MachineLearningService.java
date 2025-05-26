package com.havah_avihaim_emanuelm.finderlog.firebase.ml_kit;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

public class MachineLearningService extends Service {
    private static final String TAG = "MLService";
    public static final String ACTION_ANALYZE_IMAGE = "com.havah_avihaim_emanuelm.finderlog.ANALYZE_IMAGE";
    public static final String EXTRA_IMAGE_URI = "image_uri";
    public static final String EXTRA_LABELS = "labels";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_ANALYZE_IMAGE.equals(intent.getAction())) {
            String imageUri = intent.getStringExtra(EXTRA_IMAGE_URI);
            if (imageUri != null) {
                analyzeImageFromFirebaseStorage(imageUri);
            }
        }

        return START_NOT_STICKY;
    }

    private void analyzeImageFromFirebaseStorage(String imageUri) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl(imageUri);

        storageRef.getBytes(5 * 1024 * 1024)
                .addOnSuccessListener(bytes -> {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    runImageLabeling(bitmap, imageUri);
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to download image: " + e.getMessage()));
    }

    private void runImageLabeling(Bitmap bitmap, String imageUri) {
        try {
            InputImage image = InputImage.fromBitmap(bitmap, 0);
            ImageLabeler labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);

            labeler.process(image)
                    .addOnSuccessListener(labels -> {
                        StringBuilder stringLabels = new StringBuilder();
                        for (int i = 0; i < labels.size(); i++) {
                            if (labels.get(i).getConfidence() > 0.6) {
                                stringLabels.append(labels.get(i).getText()).append(", ");
                            }
                            Log.d(TAG, "Label: " + labels.get(i).getText() + ", Confidence: " + labels.get(i).getConfidence());
                        }

                        sendLabelsBackToUI(imageUri, stringLabels.toString());
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Image labeling failed: " + e.getMessage());
                        sendLabelsBackToUI(imageUri, "");
                    });
        } catch (Exception e) {
            Log.e(TAG, "Failed to create InputImage: " + e.getMessage());
            sendLabelsBackToUI(imageUri, "");
        }
    }

    private void sendLabelsBackToUI(String imageUri, String labels) {
        Intent resultIntent = new Intent(ACTION_ANALYZE_IMAGE);
        resultIntent.putExtra(EXTRA_IMAGE_URI, imageUri);
        resultIntent.putExtra(EXTRA_LABELS, labels);
        sendBroadcast(resultIntent);
    }
}
