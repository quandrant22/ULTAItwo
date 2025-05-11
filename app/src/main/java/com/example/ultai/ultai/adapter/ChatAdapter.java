package com.example.ultai.ultai.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ultai20.R;
import com.example.ultai.ultai.model.ChatMessage;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {

    private Context context;
    private List<ChatMessage> messages;
    private OnMoreInfoClickListener moreInfoClickListener;

    public interface OnMoreInfoClickListener {
        void onMoreInfoClick(int position);
    }

    public ChatAdapter(Context context, List<ChatMessage> messages) {
        this.context = context;
        this.messages = messages != null ? messages : new ArrayList<>();
    }

    public void setMoreInfoClickListener(OnMoreInfoClickListener listener) {
        this.moreInfoClickListener = listener;
    }

    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages != null ? messages : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == 0) {
            view = LayoutInflater.from(context).inflate(R.layout.item_chat_user, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.item_chat_bot, parent, false);
        }
        return new MessageViewHolder(view, viewType, this);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        holder.messageTextView.setText(message.getMessage());
        holder.timeText.setText(message.getTime());
        
        // Показываем "Подробнее" только для сообщений бота и если у сообщения есть дополнительная информация
        if (!message.isUser() && holder.moreInfoContainer != null) {
            if (message.getMoreInfo() != null && !message.getMoreInfo().isEmpty()) {
                holder.moreInfoContainer.setVisibility(View.VISIBLE);
            } else {
                holder.moreInfoContainer.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).isUser() ? 0 : 1;
    }

    class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView;
        TextView timeText;
        LinearLayout moreInfoContainer;
        int viewType;
        ChatAdapter adapter;

        MessageViewHolder(@NonNull View itemView, int viewType, ChatAdapter adapter) {
            super(itemView);
            this.viewType = viewType;
            this.adapter = adapter;
            messageTextView = itemView.findViewById(R.id.messageTextView);
            timeText = itemView.findViewById(R.id.timeText);
            
            // Кнопка "Подробнее" только для сообщений бота
            if (viewType == 1) {
                moreInfoContainer = itemView.findViewById(R.id.moreInfoContainer);
                if (moreInfoContainer != null) {
                    moreInfoContainer.setOnClickListener(v -> {
                        if (adapter.moreInfoClickListener != null) {
                            adapter.moreInfoClickListener.onMoreInfoClick(getAdapterPosition());
                        }
                    });
                }
            }
        }
    }
}