package com.epam.jiracom;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

/**
 * Created by Andrei_Pauliukevich1 on 4/21/2016.
 */
public class HttpClient {
    private String user;
    private String password;

    public HttpClient(String user, String password) {
        this.user = user;
        this.password = password;
    }

    public HttpURLConnection get(URL url) throws IOException{
        HttpURLConnection request = createRequest(url);
        request.setRequestMethod("GET");
        return request;
    }

    private HttpURLConnection createRequest(URL url) throws IOException{
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        request.setRequestProperty("Accept", "application/json");
        request.setRequestProperty("Authorization",
                "Basic " + Base64.getEncoder().encodeToString((this.user + ":" + this.password).getBytes()));
        return request;
    }

    public static String getContent(HttpURLConnection request) throws IOException{
        BufferedReader in = new BufferedReader(
                new InputStreamReader(request.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response.toString();
    }
}
