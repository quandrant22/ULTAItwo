package com.example.ultai.planer.stages.stage_1;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.ultai20.R;

public class Step5Fragment extends Fragment {

    // Покупательские привычки
    private CheckBox checkBoxOnlineShopping;
    private CheckBox checkBoxOfflineShopping;
    private CheckBox checkBoxMixedShopping;
    private CheckBox checkBoxBrandedProducts;
    private CheckBox checkBoxNonBrandedProducts;
    private CheckBox checkBoxQualityPreference;
    private CheckBox checkBoxPricePreference;
    private CheckBox checkBoxConveniencePreference;
    private CheckBox checkBoxWithReviews;
    private CheckBox checkBoxWithoutReviews;
    private CheckBox checkBoxEcoEthical;
    private CheckBox checkBoxFashionTrends;
    private CheckBox checkBoxPersonalNeeds;
    private CheckBox checkBoxSocialGroupBelonging;
    private CheckBox checkBoxSeasonalDiscounts;
    
    // Сценарии использования
    private CheckBox checkBoxDailyUsage;
    private CheckBox checkBoxAsNeededUsage;
    private CheckBox checkBoxRareUsage;
    private CheckBox checkBoxHomeUsage;
    private CheckBox checkBoxOfficeUsage;
    private CheckBox checkBoxTravelUsage;
    private CheckBox checkBoxPersonalUsage;
    private CheckBox checkBoxGiftUsage;
    private CheckBox checkBoxCommercialUsage;
    
    // Частота и сезонность покупок
    private CheckBox checkBoxDailyPurchases;
    private CheckBox checkBoxWeeklyPurchases;
    private CheckBox checkBoxMonthlyPurchases;
    private CheckBox checkBoxWeekdayPurchases;
    private CheckBox checkBoxWeekendPurchases;
    private CheckBox checkBoxPreHolidayPurchases;
    private CheckBox checkBoxSeasonalPurchases;
    private CheckBox checkBoxSalaryPurchases;
    private CheckBox checkBoxEventPurchases;
    private CheckBox checkBoxDiscountPurchases;
    
    // Способы принятия решений
    private RadioGroup radioGroupDecisionMaking;
    
    // Критерии выбора продукта
    private CheckBox checkBoxPriceCriteria;
    private CheckBox checkBoxQualityCriteria;
    private CheckBox checkBoxBrandCriteria;
    private CheckBox checkBoxConvenienceCriteria;
    private CheckBox checkBoxFeaturesCriteria;
    private CheckBox checkBoxDesignCriteria;
    private CheckBox checkBoxExperienceCriteria;
    private CheckBox checkBoxEcoCriteria;
    private CheckBox checkBoxPersonalCriteria;
    private CheckBox checkBoxRecommendationCriteria;
    
    // Поле с важными факторами
    private EditText editTextImportantFactors;
    
    // Платформы и каналы потребления
    private CheckBox checkBoxYouTube;
    private CheckBox checkBoxInstagram;
    private CheckBox checkBoxVK;
    private CheckBox checkBoxTwitter;
    private CheckBox checkBoxBlogs;
    private CheckBox checkBoxPodcasts;
    private CheckBox checkBoxStreaming;
    private CheckBox checkBoxNewsletter;
    
    // Источники информации
    private CheckBox checkBoxSocialNetworks;
    private CheckBox checkBoxSearchEngines;
    private CheckBox checkBoxReviews;
    private CheckBox checkBoxAdvertising;
    
    // Предпочтения в контенте
    private CheckBox checkBoxVideoContent;
    private CheckBox checkBoxTextContent;
    private CheckBox checkBoxAudioContent;
    private CheckBox checkBoxGraphicsContent;
    private CheckBox checkBoxInteractiveContent;
    
    private Button buttonFinishStep5;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Инициализация макета
        View view = inflater.inflate(R.layout.fragment_step5, container, false);

