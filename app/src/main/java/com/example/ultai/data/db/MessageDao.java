package com.example.ultai.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.ultai.data.model.Message;
import java.util.List;

@Dao
public interface MessageDao {
    @Query("SELECT * FROM messages ORDER BY timestamp ASC")
    LiveData<List<Message>> getAllMessages();

    @Insert
    void insertMessage(Message message);

    @Query("DELETE FROM messages")
    void deleteAllMessages();
} 