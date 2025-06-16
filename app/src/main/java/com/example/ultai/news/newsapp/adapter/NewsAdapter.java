package com.example.ultai.news.newsapp.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.ultai20.R;
import com.example.ultai.news.newsapp.model.NewsItem;

import java.util.ArrayList;
import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    private static final String TAG = "NewsAdapter";
    private Context context;
    private List<NewsItem> newsList;
    private String language; // Язык перевода

    public NewsAdapter(Context context, List<NewsItem> newsList, String language) {
        this.context = context;
        this.language = language;
        
        // Проверяем, что список не null
        if (newsList != null) {
            this.newsList = newsList;
        } else {
            Log.w(TAG, "Конструктор получил null список, создаем пустой список");
            this.newsList = new ArrayList<>();
        }
        
        Log.d(TAG, "NewsAdapter создан, размер списка: " + this.newsList.size());
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_news, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        NewsItem newsItem = newsList.get(position);
        Log.d(TAG, "onBindViewHolder: позиция " + position + ", заголовок: " + newsItem.getTitle());

        // Установка заголовка
        if (newsItem.getTitle() != null && !newsItem.getTitle().isEmpty()) {
            holder.titleTextView.setText(newsItem.getTitle());
        } else {
            holder.titleTextView.setText("Без заголовка");
        }

        // Установка описания
        String description = getTranslatedDescription(newsItem, language);
        if (!TextUtils.isEmpty(description)) {
            holder.descriptionTextView.setText(description);
            holder.descriptionTextView.setVisibility(View.VISIBLE);
        } else {
            holder.descriptionTextView.setText("Описание отсутствует");
            holder.descriptionTextView.setVisibility(View.VISIBLE);
        }

        // Установка источника и времени публикации
        StringBuilder sourceAndTime = new StringBuilder();
        
        if (newsItem.getSource() != null && newsItem.getSource().getName() != null) {
            sourceAndTime.append("Источник: ").append(newsItem.getSource().getName());
        } else {
            sourceAndTime.append("Неизвестный источник");
        }
        
        // Добавляем время публикации, если оно доступно
        String relativeTime = newsItem.getRelativeTime();
        if (!TextUtils.isEmpty(relativeTime)) {
            sourceAndTime.append(" • ").append(relativeTime);
        }
        
        holder.sourceTextView.setText(sourceAndTime.toString());
        
        // Устанавливаем полное время публикации как подсказку, если оно доступно
        if (holder.timeTextView != null) {
            String formattedTime = newsItem.getFormattedTime();
            if (!TextUtils.isEmpty(formattedTime)) {
                holder.timeTextView.setText(formattedTime);
                holder.timeTextView.setVisibility(View.VISIBLE);
            } else {
                holder.timeTextView.setVisibility(View.GONE);
            }
        }

        // Загрузка изображения с помощью Glide
        loadImage(holder.imageView, newsItem.getUrlToImage());
    }

    /**
     * Загружает изображение с обработкой ошибок и кэшированием
     */
    private void loadImage(ImageView imageView, String imageUrl) {
        if (context == null) {
            Log.e(TAG, "loadImage: context равен null");
            return;
        }
        
        if (imageView == null) {
            Log.e(TAG, "loadImage: imageView равен null");
            return;
        }
        
        // Массив резервных изображений для новостей без картинок
        int[] fallbackImages = {
            R.drawable.news1, R.drawable.news2, R.drawable.news3
        };
        
        try {
            // Проверяем URL изображения
            if (TextUtils.isEmpty(imageUrl)) {
                Log.d(TAG, "loadImage: URL пустой, показываем резервное изображение");
                // Если URL пустой, показываем случайное резервное изображение
                int randomIndex = (int) (Math.random() * fallbackImages.length);
                Glide.with(context)
                        .load(fallbackImages[randomIndex])
                        .apply(new RequestOptions()
                                .placeholder(R.drawable.placeholder)
                                .centerCrop())
                        .into(imageView);
                return;
            }
            
            // Проверяем и исправляем URL, если необходимо
            if (imageUrl.startsWith("//")) {
                imageUrl = "https:" + imageUrl;
                Log.d(TAG, "loadImage: исправлен URL изображения: " + imageUrl);
            } else if (!imageUrl.startsWith("http://") && !imageUrl.startsWith("https://")) {
                Log.d(TAG, "loadImage: некорректный URL изображения: " + imageUrl);
                // Показываем резервное изображение для некорректного URL
                int randomIndex = (int) (Math.random() * fallbackImages.length);
                Glide.with(context)
                        .load(fallbackImages[randomIndex])
                        .apply(new RequestOptions()
                                .placeholder(R.drawable.placeholder)
                                .centerCrop())
                        .into(imageView);
                return;
            }

            // Настраиваем параметры загрузки
            RequestOptions options = new RequestOptions()
                    .placeholder(R.drawable.placeholder)
                    .error(fallbackImages[(int) (Math.random() * fallbackImages.length)]) // Показываем резервное изображение при ошибке
                    .diskCacheStrategy(DiskCacheStrategy.ALL) // Кэшируем изображения
                    .centerCrop() // Обрезаем изображение по центру
                    .timeout(10000); // Устанавливаем таймаут 10 секунд

            // Загружаем изображение
            Glide.with(context)
                    .load(imageUrl)
                    .apply(options)
                    .into(imageView);
            
            Log.d(TAG, "loadImage: начата загрузка изображения: " + imageUrl);
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при загрузке изображения: " + imageUrl, e);
            // В случае ошибки показываем резервное изображение
            try {
                int randomIndex = (int) (Math.random() * fallbackImages.length);
                Glide.with(context)
                        .load(fallbackImages[randomIndex])
                        .apply(new RequestOptions()
                                .placeholder(R.drawable.placeholder)
                                .centerCrop())
                        .into(imageView);
            } catch (Exception ex) {
                Log.e(TAG, "Ошибка при загрузке резервного изображения", ex);
                // Последняя попытка - показать простую заглушку
                try {
                    imageView.setImageResource(R.drawable.placeholder);
                } catch (Exception finalEx) {
                    Log.e(TAG, "Критическая ошибка при установке изображения", finalEx);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

    // Метод для получения переведенного описания
    private String getTranslatedDescription(NewsItem newsItem, String language) {
        if ("ru".equals(language)) {
            return newsItem.getTranslatedDescriptionRu() != null && !newsItem.getTranslatedDescriptionRu().isEmpty()
                    ? newsItem.getTranslatedDescriptionRu()
                    : newsItem.getDescription();
        } else if ("ja".equals(language)) {
            return newsItem.getTranslatedDescriptionJp() != null && !newsItem.getTranslatedDescriptionJp().isEmpty()
                    ? newsItem.getTranslatedDescriptionJp()
                    : newsItem.getDescription();
        } else {
            return newsItem.getDescription();
        }
    }

    // Метод для обновления данных в адаптере
    public void updateData(List<NewsItem> newList) {
        Log.d(TAG, "updateData: получено " + (newList != null ? newList.size() : "null") + " новостей");
        
        if (newList == null) {
            Log.e(TAG, "updateData: получен null список");
            return;
        }
        
        // Проверяем, есть ли в списке элементы
        if (newList.isEmpty()) {
            Log.w(TAG, "updateData: получен пустой список");
            // Не очищаем текущий список, если новый пустой
            return;
        }
        
        // Фильтруем некорректные элементы (без заголовка или ссылки)
        List<NewsItem> filteredList = new ArrayList<>();
        for (NewsItem item : newList) {
            if (item != null && 
                item.getTitle() != null && !item.getTitle().isEmpty()) {
                filteredList.add(item);
            } else {
                Log.w(TAG, "updateData: пропущен некорректный элемент: " + 
                    (item != null ? "title=" + item.getTitle() : "null"));
            }
        }
        
        if (filteredList.isEmpty()) {
            Log.w(TAG, "updateData: после фильтрации список пуст");
            return;
        }
        
        Log.d(TAG, "updateData: после фильтрации осталось " + filteredList.size() + " новостей");
        
        try {
            this.newsList.clear();
            this.newsList.addAll(filteredList);
            Log.d(TAG, "updateData: данные обновлены, размер списка: " + this.newsList.size());
        } catch (Exception e) {
            Log.e(TAG, "updateData: ошибка при обновлении данных", e);
            // Если произошла ошибка, создаем новый список
            this.newsList = new ArrayList<>(filteredList);
            Log.d(TAG, "updateData: создан новый список, размер: " + this.newsList.size());
        }
        
        notifyDataSetChanged(); // Уведомляем адаптер о изменении данных
        Log.d(TAG, "updateData: адаптер обновлен, размер списка: " + this.newsList.size());
    }

    // Метод для установки языка перевода
    public void setLanguage(String language) {
        this.language = language;
        notifyDataSetChanged(); // Уведомляем адаптер о изменении данных
    }

    static class NewsViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView titleTextView;
        TextView descriptionTextView;
        TextView sourceTextView; // TextView для отображения источника
        TextView timeTextView; // TextView для отображения времени публикации

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.news_image);
            titleTextView = itemView.findViewById(R.id.news_title);
            descriptionTextView = itemView.findViewById(R.id.news_description);
            sourceTextView = itemView.findViewById(R.id.news_source);
            timeTextView = itemView.findViewById(R.id.news_time);
        }
    }
}