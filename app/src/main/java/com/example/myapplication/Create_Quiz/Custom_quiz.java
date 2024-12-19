package com.example.myapplication.Create_Quiz;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.Model.Question;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Map;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import androidx.annotation.NonNull;

public class Custom_quiz extends AppCompatActivity {

    EditText question;
    EditText aText;
    EditText bText;
    EditText cText;
    EditText dText;
    RadioButton aRadio;
    RadioButton bRadio;
    RadioButton cRadio;
    RadioButton dRadio;

    RadioGroup optionsGroup;

    int currentQuestion = 1;
    int previousQuestion = 1;
    TextView questionNumber;

    ArrayList<Question> ques;
    JSONArray jsonArray;
    String selectedOption = "";

    String fileName = "file";
    private FirebaseDatabase database;
    private FirebaseAuth auth;
    private DatabaseReference myRef;
    CardView fab, f2, fl;

    Button addOptionButton;
    Button removeOptionButton;

    int optionCount = 2;

    private LinearLayout optionCLayout, optionDLayout;

    // Новые переменные для выбора изображения
    private Button selectImageButton;
    private ImageView questionImageView;
    private Uri selectedImageUri = null;
    private StorageReference storageReference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        jsonArray = new JSONArray();
        setContentView(R.layout.activity_create_quiz);

        question = findViewById(R.id.questionView);
        aText = findViewById(R.id.aText);
        bText = findViewById(R.id.bText);
        cText = findViewById(R.id.cText);
        dText = findViewById(R.id.dText);
        questionNumber = findViewById(R.id.questionNumber);
        aRadio = findViewById(R.id.aRadio);
        bRadio = findViewById(R.id.bRadio);
        cRadio = findViewById(R.id.cRadio);
        dRadio = findViewById(R.id.dRadio);
        optionsGroup = findViewById(R.id.optionsGroup);

        addOptionButton = findViewById(R.id.addOptionButton);
        removeOptionButton = findViewById(R.id.removeOptionButton);

        optionCLayout = findViewById(R.id.optionCLayout);
        optionDLayout = findViewById(R.id.optionDLayout);

        optionCLayout.setVisibility(View.GONE);
        optionDLayout.setVisibility(View.GONE);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        storageReference = FirebaseStorage.getInstance().getReference();

        selectedOption = "";
        currentQuestion = 1;
        setListeners();

        ques = new ArrayList<>();

        fab = findViewById(R.id.nextfab);
        fl = findViewById(R.id.fab2); // кнопка сохранения
        f2 = findViewById(R.id.pre_card);

