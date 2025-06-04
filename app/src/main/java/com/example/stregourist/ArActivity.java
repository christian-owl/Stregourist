package com.example.stregourist;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.widget.Toast;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Earth;
import com.google.ar.core.Frame;
import com.google.ar.core.GeospatialPose;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.ux.ArFragment;
import android.Manifest;

import java.util.ArrayList;
import java.util.List;

public class ArActivity extends AppCompatActivity {
    private ArFragment arFragment;
    private Earth earth;
    private PlaceViewModel model;
    private List<Place> placeList = new ArrayList<>();
    private boolean recognized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar);

        model = new ViewModelProvider(this).get(PlaceViewModel.class);
        model.getAllPlaces().observe(this, places -> {
            placeList = places;
        });

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ar_fragment);
        arFragment.getArSceneView().getScene().addOnUpdateListener(frameTime -> {
            Frame frame = arFragment.getArSceneView().getArFrame();
            if(frame == null || frame.getCamera().getTrackingState()!= TrackingState.TRACKING) return;

            Session session = arFragment.getArSceneView().getSession();
            earth = session.getEarth();
            if (earth == null || earth.getTrackingState()!= TrackingState.TRACKING) return;

            GeospatialPose pose = earth.getCameraGeospatialPose();

            double userLat = pose.getLatitude();
            double userLon = pose.getLongitude();
            //heading serve ad ottenere la posizione in cui guarda la camera
            double userHeading = pose.getHeading();

            matchPlaces(userLat,userLon,userHeading);
        });
    }

    private void matchPlaces(double userLat, double userLng, double userHeading){
        if(placeList==null || placeList.isEmpty() || recognized) return;

        for(Place place : placeList){
            float distance = distanceBetween(userLat, userLng, place.latitudine, place.longitudine);
            float bearing = bearingBetween(userLat, userLng, place.latitudine, place.longitudine);
            float angleDiff = Math.abs((float) userHeading - bearing);

            if (distance < 50 && angleDiff < 30) {
                recognized = true;
                onPlaceMatched(place, distance);
                break;
            }
        }
    }
    private void onPlaceMatched(Place place, float distance) {
        runOnUiThread(() -> {
            Toast.makeText(this,
                    "Hai inquadrato: " + place.nome + " (distanza: " + (int) distance + "m)",
                    Toast.LENGTH_LONG).show();

            Intent intent = new Intent(this, PlaceDetailsActivity.class);
            intent.putExtra("name", place.getNome());
            intent.putExtra("description", place.getDescrizione());
            intent.putExtra("distance", distance/1000);
            startActivity(intent);
        });
    }
    /* Restituisce la distanza in metri tra due coordinate GPS usando la formula Haversine (sferica, precisa).
        Utile per capire quanto l’utente è vicino a un luogo. */
    private float distanceBetween(double lat1, double lon1, double lat2, double lon2) {
        float[] results = new float[1];
        Location.distanceBetween(lat1, lon1, lat2, lon2, results);
        return results[0];
    }
    /* Restituisce l’angolo (in gradi) tra due coordinate GPS, ossia la direzione da A verso B.
        Utile per verificare se l’utente sta guardando verso un punto d’interesse. */
    private float bearingBetween(double lat1, double lon1, double lat2, double lon2) {
        Location start = new Location("start");
        start.setLatitude(lat1);
        start.setLongitude(lon1);
        Location end = new Location("end");
        end.setLatitude(lat2);
        end.setLongitude(lon2);
        return start.bearingTo(end);
    }
}