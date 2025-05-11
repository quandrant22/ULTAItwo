package com.example.ultai.ui.ultai;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.ultai.data.api.ChatMessage;
import com.example.ultai.data.api.ChatRequest;
import com.example.ultai.data.api.ChatResponse;
import com.example.ultai.data.api.DeepseekApi;
import com.example.ultai.data.db.AppDatabase;
import com.example.ultai.data.db.MessageDao;
import com.example.ultai.data.model.Message;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UltaiViewModel extends AndroidViewModel {
    private final MessageDao messageDao;
    private final DeepseekApi api;
    private final MutableLiveData<Boolean> isLoading;
    private final Executor executor;

    public UltaiViewModel(@NonNull Application application) {
        super(application);
        AppDatabase database = AppDatabase.getDatabase(application);
        messageDao = database.messageDao();
        
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.deepseek.ai/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        
        api = retrofit.create(DeepseekApi.class);
        isLoading = new MutableLiveData<>(false);
        executor = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Message>> getAllMessages() {
        return messageDao.getAllMessages();
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void sendMessage(String content) {
        executor.execute(() -> {
            // Сохраняем сообщение пользователя
            Message userMessage = new Message(content, true);
            messageDao.insertMessage(userMessage);
            
            isLoading.postValue(true);
            
            try {
                // Получаем все предыдущие сообщения для контекста
                List<Message> messages = messageDao.getAllMessages().getValue();
                List<ChatMessage> chatMessages = new ArrayList<>();
                
                if (messages != null) {
                    for (Message message : messages) {
                        chatMessages.add(new ChatMessage(
                            message.isUser() ? "user" : "assistant",
                            message.getContent()
                        ));
                    }
                }
                
                // Добавляем текущее сообщение
                chatMessages.add(new ChatMessage("user", content));
                ChatRequest request = new ChatRequest(chatMessages);
                
                api.getChatCompletion(request).enqueue(new Callback<ChatResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<ChatResponse> call, @NonNull Response<ChatResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ChatResponse chatResponse = response.body();
                            if (!chatResponse.getChoices().isEmpty()) {
                                ChatMessage assistantMessage = chatResponse.getChoices().get(0).getMessage();
                                executor.execute(() -> {
                                    messageDao.insertMessage(new Message(
                                        assistantMessage.getContent(),
                                        false
                                    ));
                                });
                            }
                        }
                        isLoading.postValue(false);
                    }

                    @Override
                    public void onFailure(@NonNull Call<ChatResponse> call, @NonNull Throwable t) {
                        isLoading.postValue(false);
                    }
                });
            } catch (Exception e) {
                isLoading.postValue(false);
            }
        });
    }

    public void clearChat() {
        executor.execute(messageDao::deleteAllMessages);
    }
} 