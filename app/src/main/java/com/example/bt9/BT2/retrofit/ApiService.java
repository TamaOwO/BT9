package com.example.bt9.BT2.retrofit;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {
    @Multipart
    @POST("updateimages.php")
    Call<ResponseBody> uploadImage(
            @Part("Id") RequestBody id,
            @Part MultipartBody.Part image
    );
}
