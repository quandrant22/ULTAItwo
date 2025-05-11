package com.example.ultai.planer.stages.stage_2;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.example.ultai20.R;
import com.example.ultai20.databinding.FragmentStepStage22Binding; // !!! Имя Binding

public class Step2Fragment extends Fragment {

    private FragmentStepStage22Binding binding; // !!! Имя Binding
    private NavController navController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStepStage22Binding.inflate(inflater, container, false); // !!! Имя Binding
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        // Логика для Шага 2 Фазы 2
        // binding.backButton.setOnClickListener(v -> navController.popBackStack());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 