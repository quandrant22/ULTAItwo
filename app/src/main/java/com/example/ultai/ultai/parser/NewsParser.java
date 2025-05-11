package com.example.ultai.ultai.parser;

import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Парсер новостей из различных источников на русском языке
 */
public class NewsParser {
    private static final String TAG = "NewsParser";
    private final OkHttpClient client;
    
    // Список популярных новостных источников на русском языке
    private static final String[] NEWS_SOURCES = {
        "https://www.kommersant.ru/",
        "https://www.vedomosti.ru/",
        "https://www.rbc.ru/",
        "https://ria.ru/",
        "https://news.rambler.ru/",
        "https://lenta.ru/",
        "https://www.mk.ru/",
        "https://www.fontanka.ru/",
        "https://www.kp.ru/"
    };
    
    // User-Agent для запросов
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
    
    /**
     * Конструктор с настройкой HTTP-клиента
     */
    public NewsParser() {
        // Настраиваем HTTP-клиент с таймаутами
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS) // Увеличиваем таймаут соединения до 30 секунд
                .readTimeout(30, TimeUnit.SECONDS)    // Увеличиваем таймаут чтения до 30 секунд
                .writeTimeout(30, TimeUnit.SECONDS)   // Увеличиваем таймаут записи до 30 секунд
                .retryOnConnectionFailure(true)
                .build();
    }
    
    /**
     * Получает последние новости
     * @param count количество новостей для получения
     * @return список новостей
     */
    public List<NewsItem> getLatestNews(int count) {
        List<NewsItem> news = new ArrayList<>();
        Set<String> processedUrls = new HashSet<>(); // Для отслеживания обработанных URL
        
        // Перебираем все источники новостей
        for (String source : NEWS_SOURCES) {
            try {
                // Если уже набрали достаточно новостей, прекращаем
                if (news.size() >= count * 2) {
                    Log.d(TAG, "Получено достаточно новостей (" + news.size() + "), прекращаем сбор");
                    break;
                }
                
                // Проверяем, не обрабатывали ли мы уже этот URL
                if (processedUrls.contains(source)) {
                    Log.d(TAG, "URL уже обработан, пропускаем: " + source);
                    continue;
                }
                
                // Отмечаем URL как обработанный
                processedUrls.add(source);
                
                Log.d(TAG, "Получение новостей из источника: " + source);
                List<NewsItem> sourceNews = parseNewsSource(source);
                
                if (sourceNews != null && !sourceNews.isEmpty()) {
                    Log.d(TAG, "Получено " + sourceNews.size() + " новостей из источника: " + source);
                    news.addAll(sourceNews);
                } else {
                    Log.w(TAG, "Не удалось получить новости из источника: " + source);
                }
            } catch (Exception e) {
                Log.e(TAG, "Ошибка при получении новостей из источника: " + source, e);
                // Продолжаем с другими источниками
            }
        }
        
        // Если не удалось получить достаточно новостей, пробуем Google News
        if (news.size() < count) {
            try {
                Log.d(TAG, "Недостаточно новостей, пробуем Google News");
                List<NewsItem> googleNews = parseGoogleNews();
                if (googleNews != null && !googleNews.isEmpty()) {
                    Log.d(TAG, "Получено " + googleNews.size() + " новостей из Google News");
                    news.addAll(googleNews);
                }
            } catch (Exception e) {
                Log.e(TAG, "Ошибка при получении новостей из Google News", e);
            }
        }
        
        // Если все еще недостаточно новостей, пробуем RSS-ленты
        if (news.size() < count) {
            try {
                Log.d(TAG, "Недостаточно новостей, пробуем RSS-ленты");
                List<NewsItem> rssNews = parseRssFeeds(count);
                if (rssNews != null && !rssNews.isEmpty()) {
                    Log.d(TAG, "Получено " + rssNews.size() + " новостей из RSS-лент");
                    news.addAll(rssNews);
                }
            } catch (Exception e) {
                Log.e(TAG, "Ошибка при получении новостей из RSS-лент", e);
            }
        }
        
        // Удаляем дубликаты
        List<NewsItem> uniqueNews = new ArrayList<>();
        Set<String> titles = new HashSet<>();
        
        for (NewsItem item : news) {
            if (!titles.contains(item.getTitle())) {
                titles.add(item.getTitle());
                uniqueNews.add(item);
            }
        }
        
        // Сортируем новости по дате (сначала новые)
        uniqueNews.sort((n1, n2) -> n2.getTimestamp().compareTo(n1.getTimestamp()));
        
        // Ограничиваем количество новостей
        if (uniqueNews.size() > count) {
            return uniqueNews.subList(0, count);
        }
        
        return uniqueNews;
    }
    
    /**
     * Получает новости по заданной теме
     * @param topic тема для поиска новостей
     * @param count количество новостей для получения
     * @return список новостей
     */
    public List<NewsItem> getNewsByTopic(String topic, int count) {
        List<NewsItem> topicNews = new ArrayList<>();
        
        try {
            Log.d(TAG, "Поиск новостей по теме: " + topic);
            
            // Пробуем несколько источников для поиска новостей по теме
            
            // 1. Поиск через Яндекс.Новости
            try {
                String url = "https://news.yandex.ru/yandsearch?text=" + URLEncoder.encode(topic, "UTF-8") + "&rpt=nnews2&grhow=clutop";
                Document doc = getDocument(url);
                
                if (doc != null) {
                    Elements newsElements = doc.select("div.mg-grid__item");
                    
                    for (Element newsElement : newsElements) {
                        try {
                            Element titleElement = newsElement.selectFirst("h2 a");
                            if (titleElement == null) continue;
                            
                            String title = titleElement.text();
                            String link = titleElement.attr("href");
                            if (!link.startsWith("http")) {
                                link = "https://news.yandex.ru" + link;
                            }
                            
                            Element sourceElement = newsElement.selectFirst("div.mg-card__source-time");
                            String source = sourceElement != null ? sourceElement.text().split(",")[0] : "Яндекс.Новости";
                            
                            Element timeElement = newsElement.selectFirst("span.mg-card-source__time");
                            String timeText = timeElement != null ? timeElement.text() : "";
                            Date timestamp = parseRelativeTime(timeText);
                            
                            Element descElement = newsElement.selectFirst("div.mg-card__annotation");
                            String description = descElement != null ? descElement.text() : "";
                            
                            String imageUrl = "";
                            Element imgElement = newsElement.selectFirst("img");
                            if (imgElement != null) {
                                imageUrl = imgElement.attr("src");
                                if (!imageUrl.startsWith("http")) {
                                    imageUrl = "https:" + imageUrl;
                                }
                            }
                            
                            NewsItem newsItem = new NewsItem(title, description, link, source, timestamp, imageUrl);
                            topicNews.add(newsItem);
                            
                            if (topicNews.size() >= count) {
                                break;
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Ошибка при парсинге новости из Яндекс.Новости", e);
                        }
                    }
                    
                    Log.d(TAG, "Получено " + topicNews.size() + " новостей из Яндекс.Новости по теме: " + topic);
                }
            } catch (Exception e) {
                Log.e(TAG, "Ошибка при поиске новостей через Яндекс.Новости", e);
            }
            
            // 2. Если не нашли достаточно новостей через Яндекс, пробуем Google News
            if (topicNews.size() < count) {
                try {
                    String googleUrl = "https://news.google.com/search?q=" + URLEncoder.encode(topic, "UTF-8") + "&hl=ru&gl=RU&ceid=RU:ru";
                    Document googleDoc = getDocument(googleUrl);
                    
                    if (googleDoc != null) {
                        Elements googleNewsElements = googleDoc.select("div.NiLAwe, article.MQsxIb");
                        
                        for (Element newsElement : googleNewsElements) {
                            try {
                                Element titleElement = newsElement.selectFirst("h3 a, h4 a, a.DY5T1d");
                                if (titleElement == null) continue;
                                
                                String title = titleElement.text();
                                String link = titleElement.attr("href");
                                if (link.startsWith("./")) {
                                    link = link.replace("./", "https://news.google.com/");
                                } else if (!link.startsWith("http")) {
                                    link = "https://news.google.com/" + link;
                                }
                                
                                Element sourceElement = newsElement.selectFirst("div.SVJrMe, div.wEwyrc");
                                String source = sourceElement != null ? sourceElement.text() : "Google News";
                                if (source.contains("·")) {
                                    source = source.split("·")[0].trim();
                                }
                                
                                Element timeElement = newsElement.selectFirst("time, span.WW6dff");
                                String timeText = timeElement != null ? timeElement.text() : "";
                                Date timestamp = parseRelativeTime(timeText);
                                
                                String description = "";
                                Element descElement = newsElement.selectFirst("span.xBbh9, div.GI74Re");
                                if (descElement != null) {
                                    description = descElement.text();
                                }
                                
                                String imageUrl = "";
                                Element imgElement = newsElement.selectFirst("img.tvs3Id, img.uhHOwf");
                                if (imgElement != null) {
                                    imageUrl = imgElement.attr("src");
                                    if (imageUrl.isEmpty() && imgElement.hasAttr("data-src")) {
                                        imageUrl = imgElement.attr("data-src");
                                    }
                                }
                                
                                NewsItem newsItem = new NewsItem(title, description, link, source, timestamp, imageUrl);
                                topicNews.add(newsItem);
                                
                                if (topicNews.size() >= count) {
                                    break;
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Ошибка при парсинге новости из Google News", e);
                            }
                        }
                        
                        Log.d(TAG, "Получено " + (topicNews.size()) + " новостей из Google News по теме: " + topic);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Ошибка при поиске новостей через Google News", e);
                }
            }
            
            // 3. Если все еще недостаточно новостей, пробуем поиск по основным источникам
            if (topicNews.size() < count) {
                try {
                    for (String source : NEWS_SOURCES) {
                        try {
                            // Формируем URL для поиска по сайту
                            String searchUrl = "";
                            if (source.contains("lenta.ru")) {
                                searchUrl = "https://lenta.ru/search/v2/process?from=0&size=10&sort=2&title_only=0&domain=1&query=" + URLEncoder.encode(topic, "UTF-8");
                            } else if (source.contains("ria.ru")) {
                                searchUrl = "https://ria.ru/search/?query=" + URLEncoder.encode(topic, "UTF-8");
                            } else if (source.contains("rbc.ru")) {
                                searchUrl = "https://www.rbc.ru/search/?query=" + URLEncoder.encode(topic, "UTF-8");
                            } else if (source.contains("kommersant.ru")) {
                                searchUrl = "https://www.kommersant.ru/search/results?search_query=" + URLEncoder.encode(topic, "UTF-8");
                            } else if (source.contains("vedomosti.ru")) {
                                searchUrl = "https://www.vedomosti.ru/search?query=" + URLEncoder.encode(topic, "UTF-8");
                            } else {
                                continue; // Пропускаем источники без известного формата поиска
                            }
                            
                            Document searchDoc = getDocument(searchUrl);
                            if (searchDoc == null) continue;
                            
                            // Парсим результаты поиска
                            Elements searchResults = searchDoc.select("article, div.search-result, div.news-item");
                            if (searchResults.isEmpty()) {
                                searchResults = searchDoc.select("a[href]:has(h1), a[href]:has(h2), a[href]:has(h3), a[href]:has(h4)");
                            }
                            
                            for (Element result : searchResults) {
                                try {
                                    Element titleElement = result.selectFirst("h1, h2, h3, h4, .title");
                                    if (titleElement == null) continue;
                                    
                                    String title = titleElement.text();
                                    if (title.isEmpty()) continue;
                                    
                                    String link = "";
                                    if (result.hasAttr("href")) {
                                        link = result.attr("href");
                                    } else if (result.selectFirst("a[href]") != null) {
                                        link = result.selectFirst("a[href]").attr("href");
                                    }
                                    
                                    if (!link.startsWith("http")) {
                                        link = source + link;
                                    }
                                    
                                    String sourceName = source.replace("https://", "").replace("http://", "").replace("www.", "");
                                    if (sourceName.contains("/")) {
                                        sourceName = sourceName.substring(0, sourceName.indexOf("/"));
                                    }
                                    
                                    Date timestamp = new Date();
                                    Element timeElement = result.selectFirst("time, .date, .time");
                                    if (timeElement != null) {
                                        String timeText = timeElement.text();
                                        timestamp = parseRelativeTime(timeText);
                                    }
                                    
                                    String description = "";
                                    Element descElement = result.selectFirst("p, .description, .summary");
                                    if (descElement != null) {
                                        description = descElement.text();
                                    }
                                    
                                    String imageUrl = "";
                                    Element imgElement = result.selectFirst("img");
                                    if (imgElement != null) {
                                        imageUrl = imgElement.attr("src");
                                        if (imageUrl.isEmpty() && imgElement.hasAttr("data-src")) {
                                            imageUrl = imgElement.attr("data-src");
                                        }
                                        if (!imageUrl.startsWith("http")) {
                                            if (imageUrl.startsWith("//")) {
                                                imageUrl = "https:" + imageUrl;
                                            } else {
                                                imageUrl = source + imageUrl;
                                            }
                                        }
                                    }
                                    
                                    NewsItem newsItem = new NewsItem(title, description, link, sourceName, timestamp, imageUrl);
                                    topicNews.add(newsItem);
                                    
                                    if (topicNews.size() >= count) {
                                        break;
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Ошибка при парсинге результата поиска из " + source, e);
                                }
                            }
                            
                            if (topicNews.size() >= count) {
                                break;
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Ошибка при поиске новостей на " + source, e);
                        }
                    }
                    
                    Log.d(TAG, "Получено " + topicNews.size() + " новостей из основных источников по теме: " + topic);
                } catch (Exception e) {
                    Log.e(TAG, "Ошибка при поиске новостей по основным источникам", e);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при поиске новостей по теме: " + topic, e);
        }
        
        // Сортируем новости по дате (сначала новые)
        topicNews.sort((n1, n2) -> n2.getTimestamp().compareTo(n1.getTimestamp()));
        
        // Удаляем дубликаты
        List<NewsItem> uniqueNews = new ArrayList<>();
        Set<String> titles = new HashSet<>();
        
        for (NewsItem item : topicNews) {
            if (!titles.contains(item.getTitle())) {
                titles.add(item.getTitle());
                uniqueNews.add(item);
            }
        }
        
        // Ограничиваем количество новостей
        if (uniqueNews.size() > count) {
            return uniqueNews.subList(0, count);
        }
        
        return uniqueNews;
    }
    
    /**
     * Парсит новости из указанного источника
     * @param sourceUrl URL источника
     * @return список новостей
     */
    private List<NewsItem> parseNewsSource(String sourceUrl) {
        return parseNewsSource(sourceUrl, 10); // По умолчанию получаем 10 новостей
    }
    
    /**
     * Парсит новости из конкретного источника
     * @param sourceUrl URL источника новостей
     * @param count количество новостей для получения
     * @return список новостей
     */
    private List<NewsItem> parseNewsSource(String sourceUrl, int count) {
        List<NewsItem> news = new ArrayList<>();
        
        try {
            Log.d(TAG, "Начинаем парсинг источника: " + sourceUrl);
            Document doc = getDocument(sourceUrl);
            if (doc == null) {
                Log.w(TAG, "Не удалось получить документ для источника: " + sourceUrl);
                return news;
            }
            
            // Разные селекторы для разных источников
            if (sourceUrl.contains("lenta.ru")) {
                Elements newsElements = doc.select("div.card, div.item");
                
                if (newsElements.isEmpty()) {
                    Log.w(TAG, "Не найдены элементы новостей для источника: " + sourceUrl);
                    return news;
                }
                
                for (Element newsElement : newsElements) {
                    try {
                        Element titleElement = newsElement.selectFirst("h3, span.card-title");
                        if (titleElement == null) continue;
                        
                        String title = titleElement.text();
                        
                        Element linkElement = newsElement.selectFirst("a");
                        String link = linkElement != null ? linkElement.attr("href") : "";
                        if (!link.startsWith("http")) {
                            link = "https://lenta.ru" + link;
                        }
                        
                        Element timeElement = newsElement.selectFirst("time");
                        String timeText = timeElement != null ? timeElement.text() : "";
                        Date timestamp = parseRelativeTime(timeText);
                        
                        String description = "";
                        Element descElement = newsElement.selectFirst("p");
                        if (descElement != null) {
                            description = descElement.text();
                        }
                        
                        String imageUrl = "";
                        Element imgElement = newsElement.selectFirst("img");
                        if (imgElement != null) {
                            imageUrl = imgElement.attr("src");
                            if (!imageUrl.startsWith("http")) {
                                imageUrl = "https:" + imageUrl;
                            }
                        }
                        
                        NewsItem newsItem = new NewsItem(title, description, link, "Лента.ру", timestamp, imageUrl);
                        news.add(newsItem);
                        
                        if (news.size() >= count) {
                            break;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Ошибка при парсинге новости из Lenta.ru", e);
                    }
                }
            } else if (sourceUrl.contains("ria.ru")) {
                Elements newsElements = doc.select("div.list-item");
                
                for (Element newsElement : newsElements) {
                    try {
                        Element titleElement = newsElement.selectFirst("a.list-item__title");
                        if (titleElement == null) continue;
                        
                        String title = titleElement.text();
                        String link = titleElement.attr("href");
                        if (!link.startsWith("http")) {
                            link = "https://ria.ru" + link;
                        }
                        
                        Element timeElement = newsElement.selectFirst("div.list-item__date");
                        String timeText = timeElement != null ? timeElement.text() : "";
                        Date timestamp = parseRelativeTime(timeText);
                        
                        String description = "";
                        
                        String imageUrl = "";
                        Element imgElement = newsElement.selectFirst("img");
                        if (imgElement != null) {
                            imageUrl = imgElement.attr("src");
                            if (!imageUrl.startsWith("http")) {
                                imageUrl = "https:" + imageUrl;
                            }
                        }
                        
                        NewsItem newsItem = new NewsItem(title, description, link, "РИА Новости", timestamp, imageUrl);
                        news.add(newsItem);
                        
                        if (news.size() >= count) {
                            break;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Ошибка при парсинге новости из РИА Новости", e);
                    }
                }
            } else if (sourceUrl.contains("rbc.ru")) {
                Elements newsElements = doc.select("div.item, div.news-feed__item");
                
                for (Element newsElement : newsElements) {
                    try {
                        Element titleElement = newsElement.selectFirst("span.item__title, div.news-feed__item__title");
                        if (titleElement == null) continue;
                        
                        String title = titleElement.text();
                        
                        Element linkElement = newsElement.selectFirst("a.item__link, a.news-feed__item__link");
                        String link = linkElement != null ? linkElement.attr("href") : "";
                        if (!link.startsWith("http")) {
                            link = "https://www.rbc.ru" + link;
                        }
                        
                        Date timestamp = new Date(); // РБК обычно не показывает время на главной
                        
                        String description = "";
                        Element descElement = newsElement.selectFirst("span.item__text, div.news-feed__item__anons");
                        if (descElement != null) {
                            description = descElement.text();
                        }
                        
                        String imageUrl = "";
                        Element imgElement = newsElement.selectFirst("img");
                        if (imgElement != null) {
                            imageUrl = imgElement.attr("src");
                            if (!imageUrl.startsWith("http")) {
                                imageUrl = "https:" + imageUrl;
                            }
                        }
                        
                        NewsItem newsItem = new NewsItem(title, description, link, "РБК", timestamp, imageUrl);
                        news.add(newsItem);
                        
                        if (news.size() >= count) {
                            break;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Ошибка при парсинге новости из РБК", e);
                    }
                }
            } else if (sourceUrl.contains("kommersant.ru")) {
                Elements newsElements = doc.select("article.uho, div.rubric_lenta__item");
                
                for (Element newsElement : newsElements) {
                    try {
                        Element titleElement = newsElement.selectFirst("h2.uho__name, div.rubric_lenta__item_text");
                        if (titleElement == null) continue;
                        
                        String title = titleElement.text();
                        
                        Element linkElement = newsElement.selectFirst("a.uho__link, a.rubric_lenta__item_link");
                        String link = linkElement != null ? linkElement.attr("href") : "";
                        if (!link.startsWith("http")) {
                            link = "https://www.kommersant.ru" + link;
                        }
                        
                        Element timeElement = newsElement.selectFirst("time, span.rubric_lenta__item_time");
                        String timeText = timeElement != null ? timeElement.text() : "";
                        Date timestamp = parseRelativeTime(timeText);
                        
                        String description = "";
                        Element descElement = newsElement.selectFirst("p.doc__text, div.rubric_lenta__item_announce");
                        if (descElement != null) {
                            description = descElement.text();
                        }
                        
                        String imageUrl = "";
                        Element imgElement = newsElement.selectFirst("img");
                        if (imgElement != null) {
                            imageUrl = imgElement.attr("src");
                            if (!imageUrl.startsWith("http")) {
                                imageUrl = "https:" + imageUrl;
                            }
                        }
                        
                        NewsItem newsItem = new NewsItem(title, description, link, "Коммерсантъ", timestamp, imageUrl);
                        news.add(newsItem);
                        
                        if (news.size() >= count) {
                            break;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Ошибка при парсинге новости из Коммерсантъ", e);
                    }
                }
            } else if (sourceUrl.contains("vedomosti.ru")) {
                Elements newsElements = doc.select("article.card-article, div.news-item");
                
                for (Element newsElement : newsElements) {
                    try {
                        Element titleElement = newsElement.selectFirst("h2.card-article__title, div.news-item__title");
                        if (titleElement == null) continue;
                        
                        String title = titleElement.text();
                        
                        Element linkElement = newsElement.selectFirst("a.card-article__link, a.news-item__link");
                        String link = linkElement != null ? linkElement.attr("href") : "";
                        if (!link.startsWith("http")) {
                            link = "https://www.vedomosti.ru" + link;
                        }
                        
                        Element timeElement = newsElement.selectFirst("time, span.news-item__date");
                        String timeText = timeElement != null ? timeElement.text() : "";
                        Date timestamp = parseRelativeTime(timeText);
                        
                        String description = "";
                        Element descElement = newsElement.selectFirst("p.card-article__text, div.news-item__subtitle");
                        if (descElement != null) {
                            description = descElement.text();
                        }
                        
                        String imageUrl = "";
                        Element imgElement = newsElement.selectFirst("img");
                        if (imgElement != null) {
                            imageUrl = imgElement.attr("src");
                            if (!imageUrl.startsWith("http")) {
                                imageUrl = "https:" + imageUrl;
                            }
                        }
                        
                        NewsItem newsItem = new NewsItem(title, description, link, "Ведомости", timestamp, imageUrl);
                        news.add(newsItem);
                        
                        if (news.size() >= count) {
                            break;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Ошибка при парсинге новости из Ведомости", e);
                    }
                }
            } else if (sourceUrl.contains("meduza.io")) {
                Elements newsElements = doc.select("div.BlockMaterial-root, article.GeneralMaterial-root");
                
                for (Element newsElement : newsElements) {
                    try {
                        Element titleElement = newsElement.selectFirst("h2.BlockMaterial-title, h1.GeneralMaterial-title");
                        if (titleElement == null) continue;
                        
                        String title = titleElement.text();
                        
                        Element linkElement = newsElement.selectFirst("a.BlockMaterial-link, a.Link-root");
                        String link = linkElement != null ? linkElement.attr("href") : "";
                        if (!link.startsWith("http")) {
                            link = "https://meduza.io" + link;
                        }
                        
                        Date timestamp = new Date(); // Медуза обычно не показывает время на главной
                        
                        String description = "";
                        Element descElement = newsElement.selectFirst("div.BlockMaterial-lead, div.GeneralMaterial-lead");
                        if (descElement != null) {
                            description = descElement.text();
                        }
                        
                        String imageUrl = "";
                        Element imgElement = newsElement.selectFirst("img");
                        if (imgElement != null) {
                            imageUrl = imgElement.attr("src");
                            if (!imageUrl.startsWith("http")) {
                                imageUrl = "https:" + imageUrl;
                            }
                        }
                        
                        NewsItem newsItem = new NewsItem(title, description, link, "Медуза", timestamp, imageUrl);
                        news.add(newsItem);
                        
                        if (news.size() >= count) {
                            break;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Ошибка при парсинге новости из Медуза", e);
                    }
                }
            } else if (sourceUrl.contains("rambler.ru")) {
                Elements newsElements = doc.select("div.rui__2Ixfm, div.news-item");
                
                for (Element newsElement : newsElements) {
                    try {
                        Element titleElement = newsElement.selectFirst("a.rui__2Ixfm, div.news-item__title");
                        if (titleElement == null) continue;
                        
                        String title = titleElement.text();
                        
                        Element linkElement = newsElement.selectFirst("a.rui__2Ixfm, a.news-item__link");
                        String link = linkElement != null ? linkElement.attr("href") : "";
                        
                        Date timestamp = new Date(); // Рамблер обычно не показывает время на главной
                        
                        String description = "";
                        Element descElement = newsElement.selectFirst("div.rui__3Qtmj, div.news-item__annotation");
                        if (descElement != null) {
                            description = descElement.text();
                        }
                        
                        String imageUrl = "";
                        Element imgElement = newsElement.selectFirst("img");
                        if (imgElement != null) {
                            imageUrl = imgElement.attr("src");
                            if (!imageUrl.startsWith("http")) {
                                imageUrl = "https:" + imageUrl;
                            }
                        }
                        
                        NewsItem newsItem = new NewsItem(title, description, link, "Рамблер", timestamp, imageUrl);
                        news.add(newsItem);
                        
                        if (news.size() >= count) {
                            break;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Ошибка при парсинге новости из Рамблер", e);
                    }
                }
            } else {
                // Общий парсер для других источников
                // Ищем заголовки и ссылки
                Elements newsElements = doc.select("a[href]:has(h1), a[href]:has(h2), a[href]:has(h3), a[href]:has(h4), div.news-item, article");
                
                for (Element newsElement : newsElements) {
                    try {
                        Element titleElement = newsElement.selectFirst("h1, h2, h3, h4, .title");
                        if (titleElement == null) continue;
                        
                        String title = titleElement.text();
                        if (title.length() < 10) continue; // Слишком короткие заголовки пропускаем
                        
                        String link = newsElement.attr("href");
                        if (link.isEmpty() && newsElement.selectFirst("a") != null) {
                            link = newsElement.selectFirst("a").attr("href");
                        }
                        
                        if (!link.startsWith("http")) {
                            link = sourceUrl + link;
                        }
                        
                        // Определяем источник из URL
                        String source = sourceUrl.replace("https://", "").replace("http://", "").replace("www.", "");
                        if (source.contains("/")) {
                            source = source.substring(0, source.indexOf("/"));
                        }
                        
                        Date timestamp = new Date(); // Текущее время, если не удалось найти
                        
                        String description = "";
                        Element descElement = newsElement.selectFirst("p, .description, .summary");
                        if (descElement != null) {
                            description = descElement.text();
                        }
                        
                        String imageUrl = "";
                        Element imgElement = newsElement.selectFirst("img");
                        if (imgElement != null) {
                            imageUrl = imgElement.attr("src");
                            if (!imageUrl.startsWith("http")) {
                                if (imageUrl.startsWith("//")) {
                                    imageUrl = "https:" + imageUrl;
                                } else {
                                    imageUrl = sourceUrl + imageUrl;
                                }
                            }
                        }
                        
                        NewsItem newsItem = new NewsItem(title, description, link, source, timestamp, imageUrl);
                        
                        // Проверяем, нет ли дубликатов
                        boolean isDuplicate = false;
                        for (NewsItem existingItem : news) {
                            if (existingItem.getTitle().equals(title)) {
                                isDuplicate = true;
                                break;
                            }
                        }
                        
                        if (!isDuplicate) {
                            news.add(newsItem);
                        }
                        
                        if (news.size() >= count) {
                            break;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Ошибка при парсинге новости из " + sourceUrl, e);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при парсинге источника: " + sourceUrl, e);
        }
        
        return news;
    }
    
    /**
     * Получает HTML-документ по URL с обработкой ошибок
     */
    private Document getDocument(String url) throws IOException {
        Log.d(TAG, "Загрузка документа: " + url);
        
        // Проверяем URL на проблемные домены
        if (url.contains("meduza.io")) {
            Log.w(TAG, "Пропускаем проблемный сайт: " + url);
            return null;
        }
        
        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", USER_AGENT)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7")
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                Log.e(TAG, "Ошибка при загрузке страницы: " + url + ", код: " + response.code());
                return null;
            }
            
            if (response.body() == null) {
                Log.e(TAG, "Пустой ответ от сервера: " + url);
                return null;
            }
            
            // Определяем тип контента
            String contentType = response.header("Content-Type", "");
            String charset = "UTF-8"; // По умолчанию используем UTF-8
            
            // Пытаемся извлечь кодировку из заголовка Content-Type
            if (contentType != null && contentType.contains("charset=")) {
                int charsetIndex = contentType.indexOf("charset=");
                if (charsetIndex != -1) {
                    charset = contentType.substring(charsetIndex + 8).trim();
                    // Удаляем лишние символы, если они есть
                    if (charset.contains(";")) {
                        charset = charset.substring(0, charset.indexOf(";"));
                    }
                    charset = charset.replace("\"", "").replace("'", "");
                }
            }
            
            // Получаем содержимое ответа
            String html = response.body().string();
            
            // Проверяем, есть ли в HTML указание на кодировку
            if (html.contains("<meta") && html.contains("charset=")) {
                int metaIndex = html.indexOf("<meta");
                int charsetIndex = html.indexOf("charset=", metaIndex);
                if (charsetIndex != -1) {
                    int charsetStart = charsetIndex + 8;
                    int charsetEnd = html.indexOf("\"", charsetStart);
                    if (charsetEnd == -1) {
                        charsetEnd = html.indexOf("'", charsetStart);
                    }
                    if (charsetEnd == -1) {
                        charsetEnd = html.indexOf(">", charsetStart);
                    }
                    if (charsetEnd != -1) {
                        String htmlCharset = html.substring(charsetStart, charsetEnd).trim();
                        htmlCharset = htmlCharset.replace("\"", "").replace("'", "");
                        if (!htmlCharset.isEmpty()) {
                            charset = htmlCharset;
                        }
                    }
                }
            }
            
            // Если это XML или RSS, используем XML-парсер
            if (contentType.contains("xml") || url.contains(".xml") || url.contains("rss")) {
                return Jsoup.parse(html, "", org.jsoup.parser.Parser.xmlParser());
            }
            
            // Для HTML используем обычный парсер
            return Jsoup.parse(html, url);
        } catch (java.net.SocketTimeoutException e) {
            Log.e(TAG, "Таймаут соединения при загрузке: " + url, e);
            return null;
        } catch (java.net.UnknownHostException e) {
            Log.e(TAG, "Неизвестный хост: " + url, e);
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при загрузке документа: " + url, e);
            return null;
        }
    }
    
    /**
     * Парсит относительное время (например, "2 часа назад") в объект Date
     * @param relativeTime строка с относительным временем
     * @return объект Date
     */
    private Date parseRelativeTime(String relativeTime) {
        Date now = new Date();
        
        if (relativeTime == null || relativeTime.isEmpty()) {
            return now;
        }
        
        try {
            relativeTime = relativeTime.toLowerCase();
            
            // Проверяем форматы даты
            if (relativeTime.matches("\\d{2}\\.\\d{2}\\.\\d{4}.*")) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
                return sdf.parse(relativeTime.substring(0, 10));
            }
            
            if (relativeTime.matches("\\d{2}:\\d{2}.*")) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                return sdf.parse(relativeTime.substring(0, 5));
            }
            
            // Парсим относительное время
            if (relativeTime.contains("минут") || relativeTime.contains("мин")) {
                int minutes = extractNumber(relativeTime);
                return new Date(now.getTime() - minutes * 60 * 1000);
            } else if (relativeTime.contains("час") || relativeTime.contains("ч.")) {
                int hours = extractNumber(relativeTime);
                return new Date(now.getTime() - hours * 60 * 60 * 1000);
            } else if (relativeTime.contains("день") || relativeTime.contains("дн") || relativeTime.contains("сутки")) {
                int days = extractNumber(relativeTime);
                return new Date(now.getTime() - days * 24 * 60 * 60 * 1000);
            } else if (relativeTime.contains("недел")) {
                int weeks = extractNumber(relativeTime);
                return new Date(now.getTime() - weeks * 7 * 24 * 60 * 60 * 1000);
            } else if (relativeTime.contains("месяц") || relativeTime.contains("мес")) {
                int months = extractNumber(relativeTime);
                return new Date(now.getTime() - months * 30L * 24 * 60 * 60 * 1000);
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при парсинге времени: " + relativeTime, e);
        }
        
        return now;
    }
    
    /**
     * Извлекает число из строки
     * @param text строка с числом
     * @return извлеченное число или 0, если не удалось извлечь
     */
    private int extractNumber(String text) {
        try {
            StringBuilder sb = new StringBuilder();
            for (char c : text.toCharArray()) {
                if (Character.isDigit(c)) {
                    sb.append(c);
                } else if (sb.length() > 0) {
                    break;
                }
            }
            
            if (sb.length() > 0) {
                return Integer.parseInt(sb.toString());
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при извлечении числа из строки: " + text, e);
        }
        
        return 0;
    }
    
    /**
     * Закрывает ресурсы парсера
     */
    public void shutdown() {
        if (client != null) {
            client.dispatcher().executorService().shutdown();
            client.connectionPool().evictAll();
        }
    }
    
    /**
     * Парсит новости из Google News
     * @return список новостей
     */
    private List<NewsItem> parseGoogleNews() {
        return parseGoogleNews("https://news.google.com/topstories?hl=ru&gl=RU&ceid=RU:ru", 20);
    }
    
    /**
     * Парсит новости из Google News
     * @param url URL Google News
     * @param count количество новостей для получения
     * @return список новостей
     */
    private List<NewsItem> parseGoogleNews(String url, int count) {
        List<NewsItem> news = new ArrayList<>();
        try {
            Log.d(TAG, "Начинаем парсинг Google News: " + url);
            Document doc = getDocument(url);
            if (doc == null) {
                Log.w(TAG, "Не удалось получить документ для Google News");
                return news;
            }
            
            // Пробуем разные селекторы для Google News
            Elements newsElements = doc.select("div.NiLAwe, article.MQsxIb, div.xrnccd");
            
            if (newsElements.isEmpty()) {
                Log.w(TAG, "Не найдены элементы новостей в Google News");
                // Пробуем альтернативный селектор
                newsElements = doc.select("article, div.VlJC0");
            }
            
            Log.d(TAG, "Найдено " + newsElements.size() + " элементов новостей в Google News");
            
            for (Element newsElement : newsElements) {
                try {
                    // Пробуем разные селекторы для заголовка
                    Element titleElement = newsElement.selectFirst("h3 a, h4 a, a.DY5T1d, a.RZIKme");
                    if (titleElement == null) {
                        titleElement = newsElement.selectFirst("a[href] h3, a[href] h4");
                    }
                    
                    if (titleElement == null) continue;
                    
                    String title = titleElement.text();
                    if (title.isEmpty()) continue;
                    
                    // Получаем ссылку
                    String link = "";
                    if (titleElement.hasAttr("href")) {
                        link = titleElement.attr("href");
                    } else if (titleElement.parent() != null && titleElement.parent().hasAttr("href")) {
                        link = titleElement.parent().attr("href");
                    }
                    
                    if (link.isEmpty() && newsElement.selectFirst("a[href]") != null) {
                        link = newsElement.selectFirst("a[href]").attr("href");
                    }
                    
                    if (link.startsWith("./")) {
                        link = link.replace("./", "https://news.google.com/");
                    } else if (link.startsWith("/")) {
                        link = "https://news.google.com" + link;
                    } else if (!link.startsWith("http")) {
                        link = "https://news.google.com/" + link;
                    }
                    
                    // Получаем источник
                    Element sourceElement = newsElement.selectFirst("div.SVJrMe, div.wEwyrc, a.wEwyrc");
                    String source = sourceElement != null ? sourceElement.text() : "Google News";
                    if (source.contains("·")) {
                        source = source.split("·")[0].trim();
                    }
                    
                    // Получаем время
                    Element timeElement = newsElement.selectFirst("time, span.WW6dff");
                    String timeText = timeElement != null ? timeElement.text() : "";
                    Date timestamp = parseRelativeTime(timeText);
                    
                    // Получаем описание
                    String description = "";
                    Element descElement = newsElement.selectFirst("span.xBbh9, div.GI74Re");
                    if (descElement != null) {
                        description = descElement.text();
                    }
                    
                    // Получаем изображение
                    String imageUrl = "";
                    Element imgElement = newsElement.selectFirst("img.tvs3Id, img.uhHOwf, img[src]");
                    if (imgElement != null) {
                        imageUrl = imgElement.attr("src");
                        if (imageUrl.isEmpty() && imgElement.hasAttr("data-src")) {
                            imageUrl = imgElement.attr("data-src");
                        }
                    }
                    
                    // Создаем объект новости
                    NewsItem newsItem = new NewsItem(title, description, link, source, timestamp, imageUrl);
                    news.add(newsItem);
                    
                    if (news.size() >= count) {
                        break;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Ошибка при парсинге новости из Google News", e);
                }
            }
            
            Log.d(TAG, "Успешно получено " + news.size() + " новостей из Google News");
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при парсинге Google News", e);
        }
        return news;
    }
    
    /**
     * Парсит RSS-ленты популярных новостных сайтов
     * @param count количество новостей для получения
     * @return список новостей
     */
    public List<NewsItem> parseRssFeeds(int count) {
        List<NewsItem> rssNews = new ArrayList<>();
        Set<String> processedUrls = new HashSet<>(); // Для отслеживания обработанных URL
        
        // Список популярных RSS-лент новостных сайтов на русском языке
        String[] rssFeeds = {
            "https://lenta.ru/rss/news",
            "https://www.vedomosti.ru/rss/news",
            "https://tass.ru/rss/v2.xml",
            "https://www.kommersant.ru/RSS/news.xml",
            "https://rg.ru/xml/index.xml",
            "https://www.interfax.ru/rss.asp",
            "https://ria.ru/export/rss2/archive/index.xml",
            "https://www.fontanka.ru/fontanka.rss",
            "https://www.kp.ru/rss/news.xml"
        };
        
        for (String rssFeed : rssFeeds) {
            try {
                // Если уже набрали достаточно новостей, прекращаем
                if (rssNews.size() >= count) {
                    Log.d(TAG, "Получено достаточно новостей из RSS (" + rssNews.size() + "), прекращаем сбор");
                    break;
                }
                
                // Проверяем, не обрабатывали ли мы уже этот URL
                if (processedUrls.contains(rssFeed)) {
                    Log.d(TAG, "RSS URL уже обработан, пропускаем: " + rssFeed);
                    continue;
                }
                
                // Отмечаем URL как обработанный
                processedUrls.add(rssFeed);
                
                Log.d(TAG, "Парсинг RSS-ленты: " + rssFeed);
                Document doc = getDocument(rssFeed);
                
                if (doc == null) {
                    Log.e(TAG, "Не удалось получить документ для RSS-ленты: " + rssFeed);
                    continue;
                }
                
                // Определяем источник из URL
                String source = rssFeed.replace("https://", "").replace("http://", "").replace("www.", "");
                if (source.contains("/")) {
                    source = source.substring(0, source.indexOf("/"));
                }
                
                // Парсим элементы новостей
                Elements items = doc.select("item");
                if (items.isEmpty()) {
                    items = doc.select("entry");
                }
                
                Log.d(TAG, "Найдено " + items.size() + " элементов в RSS-ленте: " + rssFeed);
                
                for (Element item : items) {
                    try {
                        // Парсим заголовок
                        Element titleElement = item.selectFirst("title");
                        if (titleElement == null) continue;
                        
                        String title = titleElement.text();
                        if (title.isEmpty()) continue;
                        
                        // Парсим ссылку
                        String link = "";
                        Element linkElement = item.selectFirst("link");
                        if (linkElement != null) {
                            if (linkElement.hasText()) {
                                link = linkElement.text();
                            } else {
                                link = linkElement.attr("href");
                                if (link.isEmpty()) {
                                    link = linkElement.attr("url");
                                }
                            }
                        }
                        
                        if (link.isEmpty()) continue;
                        
                        // Парсим дату публикации
                        Date timestamp = new Date();
                        Element pubDateElement = item.selectFirst("pubDate");
                        if (pubDateElement == null) {
                            pubDateElement = item.selectFirst("published");
                        }
                        if (pubDateElement == null) {
                            pubDateElement = item.selectFirst("updated");
                        }
                        
                        if (pubDateElement != null) {
                            String pubDateText = pubDateElement.text();
                            try {
                                // Пробуем разные форматы даты
                                SimpleDateFormat[] dateFormats = {
                                    new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH),
                                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ENGLISH),
                                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.ENGLISH),
                                    new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss", Locale.ENGLISH)
                                };
                                
                                for (SimpleDateFormat format : dateFormats) {
                                    try {
                                        timestamp = format.parse(pubDateText);
                                        break;
                                    } catch (Exception e) {
                                        // Пробуем следующий формат
                                    }
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Ошибка при парсинге даты: " + pubDateText, e);
                            }
                        }
                        
                        // Парсим описание
                        String description = "";
                        Element descElement = item.selectFirst("description");
                        if (descElement == null) {
                            descElement = item.selectFirst("summary");
                        }
                        if (descElement == null) {
                            descElement = item.selectFirst("content");
                        }
                        
                        if (descElement != null) {
                            description = descElement.text();
                            // Очищаем HTML-теги из описания
                            description = description.replaceAll("<[^>]*>", "");
                            // Ограничиваем длину описания
                            if (description.length() > 300) {
                                description = description.substring(0, 297) + "...";
                            }
                        }
                        
                        // Парсим изображение
                        String imageUrl = "";
                        Element enclosureElement = item.selectFirst("enclosure[type^=image]");
                        if (enclosureElement != null) {
                            imageUrl = enclosureElement.attr("url");
                        } else {
                            // Пробуем найти изображение в описании
                            Element mediaContent = item.selectFirst("media:content[type^=image]");
                            if (mediaContent != null) {
                                imageUrl = mediaContent.attr("url");
                            } else {
                                // Пробуем найти изображение в HTML-описании
                                if (description.contains("<img")) {
                                    int imgStart = description.indexOf("<img");
                                    int srcStart = description.indexOf("src=\"", imgStart);
                                    if (srcStart != -1) {
                                        srcStart += 5;
                                        int srcEnd = description.indexOf("\"", srcStart);
                                        if (srcEnd != -1) {
                                            imageUrl = description.substring(srcStart, srcEnd);
                                        }
                                    }
                                }
                            }
                        }
                        
                        // Создаем объект новости
                        NewsItem newsItem = new NewsItem(title, description, link, source, timestamp, imageUrl);
                        
                        // Проверяем на дубликаты
                        boolean isDuplicate = false;
                        for (NewsItem existingItem : rssNews) {
                            if (existingItem.getTitle().equals(title)) {
                                isDuplicate = true;
                                break;
                            }
                        }
                        
                        if (!isDuplicate) {
                            rssNews.add(newsItem);
                        }
                        
                        if (rssNews.size() >= count) {
                            break;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Ошибка при парсинге элемента RSS: " + e.getMessage());
                    }
                }
                
                if (rssNews.size() >= count) {
                    break;
                }
            } catch (Exception e) {
                Log.e(TAG, "Ошибка при парсинге RSS-ленты: " + rssFeed, e);
            }
        }
        
        // Сортируем новости по дате (сначала новые)
        rssNews.sort((n1, n2) -> n2.getTimestamp().compareTo(n1.getTimestamp()));
        
        return rssNews;
    }
    
    /**
     * Класс для хранения информации о новости
     */
    public static class NewsItem {
        private final String title;
        private final String description;
        private final String url;
        private final String source;
        private final Date timestamp;
        private final String imageUrl;
        
        public NewsItem(String title, String description, String url, String source, Date timestamp, String imageUrl) {
            this.title = title;
            this.description = description;
            this.url = url;
            this.source = source;
            this.timestamp = timestamp;
            this.imageUrl = imageUrl;
        }
        
        public String getTitle() {
            return title;
        }
        
        public String getDescription() {
            return description;
        }
        
        public String getUrl() {
            return url;
        }
        
        public String getSource() {
            return source;
        }
        
        public Date getTimestamp() {
            return timestamp;
        }
        
        public String getImageUrl() {
            return imageUrl;
        }
        
        public String getFormattedTime() {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
            return sdf.format(timestamp);
        }
        
        public String getRelativeTime() {
            long diffInMillis = new Date().getTime() - timestamp.getTime();
            long diffInMinutes = diffInMillis / (60 * 1000);
            
            if (diffInMinutes < 1) {
                return "только что";
            } else if (diffInMinutes < 60) {
                return diffInMinutes + " " + getMinutesString(diffInMinutes) + " назад";
            } else {
                long diffInHours = diffInMinutes / 60;
                if (diffInHours < 24) {
                    return diffInHours + " " + getHoursString(diffInHours) + " назад";
                } else {
                    long diffInDays = diffInHours / 24;
                    if (diffInDays < 7) {
                        return diffInDays + " " + getDaysString(diffInDays) + " назад";
                    } else {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
                        return sdf.format(timestamp);
                    }
                }
            }
        }
        
        private String getMinutesString(long minutes) {
            if (minutes % 10 == 1 && minutes % 100 != 11) {
                return "минуту";
            } else if ((minutes % 10 == 2 || minutes % 10 == 3 || minutes % 10 == 4) && 
                      (minutes % 100 < 10 || minutes % 100 >= 20)) {
                return "минуты";
            } else {
                return "минут";
            }
        }
        
        private String getHoursString(long hours) {
            if (hours % 10 == 1 && hours % 100 != 11) {
                return "час";
            } else if ((hours % 10 == 2 || hours % 10 == 3 || hours % 10 == 4) && 
                      (hours % 100 < 10 || hours % 100 >= 20)) {
                return "часа";
            } else {
                return "часов";
            }
        }
        
        private String getDaysString(long days) {
            if (days % 10 == 1 && days % 100 != 11) {
                return "день";
            } else if ((days % 10 == 2 || days % 10 == 3 || days % 10 == 4) && 
                      (days % 100 < 10 || days % 100 >= 20)) {
                return "дня";
            } else {
                return "дней";
            }
        }
    }
} 