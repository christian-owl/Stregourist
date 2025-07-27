package com.example.stregourist;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//Database Singleton di room
@androidx.room.Database(entities = {Place.class, PlaceFts.class}, version = 8, exportSchema = false)
public abstract class StrgDatabase extends RoomDatabase {
    public abstract PlaceDAO placeDAO();
    public static Context appContext;
    private static volatile StrgDatabase INSTANCE;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(4);

    public static StrgDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (StrgDatabase.class) {
                if (INSTANCE == null) {
                    appContext = context.getApplicationContext();
                    Log.d("DBTest", "Database creato! Ora chiamo CallBack");
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            StrgDatabase.class, "strg_database").addCallback(roomCallback).build();
                }
            }
        }
        return INSTANCE;
    }
    private static final RoomDatabase.Callback roomCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            Log.d("CallbackTest", "roomCallback: Database creato! Inizio popolamento...");
            if(INSTANCE!=null)
                populateFromJson(appContext.getApplicationContext(), INSTANCE.placeDAO());
            else
                Log.e("CallbackTest", "INSTANCE è null! Il database non è stato inizializzato.");
        }
    };

    public static void populateFromJson(Context context, PlaceDAO placeDao) {
        StrgDatabase.databaseWriteExecutor.execute(() ->{
            try {
                Log.d("PopulationTest", "Inizio popolamento del database");
                // Legge il file JSON dalla cartella assets
                InputStream is = context.getAssets().open("places.json");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                // Converte il JSON in un array di oggetti Place
                // Gson è una libreria di Google che permette di convertire oggetti Java in JSON e viceversa
                Gson gson = new Gson();
                Place[] placesArray = gson.fromJson(reader, Place[].class);
                List<Place> placesList = Arrays.asList(placesArray);

                Log.d("PopulationTest", "Elementi letti dal JSON: " + placesList.size());

                for(Place place : placesList){
                    try{
                        Log.d("PopulateCheck", "Sto per inserire: ID=" + place.getId() + ", Nome=" + place.getNome());
                        placeDao.insertPlace(place);

                        PlaceFts placeFts = new PlaceFts();
                        placeFts.rowid = place.getId();
                        placeFts.nome = place.getNome();
                        placeFts.descrizione = place.getDescrizione();
                        placeFts.preferiti = place.getPreferiti();
                        placeFts.visitati = place.getVisitati();

                        Log.d("PopulateCheck", "Sto per inserire: ROWID=" + placeFts.getRowid() + ", Nome=" + placeFts.getNome());
                        placeDao.insertFts(placeFts);
                        Log.d("PopulateCheck", "Inserito Place: " + place.getNome());
                    }
                    catch (Exception e){
                        Log.e("PopulateCheck", "Errore inserimento: " + e.getMessage());
                    }
                }
                List<Place> target = placeDao.getAllPlace();
                Log.d("PopulationTest", "Dati inseriti nel database:"+target.size());
            } catch (IOException e) {
                Log.e("PopulationTest", "Errore nel caricamento del JSON: " + e.getMessage());
            }
        });
    }
}
