package com.example.ultai.data.repository;

import android.util.Log;
import androidx.annotation.NonNull;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Репозиторий для работы с пользователями и их данными через Firebase.
 * Использует FirebaseAuth для аутентификации и Firebase Realtime Database для данных.
 */
public class UserRepository {
    private static final String TAG = "UserRepository";
    private static final String USERS_NODE = "users"; // Узел для пользовательских данных в RTDB
    private static final String PROFILE_NODE = "profile";
    private static final String PLANNER_PROGRESS_NODE = "plannerProgress";
    private static final String BASIC_QUESTIONNAIRE_NODE = "basicQuestionnaire"; // Константа для узла базовой анкеты
    private static final String PLANNER_QUESTIONNAIRE_NODE = "plannerQuestionnaire"; // <-- НОВАЯ КОНСТАНТА
    
    private final FirebaseAuth firebaseAuth;
    private final DatabaseReference databaseReference;
    
    // Синглтон
    private static UserRepository instance;
    
    public static synchronized UserRepository getInstance() {
        if (instance == null) {
            instance = new UserRepository();
        }
        return instance;
    }
    
    private UserRepository() {
        Log.e(TAG, "!!!!!!!!!! EXECUTING UserRepository CONSTRUCTOR (NEW CODE CHECK) !!!!!!!!!!"); // Очень заметный лог

        firebaseAuth = FirebaseAuth.getInstance();
        
        String rtdbUrl = "https://ultai-c73e5-default-rtdb.europe-west1.firebasedatabase.app/"; // Ваш актуальный URL
        DatabaseReference tempDatabaseReference; // Временная локальная переменная

        Log.d(TAG, "Attempting to initialize FirebaseDatabase with URL: " + rtdbUrl);
        try {
            tempDatabaseReference = FirebaseDatabase.getInstance(rtdbUrl).getReference();
            Log.d(TAG, "Successfully initialized tempDatabaseReference with specific URL.");
        } catch (Exception e) {
            Log.e(TAG, "ERROR initializing FirebaseDatabase with specific URL: " + rtdbUrl, e);
            Log.e(TAG, "Falling back to default FirebaseDatabase instance.");
            tempDatabaseReference = FirebaseDatabase.getInstance().getReference();
        }
        databaseReference = tempDatabaseReference; // Однократное присваивание final полю
    }

