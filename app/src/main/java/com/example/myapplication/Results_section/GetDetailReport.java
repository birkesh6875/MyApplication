package com.example.myapplication.Results_section;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.myapplication.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class GetDetailReport extends AppCompatActivity {

    CircleImageView circleImageView;
    TextView textView1;
    TextView textView2;
    TextView textView3;
    TextView textView4;
    TextView textView5;
    private FirebaseDatabase database;
    private FirebaseAuth auth;
    private DatabaseReference myRef;
    final long ONE_MEGABYTE = 1024 * 1024;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.complete_student_details);
        Toolbar toolbar =  findViewById(R.id.toolbartst);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.black));
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        circleImageView = findViewById(R.id.USERIMAGE);
        textView1 = findViewById(R.id.StudentID);
        textView2 = findViewById(R.id.SemID);
        textView3 = findViewById(R.id.Branchid);
        textView4 = findViewById(R.id.SectionID);
        textView5 = findViewById(R.id.this_Section);
        String temp = getIntent().getStringExtra("USERID");



        textView1.setText(getIntent().getStringExtra("DetailID"));
        textView2.setText(getIntent().getStringExtra("DetailBranch"));
        textView3.setText(getIntent().getStringExtra("DetailSem"));
        textView4.setText(getIntent().getStringExtra("DetailSec"));
        String Temp = getIntent().getStringExtra("TestNAME") + " results is: " +
                getIntent().getStringExtra("Marks");
        textView5.setText(Temp);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
