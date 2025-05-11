package com.example.ultai.news.newsapp.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class NewsItem {
    private String title; // Заголовок новости
    private String description; // Описание новости
    private String urlToImage; // URL изображения
    private Source source; // Источник новости
    private String translatedDescriptionRu; // Переведенное описание на русский
    private String translatedDescriptionJp; // Переведенное описание на японский
    private String originalDescription; // Оригинальное описание
    private Date publishedAt; // Дата публикации новости
    
    // Конструктор по умолчанию
    public NewsItem() {
    }
    
    // Конструктор с параметрами
    public NewsItem(String title, String description, String url, Source source, Date timestamp, String urlToImage) {
        this.title = title;
        this.description = description;
        this.urlToImage = urlToImage;
        this.source = source;
        this.publishedAt = timestamp;
    }

    // Геттеры и сеттеры
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrlToImage() {
        return urlToImage;
    }

    public void setUrlToImage(String urlToImage) {
        this.urlToImage = urlToImage;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public String getTranslatedDescriptionRu() {
        return translatedDescriptionRu;
    }

    public void setTranslatedDescriptionRu(String translatedDescriptionRu) {
        this.translatedDescriptionRu = translatedDescriptionRu;
    }

    public String getTranslatedDescriptionJp() {
        return translatedDescriptionJp;
    }

    public void setTranslatedDescriptionJp(String translatedDescriptionJp) {
        this.translatedDescriptionJp = translatedDescriptionJp;
    }

    public String getOriginalDescription() {
        return originalDescription;
    }

    public void setOriginalDescription(String originalDescription) {
        this.originalDescription = originalDescription;
    }
    
    public Date getPublishedAt() {
        return publishedAt;
    }
    
    public void setPublishedAt(Date publishedAt) {
        this.publishedAt = publishedAt;
    }
    
    /**
     * Возвращает отформатированное время публикации новости
     * @return Строка с датой и временем в формате "дд.ММ.гггг ЧЧ:мм"
     */
    public String getFormattedTime() {
        if (publishedAt == null) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
        return sdf.format(publishedAt);
    }
    
    /**
     * Возвращает относительное время публикации новости
     * @return Строка вида "5 минут назад", "2 часа назад" и т.д.
     */
    public String getRelativeTime() {
        if (publishedAt == null) {
            return "";
        }
        
        long currentTime = System.currentTimeMillis();
        long publicationTime = publishedAt.getTime();
        long diff = currentTime - publicationTime;
        
        // Перевод разницы в минуты, часы и дни
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
        long hours = TimeUnit.MILLISECONDS.toHours(diff);
        long days = TimeUnit.MILLISECONDS.toDays(diff);
        
        if (minutes < 60) {
            return getMinutesString(minutes);
        } else if (hours < 24) {
            return getHoursString(hours);
        } else {
            return getDaysString(days);
        }
    }
    
    /**
     * Возвращает строку с относительным временем в минутах
     */
    private String getMinutesString(long minutes) {
        if (minutes <= 0) {
            return "только что";
        } else if (minutes == 1) {
            return "1 минуту назад";
        } else if (minutes >= 2 && minutes <= 4) {
            return minutes + " минуты назад";
        } else {
            return minutes + " минут назад";
        }
    }
    
    /**
     * Возвращает строку с относительным временем в часах
     */
    private String getHoursString(long hours) {
        if (hours == 1) {
            return "1 час назад";
        } else if (hours >= 2 && hours <= 4) {
            return hours + " часа назад";
        } else {
            return hours + " часов назад";
        }
    }
    
    /**
     * Возвращает строку с относительным временем в днях
     */
    private String getDaysString(long days) {
        if (days == 1) {
            return "1 день назад";
        } else if (days >= 2 && days <= 4) {
            return days + " дня назад";
        } else if (days <= 30) {
            return days + " дней назад";
        } else {
            return getFormattedTime();
        }
    }

    public static class Source {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}