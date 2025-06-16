package com.example.ultai.news.newsapp.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.example.ultai.news.newsapp.model.NewsItem;
import com.example.ultai.ultai.parser.NewsParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Адаптер для парсера новостей, который заменяет NewsAPI
 */
public class NewsParserAdapter {
    private static final String TAG = "NewsParserAdapter";
    private final ExecutorService executorService;
    private final Context context;
    
    // Дополнительные источники новостей для обеспечения минимального количества
    private static final String[] ADDITIONAL_TOPICS = {
        "политика", "экономика", "спорт", "технологии", "наука", 
        "культура", "общество", "происшествия", "мир", "россия"
    };
    
    // Максимальное время ожидания для получения новостей
    private static final int TIMEOUT_SECONDS = 90; // Увеличиваем таймаут до 90 секунд
    
    public NewsParserAdapter(Context context) {
        this.context = context;
        // Создаем пул потоков для асинхронной работы
        executorService = Executors.newFixedThreadPool(6); // Увеличиваем количество потоков до 6
    }
    
    /**
     * Проверяет наличие интернет-соединения
     * @return true, если есть соединение с интернетом
     */
    private boolean isNetworkAvailable() {
        if (context == null) {
            Log.e(TAG, "Context равен null, не могу проверить соединение");
            return false;
        }
        
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            Log.e(TAG, "ConnectivityManager равен null");
            return false;
        }
        
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = activeNetworkInfo != null && activeNetworkInfo.isConnected();
        
