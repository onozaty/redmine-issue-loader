package com.enjoyxstudy.redmine.issue.loader;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import com.enjoyxstudy.redmine.issue.loader.input.Config;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

public class IssueUpdateRunnerTest {

    @Test
    public void execute_チケットIDをキーとして全項目更新() throws URISyntaxException, IOException, InterruptedException {

        try (MockWebServer server = new MockWebServer()) {

            server.enqueue(new MockResponse().setBody("{\"issues\":[{\"id\":1}]}"));
            server.enqueue(new MockResponse());
            server.enqueue(new MockResponse().setBody("{\"issues\":[{\"id\":2}]}"));
            server.enqueue(new MockResponse());
            server.enqueue(new MockResponse().setBody("{\"issues\":[{\"id\":3}]}"));
            server.enqueue(new MockResponse());

            server.start();

            Path configPath = Paths
                    .get(IssueUpdateRunnerTest.class.getResource("config-issue_id-all_fields.json").toURI());
            Config config = Config.of(configPath);

            // Mockに対してリクエスト送信するよう設定
            config.setReadmineUrl(server.url("/").toString());

            Path csvPath = Paths.get(IssueUpdateRunnerTest.class.getResource("issues-all_fields.csv").toURI());

            IssueUpdateRunner runner = new IssueUpdateRunner();
            runner.execute(config, csvPath);

            // 1レコード目
            {
                RecordedRequest request = server.takeRequest();
                assertThat(request.getHeader("X-Redmine-API-Key")).isEqualTo("apikey1234567890");
                assertThat(request.getPath()).isEqualTo("/issues.json?issue_id=1");
            }
            {
                RecordedRequest request = server.takeRequest();
                assertThat(request.getHeader("X-Redmine-API-Key")).isEqualTo("apikey1234567890");
                assertThat(request.getPath()).isEqualTo("/issues/1.json");
                assertThat(request.getBody().readUtf8()).isEqualTo(
                        "{\"issue\":{\"project_id\":\"1\",\"tracker_id\":\"2\",\"status_id\":\"1\",\"priority_id\":\"2\",\"subject\":\"xxx\",\"description\":\"説明1\",\"category_id\":\"2\",\"parent_issue_id\":\"\",\"custom_fields\":[{\"id\":1,\"value\":\"A\"},{\"id\":2,\"value\":\"a\"}]}}");
            }

            // 2レコード目
            {
                RecordedRequest request = server.takeRequest();
                assertThat(request.getHeader("X-Redmine-API-Key")).isEqualTo("apikey1234567890");
                assertThat(request.getPath()).isEqualTo("/issues.json?issue_id=2");
            }
            {
                RecordedRequest request = server.takeRequest();
                assertThat(request.getHeader("X-Redmine-API-Key")).isEqualTo("apikey1234567890");
                assertThat(request.getPath()).isEqualTo("/issues/2.json");
                assertThat(request.getBody().readUtf8()).isEqualTo(
                        "{\"issue\":{\"project_id\":\"2\",\"tracker_id\":\"2\",\"status_id\":\"2\",\"priority_id\":\"1\",\"subject\":\"yyy\",\"description\":\"説明2\",\"category_id\":\"2\",\"parent_issue_id\":\"\",\"custom_fields\":[{\"id\":1,\"value\":\"B\"},{\"id\":2,\"value\":\"b\"}]}}");
            }

            // 3レコード目
            {
                RecordedRequest request = server.takeRequest();
                assertThat(request.getHeader("X-Redmine-API-Key")).isEqualTo("apikey1234567890");
                assertThat(request.getPath()).isEqualTo("/issues.json?issue_id=3");
            }
            {
                RecordedRequest request = server.takeRequest();
                assertThat(request.getHeader("X-Redmine-API-Key")).isEqualTo("apikey1234567890");
                assertThat(request.getPath()).isEqualTo("/issues/3.json");
                assertThat(request.getBody().readUtf8()).isEqualTo(
                        "{\"issue\":{\"project_id\":\"1\",\"tracker_id\":\"3\",\"status_id\":\"3\",\"priority_id\":\"3\",\"subject\":\"zzz\",\"description\":\"説明3\",\"category_id\":\"1\",\"parent_issue_id\":\"1\",\"custom_fields\":[{\"id\":1,\"value\":\"C\"},{\"id\":2,\"value\":\"c\"}]}}");
            }
        }
    }

    @Test
    public void execute_カスタムフィールドをキーとしてステータスIDを更新() throws URISyntaxException, IOException, InterruptedException {

        try (MockWebServer server = new MockWebServer()) {

            server.enqueue(new MockResponse().setBody("{\"issues\":[{\"id\":1}]}"));
            server.enqueue(new MockResponse());
            server.enqueue(new MockResponse().setBody("{\"issues\":[{\"id\":2}]}"));
            server.enqueue(new MockResponse());
            server.enqueue(new MockResponse().setBody("{\"issues\":[{\"id\":3}]}"));
            server.enqueue(new MockResponse());

            server.start();

            Path configPath = Paths
                    .get(IssueUpdateRunnerTest.class.getResource("config-custom_field-status_id.json").toURI());
            Config config = Config.of(configPath);

            // Mockに対してリクエスト送信するよう設定
            config.setReadmineUrl(server.url("/").toString());

            Path csvPath = Paths.get(IssueUpdateRunnerTest.class.getResource("issues-status_id.csv").toURI());

            IssueUpdateRunner runner = new IssueUpdateRunner();
            runner.execute(config, csvPath);

            // 1レコード目
            {
                RecordedRequest request = server.takeRequest();
                assertThat(request.getHeader("X-Redmine-API-Key")).isEqualTo("apikey1234567890");
                assertThat(request.getPath()).isEqualTo("/issues.json?cf_1=A");
            }
            {
                RecordedRequest request = server.takeRequest();
                assertThat(request.getHeader("X-Redmine-API-Key")).isEqualTo("apikey1234567890");
                assertThat(request.getPath()).isEqualTo("/issues/1.json");
                assertThat(request.getBody().readUtf8()).isEqualTo("{\"issue\":{\"status_id\":\"1\"}}");
            }

            // 2レコード目
            {
                RecordedRequest request = server.takeRequest();
                assertThat(request.getHeader("X-Redmine-API-Key")).isEqualTo("apikey1234567890");
                assertThat(request.getPath()).isEqualTo("/issues.json?cf_1=B");
            }
            {
                RecordedRequest request = server.takeRequest();
                assertThat(request.getHeader("X-Redmine-API-Key")).isEqualTo("apikey1234567890");
                assertThat(request.getPath()).isEqualTo("/issues/2.json");
                assertThat(request.getBody().readUtf8()).isEqualTo("{\"issue\":{\"status_id\":\"2\"}}");
            }

            // 3レコード目
            {
                RecordedRequest request = server.takeRequest();
                assertThat(request.getHeader("X-Redmine-API-Key")).isEqualTo("apikey1234567890");
                assertThat(request.getPath()).isEqualTo("/issues.json?cf_1=C");
            }
            {
                RecordedRequest request = server.takeRequest();
                assertThat(request.getHeader("X-Redmine-API-Key")).isEqualTo("apikey1234567890");
                assertThat(request.getPath()).isEqualTo("/issues/3.json");
                assertThat(request.getBody().readUtf8()).isEqualTo("{\"issue\":{\"status_id\":\"3\"}}");
            }
        }
    }
}
