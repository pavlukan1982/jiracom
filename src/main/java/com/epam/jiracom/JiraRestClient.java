package com.epam.jiracom;

import com.epam.jiracom.jira.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.stream.StreamSupport;

/**
 * Created by Andrei_Pauliukevich1 on 4/21/2016.
 */
public class JiraRestClient {
    private HttpClient httpClient;
    private String host;

    public static final String JIRA_API = "/rest/api/2";
    public static final String USER_SEARCH = "/user/search";
    public static final String PROJECT_GET = "/project";
    public static final String PRIORITIES_GET = "/priority";
    public static final String STATUSES_GET = "/status";

    public JiraRestClient(String user, String password, String host) {
        this.httpClient = new HttpClient(user, password);
        this.host = host;
    }

    private String doGet(String sUrl) {
        String response;
        try {
            URL url = new URL(sUrl);
            HttpURLConnection httpURLConnection = httpClient.get(url);
            if (!((httpURLConnection.getResponseCode() >= 200) && (httpURLConnection.getResponseCode() < 300))) {
                throw new RuntimeException(String.format("%d %s : %s",
                        httpURLConnection.getResponseCode(),
                        httpURLConnection.getResponseMessage(),
                        url.getPath()));
            }
            response = HttpClient.getContent(httpURLConnection);
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
                .map(jsonObject -> new Priority(jsonObject.getInt("id"), jsonObject.getString("name")))
                .toArray(Priority[]::new);
    }

    public Status[] getStatuses() {
        JSONArray jsonArray = new JSONArray(doGet(host + JIRA_API + STATUSES_GET));
        return StreamSupport.stream(jsonArray.spliterator(), false)
                .map(o -> (JSONObject) o)
                .map(jsonObject -> new Status(jsonObject.getInt("id"), jsonObject.getString("name")))
                .toArray(Status[]::new);
    }

}

