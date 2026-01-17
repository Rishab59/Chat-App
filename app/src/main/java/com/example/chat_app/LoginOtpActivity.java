package com.example.chat_app;

import android.content.Intent;
import android.os.Bundle;
//import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chat_app.utils.AndroidUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class LoginOtpActivity extends AppCompatActivity {
    String phoneNumber;

    EditText otpInput;
    Button nextBtn;
    ProgressBar progressBar;
    TextView resendOtpTextView;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    Long timeOutSeconds = 60L;
    String verificationCode;
    PhoneAuthProvider.ForceResendingToken resendingToken; // need this token to resend otp

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_otp);

        otpInput = findViewById(R.id.login_otp);
        nextBtn = findViewById(R.id.login_otp_next_btn);
        progressBar = findViewById(R.id.login_progress_bar);
        resendOtpTextView = findViewById(R.id.resend_otp_textView);

        phoneNumber = getIntent().getExtras().getString("phone");

        sendOtp(phoneNumber, false);

        nextBtn.setOnClickListener(v -> {
            String enteredOtp = otpInput.getText().toString();

            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationCode, enteredOtp);
            signIn(credential);

            setInProgress(true);
        });

        resendOtpTextView.setOnClickListener(v -> {
            sendOtp(phoneNumber, true);
        });
    }

    private void sendOtp(String phoneNumber, boolean isResend){
        startResendTimer();
        setInProgress(true);

        PhoneAuthOptions.Builder builder =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(timeOutSeconds, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                                signIn(phoneAuthCredential); // Automatic OTP checking if received on same device, then signIn without clicking nextBtn

                                setInProgress(false);
                            }

                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {
//                                Log.d("Phone", phoneNumber); // Checking correctness of the phone number fomat
                                AndroidUtil.showToast(getApplicationContext(), "OTP Verification Failed !");

                                setInProgress(false);
                            }

                            @Override
                            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                super.onCodeSent(s, forceResendingToken);

                                verificationCode = s;
                                resendingToken = forceResendingToken;

                                AndroidUtil.showToast(getApplicationContext(), "OTP Sent Successfully !");

                                setInProgress(false);
                            }
                        });

        if(isResend) {
            PhoneAuthProvider.verifyPhoneNumber(builder.setForceResendingToken(resendingToken).build());
        } else { // First time sending OTP
            PhoneAuthProvider.verifyPhoneNumber(builder.build());
        }
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

    private void signIn(PhoneAuthCredential phoneAuthCredential){
        // Login and go to next Activity (Login Username)
        setInProgress(true);

        mAuth.signInWithCredential(phoneAuthCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                setInProgress(false);
                if(task.isSuccessful()){
                    Intent intent = new Intent(LoginOtpActivity.this, LoginUsernameActivity.class);
                    intent.putExtra("phone", phoneNumber);

                    startActivity(intent);
                } else { // Entered Incorrect OTP
                    AndroidUtil.showToast(getApplicationContext(), "OTP Verification Failed !");
                }
            }
        });
    }

    private void startResendTimer(){
        resendOtpTextView.setEnabled(false);

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                timeOutSeconds--;
                resendOtpTextView.setText(String.format("Resend OTP in %d seconds", timeOutSeconds));

                if(timeOutSeconds <= 0) {
                    timeOutSeconds = 60L;
                    timer.cancel();

                    runOnUiThread(() -> {
                        resendOtpTextView.setEnabled(true);
                    });
                }
            }
        },0,1000);
    }
}