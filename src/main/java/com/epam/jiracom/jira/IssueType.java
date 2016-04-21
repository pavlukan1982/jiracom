package com.epam.jiracom.jira;

/**
 * Created by Andrei_Pauliukevich1 on 4/21/2016.
 */
public class IssueType {
    private int id;
    private String key;

    public IssueType(int id, String key) {
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
