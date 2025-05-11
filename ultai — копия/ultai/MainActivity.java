package com.example.ultai.ultai;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.navigation.NavInflater;
import androidx.navigation.NavGraph;

import com.example.ultai.R;
import com.example.ultai.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private ActivityMainBinding binding;
    public NavController navController;
    private View rootView;
    private final Handler navHandler = new Handler(Looper.getMainLooper());
    private boolean isKeyboardHiding = false;
    private ViewTreeObserver.OnGlobalLayoutListener keyboardLayoutListener;
    private boolean isNavBarVisible = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(R.style.ULTAI);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = binding.navView;
        rootView = binding.getRoot();

        // Показываем навигационную панель при запуске
        navView.setVisibility(View.VISIBLE);
        isNavBarVisible = true;

        // Получаем NavController
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        assert navHostFragment != null;
        navController = navHostFragment.getNavController();

        // Проверяем, авторизован ли пользователь
        boolean isAuthenticated = getIntent().getBooleanExtra("isAuthenticated", false);
        boolean openFirstFragment = getIntent().getBooleanExtra("openFirstFragment", false);
        Log.d(TAG, "isAuthenticated: " + isAuthenticated + ", openFirstFragment: " + openFirstFragment);
        
        // Настраиваем навигационный граф в зависимости от состояния авторизации
        NavInflater inflater = navController.getNavInflater();
        NavGraph graph = inflater.inflate(R.navigation.mobile_navigation);
        
        if (isAuthenticated) {
            // Если пользователь авторизован, начинаем с HomeFragment
            graph.setStartDestination(R.id.navigation_home);
        } else if (openFirstFragment) {
            // Если пользователь не авторизован и нужно открыть FirstFragment
            graph.setStartDestination(R.id.firstFragment);
        }
        
        navController.setGraph(graph);

        NavigationUI.setupWithNavController(navView, navController);

        // Скрытие BottomNavigationView на определённых экранах
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            int destId = destination.getId();
            if (destId == R.id.registrationFragment ||
                    destId == R.id.signInFragment ||
                    destId == R.id.anketaFragment ||
                    destId == R.id.basicQuestionnaireFragment ||
                    destId == R.id.firstFragment) {
                hideNavigationBar();
            } else {
                showNavigationBar();
            }
        });

        // Обработка нажатий в BottomNavigationView
        navView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            int currentId = navController.getCurrentDestination().getId();
            
            if (itemId != currentId) {
                // Создаем навигационные опции для предотвращения накопления фрагментов в стеке
                NavOptions navOptions = new NavOptions.Builder()
                    .setLaunchSingleTop(true)
                    .setPopUpTo(itemId, false)
                    .build();
                
                try {
                    navController.navigate(itemId, null, navOptions);
                    return true;
                } catch (Exception e) {
                    Log.e(TAG, "Ошибка навигации: " + e.getMessage());
                    return false;
                }
            }
            return true;
        });

        // Создаем слушатель для отслеживания клавиатуры
        keyboardLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            private final int screenHeight = rootView.getRootView().getHeight();
            private final Rect r = new Rect();
            private boolean isKeyboardVisible = false;

            @Override
            public void onGlobalLayout() {
                if (rootView == null || isDestroyed() || isFinishing()) {
                    return;
                }
                
                rootView.getWindowVisibleDisplayFrame(r);
                int keypadHeight = screenHeight - r.bottom;

                // Увеличиваем чувствительность обнаружения клавиатуры
                boolean isKeyboardNowVisible = keypadHeight > screenHeight * 0.05;
                
                if (isKeyboardNowVisible != isKeyboardVisible) {
                    isKeyboardVisible = isKeyboardNowVisible;
                    
                    if (isKeyboardVisible) {
                        // При появлении клавиатуры скрываем панель
                        hideNavigationBar();
                        isKeyboardHiding = false;
                    } else {
                        isKeyboardHiding = true;
                        // При скрытии клавиатуры проверяем текущий фрагмент и показываем панель
                        navHandler.removeCallbacksAndMessages(null); // Отменяем предыдущие задержки
                        navHandler.postDelayed(() -> {
                            if (!isDestroyed() && !isFinishing() && isKeyboardHiding) {
                                isKeyboardHiding = false;
                                runOnUiThread(() -> forceShowNavigationBar());
                            }
                        }, 300);
                    }
                }
            }
        };

        // Регистрируем слушатель
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(keyboardLayoutListener);

        // Новый обработчик кнопки "Назад"
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                int currentId = navController.getCurrentDestination().getId();
                if (currentId == R.id.navigation_home) {
                    finish(); // Закрытие приложения
                } else {
                    // В любом другом случае мы будем переходить на HomeFragment
                    navigateToHome();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Удаляем слушатель при уничтожении активности
        if (rootView != null && keyboardLayoutListener != null) {
            rootView.getViewTreeObserver().removeOnGlobalLayoutListener(keyboardLayoutListener);
        }
        // Очищаем все запланированные задачи
        navHandler.removeCallbacksAndMessages(null);
    }

    /**
     * Перенаправляет пользователя на HomeFragment, очищая стек навигации.
     */
    private void navigateToHome() {
        // Удаляем все фрагменты до HomeFragment из стека
        navController.popBackStack(R.id.navigation_home, false);

        // Теперь навигация на HomeFragment с очисткой стека
        navController.navigate(R.id.navigation_home, null, new NavOptions.Builder()
                .setPopUpTo(R.id.navigation_home, true) // Очистка стека до HomeFragment
                .setLaunchSingleTop(true)  // Для предотвращения множества экземпляров
                .build());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            // Открываем активность настроек
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    // Скрыть панель навигации
    public void hideNavigationBar() {
        BottomNavigationView navView = binding.navView;
        if (navView != null && navView.getVisibility() == View.VISIBLE) {
            navView.setVisibility(View.GONE);
            isNavBarVisible = false;
        }
    }
    
    // Показать панель навигации
    public void showNavigationBar() {
        BottomNavigationView navView = binding.navView;
        if (navView != null && navView.getVisibility() == View.GONE) {
            navView.setVisibility(View.VISIBLE);
            isNavBarVisible = true;
        }
    }

    // Публичный метод для принудительного отображения навигации
    public void forceShowNavigationBar() {
        BottomNavigationView navView = binding.navView;
        if (navView != null && navController != null && navController.getCurrentDestination() != null) {
            int currentDestId = navController.getCurrentDestination().getId();
            if (currentDestId != R.id.registrationFragment &&
                    currentDestId != R.id.signInFragment &&
                    currentDestId != R.id.anketaFragment &&
                    currentDestId != R.id.basicQuestionnaireFragment &&
                    currentDestId != R.id.firstFragment) {
                
                runOnUiThread(() -> {
                    navView.setVisibility(View.VISIBLE);
                    navView.setTranslationY(0);
                    navView.clearAnimation();
                    isNavBarVisible = true;
                    Log.d(TAG, "Панель навигации принудительно показана");
                });
            }
        }
    }
    
    // Метод для проверки, виден ли навбар
    public boolean isNavigationBarVisible() {
        return isNavBarVisible;
    }
}

    // Метод для проверки, виден ли навбар
    public boolean isNavigationBarVisible() {
        return isNavBarVisible;
    }
}
