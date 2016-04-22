package com.epam.jiracom.jira;

/**
 * Created by Andrei_Pauliukevich1 on 4/22/2016.
 */
public class Issue {
    private int id;
    private String key;

    public Issue(int id, String key) {
        this.id = id;
        this.key = key;
    }

    public int getId() {
        return id;
    }

    public String getKey() {
        return key;
    }
}
