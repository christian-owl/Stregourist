package com.example.stregourist;

import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.progressindicator.LinearProgressIndicator;


public class HomeFragment extends Fragment {

    private RecyclerView recyclerViewUnvisited;
    private RecyclerView recyclerViewVisited;
    private PlaceViewModel placeViewModel;
    private TextView emptyViewUnvisit;
    private TextView emptyViewVisit;
    private PlaceCardAdapter adapterUnv;
    private PlaceCardAdapter adapterVis;
    private TextView progressText;
    private LinearProgressIndicator progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        recyclerViewUnvisited = view.findViewById(R.id.home_recy_unvis);
        recyclerViewVisited = view.findViewById(R.id.home_recy_visit);
        emptyViewUnvisit = view.findViewById(R.id.empty_home_unvisit);
        emptyViewVisit = view.findViewById(R.id.empty_home_visit);
        adapterUnv = new PlaceCardAdapter();
        adapterVis = new PlaceCardAdapter();
        recyclerViewUnvisited.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        );
        recyclerViewVisited.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        );
        recyclerViewUnvisited.setAdapter(adapterUnv);
        recyclerViewVisited.setAdapter(adapterVis);

        placeViewModel = new ViewModelProvider(requireActivity()).get(PlaceViewModel.class);
        placeViewModel.getUnvisited().observe(getViewLifecycleOwner(), unvisit->{
            if(unvisit==null||unvisit.isEmpty()){
                emptyViewUnvisit.setVisibility(View.VISIBLE);
                recyclerViewUnvisited.setVisibility(View.GONE);
            }
            else{
                emptyViewUnvisit.setVisibility(View.GONE);
                recyclerViewUnvisited.setVisibility(View.VISIBLE);
                adapterUnv.setPlaces(unvisit);
            }
        });
        placeViewModel.getVisited().observe(getViewLifecycleOwner(), visit->{
            if(visit==null||visit.isEmpty()){
                emptyViewVisit.setVisibility(View.VISIBLE);
                recyclerViewVisited.setVisibility(View.GONE);
            }
            else{
                emptyViewVisit.setVisibility(View.GONE);
                recyclerViewVisited.setVisibility(View.VISIBLE);
                adapterVis.setPlaces(visit);
            }
        });
        progressText = view.findViewById(R.id.progress_text);
        progressBar = view.findViewById(R.id.progress_bar);


        placeViewModel.getVisited().observe(getViewLifecycleOwner(), visited -> {
            placeViewModel.getAllPlaces().observe(getViewLifecycleOwner(), allPlaces -> {
                int totali = (allPlaces != null) ? allPlaces.size() : 0;
                int visitati = (visited != null) ? visited.size() : 0;

                progressText.setText("Visitati: " + visitati + " su " + totali);
                if (totali > 0) {
                    int percent = (visitati * 100) / totali;
                    progressBar.setProgress(percent);
                    int color;
                    if (percent < 25) {
                        color = ContextCompat.getColor(requireContext(), R.color.red);
                    } else if (percent <= 60) {
                        color = ContextCompat.getColor(requireContext(), R.color.yellow);
                    } else {
                        color = ContextCompat.getColor(requireContext(), R.color.light_green);
                    }
                    progressBar.setIndicatorColor(color);
                } else {
                    progressBar.setProgress(0);
                }
            });
        });

        return view;
    }
}