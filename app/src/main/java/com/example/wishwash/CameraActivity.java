package com.example.wishwash;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.common.model.LocalModel;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.custom.CustomImageLabelerOptions;

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
//    private CustomModel customModel;

    private ExecutorService cameraExecutor;
    private ImageLabeler labeler;
    private LocalModel localModel;

    public CameraActivity() {
    }

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

                // localModel을 사용하여 labeler 초기화
                // 주의: 여기서 'options'는 어떤 방식으로든 초기화되어야 합니다.
                // 현재 context에서는 'options'가 어떻게 정의되는지 알 수 없으므로,
                // 필요에 따라 해당 코드를 수정해야 할 수도 있습니다.
                LocalModel localModel = new LocalModel.Builder()
                        .setAssetFilePath("wishwash.tflite")
                        .build();

                CustomImageLabelerOptions options = new CustomImageLabelerOptions.Builder(localModel)
                        .setConfidenceThreshold(0.5f)
                        .build();

                labeler = ImageLabeling.getClient(options);
                bindCameraPreview(cameraProvider); // 이미지 분석기 설정과 함께 bindCameraPreview 메서드를 호출합니다.

            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error binding camera.", e);
            }
        }, ContextCompat.getMainExecutor(this));

        cameraExecutor = Executors.newSingleThreadExecutor();
    }

    @SuppressLint("SetTextI18n")
    private void bindCameraPreview(ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        // 이미지 분석기 설정
        imageAnalysis.setAnalyzer(cameraExecutor, imageProxy -> {
            Bitmap bitmap = imageProxyToBitmap(imageProxy);

            if (bitmap != null) {
                labeler.process(InputImage.fromBitmap(bitmap, 0))
                        .addOnSuccessListener(labels -> {
                            if (labels.isEmpty()) {
                                // 결과가 없을 경우 처리할 작업을 여기에 추가하세요.
                                // 예를 들어, 사용자에게 결과가 없음을 알리는 메시지를 표시하거나
                                // 기본값을 사용하여 특정 작업을 수행할 수 있습니다.
                                Log.d(TAG, "레이블링 결과 없음");
                            } else {
                                for (ImageLabel label : labels) {
                                    label.getText();
                                    label.getConfidence();
                                    String labelText = label.getText();
                                    String confidenceText = String.valueOf(label.getConfidence());
                                    getString(R.string.label_confidence, labelText, confidenceText);
                                    Log.d(TAG, "레이블링 결과 있음");
                                }
                            }
                        })
                        .addOnFailureListener(e -> Log.e(TAG, "레이블링 중 오류 발생.", e))
                        .addOnCompleteListener(task -> imageProxy.close()); // 이미지를 처리한 후에 이미지를 해제합니다.
            } else {
                imageProxy.close(); // 이미지를 처리하지 못한 경우에도 이미지를 해제합니다.
            }
        });

        // CameraProvider에 미리보기 및 이미지 분석기를 바인딩합니다.
        Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

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

                // 버퍼 유효성 검사 및 조정
                int requiredSize = width * height * 4; // 필요한 크기 계산
                if (buffer != null) {
                    // 버퍼의 용량이 충분한지 확인
                    if (buffer.capacity() >= requiredSize) {
                        // 충분한 크기를 가지고 있으므로 해당 버퍼를 계속 사용할 수 있습니다.
                        buffer.rewind(); // 버퍼 포인터를 처음으로 되돌립니다.
                    } else {
                        // 충분한 크기를 가지고 있지 않으므로 버퍼를 재할당합니다.
                        buffer = ByteBuffer.allocate(requiredSize);
                    }
                } else {
                    // 버퍼가 null인 경우에는 새로운 버퍼를 할당합니다.
                    buffer = ByteBuffer.allocate(requiredSize);
                }

                // 비트맵 생성
                bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
                bitmap.copyPixelsFromBuffer(buffer);
            }
        } catch (Exception e) {
            Log.e(TAG, "ImageProxy를 Bitmap으로 변환하는 중 오류가 발생했습니다.", e);
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
        int width = photo.getWidth();
        int height = photo.getHeight();
        Log.d(TAG, "Photo width: " + width + ", height: " + height);
        InputImage image = InputImage.fromBitmap(photo, 0);

        if (labeler != null) {
            labeler.process(image);
        } else {
            Log.e(TAG, "labeler is not initialized.");
        }

        // Process the image using the labeler
        labeler.process(image)
                .addOnSuccessListener(labels -> {
                    // 결과값을 처리하는 코드를 추가합니다.
                    for (ImageLabel label : labels) {
                        String labelText = label.getText();
                        String confidenceText = String.valueOf(label.getConfidence());
                        Log.d(TAG, "Label: " + labelText + ", Confidence: " + confidenceText);

                        // 결과값을 UI에 표시하거나, 레이블에 기반하여 특정 동작을 수행할 수 있습니다.

                        // 예를 들어, 새로운 액티비티를 시작하고 레이블 및 신뢰도 값을 전달할 수 있습니다.
                        Intent intent = new Intent(CameraActivity.this, ResultActivity.class);
                        intent.putExtra("labelText", labelText);
                        intent.putExtra("confidenceText", confidenceText);

                        startActivity(intent);
                        finish();
                    }

                })
                .addOnFailureListener(e -> {
                    // 레이블링 중에 발생한 오류를 처리합니다.
                    Log.e(TAG, "Error during labeling.", e);
                });

    }


}