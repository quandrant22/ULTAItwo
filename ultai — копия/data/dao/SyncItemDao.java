package com.example.ultai.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.ultai.data.entities.SyncItem;

import java.util.List;

/**
 * DAO для работы с элементами синхронизации в базе данных
 */
@Dao
public interface SyncItemDao {
    
    /**
     * Получить элемент синхронизации по ID
     */
    @Query("SELECT * FROM sync_items WHERE id = :id")
    SyncItem getSyncItemById(int id);
    
    /**
     * Получить элементы синхронизации по типу
     */
    @Query("SELECT * FROM sync_items WHERE type = :type")
    List<SyncItem> getSyncItemsByType(String type);
    
    /**
     * Получить несинхронизированные элементы
     */
    @Query("SELECT * FROM sync_items WHERE synced = 0")
    List<SyncItem> getUnsyncedItems();
    
    /**
     * Получить все элементы синхронизации
     */
    @Query("SELECT * FROM sync_items")
    List<SyncItem> getAllSyncItems();
    
    /**
     * Добавить новый элемент синхронизации
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertSyncItem(SyncItem syncItem);
    
    /**
     * Обновить существующий элемент синхронизации
     */
    @Update
    void updateSyncItem(SyncItem syncItem);
    
    /**
     * Удалить элемент синхронизации
     */
    @Delete
    void deleteSyncItem(SyncItem syncItem);
    
    /**
     * Удалить все синхронизированные элементы
     */
    @Query("DELETE FROM sync_items WHERE synced = 1")
    void deleteSyncedItems();
} 