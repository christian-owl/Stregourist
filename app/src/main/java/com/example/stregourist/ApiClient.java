package com.example.stregourist;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
//Singleton di Retrofit per ApiService
public class ApiClient {
    // http://10.0.2.2:8080 host emulatore
    private static final String BASE_URL = "http://10.0.2.2:8080/api/eventi/notifica/";
    private static Retrofit retrofit = null;

    public static ApiService getApiService() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ApiService.class);
    }
}
