package com.example.chatapp.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.chatapp.Adapters.MessagesAdapter;
import com.example.chatapp.Models.Message;
import com.example.chatapp.R;
import com.example.chatapp.databinding.ActivityChatBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class ChatActivity extends AppCompatActivity {

    ActivityChatBinding binding;

    MessagesAdapter adapter;
    ArrayList<Message> messages;

    FirebaseDatabase db;
    FirebaseStorage storage;

    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        db = FirebaseDatabase.getInstance();

        dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading Image");
        dialog.setCancelable(false);

        String receiverName = getIntent().getStringExtra("RECEIVERNAME");
        String receiverUid = getIntent().getStringExtra("RECEIVERUID");
        String receiverImage = getIntent().getStringExtra("RECEIVERIMAGE");
        String senderUid = FirebaseAuth.getInstance().getUid();

        binding.chatReceiverName.setText(receiverName);
        Glide.with(this).load(receiverImage).placeholder(R.drawable.avatar).into(binding.chatProfileImage);

        String senderRoom = senderUid + receiverUid;
        String receiverRoom = receiverUid + senderUid;

        db.getReference().child("presence").child(receiverUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                String receiverStatus = snapshot.getValue(String.class);

                if(receiverStatus.equals("Online") || receiverStatus.equals("Typing...")) {
                    binding.chatReceiverStatus.setText(receiverStatus);
                    binding.chatReceiverStatus.setVisibility(View.VISIBLE);
                }
                else {
                    binding.chatReceiverStatus.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        messages = new ArrayList<>();
        adapter = new MessagesAdapter(this, messages, senderRoom, receiverRoom);
        binding.chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.chatRecyclerView.setAdapter(adapter);

        db.getReference().child("chats").child(senderRoom).child("messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                messages.clear();

                for (DataSnapshot snap : snapshot.getChildren()) {
                    Message message = snap.getValue(Message.class);
                    message.setMessageId(snap.getKey());
                    messages.add(message);
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        binding.chatSend.setOnClickListener(v -> {
            String messageTxt = binding.chatMessageBox.getText().toString();
            Date date = new Date();

            long currentTime = date.getTime();

            Message message = new Message(messageTxt, senderUid, currentTime);
            message.setImageUrl("");

            binding.chatMessageBox.setText("");

            String randomKey = db.getReference().push().getKey();
            message.setMessageId(randomKey);

            HashMap<String, Object> lastMsgObj = new HashMap<>();
            lastMsgObj.put("lastMsg", message.getMessage());
            lastMsgObj.put("lastMsgTime", currentTime);

            db.getReference().child("chats").child(senderRoom).updateChildren(lastMsgObj);
            db.getReference().child("chats").child(receiverRoom).updateChildren(lastMsgObj);

            db.getReference().child("chats").child(senderRoom).child("messages").child(randomKey).setValue(message).addOnSuccessListener(sendSuccess -> {
                db.getReference().child("chats").child(receiverRoom).child("messages").child(randomKey).setValue(message).addOnSuccessListener(receiveSuccess -> {

                });
            });
        });

        binding.chatAttachment.setOnClickListener(v -> {
            Intent i = new Intent();
            i.setAction(Intent.ACTION_GET_CONTENT);
            i.setType("image/*");

            startActivityForResult(i, 45);
        });

        binding.chatBackButton.setOnClickListener(v -> {
            finish();
        });

        Handler handler = new Handler();

        binding.chatMessageBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
                db.getReference().child("presence").child(currentUser).setValue("Typing...");

                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(userStoppedTyping, 1000);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }

            Runnable userStoppedTyping = new Runnable() {
                @Override
                public void run() {
                    String currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    db.getReference().child("presence").child(currentUser).setValue("Online");
                }
            };
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        String currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.getReference().child("presence").child(currentUser).setValue("Online");
    }

    @Override
    protected void onPause() {
        String currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.getReference().child("presence").child(currentUser).setValue("Offline");
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            if (data.getData() != null) {
                storage = FirebaseStorage.getInstance();
                Uri selectedImage = data.getData();
                Calendar calender = Calendar.getInstance();
                StorageReference reference = storage.getReference().child("chats").child(calender.getTimeInMillis() + "");
                dialog.show();
                reference.putFile(selectedImage).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        reference.getDownloadUrl().addOnSuccessListener(uploadUri -> {
                            String filePath = uploadUri.toString();

                            String receiverUid = getIntent().getStringExtra("RECEIVERUID");
                            String senderUid = FirebaseAuth.getInstance().getUid();

                            String senderRoom = senderUid + receiverUid;
                            String receiverRoom = receiverUid + senderUid;

                            String messageTxt = "Photo";
                            Date date = new Date();

                            long currentTime = date.getTime();

                            Message message = new Message(messageTxt, senderUid, currentTime);
                            message.setImageUrl(filePath);

                            binding.chatMessageBox.setText("");

                            String randomKey = db.getReference().push().getKey();

                            HashMap<String, Object> lastMsgObj = new HashMap<>();
                            lastMsgObj.put("lastMsg", message.getMessage());
                            lastMsgObj.put("lastMsgTime", currentTime);

                            db.getReference().child("chats").child(senderRoom).updateChildren(lastMsgObj);
                            db.getReference().child("chats").child(receiverRoom).updateChildren(lastMsgObj);

                            db.getReference().child("chats").child(senderRoom).child("messages").child(randomKey).setValue(message).addOnSuccessListener(sendSuccess -> {
                                db.getReference().child("chats").child(receiverRoom).child("messages").child(randomKey).setValue(message).addOnSuccessListener(receiveSuccess -> {

                                });
                            });
                            dialog.dismiss();
                        });
                    }
                });
            }
        }
    }

}