package com.example.ultai.data.converters;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

/**
 * Конвертер для преобразования массивов строк в строку и обратно для хранения в базе данных Room
 */
public class StringArrayConverter {
    
    private static final Gson gson = new Gson();
    
    /**
     * Преобразует массив строк в строку JSON для хранения в базе данных
     */
    @TypeConverter
    public static String fromStringArray(String[] array) {
        if (array == null) {
            return null;
        }
        return gson.toJson(array);
    }
    
    /**
     * Преобразует строку JSON из базы данных обратно в массив строк
     */
    @TypeConverter
    public static String[] toStringArray(String json) {
        if (json == null) {
            return null;
        }
        Type type = new TypeToken<String[]>() {}.getType();
        return gson.fromJson(json, type);
    }
} 