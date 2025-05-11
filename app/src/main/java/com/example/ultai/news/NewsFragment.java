package com.example.ultai.news;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.ultai20.R;
import com.example.ultai20.databinding.FragmentNewsBinding;
import com.example.ultai.news.newsapp.adapter.NewsAdapter;
import com.example.ultai.news.newsapp.model.NewsItem;
import com.example.ultai.news.newsapp.utils.NewsParserAdapter;
import com.example.ultai.news.newsapp.utils.NewsUpdateWorker;
import com.example.ultai.util.CompanyDataManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class NewsFragment extends Fragment {
    private FragmentNewsBinding binding;
    private RecyclerView recyclerView;
    private NewsAdapter newsAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private List<NewsItem> allNews;
    private NewsParserAdapter newsParserAdapter;
    private ExecutorService executorService;
    private static final String TAG = "NewsFragment";
    private TextView companyNameTextView;
    private CompanyDataManager companyDataManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNewsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: начало инициализации");

        // Инициализация парсера новостей
        newsParserAdapter = new NewsParserAdapter(getContext());
        executorService = Executors.newSingleThreadExecutor();
        Log.d(TAG, "Парсер новостей и ExecutorService инициализированы");

        // Инициализация RecyclerView
        recyclerView = view.findViewById(R.id.recycler_view);
        if (recyclerView == null) {
            Log.e(TAG, "RecyclerView не найден в макете!");
        } else {
            Log.d(TAG, "RecyclerView найден в макете");
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            allNews = new ArrayList<>();
            newsAdapter = new NewsAdapter(getContext(), allNews, "ru"); // Устанавливаем русский язык по умолчанию
            recyclerView.setAdapter(newsAdapter);
            Log.d(TAG, "RecyclerView настроен с адаптером");
        }

        // Инициализация SwipeRefreshLayout
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        if (swipeRefreshLayout == null) {
            Log.e(TAG, "SwipeRefreshLayout не найден в макете!");
        } else {
            Log.d(TAG, "SwipeRefreshLayout найден в макете");
            swipeRefreshLayout.setOnRefreshListener(() -> {
                fetchNewsFromParser(); // Обновляем новости при свайпе
            });
        }

        // Инициализация ViewModel
        NewsViewModel newsViewModel = new ViewModelProvider(this).get(NewsViewModel.class);
        newsViewModel.getNews().observe(getViewLifecycleOwner(), newsItems -> {
            if (newsItems != null) {
                Log.d(TAG, "Получены новости из ViewModel: " + newsItems.size());
                allNews.clear();
                allNews.addAll(newsItems);
                newsAdapter.updateData(allNews); // Обновляем данные в адаптере
                newsAdapter.notifyDataSetChanged(); // Явно уведомляем адаптер об изменениях
            } else {
                Log.d(TAG, "Получен null из ViewModel");
            }
        });

        // Навигация по нажатию на кнопку
        if (binding.imageButton7 != null) {
            binding.imageButton7.setOnClickListener(v -> {
                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.action_navigation_news_to_profileFragment);
            });
        }

        // Загрузка сохраненных новостей из SharedPreferences
        List<NewsItem> savedNews = NewsUpdateWorker.loadNewsFromSharedPreferences(getContext());
        if (savedNews != null && !savedNews.isEmpty()) {
            Log.d(TAG, "Загружено " + savedNews.size() + " новостей из SharedPreferences");
            allNews.clear();
            allNews.addAll(savedNews);
            newsAdapter.updateData(savedNews);
            newsAdapter.notifyDataSetChanged(); // Явно уведомляем адаптер об изменениях
        } else {
            Log.d(TAG, "Сохраненные новости не найдены. Загружаем новости через парсер...");
            fetchNewsFromParser();
        }

        // Инициализация WorkManager для автоматического обновления новостей раз в сутки
        setupNewsUpdateWorker();
        
        // Инициализация CompanyDataManager
        companyDataManager = CompanyDataManager.getInstance(getContext());
        
        // Загружаем и отображаем данные компании
        loadCompanyData(view);
        
        Log.d(TAG, "onViewCreated: инициализация завершена");
    }

    private void setupNewsUpdateWorker() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(
                NewsUpdateWorker.class,
                24, TimeUnit.HOURS
        )
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(getContext()).enqueueUniquePeriodicWork(
                "NewsUpdateWork",
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
        );

        Log.d(TAG, "Запланировано периодическое обновление новостей каждые 24 часа");
    }

    private void fetchNewsFromParser() {
        swipeRefreshLayout.setRefreshing(true); // Показываем индикатор загрузки
        Log.d(TAG, "Начинаем загрузку новостей...");

        executorService.execute(() -> {
            try {
                // Получаем последние новости через парсер (запрашиваем 20 новостей)
                Log.d(TAG, "Вызываем метод getLatestNews...");
                List<NewsItem> news = newsParserAdapter.getLatestNews(20);
                Log.d(TAG, "Получен результат от getLatestNews: " + (news != null ? news.size() : "null") + " новостей");
                
                // Обновляем UI в основном потоке
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        swipeRefreshLayout.setRefreshing(false); // Скрываем индикатор загрузки
                        
                        if (news != null && !news.isEmpty()) {
                            Log.d(TAG, "Обновляем адаптер с " + news.size() + " новостями");
                            for (int i = 0; i < Math.min(3, news.size()); i++) {
                                NewsItem item = news.get(i);
                                Log.d(TAG, "Новость #" + i + ": " + item.getTitle() + ", источник: " + 
                                    (item.getSource() != null ? item.getSource().getName() : "null") +
                                    ", опубликовано: " + item.getRelativeTime());
                            }
                            
                            // Сортируем новости по дате публикации (сначала новые)
                            List<NewsItem> sortedNews = new ArrayList<>(news);
                            Collections.sort(sortedNews, (a, b) -> {
                                // Если у одной из новостей дата null, помещаем её в конец списка
                                if (a.getPublishedAt() == null) return 1;
                                if (b.getPublishedAt() == null) return -1;
                                // Сортируем по убыванию даты (новые первыми)
                                return b.getPublishedAt().compareTo(a.getPublishedAt());
                            });
                            
                            allNews.clear();
                            allNews.addAll(sortedNews);
                            newsAdapter.updateData(sortedNews); // Обновляем данные в адаптере
                            newsAdapter.notifyDataSetChanged(); // Явно уведомляем адаптер об изменениях
                            
                            Log.d(TAG, "Получено " + news.size() + " новостей через парсер");
                            
                            // Сохраняем новые новости в SharedPreferences
                            NewsUpdateWorker.saveNewsToSharedPreferences(getContext(), sortedNews);
                            
                            // Скрываем сообщение об ошибке, если оно было показано
                            TextView errorTextView = binding.errorTextView;
                            if (errorTextView != null) {
                                errorTextView.setVisibility(View.GONE);
                            }
                        } else {
                            Log.w(TAG, "Не удалось получить новости через парсер");
                            showErrorMessage("В данный момент новости недоступны.");
                        }
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Ошибка при получении новостей через парсер", e);
                
                // Обновляем UI в основном потоке
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        swipeRefreshLayout.setRefreshing(false); // Скрываем индикатор загрузки
                        showErrorMessage("Не удалось загрузить новости. Пожалуйста, проверьте подключение к интернету.");
                        
                        // Пробуем загрузить сохраненные новости
                        List<NewsItem> savedNews = NewsUpdateWorker.loadNewsFromSharedPreferences(getContext());
                        if (savedNews != null && !savedNews.isEmpty()) {
                            Log.d(TAG, "Загружены сохраненные новости: " + savedNews.size());
                            allNews.clear();
                            allNews.addAll(savedNews);
                            newsAdapter.updateData(savedNews);
                            newsAdapter.notifyDataSetChanged(); // Явно уведомляем адаптер об изменениях
                        }
                    });
                }
            }
        });
    }

    private void showErrorMessage(String message) {
        TextView errorTextView = binding.errorTextView;
        if (errorTextView != null) {
            errorTextView.setVisibility(View.VISIBLE);
            errorTextView.setText(message);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        
        // Обновляем название компании при возобновлении видимости фрагмента
        if (companyDataManager != null && binding != null && binding.companyNameText != null) {
            String companyName = companyDataManager.getCompanyName();
            binding.companyNameText.setText(companyName);
            Log.d(TAG, "onResume: Обновлено название компании: " + companyName);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        
        // Освобождаем ресурсы
        if (newsParserAdapter != null) {
            newsParserAdapter.shutdown();
        }
        
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
        
        binding = null; // Очищаем ссылку на binding
    }

    /**
     * Загружает и отображает данные компании и описание деятельности
     */
    private void loadCompanyData(View view) {
        // Находим TextView для названия компании
        companyNameTextView = binding.companyNameText;
        // Находим TextView для описания деятельности компании
        TextView companyDescriptionTextView = view.findViewById(R.id.textView5);
        
        // Устанавливаем название компании из CompanyDataManager
        String companyName = companyDataManager.getCompanyName();
        companyNameTextView.setText(companyName);
        
        // Получаем описание деятельности компании
        String activityDescription = companyDataManager.getActivityDescription();
        
        if (activityDescription != null && !activityDescription.isEmpty()) {
            // Если есть сохраненное описание деятельности, отображаем его
            companyDescriptionTextView.setText(activityDescription);
        } else {
            // Получаем тип деятельности как запасной вариант
            String activityType = companyDataManager.getActivityType();
            if (activityType != null && !activityType.isEmpty()) {
                companyDescriptionTextView.setText(activityType);
            } else {
                // Если нет данных о деятельности, проверяем SharedPreferences анкеты
                SharedPreferences basicQuestPrefs = requireContext().getSharedPreferences("basic_questionnaire", Context.MODE_PRIVATE);
                String productsServicesDescription = basicQuestPrefs.getString("productsServicesDescription", "");
                
                if (productsServicesDescription != null && !productsServicesDescription.isEmpty()) {
                    companyDescriptionTextView.setText(productsServicesDescription);
                }
            }
        }
    }
}