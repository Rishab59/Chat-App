package com.example.chat_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chat_app.model.UserModel;
import com.example.chat_app.utils.FirebaseUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

public class LoginUsernameActivity extends AppCompatActivity {
    EditText userNameInput;
    Button letMeInbtn;
    ProgressBar progressBar;

    String phoneNumber;
    UserModel userModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_username);

        userNameInput = findViewById(R.id.login_username);
        letMeInbtn = findViewById(R.id.login_let_me_in_btn);
        progressBar = findViewById(R.id.login_progress_bar);

        phoneNumber = getIntent().getExtras().getString("phone");

         getUserName();

         letMeInbtn.setOnClickListener(v -> {
             setUserName();
         });
    }

    private void getUserName(){
        setInProgress(true);

        FirebaseUtil.currentUserDetails().get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                setInProgress(false);

                if(task.isSuccessful()){
                    userModel = task.getResult().toObject(UserModel.class); // converting the result to UserModel class

                    if(userModel != null) { // UserName already created in DB (Existing User)
                        userNameInput.setText(userModel.getUserName());
                    }
                }
            }
        });
    }

    private void setUserName(){
        String userName = userNameInput.getText().toString();
        if(userName.isEmpty() || userName.length() < 3){
            userNameInput.setError("Username length should be atleast 3");
            return;
        }

        setInProgress(true);

        if(userModel != null) { // Existing user
            userModel.setUserName(userName);
        } else { // New User (Signup)
            userModel = new UserModel(phoneNumber, userName, Timestamp.now());
        }

        FirebaseUtil.currentUserDetails().set(userModel).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                setInProgress(false);

                if(task.isSuccessful()){
                    Intent intent = new Intent(LoginUsernameActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Create new task and remove all tasks related to login
                    startActivity(intent);
                }
            }
        });
    }

    private void setInProgress(boolean isProgress) {
        if(isProgress) {
            progressBar.setVisibility(View.VISIBLE);
            letMeInbtn.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            letMeInbtn.setVisibility(View.VISIBLE);
        }
    }
}