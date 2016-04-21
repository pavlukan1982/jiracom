package com.epam.jiracom.jira;

/**
 * Created by Andrei_Pauliukevich1 on 4/21/2016.
 */
public class ProjectBuilder {
    private int id;
    private String key;
    private IssueType[] issueTypes;

    public ProjectBuilder(int id, String key) {
        this.id = id;
        this.key = key;
    }

    public ProjectBuilder withIssueTypes(IssueType[] issueTypes) {
        this.issueTypes = issueTypes;
        return this;
    }

    public Project build() {
        return new Project(this.id, this.key, this.issueTypes);
    }
}
