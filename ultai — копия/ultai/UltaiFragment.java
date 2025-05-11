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

import com.example.ultai.R;
import com.example.ultai.ultai.adapter.ChatAdapter;
import com.example.ultai.ultai.model.ChatMessage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

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
    private String currentUserId; // Идентификатор текущего пользователя

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_ultai, container, false);
        
        // Получаем ID текущего пользователя
        com.example.ultai.api.ApiClient apiClient = com.example.ultai.api.ApiClient.getInstance(requireContext());
        currentUserId = apiClient.getUserId();
        
        // Если пользователь не авторизован, используем временный идентификатор
        if (currentUserId == null || currentUserId.isEmpty()) {
            currentUserId = "guest";
        }
        
        Log.d(TAG, "Загрузка чата для пользователя с ID: " + currentUserId);
        
        recyclerView = root.findViewById(R.id.chatRecyclerView);
        inputMessage = root.findViewById(R.id.messageEditText);
        sendButton = root.findViewById(R.id.sendButton);
        messageInputLayout = root.findViewById(R.id.messageInputLayout);
        swipeRefreshLayout = root.findViewById(R.id.swipeRefreshLayout);
        refreshNewsButton = root.findViewById(R.id.refreshNewsButton);

        // Устанавливаем начальное состояние кнопки отправки
        sendButton.setActivated(false);

        // Инициализируем ViewModel до использования
        ultaiViewModel = new ViewModelProvider(this).get(UltaiViewModel.class);

        setupRecyclerView();
        
        // Загружаем историю чата
        loadChatHistory();

        // Настройка SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // Обработка обновления чата
            ultaiViewModel.refreshLatestResponse();
            // Скрываем индикатор через 1 секунду
            mainHandler.postDelayed(() -> swipeRefreshLayout.setRefreshing(false), 1000);
        });
        
        // Добавляем слушатель для отслеживания изменений в поле ввода
        inputMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                // Активируем или деактивируем кнопку в зависимости от наличия текста
                boolean hasText = s.length() > 0;
                sendButton.setActivated(hasText);
            }
        });

        // Наблюдение за сообщениями чата
        ultaiViewModel.getChatMessages().observe(getViewLifecycleOwner(), messages -> {
            chatAdapter.setMessages(messages);
            if (messages != null && !messages.isEmpty()) {
                recyclerView.smoothScrollToPosition(messages.size() - 1);
            }
            
            // Сохраняем историю чата
            saveChatHistory(messages);
        });

        // Наблюдение за состоянием загрузки
        ultaiViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            swipeRefreshLayout.setRefreshing(isLoading);
        });

        // Обработка нажатия на кнопку отправки
        sendButton.setOnClickListener(v -> {
            sendMessage();
        });

        // Обработка нажатия на Enter в поле ввода
        inputMessage.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage();
                return true;
            }
            return false;
        });

        // Настраиваем кнопку обновления новостей
        refreshNewsButton.setOnClickListener(v -> {
            refreshNews();
        });
        
        // Скрываем кнопку обновления новостей по умолчанию
        refreshNewsButton.setVisibility(View.GONE);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Добавляем обработку фокуса для скрытия/показа панели навигации
        inputMessage.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                isKeyboardVisible = true;
                hideNavigationBar();
                showKeyboard();
            } else {
                if (isKeyboardVisible) {
                    isKeyboardVisible = false;
                    // Добавляем небольшую задержку перед показом навигационной панели
                    mainHandler.postDelayed(this::forceShowNavigationBar, 300);
                }
            }
        });

        // Обработчик касания в RecyclerView для скрытия клавиатуры
        recyclerView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (inputMessage.hasFocus()) {
                    inputMessage.clearFocus();
                    hideKeyboard();
                    forceShowNavigationBar();
                }
            }
            return false; // Позволяем дальнейшую обработку события
        });

        // Обработка касания вне поля ввода для скрытия клавиатуры
        view.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (inputMessage.hasFocus()) {
                    // Проверяем, что касание не в поле ввода
                    if (!isTouchInsideView(event, inputMessage)) {
                        inputMessage.clearFocus();
                        hideKeyboard();
                        forceShowNavigationBar();
                    }
                }
            }
            return false; // Позволяем дальнейшую обработку события
        });

        observeViewModel();
    }

    // Проверка, находится ли касание внутри заданного View
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
        // Обеспечиваем видимость панели навигации
        mainHandler.postDelayed(this::forceShowNavigationBar, 300);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Убедимся, что клавиатура скрыта при приостановке фрагмента
        hideKeyboard();
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        
        chatAdapter = new ChatAdapter(requireContext(), new ArrayList<>());
        recyclerView.setAdapter(chatAdapter);
        
        // Регистрируем обработчик прокрутки для более плавного скроллинга
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    // Пользователь начал прокрутку вручную
                    hideKeyboard();
                    inputMessage.clearFocus();
                    forceShowNavigationBar();
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
            inputMessage.clearFocus(); // Убираем фокус с поля ввода
            hideKeyboard();
            // Восстанавливаем навигационную панель с небольшой задержкой
            mainHandler.postDelayed(this::forceShowNavigationBar, 300);
            
            // Если пользователь отправил приветствие, бот отвечает на него вежливо
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
            
            // Пытаемся скрыть клавиатуру разными способами
            if (getActivity().getCurrentFocus() != null) {
                imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
            } 
            
            // И вторая попытка
            View view = getActivity().getWindow().getCurrentFocus();
            if (view != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
            
            // Еще один метод скрытия клавиатуры
            if (getActivity().getWindow() != null) {
                getActivity().getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
                );
            }
        }
    }
    
    /**
     * Сохраняет историю чата в SharedPreferences
     */
    private void saveChatHistory(List<ChatMessage> messages) {
        if (getContext() == null || messages == null) return;
        
        // Используем отдельные настройки для каждого пользователя
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
    
    /**
     * Загружает историю чата из SharedPreferences
     */
    private void loadChatHistory() {
        if (getContext() == null) return;
        
        // Используем отдельные настройки для каждого пользователя
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
                    // Если история пуста, отправляем приветственное сообщение
                    Log.d(TAG, "История чата пуста для пользователя " + currentUserId + ", отправляем приветствие");
                    sendWelcomeMessage();
                }
            } catch (Exception e) {
                Log.e(TAG, "Ошибка загрузки истории чата для пользователя " + 
                          currentUserId + ": " + e.getMessage());
                // В случае ошибки - очищаем поврежденную историю и отправляем приветствие
                prefs.edit().remove("chat_messages").apply();
                sendWelcomeMessage();
            }
        } else {
            Log.d(TAG, "История чата для пользователя " + currentUserId + " не найдена");
            // Если истории нет, это первый запуск чата - отправляем приветствие
            sendWelcomeMessage();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        hideKeyboard();
        // Удаляем все запланированные задачи
        mainHandler.removeCallbacksAndMessages(null);
    }

    private void observeViewModel() {
        ultaiViewModel.getChatMessages().observe(getViewLifecycleOwner(), messages -> {
            chatAdapter.setMessages(messages);
            
            if (messages != null && !messages.isEmpty()) {
                recyclerView.smoothScrollToPosition(messages.size() - 1);
                
                // Проверяем, содержит ли последнее сообщение новости
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
    
    /**
     * Обновляет новости
     */
    private void refreshNews() {
        // Показываем индикатор загрузки
        swipeRefreshLayout.setRefreshing(true);
        
        // Получаем последнее сообщение пользователя, связанное с новостями
        List<ChatMessage> messages = ultaiViewModel.getChatMessages().getValue();
        if (messages != null && !messages.isEmpty()) {
            // Ищем последний запрос пользователя о новостях
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
                // Отправляем запрос на обновление новостей
                ultaiViewModel.refreshNews(lastNewsQuery);
            } else {
                // Если запрос не найден, просто запрашиваем последние новости
                ultaiViewModel.refreshNews("последние новости");
            }
        } else {
            // Если история сообщений пуста, запрашиваем последние новости
            ultaiViewModel.refreshNews("последние новости");
        }
        
        // Скрываем индикатор загрузки через 2 секунды
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
            ((MainActivity) getActivity()).forceShowNavigationBar();
        }
    }
    
    /**
     * Отправляет приветственное сообщение новому пользователю
     */
    private void sendWelcomeMessage() {
        // Небольшая задержка для загрузки UI
        mainHandler.postDelayed(() -> {
            // Случайное приветственное сообщение
            String[] welcomeMessages = {
                "Здравствуйте! Чем я могу вам помочь?",
                "Добрый день! Готов ответить на ваши вопросы.",
                "Приветствую вас! Как я могу вам помочь?",
                "Здравствуйте! Задайте свой вопрос, и я постараюсь помочь."
            };
            int randomIndex = (int) (Math.random() * welcomeMessages.length);
            
            // Важно: используем метод, который явно указывает, что сообщение от бота
            String welcomeMessage = welcomeMessages[randomIndex];
            Log.d(TAG, "Отправка приветственного сообщения: " + welcomeMessage);
            ultaiViewModel.sendWelcomeMessage(welcomeMessage);
        }, 500);
    }
    
    /**
     * Проверяет, является ли сообщение приветствием
     */
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