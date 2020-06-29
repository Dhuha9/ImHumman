package com.example.imhumman;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.imhumman.firebaseModels.PostDataModel;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.PostViewHolder> {

    private ArrayList<PostDataModel> mPostsList;
    private ArrayList<String> mSavedPostsIdList;
    private Context mContext;
    private DatabaseReference mDatabase;
    private DatabaseReference savedPost;
    private FirebaseUser mCurrentUser;

    PostsAdapter(ArrayList<PostDataModel> postsList, Context context, ArrayList<String> savedPostsId, FirebaseUser currentUser) {
        mContext = context;
        mPostsList = postsList;
        mSavedPostsIdList = savedPostsId;
        mCurrentUser = currentUser;
    }

    PostsAdapter(ArrayList<PostDataModel> postsList, Context context) {
        mContext = context;
        mPostsList = postsList;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_post, parent, false);
        return new PostViewHolder(v);

    }

    @Override
    public void onBindViewHolder(@NonNull final PostViewHolder holder, int position) {
        final PostDataModel currentItem = mPostsList.get(position);

        if (currentItem.getPost_photo() != null && currentItem.getPost_photo().equals("NO_POST_PHOTO")) {
            holder.mPostImage.setVisibility(View.GONE);
        } else {
            holder.mPostImage.setVisibility(View.VISIBLE);
            Picasso.get()
                    .load(currentItem.getPost_photo())
                    .fit()
                    .centerCrop()
                    .into(holder.mPostImage);
        }
        holder.mPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, FullScreenImageDisplay.class);
                intent.putExtra("postImageUri", currentItem.getPost_photo());
                view.getContext().startActivity(intent);
            }
        });


        if (currentItem.getUser_photo().equals("NO_USER_PhOTO")) {
            holder.mUserImage.setImageResource(R.drawable.user_no_photo);


        } else {

            Picasso.get()
                    .load(currentItem.getUser_photo())
                    .fit()
                    .centerCrop()
                    .into(holder.mUserImage);
        }
        if (mSavedPostsIdList != null && mSavedPostsIdList.contains(currentItem.getPostId())) {
            holder.mSaveIcon.setImageResource(R.drawable.saveposticon);
        }
        if (mContext instanceof SavedPostsActivity) {
            holder.mSaveIcon.setImageResource(R.drawable.saveposticon);
        }

        holder.mNameText.setText(currentItem.getUser_name());

        if (currentItem.getContent().length() > 60) {
            String str = currentItem.getContent().substring(0, 40) + " ...اعرض المزيد";
            String str2 = currentItem.getContent() + "  اعرض اقل";

            final SpannableString ss = new SpannableString(str);
            final SpannableString ss2 = new SpannableString(str2);

            ClickableSpan clickableSpan1 = new ClickableSpan() {
                @Override
                public void onClick(@NonNull View view) {

                    if (holder.mDetailsText.getText().length() < 56) {
                        holder.mDetailsText.setText(ss2);
                    } else {
                        holder.mDetailsText.setText(ss);
                    }
                }
            };

            ss.setSpan(clickableSpan1, str.length() - 11, str.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ss2.setSpan(clickableSpan1, str2.length() - 8, str2.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            holder.mDetailsText.setText(ss);
            holder.mDetailsText.setMovementMethod(LinkMovementMethod.getInstance());

        } else {
            holder.mDetailsText.setText(currentItem.getContent());
        }

        holder.mCallText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + currentItem.getPhone_number()));
                view.getContext().startActivity(intent);

            }
        });

        final LatLng userLocation = new LatLng(currentItem.getLocation_latitude(), currentItem.getLocation_longitude());

        holder.mLocationText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, MapActivityDisplayLocation.class);
                intent.putExtra("latlng", userLocation);
                view.getContext().startActivity(intent);
            }
        });
        holder.mSaveIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentUser != null) {

                    mDatabase = FirebaseDatabase.getInstance().getReference();
                    savedPost = mDatabase.child("savedPost");
                    if (holder.mSaveIcon.getDrawable().getConstantState() == ContextCompat.getDrawable(mContext, R.drawable.saveposticon).getConstantState()) {
                        savedPost.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).orderByValue().equalTo(currentItem.getPostId()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                    ds.getRef().removeValue();
                                    holder.mSaveIcon.setImageResource(R.drawable.notsavedposticon);
                                    Toast.makeText(mContext, "post Unsaved Successfully", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    } else {

                        String key = savedPost.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).push().getKey();
                        if (key != null) {
                            savedPost.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(key).setValue(currentItem.getPostId()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(mContext, "Post Saved Successfully", Toast.LENGTH_SHORT).show();
                                    holder.mSaveIcon.setImageResource(R.drawable.saveposticon);
                                }

                            });
                        }
                    }
                } else {
                    Toast.makeText(mContext, "يرجى تسجيل الدخول اولا", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(mContext, SignInActivity.class);
                    view.getContext().startActivity(intent);
                }


            }
        });


        holder.mShareIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String location = getMapLocation(userLocation);
                String post = currentItem.getUser_name() + "\n\n" + currentItem.getContent() + "\n" + "الموقع: " + location + "\n" + "رقم الهاتف: " + currentItem.getPhone_number();
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, post);
                sendIntent.setType("text/plain");

                Intent shareIntent = Intent.createChooser(sendIntent, null);
                view.getContext().startActivity(shareIntent);
            }
        });
    }

    private String getMapLocation(LatLng latLng) {
        //Locale locale=new Locale("ar_IQ");
        Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
        try {
            List<Address> listAddresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (listAddresses != null && listAddresses.size() > 0) {

                //to take part of the list of location info
                String stringAddress = "";

               /* if (listAddresses.get(0).getLocality() != null) {
                    stringAddress += listAddresses.get(0).getLocality() + " ,";
                }
                if (listAddresses.get(0).getSubLocality() != null) {
                    stringAddress += listAddresses.get(0).getSubLocality() + " ,";
                }*/

                if (listAddresses.get(0).getAdminArea() != null) {
                    stringAddress += listAddresses.get(0).getAdminArea() + " ,";
                }
                if (listAddresses.get(0).getCountryName() != null) {
                    stringAddress += listAddresses.get(0).getCountryName();
                }
                Toast.makeText(mContext, "add " + listAddresses.get(0).getAddressLine(1) + listAddresses.get(0).getAddressLine(2) + listAddresses.get(0).getSubLocality() + listAddresses.get(0).getSubThoroughfare() + listAddresses.get(0).getSubAdminArea() + listAddresses.get(0).getFeatureName(), Toast.LENGTH_SHORT).show();

                return stringAddress;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public int getItemCount() {
        return mPostsList.size();
    }

//     void setData(ArrayList<PostDataModel> newPostData) {
//        int initialSize = mPostsList.size();
//        mPostsList.addAll(newPostData);
//        Collections.reverse(mPostsList);
//        notifyItemRangeInserted(initialSize, newPostData.size());
//
//    }


    static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView mNameText, mCallText, mLocationText, mDetailsText;
        ImageView mUserImage, mPostImage, mSaveIcon, mShareIcon;


        PostViewHolder(@NonNull View itemView) {
            super(itemView);
            mNameText = itemView.findViewById(R.id.txtUserName);
            mDetailsText = itemView.findViewById(R.id.txtDetails);
            mUserImage = itemView.findViewById(R.id.userImage);
            mPostImage = itemView.findViewById(R.id.postImage);
            mCallText = itemView.findViewById(R.id.txtCall);
            mLocationText = itemView.findViewById(R.id.txtLocation);
            mSaveIcon = itemView.findViewById(R.id.saveIcon);
            mShareIcon = itemView.findViewById(R.id.icShare);


        }
    }


}
