package com.hcmute.endsemesterproject.Controllers;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.hcmute.endsemesterproject.R;
import com.hcmute.endsemesterproject.Utils.CommonConst;

import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {
    private Button CreateAccountButton;
    private EditText UserEmail, UserPassword;
    private TextView AlreadyHaveAccountLink, txt_ErrorValidEmail;
    private ProgressDialog loadingBar;
    private FirebaseAuth mAuth;
    FirebaseDatabase database;
    private DatabaseReference RootRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        RootRef = database.getReference();


        InitializeFields();

        AlreadyHaveAccountLink.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                SendUserToLoginActivity();
            }
        });

        CreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                CreateNewAccount();
            }
        });
    }

    private void InitializeFields()
    {
        CreateAccountButton = (Button) findViewById(R.id.register_button);
        UserEmail = (EditText) findViewById(R.id.register_email);
        UserPassword = (EditText) findViewById(R.id.register_password);
        AlreadyHaveAccountLink = (TextView) findViewById(R.id.already_have_account_link);
        txt_ErrorValidEmail = (TextView) findViewById(R.id.txt_errorValidMail);
        txt_ErrorValidEmail.setVisibility(View.INVISIBLE);
        loadingBar = new ProgressDialog(this);
    }

    private void CreateNewAccount()
    {
        String email = UserEmail.getText().toString().trim() ;
        String password = UserPassword.getText().toString().trim();
        txt_ErrorValidEmail.setVisibility(View.INVISIBLE);

        if (isValidEmail(email) && CommonConst.isValidPassword(password, RegisterActivity.this)) {
//            Toast.makeText(this, "Acc OKE", Toast.LENGTH_SHORT).show();
            loadingBar.setTitle("Creating New Account");
            loadingBar.setMessage("Please wait, while we wre creating new account for you...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task)
                        {
                            if (task.isSuccessful())
                            {

                                String deviceToken = FirebaseMessaging.getInstance().getToken().toString();

                                String currentUserID = mAuth.getCurrentUser().getUid();
                                RootRef.child("Users").child(currentUserID).setValue("");

                                RootRef.child("Users").child(currentUserID).child("device_token")
                                        .setValue(deviceToken);

                                SendUserToMainActivity();
                                Toast.makeText(RegisterActivity.this, currentUserID, Toast.LENGTH_SHORT).show();
                                //Toast.makeText(RegisterActivity.this, "Account Created Successfully...", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                            else
                            {
                                String message = task.getException().toString();
                                Toast.makeText(RegisterActivity.this, "Error : " + message, Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
        }
    }

    private boolean isValidEmail(String target) {
        if (TextUtils.isEmpty(target)) {
            Toast.makeText(this, "Email is incorrect !", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!target.endsWith(CommonConst.DOMAIN_PART_GMAIL)) {
            Toast.makeText(this, "Domain must: @gmail.com", Toast.LENGTH_LONG).show();
            return false;
        }

        String localPart = target.substring(0, target.length() - CommonConst.DOMAIN_PART_GMAIL.length());
        String EMAIL_REGEX = "^[a-zA-Z0-9][a-zA-Z0-9.]*(\\.[a-zA-Z0-9]+)*[a-zA-Z0-9]$";
        if(!Pattern.matches(EMAIL_REGEX, localPart)){
            txt_ErrorValidEmail.setVisibility(View.VISIBLE);
            return false;
        }

        return true;
    }

    private void SendUserToLoginActivity()
    {
        Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(loginIntent);
    }

    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}