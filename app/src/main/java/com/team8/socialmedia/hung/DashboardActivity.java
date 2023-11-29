package com.team8.socialmedia.hung;

import static com.team8.socialmedia.R.id.nav_home;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.team8.socialmedia.MainActivity;
import com.team8.socialmedia.R;
import com.team8.socialmedia.kone.ChatListFragment;
import com.team8.socialmedia.vu.HomeFragment;
import com.team8.socialmedia.vu.ProfileFragment;
import com.team8.socialmedia.vu.UsersFragment;
import com.team8.socialmedia.vu.notifications.Token;

public class DashboardActivity extends AppCompatActivity {
    FirebaseAuth firebaseAuth;
    ActionBar actionBar;

    String mUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        //Actionbar and its title
        actionBar = getSupportActionBar();
        actionBar.setTitle("Profile");

        //initialize the firebaseAuth instance
        firebaseAuth = FirebaseAuth.getInstance();

        BottomNavigationView navigationView = findViewById(R.id.navigation);
        navigationView.setOnNavigationItemSelectedListener(selectedListener);

        //default frame
        actionBar.setTitle("Home");
        HomeFragment fragment1 = new HomeFragment();
        FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
        ft1.replace(R.id.container, fragment1, "");
        ft1.commit();

        checkUserStatus();

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        // Token retrieved successfully
                        String token = task.getResult();
                        updateToken(token);
                    } else {
                    }
                });
    }

    @Override
    protected void onResume() {
        checkUserStatus();
        super.onResume();
    }

    public void updateToken(String token)
    {
        if (mUID != null) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tokens");
            Token mToken = new Token(token);
            ref.child(mUID).setValue(mToken);
        } else {
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener selectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @SuppressLint("NonConstantResourceId")
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            if (menuItem.getItemId() == R.id.nav_home) {
                actionBar.setTitle("Home");
                HomeFragment fragment1 = new HomeFragment();
                FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
                ft1.replace(R.id.container, fragment1, "");
                ft1.commit();
                return true;
            }
            if (menuItem.getItemId() == R.id.nav_profile) {
                actionBar.setTitle("Profile");
                ProfileFragment fragment2 = new ProfileFragment();
                FragmentTransaction ft2 = getSupportFragmentManager().beginTransaction();
                ft2.replace(R.id.container, fragment2, "");
                ft2.commit();
                return true;
            }
            if (menuItem.getItemId() == R.id.nav_users) {
                actionBar.setTitle("Users");
                UsersFragment fragment3 = new UsersFragment();
                FragmentTransaction ft3 = getSupportFragmentManager().beginTransaction();
                ft3.replace(R.id.container, fragment3, "");
                ft3.commit();
                return true;
            }
            if (menuItem.getItemId() == R.id.nav_chat) {
                actionBar.setTitle("Chats");
                ChatListFragment fragment4 = new ChatListFragment();
                FragmentTransaction ft4 = getSupportFragmentManager().beginTransaction();
                ft4.replace(R.id.container, fragment4, "");
                ft4.commit();
                return true;
            }
            return false;
        }
    };

    private void checkUserStatus() {
        //get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            //user is signed in stay here
            //set email of logged-in user

            mUID = user.getUid();
            SharedPreferences sp = getSharedPreferences("SP_USER",MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("Current_USERID",mUID);
            editor.apply();
        } else {
            //user not signed in, go to main activity
            startActivity(new Intent(DashboardActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onStart() {
        //check on start of app
        checkUserStatus();
        super.onStart();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}