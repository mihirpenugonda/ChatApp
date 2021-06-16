package com.example.chatapp.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapp.R;
import com.example.chatapp.Models.User;
import com.example.chatapp.Adapters.UsersAdapter;
import com.example.chatapp.databinding.ActivityMainPageBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class MainPage extends AppCompatActivity {

    ActivityMainPageBinding binding;
    ProgressDialog dialog;

    FirebaseAuth mAuth;
    FirebaseDatabase db;
    FirebaseStorage storage;

    ArrayList<User> users;
    UsersAdapter usersAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainPageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String profileUserName = getIntent().getStringExtra("USERNAME");

        BottomNavigationView bottomNavigationBar = binding.bottomNavigationView;

        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading...");
        dialog.setCancelable(false);
        dialog.show();

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        users = new ArrayList<>();
        usersAdapter = new UsersAdapter(this, users);
        binding.chatRecyclerView.setAdapter(usersAdapter);

        db.getReference().child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                users.clear();

                for (DataSnapshot name : snapshot.getChildren()) {
                    User user = name.getValue(User.class);

                    if (!user.getUid().equals(mAuth.getCurrentUser().getUid()))
                        users.add(user);

                    dialog.dismiss();
                }
                usersAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        bottomNavigationBar.setOnNavigationItemSelectedListener(listener -> {
            if(listener.getItemId() == R.id.status) {

            }
            return true;
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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out:
                mAuth.signOut();
                Intent i = new Intent(MainPage.this, MainActivity.class);
                startActivity(i);
                finish();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_nav_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


}
