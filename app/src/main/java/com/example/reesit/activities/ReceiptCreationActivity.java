package com.example.reesit.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;

import com.example.reesit.R;
import com.example.reesit.databinding.ActivityReceiptCreationBinding;
import com.example.reesit.fragments.ReceiptsCreationNoPicture;

import java.util.Objects;

public class ReceiptCreationActivity extends AppCompatActivity {

    private ActivityReceiptCreationBinding activityReceiptCreationBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityReceiptCreationBinding = ActivityReceiptCreationBinding.inflate(LayoutInflater.from(this));

        Toolbar toolbar = activityReceiptCreationBinding.toolbar.getRoot();
        toolbar.setTitle(com.example.reesit.R.string.receipts_creation_no_picture_title);
        setSupportActionBar(toolbar);
        setContentView(activityReceiptCreationBinding.getRoot());


        final Fragment noPictureTakenFragment = new ReceiptsCreationNoPicture();
        getSupportFragmentManager().beginTransaction().replace(activityReceiptCreationBinding.receiptsCreationFragmentContainer.getId(), noPictureTakenFragment).commit();


    }

    public void changeToolbarTitle(String newTitle){
        if (getSupportActionBar() != null){
            getSupportActionBar().setTitle(newTitle);
        }
    }
}