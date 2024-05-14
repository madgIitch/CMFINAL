package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.model.UserModel;
import com.example.myapplication.utils.AndroidUtil;
import com.example.myapplication.utils.FirebaseUtil;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (FirebaseUtil.isLoggedIn() && getIntent().getExtras() != null) {
            // From notification
            String userId = getIntent().getExtras().getString("userId");

            if (userId != null) {
                FirebaseUtil.allUserCollectionReference().document(userId).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        UserModel userModel = task.getResult().toObject(UserModel.class);

                        Intent mainIntent = new Intent(this, MainActivity.class);
                        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(mainIntent);

                        Intent intent = new Intent(this, ChatActivity.class);
                        AndroidUtil.passUserModelAsIntent(intent, userModel);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Log.e(TAG, "Error getting user document: ", task.getException());
                        // Handle the error, maybe show a message to the user
                        navigateToAppropriateActivity();
                    }
                });
            } else {
                Log.e(TAG, "User ID from intent extras is null");
                navigateToAppropriateActivity();
            }
        } else {
            navigateToAppropriateActivity();
        }
    }

    private void navigateToAppropriateActivity() {
        new Handler().postDelayed(() -> {
            if (FirebaseUtil.isLoggedIn()) {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            } else {
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            }
            // Finish the SplashActivity so the user can't return to it
            finish();
        }, 1000); // delay in milliseconds before the Runnable is executed
    }
}
