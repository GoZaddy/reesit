package com.faruq.reesit.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.faruq.reesit.R;
import com.faruq.reesit.databinding.ActivityOnboardingBinding;
import com.faruq.reesit.fragments.OnboardingPageFragment;
import com.faruq.reesit.models.User;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class OnboardingActivity extends AppCompatActivity {
    ActivityOnboardingBinding binding;
    private ViewPager2 pager;
    private Button getStarted;

    private FragmentStateAdapter pagerAdapter;

    private ScheduledExecutorService sched = Executors.newSingleThreadScheduledExecutor();

    private boolean enableAutoAdvance = true;

    private static class OnboardingPageInfo{
        private String text;
        private int heroResourceID;

        public OnboardingPageInfo(String text, int heroResourceID) {
            this.text = text;
            this.heroResourceID = heroResourceID;
        }

        public String getText() {
            return text;
        }

        public int getHeroResourceID() {
            return heroResourceID;
        }
    }

    private List<OnboardingPageInfo> onboardingInfo = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOnboardingBinding.inflate(LayoutInflater.from(this));

        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        Boolean isFirstTimeOpened = sharedPreferences.getBoolean(getString(R.string.shared_pref_is_first_time_opened), true);


        if (isFirstTimeOpened){
            // show onboarding activity
            setContentView(binding.getRoot());
            sharedPreferences.edit().putBoolean(getString(R.string.shared_pref_is_first_time_opened), false).apply();

            onboardingInfo.add(new OnboardingPageInfo(getString(R.string.onboarding_1_text), R.raw.onboarding));
            onboardingInfo.add(new OnboardingPageInfo(getString(R.string.onboarding_2_text), R.raw.onboarding2));
            onboardingInfo.add(new OnboardingPageInfo(getString(R.string.onboarding_3_text), R.raw.onboarding3));

            pager = binding.viewPager;
            getStarted = binding.getStartedButton;
            getStarted.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goAuthenticationActivity();
                }
            });
            pagerAdapter = new OnboardingPagerAdapter(this);
            pager.setAdapter(pagerAdapter);


            sched.schedule(new Runnable() {
                @Override
                public void run() {
                    pager.setCurrentItem(1);
                    runAutoAdvanceCarousel();
                }
            }, 1000, TimeUnit.MILLISECONDS);
        } else{
            User user = User.fromParseUser(ParseUser.getCurrentUser());
            if (user.isLoggedIn()){
                goMainActivity();
            } else {
                goAuthenticationActivity();
            }
        }

    }

    private void goAuthenticationActivity(){
        Intent intent = new Intent(this, AuthenticationActivity.class);
        startActivity(intent);
        finish();
    }

    private void goMainActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (pager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            pager.setCurrentItem(pager.getCurrentItem() - 1);
        }
    }


    private class OnboardingPagerAdapter extends FragmentStateAdapter {
        public OnboardingPagerAdapter(FragmentActivity fa) {
            super(fa);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return OnboardingPageFragment.newInstance(onboardingInfo.get(position).getText(), onboardingInfo.get(position).getHeroResourceID());
        }

        @Override
        public int getItemCount() {
            return onboardingInfo.size();
        }
    }



    private void runAutoAdvanceCarousel(){
        if (enableAutoAdvance ){
            sched.schedule(new Runnable() {
                @Override
                public void run() {

                    if (pager.getCurrentItem() == onboardingInfo.size()-1){
                        pager.setCurrentItem(pager.getCurrentItem()-2);
                    } else {
                        pager.setCurrentItem(pager.getCurrentItem()+1);
                    }
                    runAutoAdvanceCarousel();
                }
            }, 2500, TimeUnit.MILLISECONDS);
        }
    }

    public void stopAutoAdvance(){
        sched.shutdownNow();
        enableAutoAdvance = false;
    }


}