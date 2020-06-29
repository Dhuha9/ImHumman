package com.example.imhumman;

import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

public class NavBarHandler {

    private FirebaseUser currentUser;
    private Activity mActivity;
    private DrawerLayout mDrawerLayout;
    private FirebaseAuth mAuth;
    private NavigationView mNavigationView;
    private NavigationView.OnNavigationItemSelectedListener navigationSelectListener = new NavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
            int id = menuItem.getItemId();
            switch (id) {

                case R.id.navHome:
                    if (currentUser == null) {
                        youShouldSignIn();
                    } else {
                        Intent homeIntent = new Intent(mActivity, AllPostsActivity.class);
                        // userProfileIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        mActivity.startActivity(homeIntent);
                    }
                    return true;

                case R.id.navUserProfile:
                    if (currentUser == null) {
                        youShouldSignIn();
                    } else {
                        Intent userProfileIntent = new Intent(mActivity, UserProfileActivity.class);
                        // userProfileIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        mActivity.startActivity(userProfileIntent);
                    }
                    return true;

                case R.id.navMyPost:
                    if (currentUser == null) {
                        youShouldSignIn();
                    } else {
                        Intent myPostsIntent = new Intent(mActivity, MyPostsActivity.class);
                        myPostsIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        mActivity.startActivity(myPostsIntent);
                    }
                    return true;

                case R.id.navSavedPosts:
                    if (currentUser == null) {
                        youShouldSignIn();
                    } else {
                        Intent savedPostsIntent = new Intent(mActivity, SavedPostsActivity.class);
                        savedPostsIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        mActivity.startActivity(savedPostsIntent);
                    }
                    return true;

                case R.id.navSignIn:

                    Intent signInIntent = new Intent(mActivity, SignInActivity.class);
                    signInIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    mActivity.startActivity(signInIntent);

                    return true;

                case R.id.navSignUp:

                    Intent signUpIntent = new Intent(mActivity, SignUpActivity.class);
                    signUpIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    mActivity.startActivity(signUpIntent);

                    return true;

                case R.id.aboutUs:
                    Intent aboutUsIntent = new Intent(mActivity, AboutUs.class);
                    aboutUsIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    mActivity.startActivity(aboutUsIntent);
                    return true;

                case R.id.aboutInitiative:
                    Intent aboutInitiativeIntent = new Intent(mActivity, AboutInitiative.class);
                    aboutInitiativeIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    mActivity.startActivity(aboutInitiativeIntent);
                    return true;

                case R.id.signOut:
                    FirebaseAuth.getInstance().signOut();
                    //  mAuth.addAuthStateListener(firebaseAuthListener);
                    Intent signOutIntent = new Intent(mActivity, SignInActivity.class);
                    signOutIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    mActivity.startActivity(signOutIntent);
                    return true;
            }

            return true;
        }
    };


 /*   private FirebaseAuth.AuthStateListener firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            mAuth = firebaseAuth;
            currentUser = mAuth.getCurrentUser();
            displayNavMenu();
        }
    };
*/


    NavBarHandler(FirebaseUser user, Activity activity, FirebaseAuth auth, NavigationView navigationView, DrawerLayout drawerLayout) {
        mActivity = activity;
        currentUser = user;
        mAuth = auth;
        mNavigationView = navigationView;
        mDrawerLayout = drawerLayout;
    }

    private void youShouldSignIn() {
        Toast.makeText(mActivity, "يرجى تسجيل الدخول اولا", Toast.LENGTH_SHORT).show();
        Intent signInIntent = new Intent(mActivity, SignInActivity.class);
        signInIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        mActivity.startActivity(signInIntent);
    }


    void addDrawer(Toolbar toolbar) {
        displayNavMenu();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(mActivity, mDrawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();


    }

    private void displayNavMenu() {

        Menu navMenu = mNavigationView.getMenu();

        if (currentUser != null) {

            navMenu.setGroupVisible(R.id.signedGroup, false);
            navMenu.setGroupVisible(R.id.unsignedGroup, true);
            navMenu.findItem(R.id.signOut).setVisible(true);

            View headerView = mNavigationView.getHeaderView(0);
            final ImageView userImg = headerView.findViewById(R.id.headerDrawerImage);
            final TextView headerDrawerUserName = headerView.findViewById(R.id.headerDrawerUserName);
            headerDrawerUserName.setText(currentUser.getDisplayName());
            Picasso.get()
                    .load(currentUser.getPhotoUrl())
                    .fit()
                    .centerCrop()
                    .into(userImg);

        } else {

            navMenu.setGroupVisible(R.id.unsignedGroup, false);
            navMenu.setGroupVisible(R.id.signedGroup, true);
            navMenu.findItem(R.id.signOut).setVisible(false);
        }

        mNavigationView.setNavigationItemSelectedListener(navigationSelectListener);

    }


}

