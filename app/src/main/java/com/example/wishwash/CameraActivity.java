package com.example.wishwash;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions;
import com.google.firebase.ml.modeldownloader.DownloadType;
import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ExperimentalGetImage
public class CameraActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_PERMISSIONS = 101;
    private static final String[] REQUIRED_PERMISSIONS = {Manifest.permission.CAMERA};
    private static final String TAG = "CameraActivity";

    private PreviewView previewView;
    private ActivityResultLauncher<Intent> cameraLauncher;

    private ExecutorService cameraExecutor;
    private ImageLabeler labeler;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        previewView = findViewById(R.id.previewView);
        Button btn_photo = findViewById(R.id.btn_photo);

        if (checkCameraPermission()) {
            startCamera();
        } else {
            requestCameraPermission();
        }

        cameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            Bitmap photo = (Bitmap) data.getExtras().get("data");
                            processPhoto(photo);
                        }
                    }
                });

        btn_photo.setOnClickListener(v -> {
            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            cameraLauncher.launch(cameraIntent);
        });
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCameraPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error binding camera.", e);
            }
        }, ContextCompat.getMainExecutor(this));

        cameraExecutor = Executors.newSingleThreadExecutor();
    }

    private void bindCameraPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        ImageAnalysis imageAnalysis =
                new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(640, 480))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

        ImageCapture imageCapture = new ImageCapture.Builder().build();

        try {
            cameraProvider.unbindAll();
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis, imageCapture);
        } catch (Exception e) {
            Log.e(TAG, "Error binding camera provider.", e);
        }

        CustomModelDownloadConditions conditions = new CustomModelDownloadConditions.Builder()
                .requireWifi()
                .build();

        FirebaseModelDownloader.getInstance()
                .getModel("label", DownloadType.LOCAL_MODEL, conditions)
                .addOnSuccessListener(model -> {
                    ImageLabelerOptions options = new ImageLabelerOptions.Builder()
                            .setConfidenceThreshold(0.7f)
                            .build();
                    labeler = ImageLabeling.getClient(options);

                    imageAnalysis.setAnalyzer(cameraExecutor, imageProxy -> {
                        Bitmap bitmap = imageProxyToBitmap(imageProxy);

                        if (bitmap != null) {
                            labeler.process(InputImage.fromBitmap(bitmap, 0))
                                    .addOnSuccessListener(labels -> {
                                        for (ImageLabel label : labels) {
                                            label.getText();
                                            label.getConfidence();
                                            // TODO: Process label and confidence as needed
                                        }
                                    })
                                    .addOnFailureListener(e -> Log.e(TAG, "Error during labeling.", e));
                        }

                        imageProxy.close();
                    });
                })
                .addOnFailureListener(e -> Log.e(TAG, "Model download failed.", e));
    }

    @Nullable
    private Bitmap imageProxyToBitmap(ImageProxy imageProxy) {
        Bitmap bitmap = null;
        if (imageProxy == null) {
            return null;
        }
        try {
            ImageProxy.PlaneProxy[] planes = imageProxy.getPlanes();
            if (planes.length > 0) {
                ImageProxy.PlaneProxy plane = planes[0];
                ByteBuffer buffer = plane.getBuffer();
                int pixelStride = plane.getPixelStride();
                int rowStride = plane.getRowStride();
                int rowPadding = rowStride - pixelStride * imageProxy.getWidth();

                // 비트맵 크기 계산
                int width = imageProxy.getWidth();
                int height = imageProxy.getHeight();

                // 비트맵 생성
                bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
                bitmap.copyPixelsFromBuffer(buffer);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error converting ImageProxy to Bitmap.", e);
        } finally {
            // 이미지 해제
            imageProxy.close();
        }
        return bitmap;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Log.e(TAG, "Camera permission denied.");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }

    private void processPhoto(Bitmap photo) {
        // Process captured photo as needed
        // For example, you can save it to a file, upload to a server, or perform further analysis
        // Here, we simply log the width and height of the photo
        int width = photo.getWidth();
        int height = photo.getHeight();
        Log.d(TAG, "Photo width: " + width + ", height: " + height);
    }
}
