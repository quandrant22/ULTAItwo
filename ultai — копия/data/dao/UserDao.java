package com.example.ultai.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.ultai.data.entities.UserEntity;

import java.util.List;

/**
 * DAO для работы с пользователями в базе данных
 */
@Dao
public interface UserDao {
    
    /**
     * Получить пользователя по ID
     */
    @Query("SELECT * FROM users WHERE id = :id")
    UserEntity getUserById(int id);
    
    /**
     * Получить пользователя по email
     */
    @Query("SELECT * FROM users WHERE email = :email")
    UserEntity getUserByEmail(String email);
    
    /**
     * Получить все записи пользователей
     */
    @Query("SELECT * FROM users")
    List<UserEntity> getAllUsers();
    
    /**
     * Добавить нового пользователя
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertUser(UserEntity user);
    
    /**
     * Обновить существующего пользователя
     */
    @Update
    void updateUser(UserEntity user);
    
    /**
     * Удалить пользователя
     */
    @Delete
    void deleteUser(UserEntity user);
    
    /**
     * Проверить авторизацию пользователя
     */
    @Query("SELECT * FROM users WHERE email = :email AND password = :password")
    UserEntity login(String email, String password);
} 