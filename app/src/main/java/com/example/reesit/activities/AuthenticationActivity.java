package com.example.reesit.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;

import com.example.reesit.R;
import com.example.reesit.databinding.ActivityAuthenticationBinding;
import com.example.reesit.fragments.LoginFragment;
import com.example.reesit.models.User;
import com.parse.ParseUser;

public class AuthenticationActivity extends AppCompatActivity {
    private ActivityAuthenticationBinding activityAuthenticationBinding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityAuthenticationBinding = ActivityAuthenticationBinding.inflate(LayoutInflater.from(this));
        User user = User.fromParseUser(ParseUser.getCurrentUser());
        if (user.isLoggedIn()){
            goMainActivity();
        }
        setContentView(activityAuthenticationBinding.getRoot());

        Fragment fragment = new LoginFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, fragment).commit();
    }

    private void goMainActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}