        // Инициализация компонентов UI
        initializeUI(view);

        // Настройка кнопки завершения шага
        buttonFinishStep5.setOnClickListener(v -> {
            if (validateForm()) {
                saveFormData();
                // Показываем уведомление об успешном завершении
                Toast.makeText(requireContext(), "Поведенческие характеристики сохранены!", Toast.LENGTH_SHORT).show();
                
                // Возвращаемся к списку этапов
                NavController navController = Navigation.findNavController(requireView());
                navController.navigate(R.id.action_step5Fragment_to_faza1_stages);
            }
        });

        return view;
    }

    /**
     * Инициализация компонентов UI
     */
    private void initializeUI(View view) {
        // Инициализация чекбоксов для покупательских привычек
        checkBoxOnlineShopping = view.findViewById(R.id.checkBoxOnlineShopping);
        checkBoxOfflineShopping = view.findViewById(R.id.checkBoxOfflineShopping);
        checkBoxMixedShopping = view.findViewById(R.id.checkBoxMixedShopping);
        checkBoxBrandedProducts = view.findViewById(R.id.checkBoxBrandedProducts);
        checkBoxNonBrandedProducts = view.findViewById(R.id.checkBoxNonBrandedProducts);
        checkBoxQualityPreference = view.findViewById(R.id.checkBoxQualityPreference);
        checkBoxPricePreference = view.findViewById(R.id.checkBoxPricePreference);
        checkBoxConveniencePreference = view.findViewById(R.id.checkBoxConveniencePreference);
        checkBoxWithReviews = view.findViewById(R.id.checkBoxWithReviews);
        checkBoxWithoutReviews = view.findViewById(R.id.checkBoxWithoutReviews);
        checkBoxEcoEthical = view.findViewById(R.id.checkBoxEcoEthical);
        checkBoxFashionTrends = view.findViewById(R.id.checkBoxFashionTrends);
        checkBoxPersonalNeeds = view.findViewById(R.id.checkBoxPersonalNeeds);
        checkBoxSocialGroupBelonging = view.findViewById(R.id.checkBoxSocialGroupBelonging);
        checkBoxSeasonalDiscounts = view.findViewById(R.id.checkBoxSeasonalDiscounts);
        
        // Инициализация чекбоксов для сценариев использования
        checkBoxDailyUsage = view.findViewById(R.id.checkBoxDailyUsage);
        checkBoxAsNeededUsage = view.findViewById(R.id.checkBoxAsNeededUsage);
        checkBoxRareUsage = view.findViewById(R.id.checkBoxRareUsage);
        checkBoxHomeUsage = view.findViewById(R.id.checkBoxHomeUsage);
        checkBoxOfficeUsage = view.findViewById(R.id.checkBoxOfficeUsage);
        checkBoxTravelUsage = view.findViewById(R.id.checkBoxTravelUsage);
        checkBoxPersonalUsage = view.findViewById(R.id.checkBoxPersonalUsage);
        checkBoxGiftUsage = view.findViewById(R.id.checkBoxGiftUsage);
        checkBoxCommercialUsage = view.findViewById(R.id.checkBoxCommercialUsage);
        
        // Инициализация чекбоксов для частоты и сезонности покупок
        checkBoxDailyPurchases = view.findViewById(R.id.checkBoxDailyPurchases);
        checkBoxWeeklyPurchases = view.findViewById(R.id.checkBoxWeeklyPurchases);
        checkBoxMonthlyPurchases = view.findViewById(R.id.checkBoxMonthlyPurchases);
        checkBoxWeekdayPurchases = view.findViewById(R.id.checkBoxWeekdayPurchases);
        checkBoxWeekendPurchases = view.findViewById(R.id.checkBoxWeekendPurchases);
        checkBoxPreHolidayPurchases = view.findViewById(R.id.checkBoxPreHolidayPurchases);
        checkBoxSeasonalPurchases = view.findViewById(R.id.checkBoxSeasonalPurchases);
        checkBoxSalaryPurchases = view.findViewById(R.id.checkBoxSalaryPurchases);
        checkBoxEventPurchases = view.findViewById(R.id.checkBoxEventPurchases);
        checkBoxDiscountPurchases = view.findViewById(R.id.checkBoxDiscountPurchases);
        
        // Инициализация радиогруппы для способов принятия решений
        radioGroupDecisionMaking = view.findViewById(R.id.radioGroupDecisionMaking);
        
        // Инициализация чекбоксов для критериев выбора продукта
        checkBoxPriceCriteria = view.findViewById(R.id.checkBoxPriceCriteria);
        checkBoxQualityCriteria = view.findViewById(R.id.checkBoxQualityCriteria);
        checkBoxBrandCriteria = view.findViewById(R.id.checkBoxBrandCriteria);
        checkBoxConvenienceCriteria = view.findViewById(R.id.checkBoxConvenienceCriteria);
        checkBoxFeaturesCriteria = view.findViewById(R.id.checkBoxFeaturesCriteria);
        checkBoxDesignCriteria = view.findViewById(R.id.checkBoxDesignCriteria);
        checkBoxExperienceCriteria = view.findViewById(R.id.checkBoxExperienceCriteria);
        checkBoxEcoCriteria = view.findViewById(R.id.checkBoxEcoCriteria);
        checkBoxPersonalCriteria = view.findViewById(R.id.checkBoxPersonalCriteria);
        checkBoxRecommendationCriteria = view.findViewById(R.id.checkBoxRecommendationCriteria);
        
        // Инициализация поля для важных факторов
        editTextImportantFactors = view.findViewById(R.id.editTextImportantFactors);
        
        // Инициализация чекбоксов для платформ и каналов потребления
        checkBoxYouTube = view.findViewById(R.id.checkBoxYouTube);
        checkBoxInstagram = view.findViewById(R.id.checkBoxInstagram);
        checkBoxVK = view.findViewById(R.id.checkBoxVK);
        checkBoxTwitter = view.findViewById(R.id.checkBoxTwitter);
        checkBoxBlogs = view.findViewById(R.id.checkBoxBlogs);
        checkBoxPodcasts = view.findViewById(R.id.checkBoxPodcasts);
        checkBoxStreaming = view.findViewById(R.id.checkBoxStreaming);
        checkBoxNewsletter = view.findViewById(R.id.checkBoxNewsletter);
        
        // Инициализация чекбоксов для источников информации
        checkBoxSocialNetworks = view.findViewById(R.id.checkBoxSocialNetworks);
        checkBoxSearchEngines = view.findViewById(R.id.checkBoxSearchEngines);
        checkBoxReviews = view.findViewById(R.id.checkBoxReviews);
        checkBoxAdvertising = view.findViewById(R.id.checkBoxAdvertising);
        
        // Инициализация чекбоксов для предпочтений в контенте
        checkBoxVideoContent = view.findViewById(R.id.checkBoxVideoContent);
        checkBoxTextContent = view.findViewById(R.id.checkBoxTextContent);
        checkBoxAudioContent = view.findViewById(R.id.checkBoxAudioContent);
        checkBoxGraphicsContent = view.findViewById(R.id.checkBoxGraphicsContent);
        checkBoxInteractiveContent = view.findViewById(R.id.checkBoxInteractiveContent);
        
        buttonFinishStep5 = view.findViewById(R.id.buttonFinishStep5);
    }

    /**
     * Валидация формы перед сохранением
     */
    private boolean validateForm() {
        boolean isValid = true;
        
        // Проверка выбора хотя бы одной покупательской привычки
        if (!checkBoxOnlineShopping.isChecked() && 
            !checkBoxOfflineShopping.isChecked() && 
            !checkBoxMixedShopping.isChecked() &&
            !checkBoxBrandedProducts.isChecked() &&
            !checkBoxNonBrandedProducts.isChecked() &&
            !checkBoxQualityPreference.isChecked() &&
            !checkBoxPricePreference.isChecked() &&
            !checkBoxConveniencePreference.isChecked() &&
            !checkBoxWithReviews.isChecked() &&
            !checkBoxWithoutReviews.isChecked() &&
            !checkBoxEcoEthical.isChecked() &&
            !checkBoxFashionTrends.isChecked() &&
            !checkBoxPersonalNeeds.isChecked() &&
            !checkBoxSocialGroupBelonging.isChecked() &&
            !checkBoxSeasonalDiscounts.isChecked()) {
            Toast.makeText(requireContext(), "Выберите хотя бы одну покупательскую привычку", Toast.LENGTH_SHORT).show();
            isValid = false;
        }
        
        // Проверка выбора хотя бы одного сценария использования
        if (!checkBoxDailyUsage.isChecked() && 
            !checkBoxAsNeededUsage.isChecked() && 
            !checkBoxRareUsage.isChecked() &&
            !checkBoxHomeUsage.isChecked() &&
            !checkBoxOfficeUsage.isChecked() &&
            !checkBoxTravelUsage.isChecked() &&
            !checkBoxPersonalUsage.isChecked() &&
            !checkBoxGiftUsage.isChecked() &&
            !checkBoxCommercialUsage.isChecked()) {
            Toast.makeText(requireContext(), "Выберите хотя бы один сценарий использования", Toast.LENGTH_SHORT).show();
            isValid = false;
        }
        
        // Проверка выбора способа принятия решений
        if (radioGroupDecisionMaking.getCheckedRadioButtonId() == -1) {
            Toast.makeText(requireContext(), "Выберите способ принятия решений о покупке", Toast.LENGTH_SHORT).show();
            isValid = false;
        }
        
        // Проверка выбора хотя бы одного критерия для выбора продукта
        if (!checkBoxPriceCriteria.isChecked() && 
            !checkBoxQualityCriteria.isChecked() && 
            !checkBoxBrandCriteria.isChecked() &&
            !checkBoxConvenienceCriteria.isChecked() &&
            !checkBoxFeaturesCriteria.isChecked() &&
            !checkBoxDesignCriteria.isChecked() &&
            !checkBoxExperienceCriteria.isChecked() &&
            !checkBoxEcoCriteria.isChecked() &&
            !checkBoxPersonalCriteria.isChecked() &&
            !checkBoxRecommendationCriteria.isChecked()) {
            Toast.makeText(requireContext(), "Выберите хотя бы один критерий выбора продукта", Toast.LENGTH_SHORT).show();
            isValid = false;
        }
        
        // Проверка выбора хотя бы одного канала коммуникации
        if (!checkBoxYouTube.isChecked() && 
            !checkBoxInstagram.isChecked() && 
            !checkBoxVK.isChecked() &&
            !checkBoxTwitter.isChecked() &&
            !checkBoxBlogs.isChecked() &&
            !checkBoxPodcasts.isChecked() &&
            !checkBoxStreaming.isChecked()) {
            Toast.makeText(requireContext(), "Выберите хотя бы один канал коммуникации", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        return isValid;
    }

    /**
     * Сохранение данных формы
     */
    private void saveFormData() {
        // Собираем данные о покупательских привычках
        StringBuilder habits = new StringBuilder();
        if (checkBoxOnlineShopping.isChecked()) habits.append("Предпочтение онлайн-покупок, ");
        if (checkBoxOfflineShopping.isChecked()) habits.append("Офлайн-покупок, ");
        if (checkBoxMixedShopping.isChecked()) habits.append("Смешанный тип покупок, ");
        if (checkBoxBrandedProducts.isChecked()) habits.append("Покупка товаров определенных брендов, ");
        if (checkBoxNonBrandedProducts.isChecked()) habits.append("Покупка товаров без привязки к бренду, ");
        if (checkBoxQualityPreference.isChecked()) habits.append("Предпочтение качества товара, ");
        if (checkBoxPricePreference.isChecked()) habits.append("Предпочтение цены, ");
        if (checkBoxConveniencePreference.isChecked()) habits.append("Предпочтение удобства покупки, ");
        if (checkBoxWithReviews.isChecked()) habits.append("Покупка товаров с учетом отзывов и рекомендаций, ");
        if (checkBoxWithoutReviews.isChecked()) habits.append("Без учета отзывов, ");
        if (checkBoxEcoEthical.isChecked()) habits.append("Покупка товаров с учетом экологических и этических аспектов, ");
        if (checkBoxFashionTrends.isChecked()) habits.append("Покупка товаров с учетом текущих модных тенденций, ");
        if (checkBoxPersonalNeeds.isChecked()) habits.append("Покупка товаров с учетом личных потребностей и вкусов, ");
        if (checkBoxSocialGroupBelonging.isChecked()) habits.append("Покупка товаров с учетом принадлежности к определенной группе, ");
        if (checkBoxSeasonalDiscounts.isChecked()) habits.append("Предпочтение сезонных распродаж и скидок, ");
        
        // Собираем данные о сценариях использования
        StringBuilder usageScenarios = new StringBuilder();
        if (checkBoxDailyUsage.isChecked()) usageScenarios.append("Ежедневное использование, ");
        if (checkBoxAsNeededUsage.isChecked()) usageScenarios.append("Использование по мере необходимости, ");
        if (checkBoxRareUsage.isChecked()) usageScenarios.append("Редкое использование, ");
        if (checkBoxHomeUsage.isChecked()) usageScenarios.append("Использование дома, ");
        if (checkBoxOfficeUsage.isChecked()) usageScenarios.append("В офисе, ");
        if (checkBoxTravelUsage.isChecked()) usageScenarios.append("В путешествиях, ");
        if (checkBoxPersonalUsage.isChecked()) usageScenarios.append("Использование для личных нужд, ");
        if (checkBoxGiftUsage.isChecked()) usageScenarios.append("Подарочное использование, ");
        if (checkBoxCommercialUsage.isChecked()) usageScenarios.append("Использование в коммерческих целях, ");
        
        // Собираем данные о частоте и сезонности покупок
        StringBuilder purchaseFrequency = new StringBuilder();
        if (checkBoxDailyPurchases.isChecked()) purchaseFrequency.append("Ежедневные, ");
        if (checkBoxWeeklyPurchases.isChecked()) purchaseFrequency.append("Еженедельные, ");
        if (checkBoxMonthlyPurchases.isChecked()) purchaseFrequency.append("Ежемесячные покупки, ");
        if (checkBoxWeekdayPurchases.isChecked()) purchaseFrequency.append("Покупки в будние дни, ");
        if (checkBoxWeekendPurchases.isChecked()) purchaseFrequency.append("В выходные дни, ");
        if (checkBoxPreHolidayPurchases.isChecked()) purchaseFrequency.append("Покупки в предпраздничные дни, ");
        if (checkBoxSeasonalPurchases.isChecked()) purchaseFrequency.append("Покупки в зависимости от сезона, ");
        if (checkBoxSalaryPurchases.isChecked()) purchaseFrequency.append("Покупки в соответствии с получением дохода, ");
        if (checkBoxEventPurchases.isChecked()) purchaseFrequency.append("Покупки в связи с определенными событиями, ");
        if (checkBoxDiscountPurchases.isChecked()) purchaseFrequency.append("Покупки в период скидок и распродаж, ");
        
        // Получаем выбранный способ принятия решений
        String decisionMaking = "";
        int decisionMakingId = radioGroupDecisionMaking.getCheckedRadioButtonId();
        if (decisionMakingId == R.id.radioRationalDecision) {
            decisionMaking = "Рациональные (сравнение цен и характеристик товаров)";
        } else if (decisionMakingId == R.id.radioImpulsiveDecision) {
            decisionMaking = "Импульсивные (покупки под влиянием эмоций)";
        } else if (decisionMakingId == R.id.radioRecommendedDecision) {
            decisionMaking = "Основанные на рекомендациях других";
        }
        
        // Собираем данные о критериях выбора продукта
        StringBuilder selectionCriteria = new StringBuilder();
        if (checkBoxPriceCriteria.isChecked()) selectionCriteria.append("Цена и стоимость, ");
        if (checkBoxQualityCriteria.isChecked()) selectionCriteria.append("Качество продукта или услуги, ");
        if (checkBoxBrandCriteria.isChecked()) selectionCriteria.append("Бренд и репутация, ");
        if (checkBoxConvenienceCriteria.isChecked()) selectionCriteria.append("Доступность и удобство, ");
        if (checkBoxFeaturesCriteria.isChecked()) selectionCriteria.append("Характеристики и функциональность, ");
        if (checkBoxDesignCriteria.isChecked()) selectionCriteria.append("Дизайн и эстетика, ");
        if (checkBoxExperienceCriteria.isChecked()) selectionCriteria.append("Опыт пользования и обслуживания, ");
        if (checkBoxEcoCriteria.isChecked()) selectionCriteria.append("Экологические и социальные аспекты, ");
        if (checkBoxPersonalCriteria.isChecked()) selectionCriteria.append("Личные предпочтения и вкусы, ");
        if (checkBoxRecommendationCriteria.isChecked()) selectionCriteria.append("Рекомендации и реклама, ");
        
        // Получаем важные факторы при принятии решения
        String importantFactors = editTextImportantFactors.getText().toString().trim();
        
        // Собираем данные о платформах и каналах потребления
        StringBuilder platforms = new StringBuilder();
        if (checkBoxYouTube.isChecked()) platforms.append("YouTube, ");
        if (checkBoxInstagram.isChecked()) platforms.append("Instagram, ");
        if (checkBoxVK.isChecked()) platforms.append("Вконтакте, ");
        if (checkBoxTwitter.isChecked()) platforms.append("X(Твиттер), ");
        if (checkBoxBlogs.isChecked()) platforms.append("Блоги, ");
        if (checkBoxPodcasts.isChecked()) platforms.append("Подкасты, ");
        if (checkBoxStreaming.isChecked()) platforms.append("Стриминговые сервисы, ");
        if (checkBoxNewsletter.isChecked()) platforms.append("Рассылка, ");
        
        // Собираем данные об источниках информации
        StringBuilder infoSources = new StringBuilder();
        if (checkBoxSocialNetworks.isChecked()) infoSources.append("Социальные сети, ");
        if (checkBoxSearchEngines.isChecked()) infoSources.append("Поисковые системы, ");
        if (checkBoxReviews.isChecked()) infoSources.append("Отзывы потребителей, ");
        if (checkBoxAdvertising.isChecked()) infoSources.append("Реклама, ");
        
        // Собираем данные о предпочтениях в контенте
        StringBuilder contentPreferences = new StringBuilder();
        if (checkBoxVideoContent.isChecked()) contentPreferences.append("Видео, ");
        if (checkBoxTextContent.isChecked()) contentPreferences.append("Текст, ");
        if (checkBoxAudioContent.isChecked()) contentPreferences.append("Аудио, ");
        if (checkBoxGraphicsContent.isChecked()) contentPreferences.append("Графика, ");
        if (checkBoxInteractiveContent.isChecked()) contentPreferences.append("Интерактивный контент, ");
        
        // В реальном приложении здесь сохраняем данные в ViewModel или БД
        
        // Сохраняем в SharedPreferences что шаг выполнен
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("steps_completion", Context.MODE_PRIVATE);
        sharedPreferences.edit()
            .putBoolean("step5_completed", true)
            .apply();
    }
} 