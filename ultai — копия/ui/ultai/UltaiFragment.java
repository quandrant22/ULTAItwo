package com.example.ultai.ui.ultai;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ultai.R;
import com.google.android.material.button.MaterialButton;

public class UltaiFragment extends Fragment {
    private UltaiViewModel viewModel;
    private ChatAdapter adapter;
    private EditText messageEditText;
    private MaterialButton sendButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ultai, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Инициализация views
        RecyclerView recyclerView = view.findViewById(R.id.chatRecyclerView);
        messageEditText = view.findViewById(R.id.messageEditText);
        sendButton = view.findViewById(R.id.sendButton);

        // Настройка RecyclerView
        adapter = new ChatAdapter();
        recyclerView.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

        // Инициализация ViewModel
        viewModel = new ViewModelProvider(this).get(UltaiViewModel.class);

        // Наблюдение за сообщениями
        viewModel.getAllMessages().observe(getViewLifecycleOwner(), messages -> {
            adapter.submitList(messages);
            if (!messages.isEmpty()) {
                recyclerView.smoothScrollToPosition(messages.size() - 1);
            }
        });

        // Наблюдение за состоянием загрузки
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            sendButton.setEnabled(!isLoading);
            messageEditText.setEnabled(!isLoading);
        });

        // Обработка отправки сообщения
        sendButton.setOnClickListener(v -> {
            String message = messageEditText.getText().toString().trim();
            if (!message.isEmpty()) {
                viewModel.sendMessage(message);
                messageEditText.setText("");
            }
        });

        // Настройка поведения клавиатуры
        requireActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }
} 