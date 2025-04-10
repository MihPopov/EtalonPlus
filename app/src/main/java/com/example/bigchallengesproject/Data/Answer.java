package com.example.bigchallengesproject.Data;

public class Answer {
    private int id;
    private int etalonId;
    private int taskNumber;
    private String rightAnswer;
    private int points;

    public Answer(int id, int etalonId, int taskNumber, String rightAnswer, int points) {
        this.id = id;
        this.etalonId = etalonId;
        this.taskNumber = taskNumber;
        this.rightAnswer = rightAnswer;
        this.points = points;
    }

    public int getId() { return id; }
    public int getEtalonId() { return etalonId; }
    public int getTaskNumber() { return taskNumber; }
    public String getRightAnswer() { return rightAnswer; }
    public int getPoints() { return points; }
}