package com.example.bigchallengesproject.Data;

public class Etalon {
    private int id;
    private String name;
    private byte[] icon;
    private String creationDate;
    private int tasksCount;

    public Etalon(int id, String name, byte[] icon, String creationDate, int tasksCount) {
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.creationDate = creationDate;
        this.tasksCount = tasksCount;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public byte[] getIcon() { return icon; }
    public void setIcon(byte[] icon) { this.icon = icon; }

    public String getCreationDate() { return creationDate; }
    public void setCreationDate(String creationDate) { this.creationDate = creationDate; }

    public int getTasksCount() { return tasksCount; }
    public void setTasksCount(int tasksCount) { this.tasksCount = tasksCount; }
}