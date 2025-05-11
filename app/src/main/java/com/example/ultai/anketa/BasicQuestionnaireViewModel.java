package com.example.ultai.anketa;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.ultai.data.repository.UserRepository;
import com.example.ultai.models.BasicQuestionnaire;
import com.example.ultai.util.CompanyDataManager;

import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import com.google.gson.reflect.TypeToken;

public class BasicQuestionnaireViewModel extends AndroidViewModel {

    private static final String TAG = "BasicQuestionnaireVM";
    private UserRepository userRepository;

    // Данные анкеты
    private BasicQuestionnaire questionnaire;
    
    // Текущий номер вопроса
    private MutableLiveData<Integer> currentQuestionNumber;
    
    // Типы деятельности
    private List<String> activityTypes = Arrays.asList("Производство", "Товары", "Услуги");
    
    // Типы географии реализации
    private List<String> marketScopes = Arrays.asList("Локальная", "Национальная", "Международная");
    
    // Список стран
    private List<String> countries = Arrays.asList("Россия", "США", "Китай", "Германия", "Франция", "Великобритания"); 
    
    // Список городов по странам (для примера)
    private List<String> russianCities = Arrays.asList("Москва", "Санкт-Петербург", "Казань", "Екатеринбург");
    private List<String> usaCities = Arrays.asList("Нью-Йорк", "Лос-Анджелес", "Чикаго", "Хьюстон");
    
    // Менеджер данных компании
    private CompanyDataManager companyDataManager;
    
    public BasicQuestionnaireViewModel(@NonNull Application application) {
        super(application);
        
        userRepository = UserRepository.getInstance();
        questionnaire = new BasicQuestionnaire();
        currentQuestionNumber = new MutableLiveData<>();
        currentQuestionNumber.setValue(1);
        companyDataManager = CompanyDataManager.getInstance(application);
        
        loadSavedData();
    }
    
    // Методы для управления текущим вопросом
    public LiveData<Integer> getCurrentQuestionNumber() {
        return currentQuestionNumber;
    }
    
    public void nextQuestion() {
        int next = currentQuestionNumber.getValue() + 1;
        currentQuestionNumber.setValue(next);
    }
    
    public void previousQuestion() {
        int prev = currentQuestionNumber.getValue() - 1;
        if (prev >= 1) {
            currentQuestionNumber.setValue(prev);
        }
    }
    
    // Методы для получения списков данных
    public List<String> getActivityTypes() {
        return activityTypes;
    }
    
    public List<String> getMarketScopes() {
        return marketScopes;
    }
    
    public List<String> getCountries() {
        return countries;
    }
    
    public List<String> getCitiesByCountry(String country) {
        if ("Россия".equals(country)) {
            return russianCities;
        } else if ("США".equals(country)) {
            return usaCities;
        }
        return Arrays.asList("Другой город");
    }
    
    // Методы для работы с данными анкеты
    public BasicQuestionnaire getQuestionnaire() {
        return questionnaire;
    }
    
    public void setQuestionnaire(BasicQuestionnaire questionnaire) {
        this.questionnaire = questionnaire;
    }
    
    public void setCompanyName(String companyName) {
        questionnaire.setCompanyName(companyName);
        // Сохраняем название компании в CompanyDataManager
        companyDataManager.saveCompanyName(companyName);
    }
    
    public void setCountry(String country) {
        questionnaire.setCountry(country);
        // Сохраняем страну компании в CompanyDataManager
        companyDataManager.saveCountry(country);
    }
    
    public void setActivityType(String activityType) {
        questionnaire.setActivityType(activityType);
        // Сохраняем тип деятельности компании в CompanyDataManager
        companyDataManager.saveActivityType(activityType);
    }
    
    public void setProductsServicesDescription(String description) {
        questionnaire.setProductsServicesDescription(description);
    }
    
    public void setMarketScope(String marketScope) {
        questionnaire.setMarketScope(marketScope);
    }
    
    public void setBusinessState(String businessState) {
        questionnaire.setBusinessState(businessState);
    }
    
    public void setCity(String city) {
        questionnaire.setCity(city);
    }
    
    public void setTargetCountry(String targetCountry) {
        questionnaire.setTargetCountry(targetCountry);
    }
    
    public void setTargetCountries(String[] targetCountries) {
        questionnaire.setTargetCountries(targetCountries);
    }
    
