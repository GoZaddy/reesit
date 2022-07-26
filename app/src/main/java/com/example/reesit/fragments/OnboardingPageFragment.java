package com.example.reesit.fragments;

import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.reesit.R;
import com.example.reesit.activities.OnboardingActivity;
import com.example.reesit.databinding.FragmentOnboardingPageBinding;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link OnboardingPageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OnboardingPageFragment extends Fragment {

    FragmentOnboardingPageBinding binding;
    private TextView textView;
    private LottieAnimationView lottieAnimationView;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private int heroResourceId;
    private String text;

    public OnboardingPageFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param text Message to show on onboarding page.
     * @param heroResourceId image/animation to show on onboarding page.
     * @return A new instance of fragment OnboardingPageFragment.
     */
    public static OnboardingPageFragment newInstance(String text, int heroResourceId) {
        OnboardingPageFragment fragment = new OnboardingPageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, text);
        args.putInt(ARG_PARAM2, heroResourceId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            text = getArguments().getString(ARG_PARAM1);
            heroResourceId = getArguments().getInt(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentOnboardingPageBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        lottieAnimationView = binding.animationView;
        textView = binding.onboardingPageText;

        lottieAnimationView.setAnimation(heroResourceId);
        SpannableString string = new SpannableString(text);
        int endOfFirstWord = text.length();
        for(int i = 0; i < text.length(); ++i){
            if (text.charAt(i) == ' '){
                endOfFirstWord = i;
                break;
            }
        }
        string.setSpan(new RelativeSizeSpan(1.5f), 0, endOfFirstWord, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        string.setSpan(new StyleSpan(Typeface.BOLD), 0, endOfFirstWord, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(string);
        binding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (requireActivity() instanceof OnboardingActivity){
                    ((OnboardingActivity) requireActivity()).stopAutoAdvance();
                }
            }
        });
    }
}