package com.example.stregourist;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Locale;
import java.util.concurrent.Executors;

public class PlaceDetailsActivity extends AppCompatActivity implements OnMapReadyCallback{
    private TextView nameView;
    private TextView descriptionView;
    private TextView distanceView;
    private AppCompatImageButton homeButton;
    private AppCompatImageButton favButton;
    private AppCompatImageButton visitButton;
    private Place place;
    private GoogleMap map;
    private boolean mapReady = false;
    private boolean placeReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_details);

        nameView = findViewById(R.id.place_details_name);
        descriptionView = findViewById(R.id.place_details_description);
        distanceView = findViewById(R.id.place_details_distance);
        favButton = findViewById(R.id.fav_button_details);
        visitButton = findViewById(R.id.visit_button_details);

        //per evitare che l'utente clicchi i pulsanti prima che il db sia pronto
        //vengono riabilitati in runOnUi
        favButton.setEnabled(false);
        visitButton.setEnabled(false);

        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        String description = intent.getStringExtra("description");
        nameView.setText(name);
        descriptionView.setText(description);
        distanceView.setText(String.format(Locale.getDefault(), "%d km", 0));

        //inizializzazione della mappa
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync( this);
        }
        else {
            Log.e("PlaceDetails", "mapFragment è NULL!");
        }

        StrgDatabase db = StrgDatabase.getDatabase(this);
        /* getPlaceByName() è una funzione che interroga il database in modo sincrono,
        quindi se chiamata sul main thread Room lancia un RuntimeException. */
        Executors.newSingleThreadExecutor().execute(() -> {
            try{
                place = db.placeDAO().getPlaceByName(name);
                placeReady = true;
                maybeInitializeMap();

                if(place==null)
                    Log.e("PlaceDetails", "place è NULL!");
                runOnUiThread(() -> {
                    favButton.setEnabled(true);
                    visitButton.setEnabled(true);

                    updateFavoriteIcon(place.getPreferiti(), this);
                    updateVisitedIcon(place.getVisitati(), this);

                    favButton.setOnClickListener(view -> {
                        int nuovoStato = (place.getPreferiti() == 1) ? 0 : 1;
                        place.setPreferiti(nuovoStato);
                        updateFavoriteIcon(nuovoStato, getApplicationContext());
                        Executors.newSingleThreadExecutor().execute(() -> {
                            db.placeDAO().updatePlace(place);
                        });
                    });

                    visitButton.setOnClickListener(view -> {
                        int nuovoStato = (place.getVisitati() == 1) ? 0 : 1;
                        place.setVisitati(nuovoStato);
                        updateVisitedIcon(nuovoStato, getApplicationContext());
                        Executors.newSingleThreadExecutor().execute(() -> {
                            db.placeDAO().updatePlace(place);
                        });
                    });
                });
            }
            catch (Exception e){
                Log.e("PlaceDetails", "Errore DB: " + e.getMessage());
            }
        });

        homeButton = findViewById(R.id.home_button_details);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish(); //chiude l'activity corrente
            }
        });

    }

    public void updateVisitedIcon(int stato, Context context){
        if(stato == 0)
            visitButton.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_not_visited));
        else
            visitButton.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_visited));
    }
    private void updateFavoriteIcon(int isFavorite, Context context) {
        int color = ContextCompat.getColor(context,
                isFavorite == 1 ? R.color.red : R.color.black);
        favButton.setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }
    private void maybeInitializeMap() {
        if (mapReady && placeReady && map != null && place != null) {
            Log.d("initializemap", "Inizializzo mappa con: " + place.getNome());
            LatLng posizione = new LatLng(place.getLatitudine(), place.getLongitudine());
            map.addMarker(new MarkerOptions().position(posizione).title(place.getNome()));
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(posizione, 15f));
        }
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        mapReady = true;
        maybeInitializeMap();
    }
}
