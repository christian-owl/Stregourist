package com.example.stregourist;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {
    @POST("eventi")
    Call<Evento> aggiungiEvento(@Body Evento evento);

    @GET("eventi")
    Call<List<Evento>> listaEventi();

    @POST("eventi/notifica/{id}")
    Call<Void> notificaEvento(@Path("id") long id);
}