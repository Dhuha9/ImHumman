package com.example.imhumman;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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

import java.util.ArrayList;

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
        Log.i("llss", "" + currentItem);
        Log.i("llss", "" + mPostsList);
        Log.i("llss", "" + position);

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
        holder.mDetailsText.setText(currentItem.getContent());
        holder.mCallText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + currentItem.getPhone_number()));
                view.getContext().startActivity(intent);

            }
        });
        holder.mLocationText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, MapActivityDisplayLocation.class);
                LatLng userLocation = new LatLng(currentItem.getLocation_latitude(), currentItem.getLocation_longitude());
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
                String post = currentItem.getUser_name() + "\n\n" + currentItem.getContent() + "\n" + "الموقع: " + currentItem.getLocation_latitude() + "\n" + "رقم الهاتف: " + currentItem.getPhone_number();
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, post);
                sendIntent.setType("text/plain");

                Intent shareIntent = Intent.createChooser(sendIntent, null);
                view.getContext().startActivity(shareIntent);
            }
        });
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
        TextView mNameText, mDetailsText, mCallText, mLocationText;
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
