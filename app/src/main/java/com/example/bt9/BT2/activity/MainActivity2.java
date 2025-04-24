package com.example.bt9.BT2.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.bt9.BT2.Const;
import com.example.bt9.BT2.RealPathUtil;
import com.example.bt9.BT2.model.ImageUpload;
import com.example.bt9.BT2.retrofit.ServiceAPI;
import com.example.bt9.MainActivity;
import com.example.bt9.R;

import java.io.File;
import java.io.IOException;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity2 extends AppCompatActivity {

    Button btnChoose, btnUpload;
    ImageView imageViewChoose, imageViewUpload;
    EditText editTextUserName;
    TextView textViewUsername;
    Button btnNextUpload;
    private Uri mUri;
    private ProgressDialog mProgressDialog;
    public static final int MY_REQUEST_CODE=100;
    public static final String TAG = MainActivity2.class.getName();

    public static String[] storge_permissions = {
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE};
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public static String[] storge_permissions_33 = {
            android.Manifest.permission.READ_MEDIA_IMAGES,
            android.Manifest.permission.READ_MEDIA_AUDIO,
            android.Manifest.permission.READ_MEDIA_VIDEO
    };

    private ActivityResultLauncher<Intent> mActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Log.e(TAG, "onActivityResult");
                    if (result.getResultCode() == Activity.RESULT_OK) {
//request code
                        Intent data = result.getData();
                        if (data == null) {
                            return;
                        }
                        Uri uri = data.getData();
                        mUri = uri;
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                            imageViewChoose.setImageBitmap(bitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        AnhXa();

        btnNextUpload = findViewById(R.id.btnNextUpload);
        btnNextUpload.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity2.this, ProfileActivity.class);
            startActivity(intent);
        });
        mProgressDialog = new ProgressDialog(MainActivity2.this);
        mProgressDialog.setMessage("Đang tải lên...");
        btnChoose.setOnClickListener(v -> CheckPermission());

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUri != null)
                    UploadImage1();
            }
        });
    }

    public void UploadImage1() {
        mProgressDialog.show();
//khoi bảo biển và setText nếu có
        String username = editTextUserName.getText().toString().trim();
        RequestBody requestUsername = RequestBody.create(MediaType.parse("multipart/form-data"), username);
//create RequestBody instance from file
        String IMAGE_PATH = RealPathUtil.getRealPath(this, mUri);
        Log.e("ffff", IMAGE_PATH);
        File file = new File(IMAGE_PATH);
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
// MultipartBody. Part is used to send also the actual file name
        MultipartBody.Part partbodyavatar =
                MultipartBody.Part.createFormData(Const.MY_IMAGES, file.getName(), requestFile);
//goi Retrofit
        ServiceAPI.serviceAPI.upload(requestUsername, partbodyavatar).enqueue(new Callback<List<ImageUpload>>() {
            @Override
            public void onResponse(Call<List<ImageUpload>> call, Response<List<ImageUpload>> response) {
                mProgressDialog.dismiss();
                List<ImageUpload> imageUpload = response.body();
                if (imageUpload.size() > 0) {
                    for (int i = 0; i < imageUpload.size(); i++) {
                        textViewUsername.setText(imageUpload.get(i).getName());
                        Glide.with(MainActivity2.this)
                                .load(imageUpload.get(i).getAvatar())
                                .into(imageViewUpload);
                        Toast.makeText(MainActivity2.this, "Thành công", Toast.LENGTH_LONG).show();

                    }
                } else {
                    Toast.makeText(MainActivity2.this, "Thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ImageUpload>> call, Throwable t) {
                mProgressDialog.dismiss();
                Log.e("TAG", t.toString());
                Toast.makeText(MainActivity2.this, "Thất bại", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void AnhXa() {
        btnChoose = findViewById(R.id.btnChoose);
        btnUpload = findViewById(R.id.btnUpload);
        imageViewUpload = findViewById(R.id.imgMultipart);
        editTextUserName = findViewById(R.id.editUserName);
        textViewUsername = findViewById(R.id.tvUsername);
        imageViewChoose = findViewById(R.id.imgChoose);
    }

    public static String[] permissions() {
        String[] p;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU) {
            p = storge_permissions_33;
        } else {
            p = storge_permissions;
        }
        return p;
    }

    private void CheckPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            openGallery();
            return;
        }

        if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        } else { //ActivityCompot.requestPermissions (UploadImageActivity.this,permissions(),MY REQUEST CODE);
//hoặc
            requestPermissions(permissions(), MY_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            }
        }
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction (Intent.ACTION_GET_CONTENT);
        mActivityResultLauncher.launch (Intent.createChooser (intent, "Select Picture"));
    }
}