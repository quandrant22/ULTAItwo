package com.example.ultai.settings;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.NavOptions;

import com.example.ultai20.R;
import com.example.ultai20.databinding.FragmentSettingsBinding;
import com.example.ultai.data.repository.UserRepository;



public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private UserRepository userRepository;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Создаем экземпляр SettingsViewModel
        SettingsViewModel settingsViewModel =
                new ViewModelProvider(this).get(SettingsViewModel.class);

        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        
        // Инициализируем UserRepository
        userRepository = UserRepository.getInstance();
        
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton imageButtonNext = view.findViewById(R.id.imageButton2);
        imageButtonNext.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_settingsFragment_to_profileFragment);
        });
        
        // Находим контейнер выхода
        View logoutContainer = binding.logoutContainer;
        
        // Устанавливаем новый обработчик для выхода
        if (logoutContainer != null) {
            logoutContainer.setOnClickListener(v -> {
                Log.d("LogoutDebug", "Logout container clicked.");

                userRepository.logout(new UserRepository.Callback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        Log.d("LogoutDebug", "UserRepository.logout onSuccess called.");
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Log.d("LogoutDebug", "Navigating to FirstFragment after logout success.");
                                NavOptions navOptions = new NavOptions.Builder()
                                        .setPopUpTo(R.id.mobile_navigation, true)
                                        .build();
                                try {
                                    Navigation.findNavController(v).navigate(R.id.firstFragment, null, navOptions);
                                    Log.d("LogoutDebug", "Navigation initiated.");
                                } catch (Exception e) {
                                    Log.e("LogoutDebug", "Navigation failed!", e);
                                    Toast.makeText(getContext(), "Ошибка навигации после выхода", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Log.e("LogoutDebug", "getActivity() is null in logout onSuccess.");
                        }
                    }

                    @Override
                    public void onError(String message) {
                        Log.e("LogoutDebug", "UserRepository.logout onError called: " + message);
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), "Ошибка при выходе: " + message, Toast.LENGTH_LONG).show();
                                Log.d("LogoutDebug", "Navigating to FirstFragment after logout error (progress reset may have failed).");
                                NavOptions navOptions = new NavOptions.Builder()
                                        .setPopUpTo(R.id.mobile_navigation, true)
                                        .build();
                                try {
                                    Navigation.findNavController(v).navigate(R.id.firstFragment, null, navOptions);
                                    Log.d("LogoutDebug", "Navigation initiated after error.");
                                } catch (Exception e) {
                                    Log.e("LogoutDebug", "Navigation failed after error!", e);
                                    Toast.makeText(getContext(), "Ошибка навигации после выхода", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Log.e("LogoutDebug", "getActivity() is null in logout onError.");
                        }
                    }
                });
            });
        } else {
            Log.e("SettingsFragment", "Logout container (@+id/logoutContainer) not found in binding!");
        }
        
        // Создаем callback для обработки нажатия кнопки "Назад"
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Возвращаемся на предыдущий фрагмент
                Navigation.findNavController(view).popBackStack();
            }
        };

        // Регистрируем callback
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);
    }

}



