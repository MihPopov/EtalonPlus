package com.mihpopov.etalonplus.Data;

public class ComplexCriteria {

    private int id;
    private int answerId;
    private int minMistakes;
    private int maxMistakes;
    private double points;

    public ComplexCriteria(int id, int answerId, int minMistakes, int maxMistakes, double points) {
        this.id = id;
        this.answerId = answerId;
        this.minMistakes = minMistakes;
        this.maxMistakes = maxMistakes;
        this.points = points;
    }

    public int getId() { return id; }
    public int getAnswerId() { return answerId; }
    public int getMinMistakes() { return minMistakes; }
    public int getMaxMistakes() { return maxMistakes; }
    public double getPoints() { return points; }
}