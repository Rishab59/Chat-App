package com.example.chat_app;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

//import com.google.firebase.firestore.FirebaseFirestore;

//import java.util.HashMap;
//import java.util.Map;

public class LoginOtpActivity extends AppCompatActivity {
    String phoneNumber;

    EditText otpInput;
    Button nextBtn;
    ProgressBar progressBar;
    TextView resendOtpTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_otp);

        otpInput = findViewById(R.id.login_otp);
        nextBtn = findViewById(R.id.login_otp_next_btn);
        progressBar = findViewById(R.id.login_progress_bar);
        resendOtpTextView = findViewById(R.id.resend_otp_textView);

        phoneNumber = getIntent().getExtras().getString("phone");

        Toast.makeText(getApplicationContext(), phoneNumber, Toast.LENGTH_LONG).show();

//      Testing Firebase database connection
//        Map<String, String> data = new HashMap<>();
//        FirebaseFirestore.getInstance().collection("test").add(data);
    }

    private void sendOtp(String phoneNumber, boolean isResend){

    }

    private void setInProgress(boolean isProgress) {
        if(isProgress) {
            progressBar.setVisibility(View.VISIBLE);
            nextBtn.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            nextBtn.setVisibility(View.VISIBLE);
        }
    }
}