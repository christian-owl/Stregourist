package com.example.stregourist;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "places")
public class Place {
    public Place(int id, String nome, String descrizione, int preferiti, int visitati, double latitudine, double longitudine, String smallImg, String bigImg) {
        this.id = id;
        this.nome = nome;
        this.descrizione = descrizione;
        this.preferiti = preferiti;
        this.visitati = visitati;
        this.latitudine = latitudine;
        this.longitudine = longitudine;
        this.smallImg = smallImg;
        this.bigImg = bigImg;
    }

    @PrimaryKey(autoGenerate = false)
    public int id;
    @ColumnInfo(name = "nome")
    public String nome;
    @ColumnInfo(name = "descrizione")
    public String descrizione;
    @ColumnInfo(name = "preferiti")
    public int preferiti;
    @ColumnInfo(name = "visitati")
    public int visitati;
    @ColumnInfo(name = "latitudine")
    public double latitudine;
    @ColumnInfo(name = "longitudine")
    public double longitudine;
    @ColumnInfo(name = "smallImg")
    public String smallImg;
    @ColumnInfo(name = "bigImg")
    public String bigImg;
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public double getLatitudine() {
        return latitudine;
    }

    public void setLatitudine(double latitudine) {
        this.latitudine = latitudine;
    }

    public double getLongitudine() {
        return longitudine;
    }

    public void setLongitudine(double longitudine) {
        this.longitudine = longitudine;
    }

    public String getSmallImg() {
        return smallImg;
    }

    public void setSmallImg(String smallImg) {
        this.smallImg = smallImg;
    }

    public String getBigImg() {
        return bigImg;
    }

    public void setBigImg(String bigImg) {
        this.bigImg = bigImg;
    }
}
