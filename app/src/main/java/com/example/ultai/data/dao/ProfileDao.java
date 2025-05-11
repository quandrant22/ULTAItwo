package com.example.ultai.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.ultai.data.entities.ProfileEntity;

import java.util.List;

/**
 * DAO для работы с профилями пользователей в базе данных
 */
@Dao
public interface ProfileDao {
    
    /**
     * Получить профиль пользователя по ID
     */
    @Query("SELECT * FROM profiles WHERE userId = :userId")
    ProfileEntity getProfileByUserId(int userId);
    
    /**
     * Получить все профили пользователей
     */
    @Query("SELECT * FROM profiles")
    List<ProfileEntity> getAllProfiles();
    
    /**
     * Добавить новый профиль
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertProfile(ProfileEntity profile);
    
    /**
     * Обновить существующий профиль
     */
    @Update
    void updateProfile(ProfileEntity profile);
    
    /**
     * Удалить профиль
     */
    @Delete
    void deleteProfile(ProfileEntity profile);
    
    /**
     * Удалить профиль по ID пользователя
     */
    @Query("DELETE FROM profiles WHERE userId = :userId")
    void deleteProfileByUserId(int userId);
} 