    /**
     * Интерфейс для обратных вызовов операций репозитория.
     */
    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String message);
    }

    /**
     * Получает текущего аутентифицированного пользователя Firebase.
     * @return FirebaseUser или null, если пользователь не вошел.
     */
    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    /**
     * Получает UID текущего пользователя.
     * @return UID или null.
     */
    public String getCurrentUserId() {
        FirebaseUser user = getCurrentUser();
        return (user != null) ? user.getUid() : null;
    }


    /**
     * Регистрация нового пользователя.
     * @param username Имя пользователя (для профиля)
     * @param email Email
     * @param password Пароль
     * @param gender Пол (для профиля)
     * @param phone Телефон (для профиля)
     * @param callback Callback для результата (возвращает FirebaseUser при успехе)
     */
    public void register(String username, String email, String password, String gender, String phone, final Callback<FirebaseUser> callback) {
        Log.d(TAG, "register: Attempting to create user in Firebase Auth...");
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    Log.d(TAG, "register: Auth createUser task completed.");
                    if (task.isSuccessful() && task.getResult() != null) {
                        FirebaseUser firebaseUser = task.getResult().getUser();
                        Log.d(TAG, "register: Auth createUser SUCCESS, UID: " + (firebaseUser != null ? firebaseUser.getUid() : "null"));

                        if (firebaseUser != null) {
                            // 1. Обновить DisplayName в Firebase Auth
                            Log.d(TAG, "register: Attempting to update profile display name...");
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(username)
                                    .build();
                            firebaseUser.updateProfile(profileUpdates).addOnCompleteListener(profileTask -> {
                                Log.d(TAG, "register: Update profile display name task completed.");
                                if(profileTask.isSuccessful()) {
                                     Log.d(TAG, "register: Update display name SUCCESS.");
                                } else {
                                     Log.w(TAG, "register: Update display name FAILED.");
                                }
                                // Не ждем завершения обновления профиля для сохранения данных RTDB
                            });

                            // 2. Сохранить ОЧЕНЬ ПРОСТЫЕ данные профиля в Realtime Database (для теста)
                             Log.d(TAG, "register: Attempting to save VERY SIMPLE profile data to RTDB...");
                            Map<String, Object> profileData = new HashMap<>();
                            profileData.put("username", username);
                            profileData.put("email", email); // Дублируем email для удобства
                            
                            // Дополнительные проверки для телефона
                            if (phone != null && !phone.trim().isEmpty()) {
                                Log.d(TAG, "register: Adding valid phone number: '" + phone + "'");
                                profileData.put("phone", phone);
                            } else {
                                Log.w(TAG, "register: Phone is empty or null, not adding to profile data");
                            }
                            
                            profileData.put("gender", gender);
                            // Добавим метку успешной регистрации для обратной совместимости
                            profileData.put("registrationTest", "success");
                            
                            // Логируем данные профиля для отладки
                            Log.d(TAG, "register: Profile data to save: " + profileData.toString());
                            Log.d(TAG, "register: Phone value being saved: " + profileData.get("phone"));

                            // --- Логируем ТОЧНЫЙ ПУТЬ перед записью ---
                            DatabaseReference profileRef = databaseReference.child(USERS_NODE).child(firebaseUser.getUid()).child(PROFILE_NODE);
                            Log.d(TAG, "register: Attempting to setValue at path: " + profileRef.toString());

                            profileRef
                                    .setValue(profileData) // Используем настоящие данные профиля
                                    .addOnCompleteListener(dbTask -> {
                                        // --- Добавлен лог в НАЧАЛЕ колбэка --- 
                                        Log.d(TAG, "register: RTDB setValue -> INSIDE onComplete listener."); 
                                        Log.d(TAG, "register: RTDB setValue task completed.");
                                        if (dbTask.isSuccessful()) {
                                            Log.d(TAG, "register: RTDB setValue SUCCESS. Calling external onSuccess.");
                                            
                                            // Дополнительная проверка сохраненных данных
                                            verifyProfileDataSaved(firebaseUser.getUid(), profileData);
                                            
                                            callback.onSuccess(firebaseUser); // Успех после сохранения в RTDB
                                        } else {
                                            // Эта ветка может не вызваться, если сработает onFailureListener
                                            Log.e(TAG, "register: RTDB setValue FAILED (in onComplete).", dbTask.getException());
                                            callback.onError("Ошибка сохранения данных профиля: " + (dbTask.getException() != null ? dbTask.getException().getMessage() : "Unknown DB error in onComplete"));
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        // --- Добавлен лог в НАЧАЛЕ колбэка ---
                                        Log.e(TAG, "register: RTDB setValue -> INSIDE onFailure listener."); 
                                        // Явный обработчик ошибки записи в RTDB
                                        Log.e(TAG, "register: RTDB setValue FAILED (in onFailureListener).", e);
                                        callback.onError("Ошибка сохранения данных профиля (onFailure): " + e.getMessage());
                                    });
                        } else {
                             Log.e(TAG, "register: Auth success but firebaseUser is NULL!");
                             callback.onError("Ошибка регистрации: пользователь Firebase не найден после создания.");
                        }
                    } else {
                        Log.w(TAG, "register: Auth createUser FAILED.", task.getException());
                        callback.onError(task.getException() != null ? task.getException().getMessage() : "Ошибка регистрации Firebase");
                    }
                });
    }
    
    /**
     * Вход пользователя.
     * @param email Email
     * @param password Пароль
     * @param callback Callback для результата (возвращает FirebaseUser при успехе)
     */
    public void login(String email, String password, final Callback<FirebaseUser> callback) {
         firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Log.d(TAG, "Firebase signInWithEmail:success");
                        callback.onSuccess(task.getResult().getUser());
                    } else {
                        Log.w(TAG, "Firebase signInWithEmail:failure", task.getException());
                         callback.onError(task.getException() != null ? task.getException().getMessage() : "Ошибка входа Firebase");
                    }
                });
    }

    /**
     * Сбрасывает (удаляет) весь прогресс планировщика для текущего пользователя.
     * Этот метод вызывается ДО signOut.
     * @param callback Callback о завершении.
     */
    public void resetPlannerProgress(final Callback<Void> callback) {
        Log.d(TAG, "resetPlannerProgress: Initiated.");
        String userId = getCurrentUserId();
        if (userId == null) {
            // Эта ситуация не должна возникать, если вызывается до signOut,
            // но на всякий случай проверяем.
            Log.w(TAG, "resetPlannerProgress: User not authenticated (or ID is null), nothing to reset.");
            callback.onError("Пользователь не найден для сброса прогресса."); // Или onSuccess, если считать это не ошибкой
            return;
        }
        Log.d(TAG, "resetPlannerProgress: Removing node for user: " + userId);
        databaseReference.child(USERS_NODE).child(userId).child(PLANNER_PROGRESS_NODE)
                .removeValue()
                .addOnCompleteListener(task -> {
                    Log.d(TAG, "resetPlannerProgress: removeValue task completed. Successful: " + task.isSuccessful());
                    if (task.isSuccessful()) {
                        Log.d(TAG, "resetPlannerProgress: removeValue SUCCESS.");
                        callback.onSuccess(null);
                    } else {
                        Log.e(TAG, "resetPlannerProgress: removeValue FAILED.", task.getException());
                        callback.onError("Ошибка сброса прогресса: " + (task.getException() != null ? task.getException().getMessage() : "Unknown DB error"));
                    }
                })
                .addOnFailureListener(e -> {
                     Log.e(TAG, "resetPlannerProgress: removeValue EXCEPTION via onFailureListener.", e);
                     callback.onError("Ошибка сброса прогресса (onFailure): " + e.getMessage());
                });
    }

    /**
     * Выход пользователя из системы.
     * Сбрасывает прогресс планировщика ПЕРЕД фактическим выходом из Firebase.
     * @param callback Callback о завершении.
     */
     public void logout(final Callback<Void> callback) {
         Log.d(TAG, "logout: Initiated.");
         final String userId = getCurrentUserId(); 

         if (userId == null) {
             Log.w(TAG, "logout: User not authenticated at the start of logout. Proceeding with signOut only if needed.");
             // Если firebaseAuth.getCurrentUser() УЖЕ null, то signOut() можно не вызывать или он ничего не сделает.
             // Но для чистоты, если кто-то вызвал logout для уже вышедшего пользователя.
             if (firebaseAuth.getCurrentUser() != null) {
                 try {
                     firebaseAuth.signOut();
                     Log.d(TAG, "logout: Firebase signOut successful (user was already null or became null based on getCurrentUserId).");
                 } catch (Exception e) {
                     Log.e(TAG, "logout: Exception during signOut (user was initially null via getCurrentUserId).", e);
                     // Эта ошибка маловероятна, если signOut просто ничего не делает для null пользователя
                 }
             }
             callback.onSuccess(null); // Считаем выход успешным, т.к. пользователя и так не было
                    return;
                }
                
         Log.d(TAG, "logout: Attempting to reset planner progress for user: " + userId + " BEFORE signOut.");
         resetPlannerProgress(new Callback<Void>() {
             @Override
             public void onSuccess(Void result) {
                 Log.d(TAG, "logout: Planner progress reset successful for user: " + userId + ". Proceeding with signOut.");
                 try {
                     firebaseAuth.signOut();
                     Log.d(TAG, "logout: Firebase signOut successful (AFTER progress reset).");
                     callback.onSuccess(null); 
                 } catch (Exception e) {
                     Log.e(TAG, "logout: Exception during signOut (AFTER progress reset).", e);
                     callback.onError("Прогресс сброшен, но ошибка при выходе из Firebase: " + e.getMessage());
                 }
             }

             @Override
             public void onError(String message) {
                 Log.e(TAG, "logout: Failed to reset planner progress for user: " + userId + ". Message: " + message);
                 // Несмотря на ошибку сброса прогресса, все равно пытаемся выйти из системы.
                 // Можно изменить эту логику, если требуется другое поведение (например, не выходить, если прогресс не сброшен).
                 Log.w(TAG, "logout: Proceeding with signOut DESPITE planner progress reset failure.");
                 try {
                     firebaseAuth.signOut();
                     Log.d(TAG, "logout: Firebase signOut successful (DESPITE progress reset failure).");
                     // Сообщаем об ошибке сброса прогресса, но указываем, что выход выполнен.
                     callback.onError("Ошибка сброса прогресса (" + message + "), но выход из Firebase выполнен.");
                 } catch (Exception e) {
                     Log.e(TAG, "logout: Exception during signOut (AFTER progress reset failure).", e);
                     callback.onError("Ошибка сброса прогресса (" + message + ") И ошибка при выходе из Firebase: " + e.getMessage());
                 }
             }
         });
     }

     /**
      * Сохраняет/Обновляет данные профиля пользователя в RTDB.
      * @param profileData Карта с данными профиля для сохранения (ключ=поле, значение=значение).
      * @param callback Callback о завершении.
      */
      public void saveUserProfileData(Map<String, Object> profileData, final Callback<Void> callback) {
          String userId = getCurrentUserId();
          if (userId == null) {
              callback.onError("Пользователь не аутентифицирован.");
              return;
          }
          databaseReference.child(USERS_NODE).child(userId).child(PROFILE_NODE)
                  .updateChildren(profileData) // Используем updateChildren для слияния данных
                  .addOnCompleteListener(task -> {
                      if (task.isSuccessful()) {
                          Log.d(TAG, "User profile data updated in RTDB.");
                          callback.onSuccess(null);
                      } else {
                          Log.e(TAG, "Failed to update user profile data in RTDB.", task.getException());
                          callback.onError("Ошибка обновления профиля: " + (task.getException() != null ? task.getException().getMessage() : "Unknown DB error"));
                      }
                  });
      }

     /**
      * Загружает данные профиля пользователя из RTDB.
      * @param callback Callback с результатом (Карта с данными профиля или null).
      */
      public void getUserProfile(final Callback<Map<String, Object>> callback) {
          String userId = getCurrentUserId();
          if (userId == null) {
              callback.onError("Пользователь не аутентифицирован.");
              return;
          }
          Log.d(TAG, "getUserProfile: Fetching profile data for userId: " + userId);
          databaseReference.child(USERS_NODE).child(userId).child(PROFILE_NODE)
                  .addListenerForSingleValueEvent(new ValueEventListener() {
                      @Override
                      public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                          Log.d(TAG, "getUserProfile.onDataChange: Snapshot exists: " + dataSnapshot.exists());
                          if (dataSnapshot.exists()) {
                              try {
                                  @SuppressWarnings("unchecked") // Безопасно, т.к. мы ожидаем Map
                                  Map<String, Object> profileData = (Map<String, Object>) dataSnapshot.getValue();
                                  Log.d(TAG, "getUserProfile: Profile data fetched successfully: " + (profileData != null ? profileData.toString() : "null"));
                                  // Проверяем наличие ключевых полей
                                  if (profileData != null) {
                                      Log.d(TAG, "getUserProfile: Profile data contains phone: " + profileData.containsKey("phone"));
                                      if (profileData.containsKey("phone")) {
                                          Log.d(TAG, "getUserProfile: Phone value: " + profileData.get("phone"));
                                      }
                                  }
                                  Log.d(TAG, "getUserProfile: User profile data fetched from RTDB.");
                                  callback.onSuccess(profileData);
                              } catch (Exception e) {
                                   Log.e(TAG, "getUserProfile: Error parsing profile data", e);
                                   callback.onError("Ошибка чтения данных профиля.");
                              }
                    } else {
                              Log.w(TAG, "getUserProfile: User profile node does not exist in RTDB for UID: " + userId);
                              callback.onSuccess(null); // Узел не найден, возвращаем null
                          }
                      }

                      @Override
                      public void onCancelled(@NonNull DatabaseError databaseError) {
                          Log.e(TAG, "getUserProfile: Failed to fetch user profile data from RTDB.", databaseError.toException());
                          callback.onError("Ошибка загрузки профиля: " + databaseError.getMessage());
                      }
                  });
      }

     /**
      * Сохраняет прогресс (ответ и статус завершения) для конкретного шага планировщика.
      * @param phaseId ID фазы (например, "phase1")
      * @param stageId ID этапа (например, "stage1")
      * @param stepId ID шага (например, "step1")
      * @param answer Ответ пользователя (может быть String, Map, и т.д.)
      * @param callback Callback о завершении.
      */
     public void saveStepProgress(String phaseId, String stageId, String stepId, Object answer, final Callback<Void> callback) {
         String userId = getCurrentUserId();
         if (userId == null) {
             callback.onError("Пользователь не аутентифицирован.");
             return;
         }

         DatabaseReference stepRef = databaseReference.child(USERS_NODE).child(userId)
                 .child(PLANNER_PROGRESS_NODE).child(phaseId).child(stageId).child(stepId);

         Map<String, Object> stepData = new HashMap<>();
         stepData.put("completed", true);
         stepData.put("answer", answer); // Сохраняем ответ

         stepRef.setValue(stepData).addOnCompleteListener(task -> {
             if (task.isSuccessful()) {
                 Log.d(TAG, "Step progress saved: " + phaseId + "/" + stageId + "/" + stepId);
                    callback.onSuccess(null);
                } else {
                 Log.e(TAG, "Failed to save step progress.", task.getException());
                 callback.onError("Ошибка сохранения прогресса шага: " + (task.getException() != null ? task.getException().getMessage() : "Unknown DB error"));
            }
        });
    }
    
    /**
     * Загружает весь прогресс планировщика для текущего пользователя.
     * @param callback Callback с результатом (Карта со всем прогрессом или null).
     */
     public void getPlannerProgress(final Callback<Map<String, Object>> callback) {
         String userId = getCurrentUserId();
         if (userId == null) {
             callback.onError("Пользователь не аутентифицирован.");
             return;
         }
         databaseReference.child(USERS_NODE).child(userId).child(PLANNER_PROGRESS_NODE)
                 .addListenerForSingleValueEvent(new ValueEventListener() {
                      @Override
                      public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                          if (dataSnapshot.exists()) {
                               try {
                                  @SuppressWarnings("unchecked")
                                  Map<String, Object> progressData = (Map<String, Object>) dataSnapshot.getValue();
                                  Log.d(TAG, "Planner progress fetched from RTDB.");
                                  callback.onSuccess(progressData);
                               } catch (Exception e) {
                                   Log.e(TAG, "Error parsing planner progress data", e);
                                   callback.onError("Ошибка чтения прогресса.");
                               }
                } else {
                              Log.w(TAG, "Planner progress node does not exist for UID: " + userId);
                              callback.onSuccess(null); // Нет сохраненного прогресса
                          }
                      }

                      @Override
                      public void onCancelled(@NonNull DatabaseError databaseError) {
                          Log.e(TAG, "Failed to fetch planner progress from RTDB.", databaseError.toException());
                          callback.onError("Ошибка загрузки прогресса: " + databaseError.getMessage());
            }
        });
    }

    /**
     * Сохраняет/Обновляет данные базовой анкеты пользователя в RTDB.
     * @param questionnaireData Карта с данными анкеты для сохранения (ключ=поле, значение=значение).
     * @param callback Callback о завершении.
     */
    public void saveBasicQuestionnaireData(Map<String, Object> questionnaireData, final Callback<Void> callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onError("Пользователь не аутентифицирован для сохранения анкеты.");
            return;
        }
        Log.d(TAG, "saveBasicQuestionnaireData: Attempting to save data for user: " + userId);
        DatabaseReference questionnaireRef = databaseReference.child(USERS_NODE).child(userId).child(BASIC_QUESTIONNAIRE_NODE); // Новый узел

        questionnaireRef.setValue(questionnaireData)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "saveBasicQuestionnaireData: Data saved successfully for user: " + userId);
                    callback.onSuccess(null);
                } else {
                    Log.e(TAG, "saveBasicQuestionnaireData: Failed to save data for user: " + userId, task.getException());
                    callback.onError("Ошибка сохранения данных анкеты: " + (task.getException() != null ? task.getException().getMessage() : "Unknown DB error"));
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "saveBasicQuestionnaireData: Exception while saving data for user: " + userId, e);
                callback.onError("Исключение при сохранении данных анкеты: " + e.getMessage());
            });
    }

    public void getBasicQuestionnaireData(final Callback<Map<String, Object>> callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onError("Пользователь не аутентифицирован для загрузки анкеты.");
            return;
        }
        Log.d(TAG, "getBasicQuestionnaireData: Attempting to fetch data for user: " + userId);
        databaseReference.child(USERS_NODE).child(userId).child(BASIC_QUESTIONNAIRE_NODE)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        try {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> questionnaireData = (Map<String, Object>) dataSnapshot.getValue();
                            Log.d(TAG, "getBasicQuestionnaireData: Data fetched successfully for user: " + userId);
                            callback.onSuccess(questionnaireData);
                        } catch (Exception e) {
                            Log.e(TAG, "getBasicQuestionnaireData: Error parsing data for user: " + userId, e);
                            callback.onError("Ошибка парсинга данных анкеты.");
                        }
                    } else {
                        Log.w(TAG, "getBasicQuestionnaireData: No data found for user: " + userId);
                        callback.onSuccess(null); // Нет данных, но не ошибка
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "getBasicQuestionnaireData: Failed to fetch data for user: " + userId, databaseError.toException());
                    callback.onError("Ошибка загрузки данных анкеты: " + databaseError.getMessage());
            }
        });
    }
    
    /**
     * Сохраняет данные анкеты планировщика для текущего пользователя.
     * @param plannerData Карта с данными анкеты планера.
     * @param callback Callback о завершении.
     */
    public void savePlannerQuestionnaireData(Map<String, Object> plannerData, final Callback<Void> callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            Log.e(TAG, "savePlannerQuestionnaireData: Ошибка - пользователь не аутентифицирован");
            callback.onError("Пользователь не аутентифицирован для сохранения анкеты планера.");
            return;
        }
        
        // Проверка содержимого данных
        if (plannerData == null || plannerData.isEmpty()) {
            Log.e(TAG, "savePlannerQuestionnaireData: Ошибка - переданные данные пусты");
            callback.onError("Нельзя сохранить пустые данные анкеты планера.");
            return;
        }
        
        // Добавляем метку последнего обновления на всякий случай
        if (!plannerData.containsKey("updated_at")) {
            plannerData.put("updated_at", System.currentTimeMillis());
        }
        
        Log.d(TAG, "savePlannerQuestionnaireData: Пытаемся сохранить данные анкеты планера для пользователя: " + userId);
        Log.d(TAG, "savePlannerQuestionnaireData: Размер данных для сохранения: " + plannerData.size());
        
        DatabaseReference plannerRef = databaseReference.child(USERS_NODE).child(userId).child(PLANNER_QUESTIONNAIRE_NODE);
        
        // Логируем полный путь для отладки
        Log.d(TAG, "savePlannerQuestionnaireData: Полный путь для сохранения: " + plannerRef.toString());

        plannerRef.setValue(plannerData)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "savePlannerQuestionnaireData: Данные анкеты планера успешно сохранены для пользователя: " + userId);
                callback.onSuccess(null);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "savePlannerQuestionnaireData: Ошибка при сохранении данных анкеты планера: ", e);
                callback.onError("Ошибка сохранения анкеты планера: " + e.getMessage());
            })
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "savePlannerQuestionnaireData: Задача сохранения завершена успешно");
                } else {
                    Log.e(TAG, "savePlannerQuestionnaireData: Задача сохранения завершена с ошибкой", task.getException());
                }
            });
    }

    /**
     * Проверяет, существуют ли данные анкеты планировщика для текущего пользователя.
     * @param callback Callback с результатом (true, если данные есть, false иначе).
     */
    public void checkPlannerQuestionnaireExists(final Callback<Boolean> callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onError("Пользователь не аутентифицирован для проверки анкеты планера.");
            return;
        }
        DatabaseReference plannerRef = databaseReference.child(USERS_NODE).child(userId).child(PLANNER_QUESTIONNAIRE_NODE);

        plannerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                callback.onSuccess(dataSnapshot.exists()); // true если узел существует, false если нет
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "checkPlannerQuestionnaireExists: Failed to check data for user: " + userId, databaseError.toException());
                callback.onError("Ошибка проверки данных анкеты планера: " + databaseError.getMessage());
            }
        });
    }
    
    /**
      * Загружает данные анкеты планировщика (если они понадобятся для предзаполнения или просмотра).
      * @param callback Callback с результатом (Карта с данными или null).
      */
     public void getPlannerQuestionnaireData(final Callback<Map<String, Object>> callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onError("Пользователь не аутентифицирован для загрузки анкеты планера.");
            return;
        }
        DatabaseReference plannerRef = databaseReference.child(USERS_NODE).child(userId).child(PLANNER_QUESTIONNAIRE_NODE);
        plannerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    try {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> plannerData = (Map<String, Object>) dataSnapshot.getValue();
                        callback.onSuccess(plannerData);
                    } catch (Exception e) {
                         Log.e(TAG, "Error parsing planner questionnaire data", e);
                         callback.onError("Ошибка чтения данных анкеты планера.");
                    }
                } else {
                    callback.onSuccess(null); // Узел не найден
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to fetch planner questionnaire data", databaseError.toException());
                callback.onError("Ошибка загрузки анкеты планера: " + databaseError.getMessage());
            }
        });
     }

    /**
     * Вспомогательный метод для проверки сохраненных данных профиля.
     * @param userId ID пользователя
     * @param expectedData Ожидаемые данные
     */
    private void verifyProfileDataSaved(String userId, Map<String, Object> expectedData) {
        Log.d(TAG, "verifyProfileDataSaved: Verifying saved profile data...");
        databaseReference.child(USERS_NODE).child(userId).child(PROFILE_NODE)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            try {
                                @SuppressWarnings("unchecked")
                                Map<String, Object> savedData = (Map<String, Object>) dataSnapshot.getValue();
                                if (savedData != null) {
                                    Log.d(TAG, "verifyProfileDataSaved: Saved data: " + savedData.toString());
                                    // Проверяем наличие телефона
                                    if (savedData.containsKey("phone")) {
                                        Log.d(TAG, "verifyProfileDataSaved: Phone found: " + savedData.get("phone"));
                                    } else {
                                        Log.w(TAG, "verifyProfileDataSaved: Phone field MISSING!");
                                    }
                                    // Проверяем соответствие с ожидаемыми данными
                                    boolean allMatched = true;
                                    for (String key : expectedData.keySet()) {
                                        if (!savedData.containsKey(key) || !expectedData.get(key).equals(savedData.get(key))) {
                                            Log.w(TAG, "verifyProfileDataSaved: Mismatch for key '" + key + 
                                                    "', expected: " + expectedData.get(key) + 
                                                    ", actual: " + (savedData.containsKey(key) ? savedData.get(key) : "MISSING"));
                                            allMatched = false;
                                        }
                                    }
                                    if (allMatched) {
                                        Log.d(TAG, "verifyProfileDataSaved: All data matched as expected!");
                                    } else {
                                        Log.w(TAG, "verifyProfileDataSaved: Some data did not match!");
                                    }
                                } else {
                                    Log.w(TAG, "verifyProfileDataSaved: Saved data is NULL!");
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "verifyProfileDataSaved: Error parsing saved data", e);
                            }
                        } else {
                            Log.w(TAG, "verifyProfileDataSaved: Node does not exist!");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e(TAG, "verifyProfileDataSaved: Error verifying data", databaseError.toException());
                    }
                });
    }

    /**
     * Принудительно обновляет номер телефона в профиле пользователя.
     * @param phone Номер телефона
     * @param callback Callback о завершении
     */
    public void forceUpdateUserPhone(String phone, final Callback<Void> callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            Log.e(TAG, "forceUpdateUserPhone: No user ID available");
            callback.onError("Пользователь не аутентифицирован.");
            return;
        }
        
        Log.d(TAG, "forceUpdateUserPhone: Forcing phone update for user: " + userId + ", phone: " + phone);
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("phone", phone);
        
        DatabaseReference profileRef = databaseReference.child(USERS_NODE).child(userId).child(PROFILE_NODE);
        profileRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "forceUpdateUserPhone: Phone successfully updated for user: " + userId);
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "forceUpdateUserPhone: Failed to update phone for user: " + userId, e);
                    callback.onError("Ошибка обновления телефона: " + e.getMessage());
                });
    }
} 