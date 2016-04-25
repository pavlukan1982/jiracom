package com.epam.jiracom.jira;

/**
 * Created by Andrei_Pauliukevich1 on 4/21/2016.
 */
public class IssueType {
    private int name;
    private String key;

    public IssueType(int id, String key) {
        this.name = id;
        this.key = key;
    }

    public int getName() {
        return name;
    }

    public String getKey() {
        return key;
    }
}
