package com.example.stregourist;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class FavFragment extends Fragment {
    private RecyclerView recyclerView;
    private PlaceAdapter adapter;
    private TextView emptyView;
    private PlaceViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fav, container, false);

        recyclerView = view.findViewById(R.id.favRecyclerView);
        emptyView = view.findViewById(R.id.emptyFavView);


        adapter = new PlaceAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(requireActivity()).get(PlaceViewModel.class);

        viewModel.getFavPlaces().observe(getViewLifecycleOwner(), favPlaces -> {
            if (favPlaces == null || favPlaces.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
            } else {
                adapter.setPlaces(favPlaces);
                recyclerView.setVisibility(View.VISIBLE);
                emptyView.setVisibility(View.GONE);
            }
        });

        return view;
    }
}
