package com.example.stregourist;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ProximityAlertWorker extends Worker {

    public ProximityAlertWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return Result.failure();
        }

        Log.d("ProximityWorker", "Worker started");

        FusedLocationProviderClient locationClient =
                LocationServices.getFusedLocationProviderClient(getApplicationContext());

        final CountDownLatch latch = new CountDownLatch(1);
        final Location[] userLocation = new Location[1];

        locationClient.getLastLocation().addOnSuccessListener(location -> {
            userLocation[0] = location;
            latch.countDown();
        });

        try {
            latch.await(); // aspetta che la posizione sia disponibile
        } catch (InterruptedException e) {
            e.printStackTrace();
            return Result.retry();
        }

        Location location = userLocation[0];
        if (location == null) {
            Log.w("ProximityWorker", "Location is null");
            return Result.retry();
        }

        double userLat = location.getLatitude();
        double userLon = location.getLongitude();

        List<Place> allPlaces = StrgDatabase.getDatabase(getApplicationContext()).placeDAO().getAllPlace();

        Log.d("ProximityWorker", "User location: " + userLat + ", " + userLon);

        for (Place place : allPlaces) {
            if (place.getVisitati()==1) continue;
            Log.d("ProximityWorker", "Place: " + place.getNome() + " → " + place.getLatitudine() + ", " + place.getLongitudine());

            float[] result = new float[1];
            Location.distanceBetween(
                    userLat, userLon,
                    place.getLatitudine(), place.getLongitudine(),
                    result
            );

            float distance = result[0];
            Log.d("ProximityWorker", "Distanza da " + place.getNome() + ": " + distance + " m");
            if (distance < 1000) {
                NotificationManagerHelper.sendProximityNotification(getApplicationContext(), place.getNome());
                break; // notifica solo una volta
            }
        }

        return Result.success();
    }
}
