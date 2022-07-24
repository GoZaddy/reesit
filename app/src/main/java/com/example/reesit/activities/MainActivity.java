package com.example.reesit.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

import com.example.reesit.R;
import com.example.reesit.databinding.ActivityMainBinding;
import com.example.reesit.fragments.ReceiptsFragment;
import com.example.reesit.misc.Filter;
import com.example.reesit.models.ReceiptCategory;
import com.example.reesit.models.User;
import com.example.reesit.services.CategoryService;
import com.example.reesit.services.UserService;
import com.google.android.material.navigation.NavigationView;
import com.parse.LogOutCallback;
import com.parse.ParseException;

import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

//    private BottomNavigationView bottomNavigationView;
    private ActivityMainBinding activityMainBinding;

    private DrawerLayout drawer;
    private Toolbar toolbar;
    private NavigationView nvDrawer;

    private ActionBarDrawerToggle drawerToggle;
    private Integer categorySubMenuID;

    private int categoriesCount = 0;

    private final static String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = ActivityMainBinding.inflate(LayoutInflater.from(this));
        setContentView(activityMainBinding.getRoot());



        final FragmentManager fragmentManager = getSupportFragmentManager();
        final Fragment receiptsFragment = ReceiptsFragment.newInstance(new Filter());
        fragmentManager.beginTransaction().replace(R.id.main_activity_fragment_container, receiptsFragment).commit();

        toolbar = activityMainBinding.toolbar.toolbarElement;
        setSupportActionBar(toolbar);


        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24);
        }

        drawer = activityMainBinding.drawerLayout;

        nvDrawer = activityMainBinding.navigationView;


        setupDrawerContent(nvDrawer);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                drawer.openDrawer(GravityCompat.START);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }


    private void setupDrawerContent(NavigationView navigationView) {
        // add 'all receipts' menu item
        MenuItem allReceiptsMenuItem = nvDrawer.getMenu().add(1, Menu.NONE, 1, getString(R.string.drawer_all_receipts_menu_item_title));
        allReceiptsMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                getSupportFragmentManager().beginTransaction().replace(R.id.main_activity_fragment_container, ReceiptsFragment.newInstance(new Filter())).commit();
                drawer.closeDrawer(GravityCompat.START);
                return false;
            }
        });

        // add categories sub menu
        CategoryService.getCategories(Objects.requireNonNull(User.getCurrentUser()), new CategoryService.GetCategoriesCallback() {
            @Override
            public void done(List<ReceiptCategory> categories, Exception e) {
                if (e == null){
                    if (categories.size() > 0){
                        categorySubMenuID = View.generateViewId();
                        SubMenu categoriesSubMenu = nvDrawer.getMenu().addSubMenu(2,categorySubMenuID , 2, R.string.drawer_categories_group_title);
                        for(int index = 0; index < categories.size(); ++index){
                            categoriesCount += 1;
                            ReceiptCategory category = categories.get(index);
                            MenuItem categoryItem = categoriesSubMenu.add(Menu.NONE, categoriesCount, Menu.NONE, category.getName());
                            categoryItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem item) {
                                    getSupportFragmentManager().beginTransaction().replace(R.id.main_activity_fragment_container, ReceiptsFragment.newInstance(category)).commit();
                                    drawer.closeDrawer(GravityCompat.START);
                                    return false;
                                }
                            });
                        }


                    }
                } else {
                    Log.e(TAG, "Could not fetch categories", e);
                }

                // add 'log out' menu item
                MenuItem logOutMenuItem = nvDrawer.getMenu().add(3, Menu.NONE, 3, R.string.drawer_log_out_menu_item);
                logOutMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        UserService.logOutUser(new LogOutCallback() {
                            @Override
                            public void done(ParseException e) {
                                drawer.closeDrawer(GravityCompat.START);
                                Intent intent = new Intent(MainActivity.this, AuthenticationActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        });
                        return false;
                    }
                });
            }
        });

    }

    public void updateCategories(List<ReceiptCategory> addedCategories){
        if (categoriesCount != 0){
            SubMenu categoriesSubMenu =  nvDrawer.getMenu().findItem(categorySubMenuID).getSubMenu();
            for(int index = 0; index < addedCategories.size(); ++index){
                ReceiptCategory category = addedCategories.get(index);
                categoriesCount += 1;
                MenuItem categoryItem = categoriesSubMenu.add(Menu.NONE, categoriesCount, Menu.NONE, category.getName());
                categoryItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        getSupportFragmentManager().beginTransaction().replace(R.id.main_activity_fragment_container, ReceiptsFragment.newInstance(category)).commit();
                        drawer.closeDrawer(GravityCompat.START);
                        return false;
                    }
                });
            }
        } else {
            if (addedCategories.size() > 0){
                categorySubMenuID = View.generateViewId();
                SubMenu categoriesSubMenu = nvDrawer.getMenu().addSubMenu(2, categorySubMenuID, 2, R.string.drawer_categories_group_title);
                for(int index = 0; index < addedCategories.size(); ++index){
                    categoriesCount += 1;
                    ReceiptCategory category = addedCategories.get(index);
                    MenuItem categoryItem = categoriesSubMenu.add(Menu.NONE, categoriesCount, Menu.NONE, category.getName());
                    categoryItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            getSupportFragmentManager().beginTransaction().replace(R.id.main_activity_fragment_container, ReceiptsFragment.newInstance(category)).commit();
                            drawer.closeDrawer(GravityCompat.START);
                            return false;
                        }
                    });
                }


            }
        }

    }


}