package com.example.imhumman;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.imhumman.firebaseModels.PostDataModel;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MyPostsActivity extends AppCompatActivity {

    DatabaseReference mDatabase;
    DatabaseReference postsTable;
    FirebaseUser currentUser;
    RecyclerView mRecyclerView;
    MyPostsAdapter mAdapter;
    LinearLayoutManager mLayoutManager;
    DrawerLayout drawer;
    ArrayList<PostDataModel> postsList;
    ActionBarDrawerToggle toggle;
    Toolbar toolbar;
    NavigationView navigationView;
    LinearLayout footerLayout;
    int postsPerPage = 3;
    FirebaseAuth mAuth;
    TextView nothingToShow;
    ProgressBar progressBar, footerProgressBar;
    Query query;
    NavBarHandler navBarHandler;

    //private boolean shouldLoadMore;
    FirebaseAuth.AuthStateListener firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            mAuth = firebaseAuth;
            currentUser = mAuth.getCurrentUser();
            addDrawer();
        }
    };
    ChildEventListener childEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            if (dataSnapshot.getValue() != null) {
                PostDataModel postData = dataSnapshot.getValue(PostDataModel.class);
                progressBar.setVisibility(View.GONE);
                nothingToShow.setVisibility(View.GONE);

                postsList.add(0, postData);
                mRecyclerView.setHasFixedSize(true);
                mRecyclerView.setLayoutManager(mLayoutManager);
                mRecyclerView.setAdapter(mAdapter);
                mAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            PostDataModel postData = dataSnapshot.getValue(PostDataModel.class);

            for (int i = 0; i < postsList.size(); i++) {
                if (postsList.get(i).getPostId().equals(postData.getPostId())) {
                    postsList.set(i, postData);
                    mAdapter.notifyItemChanged(i);

                }
            }
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            PostDataModel postData = dataSnapshot.getValue(PostDataModel.class);

            for (int i = 0; i < postsList.size(); i++) {
                if (postsList.get(i).getPostId().equals(postData.getPostId())) {
                    postsList.remove(i);
                    mAdapter.notifyItemRemoved(i);


                }
            }
        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    public void loadMyPosts() {
        postsList.clear();
        query = postsTable.orderByChild("uid").equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    progressBar.setVisibility(View.GONE);
                    nothingToShow.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        query.addChildEventListener(childEventListener);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.all_posts);
        setToolbar();
        addDrawer();
        getLayoutViews();
        setFirebaseVariables();
        addDrawer();
        manageRecyclerView();
        loadMyPosts();
    }

    private void setFirebaseVariables() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        postsTable = mDatabase.child("posts");
        mAuth = FirebaseAuth.getInstance();
        mAuth.addAuthStateListener(firebaseAuthListener);
        currentUser = mAuth.getCurrentUser();
    }

    private void getLayoutViews() {
        nothingToShow = findViewById(R.id.txtNothingToshow);
        progressBar = findViewById(R.id.progressBar);
        footerLayout = findViewById(R.id.footerLayout);

    }

    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
    }

    private void manageRecyclerView() {

        postsList = new ArrayList<>();
        mRecyclerView = findViewById(R.id.recycleView);
        mLayoutManager = new LinearLayoutManager(this);
        mAdapter = new MyPostsAdapter(postsList, MyPostsActivity.this);
        //   mRecyclerView.addOnScrollListener(showPages);

    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(firebaseAuthListener);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        mAuth.addAuthStateListener(firebaseAuthListener);
    }

    @Override
    protected void onStop() {

        if (firebaseAuthListener != null) {
            mAuth.removeAuthStateListener(firebaseAuthListener);
        }
        postsTable.removeEventListener(childEventListener);
        super.onStop();
    }

    private void addDrawer() {
        drawer = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navView);
        navBarHandler = new NavBarHandler(currentUser, this, mAuth, navigationView, drawer);
        navBarHandler.addDrawer(toolbar);

    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else
            super.onBackPressed();
    }

}

