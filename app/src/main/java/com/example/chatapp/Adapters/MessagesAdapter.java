package com.example.chatapp.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapp.Models.Message;
import com.example.chatapp.R;
import com.example.chatapp.databinding.ItemRecieveBinding;
import com.example.chatapp.databinding.ItemSendBinding;
import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class MessagesAdapter extends RecyclerView.Adapter {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseDatabase db = FirebaseDatabase.getInstance();

    Context context;
    ArrayList<Message> messages;

    String senderRoom;
    String receiverRoom;

    final int ITEM_SENT = 1;
    final int ITEM_RECEIVE = 2;

    public MessagesAdapter(Context context, ArrayList<Message> messages, String senderRoom, String receiverRoom) {
        this.context = context;
        this.messages = messages;
        this.senderRoom = senderRoom;
        this.receiverRoom = receiverRoom;
    }

    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        if(viewType == ITEM_SENT) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_send, parent, false);
            return new SentViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_recieve, parent, false);
            return new ReceiveViewHolder(view);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull @NotNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);

        int reactions[] = new int[]{
                R.drawable.ic_fb_like,
                R.drawable.ic_fb_love,
                R.drawable.ic_fb_laugh,
                R.drawable.ic_fb_wow,
                R.drawable.ic_fb_sad,
                R.drawable.ic_fb_angry,
                R.drawable.ic_baseline_remove_24
        };

        ReactionsConfig config = new ReactionsConfigBuilder(context)
                .withReactions(reactions)
                .build();

        ReactionPopup popup = new ReactionPopup(context, config, (pos) -> {

            if(pos == -1) {
                return true;
            }

            if(holder.getClass() == SentViewHolder.class) {
                SentViewHolder viewHolder = (SentViewHolder) holder;
                viewHolder.binding.sendFeelingItem.setImageResource(reactions[pos]);
                viewHolder.binding.sendFeelingItem.setVisibility(View.VISIBLE);

                if(pos == 6) {
                    viewHolder.binding.sendFeelingItem.setVisibility(View.INVISIBLE);
                }
            } else {
                ReceiveViewHolder viewHolder = (ReceiveViewHolder) holder;
                viewHolder.binding.receiveFeelingItem.setImageResource(reactions[pos]);


                if(pos == 6) {
                    viewHolder.binding.receiveFeelingItem.setVisibility(View.INVISIBLE);
                }
            }

            message.setFeeling(pos);

            db.getReference().child("chats").child(senderRoom).child("messages").child(message.getMessageId()).setValue(message);
            db.getReference().child("chats").child(receiverRoom).child("messages").child(message.getMessageId()).setValue(message);

            return true; // true is closing popup, false is requesting a new selection
        });

        if(holder.getClass() == SentViewHolder.class) {
            SentViewHolder viewHolder = (SentViewHolder) holder;

            if(message.getMessage().equals("Photo")) {
                viewHolder.binding.sendMessageItem.setVisibility(View.GONE);
                viewHolder.binding.sendImage.setVisibility(View.VISIBLE);
                Glide.with(context).load(message.getImageUrl()).placeholder(R.drawable.placeholder).into(viewHolder.binding.sendImage);

            }

            viewHolder.binding.sendMessageItem.setText(message.getMessage());

            if(message.getFeeling() >= 0 && message.getFeeling() != 6) {
                viewHolder.binding.sendFeelingItem.setImageResource(reactions[message.getFeeling()]);
                viewHolder.binding.sendFeelingItem.setVisibility(View.VISIBLE);
            }
            else {
                viewHolder.binding.sendFeelingItem.setVisibility(View.GONE);
            }

            viewHolder.binding.sendMessageItem.setOnTouchListener((v, event) -> {
                popup.onTouch(v, event);
                return true;
            });

            viewHolder.binding.sendImage.setOnTouchListener((v, event) -> {
                popup.onTouch(v, event);
                return true;
            });
        } else {
            ReceiveViewHolder viewHolder = (ReceiveViewHolder) holder;

            if(message.getMessage().equals("Photo")) {
                viewHolder.binding.receiveMessageItem.setVisibility(View.GONE);
                viewHolder.binding.receiveImage.setVisibility(View.VISIBLE);
                Glide.with(context).load(message.getImageUrl()).placeholder(R.drawable.placeholder).into(viewHolder.binding.receiveImage);

            }

            viewHolder.binding.receiveMessageItem.setText(message.getMessage());

            if(message.getFeeling() >= 0 && message.getFeeling() != 6) {
                viewHolder.binding.receiveFeelingItem.setImageResource(reactions[message.getFeeling()]);
                viewHolder.binding.receiveFeelingItem.setVisibility(View.VISIBLE);
            }
            else {
                viewHolder.binding.receiveFeelingItem.setVisibility(View.GONE);
            }


            viewHolder.binding.receiveMessageItem.setOnTouchListener((v, event) -> {
                popup.onTouch(v, event);
                return true;
            });

            viewHolder.binding.receiveImage.setOnTouchListener((v, event) -> {
                popup.onTouch(v, event);
                return true;
            });
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);

        if (message.getSenderId().equals(mAuth.getCurrentUser().getUid()))
            return ITEM_SENT;
        else
            return ITEM_RECEIVE;
    }

    public class SentViewHolder extends RecyclerView.ViewHolder {
        ItemSendBinding binding;

        public SentViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            binding = ItemSendBinding.bind(itemView);
        }
    }

    public class ReceiveViewHolder extends RecyclerView.ViewHolder {

        ItemRecieveBinding binding;

        public ReceiveViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            binding = ItemRecieveBinding.bind(itemView);
        }
    }

}
