package com.example.chatapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapp.Activities.ChatActivity;
import com.example.chatapp.Models.User;
import com.example.chatapp.R;
import com.example.chatapp.databinding.RowConversationBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UsersViewHolder> {

    Context context;
    ArrayList<User> users;

    public UsersAdapter(Context context, ArrayList<User> users) {
        this.context = context;
        this.users = users;
    }

    public class UsersViewHolder extends RecyclerView.ViewHolder {
        RowConversationBinding binding;

        public UsersViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            binding = RowConversationBinding.bind(itemView);
        }
    }

    @NotNull
    @Override
    public UsersViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_conversation, parent, false);
        return new UsersViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull UsersAdapter.UsersViewHolder holder, int position) {
        User user = users.get(position);

        String senderId = FirebaseAuth.getInstance().getUid();

        String senderRoom = senderId + user.getUid();

        FirebaseDatabase.getInstance().getReference().child("chats").child(senderRoom).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    String lastMsg = snapshot.child("lastMsg").getValue(String.class);
                    long time = snapshot.child("lastMsgTime").getValue(Long.class);

                    holder.binding.msgChat.setText(lastMsg);
                } else {
                    holder.binding.msgChat.setText("Tap to Chat");
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        holder.binding.username.setText(user.getName());

        Glide.with(context).load(user.getProfileImage()).placeholder(R.drawable.avatar).into(holder.binding.msgImage);

        holder.itemView.setOnClickListener(v -> {
            Intent i = new Intent(context, ChatActivity.class);
            i.putExtra("RECEIVERNAME", user.getName());
            i.putExtra("RECEIVERIMAGE", user.getProfileImage());
            i.putExtra("RECEIVERUID", user.getUid());
            context.startActivity(i);
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

}
