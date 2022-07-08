package com.example.reesit.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.reesit.R;
import com.example.reesit.activities.MainActivity;
import com.example.reesit.databinding.FragmentRegisterBinding;
import com.example.reesit.services.UserService;
import com.example.reesit.utils.Validator;
import com.google.android.gms.common.SignInButton;
import com.parse.ParseException;
import com.parse.SignUpCallback;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RegisterFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RegisterFragment extends Fragment {

    private TextView logInText;
    private EditText emailField;
    private EditText passwordField;
    private EditText confirmPasswordField;
    private Button signUpButton;
    private FragmentRegisterBinding fragmentRegisterBinding;
    private ProgressBar progressBar;

    public RegisterFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment RegisterFragment.
     */
    public static RegisterFragment newInstance(String param1, String param2) {
        return new RegisterFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentRegisterBinding = FragmentRegisterBinding.inflate(inflater, container, false);
        return fragmentRegisterBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        logInText = fragmentRegisterBinding.signUpText;
        logInText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().beginTransaction().replace(R.id.flContainer, new LoginFragment()).commit();
            }
        });

        emailField = fragmentRegisterBinding.editTextEmailAddress;
        passwordField = fragmentRegisterBinding.editTextPassword;
        confirmPasswordField = fragmentRegisterBinding.editTextConfirmPassword;
        signUpButton = fragmentRegisterBinding.button;
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!passwordField.getText().toString().equals(confirmPasswordField.getText().toString())){
                    Toast.makeText(getContext(), getString(R.string.register_fragment_passwords_not_match_error_message), Toast.LENGTH_SHORT).show();
                } else {
                    // check if email is valid
                    if (Validator.isValidEmail(emailField.getText().toString())){
                        setFormStateLoading();
                        UserService.signUpNewUser(emailField.getText().toString(), passwordField.getText().toString(), new SignUpCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null){
                                    Intent intent = new Intent(getContext(), MainActivity.class);
                                    unsetFormStateLoading();
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(getContext(), getString(R.string.register_fragment_error_occurred_during_sign_up, e.getMessage()), Toast.LENGTH_SHORT).show();
                                    unsetFormStateLoading();
                                }

                            }
                        });
                    } else {
                        Toast.makeText(getContext(), getString(R.string.register_fragment_invalid_email), Toast.LENGTH_SHORT).show();
                    }


                }
            }
        });

        progressBar = fragmentRegisterBinding.progressBarRegisterFragment;
    }

    private void setFormStateLoading(){
        signUpButton.setEnabled(false);
        emailField.setEnabled(false);
        passwordField.setEnabled(false);
        confirmPasswordField.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void unsetFormStateLoading(){
        signUpButton.setEnabled(true);
        emailField.setEnabled(true);
        passwordField.setEnabled(true);
        confirmPasswordField.setEnabled(true);
        progressBar.setVisibility(View.INVISIBLE);
    }
}