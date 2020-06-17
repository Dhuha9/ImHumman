package com.example.imhumman;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.imhumman.firebaseModels.PostDataModel;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.Map;

public class UserProfileActivity extends AppCompatActivity implements Fragment.dialogFragmentListener {
    TextView txtNameProfile, txtEmailProfile, txtPhoneNumberProfile, txtPasswordProfile;
    ImageView icName, icEmail, icPhoneNumber, icPassword;
    ImageView userImage, userImageChanging;
    Button btnUpdate;
    int PICK_IMAGE_REQUEST = 1;
    Uri userImgPath = null;
    Uri userImgUri;
    Uri userImageUrlFromUser;
    FirebaseUser user;
    TextView v;
    DatabaseReference databaseRef;
    DatabaseReference usersTable;
    DatabaseReference postsTable;
    String infoToEdit;
    int GET_PHONE_CREDENTIAL = 500;
    UpdateUserInfo userInfo;

    private View.OnClickListener changeUserImage = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);

            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            btnUpdate.setVisibility(View.VISIBLE);
        }
    };

    private View.OnClickListener setEditedInfo = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            btnUpdate.setVisibility(View.VISIBLE);
            Toast.makeText(UserProfileActivity.this, "" + icName, Toast.LENGTH_LONG).show();
            switch (view.getId()) {
                case R.id.icName:
                    v = txtNameProfile;
                    infoToEdit = txtNameProfile.getText().toString();
                    break;
                case R.id.icEmail:
                    v = txtEmailProfile;
                    infoToEdit = txtEmailProfile.getText().toString();
                    break;
                case R.id.icPhoneNumber:
                    v = txtPhoneNumberProfile;
                    infoToEdit = txtPhoneNumberProfile.getText().toString();
                    break;
                case R.id.icPassword:
                    v = txtPasswordProfile;
                    infoToEdit = txtPasswordProfile.getText().toString();
                    break;
            }

            showEditDialog();

        }
    };
    private View.OnClickListener updateUserInfo = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            uploadUserImage();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        setToolbar();
        databaseRef = FirebaseDatabase.getInstance().getReference();
        usersTable = databaseRef.child("users");
        postsTable = databaseRef.child("posts");

        txtNameProfile = findViewById(R.id.txtNameProfile);
        txtEmailProfile = findViewById(R.id.txtEmailProfile);
        txtPhoneNumberProfile = findViewById(R.id.txtPhoneNumberProfile);
        txtPasswordProfile = findViewById(R.id.txtPasswordProfile);

        icName = findViewById(R.id.icName);
        icName.setOnClickListener(setEditedInfo);

        icEmail = findViewById(R.id.icEmail);
        icEmail.setOnClickListener(setEditedInfo);


        icPhoneNumber = findViewById(R.id.icPhoneNumber);
        icPhoneNumber.setOnClickListener(setEditedInfo);

        icPassword = findViewById(R.id.icPassword);
        icPassword.setOnClickListener(setEditedInfo);

        userImage = findViewById(R.id.userImage);
        userImageChanging = findViewById(R.id.userImageChanging);
        userImageChanging.setOnClickListener(changeUserImage);

        btnUpdate = findViewById(R.id.btnUpdate);
        btnUpdate.setOnClickListener(updateUserInfo);

        user = FirebaseAuth.getInstance().getCurrentUser();
        userInfo = new UpdateUserInfo(user, UserProfileActivity.this);
        setUserData();


    }

    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
    }

    private void uploadUserImage() {
        if (userImgPath == null) {
            userImgUri = Uri.parse("NO_USER_PHOTO");
            setUserProfile();
        } else {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageReference = storage.getReference();
            final StorageReference imgRef = storageReference.child("userImages/" + userImgPath.getLastPathSegment());
            UploadTask uploadTask = imgRef.putFile(userImgPath);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(UserProfileActivity.this, "Failed to upload", Toast.LENGTH_LONG).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(UserProfileActivity.this, "Successful upload", Toast.LENGTH_LONG).show();

                }
            });

            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return imgRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        userImgUri = task.getResult();
                        setUserProfile();

                    }
                }
            });
        }
    }

    private Uri getUserImageUri() {

        Uri userImgUriToInsert;
        if (userImgUri.toString().equals("NO_USER_PHOTO")) {
            userImgUriToInsert = userImageUrlFromUser;
        } else {
            userImgUriToInsert = userImgUri;
        }
        return userImgUriToInsert;
    }

    private void setUserProfile() {


        if (!user.getDisplayName().equals(txtNameProfile.getText().toString())) {
            userInfo.updateName(txtNameProfile.getText().toString());
        }

        if (userImgUri != null && !(userImgUri.toString().equals("NO_USER_PHOTO"))) {
            userInfo.updateImageUri(getUserImageUri());
        }
        if (!user.getEmail().equals(txtEmailProfile.getText().toString())) {
            userInfo.updateEmail(txtEmailProfile.getText().toString());
        }

        if (!user.getPhoneNumber().equals(txtPhoneNumberProfile.getText().toString())) {
            Intent intent = new Intent(UserProfileActivity.this, PhoneAndEmailAuthActivity.class);
            intent.putExtra("whichToVerify", "phoneNumber");
            intent.putExtra("phoneNumber", txtPhoneNumberProfile.getText().toString());
            startActivityForResult(intent, GET_PHONE_CREDENTIAL);
        }

        updateUserPosts();

    }


    private void updateUserPosts() {
        postsTable.orderByChild("uid").equalTo(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {

                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        PostDataModel postData = ds.getValue(PostDataModel.class);
                        postData.setUser_name(txtNameProfile.getText().toString());
                        postData.setUser_photo(getUserImageUri().toString());
                        Map<String, Object> postValues = postData.toMapUserInfo();
                        postsTable.child(postData.getPostId()).updateChildren(postValues);
                    }
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            if (data != null) {
                userImgPath = data.getData();
                userImage.setImageURI(userImgPath);
            }
        } else if (requestCode == GET_PHONE_CREDENTIAL && resultCode == RESULT_OK) {
            userInfo.updatePhoneNumber((PhoneAuthCredential) data.getParcelableExtra("PhoneNumberCredential"));
        }
    }

    private void setUserData() {

        txtNameProfile.setText(user.getDisplayName());
        txtEmailProfile.setText(user.getEmail());
        txtPhoneNumberProfile.setText(user.getPhoneNumber());
        Picasso.get()
                .load(user.getPhotoUrl())
                .fit()
                .centerCrop()
                .into(userImage);
        userImageUrlFromUser = user.getPhotoUrl();
    }

    @Override
    public void applyText(String txt) {
        v.setText(txt);
    }

    private void showEditDialog() {

        Fragment dialogFragment = new Fragment();
        Bundle bundle = new Bundle();
        bundle.putString("info", infoToEdit);
        dialogFragment.setArguments(bundle);
        dialogFragment.show(getSupportFragmentManager(), "profile");
        btnUpdate.setVisibility(View.VISIBLE);
    }


}