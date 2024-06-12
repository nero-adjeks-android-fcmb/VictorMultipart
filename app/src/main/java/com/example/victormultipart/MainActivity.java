package com.example.victormultipart;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_GALLERY = 2;
    private static final int REQUEST_PDF = 3;
    private ImageView imageView;
    private Bitmap capturedImageBitmap;
    private Uri selectedImageUri;
    private Uri selectedPdfUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);

        Button btnCaptureImage = findViewById(R.id.btnCaptureImage);
        btnCaptureImage.setOnClickListener(v -> dispatchTakePictureIntent());

        Button btnSelectImage = findViewById(R.id.btnSelectImage);
        btnSelectImage.setOnClickListener(v -> dispatchSelectImageIntent());

        Button btnSelectPdf = findViewById(R.id.btnSelectPdf);
        btnSelectPdf.setOnClickListener(v -> dispatchSelectPdfIntent());

        Button btnRegisterImage = findViewById(R.id.btnRegisterImage);
        btnRegisterImage.setOnClickListener(v -> registerUserImage());

        Button btnRegisterPdf = findViewById(R.id.btnRegisterPdf);
        btnRegisterPdf.setOnClickListener(v -> registerUserPdf());
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void dispatchSelectImageIntent() {
        Intent selectImageIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(selectImageIntent, REQUEST_IMAGE_GALLERY);
    }

    private void dispatchSelectPdfIntent() {
        Intent selectPdfIntent = new Intent(Intent.ACTION_GET_CONTENT);
        selectPdfIntent.setType("application/pdf");
        startActivityForResult(selectPdfIntent, REQUEST_PDF);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                Bundle extras = data.getExtras();
                if (extras != null) {
                    capturedImageBitmap = (Bitmap) extras.get("data");
                    if (capturedImageBitmap != null) {
                        imageView.setImageBitmap(capturedImageBitmap);
                        imageView.setVisibility(View.VISIBLE);
                    }
                }
            } else if (requestCode == REQUEST_IMAGE_GALLERY) {
                selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    try {
                        capturedImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                        imageView.setImageBitmap(capturedImageBitmap);
                        imageView.setVisibility(View.VISIBLE);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else if (requestCode == REQUEST_PDF) {
                selectedPdfUri = data.getData();
                if (selectedPdfUri != null) {
                    Toast.makeText(this, "PDF Selected: " + selectedPdfUri.getPath(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void registerUserImage() {
        String providerId = "50";

        if (capturedImageBitmap == null && selectedImageUri == null) {
            Toast.makeText(this, "Please capture or select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convert bitmap to byte array
        byte[] byteArray = null;
        if (capturedImageBitmap != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            capturedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byteArray = stream.toByteArray();
        } else if (selectedImageUri != null) {
            try {
                InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                if (inputStream != null) {
                    byteArray = new byte[inputStream.available()];
                    inputStream.read(byteArray);
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Create request body for image
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/png"), byteArray);

        // Create multipart body part
        MultipartBody.Part imagePart = MultipartBody.Part.createFormData("cacImage", "profile_image.png", requestFile);

        // Create other request bodies
        RequestBody providerIdRequestBody = RequestBody.create(MediaType.parse("text/plain"), providerId);

        // Log the request details
//        System.out.println("Request: providerId=" + providerId + ", image=profile_image.png");

        // Make API call
        ApiClient.getApiService().uploadImage(providerIdRequestBody, imagePart).enqueue(new Callback<VerifyFileResponse>() {
            @Override
            public void onResponse(@NonNull Call<VerifyFileResponse> call, @NonNull Response<VerifyFileResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    System.out.println("Response: " + response.body().toString());
                    Toast.makeText(MainActivity.this, "Image uploaded successfully: " + response.body().toString(), Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        // Log response error body
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "null";
                        System.out.println("Error Response: " + errorBody);
                        Toast.makeText(MainActivity.this, "Failed to upload image: " + errorBody, Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<VerifyFileResponse> call, @NonNull Throwable t) {
                System.out.println("Failure: " + t.getMessage());
                Toast.makeText(MainActivity.this, "Failed to upload image: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void registerUserPdf() {
        String providerId = "50";

        if (selectedPdfUri == null) {
            Toast.makeText(this, "Please select a PDF file", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create temporary file for PDF
        File pdfFile = new File(getCacheDir(), "document.pdf");
        try (InputStream inputStream = getContentResolver().openInputStream(selectedPdfUri);
             FileOutputStream outputStream = new FileOutputStream(pdfFile)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Create request body for PDF
        RequestBody pdfRequestBody = RequestBody.create(MediaType.parse("application/pdf"), pdfFile);
        MultipartBody.Part pdfPart = MultipartBody.Part.createFormData("cacImage", "document.pdf", pdfRequestBody);

        // Create other request bodies
        RequestBody providerIdRequestBody = RequestBody.create(MediaType.parse("text/plain"), providerId);

        // Make API call
        ApiClient.getApiService().uploadDocument(providerIdRequestBody, pdfPart).enqueue(new Callback<VerifyFileResponse>() {
            @Override
            public void onResponse(@NonNull Call<VerifyFileResponse> call, @NonNull Response<VerifyFileResponse> response) {
                if (response.body() != null && response.isSuccessful() && response.body().isSuccess()) {
                    System.out.println(response.body().toString());
                    Toast.makeText(MainActivity.this, "PDF uploaded successfully: " + response.body().toString(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Failed to upload PDF: " + response.body().toString(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<VerifyFileResponse> call, @NonNull Throwable t) {
                Toast.makeText(MainActivity.this, "Failed to upload PDF: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
