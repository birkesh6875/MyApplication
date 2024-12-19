package com.example.myapplication.AdminSection;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;

public class ManageUsersActivity extends AppCompatActivity {
    private ListView userListView;
    private FirebaseDatabase database;
    private DatabaseReference usersRef;
    private DatabaseReference adminsRef; // Добавляем ссылку на администраторов
    private ArrayAdapter<String> adapter;
    private ArrayList<String> userNames;
    private ArrayList<String> userIds;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_manage_users);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Управление пользователями");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userListView = findViewById(R.id.user_list_view);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("users");
        adminsRef = database.getReference("admins"); // Инициализируем ссылку

        // Проверяем, является ли пользователь администратором
        checkForAdmin();
    }

    private void checkForAdmin() {
        adminsRef.child(auth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean isAdminValue = snapshot.getValue(Boolean.class);
                if (isAdminValue != null && isAdminValue) {
                    loadUsers();
                } else {
                    Toast.makeText(ManageUsersActivity.this, "Вы не имеете доступа к этой странице.", Toast.LENGTH_SHORT).show();
                    finish(); // Закрываем активность
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManageUsersActivity.this, "Ошибка базы данных.", Toast.LENGTH_SHORT).show();
                finish(); // Закрываем активность
            }
        });
    }

    private void loadUsers() {
        // Получаем список администраторов
        adminsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot adminsSnapshot) {
                ArrayList<String> adminIds = new ArrayList<>();
                for (DataSnapshot adminSnapshot : adminsSnapshot.getChildren()) {
                    if (adminSnapshot.getValue(Boolean.class)) {
                        adminIds.add(adminSnapshot.getKey());
                    }
                }

                // Теперь загружаем пользователей, исключая администраторов
                usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot usersSnapshot) {
                        userNames = new ArrayList<>();
                        userIds = new ArrayList<>();
                        for (DataSnapshot userSnapshot : usersSnapshot.getChildren()) {
                            String userId = userSnapshot.getKey();
                            // Пропускаем администраторов
                            if (adminIds.contains(userId)) {
                                continue;
                            }
                            User user = userSnapshot.getValue(User.class);
                            if (user != null) {
                                userIds.add(userId);
                                userNames.add(user.getName() + " (" + userId + ")");
                            }
                        }

                        adapter = new ArrayAdapter<>(ManageUsersActivity.this, android.R.layout.simple_list_item_1, userNames);
                        userListView.setAdapter(adapter);

                        userListView.setOnItemClickListener((parent, view, position, id) -> showDeleteUserDialog(position));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(ManageUsersActivity.this, "Ошибка загрузки пользователей.", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ManageUsersActivity.this, "Ошибка загрузки администраторов.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteUserDialog(int position) {
        String userName = userNames.get(position);
        String userId = userIds.get(position);

        new AlertDialog.Builder(this)
                .setTitle("Удалить пользователя")
                .setMessage("Вы уверены, что хотите удалить пользователя: " + userName + "?")
                .setPositiveButton("Удалить", (dialog, which) -> deleteUser(userId, position))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void deleteUser(String userId, int position) {
        // Удаляем пользователя из узла 'users'
        usersRef.child(userId).removeValue();

        // Удаляем результаты пользователя
        DatabaseReference resultsRef = FirebaseDatabase.getInstance().getReference("Results");
        resultsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot resultsSnapshot) {
                for (DataSnapshot testSnapshot : resultsSnapshot.getChildren()) {
                    testSnapshot.child(userId).getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Игнорируем для данного примера
            }
        });

        // Удаляем пользователя из списка
        userNames.remove(position);
        userIds.remove(position);
        adapter.notifyDataSetChanged();

        Toast.makeText(this, "Пользователь удален", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}