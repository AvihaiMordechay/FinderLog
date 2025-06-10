package com.havah_avihaim_emanuelm.finderlog.camera;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import androidx.exifinterface.media.ExifInterface;

import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.havah_avihaim_emanuelm.finderlog.matches.MatchAlgorithm;
import com.havah_avihaim_emanuelm.finderlog.activities.MainActivity;
import com.havah_avihaim_emanuelm.finderlog.firebase.MachineLearningService;
import com.havah_avihaim_emanuelm.finderlog.firebase.StorageService;
import com.havah_avihaim_emanuelm.finderlog.utils.NetworkAwareDataLoader;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CameraHelper {

    private final Context context;
    private final PreviewView previewView;
    private ImageCapture imageCapture;
    private final StorageService storageService = StorageService.getSharedInstance();

    ProcessCameraProvider cameraProvider;
    private Uri pendingImageUri;
    private Bitmap bitmap;

    public CameraHelper(Context context, PreviewView previewView) {
        this.context = context;
        this.previewView = previewView;
    }
    // A function to start the camera preview and capture photos
    public void startCamera() {
        // Initialize the camera provider
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(context);
        // Add a listener to the camera provider future
        cameraProviderFuture.addListener(() -> {
            try {
                // Get the camera provider
                cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());
                // Choose the camera selector (front)
                CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;
                imageCapture = new ImageCapture.Builder().build();
                // Unbind all use cases before binding
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(
                        (MainActivity) context,
                        cameraSelector,
                        preview,
                        imageCapture
                );

            } catch (Exception e) {
                Log.e("CameraX", "Use case binding failed", e);
            }
        }, ContextCompat.getMainExecutor(context));
    }
    // A function that takes a photo and saves it to a file
    public void takePhoto(Runnable onReadyToDisplay) {
        if (imageCapture == null) return;
        File photoFile = new File(getOutputDirectory(),
                new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
                        .format(new Date()) + ".jpg");
        // Create output options object which contains file + metadata
        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();
        imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(context),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults output) {
                        Uri savedUri = Uri.fromFile(photoFile);
                        pendingImageUri = savedUri;

                        Log.d("CameraX", "Photo saved: " + savedUri);
                        bitmap = getCorrectlyOrientedBitmap(photoFile.getAbsolutePath());
                        if (bitmap != null) {
                            ((MainActivity) context).showCameraImagePreview(bitmap);
                        } else {
                            Log.e("CameraX", "Failed to decode bitmap from saved image");
                        }
                        closeCamera();

                        if (onReadyToDisplay != null) {
                            onReadyToDisplay.run();
                        }
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e("CameraX", "Photo capture failed: " + exception.getMessage(), exception);
                        closeCamera();
                    }
                }
        );
    }
    // A function to get the output directory for the photo
    private File getOutputDirectory() {
        // Get the external storage directory
        File mediaDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        // If the directory exists, return it
        if (mediaDir != null && mediaDir.exists())
            return mediaDir;
        else
            return context.getFilesDir();
    }
    // A function to confirm and upload the image
    public void confirmAndUploadImage(String imageTitle) {
        if (pendingImageUri == null) {
            Log.e("CameraX", "No photo to upload:");
            clearPendingImage();
            return;
        }
        // Check for internet connection
        if (!NetworkAwareDataLoader.isNetworkAvailable(context)) {
            new AlertDialog.Builder(context)
                    .setTitle("No internet connection")
                    .setMessage("Failed to upload image. \nPlease check your internet connection and try again.")
                    .setPositiveButton("OK", null)
                    .show();
            clearPendingImage();
            return;
        }
        // Upload the image to Firebase Storage
        storageService.uploadFile(pendingImageUri, storagePath -> {
            clearPendingImage();
            if (storagePath != null) {
                new MatchAlgorithm(context, imageTitle);
                // Start the Machine Learning service
                Intent intent = new Intent(context, MachineLearningService.class);
                // Send the image path to the service
                intent.setAction(MachineLearningService.ACTION_ANALYZE_IMAGE);
                // Send the image path to the service
                intent.putExtra(MachineLearningService.EXTRA_IMAGE_URI, storagePath);
                // Start the service
                context.startService(intent);
            } else {
                Log.e("CameraX", "Upload failed:");
            }
        });
    }
    // A function to close the camera
    public void closeCamera()
    {
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
            cameraProvider = null;
        }
    }
    // A function to clear the pending image URI
    public void clearPendingImage() {
        if(pendingImageUri.getPath()!=null) {
            File file = new File(pendingImageUri.getPath());
            boolean deleted = file.delete();
            if (deleted) {
                Log.d("CameraX", "Photo file deleted after upload: " + file.getAbsolutePath());
            } else {
                Log.e("CameraX", "Failed to delete photo file: " + file.getAbsolutePath());
            }
            pendingImageUri = null;
        }
        bitmap.recycle();
        bitmap = null;
    }
    // A function to get the correctly oriented bitmap from the image path
    private Bitmap getCorrectlyOrientedBitmap(String imagePath) {
        // Get the bitmap from the image path
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        try {
            // Get the orientation of the image
            ExifInterface exif = new ExifInterface(imagePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            Matrix matrix = new Matrix();
            // Rotate the bitmap based on the orientation
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.postRotate(90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.postRotate(180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.postRotate(270);
                    break;
                default:
                    return bitmap;
            }
            // Return the rotated bitmap
            Bitmap rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return rotated;
        } catch (Exception e) {
            Log.e("ImageUtils", "Failed to rotate image from path: " + imagePath, e);
            return bitmap;
        }
    }
}



