package com.example.ultai.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.ultai.data.entities.QuestionnaireEntity;

import java.util.List;

/**
 * DAO для работы с анкетами в базе данных
 */
@Dao
public interface QuestionnaireDao {
    
    /**
     * Получить анкету по ID
     */
    @Query("SELECT * FROM questionnaires WHERE id = :id")
    QuestionnaireEntity getQuestionnaireById(int id);
    
    /**
     * Получить анкету по ID пользователя
     */
    @Query("SELECT * FROM questionnaires WHERE userId = :userId")
    QuestionnaireEntity getQuestionnaireByUserId(int userId);
    
    /**
     * Получить все анкеты
     */
    @Query("SELECT * FROM questionnaires")
    List<QuestionnaireEntity> getAllQuestionnaires();
    
    /**
     * Добавить новую анкету
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertQuestionnaire(QuestionnaireEntity questionnaire);
    
    /**
     * Обновить существующую анкету
     */
    @Update
    void updateQuestionnaire(QuestionnaireEntity questionnaire);
    
    /**
     * Удалить анкету
     */
    @Delete
    void deleteQuestionnaire(QuestionnaireEntity questionnaire);
    
    /**
     * Удалить анкету по ID пользователя
     */
    @Query("DELETE FROM questionnaires WHERE userId = :userId")
    void deleteQuestionnaireByUserId(int userId);
} 