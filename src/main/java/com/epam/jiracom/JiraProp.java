package com.epam.jiracom;


import com.beust.jcommander.Parameter;
import com.epam.jiracom.jira.*;
import org.json.JSONWriter;

import java.io.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Andrei_Pauliukevich1 on 4/20/2016.
 */
public class JiraProp {

    @Parameter(names = "-help", help = true)
    private boolean help;

    @Parameter(names = "-path", description = "Path to config file")
    private String path = "";

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

    @Parameter(names = "-issueLink", description = "Type of link between issues")
    private String issueLink;

    @Parameter(names = "-description", description = "Desription of Jira issue")
    private String description;

    @Parameter(names = "-summary", description = "Summary for Jira issue", required = true)
    private String summary;

    @Parameter(names = "-projects", variableArity = true, description = "Jira projects for adding issue")
    private List<String> projects;

    @Parameter(names = "-assignees", variableArity = true, description = "Assignees for Jira projects")
    private List<String> assignees;

    @Parameter(names = "-statusPriorities", variableArity = true, description = "Statuses in order of priority")
    private List<String> statusPriorities;

    @Parameter(names = "-export", description = "Export default properties to file", help = true)
    private boolean export;

    @Parameter(names = "-sprint", description = "Sprint name or state for issues")
    private String sprint;

    private static Pattern pattern = Pattern.compile("([^\"]\\S*|\".+?\")\\s*");


    public void execute() throws IOException {

        if (this.export) {
            File file = new File(this.path);
            if (file.exists()) {
                throw new RuntimeException("Such file exists");
            }
            InputStream inputStream = getClass().getResourceAsStream("jira.properties");
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);

            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(buffer);

            System.exit(0);
        }

        doInitConfig();
        JiraRestClient restClient = new JiraRestClient(userName, password, host);

        if (assignees.size() != projects.size()) {
            throw new RuntimeException("The number of assigneers and projects must be the same");
        }

        if (0 == assignees.size()) {
            throw new RuntimeException("Assignees isn't defined");
        }
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

        if (0 == projects.size()) {
            throw new RuntimeException("Projects isn't defined");
        }
        Project[] jiraProjects = this.projects.stream()
                .map(s -> {
                    Project project = restClient.getProject(s);
                    if (null == project) {
                        throw new RuntimeException("Unable find project : " + s);
                    }
                    return project;})
                .toArray(Project[]::new);

        IssueType[] issueTypes = Arrays.stream(jiraProjects)
                .map(project -> {
                    IssueType findIssueType = Arrays.stream(project.getIssueTypes())
                            .filter(issueType -> this.type.equals(issueType.getKey()))
                            .findFirst()
                            .orElse(null);
                    if (null == findIssueType) {
                        throw new RuntimeException(String.format("Unable find %s in project %s", type, project.getKey()));
                    }
                    return findIssueType;
                })
                .toArray(IssueType[]::new);

        Priority jiraPriority = null;
        if (null != this.priority) {
            jiraPriority = Arrays.stream(restClient.getPriorities())
                    .filter(p -> this.priority.equalsIgnoreCase(p.getName()))
                    .findFirst()
                    .orElse(null);
            if (null == jiraPriority) {
                throw new RuntimeException("Unable to find default priority");
            }
        }

        if (0 == this.statusPriorities.size()) {
            throw new RuntimeException("Status isn't defined");
        }
        if (null == getPreferedStatus(restClient.getStatuses())) {
            throw new RuntimeException("Unable to find default status");
        }

        IssueLinkType[] issueLinkTypes = Arrays.stream(restClient.getIssueLinkTypes())
                .filter(issueLinkType -> issueLink.equalsIgnoreCase(issueLinkType.getInward())
                        || issueLink.equalsIgnoreCase(issueLinkType.getOutward()))
                .toArray(IssueLinkType[]::new);
        if (0 == issueLinkTypes.length) {
            throw new RuntimeException("Unable to find defined type of issue link");
        }
        if (1 < issueLinkTypes.length) {
            throw new RuntimeException("Find more than one type of issue link");
        }
        IssueLinkType issueLinkTypeJira = issueLinkTypes[0];

