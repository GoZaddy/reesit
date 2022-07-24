package com.example.reesit.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;

import com.example.reesit.R;
import com.example.reesit.databinding.ActivityReceiptDetailsBinding;
import com.example.reesit.fragments.ReceiptDetailsFragment;
import com.example.reesit.models.Receipt;

import org.parceler.Parcels;

import java.util.Objects;

public class ReceiptDetailsActivity extends AppCompatActivity {
    private ActivityReceiptDetailsBinding binding;
    private Receipt receipt;
    private int receiptRecyclerViewPosition;
    public static final String RECEIPT_INTENT_KEY = "RECEIPT_INTENT_KEY";
    public static final String RECEIPT_POSITION_INTENT_KEY = "RECEIPT_POSITION_INTENT_KEY";
    public static final String RECEIPT_POSITION_FRAGMENT_RESULT_KEY = "RECEIPT_POSITION_FRAGMENT_RESULT_KEY";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReceiptDetailsBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbar2.toolbarElement;

        setSupportActionBar(toolbar);

        receipt = (Receipt) Parcels.unwrap(getIntent().getParcelableExtra(RECEIPT_INTENT_KEY));
        receiptRecyclerViewPosition = getIntent().getIntExtra(RECEIPT_POSITION_INTENT_KEY, -1);

        getSupportFragmentManager().beginTransaction().replace(binding.receiptDetailsFragmentContainer.getId(), ReceiptDetailsFragment.newInstance(receipt))
                .setReorderingAllowed(true)
                .commit();
    }


    /**
     * Sets toolbar title to null and shows a back button on the toolbar. Adds no functionality for the back button
     */
    public void enableBackButton(){
        Objects.requireNonNull(getSupportActionBar()).setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.arrow_left);
    }

    public int getReceiptPosition(){
        return receiptRecyclerViewPosition;
    }

}