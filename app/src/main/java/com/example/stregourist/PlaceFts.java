package com.example.stregourist;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Fts4;
import androidx.room.PrimaryKey;

@Fts4
@Entity(tableName = "place_fts")
public class PlaceFts {
    @PrimaryKey(autoGenerate = false)
    public int rowid;
    public String nome;
    public String descrizione;
    public int preferiti;
    public int visitati;

    public int getRowid() {
        return rowid;
    }

    public void setRowid(int rowid) {
        this.rowid = rowid;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public int getPreferiti() {
        return preferiti;
    }
    public void setPreferiti(int preferiti) {
        this.preferiti = preferiti;
    }
    public int getVisitati() {
        return visitati;
    }

    public void setVisitati(int visitati) {
        this.visitati = visitati;
    }
}
