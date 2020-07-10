package com.example.todo_app.model;

public class Task {
    private int id;
    private String task;
    private boolean active;
    private String creationDate;

    public Task() {

    }

    public Task(int id, String task, boolean active, String creationDate) {
        this.id = id;
        this.task = task;
        this.active = active;
        this.creationDate = creationDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getCreationDate() {
        return this.creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }
}
