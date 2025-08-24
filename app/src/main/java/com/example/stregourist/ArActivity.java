package com.example.stregourist;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.MotionEvent;
import android.widget.Button;
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
    private volatile boolean shouldAnalyzeFrames = false;

    // ML Kit components caricati una sola volta
    private ImageLabeler labeler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar);

        previewView = findViewById(R.id.previewView);
        cameraExecutor = Executors.newSingleThreadExecutor();

        // Bottone per attivare l'analisi
        Button analyzeButton = findViewById(R.id.btnAnalyze);
        analyzeButton.setOnClickListener(v -> {
            shouldAnalyzeFrames = true;
            Toast.makeText(this, "Analisi avviata", Toast.LENGTH_SHORT).show();
        });

        if (allPermissionsGranted()) {
            initModel();  // Carico il modello una sola volta
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }

    private void initModel() {
        // Carico il modello TFLite solo una volta
        LocalModel localModel = new LocalModel.Builder()
                .setAssetFilePath("model.tflite")
                .build();
        CustomImageLabelerOptions options =
                new CustomImageLabelerOptions.Builder(localModel)
                        .setConfidenceThreshold(0.5f) // soglia ridotta a 0.5
                        .setMaxResultCount(1)
                        .build();
        labeler = ImageLabeling.getClient(options);
        Log.d("MLKit", "Modello TFLite caricato");
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
                    if (shouldAnalyzeFrames) {
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

            labeler.process(image)
                    .addOnSuccessListener(labels -> {
                        handleLabels(labels);
                        imageProxy.close();
                    })
                    .addOnFailureListener(e -> {
                        e.printStackTrace();
                        imageProxy.close();
                    });
        } else {
            imageProxy.close();
        }
    }

    private void handleLabels(List<ImageLabel> labels) {
        if (labels == null || labels.isEmpty()) return;

        ImageLabel bestLabel = labels.get(0);
        String text = bestLabel.getText();
        float confidence = bestLabel.getConfidence();

        Log.d("MLKit", "Riconosciuto: " + text + " (" + confidence + ")");

        if (confidence > 0.5f) { // unica soglia
            runOnUiThread(() -> onMonumentRecognized(text));
            // Se vuoi fermare l'analisi dopo il primo riconoscimento:
            shouldAnalyzeFrames = false;
        }
    }

    private void onMonumentRecognized(String label) {
        Toast.makeText(this, "Riconosciuto: " + label, Toast.LENGTH_SHORT).show();

        PlaceViewModel viewModel = new ViewModelProvider(this).get(PlaceViewModel.class);
        viewModel.getAllPlaces().observe(this, places -> {
            for (Place place : places) {
                if (place.getNome().equalsIgnoreCase(label)) {
                    Intent intent = new Intent(this, PlaceDetailsActivity.class);
                    intent.putExtra("name", place.getNome());
                    intent.putExtra("description", place.getDescrizione());
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
                initModel();
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
