package com.example.ultai.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.ultai.data.converters.StringArrayConverter;
import com.example.ultai.data.dao.ProfileDao;
import com.example.ultai.data.dao.QuestionnaireDao;
import com.example.ultai.data.dao.SyncItemDao;
import com.example.ultai.data.dao.UserDao;
import com.example.ultai.data.entities.ProfileEntity;
import com.example.ultai.data.entities.QuestionnaireEntity;
import com.example.ultai.data.entities.SyncItem;
import com.example.ultai.data.entities.UserEntity;

/**
 * Локальная база данных для хранения данных приложения.
 * Обеспечивает постоянное хранение и доступ к данным даже при отсутствии сети.
 */
@Database(
    entities = {
        UserEntity.class,
        ProfileEntity.class, 
        QuestionnaireEntity.class,
        SyncItem.class
    }, 
    version = 1,
    exportSchema = false
)
@TypeConverters({StringArrayConverter.class})
public abstract class LocalDatabase extends RoomDatabase {
    
    private static final String DATABASE_NAME = "ultai_db";
    private static LocalDatabase instance;
    
    // DAO для работы с данными
    public abstract UserDao userDao();
    public abstract ProfileDao profileDao();
    public abstract QuestionnaireDao questionnaireDao();
    public abstract SyncItemDao syncItemDao();
    
    /**
     * Получение экземпляра базы данных (Singleton)
     */
    public static synchronized LocalDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    LocalDatabase.class,
                    DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
} 