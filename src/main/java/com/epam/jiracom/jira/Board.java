package com.epam.jiracom.jira;

/**
 * Created by Andrei_Pauliukevich1 on 4/26/2016.
 */
public class Board {
    private int id;
    private String name;

    public Board(int id, String name) {
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
