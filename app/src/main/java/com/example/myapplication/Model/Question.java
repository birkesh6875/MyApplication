package com.example.myapplication.Model;

import java.io.Serializable;

public class Question implements Serializable {

    private int id;
    private String question;
    private String opt_A;
    private String opt_B;
    private String opt_C;
    private String opt_D;
    private String answer;
    private int optionCount;
    private String imageUrl; // Новое поле для URL изображения

    public Question() {

    }

    public Question(int id, String question, String opt_A, String opt_B, String opt_C, String opt_D, String answer, int optionCount, String imageUrl) {
        this.id = id;
        this.question = question;
        this.opt_A = opt_A;
        this.opt_B = opt_B;
        this.opt_C = opt_C;
        this.opt_D = opt_D;
        this.answer = answer;
        this.optionCount = optionCount;
        this.imageUrl = imageUrl;
    }

    // Геттеры и сеттеры для всех полей, включая новое поле imageUrl
    public int getId() {
        return id;
    }

    public String getQuestion() {
        return question;
    }

    public String getOpt_A() {
        return opt_A;
    }

    public String getOpt_B() {
        return opt_B;
    }

    public String getOpt_C() {
        return opt_C;
    }

    public String getOpt_D() {
        return opt_D;
    }

    public String getAnswer() {
        return answer;
    }

    public int getOptionCount() {
        return optionCount;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public void setOpt_A(String opt_A) {
        this.opt_A = opt_A;
    }

    public void setOpt_B(String opt_B) {
        this.opt_B = opt_B;
    }

    public void setOpt_C(String opt_C) {
        this.opt_C = opt_C;
    }

    public void setOpt_D(String opt_D) {
        this.opt_D = opt_D;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public void setOptionCount(int optionCount) {
        this.optionCount = optionCount;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}