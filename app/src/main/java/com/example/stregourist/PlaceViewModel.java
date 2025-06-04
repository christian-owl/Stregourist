package com.example.stregourist;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.util.List;

/*  Il ViewModel osserva la query e aggiorna i dati ogni volta che l'utente scrive qualcosa
    Impedisce che l’Activity o il Fragment debbano ricaricare i dati ogni volta
    Si integra con LiveData, che permette di aggiornare la UI automaticamente quando i dati cambiano */
public class PlaceViewModel extends AndroidViewModel {
    private final PlaceDAO dao;
    //searchquery è una variabile reattiva, cambia ogni volta che l’utente digita qualcosa.
    private final MutableLiveData<String> searchquery = new MutableLiveData<>("");
    //LiveData<List<Place>> notifica l’UI quando i dati vengono aggiornati.
    private final LiveData<List<Place>> places;

    public PlaceViewModel(@NonNull Application application) {
        super(application);
        StrgDatabase db = StrgDatabase.getDatabase(application);
        dao = db.placeDAO();
        //Transformations.switchMap aggiorna places quando cambia la query (usando searchquery)
        places = Transformations.switchMap(searchquery, query->{
            Log.d("ModelTest", "Query ricevuta dal LiveData: " + query);
            return dao.searchPlaces(query);
        });
    }

    public void search(String query) {
        if (query == null || query.trim().isEmpty()) {
            query = "";
        } else {
            query = query.trim() + "*";
        }
        Log.d("ModelTestSearch", "Nuova query: " + query);
        searchquery.setValue(query);
    }

    public LiveData<List<Place>> getPlaces() {
        return places;
    }
    public LiveData<List<Place>> getAllPlaces() {
        return dao.getAllLivePlace();
    }
    public LiveData<List<Place>> getFavPlaces() {
        return dao.getAllPreferiti();
    }
    public LiveData<List<Place>> getUnvisited() {return dao.getAllNoVisitati();}
    public LiveData<List<Place>> getVisited() {return dao.getAllVisitati();}
}
