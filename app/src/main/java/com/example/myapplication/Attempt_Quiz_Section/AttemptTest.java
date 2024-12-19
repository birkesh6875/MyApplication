package com.example.myapplication.Attempt_Quiz_Section;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
// Другие необходимые импорты
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.Model.Question;
import com.example.myapplication.Model.Test;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
// Другие необходимые импорты
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import es.dmoral.toasty.Toasty;

public class AttemptTest extends AppCompatActivity {

    ArrayList<Question> questions;
    String[] answers;
    Toolbar toolbar;
    ViewPager2 scrollView;
    LinearLayout indexLayout;
    GridView quesGrid;
    ArrayList<String> list;
    ArrayList<String> arrayList;
    int flag_controller = 1;
    long timer;
    popGridAdapter popGrid;
    Button next, prev;
    private DatabaseReference mDatabase;
    private FirebaseAuth auth;
    private String TESTNAME;
    private int countPaused = 0;
    private boolean hasTimeLimit = false;
    private int score = 0;
    private int totalQuestions = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        auth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_attempt);

        questions = ((Test) getIntent().getExtras().get("Questions")).getQuestions();
        TESTNAME = (String) getIntent().getExtras().get("TESTNAME");
        answers = new String[questions.size()];
        totalQuestions = questions.size();

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.black));
        setSupportActionBar(toolbar);

        scrollView = findViewById(R.id.discrete);
        scrollView.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);

        final QuestionAdapter questionAdapter = new QuestionAdapter(questions);
        scrollView.setAdapter(questionAdapter);

        next = findViewById(R.id.next);
        next.setOnClickListener(view -> {
            if (scrollView.getCurrentItem() == questions.size() - 1) {
                showPopUp();
            } else {
                scrollView.setCurrentItem(scrollView.getCurrentItem() + 1);
            }
        });

        prev = findViewById(R.id.prev);
        prev.setOnClickListener(view -> {
            if (scrollView.getCurrentItem() != 0) {
                scrollView.setCurrentItem(scrollView.getCurrentItem() - 1);
            }
        });

        setNextPrevButton(scrollView.getCurrentItem());

        indexLayout = findViewById(R.id.index_layout);
        indexLayout.setAlpha(.5f);
        quesGrid = findViewById(R.id.pop_grid);
        popGrid = new popGridAdapter(AttemptTest.this);
        quesGrid.setAdapter(popGrid);
        quesGrid.setOnItemClickListener((adapterView, view, i, l) -> {
            scrollView.setCurrentItem(i);
            slideUp(indexLayout);
        });

        scrollView.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                setNextPrevButton(position);
            }
        });

        Long testTime = ((Test) getIntent().getExtras().get("Questions")).getTime();
        if (testTime != null && testTime > 0) {
            timer = testTime * 60 * 1000;
            hasTimeLimit = true;
        } else {
            timer = -1;
            hasTimeLimit = false;
        }

        if (hasTimeLimit) {
            Toasty.info(AttemptTest.this, "У Вас ограничение по времени! Завершите или отправьте свой тест.", Toasty.LENGTH_LONG).show();
        }
    }

    void showPopUp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(AttemptTest.this);
        builder.setMessage("Вы хотите отправить результаты?");
        builder.setPositiveButton("Да", (dialogInterface, i) -> {
            submit();
            dialogStart();
        });

        builder.setNegativeButton("Нет", (dialogInterface, i) -> dialogInterface.cancel());
        builder.show();
    }

    /* Отправка результатов в базу данных */
    void submit() {
        flag_controller = 0;
        score = 0;
        list = new ArrayList<>();
        arrayList = new ArrayList<>();
        for (int i = 0; i < answers.length; i++) {
            if (answers[i] != null && answers[i].equals(questions.get(i).getAnswer())) {
                score++;
            }
            String temp = (answers[i] != null) ? answers[i] + ")" : "null) ";
            list.add("Ваш выбор (" + temp + " Правильный ответ (" + questions.get(i).getAnswer() + ")");
            arrayList.add(questions.get(i).getQuestion());
        }

        try {
            mDatabase.child("Results")
                    .child(((Test) getIntent().getExtras().get("Questions")).getName())
                    .child(auth.getUid()).setValue(score);
        } catch (Exception e) {
            Log.e("Result Update Failed ", e.getMessage());
        }
    }

    void dialogStart() {
        AlertDialog.Builder builder = new AlertDialog.Builder(AttemptTest.this);
        builder.setTitle("Ваш результат");
        builder.setMessage("Вы ответили правильно на " + score + " из " + totalQuestions + " вопросов.");
        builder.setCancelable(false);
        builder.setPositiveButton("Просмотреть ответы", (dialog, which) -> showDetailedResults());
        builder.setNegativeButton("Закончить", (dialog, which) -> {
            finish();
            dialog.dismiss();
        });
        builder.show();
    }

    void showDetailedResults() {
        final AlertDialog.Builder builderSingle = new AlertDialog.Builder(AttemptTest.this);
        builderSingle.setIcon(R.mipmap.ic_launcher_round);
        builderSingle.setTitle("Ответы на " + TESTNAME);
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(AttemptTest.this, android.R.layout.select_dialog_item);
        final ArrayAdapter<String> arrayAdapter1 = new ArrayAdapter<>(AttemptTest.this, android.R.layout.select_dialog_item);

        for (String y : arrayList) {
            arrayAdapter1.add(y);
        }
        for (String x : list) {
            arrayAdapter.add(x);
        }

        builderSingle.setCancelable(false);
        builderSingle.setNegativeButton("Готово!", (dialog, which) -> {
            finish();
            dialog.dismiss();
        });

        builderSingle.setAdapter(arrayAdapter1, (dialog, which) -> {
            String strName = arrayAdapter.getItem(which);
            AlertDialog.Builder builderInner = new AlertDialog.Builder(AttemptTest.this);
            builderInner.setMessage(strName);
            builderInner.setCancelable(false);
            builderInner.setTitle("Ваш ответ:");
            builderInner.setPositiveButton("Ок", (dialog1, which1) -> builderSingle.show());
            builderInner.show();
        });
        builderSingle.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (hasTimeLimit && countPaused <= 2 && countPaused >= 0 && flag_controller == 1) {
            Toasty.info(AttemptTest.this, "Завершите или отправьте свой тест! У вас ограничение по времени.", Toasty.LENGTH_LONG).show();
        }
        countPaused++;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (hasTimeLimit && countPaused > 2) {
            Toasty.success(AttemptTest.this, "Спасибо! Ваши ответы были отправлены.", Toasty.LENGTH_SHORT).show();
            countPaused = -1000;
            submit();
            dialogStart();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (hasTimeLimit) {
            Toasty.info(AttemptTest.this, "Завершите или отправьте свой тест! У вас ограничение по времени.", Toasty.LENGTH_LONG).show();
        }
    }

    void setNextPrevButton(int pos) {
        if (pos == 0) {
            prev.setText("");
        } else {
            prev.setText("Предыдущий");
        }
        if (pos == questions.size() - 1) {
            next.setText("Отправить");
        } else {
            next.setText("Следующий");
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        showPopUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.attempt_menu, menu);

        if (hasTimeLimit) {
            final MenuItem counter = menu.findItem(R.id.counter);

            new android.os.CountDownTimer(timer, 1000) {
                public void onTick(long millisUntilFinished) {
                    long millis = millisUntilFinished;
                    long hr = TimeUnit.MILLISECONDS.toHours(millis);
                    long mn = TimeUnit.MILLISECONDS.toMinutes(millis) -
                            TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis));
                    long sc = TimeUnit.MILLISECONDS.toSeconds(millis) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis));

                    String hms = format(hr) + ":" + format(mn) + ":" + format(sc);
                    counter.setTitle(hms);
                    timer = millis;
                }

                String format(long n) {
                    return n < 10 ? "0" + n : "" + n;
                }

                public void onFinish() {
                    submit();
                    dialogStart();
                }
            }.start();
        } else {
            MenuItem counter = menu.findItem(R.id.counter);
            counter.setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.submit) {
            showPopUp();
            return true;
        } else if (id == R.id.info) {
            togglePopUp();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void togglePopUp() {
        if (indexLayout.getVisibility() == View.GONE) {
            slideDown(indexLayout);
        } else {
            slideUp(indexLayout);
        }
    }

    class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.ViewHolder> {

        private ArrayList<Question> data;

        QuestionAdapter(ArrayList<Question> data) {
            this.data = data;
        }

        @Override
        public QuestionAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View v = inflater.inflate(R.layout.frag_test, parent, false);
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            v.setLayoutParams(params);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final QuestionAdapter.ViewHolder holder, final int position) {
            Question currentQuestion = data.get(position);
            Log.d("AttemptTest", "Binding question: " + currentQuestion.getQuestion());
            holder.questionText.setText(currentQuestion.getQuestion());
            holder.r1.setText(currentQuestion.getOpt_A());
            holder.r2.setText(currentQuestion.getOpt_B());

            int optionCount = currentQuestion.getOptionCount();

            // Обработка варианта C
            if (optionCount >= 3 && currentQuestion.getOpt_C() != null) {
                holder.r3.setVisibility(View.VISIBLE);
                holder.r3.setText(currentQuestion.getOpt_C());
            } else {
                holder.r3.setVisibility(View.GONE);
            }

            // Обработка варианта D
            if (optionCount == 4 && currentQuestion.getOpt_D() != null) {
                holder.r4.setVisibility(View.VISIBLE);
                holder.r4.setText(currentQuestion.getOpt_D());
            } else {
                holder.r4.setVisibility(View.GONE);
            }

            // Загружаем изображение, если оно есть
            if (currentQuestion.getImageUrl() != null && !currentQuestion.getImageUrl().isEmpty()) {
                holder.questionImageView.setVisibility(View.VISIBLE);
                Glide.with(holder.questionImageView.getContext())
                        .load(currentQuestion.getImageUrl())
                        .into(holder.questionImageView);
            } else {
                holder.questionImageView.setVisibility(View.GONE);
            }

            holder.radioGroup.setOnCheckedChangeListener(null);

            if (answers[position] == null) {
                holder.radioGroup.clearCheck();
            } else if (answers[position].equals("A")) {
                holder.radioGroup.check(R.id.radioButton);
            } else if (answers[position].equals("B")) {
                holder.radioGroup.check(R.id.radioButton2);
            } else if (answers[position].equals("C")) {
                holder.radioGroup.check(R.id.radioButton3);
            } else if (answers[position].equals("D")) {
                holder.radioGroup.check(R.id.radioButton4);
            }

            holder.radioGroup.setOnCheckedChangeListener((radioGroup, i) -> {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    if (i == R.id.radioButton) {
                        answers[adapterPosition] = "A";
                    } else if (i == R.id.radioButton2) {
                        answers[adapterPosition] = "B";
                    } else if (i == R.id.radioButton3) {
                        answers[adapterPosition] = "C";
                    } else if (i == R.id.radioButton4) {
                        answers[adapterPosition] = "D";
                    }
                    popGrid.notifyDataSetChanged();
                }
            });

            holder.clearSelectionButton.setOnClickListener(v -> {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    holder.radioGroup.setOnCheckedChangeListener(null);
                    holder.radioGroup.clearCheck();
                    answers[adapterPosition] = null;
                    holder.radioGroup.setOnCheckedChangeListener((radioGroup, i) -> {
                        if (i == R.id.radioButton) {
                            answers[adapterPosition] = "A";
                        } else if (i == R.id.radioButton2) {
                            answers[adapterPosition] = "B";
                        } else if (i == R.id.radioButton3) {
                            answers[adapterPosition] = "C";
                        } else if (i == R.id.radioButton4) {
                            answers[adapterPosition] = "D";
                        }
                        popGrid.notifyDataSetChanged();
                    });
                    popGrid.notifyDataSetChanged();
                }
            });
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            private TextView questionText;
            private ImageView questionImageView;
            private RadioGroup radioGroup;
            private RadioButton r1, r2, r3, r4;
            private Button clearSelectionButton;

            ViewHolder(View itemView) {
                super(itemView);
                questionText = itemView.findViewById(R.id.questionTextView);
                questionImageView = itemView.findViewById(R.id.questionImageView);
                radioGroup = itemView.findViewById(R.id.radioGroup);
                r1 = itemView.findViewById(R.id.radioButton);
                r2 = itemView.findViewById(R.id.radioButton2);
                r3 = itemView.findViewById(R.id.radioButton3);
                r4 = itemView.findViewById(R.id.radioButton4);
                clearSelectionButton = itemView.findViewById(R.id.clearSelectionButton);
            }
        }
    }


    class popGridAdapter extends BaseAdapter {
        Context mContext;

        popGridAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getCount() {
            return questions.size();
        }

        @Override
        public Object getItem(int i) {
            return questions.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {
            Button convertView;
            if (view == null) {
                convertView = new Button(mContext);
            } else {
                convertView = (Button) view;
            }

            if (answers[i] == null)
                convertView.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
            else
                convertView.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));

            convertView.setText("" + (i + 1));

            convertView.setOnClickListener(v -> scrollView.setCurrentItem(i));

            return convertView;
        }
    }

    public void slideUp(View view) {
        TranslateAnimation animate = new TranslateAnimation(
                0, 0, 0, -view.getHeight());
        animate.setDuration(500);
        view.startAnimation(animate);
        view.setVisibility(View.GONE);
    }

    public void slideDown(View view) {
        view.setVisibility(View.VISIBLE);
        TranslateAnimation animate = new TranslateAnimation(
                0, 0, -view.getHeight(), 0);
        animate.setDuration(500);
        view.startAnimation(animate);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}