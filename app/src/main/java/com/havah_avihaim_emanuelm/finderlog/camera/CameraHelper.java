package com.havah_avihaim_emanuelm.finderlog.camera;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CameraHelper {

    private final Context context;
    private final PreviewView previewView;
    private ImageCapture imageCapture;
    private final StorageService storageService = StorageService.getSharedInstance();

    private Uri pendingImageUri;
    private String pendingMimeType;

    public CameraHelper(Context context, PreviewView previewView) {
        this.context = context;
        this.previewView = previewView;
    }

    public void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(context);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                imageCapture = new ImageCapture.Builder().build();

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

    public void takePhoto(Runnable onReadyToDisplay) {
        if (imageCapture == null) return;

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
                        pendingImageUri = savedUri;
                        pendingMimeType = "image/jpeg";

                        Log.d("CameraX", "Photo saved: " + savedUri);
                        Bitmap bitmap = getCorrectlyOrientedBitmap(photoFile.getAbsolutePath());
                        if (bitmap != null) {
                            ((MainActivity) context).showCameraImagePreview(bitmap);
                        } else {
                            Log.e("CameraX", "Failed to decode bitmap from saved image");
                        }

                        if (onReadyToDisplay != null) {
                            onReadyToDisplay.run();
                        }
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

    public void confirmAndUploadImage(String imageTitle) {
        if (pendingImageUri == null) {
            Log.e("CameraX", "No photo to upload:");
            return;
        }

        storageService.uploadFile(pendingImageUri, storagePath -> {
            if (storagePath != null) {
                new MatchAlgorithm(context, pendingMimeType, this::clearPendingImage, imageTitle);
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
        pendingMimeType = null;
    }

    private Bitmap getCorrectlyOrientedBitmap(String imagePath) {
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        try {
            ExifInterface exif = new ExifInterface(imagePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            Matrix matrix = new Matrix();

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

            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch (Exception e) {
            e.printStackTrace();
            return bitmap;
        }
    }
}



