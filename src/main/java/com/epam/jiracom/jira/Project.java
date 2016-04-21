package com.epam.jiracom.jira;

/**
 * Created by Andrei_Pauliukevich1 on 4/21/2016.
 */
public class Project {
    private int id;
    private String key;
    private IssueType[] issueTypes;

    public Project(int id, String key, IssueType[] issueTypes) {
        this.id = id;
        this.key = key;
        this.issueTypes = issueTypes;
    }

    public int getId() {
        return id;
    }

    public String getKey() {
        return key;
    }

    public IssueType[] getIssueTypes() {
        return issueTypes;
    }
}