        // find sprint for project
        Sprint[] jiraSprints = null;
        if (null != this.sprint) {
            jiraSprints = Arrays.stream(jiraProjects)
                    .map(project -> {
                        Sprint[] sprints = Arrays.stream(restClient.getBoards(project))
                                .flatMap(board -> Arrays.stream(restClient.getSprints(board)))
                                .filter(sprint -> this.sprint.equalsIgnoreCase(sprint.getName()) ||
                                        this.sprint.equalsIgnoreCase(sprint.getState()))
                                .toArray(Sprint[]::new);
                        if (0 == sprints.length) {
                            throw new RuntimeException(String.format("Unable to find sprint for %s project",
                                    project.getId()));
                        }
                        if (1 < sprints.length) {
                            throw new RuntimeException(String.format("Find %d sprints for %s project",
                                    sprints.length,
                                    project.getId()));
                        }
                        return sprints;})
                    .map(sprints -> sprints[0])
                    .toArray(Sprint[]::new);
        }


        // crete issues
        List<Issue> issues = new ArrayList<>(jiraProjects.length);
        for (int i = 0; i < jiraProjects.length; i++) {
            StringWriter writer = new StringWriter();
            JSONWriter jsonWriter = new JSONWriter(writer)
                    .object().key("fields").object()
                    .key("project").object().key("id").value(jiraProjects[i].getId()).endObject()
                    .key("issuetype").object().key("id").value(issueTypes[i].getName()).endObject()
                    .key("assignee").object().key("name").value(jiraUsers[i].getName()).endObject()
                    .key("summary").value(this.summary)
                    .key("description").value(this.description);
            if (null != jiraPriority) {
                jsonWriter.key("priority").object().key("id").value(jiraPriority.getId()).endObject();
            }
            jsonWriter.endObject().endObject();
            issues.add(restClient.createIssue(writer.toString()));
        }

        // change status of issues
        issues.stream().forEach(issue -> {
            Status preferedStatus = getPreferedStatus(restClient.getIssueStatuses(issue.getKey()));
            restClient.changeIssueStatus(issue, preferedStatus);
        });

        // create link between issues
        for (int i = 0; i < issues.size(); i++) {
            for (int j = i + 1; j < issues.size(); j++) {
                restClient.createIssueLink(issues.get(i), issues.get(j), issueLinkTypeJira);
            }
        }

        // add issues to sprints
        if (null != this.sprint) {
            for (int i = 0; i < issues.size(); i++) {
                restClient.addIssueToSprint(jiraSprints[i], issues.get(i));
            }
        }

    }

    private Status getPreferedStatus(Status[] jiraStatuses) {
        return statusPriorities.stream()
                .map(s -> Arrays.stream(jiraStatuses)
                        .filter(status -> s.equalsIgnoreCase(status.getName()))
                        .findFirst()
                        .orElse(null))
                .filter(status -> null != status)
                .findFirst()
                .orElse(null);
    }

    private void doInitConfig() {
        Properties properties = new Properties();
        try {
            InputStream inputStream;
            if (path.isEmpty()) {
                inputStream = getClass().getResourceAsStream("jira.properties");
            } else {
                inputStream = new FileInputStream(path);
            }
            properties.load(inputStream);

            properties.keySet()
                    .stream()
                    .forEach(o -> {
                        try {
                            Field field = getClass().getDeclaredField((String) o);
                            Object value = properties.get(o);
                            if (!((String) value).isEmpty()
                                    && null == field.get(this)) {
                                if (field.getType().isAssignableFrom(List.class)) {
                                    Matcher matcher = pattern.matcher((String) value);
                                    List<String> params = new ArrayList<>();
                                    while (matcher.find()) {
                                        params.add(matcher.group(1).replace("\"", ""));
                                    }
                                    value = params;
                                } else {
                                    value = ((String) value).replace("\"", "");
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

    public boolean isHelp() {
        return help;
    }
}
