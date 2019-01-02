package com.enjoyxstudy.redmine.issue.loader;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.Test;

import com.enjoyxstudy.redmine.issue.loader.client.Client;
import com.enjoyxstudy.redmine.issue.loader.input.CustomField;
import com.enjoyxstudy.redmine.issue.loader.input.FieldType;
import com.enjoyxstudy.redmine.issue.loader.input.IssueId;
import com.enjoyxstudy.redmine.issue.loader.input.IssueTargetFieldsBuilder;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

public class IssueLoaderTest {

    @Test
    public void create_全項目() throws IOException, InterruptedException {

        try (MockWebServer server = new MockWebServer()) {

            server.enqueue(new MockResponse().setBody("{\"issue\":{\"id\":2}}"));

            server.start();

            final String apiKey = "API1234567890";
            Client client = Client.builder()
                    .redmineBaseUrl(server.url("/").toString())
                    .apiKey(apiKey)
                    .build();

            IssueLoader loader = new IssueLoader(client);
            IssueId issueId = loader.create(
                    new IssueTargetFieldsBuilder()
                            .field(FieldType.PROJECT_ID, "1")
                            .field(FieldType.TRACKER_ID, "2")
                            .field(FieldType.STATUS_ID, "3")
                            .field(FieldType.PRIORITY_ID, "4")
                            .field(FieldType.SUBJECT, "タイトル")
                            .field(FieldType.DESCRIPTION, "説明")
                            .field(FieldType.CATEGORY_ID, "5")
                            .field(FieldType.PARENT_ISSUE_ID, "6")
                            .customField(new CustomField(1, "カスタム1"))
                            .build());

            assertThat(issueId).isEqualTo(new IssueId(2));

            assertThat(server.getRequestCount()).isEqualTo(1);

            RecordedRequest request = server.takeRequest();
            assertThat(request.getMethod()).isEqualTo("POST");
            assertThat(request.getHeader("X-Redmine-API-Key")).isEqualTo(apiKey);
            assertThat(request.getPath()).isEqualTo("/issues.json");
            assertThat(request.getBody().readUtf8()).isEqualTo(
                    "{\"issue\":{\"project_id\":\"1\",\"tracker_id\":\"2\",\"status_id\":\"3\",\"priority_id\":\"4\",\"subject\":\"タイトル\",\"description\":\"説明\",\"category_id\":\"5\",\"parent_issue_id\":\"6\",\"custom_fields\":[{\"id\":1,\"value\":\"カスタム1\"}]}}");
        }
    }

    @Test
    public void create_プロジェクトIDとSubject() throws IOException, InterruptedException {

        try (MockWebServer server = new MockWebServer()) {

            server.enqueue(new MockResponse().setBody("{\"issue\":{\"id\":2}}"));

            server.start();

            final String apiKey = "API1234567890";
            Client client = Client.builder()
                    .redmineBaseUrl(server.url("/").toString())
                    .apiKey(apiKey)
                    .build();

            IssueLoader loader = new IssueLoader(client);
            IssueId issueId = loader.create(
                    new IssueTargetFieldsBuilder()
                            .field(FieldType.PROJECT_ID, "1")
                            .field(FieldType.SUBJECT, "タイトル")
                            .build());

            assertThat(issueId).isEqualTo(new IssueId(2));

            assertThat(server.getRequestCount()).isEqualTo(1);

            RecordedRequest request = server.takeRequest();
            assertThat(request.getMethod()).isEqualTo("POST");
            assertThat(request.getHeader("X-Redmine-API-Key")).isEqualTo(apiKey);
            assertThat(request.getPath()).isEqualTo("/issues.json");
            assertThat(request.getBody().readUtf8()).isEqualTo(
                    "{\"issue\":{\"project_id\":\"1\",\"subject\":\"タイトル\"}}");
        }
    }

    @Test
    public void update_カスタムフィールドをキーとしてカスタムフィールド更新() throws IOException, InterruptedException {

        try (MockWebServer server = new MockWebServer()) {

            server.enqueue(new MockResponse().setBody("{\"issues\":[{\"id\":2}]}"));
            server.enqueue(new MockResponse());

            server.start();

            final String apiKey = "API1234567890";
            Client client = Client.builder()
                    .redmineBaseUrl(server.url("/").toString())
                    .apiKey(apiKey)
                    .build();

            IssueLoader loader = new IssueLoader(client);
            loader.update(
                    new CustomField(1, "C"),
                    new IssueTargetFieldsBuilder()
                            .customField(new CustomField(2, "xxx"))
                            .customField(new CustomField(3, "yyy"))
                            .build());

            assertThat(server.getRequestCount()).isEqualTo(2);

            {
                RecordedRequest request = server.takeRequest();
                assertThat(request.getMethod()).isEqualTo("GET");
                assertThat(request.getHeader("X-Redmine-API-Key")).isEqualTo(apiKey);
                assertThat(request.getPath()).isEqualTo("/issues.json?cf_1=C");
            }
            {
                RecordedRequest request = server.takeRequest();
                assertThat(request.getMethod()).isEqualTo("PUT");
                assertThat(request.getHeader("X-Redmine-API-Key")).isEqualTo(apiKey);
                assertThat(request.getPath()).isEqualTo("/issues/2.json");
                assertThat(request.getBody().readUtf8()).isEqualTo(
                        "{\"issue\":{\"custom_fields\":[{\"id\":2,\"value\":\"xxx\"},{\"id\":3,\"value\":\"yyy\"}]}}");
            }
        }
    }

