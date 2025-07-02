package com.mihpopov.etalonplus.Data;

/**
 * Класс ComplexCriteria представляет диапазон ошибок в задании с поэлементным оцениванием задания с кратким ответом.
 */
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
    public int getMinMistakes() { return minMistakes; }
    public int getMaxMistakes() { return maxMistakes; }
    public double getPoints() { return points; }
}