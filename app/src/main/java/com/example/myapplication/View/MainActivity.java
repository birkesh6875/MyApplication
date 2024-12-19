package com.example.myapplication.View;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.Attempt_Quiz_Section.Tests;
import com.example.myapplication.Create_Quiz.create_quiz_main;
import com.example.myapplication.Model.User;
import com.example.myapplication.R;
import com.example.myapplication.Auth_Controller.ResetPasswordActivity;
import com.example.myapplication.Auth_Controller.LoginActivity;
import com.example.myapplication.Results_section.ResultsAdmin;
import com.example.myapplication.AdminSection.ManageUsersActivity; // Import the ManageUsersActivity class
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseDatabase database;
    private FirebaseAuth auth;
    private DatabaseReference myRef;
    private TextView USer_email;
    private boolean isAdmin = false;
    private TextView userID;
    private CircleImageView imageView;
    private CircleImageView imageView1;
    private FloatingActionButton floatingActionButton;
    private NavigationView navigationView; // Declare navigationView as a class member variable

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Make sure you have the correct layout

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        imageView = findViewById(R.id.card1);
        floatingActionButton = findViewById(R.id.chatHead);
        auth = FirebaseAuth.getInstance();

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();

        // Initialize navigationView before calling checkForAdmin()
        navigationView = findViewById(R.id.nav_view);

        checkForAdmin(); // Check for admin privileges after initializing navigationView

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // Set up navigation header
        View header = navigationView.getHeaderView(0);
        imageView1 = header.findViewById(R.id.imageView);
        USer_email = header.findViewById(R.id.text_user_name);
        setTextOnUser();
        navigationView.setNavigationItemSelectedListener(this);
        userID = findViewById(R.id.text_user_card);
        setUserEmail();

        floatingActionButton.setOnClickListener(v -> {
            if (isAdmin) {
                startActivity(new Intent(MainActivity.this, create_quiz_main.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            } else {
                Toast.makeText(MainActivity.this, "You are not an admin", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void checkForAdmin() {
        myRef.child("admins").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child(Objects.requireNonNull(auth.getUid()))
                                .exists() &&
                                Objects.requireNonNull(dataSnapshot.child(auth.getUid())
                                        .getValue()).toString().equals("true")) {
                            isAdmin = true;
                            Toast.makeText(getApplicationContext(),
                                    "Здравствуйте, Администратор!",
                                    Toast.LENGTH_LONG).show();
                        }

                        // Обновляем видимость пункта меню "Управление пользователями"
                        Menu nav_Menu = navigationView.getMenu();
                        nav_Menu.findItem(R.id.nav_manage_users).setVisible(isAdmin);

                        nav_Menu.findItem(R.id.create_test).setVisible(isAdmin);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Обработка ошибок.
                    }
                });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(
                R.id.drawer_layout);
        if (drawer.isDrawerOpen(
                GravityCompat.START)) {
            drawer.closeDrawer(
                    GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu if you want to add items to the action bar
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Обработка нажатий на пункты меню

        int id = item.getItemId();

        if (id == R.id.nav_test) {
            // Обработка открытия страницы с тестами для прохождения
            if (isNetworkAvailable(MainActivity.this)) {
                startActivity(new Intent(MainActivity.this, Tests.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            } else {
                alertNoConnection();
            }
        } else if (id == R.id.nav_result) {
            // Обработка открытия результатов
            if (isNetworkAvailable(MainActivity.this)) {
                Intent intent = new Intent(MainActivity.this, ResultsAdmin.class);
                intent.putExtra("ISADMIN", isAdmin);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            } else {
                alertNoConnection();
            }
        } else if (id == R.id.nav_manage_users) {
            // Обработка управления пользователями
            if (isAdmin && isNetworkAvailable(MainActivity.this)) {
                startActivity(new Intent(MainActivity.this, ManageUsersActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            } else if (isNetworkAvailable(MainActivity.this)) {
                Toast.makeText(getApplicationContext(), "Вы не являетесь администратором!", Toast.LENGTH_SHORT).show();
            } else {
                alertNoConnection();
            }
        } else if (id == R.id.create_test) {
            // Обработка создания теста
            if (isAdmin && isNetworkAvailable(MainActivity.this)) {
                startActivity(new Intent(MainActivity.this, create_quiz_main.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            } else if (isNetworkAvailable(MainActivity.this)) {
                Toast.makeText(getApplicationContext(), "Вы не являетесь администратором!", Toast.LENGTH_SHORT).show();
            } else {
                alertNoConnection();
            }
        } else if (id == R.id.nav_respass) {
            // Обработка смены пароля
            if (isNetworkAvailable(MainActivity.this)) {
                startActivity(new Intent(MainActivity.this, ResetPasswordActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            } else {
                alertNoConnection();
            }
        } else if (id == R.id.nav_signout) {
            // Обработка выхода из аккаунта
            auth.signOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void setUserEmail() {
        DatabaseReference mDatabase;
        FirebaseAuth auth;
        mDatabase = FirebaseDatabase.getInstance().getReference();
        auth = FirebaseAuth.getInstance();
        mDatabase.child("users").child(Objects.requireNonNull(auth.getUid()))
                .addValueEventListener(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    User user = dataSnapshot.getValue(User.class);
                                    String temp = "Hey " + user.name + " what's up!";
                                    userID.setText(temp);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                // Handle possible errors.
                            }
                        });
    }

    public void setTextOnUser() {
        FirebaseUser usero = FirebaseAuth.getInstance().getCurrentUser();
        USer_email.setText(Objects.requireNonNull(usero).getEmail());
    }

    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = ((ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE));

        return connectivityManager.getActiveNetworkInfo() != null &&
                connectivityManager.getActiveNetworkInfo().isConnected();
    }

    public void alertNoConnection() {
        Dialog builder = new Dialog(this);
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (builder.getWindow() != null) {
            builder.getWindow().setBackgroundDrawable(
                    new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        builder.setOnDismissListener(dialogInterface -> {
            // Do nothing
        });

        ImageView imageView = new ImageView(this);
        imageView.setImageResource(R.drawable.nowifi);
        builder.addContentView(imageView, new RelativeLayout.LayoutParams(
                400,
                400));
        builder.show();
    }
}