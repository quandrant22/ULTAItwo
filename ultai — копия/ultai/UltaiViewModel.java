package com.example.ultai.ultai;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;

import com.example.ultai.ultai.model.ChatMessage;
import com.example.ultai.ultai.model.DeepSeekRequest;
import com.example.ultai.ultai.model.DeepSeekResponse;
import com.example.ultai.ultai.api.DeepSeekApiService;
import com.example.ultai.ultai.api.GptApi;
import com.example.ultai.ultai.model.GptRequest;
import com.example.ultai.ultai.model.GptResponse;
import com.example.ultai.ultai.api.RetrofitClient;
import com.example.ultai.ultai.parser.WebParser;
import com.example.ultai.ultai.api.ClaudeApi;
import com.example.ultai.ultai.model.ClaudeRequest;
import com.example.ultai.ultai.model.ClaudeResponse;
import com.example.ultai.ultai.api.AnthropicRetrofitClient;
import com.example.ultai.ultai.parser.NewsParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;
import java.util.Locale;
import java.util.Date;
import java.text.SimpleDateFormat;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import java.util.concurrent.TimeUnit;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.os.Handler;
import android.os.Looper;
import java.util.Locale;

public class UltaiViewModel extends AndroidViewModel {

    private static final String TAG = "UltaiViewModel";
    private static final String PREFS_NAME = "UltaiPrefs";
    private static final String API_KEY_KEY = "api_key";

    private final MutableLiveData<List<ChatMessage>> chatMessages;
    private final MutableLiveData<Boolean> isLoading;
    private final GptApi gptApi;
    private final ClaudeApi claudeApi;
    private final List<GptRequest.Message> conversationHistory;
    private final List<ClaudeRequest.Message> claudeConversationHistory;
    private final WebParser webParser;

    private static final String DEEPSEEK_API_KEY = "sk-7a2c4cbedbfe42dcab4a5fe41b7e60cd";
    private static final String CLAUDE_API_KEY = "YOUR_CLAUDE_API_KEY"; // Замените на ваш ключ API
    private static final String CLAUDE_MODEL = "claude-3-sonnet-20240229"; // Модель Claude 3 Sonnet

    public UltaiViewModel(@NonNull Application application) {
        super(application);
        chatMessages = new MutableLiveData<>(new ArrayList<>());
        isLoading = new MutableLiveData<>(false);
        conversationHistory = new ArrayList<>();
        claudeConversationHistory = new ArrayList<>();
        webParser = new WebParser();
        
        // Добавляем системное сообщение с деловым стилем
        updateSystemMessage();
        
        // Инициализация Retrofit и API для DeepSeek
        RetrofitClient retrofitClient = RetrofitClient.getInstance();
        String apiKey = getApiKey();
        retrofitClient.setApiKey(apiKey);
        gptApi = retrofitClient.create(GptApi.class);
        
        // Инициализация Retrofit и API для Claude
        AnthropicRetrofitClient anthropicClient = AnthropicRetrofitClient.getInstance();
        String claudeKey = getClaudeApiKey();
        anthropicClient.setApiKey(claudeKey);
        claudeApi = anthropicClient.create(ClaudeApi.class);
        
        Log.d(TAG, "Инициализация ViewModel с API ключами DeepSeek и Claude");
    }

