package com.example.imhumman;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;

public class AllPostsActivity extends AppCompatActivity {

    DatabaseReference mDatabase;
    DatabaseReference postsTable;
    DatabaseReference savedPostTable;
    FirebaseUser currentUser;
    FirebaseAuth mAuth;
    FloatingActionButton FBtn;
    RecyclerView mRecyclerView;
    PostsAdapter mAdapter;
    LinearLayoutManager mLayoutManager;
    int totalItemCount = 0;
    boolean isLoading = false;
    DrawerLayout drawer;
    ArrayList<PostDataModel> postsList;
    ArrayList<PostDataModel> tempPostsList = new ArrayList<>();
    ArrayList<String> savedPostsList = new ArrayList<>();
    int postsPerPage = 4;
    Boolean first = false;
    String lastId;
    ActionBarDrawerToggle toggle;
    Toolbar toolbar;
    NavigationView navigationView;
    ProgressBar progressBar, footerProgressBar;
    TextView nothingToShow;


    FirebaseAuth.AuthStateListener firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            mAuth = firebaseAuth;
            currentUser = mAuth.getCurrentUser();
            displayHeaderImageAndUserName();
        }
    };
    ChildEventListener childEventListener = new ChildEventListener() {

        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            //Toast.makeText(AllPostsActivity.this, "llss "+postsList, Toast.LENGTH_LONG).show();
            if (dataSnapshot.getValue() != null) {
                PostDataModel postData = dataSnapshot.getValue(PostDataModel.class);
                if (postData != null) {
                    postData.setPostId(dataSnapshot.getKey());
                    tempPostsList.add(postData);
                    if (footerProgressBar != null) {
                        footerProgressBar.setVisibility(View.GONE);
                    }
                    if (tempPostsList.size() == postsPerPage) {
                        lastId = tempPostsList.get(0).getPostId();
                        Collections.reverse(tempPostsList);
                        if (!first) {
                            tempPostsList.remove(0);
                        }


                        progressBar.setVisibility(View.GONE);
                        nothingToShow.setVisibility(View.GONE);
                        postsList.addAll(tempPostsList);
                        isLoading = false;
                        mAdapter.notifyDataSetChanged();
                        tempPostsList.clear();

                    }
                }
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
    private View.OnClickListener FBtnClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mAuth.addAuthStateListener(firebaseAuthListener);
            if (currentUser != null) {

                Intent intent = new Intent(AllPostsActivity.this, AddPostActivity.class);
                //   intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
            } else {
                Intent intent = new Intent(AllPostsActivity.this, SignInActivity.class);
                startActivity(intent);
            }
        }
    };
    private NavigationView.OnNavigationItemSelectedListener navigationSelectListener = new NavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            drawer.closeDrawer(GravityCompat.START);
            int id = menuItem.getItemId();
            switch (id) {
                case R.id.signOut:
                    FirebaseAuth.getInstance().signOut();
                    mAuth.addAuthStateListener(firebaseAuthListener);
                    Intent signOutIntent = new Intent(AllPostsActivity.this, SignInActivity.class);
                    signOutIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivity(signOutIntent);
                    return true;
                case R.id.navHome:
                    if (currentUser == null) {
                        youShouldSignIn();
                    } else {
                        Intent homeIntent = new Intent(AllPostsActivity.this, AllPostsActivity.class);
                        // userProfileIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        startActivity(homeIntent);
                    }
                    return true;
                case R.id.navUserProfile:
                    if (currentUser == null) {
                        youShouldSignIn();
                    } else {
                        Intent userProfileIntent = new Intent(AllPostsActivity.this, UserProfileActivity.class);
                        // userProfileIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        startActivity(userProfileIntent);
                    }
                    return true;
                case R.id.navMyPost:
                    if (currentUser == null) {
                        youShouldSignIn();
                    } else {
                        Intent myPostsIntent = new Intent(AllPostsActivity.this, MyPostsActivity.class);
                        myPostsIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        startActivity(myPostsIntent);
                    }
                    return true;

                case R.id.navSavedPosts:
                    if (currentUser == null) {
                        youShouldSignIn();
                    } else {
                        Intent savedPostsIntent = new Intent(AllPostsActivity.this, SavedPostsActivity.class);
                        savedPostsIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        startActivity(savedPostsIntent);
                    }
                    return true;
            }

            return true;
        }
    };
    private RecyclerView.OnScrollListener showPages = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (dy > 0) {
                //Toast.makeText(AllPostsActivity.this, "scroll", Toast.LENGTH_SHORT).show();
                totalItemCount = mLayoutManager.getItemCount();
                int lastVisibleItemPosition = mLayoutManager.findLastVisibleItemPosition();
                if (!isLoading && totalItemCount <= (postsPerPage + lastVisibleItemPosition)) {

                    footerProgressBar.setVisibility(View.VISIBLE);
                    loadMorePagesOfPosts(lastId);
                    isLoading = true;

                }
            }
        }
    };

    private void youShouldSignIn() {
        Toast.makeText(AllPostsActivity.this, "يرجى تسجيل الدخول اولا", Toast.LENGTH_SHORT).show();
        Intent signInIntent = new Intent(AllPostsActivity.this, SignInActivity.class);
        signInIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(signInIntent);
    }

    public void loadFirstPageOfPosts() {
        first = true;
        postsTable.limitToLast(4).addChildEventListener(childEventListener);
    }

    public void loadMorePagesOfPosts(String postId) {
        first = false;
        postsTable.orderByKey().endAt(postId).limitToLast(4).addChildEventListener(childEventListener);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.all_posts);
        setToolbar();
        addDrawer();
        nothingToShow = findViewById(R.id.txtNothingToshow);
        navigationView = findViewById(R.id.navView);
        navigationView.setNavigationItemSelectedListener(navigationSelectListener);
        FBtn = findViewById(R.id.floatingActionButton);
        FBtn.show();
        FBtn.setOnClickListener(FBtnClick);
        footerProgressBar = findViewById(R.id.footerProgressBar);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        postsTable = mDatabase.child("posts");
        savedPostTable = mDatabase.child("savedPost");
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        checkUserToStart();

        mAuth.addAuthStateListener(firebaseAuthListener);
        lastId = "";

    }

    private void setToolbar() {
        toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
    }

    private void checkUserToStart() {
        if (currentUser == null) {
            manageRecyclerView();
        } else {
            getSavedPosts();
        }
    }

    private void getSavedPosts() {
        savedPostsList.clear();

        savedPostTable.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (dataSnapshot.getValue() != null) {
                        savedPostsList.add(ds.getValue().toString());
                    }
                }
                int dataCount = (int) dataSnapshot.getChildrenCount();
                if (savedPostsList.size() == dataCount) {
                    manageRecyclerView();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void manageRecyclerView() {

        postsList = new ArrayList<>();
        mRecyclerView = findViewById(R.id.recycleView);
        mLayoutManager = new LinearLayoutManager(this);
        mAdapter = new PostsAdapter(postsList, AllPostsActivity.this, savedPostsList, currentUser);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(showPages);
        loadFirstPageOfPosts();

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

        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (firebaseAuthListener != null) {
            mAuth.removeAuthStateListener(firebaseAuthListener);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void addDrawer() {
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
            final ImageView userImg = headerView.findViewById(R.id.headerDrawerImage);
            final TextView headerDrawerUserName = headerView.findViewById(R.id.headerDrawerUserName);
            headerDrawerUserName.setText(currentUser.getDisplayName());
            Picasso.get()
                    .load(currentUser.getPhotoUrl())
                    .fit()
                    .centerCrop()
                    .into(userImg);
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

