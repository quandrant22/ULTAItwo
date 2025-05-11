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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.ultai.R;
import com.example.ultai.databinding.FragmentAnketaBinding;

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

        questionnaireManager = new QuestionnaireManager();
        AnketaViewModel anketaViewModel = new ViewModelProvider(this).get(AnketaViewModel.class);

        Button startButton = binding.startbutton;
        anketaViewModel.getText().observe(getViewLifecycleOwner(), startButton::setText);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (binding != null) {
            populateQuestionnaire(binding.questionnaireLayout);

            Button buttonNext = binding.startbutton;
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
        try {
            int childCount = binding.questionnaireLayout.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = binding.questionnaireLayout.getChildAt(i);
                if (child instanceof EditText) {
                    EditText editText = (EditText) child;
                    formData.put("answer" + i, editText.getText().toString().trim());
                } else if (child instanceof Spinner) {
                    Spinner spinner = (Spinner) child;
                    formData.put("answer" + i, spinner.getSelectedItem().toString());
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
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
