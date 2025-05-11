package com.example.ultai20;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavGraph;
import androidx.navigation.NavInflater;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.navigation.NavBackStackEntry;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ultai20.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ActivityMainBinding binding;
    public NavController navController;
    private boolean isNavBarVisible = true;
    private boolean isNavBarVisibleForDestination = true;
    private Set<Integer> topLevelDestinations = new HashSet<>();
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate started.");

        mAuth = FirebaseAuth.getInstance();

        FirebaseUser initialCheckUser = mAuth.getCurrentUser();
        Log.d(TAG, ">>> IMMEDIATE onCreate check: currentUser is " + (initialCheckUser != null ? initialCheckUser.getUid() : "null"));

        setTheme(R.style.ULTAI);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = binding.navView;

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        assert navHostFragment != null;
        navController = navHostFragment.getNavController();

        boolean isAuthenticatedHint = getIntent().getBooleanExtra("isAuthenticated", false);
        Log.d(TAG, "Hint from Intent - isAuthenticated: " + isAuthenticatedHint);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        boolean actuallyAuthenticated = (currentUser != null);
        Log.d(TAG, "Actual Firebase Auth state - currentUser: " + (currentUser != null ? currentUser.getUid() : "null"));

        int startDestId;
        if (actuallyAuthenticated) {
            Log.d(TAG, "Setting start destination to Home (Based on actual check)");
            startDestId = R.id.navigation_home;
        } else {
            Log.d(TAG, "Setting start destination to FirstFragment (Based on actual check)");
            startDestId = R.id.firstFragment;
            hideNavigationBar();
        }

        NavInflater inflater = navController.getNavInflater();
        NavGraph graph = inflater.inflate(R.navigation.mobile_navigation);
        graph.setStartDestination(startDestId);

        try {
            navController.setGraph(graph, getIntent().getExtras());
        } catch (IllegalStateException e) {
            Log.e(TAG, "Failed to set graph, NavController might already have a graph?", e);
            try {
                navController.navigate(startDestId);
            } catch (Exception navEx) {
                Log.e(TAG, "Failed to navigate after setGraph failed", navEx);
            }
        }

        NavigationUI.setupWithNavController(navView, navController);

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            int destId = destination.getId();
            Log.d(TAG, "Navigated to destination: " + destination.getDisplayName() + " (ID: " + destId + ")");

            // Получаем родительский граф текущего destination
            NavGraph parentGraph = destination.getParent();
            boolean isInPlannerGraph = parentGraph != null && parentGraph.getId() == R.id.planner_nav_graph;

            if (isInPlannerGraph || // Скрываем для всех экранов анкеты
                    destId == R.id.registrationFragment ||
                    destId == R.id.signInFragment ||
                    destId == R.id.firstFragment ||
                    destId == R.id.basicQuestionnaireFragment) {
                isNavBarVisibleForDestination = false;
                hideNavigationBar();
            } else {
                isNavBarVisibleForDestination = true;
                showNavigationBar();
            }
        });

        setupKeyboardListener();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                NavDestination currentDestination = navController.getCurrentDestination();
                NavBackStackEntry previousEntry = navController.getPreviousBackStackEntry();
                
                String currentDestName = currentDestination != null ? currentDestination.getDisplayName().toString() : "null";
                String previousDestName = (previousEntry != null && previousEntry.getDestination() != null) ? previousEntry.getDestination().getDisplayName().toString() : "null";
                int startDestId = navController.getGraph().getStartDestinationId();
                String startDestName = "unknown";
                try {
                    NavDestination startDest = navController.getGraph().findNode(startDestId);
                    if (startDest != null) {
                         startDestName = startDest.getDisplayName().toString();
                    }
                } catch (Exception e) { /* ignore */ }
                
                Log.d(TAG, ">>> BACK BUTTON PRESSED <<< ");
                Log.d(TAG, "  Current Destination: " + currentDestName + " (ID: " + (currentDestination != null ? currentDestination.getId() : "null") + ")");
                Log.d(TAG, "  Previous Destination: " + previousDestName + " (ID: " + (previousEntry != null ? previousEntry.getDestination().getId() : "null") + ")");
                Log.d(TAG, "  Graph Start Destination: " + startDestName + " (ID: " + startDestId + ")");
                
                Log.d(TAG, "Calling navigateUp()...");
                if (!navController.navigateUp()) {
                    Log.d(TAG, "navigateUp() returned false, finishing activity.");
                    finish();
                } else {
                    Log.d(TAG, "navigateUp() succeeded.");
                }
            }
        });

        setupAuthStateListener();

        Log.d(TAG, "OnBackPressedCallback registered.");
        Log.d(TAG, "onCreate finished.");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: Adding MainActivity AuthStateListener.");
        if (mAuthListener != null) {
            mAuth.addAuthStateListener(mAuthListener);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: Removing MainActivity AuthStateListener.");
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void setupAuthStateListener() {
        Log.d(TAG, "Setting up AuthStateListener in MainActivity.");
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
        
        mAuthListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            Log.d(TAG, "MainActivity AuthStateListener triggered.");
            NavDestination currentDestination = navController.getCurrentDestination();
            int currentDestId = (currentDestination != null) ? currentDestination.getId() : 0;

            if (user == null) {
                // Пользователь ВЫШЕЛ
                Log.d(TAG, "User signed out (detected in MainActivity Listener). Navigating to FirstFragment.");
                // Переходим на экран входа, очищая все предыдущие экраны.
                // Делаем это только если мы еще не на экране входа/регистрации/первом экране.
                if (currentDestId != 0 && 
                    currentDestId != R.id.firstFragment && 
                    currentDestId != R.id.signInFragment && 
                    currentDestId != R.id.registrationFragment) 
                { 
                     try {
                        // Очищаем весь стек и переходим на firstFragment
                        NavOptions navOptions = new NavOptions.Builder()
                            .setPopUpTo(R.id.mobile_navigation, true) // Удаляем весь граф из стека
                            .build();
                        // Используем post, чтобы избежать проблем с навигацией во время выполнения другой навигации/изменения состояния
                        new Handler(Looper.getMainLooper()).post(() -> {
                           try {
                               if (navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() != R.id.firstFragment) { // Доп. проверка
                                    navController.navigate(R.id.firstFragment, null, navOptions);
                                    Log.d(TAG, "Navigation to firstFragment initiated by listener on sign out.");
                               }
                           } catch (Exception e) {
                                Log.e(TAG, "Navigation failed inside post (sign out)", e);
                           }
                        });
                     } catch (Exception e) {
                        Log.e(TAG, "Failed to start navigation to FirstFragment on logout", e);
                     }
                } else {
                    Log.d(TAG, "Already on auth/first screen or currentDestId is 0, no navigation needed on sign out.");
                }
            } else {
                // Пользователь ВОШЕЛ
                Log.d(TAG, "User is signed in: " + user.getUid() + " (detected in MainActivity Listener).");
                // Переходим на home, ТОЛЬКО если мы на ЭКРАНЕ ВХОДА (SignIn) или ПЕРВОМ ЭКРАНЕ (First)
                // НЕ переходим, если мы на экране РЕГИСТРАЦИИ, чтобы дать ему завершить переход на анкету.
                 if (currentDestId != 0 && 
                     (currentDestId == R.id.firstFragment || 
                      currentDestId == R.id.signInFragment)) // Убрали R.id.registrationFragment из условия
                 { 
                     Log.d(TAG, "User signed in, and currently on First/SignIn screen. Navigating to home.");
                      NavOptions navOptions = new NavOptions.Builder()
                            .setPopUpTo(R.id.mobile_navigation, true) // Удаляем весь граф из стека
                            .build();
                      new Handler(Looper.getMainLooper()).post(() -> {
                           try {
                               if (navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() != R.id.navigation_home) { // Доп. проверка
                                   navController.navigate(R.id.navigation_home, null, navOptions);
                                   Log.d(TAG, "Navigation to home initiated by listener on sign in.");
                               }
                           } catch (Exception e) {
                                Log.e(TAG, "Navigation failed inside post (sign in to home)", e);
                           }
                        });
                 } else {
                      Log.d(TAG, "User signed in, but not on First/SignIn screen (current: " + currentDestId + "). No automatic navigation needed by listener.");
                 }
            }
        };
        mAuth.addAuthStateListener(mAuthListener);
    }

    private void navigateToHome() {
        navController.popBackStack(R.id.navigation_home, false);
        navController.navigate(R.id.navigation_home, null, new NavOptions.Builder()
                .setPopUpTo(R.id.navigation_home, true)
                .setLaunchSingleTop(true)
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
            try {
                Log.d(TAG, "Navigating to SettingsFragment...");
                navController.navigate(R.id.settingsFragment);
                return true;
            } catch (Exception e) {
                Log.e(TAG, "Failed to navigate to SettingsFragment", e);
                return false;
            }
        }
        return NavigationUI.onNavDestinationSelected(item, navController) || super.onOptionsItemSelected(item);
    }

    public void hideNavigationBar() {
        BottomNavigationView navView = binding.navView;
        if (navView != null && navView.getVisibility() == View.VISIBLE) {
            navView.setVisibility(View.GONE);
            isNavBarVisible = false;
            Log.d(TAG, "NavBar hidden.");
        }
    }

    public void showNavigationBar() {
        BottomNavigationView navView = binding.navView;
        if (navView != null && navView.getVisibility() == View.GONE) {
            navView.setVisibility(View.VISIBLE);
            isNavBarVisible = true;
            Log.d(TAG, "NavBar shown.");
        }
    }

    private void setupKeyboardListener() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, windowInsets) -> {
            boolean isImeVisible = windowInsets.isVisible(WindowInsetsCompat.Type.ime());
            
            if (isImeVisible) {
                hideNavigationBar();
            } else {
                if (isNavBarVisibleForDestination) {
                    showNavigationBar();
                }
            }
            return windowInsets;
        });
    }
}
