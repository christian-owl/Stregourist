package com.example.stregourist;

import static android.content.ContentValues.TAG;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ClipData.Item;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.media.RouteListingPreference;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity {
    private androidx.appcompat.widget.SearchView searchView;
    private PlaceViewModel placeViewModel;
    private PlaceAdapter placeAdapter;
    private RecyclerView recyclerView;
    private BottomNavigationView navbar;
    private ImageButton notificationButt;
    private ActivityResultLauncher<String[]> permissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrgDatabase db = StrgDatabase.getDatabase(this);

        Executors.newSingleThreadExecutor().execute(() -> {
            List<Place> target = db.placeDAO().getAllPlace();
            Log.d("MainDatabaseTest", "Dati inseriti nel database:"+target.size());
        });

        navbar = findViewById(R.id.navbar);
        navbar.setOnNavigationItemSelectedListener(navListener);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();

        searchView = findViewById(R.id.searchView);
        recyclerView= findViewById(R.id.recycleview);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        placeAdapter= new PlaceAdapter();
        recyclerView.setAdapter(placeAdapter);

        placeViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())
        ).get(PlaceViewModel.class);
        placeViewModel.getPlaces().observe(this, places -> {
            Log.d("MainDatabaseTest", "Elementi ricevuti dal ViewModel: " + (places != null ? places.size() : "null"));
            if (places == null || places.isEmpty()) {
                recyclerView.setVisibility(View.GONE); // Nasconde la lista se vuota
            } else {
                recyclerView.setVisibility(View.VISIBLE); // Mostra la lista se ci sono risultati
                placeAdapter.setPlaces(places);
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                placeViewModel.search(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d("MainDatabaseTest", "Query digitata: " + newText);
                placeViewModel.search(newText);
                return true;
            }
        });

        notificationButt = findViewById(R.id.notification_button);
        notificationButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, NotificationSettingsActivity.class);
                startActivity(intent);
            }
        });
        //PERMESSI
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    if (PermissionUtils.hasPermissions(this)) {
                        Toast.makeText(this, "Permessi Concessi", Toast.LENGTH_SHORT).show();
                    } else {
                        showSettingsDialog();
                    }
                }
        );
        requestAllPermissionsIfNecessary();
        //NOTIFICHE
        NotificationManagerHelper.createNotificationChannel(this);
        scheduleDailyTips();
        scheduleProximityWorker();
        //testProximityWorkerNow();

        FirebaseApp.initializeApp(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "channel_events",
                    "Eventi",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
                Log.w("FCM_CHANNEL", "canale creato");
            }
        }
        FirebaseMessaging.getInstance().subscribeToTopic("eventi")
                .addOnCompleteListener(task -> {
                    String msg = task.isSuccessful() ? "Iscrizione al topic riuscita" : "Iscrizione fallita";
                    Log.d("FCM_TOPIC", msg);
                });
        /* Questo costrutto richiede al beckend di inviare una notifica all'app
        ApiService apiService = ApiClient.getApiService();
        apiService.notificaEvento(3).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("NOTIFICA", "Notifica richiesta al server con successo");
                } else {
                    Log.e("NOTIFICA", "Errore: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("NOTIFICA", "Errore di rete: " + t.getMessage());
            }
        });
         */
    }
    private void requestAllPermissionsIfNecessary() {
        if (!PermissionUtils.hasPermissions(this)) {
            permissionLauncher.launch(PermissionUtils.getRequiredPermissions());
        }
    }
    private final BottomNavigationView.OnNavigationItemSelectedListener navListener = item -> {
        Fragment selectedFragment = null;
        int itemId = item.getItemId();
        if (itemId == R.id.nav_home) {
            selectedFragment = new HomeFragment();
        } else if (itemId == R.id.nav_fav) {
            selectedFragment = new FavFragment();
        } else if (itemId == R.id.nav_ar) {
            Intent intent= new Intent(this, ArActivity.class);
            startActivity(intent);
        }
        if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
        }
        return true;
    };

    private void showSettingsDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Permessi necessari")
                .setMessage("Per usare l'app, abilita fotocamera e posizione nelle impostazioni.")
                .setPositiveButton("Vai a impostazioni", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                })
                .setNegativeButton("Annulla", (dialog, which) -> {
                    Toast.makeText(this, "Permessi Non Concessi", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .show();
    }

    private void scheduleDailyTips(){
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                DailyTipsWorker.class, 24, TimeUnit.HOURS
        ).build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "daily_tips", ExistingPeriodicWorkPolicy.KEEP, request
        );
    }
    private void scheduleProximityWorker() {
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                ProximityAlertWorker.class,
                15, TimeUnit.MINUTES
        ).build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "proximity_alert",
                ExistingPeriodicWorkPolicy.KEEP,
                request
        );
    }
    private void testProximityWorkerNow() {
        OneTimeWorkRequest testRequest =
                new OneTimeWorkRequest.Builder(ProximityAlertWorker.class).build();

        WorkManager.getInstance(this).enqueue(testRequest);
    }
}