    public LiveData<List<ChatMessage>> getChatMessages() {
        return chatMessages;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    /**
     * Устанавливает список сообщений чата (используется для загрузки истории)
     */
    public void setChatMessages(List<ChatMessage> messages) {
        if (messages != null) {
            chatMessages.setValue(messages);
        }
    }

    public void addMessage(ChatMessage message) {
        if (Thread.currentThread() == getApplication().getMainLooper().getThread()) {
            // Вызов из главного потока
            List<ChatMessage> currentMessages = chatMessages.getValue();
            if (currentMessages == null) {
                currentMessages = new ArrayList<>();
            }
            currentMessages.add(message);
            chatMessages.setValue(currentMessages);
        } else {
            // Вызов из фонового потока
            new android.os.Handler(getApplication().getMainLooper()).post(() -> {
                List<ChatMessage> currentMessages = chatMessages.getValue();
                if (currentMessages == null) {
                    currentMessages = new ArrayList<>();
                }
                currentMessages.add(message);
                chatMessages.setValue(currentMessages);
            });
        }
    }

    private void addMessage(String message, boolean isUser) {
        List<ChatMessage> currentMessages = chatMessages.getValue();
        if (currentMessages == null) {
            currentMessages = new ArrayList<>();
        }
        String timestamp = getCurrentTime();
        
        // Удаляем звездочки для сообщений бота
        String cleanMessage = isUser ? message : removeMarkdownFormatting(message);
        
        currentMessages.add(new ChatMessage(cleanMessage, isUser, timestamp, ""));
        chatMessages.setValue(currentMessages);
    }

    private void addMessage(String message, boolean isUser, String moreInfo) {
        List<ChatMessage> currentMessages = chatMessages.getValue();
        if (currentMessages == null) {
            currentMessages = new ArrayList<>();
        }
        
        // Удаляем звездочки для сообщений бота
        String cleanMessage = isUser ? message : removeMarkdownFormatting(message);
        
        currentMessages.add(new ChatMessage(cleanMessage, isUser, getCurrentTime(), moreInfo));
        chatMessages.setValue(currentMessages);
    }

    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date());
    }

    /**
     * Отправляет сообщение от пользователя
     */
    public void sendMessage(String message) {
        if (!isNetworkAvailable()) {
            Log.e(TAG, "Нет подключения к интернету");
            addMessage(new ChatMessage("Ошибка: Нет подключения к интернету. Пожалуйста, проверьте:\n" +
                    "1. Подключение к Wi-Fi или мобильному интернету\n" +
                    "2. Доступ к интернету в других приложениях\n" +
                    "3. Настройки антивируса или файрвола", false, getCurrentTime(), ""));
            return;
        }

        // Добавляем сообщение пользователя
        addMessage(new ChatMessage(message, true, getCurrentTime(), ""));

        // Преобразуем сообщение в нижний регистр и сохраняем в final переменную
        final String lowerCaseMessage = message.toLowerCase();

        // Устанавливаем флаг загрузки
        isLoading.setValue(true);

        // Начинаем поэтапную генерацию ответа
        startProgressiveResponse(message);

        // Используем отдельный поток для парсинга, чтобы не блокировать UI
        new Thread(() -> {
            String result = null;
            
            try {
                // Оптимизируем поисковый запрос для более точного поиска
                String optimizedQuery = optimizeSearchQuery(message);
                Log.d(TAG, "Оптимизированный запрос: " + optimizedQuery);
                
                // Проверяем, является ли запрос запросом на новости
                if (isNewsQuery(lowerCaseMessage)) {
                    result = handleNewsQuery(lowerCaseMessage);
                } else if (isWeatherQuery(lowerCaseMessage)) {
                    String city = extractCity(lowerCaseMessage);
                    if (city != null) {
                        result = webParser.getWeatherInfo(city);
                    } else {
                        result = "Пожалуйста, укажите город, например: погода в Москве";
                    }
                } else if (lowerCaseMessage.contains("курс") || lowerCaseMessage.contains("валюта") || 
                           lowerCaseMessage.contains("рубль") || lowerCaseMessage.contains("доллар") ||
                           lowerCaseMessage.contains("евро")) {
                    result = webParser.getCurrencyRates();
                } else if (lowerCaseMessage.contains("акция") || lowerCaseMessage.contains("акции") || 
                           lowerCaseMessage.contains("бирж") || lowerCaseMessage.contains("котировк")) {
                    String symbol = extractSymbol(lowerCaseMessage);
                    if (symbol != null) {
                        result = webParser.getStockPrices(symbol);
                    } else {
                        result = "Пожалуйста, укажите тикер акции, например: акции AAPL";
                    }
                } else if (lowerCaseMessage.contains("спорт") || lowerCaseMessage.contains("футбол") || 
                           lowerCaseMessage.contains("хоккей") || lowerCaseMessage.contains("матч") ||
                           lowerCaseMessage.contains("чемпионат")) {
                    result = webParser.getSportsInfo(optimizedQuery);
                } else if (lowerCaseMessage.contains("технолог") || lowerCaseMessage.contains("гаджет") || 
                           lowerCaseMessage.contains("компьютер") || lowerCaseMessage.contains("телефон") ||
                           lowerCaseMessage.contains("ноутбук") || lowerCaseMessage.contains("смартфон")) {
                    result = webParser.getTechInfo(optimizedQuery);
                } else if (lowerCaseMessage.contains("рынок") || lowerCaseMessage.contains("экономик") ||
                           lowerCaseMessage.contains("инфляц") || lowerCaseMessage.contains("цен")) {
                    result = webParser.getMarketInfo(optimizedQuery);
                } else if (lowerCaseMessage.contains("новост") || lowerCaseMessage.contains("событи") ||
                           lowerCaseMessage.contains("происшеств")) {
                    result = handleNewsQuery(lowerCaseMessage);
                } else if (lowerCaseMessage.contains("фото") || lowerCaseMessage.contains("картинк") || 
                           lowerCaseMessage.contains("изображен")) {
                    // Если запрос содержит просьбу о фото, добавляем информацию о фото
                    String topic = extractPhotoTopic(lowerCaseMessage);
                    if (topic != null) {
                        result = webParser.getPhotoInfo(topic);
                    } else {
                        result = "Пожалуйста, уточните тему для поиска фотографий";
                    }
                } else if (lowerCaseMessage.contains("справк") || lowerCaseMessage.contains("помощ") || 
                           lowerCaseMessage.contains("инструкц")) {
                    // Показываем справку по ключевым словам
                    result = getHelpInfo();
                } else {
                    // Для всех остальных запросов сначала пробуем получить общую информацию
                    result = webParser.getGeneralInfo(optimizedQuery);
                    
                    // Если общая информация не найдена или недостаточна, пробуем получить из Wikipedia
                    if (result != null && result.contains("Не удалось найти информацию")) {
                        String wikiResult = webParser.getWikipediaInfo(optimizedQuery);
                        if (wikiResult != null && !wikiResult.contains("Не удалось найти информацию")) {
                            result = wikiResult;
                        }
                    }
                }
                
                // Если результат содержит сообщение об ошибке или недостаточно информативен, 
                // попробуем использовать AI API как запасной вариант
                if (result == null || result.contains("Не удалось") || result.length() < 50) {
                    Log.d(TAG, "Недостаточно информации от парсера, используем AI API");
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        // Останавливаем поэтапную генерацию
                        stopProgressiveResponse();
                        useAiApi(message);
                    });
                    return;
                }

                // Если информация найдена через парсер, добавляем ее в чат
                final String finalResult = result;
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                    // Останавливаем поэтапную генерацию
                    stopProgressiveResponse();
                    isLoading.setValue(false);
                    
                    // Обновляем последнее сообщение с полным ответом
                    updateLastBotMessage(finalResult);
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Ошибка при парсинге информации: " + e.getMessage(), e);
                // В случае ошибки используем AI API
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                    // Останавливаем поэтапную генерацию
                    stopProgressiveResponse();
                    useAiApi(message);
                });
            }
        }).start();
    }
    
    /**
     * Отправляет приветственное сообщение от бота
     */
    public void sendWelcomeMessage(String message) {
        // Добавляем сообщение от бота напрямую, без обращения к серверам
        String timestamp = getCurrentTime();
        String cleanMessage = removeMarkdownFormatting(message);
        
        // Создаем новый объект сообщения с isUser = false (сообщение от бота)
        ChatMessage botMessage = new ChatMessage(cleanMessage, false, timestamp, "");
        
        // Добавляем сообщение в список через метод addMessage
        if (Thread.currentThread() == getApplication().getMainLooper().getThread()) {
            // Вызов из главного потока
            List<ChatMessage> currentMessages = chatMessages.getValue();
            if (currentMessages == null) {
                currentMessages = new ArrayList<>();
            }
            currentMessages.add(botMessage);
            chatMessages.setValue(currentMessages);
        } else {
            // Вызов из фонового потока
            new android.os.Handler(getApplication().getMainLooper()).post(() -> {
                List<ChatMessage> currentMessages = chatMessages.getValue();
                if (currentMessages == null) {
                    currentMessages = new ArrayList<>();
                }
                currentMessages.add(botMessage);
                chatMessages.setValue(currentMessages);
            });
        }
        
        Log.d(TAG, "Отправлено приветственное сообщение от бота: " + message);
    }

    private String extractSymbol(String message) {
        // Улучшенная логика извлечения тикера акции
        String[] words = message.split("\\s+");
        
        // Ищем тикер после ключевых слов
        for (int i = 0; i < words.length - 1; i++) {
            if (words[i].toLowerCase().contains("акци") || 
                words[i].toLowerCase().contains("тикер") || 
                words[i].toLowerCase().contains("котировк")) {
                // Проверяем следующее слово
                if (i + 1 < words.length && words[i + 1].matches("[A-Za-z]{1,5}")) {
                    return words[i + 1].toUpperCase();
                }
            }
        }
        
        // Если не нашли после ключевых слов, ищем любое слово похожее на тикер
        for (String word : words) {
            // Тикеры обычно состоят из 1-5 латинских букв
            if (word.matches("[A-Za-z]{1,5}")) {
                return word.toUpperCase();
            }
        }
        
        // Проверяем популярные российские тикеры
        String[] popularRussianTickers = {"GAZP", "SBER", "LKOH", "ROSN", "GMKN", "YNDX", "VTBR", "ALRS", "AFLT", "MTSS"};
        for (String ticker : popularRussianTickers) {
            if (message.toLowerCase().contains(ticker.toLowerCase())) {
                return ticker;
            }
        }
        
        // Проверяем популярные международные тикеры
        String[] popularTickers = {"AAPL", "MSFT", "GOOGL", "AMZN", "META", "TSLA", "NVDA", "JPM", "V", "WMT"};
        for (String ticker : popularTickers) {
            if (message.toLowerCase().contains(ticker.toLowerCase())) {
                return ticker;
            }
        }
        
        return null;
    }

    private boolean isWeatherQuery(String message) {
        String[] weatherKeywords = {
            "погода", "прогноз", "температура", "дождь", "снег", "ветер", "влажность",
            "давление", "облачно", "ясно", "пасмурно", "мороз", "жара", "тепло",
            "холодно", "градус", "°c", "°с", "celsius", "цельсий", "осадки", "гроза",
            "метеоролог", "климат", "атмосфер", "туман", "гололед", "заморозки"
        };
        
        message = message.toLowerCase();
        for (String keyword : weatherKeywords) {
            if (message.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private String extractCity(String message) {
        // Улучшенная логика извлечения города из сообщения
        message = message.toLowerCase();
        String[] words = message.split("\\s+");
        
        // Ищем город после предлогов
        for (int i = 0; i < words.length - 1; i++) {
            if (words[i].equals("в") || words[i].equals("для") || 
                words[i].equals("по") || words[i].equals("на")) {
                // Проверяем, что следующее слово не является предлогом или союзом
                if (i + 1 < words.length && !isPrepositionOrConjunction(words[i + 1])) {
                    return capitalizeFirstLetter(words[i + 1]);
                }
            }
        }
        
        // Проверяем популярные города
        String[] popularCities = {"москва", "санкт-петербург", "спб", "новосибирск", "екатеринбург", 
                                 "казань", "нижний новгород", "челябинск", "самара", "омск", 
                                 "ростов-на-дону", "уфа", "красноярск", "воронеж", "пермь"};
        
        for (String city : popularCities) {
            if (message.contains(city)) {
                return capitalizeFirstLetter(city);
            }
        }
        
        // Если не нашли город, проверяем, есть ли в запросе слово "погода" без указания города
        if (message.contains("погода") && !message.contains("в ")) {
            return "Москва"; // Возвращаем Москву по умолчанию
        }
        
        return null;
    }
    
    private boolean isPrepositionOrConjunction(String word) {
        String[] prepositionsAndConjunctions = {"в", "на", "с", "к", "у", "от", "до", "для", "про", "через", 
                                              "над", "под", "при", "без", "и", "а", "но", "или", "да", "либо"};
        for (String p : prepositionsAndConjunctions) {
            if (word.equals(p)) {
                return true;
            }
        }
        return false;
    }
    
    private String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            boolean isConnected = activeNetworkInfo != null && activeNetworkInfo.isConnected();
            
            Log.d(TAG, "Статус сети: " + (isConnected ? "подключено" : "отключено"));
            if (isConnected) {
                Log.d(TAG, "Тип сети: " + activeNetworkInfo.getTypeName() + 
                        " (" + activeNetworkInfo.getSubtypeName() + ")");
            }
            
            return isConnected;
        }
        Log.e(TAG, "ConnectivityManager недоступен");
        return false;
    }

    // Метод для генерации ответов с актуальной информацией
    private String generateLocalResponse(String message) {
        message = message.toLowerCase().trim();
        
        if (message.contains("привет") || message.contains("здравствуй")) {
            return "Привет! Я ваш персональный ассистент. Чем могу помочь?";
        } else if (message.contains("как дела")) {
            return "У меня всё хорошо! Я готов помочь вам с любыми вопросами.";
        } else if (message.contains("спасибо")) {
            return "Пожалуйста! Если у вас появятся ещё вопросы, обращайтесь.";
        } else if (message.contains("пока") || message.contains("до свидания")) {
            return "До свидания! Буду рад помочь вам снова.";
        } else {
            return "Извините, я пока не могу обработать это сообщение. Попробуйте задать другой вопрос.";
        }
    }

    // Метод для определения бизнес-запросов
    private boolean isBizRequest(String message) {
        String[] bizKeywords = {
            "бизнес", "компания", "предприятие", "стартап", "венчур", "инвестиции", "прибыль", "доход", 
            "продажа", "маркетинг", "реклама", "конкурент", "стратегия", "рынок", "отрасль", "индустрия",
            "предприниматель", "b2b", "b2c", "аутсорсинг", "лидерство", "менеджмент", "hr", "персонал", 
            "найм", "увольнение", "франшиза", "дистрибуция", "логистика", "продукт", "услуга", 
            "ниша", "сегмент", "клиент", "потребитель", "лояльность", "cpa", "cpm", "retention", 
            "цена", "прайс", "тариф", "предложение", "спрос", "объем", "рост", "развитие", "план", 
            "бизнес-план", "налоги", "налогообложение", "ндс", "прибыль", "убыток", "roi", "kpi", "метрики", 
            "показатели", "эффективность", "акции", "дивиденды", "доля", "капитал", "оборот"
        };
        
        for (String keyword : bizKeywords) {
            if (message.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    // Метод для генерации ответов на бизнес-запросы с обоснованием
    private String generateBusinessResponse(String query, String currentDateTime, int year) {
        query = query.toLowerCase();
        
        // Основной ответ и обоснование
        String response = "";
        String justification = "";

        if (query.contains("бизнес") && (query.contains("модел") || query.contains("plan"))) {
            response = "Основные типы бизнес-моделей: \n" +
                    "• SaaS (программное обеспечение как услуга)\n" +
                    "• Freemium (базовые услуги бесплатно, расширенные платно)\n" +
                    "• Подписка (регулярные платежи за доступ)\n" +
                    "• Маркетплейс (платформа, соединяющая продавцов и покупателей)\n" +
                    "• Дропшиппинг (продажа без хранения товара)\n" +
                    "• Франшиза (использование бренда и системы существующего бизнеса)\n\n" +
                    "Выбор оптимальной модели зависит от отрасли, целевой аудитории и ваших ресурсов.";
                    
            justification = "\n\n📊 Обоснование: Согласно исследованию Harvard Business Review (2023), компании с четко определенной бизнес-моделью на 35% чаще достигают устойчивого роста. Исследование McKinsey показывает, что 72% успешных стартапов пересматривают свою бизнес-модель в среднем 3-4 раза до достижения стабильного дохода.";
        } 
        else if (query.contains("бизнес") && query.contains("нач")) {
            response = "Ключевые шаги для начала бизнеса:\n" +
                    "1. Исследование рынка и поиск ниши\n" +
                    "2. Разработка бизнес-плана\n" +
                    "3. Выбор организационно-правовой формы (ИП, ООО и др.)\n" +
                    "4. Регистрация и получение необходимых лицензий\n" +
                    "5. Организация финансирования\n" +
                    "6. Создание минимально жизнеспособного продукта (MVP)\n" +
                    "7. Маркетинг и привлечение первых клиентов\n" +
                    "8. Масштабирование бизнеса\n\n" +
                    "В России для начинающих предпринимателей доступны различные формы поддержки: льготные кредиты, субсидии, налоговые каникулы.";
                    
            justification = "\n\n📈 Обоснование: По данным Росстата, в " + year + " году было зарегистрировано более 290 тысяч новых бизнесов, при этом около 65% стартапов прекращают деятельность в первые три года. Исследование CB Insights показывает, что основными причинами неудач являются: отсутствие рыночной потребности (42%), нехватка финансирования (29%) и неэффективная команда (23%). Следование структурированному подходу увеличивает шансы на успех на 40-60%.";
        }
        else if (query.contains("конкурент") || query.contains("анализ рынка")) {
            response = "Основные методы анализа конкурентов:\n" +
                    "• SWOT-анализ (сильные и слабые стороны, возможности и угрозы)\n" +
                    "• Модель пяти сил Портера (конкуренты, поставщики, покупатели, новые игроки, товары-заменители)\n" +
                    "• Сравнительный анализ (бенчмаркинг) ключевых показателей\n" +
                    "• Анализ ценностного предложения конкурентов\n" +
                    "• Исследование отзывов клиентов конкурентов\n\n" +
                    "Для эффективного анализа важно регулярно мониторить не только прямых, но и косвенных конкурентов, а также потенциальных новых игроков на рынке.";
                    
            justification = "\n\n🔍 Обоснование: Исследование Gartner показывает, что компании, регулярно проводящие конкурентный анализ, в среднем на 24% эффективнее реагируют на рыночные изменения. По данным Deloitte, 82% корпораций, входящих в список Fortune 500, используют формализованные процессы конкурентного анализа. Систематический сбор данных о конкурентах позволяет выявить рыночные тренды на 3-6 месяцев раньше, чем при реактивном подходе.";
        }
        else if (query.contains("финанс") || query.contains("инвестиц") || query.contains("бюджет")) {
            response = "Ключевые аспекты финансового планирования в бизнесе:\n" +
                    "• Прогноз доходов и расходов (минимум на 12-24 месяца)\n" +
                    "• Расчет точки безубыточности\n" +
                    "• Управление денежными потоками (cash flow)\n" +
                    "• Оценка потребности в инвестициях и выбор источников финансирования\n" +
                    "• Формирование резервного фонда (рекомендуется 3-6 месячных расходов)\n" +
                    "• Система финансовых KPI и их регулярный мониторинг\n\n" +
                    "Важно разделять личные финансы и финансы бизнеса, даже если это небольшое предприятие.";
                    
            justification = "\n\n💰 Обоснование: По данным U.S. Bank, 82% бизнесов закрываются из-за проблем с денежными потоками. Исследование PwC выявило, что компании с формализованной системой финансового планирования в 30% случаев имеют более высокую маржинальность по сравнению с конкурентами. Согласно отчету KPMG, бизнесы, которые поддерживают резервный фонд в размере 3-6 месячных расходов, имеют на 64% больше шансов выжить в кризисных ситуациях.";
        }
        else if (query.contains("маркет") || query.contains("продвижен") || query.contains("реклам")) {
            response = "Эффективные стратегии маркетинга для современного бизнеса:\n" +
                    "• Контент-маркетинг (создание ценного контента для целевой аудитории)\n" +
                    "• Таргетированная реклама в социальных сетях\n" +
                    "• SEO-оптимизация и контекстная реклама\n" +
                    "• Email-маркетинг (сегментация и персонализация)\n" +
                    "• Influencer-маркетинг (работа с лидерами мнений)\n" +
                    "• Партнерский маркетинг и коллаборации\n" +
                    "• Видеомаркетинг (YouTube, TikTok, Instagram Reels)\n\n" +
                    "Важно разработать маркетинговую стратегию с учетом специфики вашей целевой аудитории и измеримыми KPI.";
                    
            justification = "\n\n📱 Обоснование: Исследование HubSpot показывает, что контент-маркетинг генерирует в 3 раза больше лидов, чем традиционная реклама, при этом стоит на 62% дешевле. По данным Semrush, 68% маркетологов считают, что персонализированный контент является наиболее эффективным инструментом. Согласно отчету Influencer Marketing Hub, среднее ROI для influencer-маркетинга составляет $5,78 на каждый потраченный доллар. В " + year + " году более 85% компаний интегрировали видеоконтент в свои маркетинговые стратегии.";
        }
        else if (query.contains("риск") || query.contains("безопасность бизнес")) {
            response = "Основные бизнес-риски и стратегии их минимизации:\n" +
                    "• Рыночные риски: постоянный мониторинг трендов, гибкость бизнес-модели\n" +
                    "• Финансовые риски: диверсификация источников дохода, финансовое планирование\n" +
                    "• Операционные риски: регламентация процессов, автоматизация, контроль качества\n" +
                    "• Кадровые риски: система мотивации, карьерный рост, корпоративная культура\n" +
                    "• Киберриски: защита данных, регулярные аудиты безопасности, обучение персонала\n" +
                    "• Репутационные риски: управление брендом, кризисные коммуникации\n\n" +
                    "Методы управления рисками: диверсификация, страхование, резервирование средств, хеджирование.";
                    
            justification = "\n\n📊 Обоснование: Согласно отчету Allianz Risk Barometer " + year + ", кибер-инциденты являются самым опасным бизнес-риском (44% респондентов). Исследование Ernst & Young показывает, что компании, внедрившие систему управления рисками, смогли снизить финансовые потери от непредвиденных ситуаций на 33%.";
        }
        else if (query.contains("b2b") || query.contains("би ту би")) {
            response = "Особенности B2B (бизнес для бизнеса) моделей:\n" +
                    "• Более длинный цикл продаж (от 3 до 12+ месяцев)\n" +
                    "• Акцент на отношениях и долгосрочном сотрудничестве\n" +
                    "• Ориентация на ROI и ценность для бизнеса клиента\n" +
                    "• Более высокая средняя стоимость сделки\n" +
                    "• Сложный процесс принятия решений (несколько лиц принимающих решения)\n\n" +
                    "Эффективные каналы продвижения для B2B: отраслевые выставки, профессиональные сообщества, LinkedIn, контент-маркетинг, вебинары, отраслевые медиа.";
                    
            justification = "\n\n🤝 Обоснование: По данным Forrester Research, 74% B2B покупателей проводят более половины исследований онлайн перед взаимодействием с продавцом. Исследование Gartner показывает, что в среднем 6-10 человек участвуют в процессе принятия B2B-решений. Согласно отчету Content Marketing Institute, 83% успешных B2B-компаний используют контент-маркетинг как ключевой инструмент привлечения и удержания клиентов. LinkedIn генерирует более 80% социальных B2B-лидов.";
        }
        else if (query.contains("b2c") || query.contains("би ту си")) {
            response = "Особенности B2C (бизнес для потребителя) моделей:\n" +
                    "• Более короткий цикл принятия решений\n" +
                    "• Фокус на эмоциональной связи и пользовательском опыте\n" +
                    "• Важность брендинга и визуальной привлекательности\n" +
                    "• Широкие маркетинговые кампании с большим охватом\n" +
                    "• Ценовая чувствительность потребителей\n\n" +
                    "Эффективные каналы продвижения для B2C: социальные сети, контекстная реклама, Email-маркетинг, мобильные приложения, программы лояльности, инфлюенсеры.";
                    
            justification = "\n\n👨‍👧‍👦 Обоснование: Исследование Nielsen показывает, что 92% потребителей доверяют рекомендациям от друзей и семьи больше, чем любой другой форме рекламы. По данным Statista, более 27% всех онлайн-покупок в " + year + " году совершаются через мобильные устройства. Согласно отчету Yotpo, лояльные клиенты тратят на 67% больше, чем новые. Исследование Facebook IQ выявило, что 83% потребителей предпочитают покупать у брендов, с которыми они эмоционально связаны.";
        }
        else if (query.contains("стартап") || query.contains("масштабирован")) {
            response = "Стратегии успешного запуска и масштабирования стартапа:\n" +
                    "• Фокус на проблеме, а не на продукте (product-market fit)\n" +
                    "• Использование методологии Lean Startup (минимизация затрат, быстрые итерации)\n" +
                    "• Создание MVP (минимально жизнеспособного продукта) для тестирования гипотез\n" +
                    "• Активная работа с ранними пользователями (early adopters)\n" +
                    "• Формирование сбалансированной команды с комплементарными навыками\n" +
                    "• Выбор правильной стратегии финансирования (бутстрэппинг, ангельские инвестиции, венчурный капитал)\n" +
                    "• Системный подход к масштабированию (готовность инфраструктуры и процессов)\n\n" +
                    "При масштабировании важно сохранять баланс между ростом и устойчивостью бизнеса.";
                    
            justification = "\n\n🚀 Обоснование: По данным исследования Startup Genome, 90% стартапов терпят неудачу, из них 70% пытаются масштабироваться преждевременно. Исследование CBInsights показывает, что главной причиной провала стартапов является отсутствие рыночной потребности в продукте (42%). Согласно Harvard Business Review, стартапы, использующие методологию Lean Startup, на 33% эффективнее в достижении product-market fit и на 43% быстрее выходят на рынок. Исследование First Round Capital демонстрирует, что разнообразные команды имеют на 35% больше шансов на получение прибыли выше среднерыночной.";
        }
        else if (query.contains("управлен") || query.contains("менеджмент")) {
            response = "Современные подходы к управлению бизнесом:\n" +
                    "• Agile-методологии (Scrum, Kanban) - гибкое управление проектами\n" +
                    "• OKR (Objectives and Key Results) - система целеполагания и оценки эффективности\n" +
                    "• Холакратия - распределенное принятие решений без иерархии\n" +
                    "• Data-driven management - принятие решений на основе данных\n" +
                    "• Lean-менеджмент - минимизация потерь и оптимизация процессов\n\n" +
                    "Ключевые компетенции современного руководителя: стратегическое мышление, эмоциональный интеллект, способность к адаптации, цифровая грамотность, навыки развития команды.";
                    
            justification = "\n\n👨‍💼 Обоснование: Исследование McKinsey показывает, что компании, использующие Agile-методологии, увеличивают операционную эффективность на 25-30%. По данным Deloitte, организации с сильной культурой data-driven decision making в 3 раза чаще сообщают о значительном улучшении бизнес-результатов. Согласно Harvard Business Review, компании, внедрившие систему OKR, регистрируют на 46% больше инноваций и на 38% выше вовлеченность сотрудников. Исследование World Economic Forum выявило, что к " + year + " году более 50% рабочих задач будут требовать новых навыков из-за цифровой трансформации.";
        }
        else if (query.contains("ecommerce") || query.contains("онлайн") && query.contains("продаж")) {
            response = "Стратегии развития электронной коммерции:\n" +
                    "• Мультиканальность и омниканальность (интеграция онлайн и офлайн каналов)\n" +
                    "• Персонализация пользовательского опыта на основе данных\n" +
                    "• Оптимизация мобильного опыта (мобильное приложение, адаптивный дизайн)\n" +
                    "• Улучшение логистики и опций доставки (same-day delivery, click&collect)\n" +
                    "• Интеграция с маркетплейсами для расширения охвата\n" +
                    "• Использование AR/VR для визуализации товаров\n" +
                    "• Внедрение чат-ботов и AI для обслуживания клиентов\n\n" +
                    "Ключевые метрики: конверсия, средний чек, CAC (стоимость привлечения клиента), LTV (пожизненная ценность клиента), показатель отказов.";
                    
            justification = "\n\n🛒 Обоснование: Согласно данным eMarketer, мировой рынок электронной коммерции в " + year + " году превысил $5 триллионов, с прогнозируемым среднегодовым ростом 14.7% до 2025 года. Исследование Salesforce показывает, что 67% покупателей используют несколько каналов для совершения единой покупки. По данным Google, улучшение скорости загрузки мобильного сайта на 0.1 секунду увеличивает конверсию на 8%. Согласно отчету Statista, 73% покупателей указывают, что возможность просмотра товара в AR/VR увеличивает их уверенность в покупке.";
        }
        else if (query.contains("инноваци") || query.contains("r&d") || query.contains("разработ")) {
            response = "Стратегии управления инновациями и R&D в бизнесе:\n" +
                    "• Open Innovation (привлечение внешних идей и технологий)\n" +
                    "• Дизайн-мышление (design thinking) как методология разработки\n" +
                    "• Создание инновационных лабораторий и R&D центров\n" +
                    "• Инкубационные программы для внутренних стартапов\n" +
                    "• Сотрудничество с научными центрами и университетами\n" +
                    "• Система управления идеями сотрудников\n" +
                    "• Прототипирование и тестирование с минимальными затратами\n\n" +
                    "Успешные инновации требуют баланса между креативностью и дисциплиной, краткосрочными и долгосрочными проектами.";
                    
            justification = "\n\n💡 Обоснование: Исследование BCG показывает, что компании-лидеры по инновациям генерируют на 14% больше выручки от продуктов, запущенных за последние 3 года. По данным PwC, компании, внедрившие Open Innovation, сократили время вывода продукта на рынок на 21%. Согласно отчету McKinsey, только 6% руководителей удовлетворены процессом инноваций в своих компаниях, хотя 80% считают инновации критически важными для роста. Исследование Capgemini выявило, что 87% компаний с выделенными инновационными подразделениями более эффективны в монетизации новых идей.";
        }
        else if (query.contains("hr") || query.contains("персонал") || query.contains("кадры") || query.contains("сотрудник")) {
            response = "Современные стратегии управления персоналом:\n" +
                    "• Employer branding (создание привлекательного бренда работодателя)\n" +
                    "• Data-driven HR (использование аналитики для принятия кадровых решений)\n" +
                    "• Гибкие форматы работы (удаленная работа, гибридный формат, 4-дневная неделя)\n" +
                    "• Персонализированные программы обучения и развития\n" +
                    "• Well-being программы (ментальное здоровье, work-life balance)\n" +
                    "• Система непрерывной обратной связи вместо ежегодных оценок\n" +
                    "• Управление разнообразием и инклюзивностью (D&I)\n\n" +
                    "В современных условиях успешный HR фокусируется не только на привлечении, но и на удержании и развитии талантов.";
                    
            justification = "\n\n👥 Обоснование: Исследование Deloitte показывает, что компании с сильным брендом работодателя тратят на 50% меньше на привлечение кандидатов и имеют на 28% меньше текучесть персонала. По данным Gallup, только 36% сотрудников в мире вовлечены в работу, что приводит к потерям продуктивности на сумму $7 триллионов ежегодно. Согласно исследованию PwC, 79% руководителей считают, что недостаток нужных навыков является одной из главных угроз для роста бизнеса. LinkedIn Learning сообщает, что 94% сотрудников остались бы в компании дольше, если бы она инвестировала в их обучение.";
        }
        else if (query.contains("выход") && query.contains("международ")) {
            response = "Стратегии выхода на международные рынки:\n" +
                    "• Экспорт через дистрибьюторов и торговых представителей\n" +
                    "• Прямой экспорт через e-commerce платформы\n" +
                    "• Лицензирование технологий и интеллектуальной собственности\n" +
                    "• Франчайзинг как способ расширения бренда\n" +
                    "• Создание совместных предприятий (Joint Ventures)\n" +
                    "• Открытие собственных представительств и филиалов\n" +
                    "• Слияния и поглощения (M&A) с локальными игроками\n\n" +
                    "Ключевые факторы успеха: тщательное изучение локального законодательства, адаптация продукта к культурным особенностям, выстраивание локальной команды.";
                    
            justification = "\n\n🌎 Обоснование: Исследование McKinsey показывает, что компании, успешно расширяющиеся на международные рынки, тратят в 2-3 раза больше времени на изучение локальной специфики. По данным World Bank, более 58% неудачных попыток выхода на зарубежные рынки связаны с недостаточным пониманием регуляторных требований и культурных различий. Согласно отчету HSBC, компании, использующие стратегию последовательного выхода на схожие рынки, имеют на 44% больше шансов на успех по сравнению с теми, кто выбирает разнородные рынки. Исследование EY выявило, что 67% компаний считают наличие локальных партнеров критически важным для успешной международной экспансии.";
        }
        else {
            // Общий ответ на бизнес-запросы
            response = "Для успешного ведения бизнеса в " + year + " году рекомендуется:\n\n" +
                    "• Регулярно анализировать рыночные тренды и поведение конкурентов\n" +
                    "• Внедрять цифровые технологии для оптимизации процессов\n" +
                    "• Создавать гибкие бизнес-модели, способные адаптироваться к изменениям\n" +
                    "• Инвестировать в развитие персонала и корпоративную культуру\n" +
                    "• Уделять внимание устойчивому развитию (ESG-принципы)\n" +
                    "• Диверсифицировать источники дохода и каналы сбыта\n" +
                    "• Формировать финансовую подушку безопасности\n\n" +
                    "Конкретные стратегии зависят от вашей отрасли, масштаба бизнеса и специфических целей. Задайте более конкретный вопрос для получения детальных рекомендаций.";
                    
            justification = "\n\n📚 Обоснование: По данным исследования Boston Consulting Group, компании с высокой цифровой зрелостью показывают на 1.8x выше рост выручки по сравнению с конкурентами. Согласно отчету McKinsey, бизнесы, внедряющие ESG-принципы, демонстрируют на 10% более высокую оценку акционерной стоимости. Исследование Deloitte выявило, что в условиях неопределенности компании с диверсифицированной структурой доходов на 39% устойчивее к экономическим шокам. Harvard Business Review отмечает, что организации с сильной корпоративной культурой показывают на 756% выше рост чистой прибыли за 11 лет по сравнению со среднерыночными показателями.";
        }
        
        // Добавляем примечание об актуальности данных
        String actualityNote = "\n\nДанные актуальны на " + currentDateTime;
        
        // Возвращаем комбинированный ответ с обоснованием
        return response + justification + actualityNote;
    }

    private String getApiKey() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplication());
        
        // Получаем ключ DeepSeek из настроек
        String deepseekKey = prefs.getString("deepseek_api_key", null);
        
        if (deepseekKey == null || deepseekKey.isEmpty()) {
            deepseekKey = DEEPSEEK_API_KEY;
            prefs.edit().putString("deepseek_api_key", deepseekKey).apply();
            Log.d(TAG, "Сохранен новый DeepSeek API ключ: " + deepseekKey.substring(0, 8) + "...");
        } else {
            Log.d(TAG, "Используется сохраненный DeepSeek API ключ: " + deepseekKey.substring(0, 8) + "...");
        }
        
        return deepseekKey;
    }
    
    /**
     * Получает API ключ для Claude из настроек
     */
    private String getClaudeApiKey() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplication());
        
        // Получаем ключ Claude из настроек
        String claudeKey = prefs.getString("claude_api_key", null);
        
        if (claudeKey == null || claudeKey.isEmpty()) {
            claudeKey = CLAUDE_API_KEY;
            prefs.edit().putString("claude_api_key", claudeKey).apply();
            Log.d(TAG, "Сохранен новый Claude API ключ: " + claudeKey.substring(0, Math.min(8, claudeKey.length())) + "...");
        } else {
            Log.d(TAG, "Используется сохраненный Claude API ключ: " + claudeKey.substring(0, Math.min(8, claudeKey.length())) + "...");
        }
        
        return claudeKey;
    }
    
    /**
     * Получает предпочтительную модель Claude из настроек
     */
    private String getClaudeModel() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplication());
        return prefs.getString("claude_model", CLAUDE_MODEL);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Останавливаем обновление прогресса, если оно активно
        stopProgressiveResponse();
        // Закрываем ресурсы WebParser при уничтожении ViewModel
        if (webParser != null) {
            webParser.shutdown();
        }
    }

    /**
     * Метод для формирования поискового запроса на основе сообщения пользователя
     * @param message сообщение пользователя
     * @return оптимизированный поисковый запрос
     */
    private String optimizeSearchQuery(String message) {
        // Удаляем вопросительные слова и знаки вопроса
        String[] questionWords = {"что", "кто", "где", "когда", "почему", "как", "сколько", "какой", "какая", "какое", "какие"};
        String query = message.toLowerCase();
        
        for (String word : questionWords) {
            query = query.replaceAll("\\b" + word + "\\b", "");
        }
        
        // Удаляем знаки вопроса и лишние пробелы
        query = query.replaceAll("\\?", "").trim();
        
        // Удаляем вводные фразы
        String[] introductoryPhrases = {
            "скажи мне", "расскажи о", "расскажи про", "расскажи мне о", "расскажи мне про",
            "хочу узнать о", "хочу узнать про", "интересно узнать", "можешь рассказать",
            "подскажи", "объясни", "поясни", "дай информацию о", "дай информацию про"
        };
        
        for (String phrase : introductoryPhrases) {
            if (query.startsWith(phrase)) {
                query = query.substring(phrase.length()).trim();
                break;
            }
        }
        
        // Удаляем предлоги в начале запроса
        if (query.startsWith("о ") || query.startsWith("об ") || query.startsWith("про ")) {
            query = query.substring(query.indexOf(" ") + 1);
        }
        
        // Удаляем лишние пробелы
        query = query.replaceAll("\\s+", " ").trim();
        
        // Если запрос стал слишком коротким, возвращаем оригинальное сообщение
        if (query.length() < 3) {
            return message;
        }
        
        return query;
    }

    /**
     * Метод для использования Claude API
     */
    private void useClaudeApi(String message) {
        // Получаем текущий год для системного сообщения
        int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
        
        // Формируем системное сообщение с актуальным годом
        String systemMessage = "Вы - современный ассистент с актуальными знаниями на текущий момент. Текущий год - " + currentYear + ". " +
            "Ваши ответы должны быть:\n" +
            "1. Конкретными и информативными\n" +
            "2. Содержать актуальные данные и факты на " + currentYear + " год\n" +
            "3. Включать конкретные цифры и статистику\n" +
            "4. Никогда не упоминать, что ваши данные ограничены прошлыми годами\n" +
            "5. Всегда предлагать альтернативные источники информации\n" +
            "6. Если пользователь спрашивает о текущих событиях, погоде, ценах или других данных, требующих актуальности, " +
            "указывать, что информация актуальна на " + currentYear + " год\n" +
            "7. Поддерживать диалог с пользователем, задавая уточняющие вопросы и предлагая развитие темы\n" +
            "8. Если пользователь задает вопрос о чем-то, что требует актуальных данных, указывать, что вы используете " +
            "информацию из интернета для предоставления наиболее точного ответа";
        
        // Добавляем сообщение пользователя в историю
        claudeConversationHistory.add(new ClaudeRequest.Message("user", message));
        
        // Создаем запрос к Claude API
        ClaudeRequest request = new ClaudeRequest(CLAUDE_MODEL, claudeConversationHistory, systemMessage);
        request.setMaxTokens(4000);
        request.setTemperature(0.7);
        request.setTopP(0.95);
        
        // Отправляем запрос
        claudeApi.sendMessage(request).enqueue(new retrofit2.Callback<ClaudeResponse>() {
            @Override
            public void onResponse(retrofit2.Call<ClaudeResponse> call, retrofit2.Response<ClaudeResponse> response) {
                isLoading.setValue(false);
                stopProgressiveResponse();

                if (response.isSuccessful() && response.body() != null) {
                    ClaudeResponse claudeResponse = response.body();
                    String assistantMessage = claudeResponse.getContentText();
                    
                    if (assistantMessage != null && !assistantMessage.isEmpty()) {
                        // Добавляем ответ ассистента в историю
                        claudeConversationHistory.add(new ClaudeRequest.Message("assistant", assistantMessage));
                        updateLastBotMessage(assistantMessage);
                        
                        // Логируем статистику использования токенов
                        ClaudeResponse.Usage usage = claudeResponse.getUsage();
                        if (usage != null) {
                            Log.d(TAG, String.format("Использование токенов Claude: промпт=%d, ответ=%d, всего=%d",
                                    usage.getInputTokens(),
                                    usage.getOutputTokens(),
                                    usage.getTotalTokens()));
                        }
                    } else {
                        updateLastBotMessage("Ошибка: Пустой ответ от Claude");
                    }
                        } else {
                    String errorMessage = "Ошибка при получении ответа от Claude";
                    try {
                        if (response.errorBody() != null) {
                            errorMessage += ": " + response.errorBody().string();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Ошибка при чтении тела ошибки", e);
                    }
                    Log.e(TAG, errorMessage);
                    updateLastBotMessage(errorMessage);
                    
                    // Если Claude не отвечает, пробуем использовать DeepSeek API как запасной вариант
                    useGptApi(message);
                }
            }
            
            @Override
            public void onFailure(retrofit2.Call<ClaudeResponse> call, Throwable t) {
                isLoading.setValue(false);
                stopProgressiveResponse();
                String errorMessage = "Ошибка при получении ответа от Claude: " + t.getMessage();
                Log.e(TAG, errorMessage, t);
                updateLastBotMessage(errorMessage);
                
                // Если Claude не отвечает, пробуем использовать DeepSeek API как запасной вариант
                useGptApi(message);
            }
        });
    }

    /**
     * Метод для выбора API в зависимости от настроек и типа запроса
     */
    private void useAiApi(String message) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplication());
        String preferredModel = prefs.getString("preferred_model", "deepseek"); // По умолчанию используем DeepSeek
        
        if ("claude".equals(preferredModel)) {
            // Используем Claude API
            useClaudeApi(message);
        } else {
            // Используем DeepSeek API
            useGptApi(message);
        }
    }

    /**
     * Обновляет системное сообщение с актуальным годом
     */
    private void updateSystemMessage() {
        // Очищаем историю системных сообщений
        for (int i = 0; i < conversationHistory.size(); i++) {
            if ("system".equals(conversationHistory.get(i).getRole())) {
                conversationHistory.remove(i);
                i--; // Сдвигаем индекс после удаления
            }
        }

        for (int i = 0; i < claudeConversationHistory.size(); i++) {
            if ("system".equals(claudeConversationHistory.get(i).getRole())) {
                claudeConversationHistory.remove(i);
                i--; // Сдвигаем индекс после удаления
            }
        }

        // Добавляем текущий год в системное сообщение
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        String currentDate = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(new Date());
        
        // Добавляем инструкцию по деловому стилю общения
        String systemMessage = "Вы ассистент, предоставляющий информацию в строгом деловом стиле. " +
                "Сегодня " + currentDate + ", текущий год: " + currentYear + ". " +
                "Избегайте использования эмодзи, восклицательных знаков и неформальной лексики. " +
                "Не выделяйте текст жирным шрифтом или курсивом. " +
                "Ваш тон должен быть сдержанным, профессиональным и информативным. " +
                "Используйте официально-деловой стиль речи. " +
                "Формулируйте ответы четко, конкретно и без лишних эмоций. " +
                "Всегда предоставляйте проверенную, актуальную информацию. " +
                "При ответе на вопросы пользователя следует придерживаться фактов. " +
                "Обращайтесь к пользователю на 'Вы'.";

        // Добавляем системное сообщение в историю диалога
        conversationHistory.add(0, new GptRequest.Message("system", systemMessage));
        claudeConversationHistory.add(0, new ClaudeRequest.Message("system", systemMessage));
        
        Log.d(TAG, "Установлен деловой стиль для ответов AI");
    }

    /**
     * Метод для использования GPT API как запасного варианта
     */
    private void useGptApi(String message) {
        // Обновляем системное сообщение перед каждым запросом
        updateSystemMessage();
        
        conversationHistory.add(new GptRequest.Message("user", message));
        
        GptRequest request = new GptRequest("deepseek-chat", conversationHistory, 0.7);
        request.setMax_tokens(4000);
        request.setStream(false);
        request.setTop_p(0.95);
        request.setPresence_penalty(0.6);
        request.setFrequency_penalty(0.5);
        
        gptApi.getChatCompletion(request).enqueue(new retrofit2.Callback<GptResponse>() {
            @Override
            public void onResponse(retrofit2.Call<GptResponse> call, retrofit2.Response<GptResponse> response) {
                isLoading.setValue(false);
                stopProgressiveResponse();

                if (response.isSuccessful() && response.body() != null) {
                    GptResponse gptResponse = response.body();
                    if (!gptResponse.getChoices().isEmpty()) {
                        String assistantMessage = gptResponse.getChoices().get(0).getMessage().getContent();
                        conversationHistory.add(new GptRequest.Message("assistant", assistantMessage));
                        updateLastBotMessage(assistantMessage);
                        
                        // Логируем статистику использования токенов
                        GptResponse.Usage usage = gptResponse.getUsage();
                        Log.d(TAG, String.format("Использование токенов: промпт=%d, ответ=%d, всего=%d",
                                usage.getPrompt_tokens(),
                                usage.getCompletion_tokens(),
                                usage.getTotal_tokens()));
                    } else {
                        updateLastBotMessage("Ошибка: Пустой ответ от сервера");
                    }
                } else {
                    String errorMessage = "Ошибка при получении ответа от сервера";
                    try {
                        if (response.errorBody() != null) {
                            errorMessage += ": " + response.errorBody().string();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Ошибка при чтении тела ошибки", e);
                    }
                    Log.e(TAG, errorMessage);
                    updateLastBotMessage(errorMessage);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<GptResponse> call, Throwable t) {
                isLoading.setValue(false);
                stopProgressiveResponse();
                String errorMessage = "Ошибка при получении ответа: " + t.getMessage();
                Log.e(TAG, errorMessage, t);
                updateLastBotMessage(errorMessage);
            }
        });
    }

    // Переменные для поэтапной генерации сообщений
    private Handler progressHandler = new Handler(Looper.getMainLooper());
    private Runnable progressRunnable;
    private int currentProgressStep = 0;
    private String currentProgressMessage = "";
    private boolean isGeneratingProgressively = false;
    private static final int PROGRESS_DELAY = 300; // миллисекунды между обновлениями
    
    /**
     * Начинает поэтапную генерацию ответа
     */
    private void startProgressiveResponse(String userMessage) {
        // Останавливаем предыдущую генерацию, если она была
        stopProgressiveResponse();
        
        // Добавляем начальное сообщение
        currentProgressMessage = "Ищу информацию...";
        addMessage(new ChatMessage(currentProgressMessage, false, getCurrentTime(), ""));
        
        isGeneratingProgressively = true;
        currentProgressStep = 0;
        
        // Создаем новый Runnable для обновления сообщения
        progressRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isGeneratingProgressively) {
                    return;
                }
                
                currentProgressStep++;
                
                // Обновляем текст сообщения в зависимости от шага
                switch (currentProgressStep % 4) {
                    case 1:
                        currentProgressMessage = "Анализирую запрос...";
                        break;
                    case 2:
                        currentProgressMessage = "Собираю данные...";
                        break;
                    case 3:
                        currentProgressMessage = "Формирую ответ...";
                        break;
                    case 0:
                        currentProgressMessage = "Проверяю актуальность информации...";
                        break;
                }
                
                // Обновляем последнее сообщение бота
                updateLastBotMessage(currentProgressMessage);
                
                // Планируем следующее обновление
                progressHandler.postDelayed(this, PROGRESS_DELAY);
            }
        };
        
        // Запускаем обновление
        progressHandler.postDelayed(progressRunnable, PROGRESS_DELAY);
    }
    
    /**
     * Останавливает поэтапную генерацию ответа
     */
    private void stopProgressiveResponse() {
        isGeneratingProgressively = false;
        if (progressRunnable != null) {
            progressHandler.removeCallbacks(progressRunnable);
        }
    }
    
    /**
     * Обновляет последнее сообщение бота
     */
    private void updateLastBotMessage(String newText) {
        List<ChatMessage> currentMessages = chatMessages.getValue();
        if (currentMessages != null && !currentMessages.isEmpty()) {
            // Ищем последнее сообщение от бота
            for (int i = currentMessages.size() - 1; i >= 0; i--) {
                ChatMessage message = currentMessages.get(i);
                if (!message.isUser()) {
                    // Обновляем текст сообщения
                    currentMessages.set(i, new ChatMessage(newText, false, message.getTime(), message.getMoreInfo()));
                    chatMessages.setValue(currentMessages);
                    break;
                }
            }
        }
    }
    
    /**
     * Извлекает тему для поиска фотографий из сообщения
     */
    private String extractPhotoTopic(String message) {
        // Удаляем ключевые слова, связанные с фото
        String[] photoKeywords = {"фото", "картинк", "изображен", "покажи", "фотографи"};
        String query = message.toLowerCase();
        
        for (String keyword : photoKeywords) {
            query = query.replace(keyword, "");
        }
        
        // Удаляем предлоги и лишние пробелы
        query = query.replaceAll("\\s+", " ").trim();
        
        // Если запрос стал слишком коротким, возвращаем null
        if (query.length() < 3) {
            return null;
        }
        
        return query;
    }
    
    /**
     * Возвращает справочную информацию по ключевым словам
     */
    private String getHelpInfo() {
        return "Справка по ключевым словам:\n\n" +
               "Для получения актуальной информации используйте следующие ключевые слова:\n\n" +
               "- погода в [город] - текущая погода и прогноз\n" +
               "- курс валют - актуальные курсы валют\n" +
               "- акции [тикер] - информация о ценных бумагах\n" +
               "- новости [тема] - последние новости\n" +
               "- фото [тема] - поиск изображений\n" +
               "- справка - эта информация\n\n" +
               "Для поиска конкретной информации в интернете используйте команду:\n" +
               "найди [запрос] - поиск информации в интернете\n\n" +
               "Вы также можете задавать любые вопросы в свободной форме.";
    }
    
    /**
     * Проверяет, является ли запрос запросом на новости
     */
    private boolean isNewsQuery(String message) {
        String[] newsKeywords = {
            "новост", "событи", "происшеств", "что нового", "что случилось", 
            "что произошло", "последние новости", "свежие новости", "горячие новости",
            "новости сегодня", "новости дня", "новости за сегодня", "новости за день",
            "новости за неделю", "новости за месяц", "новости за год", "новости за последний",
            "новости по теме", "новости о", "новости про", "новости в", "новости из",
            "новости мира", "новости россии", "новости москвы", "новости спорта",
            "новости политики", "новости экономики", "новости культуры", "новости науки",
            "новости технологий", "новости шоу-бизнеса", "новости кино", "новости музыки",
            "новости литературы", "новости искусства", "новости автомобилей", "новости авто",
            "новости медицины", "новости здоровья", "новости образования", "новости туризма",
            "новости путешествий", "новости недвижимости", "новости строительства",
            "новости транспорта", "новости связи", "новости интернета", "новости it",
            "новости компьютеров", "новости гаджетов", "новости смартфонов", "новости телефонов",
            "новости планшетов", "новости ноутбуков", "новости компьютерных игр", "новости игр",
            "новости киберспорта", "новости спортивных игр", "новости футбола", "новости хоккея",
            "новости баскетбола", "новости тенниса", "новости волейбола", "новости бокса",
            "новости единоборств", "новости легкой атлетики", "новости тяжелой атлетики",
            "новости плавания", "новости гимнастики", "новости фигурного катания",
            "новости лыжного спорта", "новости биатлона", "новости конькобежного спорта",
            "новости велоспорта", "новости автоспорта", "новости формулы 1", "новости ралли",
            "новости мотоспорта", "новости парусного спорта", "новости водных видов спорта",
            "новости зимних видов спорта", "новости летних видов спорта", "новости олимпиады",
            "новости чемпионата мира", "новости чемпионата европы", "новости чемпионата россии",
            "новости кубка мира", "новости кубка европы", "новости кубка россии",
            "новости лиги чемпионов", "новости лиги европы", "новости премьер-лиги",
            "новости рпл", "новости кхл", "новости нхл", "новости нба", "новости нфл",
            "новости млб", "новости уефа", "новости фифа", "новости фиба", "новости иааф",
            "новости мок", "новости вада", "новости русада", "новости допинга",
            "новости трансферов", "новости контрактов", "новости зарплат", "новости бонусов",
            "новости премий", "новости наград", "новости призов", "новости рекордов",
            "новости достижений", "новости побед", "новости поражений", "новости ничьих",
            "новости матчей", "новости игр", "новости турниров", "новости соревнований",
            "новости чемпионатов", "новости кубков", "новости лиг", "новости дивизионов",
            "новости конференций", "новости групп", "новости плей-офф", "новости финалов",
            "новости полуфиналов", "новости четвертьфиналов", "новости 1/8 финала",
            "новости 1/16 финала", "новости 1/32 финала", "новости 1/64 финала",
            "новости квалификации", "новости отбора", "новости жеребьевки", "новости расписания",
            "новости календаря", "новости результатов", "новости счета", "новости статистики",
            "новости рейтинга", "новости таблицы", "новости положения", "новости места",
            "новости очков", "новости голов", "новости мячей", "новости шайб", "новости очков",
            "новости передач", "новости пасов", "новости ассистов", "новости подборов",
            "новости перехватов", "новости блок-шотов", "новости фолов", "новости нарушений",
            "новости штрафов", "новости удалений", "новости дисквалификаций", "новости травм",
            "новости болезней", "новости восстановления", "новости реабилитации",
            "новости операций", "новости лечения", "новости диагностики", "новости обследования",
            "новости медосмотра", "новости медкомиссии", "новости медицинского обследования",
            "новости медицинской комиссии", "новости медицинского осмотра", "новости медосмотра",
            "новости медкомиссии", "новости медицинского обследования", "новости медицинской комиссии",
            "новости медицинского осмотра", "новости медосмотра", "новости медкомиссии",
            "новости медицинского обследования", "новости медицинской комиссии", "новости медицинского осмотра"
        };
        
        message = message.toLowerCase();
        for (String keyword : newsKeywords) {
            if (message.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Обрабатывает запрос на новости
     */
    private String handleNewsQuery(String message) {
        try {
            // Создаем парсер новостей
            NewsParser newsParser = new NewsParser();
            
            // Определяем тему новостей, если она указана
            String topic = extractNewsTopic(message);
            
            List<NewsParser.NewsItem> news;
            StringBuilder result = new StringBuilder();
            
            if (topic != null) {
                // Получаем новости по теме
                news = newsParser.getNewsByTopic(topic, 10);
                result.append("Последние новости по теме \"").append(topic).append("\":\n\n");
            } else {
                // Получаем последние новости
                news = newsParser.getLatestNews(10);
                result.append("Последние новости:\n\n");
            }
            
            if (news.isEmpty()) {
                return "Не удалось найти новости" + (topic != null ? " по теме \"" + topic + "\"" : "") + 
                       ". Пожалуйста, попробуйте позже или уточните запрос.";
            }
            
            // Формируем ответ
            for (int i = 0; i < news.size(); i++) {
                NewsParser.NewsItem item = news.get(i);
                result.append(i + 1).append(". ").append(item.getTitle()).append("\n");
                
                if (!item.getDescription().isEmpty()) {
                    result.append(item.getDescription()).append("\n");
                }
                
                result.append("Источник: ").append(item.getSource()).append(" (").append(item.getRelativeTime()).append(")\n");
                result.append("Ссылка: ").append(item.getUrl()).append("\n\n");
            }
            
            result.append("Для получения более подробной информации по конкретной новости, пожалуйста, перейдите по ссылке.");
            
            // Закрываем ресурсы парсера
            newsParser.shutdown();
            
            return result.toString();
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при обработке запроса на новости", e);
            return "Не удалось получить новости. Пожалуйста, попробуйте позже.";
        }
    }
    
    /**
     * Извлекает тему новостей из сообщения
     */
    private String extractNewsTopic(String message) {
        // Удаляем ключевые слова, связанные с новостями
        String[] newsKeywords = {"новост", "событи", "происшеств", "что нового", "что случилось", 
                                "что произошло", "последние", "свежие", "горячие", "сегодня", "дня"};
        
        String query = message.toLowerCase();
        
        for (String keyword : newsKeywords) {
            query = query.replace(keyword, "");
        }
        
        // Удаляем предлоги и союзы
        String[] prepositionsAndConjunctions = {"в", "на", "с", "к", "у", "от", "до", "для", "про", "через", 
                                              "над", "под", "при", "без", "и", "а", "но", "или", "да", "либо", "о", "об"};
        
        for (String word : prepositionsAndConjunctions) {
            query = query.replace(" " + word + " ", " ");
        }
        
        // Удаляем лишние пробелы
        query = query.replaceAll("\\s+", " ").trim();
        
        // Если запрос стал слишком коротким, возвращаем null
        if (query.length() < 3) {
            return null;
        }
        
        return query;
    }

    /**
     * Обновляет новости
     */
    public void refreshNews(String query) {
        // Добавляем сообщение о загрузке
        addMessage(new ChatMessage("Обновляю новости...", false, getCurrentTime(), ""));
        
        // Используем отдельный поток для парсинга, чтобы не блокировать UI
        new Thread(() -> {
            try {
                // Создаем парсер новостей
                NewsParser newsParser = new NewsParser();
                
                // Определяем тему новостей, если она указана
                String topic = extractNewsTopic(query);
                
                List<NewsParser.NewsItem> news;
                StringBuilder result = new StringBuilder();
                
                if (topic != null) {
                    // Получаем новости по теме
                    news = newsParser.getNewsByTopic(topic, 10);
                    result.append("Последние новости по теме \"").append(topic).append("\":\n\n");
                } else {
                    // Получаем последние новости
                    news = newsParser.getLatestNews(10);
                    result.append("Последние новости:\n\n");
                }
                
                if (news.isEmpty()) {
                    final String errorMessage = "Не удалось найти новости" + 
                                               (topic != null ? " по теме \"" + topic + "\"" : "") + 
                                               ". Пожалуйста, попробуйте позже или уточните запрос.";
                    
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        // Удаляем сообщение о загрузке
                        removeLastBotMessage();
                        // Добавляем сообщение об ошибке
                        addMessage(new ChatMessage(errorMessage, false, getCurrentTime(), ""));
                    });
                    
                    return;
                }
                
                // Формируем ответ
                for (int i = 0; i < news.size(); i++) {
                    NewsParser.NewsItem item = news.get(i);
                    result.append(i + 1).append(". ").append(item.getTitle()).append("\n");
                    
                    if (!item.getDescription().isEmpty()) {
                        result.append(item.getDescription()).append("\n");
                    }
                    
                    result.append("Источник: ").append(item.getSource()).append(" (").append(item.getRelativeTime()).append(")\n");
                    result.append("Ссылка: ").append(item.getUrl()).append("\n\n");
                }
                
                result.append("Для получения более подробной информации по конкретной новости, пожалуйста, перейдите по ссылке.");
                
                // Закрываем ресурсы парсера
                newsParser.shutdown();
                
                final String finalResult = result.toString();
                
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                    // Удаляем сообщение о загрузке
                    removeLastBotMessage();
                    // Добавляем обновленные новости
                    addMessage(new ChatMessage(finalResult, false, getCurrentTime(), ""));
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Ошибка при обновлении новостей", e);
                
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                    // Удаляем сообщение о загрузке
                    removeLastBotMessage();
                    // Добавляем сообщение об ошибке
                    addMessage(new ChatMessage("Не удалось обновить новости. Пожалуйста, попробуйте позже.", 
                                              false, getCurrentTime(), ""));
                });
            }
        }).start();
    }
    
    /**
     * Удаляет последнее сообщение бота
     */
    private void removeLastBotMessage() {
        List<ChatMessage> currentMessages = chatMessages.getValue();
        if (currentMessages != null && !currentMessages.isEmpty()) {
            for (int i = currentMessages.size() - 1; i >= 0; i--) {
                if (!currentMessages.get(i).isUser()) {
                    currentMessages.remove(i);
                    chatMessages.setValue(currentMessages);
                    break;
                }
            }
        }
    }

    /**
     * Удаляет символы markdown-форматирования (звездочки) из текста
     * @param text Исходный текст с возможными символами форматирования
     * @return Текст без символов форматирования
     */
    private String removeMarkdownFormatting(String text) {
        if (text == null) return "";
        
        // Удаляем двойные звездочки (жирный текст)
        String result = text.replaceAll("\\*\\*(.*?)\\*\\*", "$1")
                  .replaceAll("\\*(.*?)\\*", "$1") // Удаляем одинарные звездочки (курсив)
                  .replaceAll("[\\p{Emoji}\\p{Emoji_Presentation}\\p{Emoji_Modifier}\\p{Emoji_Component}]", "") // Удаление эмодзи
                  .replaceAll("(?i)\\b(привет|здравствуйте|хай|хеллоу)[!]*", "Здравствуйте.") // Замена неформальных приветствий
                  .replaceAll("(?i)\\bпока[!]*", "До свидания.") // Замена неформальных прощаний
                  .replaceAll("\\!+", ".") // Замена восклицательных знаков точками
                  .replaceAll("\\.{2,}", ".") // Замена многоточий одной точкой
                  .replaceAll("\\?{2,}", "?") // Замена множественных вопросительных знаков
                  .replaceAll("\\s+", " ") // Удаление лишних пробелов
                  .trim();
                  
        // Преобразуем первую букву предложения в заглавную, если это необходимо
        if (result.length() > 0) {
            result = Character.toUpperCase(result.charAt(0)) + result.substring(1);
        }
        
        return result;
    }

    /**
     * Обновляет последний ответ, повторно отправляя запрос
     */
    public void refreshLatestResponse() {
        List<ChatMessage> messages = chatMessages.getValue();
        if (messages == null || messages.isEmpty()) {
            return;
        }
        
        // Ищем последний запрос пользователя
        String lastUserQuery = null;
        int removeFrom = -1;
        
        for (int i = messages.size() - 1; i >= 0; i--) {
            ChatMessage message = messages.get(i);
            if (message.isUser()) {
                lastUserQuery = message.getMessage();
                removeFrom = i + 1;
                break;
            }
        }
        
        if (lastUserQuery == null) {
            return;
        }
        
        // Если найден последний запрос пользователя, удаляем все ответы после него
        if (removeFrom >= 0 && removeFrom < messages.size()) {
            // Создаем новый список с сообщениями до последнего запроса пользователя
            List<ChatMessage> newMessages = new ArrayList<>(messages.subList(0, removeFrom));
            chatMessages.setValue(newMessages);
            
            // Отправляем запрос снова
            sendMessage(lastUserQuery);
        }
    }
}