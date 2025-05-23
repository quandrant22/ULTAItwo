package com.example.ultai.ultai;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ultai20.R;
import com.example.ultai.ultai.adapter.ChatAdapter;
import com.example.ultai.ultai.model.ChatMessage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.ultai20.MainActivity;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.ultai.data.repository.UserRepository;
import android.widget.Toast;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import androidx.navigation.NavController;

public class UltaiFragment extends Fragment {
    private static final String TAG = "UltaiFragment";
    private RecyclerView recyclerView;
    private EditText inputMessage;
    private ImageButton sendButton;
    private LinearLayout messageInputLayout;
    private ChatAdapter chatAdapter;
    private UltaiViewModel ultaiViewModel;
    private static final String CHAT_HISTORY_PREF = "chat_history";
    private SwipeRefreshLayout swipeRefreshLayout;
    private ImageButton refreshNewsButton;
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    private boolean isKeyboardVisible = false;
    private String currentUserId;
    private UserRepository userRepository;
    private ImageButton backButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_ultai, container, false);
        
        userRepository = UserRepository.getInstance();
        currentUserId = userRepository.getCurrentUserId();
        
        if (currentUserId == null) {
            Log.e("UltaiFragment", "User ID is null, cannot fetch data.");
            Toast.makeText(requireContext(), "Ошибка: не удалось получить ID пользователя", Toast.LENGTH_SHORT).show();
            return root;
        }
        
        Log.d(TAG, "Загрузка чата для пользователя с ID: " + currentUserId);
        
        recyclerView = root.findViewById(R.id.chatRecyclerView);
        inputMessage = root.findViewById(R.id.messageEditText);
        sendButton = root.findViewById(R.id.sendButton);
        messageInputLayout = root.findViewById(R.id.messageInputLayout);
        swipeRefreshLayout = root.findViewById(R.id.swipeRefreshLayout);
        refreshNewsButton = root.findViewById(R.id.refreshNewsButton);
        backButton = root.findViewById(R.id.backButton);

        sendButton.setActivated(false);

        ultaiViewModel = new ViewModelProvider(this).get(UltaiViewModel.class);

        setupRecyclerView();
        
        loadChatHistory();

        swipeRefreshLayout.setOnRefreshListener(() -> {
            ultaiViewModel.refreshLatestResponse();
            mainHandler.postDelayed(() -> swipeRefreshLayout.setRefreshing(false), 1000);
        });
        
        inputMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                boolean hasText = s.length() > 0;
                sendButton.setActivated(hasText);
            }
        });

        ultaiViewModel.getChatMessages().observe(getViewLifecycleOwner(), messages -> {
            chatAdapter.setMessages(messages);
            if (messages != null && !messages.isEmpty()) {
                recyclerView.smoothScrollToPosition(messages.size() - 1);
            }
            
            saveChatHistory(messages);
        });

        ultaiViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            swipeRefreshLayout.setRefreshing(isLoading);
        });

        sendButton.setOnClickListener(v -> {
            sendMessage();
        });

        inputMessage.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage();
                return true;
            }
            return false;
        });

        refreshNewsButton.setOnClickListener(v -> {
            refreshNews();
        });
        
        refreshNewsButton.setVisibility(View.GONE);

        backButton.setOnClickListener(v -> {
            Log.d(TAG, "Back button clicked");
            
            if (getActivity() != null && getActivity() instanceof MainActivity) {
                MainActivity mainActivity = (MainActivity) getActivity();
                NavController navController = mainActivity.navController;
                
                try {
                    // Получаем сохраненную предыдущую страницу
                    int lastNonUltaiDestination = mainActivity.getLastNonUltaiDestination();
                    Log.d(TAG, "Last non-Ultai destination: " + lastNonUltaiDestination);
                    
                    // Переходим к сохраненной предыдущей странице
                    BottomNavigationView bottomNav = mainActivity.findViewById(R.id.nav_view);
                    if (bottomNav != null && lastNonUltaiDestination != R.id.navigation_ultai) {
                        bottomNav.setSelectedItemId(lastNonUltaiDestination);
                        Log.d(TAG, "Navigated to last non-Ultai destination: " + lastNonUltaiDestination);
                    } else {
                        // Если не удалось получить предыдущую страницу, используем стандартную навигацию
                        Log.d(TAG, "Using standard navigation back");
                        boolean navigatedUp = navController.navigateUp();
                        Log.d(TAG, "NavigateUp result: " + navigatedUp);
                        
                        if (!navigatedUp) {
                            getActivity().onBackPressed();
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error handling back navigation: " + e.getMessage());
                    getActivity().onBackPressed();
                }
            } else {
                Log.w(TAG, "Activity is null or not MainActivity");
            }
        });

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        inputMessage.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                isKeyboardVisible = true;
                hideNavigationBar();
                showKeyboard();
            } else {
                if (isKeyboardVisible) {
                    isKeyboardVisible = false;
                    if (getActivity() instanceof MainActivity) {
                        mainHandler.postDelayed(((MainActivity) getActivity())::showNavigationBar, 300);
                    }
                }
            }
        });

        recyclerView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (inputMessage.hasFocus()) {
                    inputMessage.clearFocus();
                    hideKeyboard();
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).showNavigationBar();
                    }
                }
            }
            return false;
        });

        view.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (inputMessage.hasFocus()) {
                    if (!isTouchInsideView(event, inputMessage)) {
                        inputMessage.clearFocus();
                        hideKeyboard();
                        if (getActivity() instanceof MainActivity) {
                            ((MainActivity) getActivity()).showNavigationBar();
                        }
                    }
                }
            }
            return false;
        });

        observeViewModel();
    }

    private boolean isTouchInsideView(MotionEvent event, View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        return event.getRawX() >= location[0] &&
               event.getRawX() <= location[0] + view.getWidth() &&
               event.getRawY() >= location[1] &&
               event.getRawY() <= location[1] + view.getHeight();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof MainActivity) {
            mainHandler.postDelayed(((MainActivity) getActivity())::showNavigationBar, 300);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        hideKeyboard();
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        
        chatAdapter = new ChatAdapter(requireContext(), new ArrayList<>());
        recyclerView.setAdapter(chatAdapter);
        
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    hideKeyboard();
                    inputMessage.clearFocus();
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).showNavigationBar();
                    }
                }
            }
        });
    }

    private void sendMessage() {
        String message = inputMessage.getText().toString().trim();
        if (!message.isEmpty()) {
            boolean isGreeting = isGreetingMessage(message);
            
            ultaiViewModel.sendMessage(message);
            inputMessage.setText("");
            inputMessage.clearFocus();
            hideKeyboard();
            if (getActivity() instanceof MainActivity) {
                mainHandler.postDelayed(((MainActivity) getActivity())::showNavigationBar, 300);
            }
            
            if (isGreeting) {
                mainHandler.postDelayed(() -> {
                    String[] greetingResponses = {
                        "Здравствуйте! Рад вас видеть. Чем могу помочь?",
                        "Приветствую! Как я могу быть полезен сегодня?",
                        "Добрый день! Готов ответить на ваши вопросы.",
                        "Здравствуйте! Чем я могу вам помочь сегодня?"
                    };
                    int randomIndex = (int) (Math.random() * greetingResponses.length);
                    ultaiViewModel.sendWelcomeMessage(greetingResponses[randomIndex]);
                }, 800);
            }
        }
    }

    private void hideKeyboard() {
        if (getActivity() != null) {
            isKeyboardVisible = false;
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            
            if (getActivity().getCurrentFocus() != null) {
                imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
            } 
            
            View view = getActivity().getWindow().getCurrentFocus();
            if (view != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
            
            if (getActivity().getWindow() != null) {
                getActivity().getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
                );
            }
        }
    }
    
    private void saveChatHistory(List<ChatMessage> messages) {
        if (getContext() == null || messages == null) return;
        
        SharedPreferences prefs = getContext().getSharedPreferences(
                CHAT_HISTORY_PREF + "_" + currentUserId, 
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        
        Gson gson = new Gson();
        String json = gson.toJson(messages);
        
        editor.putString("chat_messages", json);
        editor.apply();
        
        Log.d(TAG, "Сохранена история чата для пользователя: " + currentUserId);
    }
    
    private void loadChatHistory() {
        if (getContext() == null) return;
        
        SharedPreferences prefs = getContext().getSharedPreferences(
                CHAT_HISTORY_PREF + "_" + currentUserId, 
                Context.MODE_PRIVATE);
        String json = prefs.getString("chat_messages", null);
        
        if (json != null) {
            try {
                Gson gson = new Gson();
                Type type = new TypeToken<List<ChatMessage>>(){}.getType();
                List<ChatMessage> messages = gson.fromJson(json, type);
                
                if (messages != null && !messages.isEmpty()) {
                    ultaiViewModel.setChatMessages(messages);
                    Log.d(TAG, "Загружена история чата для пользователя: " + currentUserId + 
                              ", количество сообщений: " + messages.size());
                } else {
                    Log.d(TAG, "История чата пуста для пользователя " + currentUserId + ", отправляем приветствие");
                    sendWelcomeMessage();
                }
            } catch (Exception e) {
                Log.e(TAG, "Ошибка загрузки истории чата для пользователя " + 
                          currentUserId + ": " + e.getMessage());
                prefs.edit().remove("chat_messages").apply();
                sendWelcomeMessage();
            }
        } else {
            Log.d(TAG, "История чата для пользователя " + currentUserId + " не найдена");
            sendWelcomeMessage();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        hideKeyboard();
        mainHandler.removeCallbacksAndMessages(null);
    }

    private void observeViewModel() {
        ultaiViewModel.getChatMessages().observe(getViewLifecycleOwner(), messages -> {
            chatAdapter.setMessages(messages);
            
            if (messages != null && !messages.isEmpty()) {
                recyclerView.smoothScrollToPosition(messages.size() - 1);
                
                if (!messages.isEmpty() && !messages.get(messages.size() - 1).isUser()) {
                    String lastMessage = messages.get(messages.size() - 1).getMessage();
                    if (lastMessage.contains("Последние новости") || 
                        lastMessage.contains("новости по теме")) {
                        refreshNewsButton.setVisibility(View.VISIBLE);
                    } else {
                        refreshNewsButton.setVisibility(View.GONE);
                    }
                }
            }
        });
    }
    
    private void refreshNews() {
        swipeRefreshLayout.setRefreshing(true);
        
        List<ChatMessage> messages = ultaiViewModel.getChatMessages().getValue();
        if (messages != null && !messages.isEmpty()) {
            String lastNewsQuery = null;
            for (int i = messages.size() - 1; i >= 0; i--) {
                ChatMessage message = messages.get(i);
                if (message.isUser()) {
                    String text = message.getMessage().toLowerCase();
                    if (text.contains("новост") || text.contains("событи") || 
                        text.contains("происшеств") || text.contains("что нового") || 
                        text.contains("что случилось") || text.contains("что произошло")) {
                        lastNewsQuery = message.getMessage();
                        break;
                    }
                }
            }
            
            if (lastNewsQuery != null) {
                ultaiViewModel.refreshNews(lastNewsQuery);
            } else {
                ultaiViewModel.refreshNews("последние новости");
            }
        } else {
            ultaiViewModel.refreshNews("последние новости");
        }
        
        mainHandler.postDelayed(() -> {
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
            }
        }, 2000);
    }

    private void hideNavigationBar() {
        if (getActivity() != null && getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).hideNavigationBar();
        }
    }

    private void showKeyboard() {
        if (getActivity() != null && inputMessage != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(inputMessage, InputMethodManager.SHOW_IMPLICIT);
            isKeyboardVisible = true;
        }
    }

    private void showNavigationBar() {
        if (getActivity() != null && getActivity() instanceof MainActivity) {
            isKeyboardVisible = false;
            ((MainActivity) getActivity()).showNavigationBar();
        }
    }

    private void forceShowNavigationBar() {
        if (getActivity() != null && getActivity() instanceof MainActivity) {
            isKeyboardVisible = false;
            ((MainActivity) getActivity()).showNavigationBar();
        }
    }
    
    private void sendWelcomeMessage() {
        mainHandler.postDelayed(() -> {
            String[] welcomeMessages = {
                "Здравствуйте! Чем я могу вам помочь?",
                "Добрый день! Готов ответить на ваши вопросы.",
                "Приветствую вас! Как я могу вам помочь?",
                "Здравствуйте! Задайте свой вопрос, и я постараюсь помочь."
            };
            int randomIndex = (int) (Math.random() * welcomeMessages.length);
            
            String welcomeMessage = welcomeMessages[randomIndex];
            Log.d(TAG, "Отправка приветственного сообщения: " + welcomeMessage);
            ultaiViewModel.sendWelcomeMessage(welcomeMessage);
        }, 500);
    }
    
    private boolean isGreetingMessage(String message) {
        message = message.toLowerCase().trim();
        return message.equals("привет") || 
               message.equals("здравствуйте") || 
               message.equals("здравствуй") || 
               message.equals("добрый день") || 
               message.equals("доброе утро") || 
               message.equals("добрый вечер") || 
               message.equals("приветствую") || 
               message.startsWith("привет,") || 
               message.startsWith("здравствуйте,");
    }
}
