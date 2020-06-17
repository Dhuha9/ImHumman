package com.example.imhumman;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class FullScreenImageDisplay extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image_display);
        ImageView fullSizeImage = findViewById(R.id.fullSizeImage);
        Intent intent = getIntent();
        String imageUri = intent.getStringExtra("postImageUri");
        Picasso.get()
                .load(imageUri)
                .centerInside()
                .fit()
                .into(fullSizeImage);
    }
}
