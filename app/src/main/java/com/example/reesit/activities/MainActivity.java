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
import android.view.MenuItem;

import com.example.reesit.R;
import com.example.reesit.databinding.ActivityMainBinding;
import com.example.reesit.fragments.ReceiptsFragment;
import com.example.reesit.services.UserService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

//    private BottomNavigationView bottomNavigationView;
    private ActivityMainBinding activityMainBinding;

    private DrawerLayout drawer;
    private Toolbar toolbar;
    private NavigationView nvDrawer;

    private ActionBarDrawerToggle drawerToggle;


    private final static String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = ActivityMainBinding.inflate(LayoutInflater.from(this));
        setContentView(activityMainBinding.getRoot());



        final FragmentManager fragmentManager = getSupportFragmentManager();
        final Fragment receiptsFragment = new ReceiptsFragment();
        fragmentManager.beginTransaction().replace(R.id.content, receiptsFragment).commit();

        toolbar = activityMainBinding.toolbar.getRoot();
        setSupportActionBar(toolbar);


        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24);
        }

        drawer = activityMainBinding.drawerLayout;

        nvDrawer = activityMainBinding.navigationView;

        setupDrawerContent(nvDrawer);

//        final Fragment settingsFragment = new UserSettings();
//        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
//            @Override
//            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//                Fragment fragment;
//                switch(item.getItemId()){
//                    case R.id.list_menu_item:
//                        fragment = receiptsFragment;
//                        break;
//                    case R.id.settings_menu_item:
//                        fragment = settingsFragment;
//                        break;
//                    default:
//                        fragment = receiptsFragment;
//                        break;
//                }
//
//                fragmentManager.beginTransaction().replace(R.id.content, fragment).commit();
//                return true;
//            }
//        });
//
//        bottomNavigationView.setSelectedItemId(R.id.list_menu_item);

    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Log.i(TAG, String.valueOf(item.getItemId()));
        switch(item.getItemId()){
            case android.R.id.home:
                drawer.openDrawer(GravityCompat.START);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void goSettingsActivity(){
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
            new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(MenuItem menuItem) {
                    switch(menuItem.getItemId()){
                        case R.id.settings_menu_item:
                            goSettingsActivity();
                            break;
                        case R.id.logout_menu_item:
                            UserService.logOutUser(new LogOutCallback() {
                                @Override
                                public void done(ParseException e) {
                                    Intent intent = new Intent(MainActivity.this, AuthenticationActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                    }
                    return true;
                }
            }
        );
    }
}