package com.example.reesit.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.example.reesit.R;
import com.example.reesit.databinding.ActivityFilterBinding;
import com.example.reesit.databinding.DialogCreateCategoryAddNameBinding;
import com.example.reesit.databinding.DialogCreateCategoryConfirmBinding;
import com.example.reesit.fragments.FilterReceiptsFragment;
import com.example.reesit.misc.Filter;
import com.example.reesit.models.ReceiptCategory;
import com.example.reesit.models.User;
import com.example.reesit.services.CategoryService;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

public class FilterActivity extends AppCompatActivity {

    private ActivityFilterBinding binding;
    private Filter filter;
    private static final String TAG = "FilterActivity";
    public static final String FILTER_OBJECT_INTENT_KEY = "FILTER";
    public static final String FILTER_OBJECT_RESULT_INTENT_KEY = "FILTER_RESULT";
    public static final String CREATED_CATEGORIES_RESULT_INTENT_KEY = "CREATED_CATEGORIES";
    public static final String RESET_FILTER_FRAGMENT_RESULT_KEY = "RESET_FILTER_FRAGMENT_RESULT_KEY";
    public static final String FILTER_CHANGE_FRAGMENT_NEW_FRAGMENT_RESULT_KEY = "FILTER_CHANGE_FRAGMENT_NEW_FRAGMENT_RESULT_KEY";
    private FilterReceiptsFragment filterReceiptsFragment;

    private List<ReceiptCategory> createdCategories = new ArrayList<>();

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

        filterReceiptsFragment = FilterReceiptsFragment.newInstance(filter);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .add(binding.fragmentContainerView.getId(), filterReceiptsFragment, TAG)
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
            getSupportFragmentManager().setFragmentResult(RESET_FILTER_FRAGMENT_RESULT_KEY, new Bundle());
        } else if (item.getItemId() == R.id.filter_activity_save_category){
            // make sure 'Greater than' and 'Less than' values are currently set
            filterReceiptsFragment.setGreaterThanLessThanInFilter();


            // trigger dialogs
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            DialogCreateCategoryAddNameBinding dialogCreateCategoryAddNameBinding =
                    DialogCreateCategoryAddNameBinding.inflate(LayoutInflater.from(this));
            DialogCreateCategoryConfirmBinding dialogCreateCategoryConfirmBinding =
                    DialogCreateCategoryConfirmBinding.inflate(LayoutInflater.from(this));
            builder.setView(dialogCreateCategoryAddNameBinding.getRoot())
                    .setPositiveButton(R.string.add_new_category_dialog_category_name_positive_button_text, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ReceiptCategory category =
                                    new ReceiptCategory(dialogCreateCategoryAddNameBinding.categoryNameEdittext.getText().toString(), filter);
                            dialogCreateCategoryConfirmBinding.categoryNameTextView.
                                    setText(category.getName());
                            dialogCreateCategoryConfirmBinding.filterInfoTextView.setText(category.getFilter().getStringValue(FilterActivity.this));
                            dialog.dismiss();

                            AlertDialog.Builder confirmDialogBuilder = new AlertDialog.Builder(FilterActivity.this);
                            confirmDialogBuilder.setView(dialogCreateCategoryConfirmBinding.getRoot())
                                    .setPositiveButton(R.string.add_new_category_dialog_confirm_positive_button_text, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface innerDialog, int which) {
                                            // set buttons to disabled
                                            // show progress on positive button

                                            innerDialog.dismiss();
                                            Snackbar.make(binding.getRoot(), getString(R.string.add_new_category_loading_text), BaseTransientBottomBar.LENGTH_SHORT).show();
                                            CategoryService.createCategory(category, User.getCurrentUser(), new CategoryService.AddCategoryCallback() {
                                                @Override
                                                public void done(ReceiptCategory newCategory, Exception e) {
                                                    if (e == null){
                                                        createdCategories.add(category);
                                                        Snackbar.make(binding.getRoot(), getString(R.string.add_new_category_success_message, category.getName()), BaseTransientBottomBar.LENGTH_SHORT).show();
                                                    } else {

                                                        if (e instanceof CategoryService.CategoryAlreadyExistsException){
                                                            Toast.makeText(FilterActivity.this, getString(R.string.add_new_category_error_message_format, e.getMessage()), Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            Toast.makeText(FilterActivity.this, getString(R.string.add_new_category_error_message), Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                }
                                            });
                                        }
                                    })
                                    .setNegativeButton(R.string.add_new_category_dialog_confirm_negative_button_text, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });

                            confirmDialogBuilder.show();
                        }
                    })
                    .setNegativeButton(R.string.add_new_category_dialog_category_name_negative_button_text, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

            builder.show();


        }
        return super.onOptionsItemSelected(item);

    }

    public void onApplyFilter(Filter filter){
        Intent intent = new Intent();
        intent.putExtra(FilterActivity.FILTER_OBJECT_RESULT_INTENT_KEY, Parcels.wrap(filter));
        if (createdCategories.size() > 0){
            ArrayList<Parcelable> parcelableList = new ArrayList<>();
            for(ReceiptCategory addedCategory: createdCategories){
                parcelableList.add(Parcels.wrap(addedCategory));
            }
            intent.putParcelableArrayListExtra(CREATED_CATEGORIES_RESULT_INTENT_KEY, parcelableList);
        }

        setResult(Activity.RESULT_OK, intent);
        finish();
    }



    @Override
    public void onBackPressed() {
        if (createdCategories.size() > 0){
            Intent intent = new Intent();
            ArrayList<Parcelable> parcelableList = new ArrayList<>();
            for(ReceiptCategory addedCategory: createdCategories){
                parcelableList.add(Parcels.wrap(addedCategory));
            }
            intent.putParcelableArrayListExtra(CREATED_CATEGORIES_RESULT_INTENT_KEY, parcelableList);
            setResult(Activity.RESULT_OK, intent);
        }
        super.onBackPressed();
        finish();


    }
}