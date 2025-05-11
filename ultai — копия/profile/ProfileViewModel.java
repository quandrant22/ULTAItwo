package com.example.ultai.profile;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.ultai.api.ApiClient;
import com.example.ultai.api.ApiResponse;
import com.example.ultai.api.models.UserResponse;
import com.example.ultai.R;
import com.example.ultai.util.CompanyDataManager;
import com.example.ultai.api.NetworkClient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProfileViewModel extends AndroidViewModel {

    private final MutableLiveData<String> mText;
    private final MutableLiveData<UserProfile> userProfile;
    private final MutableLiveData<Boolean> isEditMode;
    private final MutableLiveData<Boolean> isLoading;
    private final MutableLiveData<String> errorMessage;
    
    private final ExecutorService executorService;
    private final ApiClient apiClient;
    private final NetworkClient networkClient;

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        mText = new MutableLiveData<>();
        mText.setValue(application.getString(R.string.profile_title));
        
        userProfile = new MutableLiveData<>();
        isEditMode = new MutableLiveData<>(false);
        isLoading = new MutableLiveData<>(false);
        errorMessage = new MutableLiveData<>();
        
        executorService = Executors.newSingleThreadExecutor();
        apiClient = ApiClient.getInstance(application);
        networkClient = NetworkClient.getInstance(application);
        
        // Загружаем данные профиля при создании ViewModel
        loadUserProfile();
    }
    
    // Загрузка данных профиля из API
    public void loadUserProfile() {
        isLoading.setValue(true);
        
        // Сначала пытаемся загрузить данные из локального хранилища
        loadProfileFromLocalStorage();
        
        // Затем асинхронно загружаем данные из API
        executorService.execute(() -> {
            ApiResponse<UserResponse> response = apiClient.getUserData();
            
            if (response.isSuccess() && response.getData() != null) {
                UserResponse userData = response.getData();
                UserProfile profile = new UserProfile(
                        userData.getUser().getUsername(),
                        userData.getUser().getEmail(),
                        userData.getUser().getPhone(),
                        userData.getUser().getGender()
                );
                
                // Поскольку в классе User нет метода getRole(), загружаем роль из локального хранилища
                SharedPreferences prefs = getApplication().getSharedPreferences("user_profile", Context.MODE_PRIVATE);
                String role = prefs.getString("role", "");
                profile.setRole(role);
                
                if (userData.getCompany() != null && userData.getCompany().getName() != null) {
                    profile.setCompanyName(userData.getCompany().getName());
                } else {
                    // Если нет данных о компании в API, берём из локального хранилища
                    String companyName = CompanyDataManager.getInstance(getApplication()).getCompanyName();
                    if (CompanyDataManager.getInstance(getApplication()).hasCompanyData()) {
                        profile.setCompanyName(companyName);
                    }
                }
                
                userProfile.postValue(profile);
                errorMessage.postValue(null);
                
                // Сохраняем полученные данные локально
                saveProfileDataLocally(profile);
            }
            
            isLoading.postValue(false);
        });
    }
    
    // Сохранение данных профиля локально
    private boolean saveProfileDataLocally(UserProfile profile) {
        try {
            SharedPreferences prefs = getApplication().getSharedPreferences("user_profile", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("name", profile.getName());
            editor.putString("email", profile.getEmail());
            editor.putString("phone", profile.getPhone());
            editor.putString("gender", profile.getGender());
            editor.putString("role", profile.getRole());
            editor.putString("companyName", profile.getCompanyName());
            
            boolean success = editor.commit();
            Log.d("ProfileViewModel", "Профиль сохранен локально: " + success);
            return success;
        } catch (Exception e) {
            Log.e("ProfileViewModel", "Ошибка при сохранении профиля локально", e);
            return false;
        }
    }
    
    // Загрузка профиля из локального хранилища
    private void loadProfileFromLocalStorage() {
        try {
            SharedPreferences prefs = getApplication().getSharedPreferences("user_profile", Context.MODE_PRIVATE);
            String name = prefs.getString("name", null);
            
            if (name != null) {
                // Если есть сохраненные данные - используем их
                UserProfile savedProfile = new UserProfile(
                        name,
                        prefs.getString("email", "tokio_sakura@gmail.com"),
                        prefs.getString("phone", "+1123456789"),
                        prefs.getString("gender", "не указан")
                );
                
                savedProfile.setRole(prefs.getString("role", ""));
                
                // Получаем название компании - сначала из SharedPreferences, потом из CompanyDataManager
                String companyName = prefs.getString("companyName", null);
                if (companyName == null || companyName.isEmpty()) {
                    CompanyDataManager companyDataManager = CompanyDataManager.getInstance(getApplication());
                    if (companyDataManager.hasCompanyData()) {
                        companyName = companyDataManager.getCompanyName();
                    }
                }
                
                savedProfile.setCompanyName(companyName);
                userProfile.postValue(savedProfile);
                Log.d("ProfileViewModel", "Загружены данные из SharedPreferences");
            } else {
                // Если нет - используем тестовые данные по умолчанию
                UserProfile dummyProfile = new UserProfile(
                        "Мария",
                        "tokio_sakura@gmail.com",
                        "+1123456789",
                        "не указан"
                );
                
                // Проверяем есть ли данные о компании
                CompanyDataManager companyDataManager = CompanyDataManager.getInstance(getApplication());
                if (companyDataManager.hasCompanyData()) {
                    dummyProfile.setCompanyName(companyDataManager.getCompanyName());
                }
                
                userProfile.postValue(dummyProfile);
                Log.d("ProfileViewModel", "Использованы тестовые данные по умолчанию");
            }
        } catch (Exception e) {
            Log.e("ProfileViewModel", "Ошибка при загрузке данных из локального хранилища", e);
            
            // В случае ошибки выдаем тестовые данные
            UserProfile dummyProfile = new UserProfile(
                    "Мария",
                    "tokio_sakura@gmail.com",
                    "+1123456789",
                    "не указан"
            );
            userProfile.postValue(dummyProfile);
            errorMessage.postValue("Ошибка при загрузке профиля: " + e.getMessage());
        }
    }
    
    // Сохранение обновленных данных профиля
    public void saveUserProfile(UserProfile updatedProfile) {
        isLoading.setValue(true);
        executorService.execute(() -> {
            try {
                // Сначала сохраняем данные в локальном хранилище
                boolean localSaveSuccess = saveProfileDataLocally(updatedProfile);
                
                if (!localSaveSuccess) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        errorMessage.postValue("Ошибка при локальном сохранении данных профиля");
                        isLoading.postValue(false);
                    });
                    return;
                }
                
                // Сохраняем данные компании
                boolean companyDataSuccess = CompanyDataManager.getInstance(getApplication()).saveCompanyNameCommit(updatedProfile.getCompanyName());
                
                if (!companyDataSuccess) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        errorMessage.postValue("Предупреждение: ошибка при сохранении данных компании. Другие данные сохранены успешно.");
                        isLoading.postValue(false);
                    });
                }
                
                // Отправка данных на локальный сервер
                apiClient.updateProfile(
                    updatedProfile.getName(),
                    updatedProfile.getEmail(),
                    updatedProfile.getPhone(),
                    updatedProfile.getGender(),
                    updatedProfile.getRole(),
                    updatedProfile.getCompanyName(),
                    new ApiClient.ApiResponseCallback() {
                        @Override
                        public void onSuccess(Object response) {
                            // Обновление LiveData
                            new Handler(Looper.getMainLooper()).post(() -> {
                                userProfile.postValue(updatedProfile);
                                isEditMode.postValue(false);
                                isLoading.postValue(false);
                                errorMessage.postValue(null);
                                
                                // После успешного сохранения в локальном сервере отправляем 
                                // на внешний сервер
                                sendProfileToRemoteServer(updatedProfile);
                            });
                        }

                        @Override
                        public void onError(String message) {
                            // Обновление LiveData, даже при ошибке локального сервера
                            // используем локальные данные
                            new Handler(Looper.getMainLooper()).post(() -> {
                                userProfile.postValue(updatedProfile);
                                isEditMode.postValue(false);
                                isLoading.postValue(false);
                                errorMessage.postValue("Предупреждение: " + message + ". Данные сохранены локально.");
                                
                                // Отправка данных на внешний сервер
                                sendProfileToRemoteServer(updatedProfile);
                            });
                        }
                    }
                );
            } catch (Exception e) {
                Log.e("ProfileViewModel", "Error saving profile", e);
                new Handler(Looper.getMainLooper()).post(() -> {
                    errorMessage.postValue("Ошибка при сохранении данных профиля: " + e.getMessage());
                    isLoading.postValue(false);
                });
            }
        });
    }
    
    // Отправка данных профиля на внешний сервер
    private void sendProfileToRemoteServer(UserProfile profile) {
        networkClient.updateProfile(
            profile.getName(),
            profile.getEmail(),
            profile.getPhone(),
            profile.getGender(),
            profile.getRole(),
            profile.getCompanyName(),
            new NetworkClient.NetworkCallback<Void>() {
                @Override
                public void onSuccess(Void data) {
                    Log.d("ProfileViewModel", "Профиль успешно отправлен на внешний сервер");
                }

                @Override
                public void onError(String message) {
                    Log.e("ProfileViewModel", "Ошибка отправки профиля на внешний сервер: " + message);
                    // Сохраняем ошибку, но не показываем пользователю, так как данные уже сохранены локально
                }
            }
        );
    }
    
    // Переключение режима редактирования
    public void toggleEditMode() {
        Boolean currentMode = isEditMode.getValue();
        isEditMode.setValue(currentMode != null ? !currentMode : true);
    }

    // Геттеры для LiveData
    public LiveData<String> getText() {
        return mText;
    }
    
    public LiveData<UserProfile> getUserProfile() {
        return userProfile;
    }
    
    public LiveData<Boolean> getIsEditMode() {
        return isEditMode;
    }
    
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
    
    // Класс для хранения данных пользователя
    public static class UserProfile {
        private String name;
        private String email;
        private String phone;
        private String gender;
        private String role;
        private String companyName;
        
        public UserProfile(String name, String email, String phone, String gender) {
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.gender = gender;
            this.role = "";
            this.companyName = "";
        }
        
        // Геттеры и сеттеры
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getEmail() {
            return email;
        }
        
        public void setEmail(String email) {
            this.email = email;
        }
        
        public String getPhone() {
            return phone;
        }
        
        public void setPhone(String phone) {
            this.phone = phone;
        }
        
        public String getGender() {
            return gender;
        }
        
        public void setGender(String gender) {
            this.gender = gender;
        }
        
        public String getRole() {
            return role;
        }
        
        public void setRole(String role) {
            this.role = role;
        }
        
        public String getCompanyName() {
            return companyName;
        }
        
        public void setCompanyName(String companyName) {
            this.companyName = companyName;
        }
    }
}