package com.example.ultai.planer;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.ultai20.R;
import com.example.ultai20.databinding.FragmentPlanerBinding;
import com.example.ultai.util.CompanyDataManager;
import com.example.ultai.data.repository.UserRepository;

public class PlanerFragment extends Fragment {
    private static final String TAG = "PlanerFragment";
    private FragmentPlanerBinding binding;
    private UserRepository userRepository;
    private NavController navController;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPlanerBinding.inflate(inflater, container, false);
        userRepository = UserRepository.getInstance();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        // Загружаем и отображаем данные компании
        loadCompanyData(view);

        // Логика проверки анкеты планировщика
        checkPlannerQuestionnaire();

        // Переход на HomeFragment
        binding.imageButton2.setOnClickListener(v -> {
            navController.navigate(R.id.action_navigation_planer_to_navigation_home2);
        });

        // Переход на ProfileFragment через ImageButton
        binding.imageButton7.setOnClickListener(v ->
                navController.navigate(R.id.action_navigation_planer_to_profileFragment)
        );

        // Переход на Faza1Stages
        binding.imageView22.setOnClickListener(v ->
                navController.navigate(R.id.action_navigation_planer_to_faza1_stages)
        );
    }

    private void checkPlannerQuestionnaire() {
        if (userRepository == null || navController == null) {
            Log.e(TAG, "UserRepository or NavController not initialized in checkPlannerQuestionnaire.");
            Toast.makeText(getContext(), "Ошибка инициализации планировщика.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = userRepository.getCurrentUserId();
        if (userId == null) {
            Log.e(TAG, "User not authenticated. Cannot check planner questionnaire.");
            return;
        }
        
        Log.d(TAG, "Checking for planner questionnaire for user: " + userId);

        userRepository.checkPlannerQuestionnaireExists(new UserRepository.Callback<Boolean>() {
            @Override
            public void onSuccess(Boolean exists) {
                if (!isAdded() || navController == null) return;

                if (exists) {
                    Log.d(TAG, "Planner questionnaire EXISTS for user: " + userId + ". Staying on PlanerFragment.");
                    // Анкета уже пройдена, остаемся на экране планера
                } else {
                    Log.d(TAG, "Planner questionnaire DOES NOT EXIST for user: " + userId + ". Navigating to questionnaire.");
                    
                    // Показываем сообщение пользователю о необходимости заполнить анкету
                    Toast.makeText(getContext(), "Для использования планера необходимо заполнить анкету", Toast.LENGTH_LONG).show();
                    
                    try {
                        if (navController.getCurrentDestination() != null && 
                            navController.getCurrentDestination().getId() == R.id.navigation_planer) {
                            // Переходим напрямую к первому вопросу анкеты планера
                            navController.navigate(R.id.action_navigation_planer_to_businessGoalFragment);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Navigation to businessGoalFragment failed", e);
                        // Fallback - переходим через промежуточный фрагмент
                        try {
                            navController.navigate(R.id.action_navigation_planer_to_plannerQuestionnaireFragment);
                        } catch (Exception fallbackE) {
                            Log.e(TAG, "Fallback navigation also failed", fallbackE);
                        }
                    }
                }
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) return;
                Log.e(TAG, "Error checking planner questionnaire: " + message);
                Toast.makeText(getContext(), "Ошибка при проверке анкеты: " + message, Toast.LENGTH_SHORT).show();
            }
        });
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
        binding = null;
    }
}

