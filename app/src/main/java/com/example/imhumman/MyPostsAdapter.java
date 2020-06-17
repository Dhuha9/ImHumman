package com.example.imhumman;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.imhumman.firebaseModels.PostDataModel;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

public class MyPostsAdapter extends RecyclerView.Adapter<MyPostsAdapter.PostViewHolder> {

    private DatabaseReference mDatabase;
    private DatabaseReference postsTable;
    private ArrayList<PostDataModel> mPostsList;
    private Context mContext;

    MyPostsAdapter(ArrayList<PostDataModel> postsList, Context context) {
        mContext = context;
        mPostsList = postsList;
        Toast.makeText(context, "" + postsList, Toast.LENGTH_SHORT).show();
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_mypost, parent, false);
        return new PostViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, final int position) {
        final PostDataModel currentItem = mPostsList.get(position);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        postsTable = mDatabase.child("posts");
        if (currentItem.getPost_photo().equals("NO_POST_PHOTO")) {
            holder.mPostImage.setVisibility(View.GONE);
        } else {
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


        Picasso.get()
                .load(currentItem.getUser_photo())
                .fit()
                .centerCrop()
                .into(holder.mUserImage);

        holder.mNameText.setText(currentItem.getUser_name());
        holder.mDetailsText.setText(currentItem.getContent());
        holder.mEditPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String, Object> postValues = currentItem.toMap();
                Log.i("ph2", "" + postValues);
                Log.i("ph2c", "" + currentItem);

                Intent intent = new Intent(mContext, AddPostActivity.class);
                intent.putExtra("postId", currentItem.getPostId());
                intent.putExtra("map", (Serializable) postValues);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

                ((Activity) mContext).startActivityForResult(intent, 500);
            }
        });
        holder.mDeletPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postsTable.child(currentItem.getPostId()).removeValue();
                if (!currentItem.getPost_photo().equals("NO_POST_PHOTO")) {
                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference imagePostRef = storage.getReferenceFromUrl(currentItem.getPost_photo());
                    imagePostRef.delete();
                    Toast.makeText(mContext, "Post Deleted Successfully", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPostsList.size();
    }

    public void setData(ArrayList<PostDataModel> postData) {
        this.mPostsList = postData;
    }

    public Long getLastItemDate() {

        return mPostsList.get(mPostsList.size() - 1).getCreationDateLong();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView mNameText, mDetailsText, mEditPost, mDeletPost;
        ImageView mUserImage;
        ImageView mPostImage;


        PostViewHolder(@NonNull View itemView) {
            super(itemView);
            mNameText = itemView.findViewById(R.id.txtUserName);
            mDetailsText = itemView.findViewById(R.id.txtDetails);
            mUserImage = itemView.findViewById(R.id.userImage);
            mPostImage = itemView.findViewById(R.id.postImage);
            mEditPost = itemView.findViewById(R.id.txtEdit);
            mDeletPost = itemView.findViewById(R.id.txtDelete);

        }
    }


}
