package com.example.ultai.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.ultai.R;
import com.example.ultai.util.CompanyDataManager;


public class HomeFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Загрузка и отображение данных компании
        loadCompanyData(view);

        // Переход на DashboardFragment
        View roundedRectangle = view.findViewById(R.id.rounded_rectangle);
        roundedRectangle.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_navigation_home_to_navigation_dashboard)
        );

        // Переход на PlanerFragment
        View roundedRectangle2 = view.findViewById(R.id.rounded_rectangle2);
        roundedRectangle2.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_navigation_home_to_navigation_planer2)
        );

        // Переход на NewsFragment
        View roundedRectangle4 = view.findViewById(R.id.rounded_rectangle4);
        roundedRectangle4.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_navigation_home_to_navigation_news)
        );

        // Переход на ProfileFragment через ImageButton
        ImageButton imageButton7 = view.findViewById(R.id.imageButton7);
        imageButton7.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_profileFragment)
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
}