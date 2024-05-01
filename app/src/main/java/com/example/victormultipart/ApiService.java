package com.example.victormultipart;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {
    @Multipart
    @POST("create")
    Call<RegisterResponse> registerUser(
            @Part MultipartBody.Part image,
            @Part("firstName") RequestBody firstName,
            @Part("lastName") RequestBody lastName,
            @Part("email") RequestBody email,
            @Part("password") RequestBody password,
            @Part("phone") RequestBody phone,
            @Part("stateOfResidence") RequestBody stateOfResidence,
            @Part("street") RequestBody street,
            @Part("city") RequestBody city

    );
}


