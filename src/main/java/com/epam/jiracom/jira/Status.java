package com.epam.jiracom.jira;

/**
 * Created by Andrei_Pauliukevich1 on 4/21/2016.
 */
public class Status {
    private int id;
    private String name;

    public Status(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
