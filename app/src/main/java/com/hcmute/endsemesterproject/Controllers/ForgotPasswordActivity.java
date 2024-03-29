package com.hcmute.endsemesterproject.Controllers;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.hcmute.endsemesterproject.R;

public class ForgotPasswordActivity extends AppCompatActivity {
    //declare variables
    Button btnBack, btnReset;
    EditText edtEmail;
    FirebaseAuth mAuth;
    String strEmail;
    private ProgressDialog loadingBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        //Initialization
        btnBack = (Button) findViewById(R.id.back_button);
        btnReset = (Button) findViewById(R.id.reset_button);
        edtEmail = (EditText) findViewById(R.id.registered_email);

        mAuth = FirebaseAuth.getInstance();
        loadingBar = new ProgressDialog(this);

        handleResetPassword();
        returnLogin();
    }

    private void returnLogin() {
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void handleResetPassword() {
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strEmail = edtEmail.getText().toString().trim();
                if (!TextUtils.isEmpty(strEmail)){
                    resetPassword();
                } else{
                    Toast.makeText(ForgotPasswordActivity.this,  "Email field can't be empty.", Toast.LENGTH_SHORT).show();
                }
            }
        });    }

    private void resetPassword() {
        loadingBar.setTitle("Sending Email");
        loadingBar.setMessage("Please wait while we are sending you password reset email...");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();

        mAuth.sendPasswordResetEmail(strEmail).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(ForgotPasswordActivity.this,
                        "Reset Password link has been sent to your Email.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ForgotPasswordActivity.this,
                        "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}