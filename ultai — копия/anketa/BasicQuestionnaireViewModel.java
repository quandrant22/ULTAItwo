package com.example.ultai.anketa;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.ultai.api.ApiClient;
import com.example.ultai.api.NetworkClient;
import com.example.ultai.models.BasicQuestionnaire;
import com.example.ultai.util.CompanyDataManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public class BasicQuestionnaireViewModel extends AndroidViewModel {

    private static final String TAG = "BasicQuestionnaireVM";

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
    
    // Сетевые клиенты
    private ApiClient apiClient;
    private NetworkClient networkClient;
    
    public BasicQuestionnaireViewModel(@NonNull Application application) {
        super(application);
        
        // Инициализация анкеты
        questionnaire = new BasicQuestionnaire();
        
        // Инициализация отслеживания номера текущего вопроса
        currentQuestionNumber = new MutableLiveData<>();
        currentQuestionNumber.setValue(1);
        
        // Инициализация CompanyDataManager
        companyDataManager = CompanyDataManager.getInstance(application);
        
        // Инициализация API клиентов
        apiClient = ApiClient.getInstance(application);
        networkClient = NetworkClient.getInstance(application);
        
        // Загрузка сохраненных данных
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
    
    // Метод для сохранения данных на сервере
    public void saveQuestionnaireData(int userId, OnQuestionnaireSaveListener listener) {
        questionnaire.setUserId(userId);
        
        // Сначала сохраняем данные локально
        saveDataLocally();
        
        // Отправка данных на локальный сервер через ApiClient
        apiClient.saveBasicQuestionnaire(questionnaire, new ApiClient.ApiResponseCallback() {
            @Override
            public void onSuccess(Object response) {
                // Отправка данных на внешний сервер
                sendQuestionnaireToRemoteServer();
                
                if (listener != null) {
                    listener.onSuccess();
                }
            }

            @Override
            public void onError(String errorMessage) {
                // Данные уже сохранены локально
                // Пытаемся отправить на внешний сервер даже если локальный не сработал
                sendQuestionnaireToRemoteServer();
                
                if (listener != null) {
                    listener.onError(errorMessage + " (данные сохранены локально)");
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
    
    // Отправка данных анкеты на внешний сервер
    private void sendQuestionnaireToRemoteServer() {
        try {
            // Преобразуем объект анкеты в JSON
            JSONObject questionnaireJson = new JSONObject();
            questionnaireJson.put("userId", questionnaire.getUserId());
            questionnaireJson.put("companyName", questionnaire.getCompanyName());
            questionnaireJson.put("country", questionnaire.getCountry());
            questionnaireJson.put("activityType", questionnaire.getActivityType());
            questionnaireJson.put("productsServicesDescription", questionnaire.getProductsServicesDescription());
            questionnaireJson.put("marketScope", questionnaire.getMarketScope());
            questionnaireJson.put("businessState", questionnaire.getBusinessState());
            
            // Добавляем дополнительные поля
            if ("Локальная".equals(questionnaire.getMarketScope()) && questionnaire.getCity() != null) {
                questionnaireJson.put("city", questionnaire.getCity());
            } else if ("Национальная".equals(questionnaire.getMarketScope()) && questionnaire.getTargetCountry() != null) {
                questionnaireJson.put("targetCountry", questionnaire.getTargetCountry());
            } else if ("Международная".equals(questionnaire.getMarketScope()) && questionnaire.getTargetCountries() != null) {
                JSONArray countriesArray = new JSONArray();
                for (String country : questionnaire.getTargetCountries()) {
                    countriesArray.put(country);
                }
                questionnaireJson.put("targetCountries", countriesArray);
            }
            
            // Отправляем данные на внешний сервер
            networkClient.saveQuestionnaire(questionnaireJson, new NetworkClient.NetworkCallback<String>() {
                @Override
                public void onSuccess(String data) {
                    Log.d(TAG, "Анкета успешно отправлена на внешний сервер");
                }

                @Override
                public void onError(String message) {
                    Log.e(TAG, "Ошибка отправки анкеты на внешний сервер: " + message);
                    // Ошибку не показываем пользователю, так как данные сохранены локально
                }
            });
        } catch (JSONException e) {
            Log.e(TAG, "Ошибка формирования JSON для внешнего сервера", e);
        }
    }
    
    // Метод загрузки сохраненных данных
    private void loadSavedData() {
        // Загружаем основные данные из CompanyDataManager
        String companyName = companyDataManager.getCompanyName();
        String country = companyDataManager.getCountry();
        String activityType = companyDataManager.getActivityType();
        
        // Устанавливаем данные в анкету
        if (companyName != null && companyDataManager.hasCompanyData()) {
            questionnaire.setCompanyName(companyName);
        }
        
        if (country != null && !country.isEmpty()) {
            questionnaire.setCountry(country);
        }
        
        if (activityType != null && !activityType.isEmpty()) {
            questionnaire.setActivityType(activityType);
        }
        
        // Загружаем остальные данные из SharedPreferences
        SharedPreferences prefs = getApplication().getSharedPreferences("questionnaire_data", Context.MODE_PRIVATE);
        
        String productsDesc = prefs.getString("productsServicesDescription", null);
        String marketScope = prefs.getString("marketScope", null);
        String businessState = prefs.getString("businessState", null);
        
        if (productsDesc != null) {
            questionnaire.setProductsServicesDescription(productsDesc);
        }
        
        if (marketScope != null) {
            questionnaire.setMarketScope(marketScope);
            
            // Загружаем дополнительные поля в зависимости от типа географии
            if ("Локальная".equals(marketScope)) {
                String city = prefs.getString("city", null);
                if (city != null) {
                    questionnaire.setCity(city);
                }
            } else if ("Национальная".equals(marketScope)) {
                String targetCountry = prefs.getString("targetCountry", null);
                if (targetCountry != null) {
                    questionnaire.setTargetCountry(targetCountry);
                }
            } else if ("Международная".equals(marketScope)) {
                String targetCountriesStr = prefs.getString("targetCountries", null);
                if (targetCountriesStr != null && !targetCountriesStr.isEmpty()) {
                    String[] targetCountries = targetCountriesStr.split(",");
                    questionnaire.setTargetCountries(targetCountries);
                }
            }
        }
        
        if (businessState != null) {
            questionnaire.setBusinessState(businessState);
        }
        
        Log.d(TAG, "Загружены локальные данные анкеты: компания=" + companyName + ", страна=" + country);
    }
    
    // Интерфейс для обратного вызова при сохранении анкеты
    public interface OnQuestionnaireSaveListener {
        void onSuccess();
        void onError(String errorMessage);
    }
} 