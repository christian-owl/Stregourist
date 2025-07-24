package com.example.stregourist;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.MotionEvent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.*;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.common.model.LocalModel;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.custom.CustomImageLabelerOptions;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ArActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA};

    private PreviewView previewView;
    private ExecutorService cameraExecutor;
    private boolean isProcessingFrame = false;
    private volatile boolean requestImageProcessing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar);

        previewView = findViewById(R.id.previewView);
        cameraExecutor = Executors.newSingleThreadExecutor();

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        previewView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                requestImageProcessing = true;
            }
            return false;
        });

    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(1280, 720))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(cameraExecutor, imageProxy -> {
                    if (requestImageProcessing) {
                        requestImageProcessing = false;
                        processImageProxy(imageProxy);
                    } else {
                        imageProxy.close();
                    }
                });

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void processImageProxy(ImageProxy imageProxy) {
        @SuppressWarnings("UnsafeOptInUsageError")
        Image mediaImage = imageProxy.getImage();

        if (mediaImage != null) {
            int rotationDegrees = imageProxy.getImageInfo().getRotationDegrees();
            InputImage image = InputImage.fromMediaImage(mediaImage, rotationDegrees);

            //modello personalizzato di ml
            LocalModel localModel = new LocalModel.Builder()
                    .setAssetFilePath("model.tflite")
                    .build();

            CustomImageLabelerOptions customImageLabelerOptions =
                    new CustomImageLabelerOptions.Builder(localModel)
                            .setConfidenceThreshold(0.6f)
                            .setMaxResultCount(1)
                            .build();

            ImageLabeler labeler = ImageLabeling.getClient(customImageLabelerOptions);


            labeler.process(image)
                    .addOnSuccessListener(labels -> {
                        handleLabels(labels);
                        imageProxy.close();
                        isProcessingFrame = false;
                    })
                    .addOnFailureListener(e -> {
                        e.printStackTrace();
                        imageProxy.close();
                        isProcessingFrame = false;
                    });
        } else {
            imageProxy.close();
            isProcessingFrame = false;
        }
    }

    private void handleLabels(List<ImageLabel> labels) {
        for (ImageLabel label : labels) {
            String text = label.getText();
            float confidence = label.getConfidence();
            Log.d("MLKit", "Detected: " + text + " (" + confidence + ")");

            if (confidence > 0.75f) { // Puoi regolare la soglia
                runOnUiThread(() -> onMonumentRecognized(text));
                break;
            }
        }
    }

    private void onMonumentRecognized(String label) {
        Toast.makeText(this, "Riconosciuto: " + label, Toast.LENGTH_SHORT).show();

        PlaceViewModel viewModel = new ViewModelProvider(this).get(PlaceViewModel.class);

        viewModel.getAllPlaces().observe(this, places -> {
            for (Place place : places) {
                if (place.getNome().equalsIgnoreCase(label)) {

                    Intent intent = new Intent(this, PlaceDetailsActivity.class);
                    intent.putExtra("id", place.getId());
                    intent.putExtra("nome", place.getNome());
                    startActivity(intent);

                    viewModel.getAllPlaces().removeObservers(this);
                    break;
                }
            }
        });
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(getBaseContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this, "Permessi non concessi", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}