package com.example.imhumman;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
import com.google.android.gms.tasks.OnSuccessListener;
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
    NavBarHandler navBarHandler;

    FirebaseAuth.AuthStateListener firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            mAuth = firebaseAuth;
            currentUser = mAuth.getCurrentUser();
            Toast.makeText(AllPostsActivity.this, "user " + currentUser, Toast.LENGTH_SHORT).show();
            addDrawer();
        }
    };

    ChildEventListener childEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            Toast.makeText(AllPostsActivity.this, "sss" + s, Toast.LENGTH_LONG).show();
            if (dataSnapshot.getValue() != null) {
                PostDataModel postData = dataSnapshot.getValue(PostDataModel.class);
                if (postData != null) {
                    postData.setPostId(dataSnapshot.getKey());
                    tempPostsList.add(postData);
                    if (footerProgressBar != null) {
                        footerProgressBar.setVisibility(View.GONE);
                    }
                    String post = tempPostsList.get(0).getContent();
                    Toast.makeText(AllPostsActivity.this, "post " + post, Toast.LENGTH_LONG).show();
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

            } else {
                Toast.makeText(AllPostsActivity.this, "null post", Toast.LENGTH_SHORT).show();
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
                currentUser.reload().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if (currentUser.isEmailVerified()) {
                            Intent intent = new Intent(AllPostsActivity.this, AddPostActivity.class);
                            //   intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                            startActivity(intent);
                        } else {
                            Toast.makeText(AllPostsActivity.this, "يجب مراجعة البريد الالكتروني لاتمام عملية تسجيل الدخول", Toast.LENGTH_LONG).show();
                        }
                    }
                });


            } else {
                Intent intent = new Intent(AllPostsActivity.this, SignInActivity.class);
                startActivity(intent);
            }
        }
    };

    private RecyclerView.OnScrollListener showPages = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (dy > 0) {
                Toast.makeText(AllPostsActivity.this, "scroll", Toast.LENGTH_SHORT).show();
                totalItemCount = mLayoutManager.getItemCount();
                int lastVisibleItemPosition = mLayoutManager.findLastVisibleItemPosition();
                int f = postsPerPage + lastVisibleItemPosition;
                Toast.makeText(AllPostsActivity.this, "num post2 " + totalItemCount + " f " + f + " loading " + isLoading, Toast.LENGTH_SHORT).show();

                if (!isLoading && totalItemCount <= (postsPerPage + lastVisibleItemPosition)) {

                    footerProgressBar.setVisibility(View.VISIBLE);
                    loadMorePagesOfPosts(lastId);
                    isLoading = true;

                }
            }
        }
    };


    public void loadFirstPageOfPosts() {
        first = true;
        postsTable.limitToLast(4).addChildEventListener(childEventListener);
    }

    public void loadMorePagesOfPosts(String postId) {
        first = false;
        postsTable.orderByKey().endAt(postId).limitToLast(4).addChildEventListener(childEventListener);

    }

    ValueEventListener valueEventListener = new ValueEventListener() {
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
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.all_posts);
        setToolbar();
        addDrawer();
        getLayoutViews();
        setFirebaseVariables();
        checkUserToStart();
        lastId = "";
    }

    private void getLayoutViews() {
        nothingToShow = findViewById(R.id.txtNothingToshow);
        FBtn = findViewById(R.id.floatingActionButton);
        FBtn.show();
        FBtn.setOnClickListener(FBtnClick);
        footerProgressBar = findViewById(R.id.footerProgressBar);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void setFirebaseVariables() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        postsTable = mDatabase.child("posts");
        savedPostTable = mDatabase.child("savedPost");
        mAuth = FirebaseAuth.getInstance();
        mAuth.addAuthStateListener(firebaseAuthListener);
        currentUser = mAuth.getCurrentUser();
    }


    private void checkUserToStart() {
        if (currentUser == null) {
            manageRecyclerView();
        } else {
            getSavedPosts();
        }
    }

    private void setToolbar() {
        toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle(R.string.main_page_title);
    }

    private void getSavedPosts() {
        savedPostsList.clear();

        savedPostTable.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(valueEventListener);
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
        if (firebaseAuthListener != null) {
            mAuth.removeAuthStateListener(firebaseAuthListener);

        }
        postsTable.removeEventListener(childEventListener);
        savedPostTable.removeEventListener(valueEventListener);
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
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

