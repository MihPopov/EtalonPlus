package com.mihpopov.etalonplus.Data;

public class Grade {
    private int id;
    private int etalonId;
    private double minPoints;
    private double maxPoints;
    private String grade;

    public Grade(int id, int etalonId, double minPoints, double maxPoints, String grade) {
        this.id = id;
        this.etalonId = etalonId;
        this.minPoints = minPoints;
        this.maxPoints = maxPoints;
        this.grade = grade;
    }

    public int getId() { return id; }
    public double getMinPoints() { return minPoints; }
    public double getMaxPoints() { return maxPoints; }
    public String getGrade() { return grade; }
}