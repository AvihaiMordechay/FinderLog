package com.havah_avihaim_emanuelm.finderlog.camera;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.annotation.NonNull;
import androidx.camera.core.ImageCaptureException;

import com.havah_avihaim_emanuelm.finderlog.activities.MainActivity;
import com.google.common.util.concurrent.ListenableFuture;
import com.havah_avihaim_emanuelm.finderlog.firebase.firestore.FirestoreService;
import com.havah_avihaim_emanuelm.finderlog.firebase.firestore.FoundItem;
import com.havah_avihaim_emanuelm.finderlog.firebase.ml_kit.MachineLearningService;
import com.havah_avihaim_emanuelm.finderlog.firebase.storage.StorageService;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CameraHelper {

    private final Context context;
    private final PreviewView previewView;
    private ImageCapture imageCapture;
    private StorageService storageService;
    private FirestoreService firestoreService;
    private MachineLearningService machineLearningService;

    public CameraHelper(Context context, PreviewView previewView, StorageService storageService, FirestoreService firestoreService, MachineLearningService machineLearningService) {
        this.context = context;
        this.previewView = previewView;
        this.storageService = storageService;
        this.firestoreService = firestoreService;
        this.machineLearningService = machineLearningService;
    }

    public void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(context);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // Preview
                androidx.camera.core.Preview preview = new androidx.camera.core.Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                // Select back camera as default
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                // ImageCapture
                imageCapture = new ImageCapture.Builder().build();

                // Unbind use cases before rebinding
                cameraProvider.unbindAll();

                // Bind to lifecycle
                cameraProvider.bindToLifecycle(
                        (MainActivity) context, // main activity
                        cameraSelector,
                        preview,
                        imageCapture
                );

            } catch (Exception e) {
                Log.e("CameraX", "Use case binding failed", e);
            }
        }, ContextCompat.getMainExecutor(context));
    }

    public void takePhoto() {
        if (imageCapture == null) return;

        // File to save the image
        File photoFile = new File(getOutputDirectory(),
                new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
                        .format(new Date()) + ".jpg");

        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(context),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults output) {
                        Uri savedUri = Uri.fromFile(photoFile);
                        String msg = "Photo saved: " + savedUri;
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                        Log.d("CameraX", msg);

//                       Upload image to Firebase Storage
                        storageService.uploadFile(savedUri, storagePath -> {
                            if (storagePath != null) {

                                machineLearningService.analyzeImageFromFirebaseStorage(savedUri.toString());
                                // Store image metadata in Fire-store
//                                firestoreService.addItem(new FoundItem("test", storagePath, "image/jpeg"));
                            } else {
                                Log.e("Upload", "Upload failed");
                            }
                        });
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e("CameraX", "Photo capture failed: " + exception.getMessage(), exception);
                    }
                }
        );
    }

    private File getOutputDirectory() {
        File mediaDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (mediaDir != null && mediaDir.exists())
            return mediaDir;
        else
            return context.getFilesDir();
    }
}

