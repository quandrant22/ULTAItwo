package com.example.ultai.planer;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.ultai.R;
import com.example.ultai.databinding.FragmentPlanerBinding;
import com.example.ultai.util.CompanyDataManager;



public class PlanerFragment extends Fragment {
    private FragmentPlanerBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Используем только один метод onCreateView с ViewBinding
        binding = FragmentPlanerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Загружаем и отображаем данные компании
        loadCompanyData(view);

        // Получаем NavController
        NavController navController = Navigation.findNavController(view);

        // Переход на HomeFragment
        binding.imageButton2.setOnClickListener(v ->
                navController.navigate(R.id.action_navigation_planer_to_navigation_home)
        );

        // Переход на ProfileFragment через ImageButton
        binding.imageButton7.setOnClickListener(v ->
                navController.navigate(R.id.action_navigation_planer_to_profileFragment)
        );

        // Переход на Faza1Stages
        binding.imageView22.setOnClickListener(v ->
                navController.navigate(R.id.action_navigation_planer_to_faza1_stages)
        );
    }
    
    /**
     * Загружает и отображает данные компании и описание деятельности
     */
    private void loadCompanyData(View view) {
        TextView companyNameTextView = view.findViewById(R.id.textView4);
        TextView companyDescriptionTextView = view.findViewById(R.id.textView5);

        // Получаем компоненты данных компании
        CompanyDataManager companyDataManager = CompanyDataManager.getInstance(requireContext());
        
        // Устанавливаем название компании
        String companyName = companyDataManager.getCompanyName();
        if (companyName != null && !companyName.isEmpty()) {
            companyNameTextView.setText(companyName);
        }

        // Получаем описание деятельности компании
        String activityDescription = companyDataManager.getActivityDescription();
        
        if (activityDescription != null && !activityDescription.isEmpty()) {
            // Если есть сохраненное описание деятельности, отображаем его
            companyDescriptionTextView.setText(activityDescription);
        } else {
            // Получаем тип деятельности как запасной вариант
            String activityType = companyDataManager.getActivityType();
            if (activityType != null && !activityType.isEmpty()) {
                companyDescriptionTextView.setText(activityType);
            } else {
                // Если нет данных о деятельности, проверяем SharedPreferences анкеты
                SharedPreferences basicQuestPrefs = requireContext().getSharedPreferences("basic_questionnaire", Context.MODE_PRIVATE);
                String productsServicesDescription = basicQuestPrefs.getString("productsServicesDescription", "");
                
                if (productsServicesDescription != null && !productsServicesDescription.isEmpty()) {
                    companyDescriptionTextView.setText(productsServicesDescription);
                }
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Во избежание утечек памяти
    }
}
