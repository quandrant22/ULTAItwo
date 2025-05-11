package com.example.ultai.ultai;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SeekBarPreference;

import com.example.ultai.R;

/**
 * Активность настроек приложения
 */
public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Настройки");
        }
    }

    /**
     * Фрагмент настроек
     */
    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);
            
            // Настройка отображения текущих значений в саммари
            setupPreferenceSummaries();
            
            // Настройка обработчиков изменений
            setupPreferenceListeners();
        }
        
        /**
         * Настройка отображения текущих значений в саммари
         */
        private void setupPreferenceSummaries() {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
            
            // API ключи
            setupApiKeySummary("deepseek_api_key", prefs);
            setupApiKeySummary("claude_api_key", prefs);
            
            // Модели
            setupListPreferenceSummary("preferred_model");
            setupListPreferenceSummary("claude_model");
            
            // Температура
            SeekBarPreference temperaturePref = findPreference("temperature");
            if (temperaturePref != null) {
                temperaturePref.setSummary("Текущее значение: " + (temperaturePref.getValue() / 100.0));
            }
            
            // Версия приложения
            Preference versionPref = findPreference("app_version");
            if (versionPref != null) {
                try {
                    String versionName = requireContext().getPackageManager()
                            .getPackageInfo(requireContext().getPackageName(), 0).versionName;
                    versionPref.setSummary(versionName);
                } catch (Exception e) {
                    Log.e(TAG, "Ошибка при получении версии приложения", e);
                }
            }
        }
        
        /**
         * Настройка обработчиков изменений
         */
        private void setupPreferenceListeners() {
            // Обработчик изменения API ключа DeepSeek
            EditTextPreference deepseekKeyPref = findPreference("deepseek_api_key");
            if (deepseekKeyPref != null) {
                deepseekKeyPref.setOnPreferenceChangeListener((preference, newValue) -> {
                    String apiKey = (String) newValue;
                    setupMaskedSummary(preference, apiKey);
                    return true;
                });
            }
            
            // Обработчик изменения API ключа Claude
            EditTextPreference claudeKeyPref = findPreference("claude_api_key");
            if (claudeKeyPref != null) {
                claudeKeyPref.setOnPreferenceChangeListener((preference, newValue) -> {
                    String apiKey = (String) newValue;
                    setupMaskedSummary(preference, apiKey);
                    return true;
                });
            }
            
            // Обработчик изменения предпочтительной модели
            ListPreference modelPref = findPreference("preferred_model");
            if (modelPref != null) {
                modelPref.setOnPreferenceChangeListener((preference, newValue) -> {
                    String value = (String) newValue;
                    int index = modelPref.findIndexOfValue(value);
                    if (index >= 0) {
                        preference.setSummary(modelPref.getEntries()[index]);
                    }
                    return true;
                });
            }
            
            // Обработчик изменения модели Claude
            ListPreference claudeModelPref = findPreference("claude_model");
            if (claudeModelPref != null) {
                claudeModelPref.setOnPreferenceChangeListener((preference, newValue) -> {
                    String value = (String) newValue;
                    int index = claudeModelPref.findIndexOfValue(value);
                    if (index >= 0) {
                        preference.setSummary(claudeModelPref.getEntries()[index]);
                    }
                    return true;
                });
            }
            
            // Обработчик изменения температуры
            SeekBarPreference temperaturePref = findPreference("temperature");
            if (temperaturePref != null) {
                temperaturePref.setOnPreferenceChangeListener((preference, newValue) -> {
                    int value = (int) newValue;
                    preference.setSummary("Текущее значение: " + (value / 100.0));
                    return true;
                });
            }
        }
        
        /**
         * Настройка отображения API ключа в саммари (маскированный вид)
         */
        private void setupApiKeySummary(String prefKey, SharedPreferences prefs) {
            EditTextPreference preference = findPreference(prefKey);
            if (preference != null) {
                String apiKey = prefs.getString(prefKey, "");
                setupMaskedSummary(preference, apiKey);
            }
        }
        
        /**
         * Настройка отображения маскированного значения в саммари
         */
        private void setupMaskedSummary(Preference preference, String value) {
            if (value != null && !value.isEmpty()) {
                // Маскируем ключ API, показывая только первые и последние 4 символа
                String maskedKey;
                if (value.length() > 8) {
                    maskedKey = value.substring(0, 4) + "..." + value.substring(value.length() - 4);
                } else {
                    maskedKey = value;
                }
                preference.setSummary("Установлен: " + maskedKey);
            } else {
                preference.setSummary("Не установлен");
            }
        }
        
        /**
         * Настройка отображения текущего значения в саммари для ListPreference
         */
        private void setupListPreferenceSummary(String prefKey) {
            ListPreference preference = findPreference(prefKey);
            if (preference != null) {
                String value = preference.getValue();
                int index = preference.findIndexOfValue(value);
                if (index >= 0) {
                    preference.setSummary(preference.getEntries()[index]);
                }
            }
        }
    }
} 