package com.example.imhumman;

import android.content.Intent;
import android.os.Bundle;
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


public class SavedPostsActivity extends AppCompatActivity {

    DatabaseReference mDatabase;
    DatabaseReference postsTable;
    FirebaseUser currentUser;
    FirebaseAuth mAuth;
    DatabaseReference savedPostTable;
    RecyclerView mRecyclerView;
    PostsAdapter mAdapter;
    LinearLayoutManager mLayoutManager;
    DrawerLayout drawer;
    ArrayList<PostDataModel> postsList;
    ArrayList<PostDataModel> tempPostsList = new ArrayList<>();
    String lastId;
    ActionBarDrawerToggle toggle;
    Toolbar toolbar;
    NavigationView navigationView;
    LinearLayout footerLayout;
    TextView nothingToShow;
    ProgressBar progressBar, footerProgressBar;


    FirebaseAuth.AuthStateListener firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            mAuth = firebaseAuth;
            currentUser = mAuth.getCurrentUser();
            displayHeaderImageAndUserName();
        }
    };
    private NavigationView.OnNavigationItemSelectedListener navigationSelectListener = new NavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

            int id = menuItem.getItemId();
            switch (id) {
                case R.id.signOut:
                    FirebaseAuth.getInstance().signOut();
                    mAuth.addAuthStateListener(firebaseAuthListener);
                    Intent signOutIntent = new Intent(SavedPostsActivity.this, SignInActivity.class);
                    startActivity(signOutIntent);
                    return true;
                case R.id.navHome:
                    if (currentUser == null) {
                        youShouldSignIn();
                    } else {
                        Intent homeIntent = new Intent(SavedPostsActivity.this, AllPostsActivity.class);
                        // userProfileIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        startActivity(homeIntent);
                    }
                    return true;

                case R.id.navUserProfile:
                    if (currentUser == null) {
                        youShouldSignIn();
                    } else {
                        Intent userProfileIntent = new Intent(SavedPostsActivity.this, UserProfileActivity.class);
                        startActivity(userProfileIntent);
                    }
                    return true;
                case R.id.navMyPost:
                    if (currentUser == null) {
                        youShouldSignIn();
                    } else {
                        Intent myPostsIntent = new Intent(SavedPostsActivity.this, MyPostsActivity.class);
                        startActivity(myPostsIntent);
                    }
                    return true;

                case R.id.navSavedPosts:
                    if (currentUser == null) {
                        youShouldSignIn();
                    } else {
                        Intent savedPostsIntent = new Intent(SavedPostsActivity.this, SavedPostsActivity.class);
                        startActivity(savedPostsIntent);
                    }
                    return true;
            }

            return true;
        }
    };


    private void youShouldSignIn() {
        Toast.makeText(SavedPostsActivity.this, "يرجى تسجيل الدخول اولا", Toast.LENGTH_SHORT).show();
        Intent signInIntent = new Intent(SavedPostsActivity.this, SignInActivity.class);
        startActivity(signInIntent);
    }


    public void loadSavedPosts() {
        postsList.clear();
        Query query = savedPostTable.child(FirebaseAuth.getInstance().getCurrentUser().getUid());

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

        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.getValue() != null) {
                    String postId = dataSnapshot.getValue().toString();
                    postsTable.child(postId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
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
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

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
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.all_posts);
        setToolbar();
        nothingToShow = findViewById(R.id.txtNothingToshow);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        postsTable = mDatabase.child("posts");
        savedPostTable = mDatabase.child("savedPost");
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        progressBar = findViewById(R.id.progressBar);

        addDrawer();
        mAuth.addAuthStateListener(firebaseAuthListener);
        navigationView = findViewById(R.id.navView);
        navigationView.setNavigationItemSelectedListener(navigationSelectListener);
        footerLayout = findViewById(R.id.footerLayout);

        manageRecyclerView();

        lastId = "";
        loadSavedPosts();


    }

    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
    }

    private void manageRecyclerView() {

        postsList = new ArrayList<>();
        mRecyclerView = findViewById(R.id.recycleView);
        mLayoutManager = new LinearLayoutManager(this);
        mAdapter = new PostsAdapter(postsList, SavedPostsActivity.this);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);


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
        super.onStop();
        if (firebaseAuthListener != null) {
            mAuth.removeAuthStateListener(firebaseAuthListener);
        }
    }

    private void addDrawer() {
        toolbar = findViewById(R.id.toolBar);
        drawer = findViewById(R.id.drawerLayout);
        displayHeaderImageAndUserName();
        toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

    }

    private void displayHeaderImageAndUserName() {
        if (currentUser != null) {
            navigationView = findViewById(R.id.navView);
            View headerView = navigationView.getHeaderView(0);
            final ImageView userImage = headerView.findViewById(R.id.headerDrawerImage);
            final TextView headerDrawerUserName = headerView.findViewById(R.id.headerDrawerUserName);
            headerDrawerUserName.setText(currentUser.getDisplayName());
            Picasso.get()
                    .load(currentUser.getPhotoUrl())
                    .fit()
                    .centerCrop()
                    .into(userImage);
        }

    }


    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else
            super.onBackPressed();
    }

}



