package com.github.onozaty.redmine.issue.loader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;

import org.junit.Test;

import com.github.onozaty.redmine.issue.loader.client.Client;
import com.github.onozaty.redmine.issue.loader.input.BasicAuth;
import com.github.onozaty.redmine.issue.loader.input.CustomField;
import com.github.onozaty.redmine.issue.loader.input.FieldType;
import com.github.onozaty.redmine.issue.loader.input.IssueId;
import com.github.onozaty.redmine.issue.loader.input.IssueTargetFieldsBuilder;

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
            Client client = createClient(server, apiKey);

            IssueLoader loader = new IssueLoader(client);
            IssueId issueId = loader.create(
                    new IssueTargetFieldsBuilder()
                            .field(FieldType.PROJECT_ID, "1")
                            .field(FieldType.TRACKER_ID, "2")
                            .field(FieldType.STATUS_ID, "3")
                            .field(FieldType.PRIORITY_ID, "4")
                            .field(FieldType.ASSIGNED_TO_ID, "5")
                            .field(FieldType.CATEGORY_ID, "6")
                            .field(FieldType.FIXED_VERSION_ID, "7")
                            .field(FieldType.PARENT_ISSUE_ID, "8")
                            .field(FieldType.SUBJECT, "タイトル")
                            .field(FieldType.DESCRIPTION, "説明")
                            .field(FieldType.START_DATE, "2012-12-12")
                            .field(FieldType.DUE_DATE, "2013-01-01")
                            .field(FieldType.DONE_RATIO, "9")
                            .field(FieldType.IS_PRIVATE, "true")
                            .field(FieldType.ESTIMATED_HOURS, "10.5")
                            .customField(new CustomField(1, "カスタム1"))
                            .build());

            assertThat(issueId).isEqualTo(new IssueId(2));

            assertThat(server.getRequestCount()).isEqualTo(1);

            RecordedRequest request = server.takeRequest();
            assertThat(request.getMethod()).isEqualTo("POST");
            assertThat(request.getHeader("X-Redmine-API-Key")).isEqualTo(apiKey);
            assertThat(request.getPath()).isEqualTo("/issues.json");
            assertThat(request.getBody().readUtf8()).isEqualTo(
                    "{\"issue\":{\"project_id\":\"1\",\"tracker_id\":\"2\",\"status_id\":\"3\",\"priority_id\":\"4\",\"assigned_to_id\":\"5\",\"category_id\":\"6\",\"fixed_version_id\":\"7\",\"parent_issue_id\":\"8\",\"subject\":\"タイトル\",\"description\":\"説明\",\"start_date\":\"2012-12-12\",\"due_date\":\"2013-01-01\",\"done_ratio\":\"9\",\"is_private\":\"true\",\"estimated_hours\":\"10.5\",\"custom_fields\":[{\"id\":1,\"value\":\"カスタム1\"}]}}");
        }
    }

    @Test
    public void create_プロジェクトIDとSubject() throws IOException, InterruptedException {

        try (MockWebServer server = new MockWebServer()) {

            server.enqueue(new MockResponse().setBody("{\"issue\":{\"id\":2}}"));

            server.start();

            final String apiKey = "API1234567890";
            Client client = createClient(server, apiKey);

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
    public void create_Basic認証() throws IOException, InterruptedException {

        try (MockWebServer server = new MockWebServer()) {

            server.enqueue(new MockResponse().setBody("{\"issue\":{\"id\":2}}"));

            server.start();

            BasicAuth basicAuth = BasicAuth.builder()
                    .username("user")
                    .password("pass")
                    .build();

            Client client = Client.builder()
                    .redmineBaseUrl(server.url("/").toString())
                    .basicAuth(basicAuth)
                    .timeout(10)
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
            assertThat(request.getHeader("X-Redmine-API-Key")).isNull();
            assertThat(request.getHeader("Authorization")).isEqualTo("Basic dXNlcjpwYXNz");
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
            Client client = createClient(server, apiKey);

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
                assertThat(request.getPath()).isEqualTo("/issues.json?status_id=*&cf_1=C");
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
            Client client = createClient(server, apiKey);

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
                assertThat(request.getPath()).isEqualTo("/issues.json?status_id=*&issue_id=2");
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
            Client client = createClient(server, apiKey);

            IssueLoader loader = new IssueLoader(client);
            loader.update(
                    new IssueId(2),
                    new IssueTargetFieldsBuilder()
                            .field(FieldType.PROJECT_ID, "1")
                            .field(FieldType.TRACKER_ID, "2")
                            .field(FieldType.STATUS_ID, "3")
                            .field(FieldType.PRIORITY_ID, "4")
                            .field(FieldType.ASSIGNED_TO_ID, "5")
                            .field(FieldType.CATEGORY_ID, "6")
                            .field(FieldType.FIXED_VERSION_ID, "7")
                            .field(FieldType.PARENT_ISSUE_ID, "8")
                            .field(FieldType.SUBJECT, "タイトル")
                            .field(FieldType.DESCRIPTION, "説明")
                            .field(FieldType.START_DATE, "2012-12-12")
                            .field(FieldType.DUE_DATE, "2013-01-01")
                            .field(FieldType.DONE_RATIO, "9")
                            .field(FieldType.IS_PRIVATE, "true")
                            .field(FieldType.ESTIMATED_HOURS, "10.5")
                            .customField(new CustomField(1, "カスタム1"))
                            .build());

            assertThat(server.getRequestCount()).isEqualTo(2);

            {
                RecordedRequest request = server.takeRequest();
                assertThat(request.getMethod()).isEqualTo("GET");
                assertThat(request.getHeader("X-Redmine-API-Key")).isEqualTo(apiKey);
                assertThat(request.getPath()).isEqualTo("/issues.json?status_id=*&issue_id=2");
            }
            {
                RecordedRequest request = server.takeRequest();
                assertThat(request.getMethod()).isEqualTo("PUT");
                assertThat(request.getHeader("X-Redmine-API-Key")).isEqualTo(apiKey);
                assertThat(request.getPath()).isEqualTo("/issues/2.json");
                assertThat(request.getBody().readUtf8()).isEqualTo(
                        "{\"issue\":{\"project_id\":\"1\",\"tracker_id\":\"2\",\"status_id\":\"3\",\"priority_id\":\"4\",\"assigned_to_id\":\"5\",\"category_id\":\"6\",\"fixed_version_id\":\"7\",\"parent_issue_id\":\"8\",\"subject\":\"タイトル\",\"description\":\"説明\",\"start_date\":\"2012-12-12\",\"due_date\":\"2013-01-01\",\"done_ratio\":\"9\",\"is_private\":\"true\",\"estimated_hours\":\"10.5\",\"custom_fields\":[{\"id\":1,\"value\":\"カスタム1\"}]}}");
            }
        }
    }

    @Test
    public void update_キーに一致するチケットが0件() throws IOException, InterruptedException {

        try (MockWebServer server = new MockWebServer()) {

            server.enqueue(new MockResponse().setBody("{\"issues\":[]}"));

            server.start();

            final String apiKey = "API1234567890";
            Client client = createClient(server, apiKey);

            IssueLoader loader = new IssueLoader(client);

            // 例外がスローされることを確認
            assertThatThrownBy(() -> {
                loader.update(
                        new CustomField(1, "C"),
                        new IssueTargetFieldsBuilder()
                                .customField(new CustomField(2, "xxx"))
                                .customField(new CustomField(3, "yyy"))
                                .build());
            })
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("The target issue was not found. CustomField(id=1, value=C)");

            assertThat(server.getRequestCount()).isEqualTo(1);

            {
                RecordedRequest request = server.takeRequest();
                assertThat(request.getMethod()).isEqualTo("GET");
                assertThat(request.getHeader("X-Redmine-API-Key")).isEqualTo(apiKey);
                assertThat(request.getPath()).isEqualTo("/issues.json?status_id=*&cf_1=C");
            }
        }
    }

    @Test
    public void update_キーに一致するチケットが複数件() throws IOException, InterruptedException {

        try (MockWebServer server = new MockWebServer()) {

            server.enqueue(new MockResponse().setBody("{\"issues\":[{\"id\":2},{\"id\":3}]}"));

            server.start();

            final String apiKey = "API1234567890";
            Client client = createClient(server, apiKey);

            IssueLoader loader = new IssueLoader(client);

            // 例外がスローされることを確認
            assertThatThrownBy(() -> {
                loader.update(
                        new CustomField(1, "C"),
                        new IssueTargetFieldsBuilder()
                                .customField(new CustomField(2, "xxx"))
                                .customField(new CustomField(3, "yyy"))
                                .build());
            })
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("There are multiple target issue. CustomField(id=1, value=C)");

            assertThat(server.getRequestCount()).isEqualTo(1);

            {
                RecordedRequest request = server.takeRequest();
                assertThat(request.getMethod()).isEqualTo("GET");
                assertThat(request.getHeader("X-Redmine-API-Key")).isEqualTo(apiKey);
                assertThat(request.getPath()).isEqualTo("/issues.json?status_id=*&cf_1=C");
            }
        }
    }

    @Test
    public void create_Redmineとの通信でエラー() throws IOException, InterruptedException {

        try (MockWebServer server = new MockWebServer()) {

            server.enqueue(new MockResponse().setStatus("HTTP/1.1 422 Unprocessable Entity"));

            server.start();

            final String apiKey = "API1234567890";
            Client client = createClient(server, apiKey);

            IssueLoader loader = new IssueLoader(client);

            assertThatThrownBy(() -> {
                loader.create(
                        new IssueTargetFieldsBuilder()
                                .field(FieldType.PROJECT_ID, "1")
                                .field(FieldType.SUBJECT, "タイトル")
                                .build());
            })
                    .isInstanceOf(IOException.class)
                    .hasMessageStartingWith("Failed to call Redmine API.");
        }
    }

    @Test
    public void update_Redmineとの通信でエラー_PKでの検索() throws IOException, InterruptedException {

        try (MockWebServer server = new MockWebServer()) {

            server.enqueue(new MockResponse().setStatus("HTTP/1.1 422 Unprocessable Entity"));

            server.start();

            final String apiKey = "API1234567890";
            Client client = createClient(server, apiKey);

            IssueLoader loader = new IssueLoader(client);

            assertThatThrownBy(() -> {
                loader.update(
                        new CustomField(1, "C"),
                        new IssueTargetFieldsBuilder()
                                .customField(new CustomField(2, "xxx"))
                                .customField(new CustomField(3, "yyy"))
                                .build());
            })
                    .isInstanceOf(IOException.class)
                    .hasMessageStartingWith("Failed to call Redmine API.");
        }
    }

    @Test
    public void update_Redmineとの通信でエラー_更新() throws IOException, InterruptedException {

        try (MockWebServer server = new MockWebServer()) {

            server.enqueue(new MockResponse().setBody("{\"issues\":[{\"id\":2}]}"));
            server.enqueue(new MockResponse().setStatus("HTTP/1.1 422 Unprocessable Entity"));

            server.start();

            final String apiKey = "API1234567890";
            Client client = createClient(server, apiKey);

            IssueLoader loader = new IssueLoader(client);

            assertThatThrownBy(() -> {
                loader.update(
                        new CustomField(1, "C"),
                        new IssueTargetFieldsBuilder()
                                .customField(new CustomField(2, "xxx"))
                                .customField(new CustomField(3, "yyy"))
                                .build());
            })
                    .isInstanceOf(IOException.class)
                    .hasMessageStartingWith("Failed to call Redmine API.");
        }
    }

    private Client createClient(MockWebServer server, final String apiKey) {

        return Client.builder()
                .redmineBaseUrl(server.url("/").toString())
                .apiKey(apiKey)
                .timeout(10)
                .build();
    }

}
