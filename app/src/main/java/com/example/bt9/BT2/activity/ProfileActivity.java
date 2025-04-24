package com.example.bt9.BT2.activity;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.bt9.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    private static final int STORAGE_PERMISSION_CODE = 100;
    private CircleImageView imgAvatar;
    Button btnNextLightControl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        btnNextLightControl = findViewById(R.id.btnNextLightControl);
        btnNextLightControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this, LightControlClientActivity.class);
                startActivity(intent);
            }
        });

        imgAvatar = findViewById(R.id.imgAvatar);
        imgAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userId = findViewById(R.id.tvUserId).toString();
                openImageUploadActivity(userId);
            }
        });
    }

    private void openImageUploadActivity(String userId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // Android 14+
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED},
                        STORAGE_PERMISSION_CODE);
            } else {
                launchUploadActivity(userId);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_MEDIA_IMAGES},
                        STORAGE_PERMISSION_CODE);
            } else {
                launchUploadActivity(userId);
            }
        } else { // Android 12 and below
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        STORAGE_PERMISSION_CODE);
            } else {
                launchUploadActivity(userId);
            }
        }
    }
    private void launchUploadActivity(String userId) {
        Intent intent = new Intent(this, UploadImagesActivity.class);
        intent.putExtra("USER_ID", userId);
        startActivity(intent);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                String userId = "3"; // Get dynamically as needed
                launchUploadActivity(userId);
            } else {
                Toast.makeText(this, "Permission denied. Cannot access images.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
