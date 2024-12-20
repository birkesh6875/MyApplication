package com.example.myapplication.Results_section;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.Model.User;
import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;

public class ResultsAdminDetailed extends AppCompatActivity {

    private DatabaseReference myRef;
    private ProgressBar progressBar;
    private CardView button;
    private ResultsAdminDetailed.TestAdapter testAdapter;
    ArrayList<TestResults> result = new ArrayList<>();
    private String testName;
    private int lastPos = -1;
    public boolean isAdmin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tests);
        button = findViewById(R.id.download_card);
        isAdmin = getIntent().getBooleanExtra("ISAdmin", false);
        testName = getIntent().getStringExtra("test");

        if (!isAdmin) {
            setTitle("Result");
        }

        if (isAdmin) {
            button.setVisibility(View.VISIBLE);
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.black));
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        progressBar = findViewById(R.id.loader1);
        progressBar.setVisibility(View.VISIBLE);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference();

        ListView listView = findViewById(R.id.test_listview);
        testAdapter = new ResultsAdminDetailed.TestAdapter(ResultsAdminDetailed.this, result);
        listView.setAdapter(testAdapter);
        getSupportActionBar().setTitle(testName);

        getResults();

        button.setOnClickListener(v -> {
            // check if available and not read only
            if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
                Log.e("TAG", "Storage not available or read only");
                return;
            }
            saveToExcel(testName.concat(".xls"));
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void saveToExcel(String fileName) {
        getResults();
        //New Workbook
        HSSFWorkbook hSSFWorkbook = new HSSFWorkbook();
        Sheet createSheet = hSSFWorkbook.createSheet(this.testName);
        //create row
        Row createRow = createSheet.createRow(0);
        createRow.createCell(0).setCellValue("ID");
        createRow.createCell(1).setCellValue("Semester");
        createRow.createCell(2).setCellValue("Branch");
        createRow.createCell(3).setCellValue("Section");
        createRow.createCell(4).setCellValue("Score");

        int i = 0;
        while (i < this.testAdapter.dataList.size()) {
            int i2 = i + 1;
            Row createRow2 = createSheet.createRow(i2);
            createRow2.createCell(0).setCellValue(this.testAdapter.dataList.get(i).user.name);
            createRow2.createCell(1).setCellValue(this.testAdapter.dataList.get(i).score);
            i = i2;
        }

        //save the file under download folder of android
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),fileName);

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            hSSFWorkbook.write(fileOutputStream);
            StringBuilder sb = new StringBuilder();
            sb.append("Writing file");
            sb.append(fileName);
            Log.w("FileSaver",sb.toString());
            fileOutputStream.close();
            Toast.makeText(ResultsAdminDetailed.this, "File is saved under Download folder", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(ResultsAdminDetailed.this, "Can't be saved", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public boolean isExternalStorageReadOnly() {
        return "mounted_ro".equals(Environment.getExternalStorageState());
    }

    public boolean isExternalStorageAvailable() {
        return "mounted".equals(Environment.getExternalStorageState());
    }

    public void getResults() {
        myRef.child("Results").child(testName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                result.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    TestResults t = new TestResults();
                    t.userID = snapshot.getKey();
                    t.score = Objects.requireNonNull(snapshot.getValue()).toString();
                    result.add(t);
                }

                Collections.sort(result, (o1, o2) -> o2.score.compareTo(o1.score));

                getDetails();
                Log.e("The read success: ", "su" + result.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                Log.e("The read failed: ", databaseError.getMessage());
            }
        });
    }

    private void getDetails() {
        myRef.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (int i = 0; i < result.size(); i++) {
                    if (dataSnapshot.child(result.get(i).userID).exists())
                        result.get(i).user = dataSnapshot.child(result.get(i).userID).getValue(User.class);
                    else {
                        User user1 = new User();
                        user1.setName("Unknown");
                        result.get(i).user = user1;
                    }
                }

                testAdapter.dataList = result;
                testAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    class TestResults {
        public String userID, score;
        public User user;
    }

    class TestAdapter extends ArrayAdapter<TestResults> {
        private Context mContext;
        ArrayList<TestResults> dataList;

        public TestAdapter(Context context, ArrayList<TestResults> list) {
            super(context, 0, list);
            mContext = context;
            dataList = list;
        }

        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View listItem = convertView;
            if (listItem == null)
                listItem = LayoutInflater.from(mContext).inflate(R.layout.test_item, parent, false);

            ((ImageView) listItem.findViewById(R.id.item_imageView))
                    .setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ranking));

            if (dataList.get(position).user != null)
                ((TextView) listItem.findViewById(R.id.item_textView)).setText(dataList.get(position).user.name);
            else {
                ((TextView) listItem.findViewById(R.id.item_textView)).setText("Details not added yet");
            }

            Animation animation = AnimationUtils.loadAnimation(getContext(), (position > lastPos) ? R.anim.up_from_bottom : R.anim.down_from_top);
            (listItem).startAnimation(animation);
            lastPos = position;

            if (isAdmin) {
                ((TextView) listItem.findViewById(R.id.item_textView)).setOnClickListener(v -> {
                    if (dataList.get(position).user.name != null) {
                        Intent intent = new Intent(ResultsAdminDetailed.this, GetDetailReport.class);
                        intent.putExtra("USERID", dataList.get(position).userID);
                        intent.putExtra("DetailID", dataList.get(position).user.name);
                        intent.putExtra("TestNAME", testName);
                        intent.putExtra("Marks", dataList.get(position).score);
                        startActivity(intent);
                    }
                });
            }

            ((Button) listItem.findViewById(R.id.item_button)).setText(dataList.get(position).score);
            return listItem;
        }
    }
}
