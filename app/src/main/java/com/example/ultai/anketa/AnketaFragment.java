package com.example.ultai.anketa;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.ultai20.R;
import com.example.ultai20.databinding.FragmentAnketaBinding;
import com.example.ultai20.models.Questionnaire;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AnketaFragment extends Fragment {

    private FragmentAnketaBinding binding;
    private QuestionnaireManager questionnaireManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentAnketaBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        AnketaViewModel anketaViewModel = new ViewModelProvider(this).get(AnketaViewModel.class);


        Button submitBtn = binding.submitButton;

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (binding != null) {

            Button buttonNext = binding.submitButton;
            buttonNext.setOnClickListener(v -> {
                JSONObject formData = collectFormData();
                if (formData.length() > 0) {
                    postFormData(formData);
                } else {
                    Toast.makeText(getContext(), "Форма не заполнена!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // Метод для сбора данных из формы
    private JSONObject collectFormData() {
        JSONObject formData = new JSONObject();
        if (binding == null) {
            return formData;
        }

        try {
            // Вопрос 1: Название компании
            formData.put("companyName", binding.companyNameEditText.getText().toString().trim());

            // Вопрос 2: Текущее состояние бизнеса
            if (binding.statusPlanningRadioButton.isChecked()) {
                formData.put("businessStatus", "Планирую запустить");
            } else if (binding.statusLaunchedRadioButton.isChecked()) {
                formData.put("businessStatus", "Уже запущен");
            } else {
                formData.put("businessStatus", ""); // Или другое значение по умолчанию/ошибка
            }

            // Вопрос 3: Страна
            formData.put("country", binding.countrySpinner.getSelectedItem().toString());

            // Вопрос 4: Тип деятельности
            if (binding.typeGoodsRadioButton.isChecked()) {
                formData.put("businessType", "Товары");
            } else if (binding.typeServicesRadioButton.isChecked()) {
                formData.put("businessType", "Услуги");
            } else {
                formData.put("businessType", "");
            }

            // Вопрос 5: Описание услуг/товаров
            formData.put("servicesDescription", binding.servicesEditText.getText().toString().trim());

            // Вопрос 6: География реализации (страна)
            formData.put("geoCountry", binding.geoCountrySpinner.getSelectedItem().toString());

            // Вопрос ?: Город реализации (предполагаем, что он связан с geoCountrySpinner)
            // В XML id для города - geoCitySpinner
            formData.put("geoCity", binding.geoCitySpinner.getSelectedItem().toString());

            // Добавьте сюда сбор данных для остальных вопросов, если они есть в XML
            // Убедитесь, что имена ключей ("companyName", "businessStatus" и т.д.) 
            // соответствуют тому, что ожидает ваш бэкенд (Google Apps Script)

        } catch (JSONException e) {
            e.printStackTrace();
            // Показываем ошибку пользователю
            Toast.makeText(getContext(), "Ошибка при сборе данных формы", Toast.LENGTH_SHORT).show();
        } catch (NullPointerException npe) {
            // Обработка случая, когда элемент binding или его свойство равно null
            npe.printStackTrace();
            Toast.makeText(getContext(), "Ошибка: элемент формы не найден", Toast.LENGTH_SHORT).show();
        }
        return formData;
    }

    // Метод для отправки данных на сервер
    private void postFormData(JSONObject formData) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
        RequestBody body = RequestBody.create(
                formData.toString(), MediaType.get("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url("https://script.google.com/macros/s/AKfycbz764JkyX03OQAnYWr7jIZIUe8gynmG-CSNLpr8iGtRyTo3pPb8gCwxuPklT_6U8Ezc/exec")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Ошибка отправки данных!", Toast.LENGTH_SHORT).show()
                    );
                }
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    // Обработка неудачного ответа
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "Ошибка сервера: " + response.code(), Toast.LENGTH_SHORT).show()
                        );
                    }
                } else {
                    // Обработка успешного ответа
                    final String responseData = response.body().string();
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Данные успешно отправлены!", Toast.LENGTH_SHORT).show();
                            NavController navController = Navigation.findNavController(requireView());
                            navController.navigate(R.id.action_anketaFragment_to_navigation_home);
                        });
                    }
                }
            }
        });
    }

    // Заполнение формы вопросами и элементами UI
    private void populateQuestionnaire(LinearLayout layout) {
        List<QuestionnaireItem> questionnaireOneList = questionnaireManager.getQuestionnaireOneList();
        for (QuestionnaireItem item : questionnaireOneList) {
            // Вопрос
            TextView questionText = new TextView(getContext());
            questionText.setText(item.question[0]);
            layout.addView(questionText);

            // Поле ввода или выпадающий список
            if ("textArea".equals(item.type)) {
                EditText editText = new EditText(getContext());
                editText.setHint("Введите ваш ответ...");
                layout.addView(editText);
            } else if ("select".equals(item.type) || "dropDown".equals(item.type)) {
                Spinner spinner = new Spinner(getContext());
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        getContext(), android.R.layout.simple_spinner_item, item.answers);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);
                layout.addView(spinner);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
