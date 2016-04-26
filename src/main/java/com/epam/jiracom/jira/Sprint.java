package com.epam.jiracom.jira;

/**
 * Created by Andrei_Pauliukevich1 on 4/26/2016.
 */
public class Sprint {
    private int id;
    private String name;
    private String state;

    public Sprint(int id, String name, String state) {
        this.id = id;
        this.name = name;
        this.state = state;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getState() {
        return state;
    }
}
