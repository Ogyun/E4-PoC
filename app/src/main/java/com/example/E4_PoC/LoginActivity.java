package com.example.E4_PoC;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.E4_PoC.R;

public class LoginActivity extends AppCompatActivity {

    private EditText emailField, passwordField;
    private Button login;
    private EmailUtilities emailUtilities;
    private String email;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailField = findViewById(R.id.email);
        passwordField = findViewById(R.id.password);
        login = findViewById(R.id.login);

        /* Logic behind login button */
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /* Obtain user inputs */
                email = emailField.getText().toString();
                password = passwordField.getText().toString();

                //Create a new instance of EmailUtilities and pass the required parameters
                emailUtilities = new EmailUtilities(LoginActivity.this,email,password);

                /* Validate input fields */
                if (email.isEmpty() || password.isEmpty()) {
                    /* Display a message toast to user to enter credentials */
                    Toast.makeText(LoginActivity.this, "Please enter email and password!", Toast.LENGTH_LONG).show();

                } else {
                    new Thread(new Runnable() {

                        @Override
                        public void run() {
                              if (emailUtilities.Authenticate()) {

                                  Intent i = new Intent(LoginActivity.this, MainActivity.class);
                                  i.putExtra("emailClass", (Parcelable) emailUtilities);
                                  startActivity(i);
                                  finish();
                                  return;
                              } else{
                                  runOnUiThread(new Runnable() {
                                   @Override
                                   public void run() {
                                       Toast.makeText(LoginActivity.this, "Authentication failed!", Toast.LENGTH_LONG).show();
                                       return;
                                   }
                               });
                              }


                        }

                    }).start();

                }

            }
        });
    }

}