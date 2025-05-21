package com.example.ultai.planner_questionnaire;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import android.util.Log;

import com.example.ultai.data.repository.UserRepository;

import java.util.HashMap;
import java.util.Map;

public class PlannerQuestionnaireViewModel extends ViewModel {

    private static final String TAG = "PlannerQuestionnaireVM";

    private final UserRepository userRepository;
    private final MutableLiveData<Map<String, Object>> questionnaireData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> saveSuccess = new MutableLiveData<>(false);

    public PlannerQuestionnaireViewModel() {
        userRepository = UserRepository.getInstance();
        // questionnaireData.setValue(new HashMap<>()); // Инициализация будет в loadExistingData или если данных нет
        loadExistingData(); 
    }

    public LiveData<Map<String, Object>> getQuestionnaireData() {
        return questionnaireData;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getSaveSuccess() {
        return saveSuccess;
    }

    /**
     * Обновляет ответ на конкретный вопрос в карте данных.
     * @param key Ключ (идентификатор вопроса/поля).
     * @param value Значение ответа.
     */
    public void updateAnswer(String key, Object value) {
        Map<String, Object> currentData = questionnaireData.getValue();
        if (currentData == null) {
            currentData = new HashMap<>();
        }
        currentData.put(key, value);
        questionnaireData.setValue(currentData); // Уведомляем наблюдателей об изменении
    }

    /**
     * Сохраняет все собранные данные анкеты в Firebase.
     */
    public void saveFinalQuestionnaire() {
        Map<String, Object> finalData = questionnaireData.getValue();
        if (finalData == null || finalData.isEmpty()) {
            Log.e(TAG, "saveFinalQuestionnaire: Нет данных для сохранения.");
            errorMessage.setValue("Нет данных для сохранения.");
            return;
        }

        Log.d(TAG, "saveFinalQuestionnaire: Начинаем процесс сохранения анкеты планера. Размер данных: " + finalData.size());
        // Добавляем специальную метку времени для отслеживания последнего сохранения
        finalData.put("lastSavedTimestamp", System.currentTimeMillis());
        finalData.put("completed", true);
        
        isLoading.setValue(true);
        errorMessage.setValue(null);
        saveSuccess.setValue(false);

        // Временно записываем данные в лог для отладки
        for (Map.Entry<String, Object> entry : finalData.entrySet()) {
            Log.d(TAG, "saveFinalQuestionnaire: Сохраняемые данные [" + entry.getKey() + "] = " + 
                (entry.getValue() != null ? entry.getValue().toString() : "null"));
        }

        userRepository.savePlannerQuestionnaireData(finalData, new UserRepository.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Log.d(TAG, "saveFinalQuestionnaire: Анкета планера успешно сохранена в Firebase.");
                isLoading.setValue(false);
                saveSuccess.setValue(true);
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "saveFinalQuestionnaire: Ошибка при сохранении анкеты планера: " + message);
                errorMessage.setValue(message);
                isLoading.setValue(false);
                saveSuccess.setValue(false);
            }
        });
    }
    
    private void loadExistingData() {
        isLoading.setValue(true);
        errorMessage.setValue(null); // Сбрасываем предыдущие ошибки

        userRepository.getPlannerQuestionnaireData(new UserRepository.Callback<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> result) {
                if (result != null) {
                    questionnaireData.setValue(result);
                    Log.d(TAG, "Planner questionnaire data loaded successfully.");
                } else {
                    questionnaireData.setValue(new HashMap<>()); // Инициализируем пустой картой, если данных нет
                    Log.d(TAG, "No existing planner questionnaire data found, initializing empty.");
                }
                isLoading.setValue(false);
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Error loading planner questionnaire data: " + message);
                errorMessage.setValue(message);
                questionnaireData.setValue(new HashMap<>()); // Инициализируем пустой картой в случае ошибки загрузки
                isLoading.setValue(false);
            }
        });
    }
} 