    // Обновленный метод для сохранения данных анкеты в Firebase RTDB
    public void saveQuestionnaireDataToFirebase(final OnQuestionnaireSaveListener listener) {
        FirebaseUser currentUser = userRepository.getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "saveQuestionnaireDataToFirebase: User not authenticated!");
            if (listener != null) {
                listener.onError("Пользователь не авторизован для сохранения анкеты.");
            }
            return;
        }

        // Локальное сохранение остается как есть, если оно все еще нужно
        saveDataLocally(); 
        Log.d(TAG, "saveQuestionnaireDataToFirebase: Local save complete. Proceeding to Firebase save.");

        // Преобразуем объект questionnaire в Map<String, Object>
        Gson gson = new Gson();
        String jsonQuestionnaire = gson.toJson(questionnaire);
        Type type = new TypeToken<Map<String, Object>>(){}.getType();
        Map<String, Object> questionnaireMap = gson.fromJson(jsonQuestionnaire, type);

        if (questionnaireMap != null) {
            questionnaireMap.remove("id"); 
            questionnaireMap.remove("userId"); 
        } else {
            Log.e(TAG, "saveQuestionnaireDataToFirebase: questionnaireMap is null after Gson conversion!");
            if (listener != null) {
                listener.onError("Ошибка подготовки данных анкеты для сохранения.");
            }
            return;
        }

        userRepository.saveBasicQuestionnaireData(questionnaireMap, new UserRepository.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Log.d(TAG, "saveQuestionnaireDataToFirebase: Successfully saved to Firebase RTDB.");
                if (listener != null) {
                    listener.onSuccess();
                }
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "saveQuestionnaireDataToFirebase: Failed to save to Firebase RTDB. Message: " + message);
                if (listener != null) {
                    listener.onError("Ошибка сохранения анкеты на сервере: " + message + " (данные могут быть сохранены локально)");
                }
            }
        });
    }
    
    // Метод для локального сохранения данных
    private void saveDataLocally() {
        // Сохраняем все основные данные анкеты в CompanyDataManager
        companyDataManager.saveCompanyName(questionnaire.getCompanyName());
        companyDataManager.saveCountry(questionnaire.getCountry());
        companyDataManager.saveActivityType(questionnaire.getActivityType());
        companyDataManager.saveActivityDescription(questionnaire.getProductsServicesDescription());
        
        // Создадим кастомный SharedPreferences для остальных данных анкеты
        SharedPreferences prefs = getApplication().getSharedPreferences("questionnaire_data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        
        editor.putString("productsServicesDescription", questionnaire.getProductsServicesDescription());
        editor.putString("marketScope", questionnaire.getMarketScope());
        editor.putString("businessState", questionnaire.getBusinessState());
        
        // Дополнительно сохраняем описание деятельности в отдельном SharedPreferences для главной страницы
        SharedPreferences basicQuestPrefs = getApplication().getSharedPreferences("basic_questionnaire", Context.MODE_PRIVATE);
        basicQuestPrefs.edit()
            .putString("productsServicesDescription", questionnaire.getProductsServicesDescription())
            .putString("companyName", questionnaire.getCompanyName())
            .commit();
        
        // Сохраняем дополнительные поля в зависимости от типа географии
        if ("Локальная".equals(questionnaire.getMarketScope()) && questionnaire.getCity() != null) {
            editor.putString("city", questionnaire.getCity());
        } else if ("Национальная".equals(questionnaire.getMarketScope()) && questionnaire.getTargetCountry() != null) {
            editor.putString("targetCountry", questionnaire.getTargetCountry());
        } else if ("Международная".equals(questionnaire.getMarketScope()) && questionnaire.getTargetCountries() != null) {
            // Для массива targetCountries преобразуем его в строку, разделенную запятыми
            if (questionnaire.getTargetCountries() != null && questionnaire.getTargetCountries().length > 0) {
                editor.putString("targetCountries", String.join(",", questionnaire.getTargetCountries()));
            }
        }
        
        // Применяем изменения
        boolean success = editor.commit();
        Log.d(TAG, "Локальное сохранение анкеты: " + (success ? "успешно" : "c ошибкой"));
    }
    
    // Метод загрузки сохраненных данных
    private void loadSavedData() {
        // Загружаем основные данные из CompanyDataManager
        questionnaire.setCompanyName(companyDataManager.getCompanyName());
        questionnaire.setCountry(companyDataManager.getCountry());
        questionnaire.setActivityType(companyDataManager.getActivityType());
        questionnaire.setProductsServicesDescription(companyDataManager.getActivityDescription());
        
        // Загружаем остальные данные из SharedPreferences
        SharedPreferences prefs = getApplication().getSharedPreferences("questionnaire_data", Context.MODE_PRIVATE);
        questionnaire.setMarketScope(prefs.getString("marketScope", null));
        questionnaire.setBusinessState(prefs.getString("businessState", null));
        questionnaire.setCity(prefs.getString("city", null));
        questionnaire.setTargetCountry(prefs.getString("targetCountry", null));
        
        String targetCountriesStr = prefs.getString("targetCountries", null);
        if (targetCountriesStr != null && !targetCountriesStr.isEmpty()) {
            questionnaire.setTargetCountries(targetCountriesStr.split(","));
        }
        
        // Вы можете решить, нужно ли восстанавливать номер текущего вопроса
        // currentQuestionNumber.setValue(prefs.getInt("currentQuestion", 1)); 
    }
    
    // Интерфейс для обратного вызова при сохранении анкеты
    public interface OnQuestionnaireSaveListener {
        void onSuccess();
        void onError(String errorMessage);
    }
} 