package com.example.ultai.ui.ultai;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ultai.R;
import com.example.ultai.data.model.Message;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class ChatAdapter extends ListAdapter<Message, ChatAdapter.MessageViewHolder> {
    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public ChatAdapter() {
        super(new MessageDiffCallback());
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = getItem(position);
        holder.bind(message);
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView messageText;
        private final TextView timeText;
        private final MaterialCardView messageCard;
        private final ConstraintLayout container;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
            timeText = itemView.findViewById(R.id.timeText);
            messageCard = itemView.findViewById(R.id.messageCard);
            container = (ConstraintLayout) itemView;
        }

        public void bind(Message message) {
            messageText.setText(message.getContent());
            timeText.setText(timeFormat.format(message.getTimestamp()));

            // Настраиваем внешний вид сообщения в зависимости от отправителя
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) messageCard.getLayoutParams();
            if (message.isUser()) {
                messageCard.setCardBackgroundColor(itemView.getContext().getColor(R.color.user_message_background));
                params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
                params.startToStart = ConstraintLayout.LayoutParams.UNSET;
                params.setMarginStart(48);
                params.setMarginEnd(8);
            } else {
                messageCard.setCardBackgroundColor(itemView.getContext().getColor(R.color.assistant_message_background));
                params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
                params.endToEnd = ConstraintLayout.LayoutParams.UNSET;
                params.setMarginStart(8);
                params.setMarginEnd(48);
            }
            messageCard.setLayoutParams(params);
        }
    }

    static class MessageDiffCallback extends DiffUtil.ItemCallback<Message> {
        @Override
        public boolean areItemsTheSame(@NonNull Message oldItem, @NonNull Message newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Message oldItem, @NonNull Message newItem) {
            return oldItem.getContent().equals(newItem.getContent()) &&
                   oldItem.isUser() == newItem.isUser() &&
                   oldItem.getTimestamp().equals(newItem.getTimestamp());
        }
    }
} 