    @Test
    public void update_チケットIDをキーとしてステータス更新() throws IOException, InterruptedException {

        try (MockWebServer server = new MockWebServer()) {

            server.enqueue(new MockResponse().setBody("{\"issues\":[{\"id\":2}]}"));
            server.enqueue(new MockResponse());

            server.start();

            final String apiKey = "API1234567890";
            Client client = Client.builder()
                    .redmineBaseUrl(server.url("/").toString())
                    .apiKey(apiKey)
                    .build();

            IssueLoader loader = new IssueLoader(client);
            loader.update(
                    new IssueId(2),
                    new IssueTargetFieldsBuilder()
                            .field(FieldType.STATUS_ID, "1")
                            .build());

            assertThat(server.getRequestCount()).isEqualTo(2);

            {
                RecordedRequest request = server.takeRequest();
                assertThat(request.getMethod()).isEqualTo("GET");
                assertThat(request.getHeader("X-Redmine-API-Key")).isEqualTo(apiKey);
                assertThat(request.getPath()).isEqualTo("/issues.json?issue_id=2");
            }
            {
                RecordedRequest request = server.takeRequest();
                assertThat(request.getMethod()).isEqualTo("PUT");
                assertThat(request.getHeader("X-Redmine-API-Key")).isEqualTo(apiKey);
                assertThat(request.getPath()).isEqualTo("/issues/2.json");
                assertThat(request.getBody().readUtf8()).isEqualTo("{\"issue\":{\"status_id\":\"1\"}}");
            }
        }
    }

    @Test
    public void update_チケットIDをキーとして全項目更新() throws IOException, InterruptedException {

        try (MockWebServer server = new MockWebServer()) {

            server.enqueue(new MockResponse().setBody("{\"issues\":[{\"id\":2}]}"));
            server.enqueue(new MockResponse());

            server.start();

            final String apiKey = "API1234567890";
            Client client = Client.builder()
                    .redmineBaseUrl(server.url("/").toString())
                    .apiKey(apiKey)
                    .build();

            IssueLoader loader = new IssueLoader(client);
            loader.update(
                    new IssueId(2),
                    new IssueTargetFieldsBuilder()
                            .field(FieldType.PROJECT_ID, "1")
                            .field(FieldType.TRACKER_ID, "2")
                            .field(FieldType.STATUS_ID, "3")
                            .field(FieldType.PRIORITY_ID, "4")
                            .field(FieldType.SUBJECT, "タイトル")
                            .field(FieldType.DESCRIPTION, "説明")
                            .field(FieldType.CATEGORY_ID, "5")
                            .field(FieldType.PARENT_ISSUE_ID, "6")
                            .customField(new CustomField(1, "カスタム1"))
                            .build());

            assertThat(server.getRequestCount()).isEqualTo(2);

            {
                RecordedRequest request = server.takeRequest();
                assertThat(request.getMethod()).isEqualTo("GET");
                assertThat(request.getHeader("X-Redmine-API-Key")).isEqualTo(apiKey);
                assertThat(request.getPath()).isEqualTo("/issues.json?issue_id=2");
            }
            {
                RecordedRequest request = server.takeRequest();
                assertThat(request.getMethod()).isEqualTo("PUT");
                assertThat(request.getHeader("X-Redmine-API-Key")).isEqualTo(apiKey);
                assertThat(request.getPath()).isEqualTo("/issues/2.json");
                assertThat(request.getBody().readUtf8()).isEqualTo(
                        "{\"issue\":{\"project_id\":\"1\",\"tracker_id\":\"2\",\"status_id\":\"3\",\"priority_id\":\"4\",\"subject\":\"タイトル\",\"description\":\"説明\",\"category_id\":\"5\",\"parent_issue_id\":\"6\",\"custom_fields\":[{\"id\":1,\"value\":\"カスタム1\"}]}}");
            }
        }
    }
}
