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
import com.example.reesit.databinding.FragmentLoginBinding;
import com.example.reesit.services.UserService;
import com.example.reesit.utils.Validator;
import com.parse.Parse;
import com.parse.ParseUser;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoginFragment extends Fragment {

    private FragmentLoginBinding fragmentLoginBinding;
    private TextView signUpText;
    private EditText emailField;
    private EditText passwordField;
    private Button logInButton;
    private ProgressBar progressBar;

    public LoginFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment LoginFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentLoginBinding = FragmentLoginBinding.inflate(inflater, container, false);
        return fragmentLoginBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        signUpText = fragmentLoginBinding.signUpText;
        signUpText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().beginTransaction().replace(R.id.flContainer, new RegisterFragment()).commit();
            }
        });

        emailField = fragmentLoginBinding.editTextEmailAddress;
        passwordField = fragmentLoginBinding.editTextPassword;
        logInButton = fragmentLoginBinding.button;
        logInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailInput = emailField.getText().toString();
                String passwordInput = passwordField.getText().toString();
                if (Validator.isValidEmail(emailInput)){
                    setFormStateLoading();
                    UserService.loginUser(emailInput, passwordInput, (parseUser, e) -> {
                        if (e == null){
                            Intent intent = new Intent(getContext(), MainActivity.class);
                            unsetFormStateLoading();
                            startActivity(intent);
                        } else {
                            Toast.makeText(getContext(), getString(R.string.login_fragment_error_occurred_during_log_in, e.getMessage()), Toast.LENGTH_SHORT).show();
                            unsetFormStateLoading();
                        }
                    });
                } else {
                    Toast.makeText(getContext(), getString(R.string.login_fragment_invalid_email), Toast.LENGTH_SHORT).show();
                    unsetFormStateLoading();
                }

            }
        });
        progressBar = fragmentLoginBinding.progressBar;



    }

    private void setFormStateLoading(){
        logInButton.setEnabled(false);
        emailField.setEnabled(false);
        passwordField.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void unsetFormStateLoading(){
        logInButton.setEnabled(true);
        emailField.setEnabled(true);
        passwordField.setEnabled(true);
        progressBar.setVisibility(View.INVISIBLE);
    }
}