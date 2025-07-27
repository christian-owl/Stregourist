package com.example.stregourist;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

/* L'Adapter è un ponte tra i dati e la RecyclerView. Si occupa di prendere la lista dei Place
e creare le "view" necessarie per mostrarli a schermo. */
public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder> {
    private List<Place> places = new ArrayList<>();

    public PlaceAdapter() {
        Log.d("AdapterTest", "Adapter creato!");
    }

    public void setPlaces(List<Place> newPlaces) {
        this.places = newPlaces;
        Log.d("AdapterTest", "Elementi ricevuti nell'Adapter: " + newPlaces.size());
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.place_item, parent, false);
        return new PlaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceViewHolder holder, int position) {
        Place place = places.get(position);
        Log.d("AdapterTest", "Binding elemento: " + place.getNome());
        holder.bind(places.get(position), holder);
    }

    @Override
    public int getItemCount() {
        return places.size();
    }

    static class PlaceViewHolder extends RecyclerView.ViewHolder {
        private final TextView placeName;
        private final TextView placeDescript;
        private final AppCompatImageButton favButton;
        private final ImageView image;

        public PlaceViewHolder(@NonNull View itemView) {
            super(itemView);
            placeName = itemView.findViewById(R.id.place_name);
            placeDescript = itemView.findViewById(R.id.place_description);
            favButton = itemView.findViewById(R.id.favorite_button);
            image = itemView.findViewById(R.id.place_image);
        }

        public void bind(Place place, PlaceAdapter.PlaceViewHolder holder) {
            placeName.setText(place.getNome());
            placeDescript.setText(place.getDescrizione());

            Context context = itemView.getContext();
            int resId = context.getResources().getIdentifier(
                    place.getSmallImg(), "drawable", context.getPackageName());

            if (resId != 0) {
                image.setImageResource(resId);
            } else {
                // fallback nel caso non trovasse l'immagine
                image.setImageResource(R.drawable.ic_not_visited);
            }

            updateFavoriteIcon(place.getPreferiti());
            favButton.setOnClickListener(v->{
                int nuovoStato = (place.getPreferiti() == 1) ? 0 : 1;
                place.setPreferiti(nuovoStato);
                updateFavoriteIcon(nuovoStato);

                Executors.newSingleThreadExecutor().execute(() -> {
                    StrgDatabase.getDatabase(itemView.getContext())
                            .placeDAO()
                            .updatePlace(place);
                });
            });
            holder.itemView.setOnClickListener(v ->{
                Intent intent = new Intent(context, PlaceDetailsActivity.class);
                intent.putExtra("name", place.getNome());
                intent.putExtra("description", place.getDescrizione());
                context.startActivity(intent);
            });
        }
        private void updateFavoriteIcon(int isFavorite) {
            int color = ContextCompat.getColor(itemView.getContext(),
                    isFavorite == 1 ? R.color.red : R.color.black);
            favButton.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        }
    }
}