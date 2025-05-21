package com.example.ultai.profile;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.ultai.data.repository.UserRepository;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.Map;

public class ProfileViewModel extends AndroidViewModel {

    private static final String TAG = "ProfileViewModel";

    private final UserRepository userRepository;

    // LiveData для UI
    private final MutableLiveData<FirebaseUser> firebaseUserLiveData = new MutableLiveData<>();
    // UserProfile будет содержать данные из /users/$uid/profile (если есть специфичные поля)
    // и данные из /users/$uid/basicQuestionnaire
    private final MutableLiveData<UserProfile> combinedUserProfileLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isEditModeLiveData = new MutableLiveData<>(false); // Оставляем, если есть режим редактирования

    // Внутренний класс для представления объединенного профиля
    // Можно расширить полями из вашей анкеты
    public static class UserProfile {
        private String uid;
        private String name; // Из FirebaseUser.displayName или /profile
        private String email; // Из FirebaseUser.email
        private String phone; // Из /profile или /basicQuestionnaire
        private String gender; // Из /profile или /basicQuestionnaire
        private String companyName; // Из /basicQuestionnaire
        private String activityType; // Из /basicQuestionnaire
        private String productsServicesDescription; // Из /basicQuestionnaire
        // ... другие поля из BasicQuestionnaire ...

        public UserProfile() {}

        // Геттеры и Сеттеры
        public String getUid() { return uid; }
        public void setUid(String uid) { this.uid = uid; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getGender() { return gender; }
        public void setGender(String gender) { this.gender = gender; }
        public String getCompanyName() { return companyName; }
        public void setCompanyName(String companyName) { this.companyName = companyName; }
        public String getActivityType() { return activityType; }
        public void setActivityType(String activityType) { this.activityType = activityType; }
        public String getProductsServicesDescription() { return productsServicesDescription; }
        public void setProductsServicesDescription(String productsServicesDescription) { this.productsServicesDescription = productsServicesDescription; }
    }

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        userRepository = UserRepository.getInstance();
        loadAllProfileData();
    }

    public LiveData<FirebaseUser> getFirebaseUserLiveData() {
        return firebaseUserLiveData;
    }

    public LiveData<UserProfile> getCombinedUserProfileLiveData() {
        return combinedUserProfileLiveData;
    }

