package com.example.imhumman;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

class UpdateUserInfo {
    private UserProfileChangeRequest profileUpdates;
    private FirebaseUser currentUser;
    private Context mContext;

    UpdateUserInfo(FirebaseUser user, Context context) {
        mContext = context;
        currentUser = user;
    }

    void updateName(String name) {

        profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build();
        currentUser.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(mContext, "name updated", Toast.LENGTH_SHORT).show();

                        }
                    }
                });
        Toast.makeText(mContext, "user in update " + currentUser, Toast.LENGTH_LONG).show();
    }

    void updateEmail(String email) {
        currentUser.updateEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(mContext, "email updated", Toast.LENGTH_SHORT).show();
                            emailVerification();
                        }
                    }
                });
    }

    private void emailVerification() {
        currentUser.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            if (currentUser.isEmailVerified()) {
                                Toast.makeText(mContext, "email verified", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    void updatePassword(String email, final ProgressBar progressBar) {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            Toast.makeText(mContext, "تم ارسال ايميل يمكنك من تغيير الباسورد", Toast.LENGTH_LONG).show();
                            //updateUserPassword(email,password);
                        } else {
                            Toast.makeText(mContext, "فشلت عملية ارسال ايميل لتغيير الباسورد , يرجى المحاولة مرة اخرى", Toast.LENGTH_LONG).show();
                        }

                    }
                });
    }

/*    private void updateUserPassword(String email,String password) {
        currentUser.updatePassword(password)
            .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(mContext, "password updated", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    }*/

    void updatePhoneNumber(PhoneAuthCredential phoneNumberCredential) {
        currentUser.updatePhoneNumber(phoneNumberCredential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(mContext, "phone number updated", Toast.LENGTH_SHORT).show();

            }
        });
    }

    void updateImageUri(Uri imageUri) {
        if (FirebaseAuth.getInstance().getCurrentUser() != null && FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl() != null) {
            final String previousImage = String.valueOf(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl());
            Toast.makeText(mContext, "in update fun" + previousImage, Toast.LENGTH_LONG).show();
            Log.i("imgg", previousImage);
            profileUpdates = new UserProfileChangeRequest.Builder()
                    .setPhotoUri(imageUri)
                    .build();
            currentUser.updateProfile(profileUpdates)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful() && !previousImage.isEmpty()) {
                                FirebaseStorage storage = FirebaseStorage.getInstance();
                                StorageReference imagePostRef = storage.getReferenceFromUrl(previousImage);
                                imagePostRef.delete();
                                Toast.makeText(mContext, "image updated", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }

    }

//    private void update(UserProfileChangeRequest profileUpdates){
//        currentUser.updateProfile()
//    }
}
