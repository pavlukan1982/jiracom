package com.epam.jiracom;


import com.beust.jcommander.Parameter;
import com.epam.jiracom.jira.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Created by Andrei_Pauliukevich1 on 4/20/2016.
 */
public class JiraProp {

    @Parameter(names = "-help", help = true)
    private boolean help;

    @Parameter(names = "-path", description = "Path to config file")
    private String path = "src\\main\\resources\\jira.properties";

    @Parameter(names = "-password", description = "Password for Jira user")
    private String password;

    @Parameter(names = "-host", description = "Jira host")
    private String host;

    @Parameter(names = "-userName", description = "Jira user name")
    private String userName;

    @Parameter(names = "-type", description = "Type of Jira issue")
    private String type;

    @Parameter(names = "-priority", description = "Priority of Jira issue")
    private String priority;

    @Parameter(names = "-subject", description = "Subject of Jira issue")
    private String subject;

    @Parameter(names = "-summary", description = "Summary for Jira issue")
    private String summary;

    @Parameter(names = "-projects", variableArity = true, description = "Jira projects for adding issue")
    private List<String> projects;

    @Parameter(names = "-assignees", variableArity = true, description = "Assignees for Jira projects")
    private List<String> assignees;

    @Parameter(names = "-statusPriorities", variableArity = true, description = "Statuses in order of priority")
    private List<String> statusPriorities;


    public void execute() throws IOException {
        doInitConfig();
        JiraRestClient restClient = new JiraRestClient(userName, password, host);

        User[] jiraUsers = assignees.stream()
                .map(s -> {
                    User[] users = restClient.findUser(s);
                    if (0 == users.length) {
                        throw new RuntimeException("Unable to find Jira user for : " + s);
                    }
                    if (users.length > 1) {
                        throw new RuntimeException("Found more than one Jira user for : " + s);
                    }
                    return users[0];})
                .toArray(User[]::new);

        Project[] projects = this.projects.stream()
                .map(s -> {
                    Project project = restClient.getProject(s);
                    if (null == project) {
                        throw new RuntimeException("Unable find project : " + s);
                    }
                    return project;})
                .toArray(Project[]::new);

        Arrays.stream(projects)
                .forEach(project -> {
                    IssueType findIssueType = Arrays.stream(project.getIssueTypes())
                            .filter(issueType -> this.type.equals(issueType.getKey()))
                            .findFirst()
                            .orElse(null);
                    if (null == findIssueType) {
                        throw new RuntimeException(String.format("Unable find %s in project %s", type, project.getKey()));
                    }
                });

        if (null == this.priority) {
            throw new RuntimeException("Priority isn't defined");
        }
        Priority priority = Arrays.stream(restClient.getPriorities())
                .filter(p -> this.priority.equalsIgnoreCase(p.getName()))
                .findFirst()
                .orElse(null);
        if (null == priority) {
            throw new RuntimeException("Unable to find default priority");
        }

        if (0 == this.statusPriorities.size()) {
            throw new RuntimeException("Status isn't defined");
        }
        Status[] statuses = restClient.getStatuses();
        Status preferedStatus = statusPriorities.stream()
                .map(s -> Arrays.stream(statuses)
                        .filter(status -> s.equalsIgnoreCase(status.getName()))
                        .findFirst()
                        .orElse(null))
                .filter(status -> null != status)
                .findFirst()
                .orElse(null);
        if (null == preferedStatus) {
            throw new RuntimeException("Unable to find default status");
        }

    }

    private void doInitConfig() {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(path));
            properties.keySet()
                    .stream()
                    .forEach(o -> {
                        try {
                            Field field = getClass().getDeclaredField((String) o);
                            Object value = properties.get(o);
                            if (!((String) value).isEmpty()
                                    && null == field.get(this)) {
                                if (field.getType().isAssignableFrom(List.class)) {
                                    value = Arrays.asList(((String) value).split(" "));
                                }
                                field.set(this, value);
                            }
                        } catch (NoSuchFieldException | IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
