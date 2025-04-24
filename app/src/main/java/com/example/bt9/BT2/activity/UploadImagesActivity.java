package com.example.bt9.BT2.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.content.CursorLoader;

import com.example.bt9.BT2.retrofit.ApiService;
import com.example.bt9.R;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UploadImagesActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private Button btnChooseFile, btnUploadImages;
    private ImageView previewImageView;
    private Uri imageUri;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_images);

        // Get User ID from intent
        userId = getIntent().getStringExtra("USER_ID");

        // Initialize UI elements
        btnChooseFile = findViewById(R.id.btn_choose_file);
        btnUploadImages = findViewById(R.id.btn_upload_images);
        previewImageView = findViewById(R.id.preview_image_view);

        // Check if an image was passed
        String imageUriString = getIntent().getStringExtra("IMAGE_URI");
        if (imageUriString != null && !imageUriString.isEmpty()) {
            imageUri = Uri.parse(imageUriString);
            previewImageView.setImageURI(imageUri);
        }

        // Set click listeners for buttons
        btnChooseFile.setOnClickListener(v -> openFileChooser());
        btnUploadImages.setOnClickListener(v -> uploadImages());
    }

    private void openFileChooser(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            previewImageView.setImageURI(imageUri);
        }
    }

    private void uploadImages() {
        if (imageUri == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show progress dialog
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading images...");
        progressDialog.show();

        // Create a file body for the image
        try {
            // Set up Retrofit call to upload image
            uploadImageToApi(progressDialog);
        } catch (Exception e) {
            progressDialog.dismiss();
            Toast.makeText(this, "Error uploading images: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    private void uploadImageToApi(final ProgressDialog progressDialog) {
        // create Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://app.iotstar.vn:8081/appfoods/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        // Convert image uri to file
        File file = null;
        try {
            file = new File(getRealPathFromURI(imageUri));
        } catch (Exception e) {
            progressDialog.dismiss();
            Toast.makeText(this, "Error preparing file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        // Create request body for file and user ID
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part imagePart = MultipartBody.Part.createFormData("Images", file.getName(), requestFile);
        RequestBody idPart = RequestBody.create(MediaType.parse("text/plain"), userId);

        // Execute API call
        Call<ResponseBody> call = apiService.uploadImage(idPart, imagePart);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                progressDialog.dismiss();
                if (response.isSuccessful()) {
                    Toast.makeText(UploadImagesActivity.this, "Upload successful", Toast.LENGTH_SHORT).show();
                    finish(); // Return to previous activity
                } else {
                    Toast.makeText(UploadImagesActivity.this, "Upload failed: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(UploadImagesActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    // Helper method to get file path from URI
    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        CursorLoader loader = new CursorLoader(this, contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
    }
}
