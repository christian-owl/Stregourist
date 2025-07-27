package com.example.stregourist;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
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

public class PlaceCardAdapter  extends RecyclerView.Adapter<PlaceCardAdapter.PlaceViewHolder> {
    private List<Place> places = new ArrayList<>();

    public PlaceCardAdapter() {
        Log.d("CardAdapterTest", "Adapter creato!");
    }

    public void setPlaces(List<Place> newPlaces) {
        this.places = newPlaces;
        Log.d("CardAdapterTest", "Elementi ricevuti nell'Adapter: " + newPlaces.size());
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.place_item_card, parent, false);
        return new PlaceCardAdapter.PlaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceViewHolder holder, int position) {
        Place place = places.get(position);
        Log.d("CardAdapterTest", "Binding elemento: " + place.getNome());
        holder.bind(places.get(position), holder);
    }

    @Override
    public int getItemCount() {
        return places.size();
    }

    public static class PlaceViewHolder extends RecyclerView.ViewHolder {
        private final TextView name;
        private final TextView description;
        private final AppCompatImageButton visitedButton;
        private final ImageView image;

        public PlaceViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.place_card_name);
            description = itemView.findViewById(R.id.place_card_description);
            visitedButton = itemView.findViewById(R.id.visited_button);
            image = itemView.findViewById(R.id.place_card_img);
        }
        public void bind(Place place, PlaceViewHolder holder){
            name.setText(place.getNome());
            String newDescript = " ";
            if(place.getDescrizione().length()>30){
                newDescript= place.getDescrizione().substring(0,30)+"...";
                description.setText(newDescript);
            }
            else{
                description.setText(place.getDescrizione());
            }

            Context context = itemView.getContext();
            int resId = context.getResources().getIdentifier(
                    place.getBigImg(), "drawable", context.getPackageName());

            if (resId != 0) {
                image.setImageResource(resId);
            } else {
                // fallback nel caso non trovasse l'immagine
                image.setImageResource(R.drawable.ic_not_visited);
            }

            updateVisitedIcon(place.getVisitati(), holder.itemView.getContext());
            visitedButton.setOnClickListener(v->{
                int nuovoStato = (place.getVisitati() == 1) ? 0 : 1;
                place.setVisitati(nuovoStato);
                updateVisitedIcon(nuovoStato, v.getContext());
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
        public void updateVisitedIcon(int stato, Context context){
            if(stato == 0)
                visitedButton.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_not_visited));
            else
                visitedButton.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_visited));
        }
    }
}
