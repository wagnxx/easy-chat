package mob.godutch.easychat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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

import mob.godutch.easychat.utils.AndroidUtil;

public class LoginOPTActivity extends AppCompatActivity {

    String phoneNumber;
    Long timeoutSeconds = 60L;
    String verificationCode;
    PhoneAuthProvider.ForceResendingToken resendingToken;
    Button nextBtn;
    EditText optInput;
    ProgressBar progressBar;
    TextView resendOptTextView;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_optactivity);

        phoneNumber = getIntent().getStringExtra("phone");

        nextBtn = findViewById(R.id.login_next_btn);
        optInput = findViewById(R.id.login_opt_input);
        progressBar = findViewById(R.id.login_progress_bar);
        resendOptTextView = findViewById(R.id.resend_opt_textview);

        sendOpt(phoneNumber, false);

        nextBtn.setOnClickListener(v -> {
            String enteredOpt = optInput.getText().toString();
            PhoneAuthCredential phoneAuthCredential = PhoneAuthProvider.getCredential(verificationCode, enteredOpt);
            signIn(phoneAuthCredential);
            setInProgress(true);
        });

        resendOptTextView.setOnClickListener(v -> {
            sendOpt(phoneNumber, true);
        });
    }

    void sendOpt(String phoneNumber, boolean isResend) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            AndroidUtil.showToast(getApplicationContext(), "Phone number is invalid");
            return;
        }

        startResendTimer();
        setInProgress(true);

        PhoneAuthOptions.Builder builder = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(timeoutSeconds, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new VerificationCallbacks());

        if (isResend && resendingToken != null) {
            PhoneAuthProvider.verifyPhoneNumber(builder.setForceResendingToken(resendingToken).build());
        } else {
            PhoneAuthProvider.verifyPhoneNumber(builder.build());
        }
    }

    private class VerificationCallbacks extends PhoneAuthProvider.OnVerificationStateChangedCallbacks {
        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            signIn(phoneAuthCredential);
            setInProgress(false);
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            runOnUiThread(() -> {
                AndroidUtil.showToast(getApplicationContext(), "OPT verification failed: " + e.getMessage());
                setInProgress(false);
            });
        }

        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            verificationCode = s;
            resendingToken = forceResendingToken;
            runOnUiThread(() -> {
                AndroidUtil.showToast(getApplicationContext(), "OPT sent successfully");
                setInProgress(false);
            });
        }
    }

    void setInProgress( boolean inProgress) {
        if (inProgress) {
            progressBar.setVisibility(View.VISIBLE);
            nextBtn.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            nextBtn.setVisibility(View.VISIBLE);
        }
    }
    void  signIn(PhoneAuthCredential phoneAuthCredential){
        setInProgress(true);
        mAuth.signInWithCredential(phoneAuthCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                setInProgress(false);
                if (task.isSuccessful()) {
                    Intent intent = new Intent(getApplicationContext(), LoginUsernameActivity.class);
                    intent.putExtra("phone", phoneNumber);
                    startActivity(intent);
                } else  {
                    AndroidUtil.showToast(getApplicationContext(), "OTP verification failed");
                }
            }
        });
    }

    void startResendTimer() {
        resendOptTextView.setEnabled(false);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                timeoutSeconds--;
                resendOptTextView.setText("Resend OTP in" + timeoutSeconds + " seconds");
                if (timeoutSeconds <= 0) {
                    timer.cancel();
                    timeoutSeconds = 60L;
                    runOnUiThread(() -> {
                        resendOptTextView.setEnabled(true);
                    });
                }
            }
        },0,1000);
    }
}