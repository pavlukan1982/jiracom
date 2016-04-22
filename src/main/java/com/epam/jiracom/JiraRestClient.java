package com.epam.jiracom;

import com.epam.jiracom.jira.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONWriter;

import java.io.DataOutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.stream.StreamSupport;

/**
 * Created by Andrei_Pauliukevich1 on 4/21/2016.
 */
public class JiraRestClient {
    private HttpClient httpClient;
    private String host;

    public static final String JIRA_API = "/rest/api/latest";
    public static final String USER_SEARCH = "/user/search";
    public static final String PROJECT_GET = "/project";
    public static final String PRIORITIES_GET = "/priority";
    public static final String STATUSES_GET = "/status";
    public static final String ISSUE = "/issue";
    public static final String ISSUE_STATUSES = "/issue/%s/transitions";
    public static final String ISSUE_LINK_TYPE = "/issueLinkType";
    public static final String ISSUE_LINK = "/issueLink";

    public JiraRestClient(String user, String password, String host) {
        this.httpClient = new HttpClient(user, password);
        this.host = host;
    }

    private String doGet(String sUrl) {
        String response;
        try {
            URL url = new URL(sUrl);
            HttpURLConnection connection = httpClient.getConnection(url, "GET");
            response = doRequest(connection);
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
        return response;
    }

    private String doPost(String sUrl, String content) {
        String response;
        try {
            URL url = new URL(sUrl);
            HttpURLConnection connection = httpClient.getConnection(url, "POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(content);
            wr.flush();
            wr.close();
            response = doRequest(connection);
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
        return response;
    }

    private String doRequest(HttpURLConnection connection) {
        String response;
        try {
            int responseCode = connection.getResponseCode();
            if (!((responseCode >= 200) && (responseCode < 300))) {
                throw new RuntimeException(String.format("%d %s : %s",
                        responseCode,
                        connection.getResponseMessage(),
                        connection.getURL().getPath()));
            }
            response = HttpClient.getContent(connection);
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
        return response;
    }

    public User[] findUser(String userName) {
        JSONArray jsonArray = new JSONArray(doGet(host + JIRA_API + USER_SEARCH + "?username=" + userName));
        return StreamSupport.stream(jsonArray.spliterator(), false)
                .map(o -> (JSONObject) o)
                .map(o -> new User(o.getString("key"), o.getString("emailAddress")))
                .toArray(User[]::new);
    }

    public Project getProject(String key) {
        JSONObject jsonObject = new JSONObject(doGet(host + JIRA_API + PROJECT_GET + "/" + key));
        return (new ProjectBuilder(jsonObject.getInt("id"), jsonObject.getString("key")))
                .withIssueTypes(StreamSupport.stream(jsonObject.getJSONArray("issueTypes").spliterator(), false)
                        .map(o -> (JSONObject) o)
                        .map(o -> new IssueType(o.getInt("id"), o.getString("name")))
                        .toArray(IssueType[]::new))
                .build();
    }

    public Priority[] getPriorities() {
        JSONArray jsonArray = new JSONArray(doGet(host + JIRA_API + PRIORITIES_GET));
        return StreamSupport.stream(jsonArray.spliterator(), false)
                .map(o -> (JSONObject) o)
                .map(jsonObject -> new Priority(jsonObject.getString("id"), jsonObject.getString("name")))
                .toArray(Priority[]::new);
    }

    public Status[] getStatuses() {
        JSONArray jsonArray = new JSONArray(doGet(host + JIRA_API + STATUSES_GET));
        return StreamSupport.stream(jsonArray.spliterator(), false)
                .map(o -> (JSONObject) o)
                .map(jsonObject -> new Status(jsonObject.getInt("id"), jsonObject.getString("name")))
                .toArray(Status[]::new);
    }

    public Issue createIssue(String content) {
        JSONObject jsonObject = new JSONObject(doPost(host + JIRA_API + ISSUE, content));
        return new Issue(jsonObject.getInt("id"), jsonObject.getString("key"));
    }

    public Status[] getIssueStatuses(String issueId) {
        JSONObject jsonTransitions = new JSONObject(doGet(
                String.format(host + JIRA_API + ISSUE_STATUSES, issueId)));
        return StreamSupport.stream(jsonTransitions.getJSONArray("transitions").spliterator(), false)
                .map(o -> (JSONObject) o)
                .map(jsonObject -> new Status(jsonObject.getInt("id"), jsonObject.getString("name")))
                .toArray(Status[]::new);
    }

    public void changeIssueStatus(Issue issue, Status status) {
        StringWriter writer = new StringWriter();
        JSONWriter jsonWriter = new JSONWriter(writer).object().key("transition")
                .object().key("id").value(status.getId()).endObject().endObject();
        doPost(String.format(host + JIRA_API + ISSUE_STATUSES, issue.getId()), writer.toString());
    }

    public IssueLinkType[] getIssueLinkTypes() {
        JSONObject jsonIssueTypes = new JSONObject(doGet(host + JIRA_API + ISSUE_LINK_TYPE));
        return StreamSupport.stream(jsonIssueTypes.getJSONArray("issueLinkTypes").spliterator(), false)
                .map(o -> (JSONObject) o)
                .map(jsonObject -> new IssueLinkType(jsonObject.getInt("id"),
                        jsonObject.getString("name"),
                        jsonObject.getString("inward"),
                        jsonObject.getString("outward")))
                .toArray(IssueLinkType[]::new);
    }

    public void createIssueLink(Issue inwardIssue, Issue outwardIssue, IssueLinkType linkType) {
        StringWriter writer = new StringWriter();
        JSONWriter jsonWriter = new JSONWriter(writer).object()
                .key("type").object().key("name").value(linkType.getName()).endObject()
                .key("inwardIssue").object().key("key").value(inwardIssue.getKey()).endObject()
                .key("outwardIssue").object().key("key").value(outwardIssue.getKey()).endObject()
                .endObject();
        doPost(host + JIRA_API + ISSUE_LINK, writer.toString());
    }
}