    public LiveData<Boolean> getIsLoadingLiveData() {
        return isLoadingLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public LiveData<Boolean> getIsEditModeLiveData() {
        return isEditModeLiveData;
    }

    public void toggleEditMode() {
        isEditModeLiveData.setValue(Boolean.FALSE.equals(isEditModeLiveData.getValue()));
    }

    public void loadAllProfileData() {
        isLoadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        FirebaseUser currentUser = userRepository.getCurrentUser();
        firebaseUserLiveData.setValue(currentUser);

        if (currentUser == null) {
            errorLiveData.setValue("Пользователь не аутентифицирован.");
            isLoadingLiveData.setValue(false);
            combinedUserProfileLiveData.setValue(null); // Очищаем профиль
            return;
        }

        final UserProfile tempProfile = new UserProfile();
        tempProfile.setUid(currentUser.getUid());
        if (currentUser.getDisplayName() != null && !currentUser.getDisplayName().isEmpty()) {
            tempProfile.setName(currentUser.getDisplayName());
        }
        if (currentUser.getEmail() != null) {
            tempProfile.setEmail(currentUser.getEmail());
        }

        // 1. Загрузка данных из /users/$uid/profile (если есть специфичные поля)
        userRepository.getUserProfile(new UserRepository.Callback<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> profileDataMap) {
                if (profileDataMap != null) {
                    // Логирование всех полученных данных
                    Log.d(TAG, "ProfileData from Firebase: " + profileDataMap.toString());
                    
                    // Пример извлечения данных, которые могли быть сохранены при регистрации
                    // (кроме registrationTest, которое мы использовали для теста)
                    if (profileDataMap.get("username") != null && tempProfile.getName() == null) { // Если display name не был установлен
                        tempProfile.setName((String) profileDataMap.get("username"));
                        Log.d(TAG, "Setting name from profile data: " + profileDataMap.get("username"));
                    }
                    if (profileDataMap.get("phone") != null) {
                        tempProfile.setPhone((String) profileDataMap.get("phone"));
                        Log.d(TAG, "Setting phone from profile data: " + profileDataMap.get("phone"));
                    } else {
                        Log.w(TAG, "Phone field is NULL or missing in profile data");
                    }
                    if (profileDataMap.get("gender") != null) {
                        tempProfile.setGender((String) profileDataMap.get("gender"));
                        Log.d(TAG, "Setting gender from profile data: " + profileDataMap.get("gender"));
                    }
                } else {
                    Log.w(TAG, "Profile data is NULL - no data found in Firebase");
                }
                // 2. После загрузки профиля, загружаем данные анкеты
                loadQuestionnaireData(tempProfile);
            }

            @Override
            public void onError(String message) {
                Log.w(TAG, "Error loading user profile node: " + message);
                // Продолжаем загрузку анкеты, даже если узел /profile не найден или с ошибкой
                loadQuestionnaireData(tempProfile);
            }
        });
    }

    private void loadQuestionnaireData(UserProfile buildingProfile) {
        userRepository.getBasicQuestionnaireData(new UserRepository.Callback<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> questionnaireDataMap) {
                if (questionnaireDataMap != null) {
                    buildingProfile.setCompanyName((String) questionnaireDataMap.get("companyName"));
                    buildingProfile.setActivityType((String) questionnaireDataMap.get("activityType"));
                    buildingProfile.setProductsServicesDescription((String) questionnaireDataMap.get("productsServicesDescription"));
                    // Добавьте другие поля из BasicQuestionnaire, которые есть в questionnaireDataMap
                    // Например: buildingProfile.setCountry((String) questionnaireDataMap.get("country"));
                    // buildingProfile.setMarketScope((String) questionnaireDataMap.get("marketScope"));
                    // buildingProfile.setBusinessState((String) questionnaireDataMap.get("businessState"));
                    // buildingProfile.setCity((String) questionnaireDataMap.get("city"));
                }
                combinedUserProfileLiveData.setValue(buildingProfile);
                isLoadingLiveData.setValue(false);
                        }

                        @Override
                        public void onError(String message) {
                Log.e(TAG, "Error loading basic questionnaire data: " + message);
                errorLiveData.setValue("Ошибка загрузки данных анкеты: " + message);
                 // Даже если анкета не загрузилась, показываем то, что есть (например, из FirebaseUser и /profile)
                combinedUserProfileLiveData.setValue(buildingProfile);
                isLoadingLiveData.setValue(false);
            }
        });
    }
    
    // Метод для сохранения обновленного профиля (если будет режим редактирования)
    public void saveUserProfile(UserProfile updatedProfile) {
        isLoadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        if (userRepository.getCurrentUser() == null) {
            errorLiveData.setValue("Пользователь не аутентифицирован для сохранения.");
            isLoadingLiveData.setValue(false);
            return;
        }

        // 1. Обновление FirebaseUser (DisplayName, Email, если они менялись и это разрешено)
        FirebaseUser currentUser = userRepository.getCurrentUser();
        // TODO: Реализовать обновление FirebaseUser, если нужно (updateProfile, updateEmail)
        // Это асинхронные операции, их нужно будет правильно встроить в цепочку.

        // 2. Подготовка данных для сохранения в /users/$uid/profile и /users/$uid/basicQuestionnaire
        Map<String, Object> profileNodeData = new HashMap<>();
        // Поля, которые идут в /profile (если такие есть, кроме тех, что в анкете)
        // profileNodeData.put("username", updatedProfile.getName()); // Если отличается от FirebaseUser.displayName
        if (updatedProfile.getPhone() != null) profileNodeData.put("phone", updatedProfile.getPhone());
        if (updatedProfile.getGender() != null) profileNodeData.put("gender", updatedProfile.getGender());

        Map<String, Object> questionnaireNodeData = new HashMap<>();
        // Поля, которые идут в /basicQuestionnaire
        if (updatedProfile.getCompanyName() != null) questionnaireNodeData.put("companyName", updatedProfile.getCompanyName());
        if (updatedProfile.getActivityType() != null) questionnaireNodeData.put("activityType", updatedProfile.getActivityType());
        if (updatedProfile.getProductsServicesDescription() != null) questionnaireNodeData.put("productsServicesDescription", updatedProfile.getProductsServicesDescription());
        // ... добавьте остальные поля анкеты ...

        // Сохраняем данные профиля (если есть что сохранять)
        if (!profileNodeData.isEmpty()) {
            userRepository.saveUserProfileData(profileNodeData, new UserRepository.Callback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    Log.d(TAG, "User profile node data updated successfully.");
                    // Теперь сохраняем данные анкеты
                    saveQuestionnaireNode(questionnaireNodeData, updatedProfile);
                }

                @Override
                public void onError(String message) {
                    Log.e(TAG, "Failed to update user profile node data: " + message);
                    errorLiveData.setValue("Ошибка обновления профиля: " + message);
                    isLoadingLiveData.setValue(false);
                }
            });
        } else {
            // Если в /profile ничего не менялось, сразу сохраняем анкету
            saveQuestionnaireNode(questionnaireNodeData, updatedProfile);
        }
    }

    private void saveQuestionnaireNode(Map<String, Object> questionnaireNodeData, UserProfile originalUpdatedProfile) {
        if (!questionnaireNodeData.isEmpty()) {
            userRepository.saveBasicQuestionnaireData(questionnaireNodeData, new UserRepository.Callback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    Log.d(TAG, "Basic questionnaire data updated successfully.");
                    combinedUserProfileLiveData.setValue(originalUpdatedProfile); // Обновляем LiveData полным профилем
                    isEditModeLiveData.setValue(false);
                    isLoadingLiveData.setValue(false);
                }

                @Override
                public void onError(String message) {
                    Log.e(TAG, "Failed to update basic questionnaire data: " + message);
                    errorLiveData.setValue("Ошибка обновления анкеты: " + message);
                    isLoadingLiveData.setValue(false);
                }
            });
        } else {
            // Если и в анкете ничего не менялось (маловероятно при редактировании)
            Log.d(TAG, "No data to update in questionnaire node.");
            combinedUserProfileLiveData.setValue(originalUpdatedProfile);
            isEditModeLiveData.setValue(false);
            isLoadingLiveData.setValue(false);
        }
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        // Если бы у нас был ExecutorService, мы бы его здесь останавливали
        // executorService.shutdown(); 
    }
}