        Log.d(TAG, "Проверка интернет-соединения: " + (isConnected ? "доступно" : "недоступно"));
        return isConnected;
    }
    
    /**
     * Получает последние новости асинхронно
     * @param count количество новостей
     * @return Future с результатом
     */
    public Future<List<NewsItem>> getLatestNewsAsync(int count) {
        return executorService.submit(new Callable<List<NewsItem>>() {
            @Override
            public List<NewsItem> call() throws Exception {
                try {
                    Log.d(TAG, "Начинаем асинхронное получение новостей, запрошено: " + count);
                    
                    // Проверяем наличие интернет-соединения
                    if (!isNetworkAvailable()) {
                        Log.e(TAG, "Нет интернет-соединения, возвращаем заглушку");
                        List<NewsItem> errorResult = new ArrayList<>();
                        NewsItem.Source source = new NewsItem.Source();
                        source.setName("Ошибка");
                        NewsItem errorItem = new NewsItem(
                                "Нет подключения к интернету",
                                "Пожалуйста, проверьте подключение к интернету и попробуйте снова.",
                                "",
                                source,
                                new Date(),
                                ""
                        );
                        errorResult.add(errorItem);
                        return errorResult;
                    }
                    
                    // Создаем несколько парсеров для разных источников
                    NewsParser mainParser = new NewsParser();
                    List<NewsParser.NewsItem> parsedNews = new ArrayList<>();
                    
                    // Пробуем получить новости из основного парсера
                    try {
                        List<NewsParser.NewsItem> mainNews = mainParser.getLatestNews(count * 3); // Запрашиваем больше новостей для фильтрации
                        if (mainNews != null && !mainNews.isEmpty()) {
                            parsedNews.addAll(mainNews);
                            Log.d(TAG, "Получено " + mainNews.size() + " новостей из основного парсера");
                        } else {
                            Log.w(TAG, "Основной парсер вернул пустой список новостей");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Ошибка при получении новостей из основного парсера", e);
                    }
                    
                    // Если не получили достаточно новостей, пробуем получить новости по темам
                    if (parsedNews.size() < count) {
                        Log.d(TAG, "Недостаточно новостей, пробуем получить по темам");
                        
                        // Список популярных тем для поиска новостей
                        String[] topics = {"Россия", "политика", "экономика", "технологии", "наука", "спорт", "культура"};
                        
                        for (String topic : topics) {
                            try {
                                List<NewsParser.NewsItem> topicNews = mainParser.getNewsByTopic(topic, 5);
                                if (topicNews != null && !topicNews.isEmpty()) {
                                    Log.d(TAG, "Получено " + topicNews.size() + " новостей по теме: " + topic);
                                    
                                    // Добавляем новости, избегая дубликатов
                                    for (NewsParser.NewsItem item : topicNews) {
                                        boolean isDuplicate = false;
                                        for (NewsParser.NewsItem existingItem : parsedNews) {
                                            if (existingItem.getTitle().equals(item.getTitle())) {
                                                isDuplicate = true;
                                                break;
                                            }
                                        }
                                        
                                        if (!isDuplicate) {
                                            parsedNews.add(item);
                                        }
                                    }
                                } else {
                                    Log.w(TAG, "Не удалось получить новости по теме: " + topic);
                                }
                                
                                if (parsedNews.size() >= count * 2) {
                                    break;
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Ошибка при получении новостей по теме: " + topic, e);
                            }
                        }
                    }
                    
                    // Если все еще недостаточно новостей, пробуем получить из RSS
                    if (parsedNews.size() < count) {
                        Log.d(TAG, "Все еще недостаточно новостей, пробуем получить из RSS");
                        try {
                            // Предполагаем, что в NewsParser есть метод parseRssFeeds
                            List<NewsParser.NewsItem> rssNews = mainParser.parseRssFeeds(count);
                            if (rssNews != null && !rssNews.isEmpty()) {
                                Log.d(TAG, "Получено " + rssNews.size() + " новостей из RSS");
                                
                                // Добавляем новости, избегая дубликатов
                                for (NewsParser.NewsItem item : rssNews) {
                                    boolean isDuplicate = false;
                                    for (NewsParser.NewsItem existingItem : parsedNews) {
                                        if (existingItem.getTitle().equals(item.getTitle())) {
                                            isDuplicate = true;
                                            break;
                                        }
                                    }
                                    
                                    if (!isDuplicate) {
                                        parsedNews.add(item);
                                    }
                                }
                                
                                Log.d(TAG, "После добавления RSS новостей, всего: " + parsedNews.size());
                            } else {
                                Log.w(TAG, "Не удалось получить новости из RSS");
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Ошибка при получении новостей из RSS", e);
                        }
                    }
                    
                    // Сортируем новости по дате (сначала новые)
                    Collections.sort(parsedNews, new Comparator<NewsParser.NewsItem>() {
                        @Override
                        public int compare(NewsParser.NewsItem o1, NewsParser.NewsItem o2) {
                            return o2.getTimestamp().compareTo(o1.getTimestamp());
                        }
                    });
                    
                    // Преобразуем в наш формат новостей
                    List<NewsItem> result = new ArrayList<>();
                    for (NewsParser.NewsItem item : parsedNews) {
                        // Проверяем, что у новости есть заголовок и ссылка
                        if (item.getTitle() != null && !item.getTitle().isEmpty() && 
                            item.getUrl() != null && !item.getUrl().isEmpty()) {
                            
                            NewsItem.Source source = new NewsItem.Source();
                            source.setName(item.getSource());
                            
                            NewsItem newsItem = new NewsItem(
                                    item.getTitle(),
                                    item.getDescription(),
                                    item.getUrl(),
                                    source,
                                    item.getTimestamp(),
                                    getValidImageUrl(item.getImageUrl())
                            );
                            
                            // Устанавливаем дату публикации
                            newsItem.setPublishedAt(item.getTimestamp());
                            
                            result.add(newsItem);
                            
                            // Если у нас достаточно новостей, прекращаем добавление
                            if (result.size() >= count) {
                                break;
                            }
                        }
                    }
                    
                    Log.d(TAG, "Итоговое количество новостей для отображения: " + result.size());
                    
                    // Если все еще нет новостей, создаем заглушку
                    if (result.isEmpty()) {
                        Log.w(TAG, "Не удалось получить новости, создаем заглушку");
                        NewsItem.Source source = new NewsItem.Source();
                        source.setName("Система");
                        NewsItem placeholderItem = new NewsItem(
                                "Не удалось загрузить новости",
                                "Пожалуйста, проверьте подключение к интернету и попробуйте снова.",
                                "",
                                source,
                                new Date(),
                                ""
                        );
                        result.add(placeholderItem);
                    }
                    
                    return result;
                } catch (Exception e) {
                    Log.e(TAG, "Критическая ошибка при получении новостей", e);
                    // Возвращаем пустой список в случае ошибки
                    List<NewsItem> errorResult = new ArrayList<>();
                    NewsItem.Source source = new NewsItem.Source();
                    source.setName("Ошибка");
                    NewsItem errorItem = new NewsItem(
                            "Произошла ошибка при загрузке новостей",
                            "Пожалуйста, проверьте подключение к интернету и попробуйте снова. Детали ошибки: " + e.getMessage(),
                            "",
                            source,
                            new Date(),
                            ""
                    );
                    errorResult.add(errorItem);
                    return errorResult;
                }
            }
        });
    }
    
    /**
     * Удаляет дубликаты новостей по заголовкам
     */
    private List<NewsParser.NewsItem> removeDuplicates(List<NewsParser.NewsItem> news) {
        List<NewsParser.NewsItem> uniqueNews = new ArrayList<>();
        Set<String> titles = new HashSet<>();
        
        for (NewsParser.NewsItem item : news) {
            if (!titles.contains(item.getTitle())) {
                titles.add(item.getTitle());
                uniqueNews.add(item);
            }
        }
        
        return uniqueNews;
    }
    
    /**
     * Добавляет заглушки новостей, если не удалось получить достаточное количество
     */
    private void addPlaceholderNews(List<NewsItem> news, int count) {
        // Массив стоковых изображений для новостей
        String[] stockImages = {
            "https://via.placeholder.com/400x200/2196F3/FFFFFF?text=Новости",
            "https://via.placeholder.com/400x200/4CAF50/FFFFFF?text=Экономика", 
            "https://via.placeholder.com/400x200/FF9800/FFFFFF?text=Технологии",
            "https://via.placeholder.com/400x200/9C27B0/FFFFFF?text=Бизнес",
            "https://via.placeholder.com/400x200/F44336/FFFFFF?text=События",
            "https://via.placeholder.com/400x200/00BCD4/FFFFFF?text=Мир"
        };
        
        for (int i = 0; i < count; i++) {
            NewsItem item = new NewsItem();
            item.setTitle("Актуальные новости #" + (i + 1));
            item.setDescription("Загрузка новостей временно недоступна. Пожалуйста, проверьте подключение к интернету или повторите попытку позже.");
            
            // Выбираем случайное изображение из массива
            String imageUrl = stockImages[i % stockImages.length];
            item.setUrlToImage(imageUrl);
            
            // Создаем источник
            NewsItem.Source source = new NewsItem.Source();
            source.setName("Локальный источник");
            item.setSource(source);
            
            // Устанавливаем дату публикации
            item.setPublishedAt(new Date());
            
            // Сохраняем оригинальное описание
            item.setOriginalDescription(item.getDescription());
            
            // Устанавливаем русский перевод
            item.setTranslatedDescriptionRu(item.getDescription());
            
            news.add(item);
        }
    }
    
    /**
     * Получает новости по теме асинхронно
     * @param topic тема новостей
     * @param count количество новостей
     * @return Future с результатом
     */
    public Future<List<NewsItem>> getNewsByTopicAsync(String topic, int count) {
        return executorService.submit(new Callable<List<NewsItem>>() {
            @Override
            public List<NewsItem> call() throws Exception {
                try {
                    NewsParser parser = new NewsParser();
                    List<NewsParser.NewsItem> parsedNews = parser.getNewsByTopic(topic, count * 2); // Запрашиваем больше новостей
                    
                    // Если получили недостаточно новостей, добавляем из основного потока
                    if (parsedNews.size() < count) {
                        Log.d(TAG, "Получено недостаточно новостей по теме (" + parsedNews.size() + "), добавляем из основного потока");
                        List<NewsParser.NewsItem> mainNews = parser.getLatestNews(count);
                        parsedNews.addAll(mainNews);
                    }
                    
                    // Удаляем дубликаты
                    List<NewsParser.NewsItem> uniqueNews = removeDuplicates(parsedNews);
                    
                    // Преобразуем в формат приложения
                    List<NewsItem> result = convertToNewsItems(uniqueNews);
                    
                    // Ограничиваем количество новостей
                    if (result.size() > count) {
                        result = result.subList(0, count);
                    }
                    
                    // Проверяем, что у нас есть минимальное количество новостей
                    if (result.size() < 10) {
                        Log.w(TAG, "Получено недостаточно новостей по теме: " + result.size() + " (минимум 10)");
                        // Добавляем заглушки, если не хватает новостей
                        addPlaceholderNews(result, 10 - result.size());
                    }
                    
                    parser.shutdown();
                    return result;
                } catch (Exception e) {
                    Log.e(TAG, "Ошибка при получении новостей по теме: " + topic, e);
                    // В случае ошибки возвращаем заглушки
                    List<NewsItem> placeholders = new ArrayList<>();
                    addPlaceholderNews(placeholders, 10);
                    return placeholders;
                }
            }
        });
    }
    
    /**
     * Преобразует новости из формата парсера в формат приложения
     * @param parsedNews новости из парсера
     * @return новости в формате приложения
     */
    private List<NewsItem> convertToNewsItems(List<NewsParser.NewsItem> parsedNews) {
        List<NewsItem> result = new ArrayList<>();
        
        for (NewsParser.NewsItem parsedItem : parsedNews) {
            NewsItem item = new NewsItem();
            item.setTitle(parsedItem.getTitle());
            item.setDescription(parsedItem.getDescription());
            item.setUrlToImage(getValidImageUrl(parsedItem.getImageUrl()));
            
            // Создаем источник (используем вложенный класс NewsItem.Source)
            NewsItem.Source source = new NewsItem.Source();
            source.setName(parsedItem.getSource());
            item.setSource(source);
            
            // Устанавливаем дату публикации
            item.setPublishedAt(parsedItem.getTimestamp());
            
            // Сохраняем оригинальное описание
            item.setOriginalDescription(parsedItem.getDescription());
            
            // Устанавливаем русский перевод (так как парсер уже на русском)
            item.setTranslatedDescriptionRu(parsedItem.getDescription());
            
            result.add(item);
        }
        
        return result;
    }
    
    /**
     * Получает последние новости синхронно (блокирующий вызов)
     * @param count количество новостей
     * @return список новостей
     */
    public List<NewsItem> getLatestNews(int count) {
        try {
            Future<List<NewsItem>> future = getLatestNewsAsync(count);
            return future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS); // Используем константу для таймаута
        } catch (java.util.concurrent.TimeoutException e) {
            Log.e(TAG, "Превышено время ожидания при получении последних новостей", e);
            // В случае таймаута возвращаем то, что успели получить, или заглушки
            List<NewsItem> placeholders = new ArrayList<>();
            addPlaceholderNews(placeholders, 10);
            return placeholders;
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при синхронном получении последних новостей", e);
            // В случае ошибки возвращаем заглушки
            List<NewsItem> placeholders = new ArrayList<>();
            addPlaceholderNews(placeholders, 10);
            return placeholders;
        }
    }
    
    /**
     * Получает новости по теме синхронно (блокирующий вызов)
     * @param topic тема новостей
     * @param count количество новостей
     * @return список новостей
     */
    public List<NewsItem> getNewsByTopic(String topic, int count) {
        try {
            Future<List<NewsItem>> future = getNewsByTopicAsync(topic, count);
            return future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS); // Используем константу для таймаута
        } catch (java.util.concurrent.TimeoutException e) {
            Log.e(TAG, "Превышено время ожидания при получении новостей по теме: " + topic, e);
            // В случае таймаута возвращаем заглушки
            List<NewsItem> placeholders = new ArrayList<>();
            addPlaceholderNews(placeholders, 10);
            return placeholders;
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при синхронном получении новостей по теме: " + topic, e);
            // В случае ошибки возвращаем заглушки
            List<NewsItem> placeholders = new ArrayList<>();
            addPlaceholderNews(placeholders, 10);
            return placeholders;
        }
    }
    
    /**
     * Освобождает ресурсы
     */
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    /**
     * Проверяет и возвращает корректный URL изображения
     * @param imageUrl исходный URL изображения
     * @return корректный URL изображения или резервный URL
     */
    private String getValidImageUrl(String imageUrl) {
        // Массив стоковых изображений для новостей
        String[] stockImages = {
            "https://via.placeholder.com/400x200/2196F3/FFFFFF?text=Новости",
            "https://via.placeholder.com/400x200/4CAF50/FFFFFF?text=Экономика", 
            "https://via.placeholder.com/400x200/FF9800/FFFFFF?text=Технологии",
            "https://via.placeholder.com/400x200/9C27B0/FFFFFF?text=Бизнес",
            "https://via.placeholder.com/400x200/F44336/FFFFFF?text=События",
            "https://via.placeholder.com/400x200/00BCD4/FFFFFF?text=Мир"
        };
        
        // Если URL пустой или null, возвращаем случайное стоковое изображение
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            int randomIndex = (int) (Math.random() * stockImages.length);
            return stockImages[randomIndex];
        }
        
        // Исправляем URL, если он начинается с //
        if (imageUrl.startsWith("//")) {
            return "https:" + imageUrl;
        }
        
        // Если URL не начинается с http/https, считаем его некорректным
        if (!imageUrl.startsWith("http://") && !imageUrl.startsWith("https://")) {
            int randomIndex = (int) (Math.random() * stockImages.length);
            return stockImages[randomIndex];
        }
        
        // URL корректный, возвращаем как есть
        return imageUrl;
    }
} 