package com.example.victormultipart;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private EditText editTextFirstName, editTextLastName, editTextEmail, editTextPhone, editTextPassword;
    private ImageView imageView;
    private Bitmap capturedImageBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextFirstName = findViewById(R.id.editTextFirstName);
        editTextLastName = findViewById(R.id.editTextLastName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextPhone = findViewById(R.id.editTextPhone);
        imageView = findViewById(R.id.imageView);

        Button btnCaptureImage = findViewById(R.id.btnCaptureImage);
        btnCaptureImage.setOnClickListener(v -> dispatchTakePictureIntent());

        Button btnRegister = findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(v -> registerUser());
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            if (extras != null) {
                capturedImageBitmap = (Bitmap) extras.get("data");
                if (capturedImageBitmap != null) {
                    imageView.setImageBitmap(capturedImageBitmap);
                    imageView.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private void registerUser() {
        String firstName = editTextFirstName.getText().toString().trim();
        String lastName = editTextLastName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (capturedImageBitmap == null) {
            Toast.makeText(this, "Please capture an image", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convert bitmap to byte array
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        capturedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        // Create request body for image
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/png"), byteArray);

        // Create multipart body part
        MultipartBody.Part imagePart = MultipartBody.Part.createFormData("image", "profile_image.jpg", requestFile);

        // Create other request bodies
        RequestBody firstNameRequestBody = RequestBody.create(MediaType.parse("text/plain"), firstName);
        RequestBody lastNameRequestBody = RequestBody.create(MediaType.parse("text/plain"), lastName);
        RequestBody emailRequestBody = RequestBody.create(MediaType.parse("text/plain"), email);
        RequestBody phoneRequestBody = RequestBody.create(MediaType.parse("text/plain"), phone);
        RequestBody passwordRequestBody = RequestBody.create(MediaType.parse("text/plain"), password);
        RequestBody stateOfResidenceRequestBody = RequestBody.create(MediaType.parse("text/plain"), "Edo");
        RequestBody cityRequestBody = RequestBody.create(MediaType.parse("text/plain"), "Benin");
        RequestBody streetRequestBody = RequestBody.create(MediaType.parse("text/plain"), "Ring road");

        // Make API call
        ApiClient.getApiService().registerUser(imagePart, firstNameRequestBody, lastNameRequestBody,
                emailRequestBody, passwordRequestBody, phoneRequestBody, stateOfResidenceRequestBody, streetRequestBody, cityRequestBody).enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(@NonNull Call<RegisterResponse> call, @NonNull Response<RegisterResponse> response) {
                if (response.isSuccessful()) {
                    System.out.println(response.body().toString());
                    Toast.makeText(MainActivity.this, "User registered successfully:========>" + response.body().toString(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Error: " + response.body().getError(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<RegisterResponse> call, @NonNull Throwable t) {
                System.out.println(t.getMessage());
                Toast.makeText(MainActivity.this, "Failed to register user: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
