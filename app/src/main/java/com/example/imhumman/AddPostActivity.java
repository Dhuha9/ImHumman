package com.example.imhumman;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.imhumman.firebaseModels.PostDataModel;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AddPostActivity extends AppCompatActivity implements Fragment.dialogFragmentListener {


    PostDataModel postData;
    DatabaseReference databaseRef;
    DatabaseReference postsTable;
    DatabaseReference usersTable;
    String postId;

    Uri postImgUri = null;
    String postImageFromIntent;
    ImageView icAddPhoneNumber, icAddLocation, imgAddImagePost;
    EditText edtContent;
    TextView txtAddLocation, txtAddPhoneNumber;
    LatLng latLng;
    String postPhoneNumber;
    Uri postImgPath = null;

    String whatToDo;
    int PICK_IMAGE_REQUEST = 1;
    int GET_LATLONG = 2;
    ProgressBar addPostProgress;

    View.OnClickListener showFileChooser = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);

            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
        }
    };
    private View.OnClickListener showMap = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(AddPostActivity.this, MapActivity.class);
            startActivityForResult(intent, GET_LATLONG);

        }
    };
    private View.OnClickListener addPhoneNumber = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Fragment dialogFragment = new Fragment();
            Bundle bundle = new Bundle();
            bundle.putString("info", txtAddPhoneNumber.getText().toString());
            dialogFragment.setArguments(bundle);
            dialogFragment.show(getSupportFragmentManager(), "phone");

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);
        setToolbar();
        databaseRef = FirebaseDatabase.getInstance().getReference();
        postsTable = databaseRef.child("posts");
        postData = new PostDataModel();
        imgAddImagePost = findViewById(R.id.imgAddImagePost);
        imgAddImagePost.setOnClickListener(showFileChooser);

        txtAddPhoneNumber = findViewById(R.id.txtAddPhoneNumber);

        icAddLocation = findViewById(R.id.icAddLocation);
        icAddLocation.setOnClickListener(showMap);


        txtAddLocation = findViewById(R.id.txtAddLocation);

        icAddPhoneNumber = findViewById(R.id.icAddPhoneNumber);
        icAddPhoneNumber.setOnClickListener(addPhoneNumber);

        edtContent = findViewById(R.id.edtContent);

        addPostProgress = findViewById(R.id.addPostProgress);

        whatToDo = "AddPostActivity";
        if (getCallingActivity() != null) {

            if (getCallingActivity().getClassName().equals(MyPostsActivity.class.getName())) {
                whatToDo = "UpdatePost";
                Intent intent = getIntent();
                postId = intent.getExtras().getString("postId");
                Map<String, Object> postValues = (Map<String, Object>) intent.getSerializableExtra("map");

                if (postValues != null) {
                    String contentData = postValues.get("content").toString();
                    double latitudeData = (double) postValues.get("location_latitude");
                    double longitudeData = (double) postValues.get("location_longitude");
                    latLng = new LatLng(latitudeData, longitudeData);
                    LatLng updatedLocation = new LatLng(latitudeData, longitudeData);
                    Log.i("ph", "" + postValues);
                    Log.i("ph", "" + postValues.get("phone_number"));

                    String phoneNumberData = postValues.get("phone_number").toString();
                    postImageFromIntent = postValues.get("post_photo").toString();

                    Picasso.get()
                            .load(postImageFromIntent)
                            .fit()
                            .centerCrop()
                            .into(imgAddImagePost);


                    edtContent.setText(contentData);
                    txtAddPhoneNumber.setText(phoneNumberData);
                    getMapLocation(updatedLocation);


                }
            }

        }
    }

    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.add_post_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.addPost) {
            String content = edtContent.getText().toString();
            if (latLng == null || content.isEmpty() || postPhoneNumber.isEmpty()) {
                Toast.makeText(this, "you have to fill the data needed", Toast.LENGTH_SHORT).show();
            } else {
                addPostProgress.setVisibility(View.VISIBLE);
                uploadPostImage();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void uploadPostImage() {

        if (postImgPath == null) {
            postImgUri = Uri.parse("NO_POST_PHOTO");
            if (whatToDo.equals("AddPostActivity")) {
                getUserInfo();
            } else if (whatToDo.equals("UpdatePost")) {
                updatePostTable();
            }
        } else {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageReference = storage.getReference();
            final StorageReference imgRef = storageReference.child("postImages/" + postImgPath.getLastPathSegment());
            UploadTask uploadTask = imgRef.putFile(postImgPath);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(AddPostActivity.this, "Failed to upload", Toast.LENGTH_LONG).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(AddPostActivity.this, "Successful upload", Toast.LENGTH_LONG).show();

                }
            });

            // Task<Uri> uriTask =
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
                        postImgUri = task.getResult();
                        Toast.makeText(AddPostActivity.this, postImgUri.toString(), Toast.LENGTH_SHORT).show();
                        if (whatToDo.equals("AddPostActivity")) {
                            getUserInfo();
                        } else if (whatToDo.equals("UpdatePost")) {
                            updatePostTable();
                        }

                    }
                }
            });

        }
    }


    private void getUserInfo() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String fullName = user.getDisplayName();
        String userImage;
        if (user.getPhotoUrl() != null) {
            userImage = user.getPhotoUrl().toString();
        } else {
            userImage = "NO_PhOTO";
        }
        String userId = user.getUid();
        insertPostToDb(fullName, userImage, userId);

    }

    private void insertPostToDb(String fullName, String userImage, String userId) {

        postData.setUser_name(fullName);
        postData.setContent(edtContent.getText().toString());
        postData.setLocation_latitude(latLng.latitude);
        postData.setLocation_longitude(latLng.longitude);
        postData.setPhone_number(postPhoneNumber);
        postData.setPost_photo(postImgUri.toString());
        postData.setUser_photo(userImage);
        postData.setUid(userId);

        String key = postsTable.push().getKey();
        postData.setPostId(key);

        if (key != null) {
            postsTable.child(key).setValue(postData).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    addPostProgress.setVisibility(View.GONE);
                    Intent intent = new Intent(AddPostActivity.this, AllPostsActivity.class);
                    startActivity(intent);
                }

            });

        }


    }

    private void updatePostTable() {
        String postImageDownloadUri;
        if (postId != null) {
            String content = edtContent.getText().toString();
            if (postImgUri.toString().equals("NO_POST_PHOTO")) {
                postImageDownloadUri = postImageFromIntent;
            } else {
                postImageDownloadUri = postImgUri.toString();
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference imagePostRef = storage.getReferenceFromUrl(postImageFromIntent);
                imagePostRef.delete();
            }
            Log.i("phh", "" + txtAddPhoneNumber.getText());
            String phone = postPhoneNumber == null ? txtAddPhoneNumber.getText().toString() : postPhoneNumber;
            Log.i("phh2", "" + phone);

            PostDataModel postData = new PostDataModel(content, latLng.latitude, latLng.longitude, phone, postImageDownloadUri);
            Log.i("phPost", "" + postData);
            Log.i("phPost", "" + postData.getPhone_number());

            Map<String, Object> postValues = postData.toMap();
            Log.i("phPost", "" + postValues);

            postsTable.child(postId).updateChildren(postValues);
            Toast.makeText(this, "Post Edited successfully", Toast.LENGTH_SHORT).show();
            addPostProgress.setVisibility(View.GONE);
            startActivity(new Intent(this, MyPostsActivity.class));
        } else {
            Toast.makeText(this, postId + "is null", Toast.LENGTH_SHORT).show();
        }
    }

    private void getMapLocation(LatLng latLng) {

        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        try {
            List<Address> listAdressess = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (listAdressess != null && listAdressess.size() > 0) {

                //to take part of the list of location info
                String stringAddress = "";

                if (listAdressess.get(0).getLocality() != null) {
                    stringAddress += listAdressess.get(0).getLocality() + " ,";
                }

                if (listAdressess.get(0).getAdminArea() != null) {
                    stringAddress += listAdressess.get(0).getAdminArea() + " ,";
                }
                if (listAdressess.get(0).getCountryName() != null) {
                    stringAddress += listAdressess.get(0).getCountryName();
                }

                txtAddLocation.setText(stringAddress);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            if (data != null) {
                postImgPath = data.getData();
                imgAddImagePost.setImageURI(postImgPath);

            }
        } else if (requestCode == GET_LATLONG && resultCode == RESULT_OK) {

            if (data != null && data.getExtras() != null && !data.getExtras().isEmpty()) {

                latLng = data.getExtras().getParcelable("latlng");
                getMapLocation(latLng);
            }
        }
    }


    @Override
    public void applyText(String txt) {
        postPhoneNumber = txt;
        txtAddPhoneNumber.setText(postPhoneNumber);
    }
}


