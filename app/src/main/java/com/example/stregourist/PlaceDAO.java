package com.example.stregourist;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface PlaceDAO {
    @Insert
    void insertPlace(Place place);
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllPlace(List<Place> places);
    @Query("SELECT * from places")
    List<Place> getAllPlace();
    @Query("SELECT * FROM places WHERE nome = :nome LIMIT 1")
    Place getPlaceByName(String nome);
    @Query("SELECT * from places")
    LiveData<List<Place>> getAllLivePlace();
    @Query("SELECT * FROM places WHERE id IN (SELECT rowid FROM place_fts WHERE place_fts MATCH :query)")
    LiveData<List<Place>> searchPlaces(String query);
    @Insert
    void insertFts(PlaceFts placeFts);
    @Query("SELECT * FROM places WHERE preferiti = 1")
    LiveData<List<Place>> getAllPreferiti();
    @Query("SELECT * FROM places WHERE visitati = 0")
    LiveData<List<Place>> getAllNoVisitati();
    @Query("SELECT * FROM places WHERE visitati = 0")
    List<Place> getAllNoVisitatiList();
    @Query("SELECT * FROM places WHERE visitati = 1")
    LiveData<List<Place>> getAllVisitati();
    @Update
    void updatePlace(Place place);
}
