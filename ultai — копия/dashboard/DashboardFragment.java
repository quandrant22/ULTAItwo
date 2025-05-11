package com.example.ultai.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.ultai.R;
import com.example.ultai.databinding.FragmentDashboardBinding;


public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        DashboardViewModel dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textDashboard;
        dashboardViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Переход на HomeFragment при нажатии на ImageButton с ID imageButton4
        ImageButton imageButton4 = view.findViewById(R.id.imageButton4);
        imageButton4.setOnClickListener(v -> {
            try {
                Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
                        .navigate(R.id.action_navigation_dashboard_to_navigation_home2);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                // Вывод сообщения об ошибке
                Toast.makeText(requireContext(),
                        "Navigation error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
