package com.example.bigchallengesproject.Data;

public class Grade {
    private int id;
    private int etalonId;
    private String minPoints;
    private String maxPoints;
    private String grade;

    public Grade(int id, int etalonId, String minPoints, String maxPoints, String grade) {
        this.id = id;
        this.etalonId = etalonId;
        this.minPoints = minPoints;
        this.maxPoints = maxPoints;
        this.grade = grade;
    }

    public int getId() { return id; }
    public String getMinPoints() { return minPoints; }
    public String getMaxPoints() { return maxPoints; }
    public String getGrade() { return grade; }
}