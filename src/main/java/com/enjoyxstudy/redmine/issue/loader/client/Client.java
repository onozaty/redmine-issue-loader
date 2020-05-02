package com.enjoyxstudy.redmine.issue.loader.client;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import com.enjoyxstudy.redmine.issue.loader.input.BasicAuth;
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
public class Client {

    private final OkHttpClient httpClient;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);

    private final String redmineBaseUrl;

    private final String apiKey;

    private final BasicAuth basicAuth;

    @Builder
    public Client(String redmineBaseUrl, String apiKey, BasicAuth basicAuth, int timeout) {
        this.redmineBaseUrl = redmineBaseUrl;
        this.apiKey = apiKey;
        this.basicAuth = basicAuth;

        httpClient = new OkHttpClient.Builder()
                .connectTimeout(timeout, TimeUnit.SECONDS)
                .readTimeout(timeout, TimeUnit.SECONDS)
                .writeTimeout(timeout, TimeUnit.SECONDS)
                .build();
    }

    public List<Issue> getIssues(List<QueryParameter> queryParameters) throws IOException {
        return get("issues.json", queryParameters, IssuesBody.class).getIssues();
    }

    public int createIssue(Map<String, Object> targetFields) throws IOException {
        return (int) post("issues.json", new IssueBody(targetFields), IssueBody.class).getFields().get("id");
    }

    public void updateIssue(int issueId, Map<String, Object> targetFields) throws IOException {
        put("issues/" + issueId + ".json", new IssueBody(targetFields));
    }

    private <T> T get(String path, List<QueryParameter> queryParameters, Class<T> responseType) throws IOException {

        okhttp3.HttpUrl.Builder httpUrlBuilder = HttpUrl.get(redmineBaseUrl)
                .resolve(path)
                .newBuilder();

        for (QueryParameter queryParameter : queryParameters) {
            httpUrlBuilder.addQueryParameter(queryParameter.getName(), queryParameter.getValue());
        }

        Request request = newRequestBuilder(httpUrlBuilder.build())
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

        Request request = newRequestBuilder(url)
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

        Request request = newRequestBuilder(url)
                .put(requestBody)
                .build();

        Response response = httpClient.newCall(request).execute();

        if (!response.isSuccessful()) {
            throw new IOException("Failed to call Redmine API. " + response);
        }
    }

    private Request.Builder newRequestBuilder(HttpUrl url) {

        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.url(url);

        if (StringUtils.isNotEmpty(apiKey)) {
            requestBuilder.addHeader("X-Redmine-API-Key", apiKey);
        } else {
            requestBuilder.addHeader("Authorization", basicAuth.toAuthorizationValue());
        }

        return requestBuilder;
    }
}
