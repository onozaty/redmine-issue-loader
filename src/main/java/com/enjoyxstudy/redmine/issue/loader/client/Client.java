package com.enjoyxstudy.redmine.issue.loader.client;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import lombok.Builder;
import lombok.Value;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Value
@Builder
public class Client {

    private final OkHttpClient httpClient = new OkHttpClient();

    private final ObjectMapper objectMapper = new ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);

    private final String redmineBaseUrl;

    private final String apiKey;

    public List<Issue> getIssues(QueryParameter query) throws IOException {
        return get("issues.json", query, IssuesBody.class).getIssues();
    }

    public int createIssue(Map<String, Object> targetFields) throws IOException {
        return (int) post("issues.json", new IssueBody(targetFields), IssueBody.class).getFields().get("id");
    }

    public void updateIssue(int issueId, Map<String, Object> targetFields) throws IOException {
        put("issues/" + issueId + ".json", new IssueBody(targetFields));
    }

    private <T> T get(String path, QueryParameter query, Class<T> responseType) throws IOException {

        HttpUrl url = HttpUrl.get(redmineBaseUrl)
                .resolve(path)
                .newBuilder()
                .addQueryParameter(query.getName(), query.getValue())
                .build();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("X-Redmine-API-Key", apiKey)
                .build();

        Response response = httpClient.newCall(request).execute();

        if (!response.isSuccessful()) {
            throw new IOException("Failed to call Redmine API. " + response);
        }

        return objectMapper.readValue(
                response.body().string(),
                responseType);
    }

    private <T> T post(String path, Object body, Class<T> responseType) throws IOException {

        HttpUrl url = HttpUrl.get(redmineBaseUrl)
                .resolve(path)
                .newBuilder()
                .build();

        RequestBody requestBody = RequestBody.create(
                MediaType.get("application/json; charset=utf-8"),
                objectMapper.writeValueAsString(body));

        Request request = new Request.Builder()
                .url(url)
                .addHeader("X-Redmine-API-Key", apiKey)
                .post(requestBody)
                .build();

        Response response = httpClient.newCall(request).execute();

        if (!response.isSuccessful()) {
            throw new IOException("Failed to call Redmine API. " + response);
        }

        return objectMapper.readValue(
                response.body().string(),
                responseType);
    }

    private void put(String path, Object body) throws IOException {

        HttpUrl url = HttpUrl.get(redmineBaseUrl)
                .resolve(path)
                .newBuilder()
                .build();

        RequestBody requestBody = RequestBody.create(
                MediaType.get("application/json; charset=utf-8"),
                objectMapper.writeValueAsString(body));

        Request request = new Request.Builder()
                .url(url)
                .addHeader("X-Redmine-API-Key", apiKey)
                .put(requestBody)
                .build();

        Response response = httpClient.newCall(request).execute();

        if (!response.isSuccessful()) {
            throw new IOException("Failed to call Redmine API. " + response);
        }
    }
}
