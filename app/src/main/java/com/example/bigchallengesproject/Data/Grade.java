package com.example.bigchallengesproject.Data;

public class Grade {
    private int id;
    private int etalonId;
    private int minPoints;
    private int maxPoints;
    private String grade;

    public Grade(int id, int etalonId, int minPoints, int maxPoints, String grade) {
        this.id = id;
        this.etalonId = etalonId;
        this.minPoints = minPoints;
        this.maxPoints = maxPoints;
        this.grade = grade;
    }

    public int getId() { return id; }
    public int getEtalonId() { return etalonId; }
    public int getMinPoints() { return minPoints; }
    public int getMaxPoints() { return maxPoints; }
    public String getGrade() { return grade; }
}