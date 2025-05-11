package com.example.ultai.data.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Сущность для отслеживания элементов, требующих синхронизации с внешним сервером
 */
@Entity(tableName = "sync_items")
public class SyncItem {
    
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    // Тип элемента (user, profile, questionnaire)
    private String type;
    
    // ID элемента в локальной базе данных
    private int itemId;
    
    // JSON-представление данных для синхронизации
    private String jsonData;
    
    // Флаг синхронизации
    private boolean synced;
    
    // Время создания записи
    private long createdAt;
    
    // Время последней попытки синхронизации
    private long lastSyncAttempt;
    
    // Количество попыток синхронизации
    private int syncAttempts;
    
    // Конструктор по умолчанию
    public SyncItem() {
        this.createdAt = System.currentTimeMillis();
        this.synced = false;
        this.syncAttempts = 0;
    }
    
    // Конструктор с параметрами
    public SyncItem(String type, int itemId, String jsonData) {
        this.type = type;
        this.itemId = itemId;
        this.jsonData = jsonData;
        this.createdAt = System.currentTimeMillis();
        this.synced = false;
        this.syncAttempts = 0;
    }
    
    // Геттеры и сеттеры
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public int getItemId() {
        return itemId;
    }
    
    public void setItemId(int itemId) {
        this.itemId = itemId;
    }
    
    public String getJsonData() {
        return jsonData;
    }
    
    public void setJsonData(String jsonData) {
        this.jsonData = jsonData;
    }
    
    public boolean isSynced() {
        return synced;
    }
    
    public void setSynced(boolean synced) {
        this.synced = synced;
    }
    
    public long getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
    
    public long getLastSyncAttempt() {
        return lastSyncAttempt;
    }
    
    public void setLastSyncAttempt(long lastSyncAttempt) {
        this.lastSyncAttempt = lastSyncAttempt;
    }
    
    public int getSyncAttempts() {
        return syncAttempts;
    }
    
    public void setSyncAttempts(int syncAttempts) {
        this.syncAttempts = syncAttempts;
    }
    
    /**
     * Увеличить счетчик попыток синхронизации
     */
    public void incrementSyncAttempts() {
        this.syncAttempts++;
        this.lastSyncAttempt = System.currentTimeMillis();
    }
} 