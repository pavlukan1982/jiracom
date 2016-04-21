package com.epam.jiracom;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
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

    public JiraRestClient(String user, String password, String host) {
        this.httpClient = new HttpClient(user, password);
        this.host = host;
    }

    public String[] findUser(String userName) {
        JSONArray jsonArray = null;
        try {
            URL url = new URL(host + JIRA_API + USER_SEARCH + "?username=" + userName);
            HttpURLConnection httpURLConnection = httpClient.get(url);
            jsonArray = new JSONArray(HttpClient.getContent(httpURLConnection));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return StreamSupport.stream(jsonArray.spliterator(), false)
                .map(o -> ((JSONObject) o).get("key"))
                .toArray(String[]::new);
    }
}