        f2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (previousQuestion > 1) {
                    previousQuestion--;
                    setAllData(previousQuestion);
                }
                if (previousQuestion == 1)
                    f2.setVisibility(View.INVISIBLE);
                Toast.makeText(Custom_quiz.this, String.valueOf(previousQuestion), Toast.LENGTH_SHORT).show();
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Сохраняем текущий вопрос
                boolean cont = getEnteredQuestionsValue();
                if (cont) {
                    previousQuestion++;
                    currentQuestion++;
                    Toast.makeText(Custom_quiz.this, "Вопрос " + currentQuestion, Toast.LENGTH_SHORT).show();
                    questionNumber.setText(String.valueOf(currentQuestion));
                    clearAllData();
                    f2.setVisibility(View.VISIBLE);
                    resetOptionCount();
                }
            }
        });

        fl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Сохраняем текущий вопрос, если он ещё не сохранён
                boolean cont = getEnteredQuestionsValue();

                if (cont || jsonArray.length() > 0) {
                    final JSONObject tempObject = new JSONObject();
                    // Получаем вид из dialog_custom.xml
                    LayoutInflater li = LayoutInflater.from(Custom_quiz.this);
                    View promptsView = li.inflate(R.layout.dialog_custom, null);
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Custom_quiz.this);

                    // Устанавливаем dialog_custom.xml для AlertDialog Builder
                    alertDialogBuilder.setView(promptsView);
                    final EditText userInput = promptsView.findViewById(R.id.editTextDialogUserInput);
                    final EditText userTime = promptsView.findViewById(R.id.editTextDialogUserInput1);

                    // Опционально устанавливаем ограничение по времени
                    userTime.setHint("Введите время в минутах (опционально)");

                    // Настраиваем диалоговое окно
                    alertDialogBuilder
                            .setCancelable(false)
                            .setPositiveButton("OK", null) // Установим null, чтобы переопределить обработчик позже
                            .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

                    // Создаём AlertDialog
                    final AlertDialog alertDialog = alertDialogBuilder.create();
                    // Отображаем его
                    alertDialog.show();

                    // Переопределяем обработчик кнопки "OK"
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String testName = userInput.getText().toString().trim();
                            if (TextUtils.isEmpty(testName)) {
                                userInput.setError("Имя теста не может быть пустым");
                                return;
                            }

                            // Проверяем, существует ли тест с таким названием
                            myRef.child("tests").child(testName).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        // Тест с таким названием уже существует
                                        Toast.makeText(Custom_quiz.this, "Тест с таким названием уже существует. Пожалуйста, выберите другое название.", Toast.LENGTH_LONG).show();
                                    } else {
                                        // Тест с таким названием не существует, можно сохранить
                                        String timeStr = userTime.getText().toString().trim();
                                        try {
                                            tempObject.put("Questions", jsonArray);
                                            if (!TextUtils.isEmpty(timeStr)) {
                                                int time = Integer.parseInt(timeStr);
                                                tempObject.put("Time", time);
                                            } else {
                                                tempObject.put("Time", 0); // Без ограничения по времени
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        final String jsonStr = tempObject.toString();
                                        Map<String, Object> result = new Gson().fromJson(jsonStr, Map.class);
                                        myRef.child("tests").child(testName).setValue(result);
                                        Toast.makeText(getApplicationContext(), "Тест успешно сохранён", Toast.LENGTH_SHORT).show();
                                        alertDialog.dismiss();
                                        finish(); // Закрываем активность после сохранения
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    // Обработка ошибки
                                    Toast.makeText(Custom_quiz.this, "Ошибка при проверке названия теста", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Некорректный формат вопроса", Toast.LENGTH_SHORT).show();
                }
            }
        });

        addOptionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                increaseOptionCount();
            }
        });

        removeOptionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                decreaseOptionCount();
            }
        });

        // Устанавливаем начальное количество вариантов
        resetOptionCount();

        // Инициализируем элементы для выбора изображения
        selectImageButton = findViewById(R.id.selectImageButton);
        questionImageView = findViewById(R.id.questionImageView);

        selectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Выберите изображение"), 1);
            }
        });
    }

    private void resetOptionCount() {
        optionCount = 2;
        optionCLayout.setVisibility(View.GONE);
        optionDLayout.setVisibility(View.GONE);

        cText.setText("");
        dText.setText("");
        cRadio.setChecked(false);
        dRadio.setChecked(false);
        checkOptionButtons();
    }

    private void increaseOptionCount() {
        if (optionCount < 4) {
            optionCount++;
            if (optionCount == 3) {
                optionCLayout.setVisibility(View.VISIBLE);
                cText.setVisibility(View.VISIBLE);
                cRadio.setVisibility(View.VISIBLE);
            } else if (optionCount == 4) {
                optionDLayout.setVisibility(View.VISIBLE);
                dText.setVisibility(View.VISIBLE);
                dRadio.setVisibility(View.VISIBLE);
            }
        }
        checkOptionButtons();
    }

    private void decreaseOptionCount() {
        if (optionCount > 2) {
            if (optionCount == 4) {
                optionDLayout.setVisibility(View.GONE);
                dText.setText("");
                dText.setVisibility(View.GONE);
                dRadio.setChecked(false);
                dRadio.setVisibility(View.GONE);
                if (dRadio.isChecked()) {
                    dRadio.setChecked(false);
                    selectedOption = "";
                }
            } else if (optionCount == 3) {
                optionCLayout.setVisibility(View.GONE);
                cText.setText("");
                cText.setVisibility(View.GONE);
                cRadio.setChecked(false);
                cRadio.setVisibility(View.GONE);
                if (cRadio.isChecked()) {
                    cRadio.setChecked(false);
                    selectedOption = "";
                }
            }
            optionCount--;
        }
        checkOptionButtons();
    }

    private void checkOptionButtons() {
        if (optionCount == 2) {
            removeOptionButton.setEnabled(false);
        } else {
            removeOptionButton.setEnabled(true);
        }
        if (optionCount == 4) {
            addOptionButton.setEnabled(false);
        } else {
            addOptionButton.setEnabled(true);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        final AlertDialog.Builder builder = new AlertDialog.Builder(Custom_quiz.this);
        builder.setMessage("Выйти без сохранения?");
        builder.setPositiveButton("Ок", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                finish();
            }
        }).setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();

            }
        });
        builder.show();
    }

    public void setAllData(int position) {
        clearAllData();
        Question question1 = ques.get(position - 1);
        questionNumber.setText(String.valueOf(question1.getId()));
        question.setText(question1.getQuestion());
        aText.setText(question1.getOpt_A());
        bText.setText(question1.getOpt_B());
        switch (question1.getOptionCount()) {
            case 2:
                resetOptionCount();
                break;
            case 3:
                increaseOptionCount(); // до 3
                cText.setText(question1.getOpt_C());
                break;
            case 4:
                increaseOptionCount(); // до 3
                increaseOptionCount(); // до 4
                cText.setText(question1.getOpt_C());
                dText.setText(question1.getOpt_D());
                break;
        }
        switch (question1.getAnswer()) {
            case "A":
                aRadio.setChecked(true);
                break;
            case "B":
                bRadio.setChecked(true);
                break;
            case "C":
                cRadio.setChecked(true);
                break;
            case "D":
                dRadio.setChecked(true);
                break;
        }

        // Загружаем изображение вопроса, если оно есть
        if (question1.getImageUrl() != null) {
            questionImageView.setVisibility(View.VISIBLE);
            Glide.with(this).load(question1.getImageUrl()).into(questionImageView);
        } else {
            questionImageView.setVisibility(View.GONE);
        }
    }

    private void clearAllData() {
        aRadio.setChecked(false);
        bRadio.setChecked(false);
        cRadio.setChecked(false);
        dRadio.setChecked(false);
        aText.setText(null);
        bText.setText(null);
        cText.setText(null);
        dText.setText(null);
        question.setText(null);
        selectedOption = "";

        selectedImageUri = null;
        questionImageView.setImageDrawable(null);
        questionImageView.setVisibility(View.GONE);

        resetOptionCount();
    }

    private boolean getEnteredQuestionsValue() {

        boolean cont = false;
        if (TextUtils.isEmpty(question.getText().toString().trim())) {
            question.setError("Пожалуйста, заполните вопрос");
        } else if (TextUtils.isEmpty(aText.getText().toString().trim())) {
            aText.setError("Пожалуйста, заполните вариант A");
        } else if (TextUtils.isEmpty(bText.getText().toString().trim())) {
            bText.setError("Пожалуйста, заполните вариант B");
        } else if (optionCount >= 3 && TextUtils.isEmpty(cText.getText().toString().trim())) {
            cText.setError("Пожалуйста, заполните вариант C");
        } else if (optionCount == 4 && TextUtils.isEmpty(dText.getText().toString().trim())) {
            dText.setError("Пожалуйста, заполните вариант D");
        } else if (selectedOption.equals("")) {
            Toast.makeText(this, "Пожалуйста, выберите правильный ответ", Toast.LENGTH_SHORT).show();
        } else {
            // Создаем объект Question
            final Question quest = new Question();
            quest.setId(currentQuestion);
            quest.setQuestion(question.getText().toString());
            quest.setOpt_A(aText.getText().toString());
            quest.setOpt_B(bText.getText().toString());
            if (optionCount >= 3)
                quest.setOpt_C(cText.getText().toString());
            if (optionCount == 4)
                quest.setOpt_D(dText.getText().toString());
            quest.setOptionCount(optionCount);
            quest.setAnswer(selectedOption);

            if (selectedImageUri != null) {
                // Если изображение выбрано, загружаем его в Firebase Storage
                final ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setTitle("Загрузка изображения...");
                progressDialog.show();

                StorageReference ref = storageReference.child("question_images/" + System.currentTimeMillis());
                ref.putFile(selectedImageUri)
                        .addOnSuccessListener(taskSnapshot -> {
                            progressDialog.dismiss();
                            // Получаем URL загруженного изображения
                            ref.getDownloadUrl().addOnSuccessListener(uri -> {
                                String imageUrl = uri.toString();
                                quest.setImageUrl(imageUrl);
                                // Добавляем вопрос в список и JSON
                                addQuestion(quest);
                            });
                        })
                        .addOnFailureListener(e -> {
                            progressDialog.dismiss();
                            Toast.makeText(Custom_quiz.this, "Ошибка загрузки изображения", Toast.LENGTH_SHORT).show();
                        })
                        .addOnProgressListener(taskSnapshot -> {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            progressDialog.setMessage("Загружено " + (int) progress + "%");
                        });
            } else {
                // Если изображение не выбрано, добавляем вопрос сразу
                addQuestion(quest);
            }
            cont = true;
        }
        return cont;
    }

    private void addQuestion(Question quest) {
        ques.add(quest);

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("answer", quest.getAnswer());
            jsonObject.put("opt_A", quest.getOpt_A());
            jsonObject.put("opt_B", quest.getOpt_B());
            if (optionCount >= 3)
                jsonObject.put("opt_C", quest.getOpt_C());
            if (optionCount == 4)
                jsonObject.put("opt_D", quest.getOpt_D());
            jsonObject.put("question", quest.getQuestion());
            jsonObject.put("optionCount", quest.getOptionCount());
            if (quest.getImageUrl() != null)
                jsonObject.put("imageUrl", quest.getImageUrl());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        jsonArray.put(jsonObject);
    }

    private void setListeners() {
        aRadio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                selectedOption = "A";
                bRadio.setChecked(false);
                cRadio.setChecked(false);
                dRadio.setChecked(false);
            }
        });
        bRadio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                selectedOption = "B";
                aRadio.setChecked(false);
                cRadio.setChecked(false);
                dRadio.setChecked(false);
            }
        });
        cRadio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                selectedOption = "C";
                aRadio.setChecked(false);
                bRadio.setChecked(false);
                dRadio.setChecked(false);
            }
        });
        dRadio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                selectedOption = "D";
                aRadio.setChecked(false);
                bRadio.setChecked(false);
                cRadio.setChecked(false);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Обрабатываем результат выбора изображения
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            questionImageView.setImageURI(selectedImageUri);
            questionImageView.setVisibility(View.VISIBLE);
        }
    }
}