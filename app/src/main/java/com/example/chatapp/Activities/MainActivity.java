package com.example.chatapp.Activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapp.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();

        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            Intent i = new Intent(MainActivity.this, MainPage.class);
            startActivity(i);
            finish();
        }

        binding.phoneActivitySubmit.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, OTPActivity.class);
            i.putExtra("PHONE_NUMBER", binding.phoneActivityEditText.getText().toString());
            startActivity(i);
        });


    }
}