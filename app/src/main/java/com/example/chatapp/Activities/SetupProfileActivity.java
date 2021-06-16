package com.example.chatapp.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapp.Models.User;
import com.example.chatapp.databinding.ActivitySetupProfileBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class SetupProfileActivity extends AppCompatActivity {

    ActivitySetupProfileBinding binding;
    FirebaseAuth mAuth;
    FirebaseDatabase db;
    FirebaseStorage storage;

    ProgressDialog dialog;

    Uri selectedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySetupProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();

        dialog = new ProgressDialog(this);
        dialog.setMessage("Updating Profile");
        dialog.setCancelable(false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        binding.profileImage.setOnClickListener(v -> {
            Intent i = new Intent();
            i.setAction(Intent.ACTION_GET_CONTENT);
            i.setType("image/*");

            startActivityForResult(i, 45);
        });

        binding.profileActivitySubmit.setOnClickListener(v -> {
            String name = binding.profileActivityEditTextName.getText().toString();

            if(name.isEmpty()) {
                binding.profileActivityEditTextName.setError("Please Type a Name");
                return;
            }

            dialog.show();

            if(selectedImage != null) {
                StorageReference reference = storage.getReference().child("Profiles").child(mAuth.getUid());
                reference.putFile(selectedImage).addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        reference.getDownloadUrl().addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();
                            String uid = mAuth.getUid();
                            String phone = mAuth.getCurrentUser().getPhoneNumber();

                            User user = new User(uid, name, phone, imageUrl);

                            db.getReference().child("users").child(uid).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    dialog.dismiss();
                                    Intent i = new Intent(SetupProfileActivity.this, MainPage.class);
                                    i.putExtra("USERNAME", name);
                                    startActivity(i);
                                    finish();
                                }
                            });
                        });
                    }
                });
            } else {
                String uid = mAuth.getUid();
                String phone = mAuth.getCurrentUser().getPhoneNumber();

                User user = new User(uid, name, phone, "No Image");

                db.getReference().child("users").child(uid).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        dialog.dismiss();
                        Intent i = new Intent(SetupProfileActivity.this, MainPage.class);
                        startActivity(i);
                        finish();
                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            if (data.getData() != null) {
                binding.profileImage.setImageURI(data.getData());
                selectedImage = data.getData();
            }
        }
    }
}