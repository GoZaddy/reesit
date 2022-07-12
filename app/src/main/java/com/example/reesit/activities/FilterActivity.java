package com.example.reesit.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.widget.Toolbar;

import com.example.reesit.R;
import com.example.reesit.databinding.ActivityFilterBinding;
import com.example.reesit.fragments.FilterReceiptsFragment;
import com.example.reesit.misc.Filter;

import org.parceler.Parcels;

public class FilterActivity extends AppCompatActivity {

    private ActivityFilterBinding binding;
    private Filter filter;
    private static final String TAG = "FilterActivity";
    public static final String FILTER_OBJECT_INTENT_KEY = "FILTER";
    public static final String FILTER_OBJECT_RESULT_INTENT_KEY = "FILTER_RESULT";
    public static final String FILTER_CHANGE_FRAGMENT_RESULT_KEY = "FILTER_CHANGE_FRAGMENT_RESULT_KEY";
    public static final String FILTER_CHANGE_FRAGMENT_NEW_FRAGMENT_RESULT_KEY = "FILTER_CHANGE_FRAGMENT_NEW_FRAGMENT_RESULT_KEY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFilterBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbar.toolbarElement;
        toolbar.setTitle(com.example.reesit.R.string.filter_activity_title);
        setSupportActionBar(toolbar);

        // get filter object from intent
        Intent intent = getIntent();
        filter = (Filter) Parcels.unwrap(intent.getParcelableExtra(FILTER_OBJECT_INTENT_KEY));

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .add(binding.fragmentContainerView.getId(), FilterReceiptsFragment.newInstance(filter), TAG)
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_filter_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.filter_activity_reset_filters){
            filter = new Filter();
            Bundle bundle = new Bundle();
            bundle.putParcelable(FILTER_CHANGE_FRAGMENT_NEW_FRAGMENT_RESULT_KEY, Parcels.wrap(filter));
            getSupportFragmentManager().setFragmentResult(FILTER_CHANGE_FRAGMENT_RESULT_KEY, bundle);
        } else if (item.getItemId() == R.id.filter_activity_save_category){

        }
        return super.onOptionsItemSelected(item);
    }
}