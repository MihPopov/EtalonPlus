package com.example.bigchallengesproject.Data;

public class Answer {

    private int id;
    private int etalonId;
    private int taskNumber;
    private String answerType;
    private String rightAnswer;
    private double points;
    private int orderMatters;
    private String checkMethod;

    public Answer(int id, int etalonId, int taskNumber, String answerType, String rightAnswer, double points, int orderMatters, String checkMethod) {
        this.id = id;
        this.etalonId = etalonId;
        this.taskNumber = taskNumber;
        this.answerType = answerType;
        this.rightAnswer = rightAnswer;
        this.points = points;
        this.orderMatters = orderMatters;
        this.checkMethod = checkMethod;
    }

    public Answer(int id, int etalonId, int taskNumber, String answerType) {
        this.id = id;
        this.etalonId = etalonId;
        this.taskNumber = taskNumber;
        this.answerType = answerType;
    }

    public int getId() { return id; }
    public int getEtalonId() {
        return etalonId;
    }
    public int getTaskNumber() { return taskNumber; }
    public String getAnswerType() {
        return answerType;
    }
    public String getRightAnswer() { return rightAnswer; }
    public double getPoints() { return points; }
    public int getOrderMatters() {return orderMatters; }
    public String getCheckMethod() { return checkMethod; }
}