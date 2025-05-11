package com.example.ultai.ultai.parser;

import android.util.Log;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ConnectionPool;
import okhttp3.CacheControl;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.net.URLEncoder;
import org.json.JSONObject;

public class WebParser {
    private static final String TAG = "WebParser";
    private final OkHttpClient client;
    private static final Map<String, String> USER_AGENTS = new HashMap<>();
    private final ExecutorService executorService;
    
    static {
        USER_AGENTS.put("Chrome", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
        USER_AGENTS.put("Firefox", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:89.0) Gecko/20100101 Firefox/89.0");
        USER_AGENTS.put("Safari", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.1.1 Safari/605.1.15");
    }

    public WebParser() {
        client = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .connectionPool(new ConnectionPool(5, 30, TimeUnit.SECONDS))
                .retryOnConnectionFailure(true)
                .build();
        
        executorService = Executors.newFixedThreadPool(3); // Создаем пул потоков для параллельных запросов
    }

    public String getWeatherInfo(String city) {
        try {
            // Парсинг Яндекс.Погода
            String yandexUrl = "https://yandex.ru/pogoda/" + city;
            Document doc = getDocument(yandexUrl);
            
            if (doc != null) {
                Element weatherBlock = doc.selectFirst(".temp");
                Element feelsLike = doc.selectFirst(".feels-like");
                Element humidity = doc.selectFirst(".humidity");
                Element wind = doc.selectFirst(".wind");
                Element pressure = doc.selectFirst(".pressure");
                
                if (weatherBlock == null) {
                    return "🌡️ Не удалось получить информацию о погоде для города " + city;
                }
                
                StringBuilder result = new StringBuilder();
                result.append("🌡️ Погода в ").append(city).append(":\n");
                result.append("• Температура: ").append(weatherBlock != null ? weatherBlock.text() : "Н/Д").append("\n");
                result.append("• Ощущается как: ").append(feelsLike != null ? feelsLike.text() : "Н/Д").append("\n");
                result.append("• Влажность: ").append(humidity != null ? humidity.text() : "Н/Д").append("\n");
                result.append("• Ветер: ").append(wind != null ? wind.text() : "Н/Д").append("\n");
                result.append("• Давление: ").append(pressure != null ? pressure.text() : "Н/Д");
                
                return result.toString();
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при парсинге погоды: " + e.getMessage());
        }
        return "🌡️ Не удалось получить информацию о погоде. Пожалуйста, попробуйте позже.";
    }

    public String getCurrencyRates() {
        try {
            // Парсинг курсов валют
            String url = "https://www.cbr.ru/currency_base/daily/";
            Document doc = getDocument(url);
            
            if (doc != null) {
                Elements rates = doc.select(".data tr");
                StringBuilder result = new StringBuilder();
                result.append("💱 Курсы валют:\n");
                
                for (Element rate : rates) {
                    if (rate.selectFirst("td") != null) {
                        String currency = rate.select("td").get(1).text();
                        String value = rate.select("td").get(4).text();
                        String change = rate.select("td").get(5).text();
                        result.append(String.format("• %s: %s ₽ (%s%%)\n", 
                            currency, value, change));
                    }
                }
                
                return result.toString();
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при парсинге курсов валют: " + e.getMessage());
        }
        return "💱 Не удалось получить информацию о курсах валют. Пожалуйста, попробуйте позже.";
    }

    public String getStockPrices(String symbol) {
        try {
            // Парсинг цен акций
            String url = "https://finance.yahoo.com/quote/" + symbol;
            Document doc = getDocument(url);
            
            if (doc != null) {
                Element price = doc.selectFirst("[data-test=qsp-price]");
                Element change = doc.selectFirst("[data-test=qsp-price-change]");
                Element volume = doc.selectFirst("[data-test=TD_VOLUME-value]");
                
                StringBuilder result = new StringBuilder();
                result.append("📈 Акции ").append(symbol).append(":\n");
                result.append("• Цена: $").append(price.text()).append("\n");
                result.append("• Изменение: ").append(change.text()).append("\n");
                result.append("• Объем: ").append(volume.text());
                
                return result.toString();
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при парсинге цен акций: " + e.getMessage());
        }
        return "📈 Не удалось получить информацию о ценах акций " + symbol + ". Пожалуйста, проверьте тикер и попробуйте позже.";
    }

    public String getNews(String query) {
        try {
            // Парсинг новостей
            String url = "https://news.google.com/search?q=" + query + "&hl=ru&gl=RU&ceid=RU:ru";
            Document doc = getDocument(url);
            
            if (doc != null) {
                Elements news = doc.select("article");
                StringBuilder result = new StringBuilder();
                result.append("📰 Новости по запросу \"").append(query).append("\":\n\n");
                
                for (int i = 0; i < Math.min(5, news.size()); i++) {
                    Element article = news.get(i);
                    String title = article.select("h3").text();
                    String link = article.select("a").attr("href");
                    String source = article.select(".QmrVtf").text();
                    String time = article.select(".hvbAAd").text();
                    
                    result.append(String.format("%d. %s\n", i + 1, title));
                    result.append("   Источник: ").append(source).append(" | ").append(time).append("\n");
                    result.append("   Ссылка: ").append(link).append("\n\n");
                }
                
                return result.toString();
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при парсинге новостей: " + e.getMessage());
        }
        return "📰 Не удалось получить новости по запросу \"" + query + "\". Пожалуйста, попробуйте позже или измените запрос.";
    }

    public String getGeneralInfo(String query) {
        try {
            // Парсинг Google Knowledge Graph
            String url = "https://www.google.com/search?q=" + query;
            Document doc = getDocument(url);
            
            if (doc != null) {
                StringBuilder result = new StringBuilder();
                
                // Получаем основную информацию
                Element knowledgeGraph = doc.selectFirst(".kno-rdesc");
                if (knowledgeGraph != null) {
                    result.append("📚 Основная информация:\n");
                    result.append(knowledgeGraph.text()).append("\n\n");
                }
                
                // Получаем последние новости
                Elements news = doc.select(".g");
                if (!news.isEmpty()) {
                    result.append("📰 Последние новости:\n");
                    for (int i = 0; i < Math.min(3, news.size()); i++) {
                        Element article = news.get(i);
                        String title = article.select("h3").text();
                        String link = article.select("a").attr("href");
                        String source = article.select(".VuuXrf").text();
                        result.append(String.format("%d. %s\n", i + 1, title));
                        result.append("   Источник: ").append(source).append("\n");
                        result.append("   Ссылка: ").append(link).append("\n\n");
                    }
                }
                
                // Получаем статистику
                Elements stats = doc.select(".Z0LcW");
                if (!stats.isEmpty()) {
                    result.append("📊 Статистика:\n");
                    for (Element stat : stats) {
                        result.append("• ").append(stat.text()).append("\n");
                    }
                }
                
                if (result.length() > 0) {
                    return result.toString();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при получении общей информации: " + e.getMessage());
        }
        return "🔍 Не удалось найти информацию по запросу \"" + query + "\". Попробуйте уточнить запрос.";
    }

    public String getWikipediaInfo(String query) {
        try {
            // Парсинг Wikipedia
            String url = "https://ru.wikipedia.org/wiki/" + query;
            Document doc = getDocument(url);
            
            if (doc != null) {
                StringBuilder result = new StringBuilder();
                result.append("📖 Информация из Wikipedia:\n\n");
                
                // Получаем краткое описание
                Element summary = doc.selectFirst(".mw-parser-output > p");
                if (summary != null) {
                    result.append(summary.text()).append("\n\n");
                }
                
                // Получаем основные факты
                Elements facts = doc.select(".infobox tr");
                if (!facts.isEmpty()) {
                    result.append("📋 Основные факты:\n");
                    for (Element fact : facts) {
                        String label = fact.select("th").text();
                        String value = fact.select("td").text();
                        if (!label.isEmpty() && !value.isEmpty()) {
                            result.append("• ").append(label).append(": ").append(value).append("\n");
                        }
                    }
                }
                
                if (result.length() > 30) { // Проверяем, что есть какая-то полезная информация
                    return result.toString();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при получении информации из Wikipedia: " + e.getMessage());
        }
        return "📖 Не удалось найти информацию в Wikipedia по запросу \"" + query + "\". Попробуйте изменить запрос.";
    }

    public String getMarketInfo(String query) {
        try {
            // Парсинг рыночной информации
            String url = "https://www.google.com/finance/quote/" + query;
            Document doc = getDocument(url);
            
            if (doc != null) {
                StringBuilder result = new StringBuilder();
                result.append("📈 Рыночная информация:\n\n");
                
                // Получаем текущую цену
                Element price = doc.selectFirst("[data-last-price]");
                if (price != null) {
                    result.append("• Текущая цена: ").append(price.text()).append("\n");
                }
                
                // Получаем изменение
                Element change = doc.selectFirst("[data-price-change]");
                if (change != null) {
                    result.append("• Изменение: ").append(change.text()).append("\n");
                }
                
                // Получаем объем торгов
                Element volume = doc.selectFirst("[data-volume]");
                if (volume != null) {
                    result.append("• Объем торгов: ").append(volume.text()).append("\n");
                }
                
                if (result.length() > 30) { // Проверяем, что есть какая-то полезная информация
                    return result.toString();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при получении рыночной информации: " + e.getMessage());
        }
        return "📈 Не удалось получить рыночную информацию по запросу \"" + query + "\". Пожалуйста, уточните запрос.";
    }

    public String getSportsInfo(String query) {
        try {
            // Парсинг спортивной информации
            String url = "https://www.google.com/search?q=" + query + "&tbm=nws&tbs=sbd:1";
            Document doc = getDocument(url);
            
            if (doc != null) {
                StringBuilder result = new StringBuilder();
                result.append("⚽ Спортивные новости:\n\n");
                
                Elements news = doc.select(".g");
                for (int i = 0; i < Math.min(5, news.size()); i++) {
                    Element article = news.get(i);
                    String title = article.select("h3").text();
                    String link = article.select("a").attr("href");
                    String source = article.select(".VuuXrf").text();
                    String time = article.select(".LfVVr").text();
                    
                    result.append(String.format("%d. %s\n", i + 1, title));
                    result.append("   Источник: ").append(source).append(" | ").append(time).append("\n");
                    result.append("   Ссылка: ").append(link).append("\n\n");
                }
                
                if (result.length() > 30) { // Проверяем, что есть какая-то полезная информация
                    return result.toString();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при получении спортивной информации: " + e.getMessage());
        }
        return "⚽ Не удалось получить спортивные новости по запросу \"" + query + "\". Пожалуйста, попробуйте позже.";
    }

    public String getTechInfo(String query) {
        try {
            // Парсинг технологической информации
            String url = "https://www.google.com/search?q=" + query + "&tbm=nws&tbs=sbd:1";
            Document doc = getDocument(url);
            
            if (doc != null) {
                StringBuilder result = new StringBuilder();
                result.append("💻 Технологические новости:\n\n");
                
                Elements news = doc.select(".g");
                for (int i = 0; i < Math.min(5, news.size()); i++) {
                    Element article = news.get(i);
                    String title = article.select("h3").text();
                    String link = article.select("a").attr("href");
                    String source = article.select(".VuuXrf").text();
                    String time = article.select(".LfVVr").text();
                    
                    result.append(String.format("%d. %s\n", i + 1, title));
                    result.append("   Источник: ").append(source).append(" | ").append(time).append("\n");
                    result.append("   Ссылка: ").append(link).append("\n\n");
                }
                
                if (result.length() > 30) { // Проверяем, что есть какая-то полезная информация
                    return result.toString();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при получении технологической информации: " + e.getMessage());
        }
        return "💻 Не удалось получить технологические новости по запросу \"" + query + "\". Пожалуйста, попробуйте позже.";
    }

    /**
     * Получает информацию о фотографиях по заданной теме
     * @param topic тема для поиска фотографий
     * @return информация о фотографиях
     */
    public String getPhotoInfo(String topic) {
        try {
            String url = "https://www.google.com/search?q=" + URLEncoder.encode(topic + " фото", "UTF-8") + "&tbm=isch";
            Document doc = getDocument(url);
            
            if (doc == null) {
                return "Не удалось найти фотографии по теме: " + topic;
            }
            
            // Получаем информацию о количестве найденных изображений
            Elements stats = doc.select("div#result-stats");
            String statsText = stats.isEmpty() ? "" : stats.first().text();
            
            // Получаем описания изображений
            Elements descriptions = doc.select("div.rg_meta");
            List<String> imageDescriptions = new ArrayList<>();
            
            for (Element description : descriptions) {
                try {
                    String json = description.text();
                    JSONObject jsonObject = new JSONObject(json);
                    String imageDesc = jsonObject.optString("pt", "");
                    if (!imageDesc.isEmpty()) {
                        imageDescriptions.add(imageDesc);
                    }
                    
                    // Ограничиваем количество описаний
                    if (imageDescriptions.size() >= 5) {
                        break;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Ошибка при парсинге описания изображения", e);
                }
            }
            
            // Формируем ответ
            StringBuilder result = new StringBuilder();
            result.append("Информация о фотографиях по теме \"").append(topic).append("\":\n\n");
            
            if (!statsText.isEmpty()) {
                result.append("Найдено: ").append(statsText).append("\n\n");
            }
            
            result.append("Популярные запросы по этой теме:\n");
            Elements relatedSearches = doc.select("div.brs_col a");
            int relatedCount = 0;
            
            for (Element related : relatedSearches) {
                String relatedText = related.text();
                if (!relatedText.isEmpty()) {
                    result.append("- ").append(relatedText).append("\n");
                    relatedCount++;
                    
                    if (relatedCount >= 5) {
                        break;
                    }
                }
            }
            
            if (relatedCount == 0) {
                result.append("- Нет связанных запросов\n");
            }
            
            result.append("\nДля просмотра фотографий по теме \"").append(topic).append("\" рекомендуем использовать следующие ресурсы:\n");
            result.append("1. Google Картинки: https://images.google.com/\n");
            result.append("2. Яндекс Картинки: https://yandex.ru/images/\n");
            result.append("3. Unsplash: https://unsplash.com/\n");
            result.append("4. Pinterest: https://pinterest.com/\n");
            
            return result.toString();
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при получении информации о фотографиях", e);
            return "Не удалось найти информацию о фотографиях по теме: " + topic;
        }
    }

    private Document getDocument(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", USER_AGENTS.get("Chrome"))
                .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                .addHeader("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3")
                .cacheControl(new CacheControl.Builder().maxStale(10, TimeUnit.MINUTES).build()) // Добавляем кэширование
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return Jsoup.parse(response.body().string());
            }
        }
        return null;
    }

    public void shutdown() {
        client.dispatcher().executorService().shutdown();
        executorService.shutdown();
    }
} 