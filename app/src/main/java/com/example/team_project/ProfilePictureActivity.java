package com.example.team_project;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.team_project.fragments.ProfileFragment;
import com.example.team_project.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ProfilePictureActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 111;
    public ImageView profileImageView;
    public Button takePictureButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_picture);

        takePictureButton = findViewById(R.id.take_picture_button);
        profileImageView = findViewById(R.id.profile_image_view);

        takePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLaunchCamera();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == this.RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            profileImageView.setImageBitmap(imageBitmap);
            encodeBitmapAndSaveToFirebase(imageBitmap);
            finish();
            Intent toProfile = new Intent(this, MainActivity.class);
            startActivity(toProfile);
        }
    }

    public void onLaunchCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(this.getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
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
}
