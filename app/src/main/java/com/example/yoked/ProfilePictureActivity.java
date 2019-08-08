package com.example.yoked;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ProfilePictureActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 111;
    private static final int REQUEST_IMAGE_UPLOAD = 222;
    public ImageView mProfileImageView;
    public Button mTakePictureButton, mUploadPictureButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_picture);

        mTakePictureButton = findViewById(R.id.take_picture_button);
        mUploadPictureButton = findViewById(R.id.upload_picture_button);
        mProfileImageView = findViewById(R.id.profile_image_view);

        mTakePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLaunchCamera();
            }
        });

        mUploadPictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLaunchGallery();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Bitmap imageBitmap = null;
        // from camera
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == this.RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
        }
        // from gallery
        else if (requestCode == REQUEST_IMAGE_UPLOAD) {
            if (data != null) {
                try {
                    imageBitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        // Configure byte output stream
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        // Compress the image further
        // TODO - CHANGE TO HIGHER QUALITY FOR REAL
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        int dimension = getSquareCropDimensionForBitmap(imageBitmap);
        Bitmap croppedBitmap = ThumbnailUtils.extractThumbnail(imageBitmap, dimension, dimension);
        mProfileImageView.setImageBitmap(croppedBitmap);
        encodeBitmapAndSaveToFirebase(croppedBitmap);
        finish();
        Intent toProfile = new Intent(this, MainActivity.class);
        startActivity(toProfile);
    }

    public void onLaunchCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(this.getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void onLaunchGallery()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"), 222);
    }

    public void encodeBitmapAndSaveToFirebase(Bitmap bitmap) {
        // save image to firebase
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        String imageEncoded = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("profile_picture");
        ref.setValue(imageEncoded);
    }

    public int getSquareCropDimensionForBitmap(Bitmap bitmap)
    {
        //use the smallest dimension of the image to crop to
        return Math.min(bitmap.getWidth(), bitmap.getHeight());
    }
}
