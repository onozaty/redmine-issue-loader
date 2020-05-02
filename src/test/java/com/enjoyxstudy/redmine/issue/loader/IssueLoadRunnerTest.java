package com.enjoyxstudy.redmine.issue.loader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.enjoyxstudy.redmine.issue.loader.input.Config;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

public class IssueLoadRunnerTest {

    @Test
    public void execute_新規作成_全項目() throws URISyntaxException, IOException, InterruptedException {

        try (MockWebServer server = new MockWebServer()) {

            server.enqueue(new MockResponse().setBody("{\"issue\":{\"id\":1}}"));
            server.enqueue(new MockResponse().setBody("{\"issue\":{\"id\":2}}"));
            server.enqueue(new MockResponse().setBody("{\"issue\":{\"id\":3}}"));

            server.start();

            Path configPath = Paths.get(IssueLoadRunnerTest.class.getResource("create-all_fields.json").toURI());
            Config config = Config.of(configPath);

            // Mockに対してリクエスト送信するよう設定
            config.setReadmineUrl(server.url("/").toString());

            Path csvPath = Paths.get(IssueLoadRunnerTest.class.getResource("issues-all_fields.csv").toURI());

            IssueLoadRunner runner = new IssueLoadRunner(System.out);
            runner.execute(config, csvPath);

            assertThat(server.getRequestCount()).isEqualTo(3);

            // 1レコード目
            {
                RecordedRequest request = server.takeRequest();
                assertThat(request.getMethod()).isEqualTo("POST");
                assertThat(request.getHeader("X-Redmine-API-Key")).isEqualTo("apikey1234567890");
                assertThat(request.getPath()).isEqualTo("/issues.json");
                assertThat(request.getBody().readUtf8()).isEqualTo(
                        "{\"issue\":{\"project_id\":\"1\",\"tracker_id\":\"2\",\"status_id\":\"1\",\"priority_id\":\"2\",\"assigned_to_id\":\"5\",\"category_id\":\"2\",\"fixed_version_id\":\"2\",\"parent_issue_id\":\"\",\"subject\":\"xxx\",\"description\":\"説明1\",\"start_date\":\"2019-02-01\",\"due_date\":\"2019-02-20\",\"done_ratio\":\"10\",\"is_private\":\"true\",\"estimated_hours\":\"2.5\",\"custom_fields\":[{\"id\":1,\"value\":\"A\"},{\"id\":2,\"value\":\"a\"}]}}");
            }

            // 2レコード目
            {
                RecordedRequest request = server.takeRequest();
                assertThat(request.getMethod()).isEqualTo("POST");
                assertThat(request.getHeader("X-Redmine-API-Key")).isEqualTo("apikey1234567890");
                assertThat(request.getPath()).isEqualTo("/issues.json");
                assertThat(request.getBody().readUtf8()).isEqualTo(
                        "{\"issue\":{\"project_id\":\"2\",\"tracker_id\":\"2\",\"status_id\":\"2\",\"priority_id\":\"1\",\"assigned_to_id\":\"\",\"category_id\":\"2\",\"fixed_version_id\":\"\",\"parent_issue_id\":\"\",\"subject\":\"yyy\",\"description\":\"説明2\",\"start_date\":\"2019-03-02\",\"due_date\":\"\",\"done_ratio\":\"\",\"is_private\":\"false\",\"estimated_hours\":\"\",\"custom_fields\":[{\"id\":1,\"value\":\"B\"},{\"id\":2,\"value\":\"b\"}]}}");
            }

            // 3レコード目
            {
                RecordedRequest request = server.takeRequest();
                assertThat(request.getMethod()).isEqualTo("POST");
                assertThat(request.getHeader("X-Redmine-API-Key")).isEqualTo("apikey1234567890");
                assertThat(request.getPath()).isEqualTo("/issues.json");
                assertThat(request.getBody().readUtf8()).isEqualTo(
                        "{\"issue\":{\"project_id\":\"1\",\"tracker_id\":\"3\",\"status_id\":\"3\",\"priority_id\":\"3\",\"assigned_to_id\":\"6\",\"category_id\":\"1\",\"fixed_version_id\":\"1\",\"parent_issue_id\":\"1\",\"subject\":\"zzz\",\"description\":\"説明3\",\"start_date\":\"2019-03-12\",\"due_date\":\"2019-10-30\",\"done_ratio\":\"90\",\"is_private\":\"false\",\"estimated_hours\":\"10\",\"custom_fields\":[{\"id\":1,\"value\":\"C\"},{\"id\":2,\"value\":\"c\"}]}}");
            }
        }
    }

    @Test
    public void execute_新規作成_プロジェクトIDとSubject() throws URISyntaxException, IOException, InterruptedException {

        try (MockWebServer server = new MockWebServer()) {

            server.enqueue(new MockResponse().setBody("{\"issue\":{\"id\":1}}"));
            server.enqueue(new MockResponse().setBody("{\"issue\":{\"id\":2}}"));

            server.start();

            Path configPath = Paths
                    .get(IssueLoadRunnerTest.class.getResource("create-project_id-subject.json").toURI());
            Config config = Config.of(configPath);

            // Mockに対してリクエスト送信するよう設定
            config.setReadmineUrl(server.url("/").toString());

            Path csvPath = Paths.get(IssueLoadRunnerTest.class.getResource("issues-project_id-subject.csv").toURI());

            IssueLoadRunner runner = new IssueLoadRunner();
            runner.execute(config, csvPath);

            assertThat(server.getRequestCount()).isEqualTo(2);

            // 1レコード目
            {
                RecordedRequest request = server.takeRequest();
                assertThat(request.getMethod()).isEqualTo("POST");
                assertThat(request.getHeader("X-Redmine-API-Key")).isEqualTo("apikey1234567890");
                assertThat(request.getPath()).isEqualTo("/issues.json");
                assertThat(request.getBody().readUtf8()).isEqualTo(
                        "{\"issue\":{\"project_id\":\"1\",\"subject\":\"タイトル1\"}}");
            }

            // 2レコード目
            {
                RecordedRequest request = server.takeRequest();
                assertThat(request.getMethod()).isEqualTo("POST");
                assertThat(request.getHeader("X-Redmine-API-Key")).isEqualTo("apikey1234567890");
                assertThat(request.getPath()).isEqualTo("/issues.json");
                assertThat(request.getBody().readUtf8()).isEqualTo(
                        "{\"issue\":{\"project_id\":\"2\",\"subject\":\"タイトル2\"}}");
            }
        }
    }

    @Test
    public void execute_Basic認証() throws URISyntaxException, IOException, InterruptedException {

        try (MockWebServer server = new MockWebServer()) {

            server.enqueue(new MockResponse().setBody("{\"issue\":{\"id\":1}}"));
            server.enqueue(new MockResponse().setBody("{\"issue\":{\"id\":2}}"));

            server.start();

            Path configPath = Paths
                    .get(IssueLoadRunnerTest.class.getResource("basic-auth.json").toURI());
            Config config = Config.of(configPath);

            // Mockに対してリクエスト送信するよう設定
            config.setReadmineUrl(server.url("/").toString());

            Path csvPath = Paths.get(IssueLoadRunnerTest.class.getResource("issues-project_id-subject.csv").toURI());

            IssueLoadRunner runner = new IssueLoadRunner();
            runner.execute(config, csvPath);

            assertThat(server.getRequestCount()).isEqualTo(2);

            // 1レコード目
            {
                RecordedRequest request = server.takeRequest();
                assertThat(request.getMethod()).isEqualTo("POST");
                assertThat(request.getHeader("X-Redmine-API-Key")).isNull();
                assertThat(request.getHeader("Authorization")).isEqualTo("Basic dXNlcjpwYXNz");
                assertThat(request.getPath()).isEqualTo("/issues.json");
                assertThat(request.getBody().readUtf8()).isEqualTo(
                        "{\"issue\":{\"project_id\":\"1\",\"subject\":\"タイトル1\"}}");
            }

            // 2レコード目
            {
                RecordedRequest request = server.takeRequest();
                assertThat(request.getMethod()).isEqualTo("POST");
                assertThat(request.getHeader("X-Redmine-API-Key")).isNull();
                assertThat(request.getHeader("Authorization")).isEqualTo("Basic dXNlcjpwYXNz");
                assertThat(request.getPath()).isEqualTo("/issues.json");
                assertThat(request.getBody().readUtf8()).isEqualTo(
                        "{\"issue\":{\"project_id\":\"2\",\"subject\":\"タイトル2\"}}");
            }
        }
    }

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
                    .get(IssueLoadRunnerTest.class.getResource("update-all_fields-with-issue_id.json").toURI());
            Config config = Config.of(configPath);

            // Mockに対してリクエスト送信するよう設定
            config.setReadmineUrl(server.url("/").toString());

            Path csvPath = Paths.get(IssueLoadRunnerTest.class.getResource("issues-all_fields.csv").toURI());

            IssueLoadRunner runner = new IssueLoadRunner();
            runner.execute(config, csvPath);

            assertThat(server.getRequestCount()).isEqualTo(6);

            // 1レコード目
            {
                RecordedRequest request = server.takeRequest();
                assertThat(request.getMethod()).isEqualTo("GET");
                assertThat(request.getHeader("X-Redmine-API-Key")).isEqualTo("apikey1234567890");
                assertThat(request.getPath()).isEqualTo("/issues.json?status_id=*&issue_id=1");
            }
            {
                RecordedRequest request = server.takeRequest();
                assertThat(request.getMethod()).isEqualTo("PUT");
                assertThat(request.getHeader("X-Redmine-API-Key")).isEqualTo("apikey1234567890");
                assertThat(request.getPath()).isEqualTo("/issues/1.json");
                assertThat(request.getBody().readUtf8()).isEqualTo(
                        "{\"issue\":{\"project_id\":\"1\",\"tracker_id\":\"2\",\"status_id\":\"1\",\"priority_id\":\"2\",\"assigned_to_id\":\"5\",\"category_id\":\"2\",\"fixed_version_id\":\"2\",\"parent_issue_id\":\"\",\"subject\":\"xxx\",\"description\":\"説明1\",\"start_date\":\"2019-02-01\",\"due_date\":\"2019-02-20\",\"done_ratio\":\"10\",\"is_private\":\"true\",\"estimated_hours\":\"2.5\",\"custom_fields\":[{\"id\":1,\"value\":\"A\"},{\"id\":2,\"value\":\"a\"}]}}");
            }

            // 2レコード目
            {
                RecordedRequest request = server.takeRequest();
                assertThat(request.getMethod()).isEqualTo("GET");
                assertThat(request.getHeader("X-Redmine-API-Key")).isEqualTo("apikey1234567890");
                assertThat(request.getPath()).isEqualTo("/issues.json?status_id=*&issue_id=2");
            }
            {
                RecordedRequest request = server.takeRequest();
                assertThat(request.getMethod()).isEqualTo("PUT");
                assertThat(request.getHeader("X-Redmine-API-Key")).isEqualTo("apikey1234567890");
                assertThat(request.getPath()).isEqualTo("/issues/2.json");
                assertThat(request.getBody().readUtf8()).isEqualTo(
                        "{\"issue\":{\"project_id\":\"2\",\"tracker_id\":\"2\",\"status_id\":\"2\",\"priority_id\":\"1\",\"assigned_to_id\":\"\",\"category_id\":\"2\",\"fixed_version_id\":\"\",\"parent_issue_id\":\"\",\"subject\":\"yyy\",\"description\":\"説明2\",\"start_date\":\"2019-03-02\",\"due_date\":\"\",\"done_ratio\":\"\",\"is_private\":\"false\",\"estimated_hours\":\"\",\"custom_fields\":[{\"id\":1,\"value\":\"B\"},{\"id\":2,\"value\":\"b\"}]}}");
            }

            // 3レコード目
            {
                RecordedRequest request = server.takeRequest();
                assertThat(request.getMethod()).isEqualTo("GET");
                assertThat(request.getHeader("X-Redmine-API-Key")).isEqualTo("apikey1234567890");
                assertThat(request.getPath()).isEqualTo("/issues.json?status_id=*&issue_id=3");
            }
            {
                RecordedRequest request = server.takeRequest();
                assertThat(request.getMethod()).isEqualTo("PUT");
                assertThat(request.getHeader("X-Redmine-API-Key")).isEqualTo("apikey1234567890");
                assertThat(request.getPath()).isEqualTo("/issues/3.json");
                assertThat(request.getBody().readUtf8()).isEqualTo(
                        "{\"issue\":{\"project_id\":\"1\",\"tracker_id\":\"3\",\"status_id\":\"3\",\"priority_id\":\"3\",\"assigned_to_id\":\"6\",\"category_id\":\"1\",\"fixed_version_id\":\"1\",\"parent_issue_id\":\"1\",\"subject\":\"zzz\",\"description\":\"説明3\",\"start_date\":\"2019-03-12\",\"due_date\":\"2019-10-30\",\"done_ratio\":\"90\",\"is_private\":\"false\",\"estimated_hours\":\"10\",\"custom_fields\":[{\"id\":1,\"value\":\"C\"},{\"id\":2,\"value\":\"c\"}]}}");
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
                    .get(IssueLoadRunnerTest.class.getResource("update-status_id-with-custom_field.json").toURI());
            Config config = Config.of(configPath);

            // Mockに対してリクエスト送信するよう設定
            config.setReadmineUrl(server.url("/").toString());

            Path csvPath = Paths.get(IssueLoadRunnerTest.class.getResource("issues-status_id.csv").toURI());

            IssueLoadRunner runner = new IssueLoadRunner();
            runner.execute(config, csvPath);

            assertThat(server.getRequestCount()).isEqualTo(6);

            // 1レコード目
            {
                RecordedRequest request = server.takeRequest();
                assertThat(request.getMethod()).isEqualTo("GET");
                assertThat(request.getHeader("X-Redmine-API-Key")).isEqualTo("apikey1234567890");
                assertThat(request.getPath()).isEqualTo("/issues.json?status_id=*&cf_1=A");
            }
            {
                RecordedRequest request = server.takeRequest();
                assertThat(request.getMethod()).isEqualTo("PUT");
                assertThat(request.getHeader("X-Redmine-API-Key")).isEqualTo("apikey1234567890");
                assertThat(request.getPath()).isEqualTo("/issues/1.json");
                assertThat(request.getBody().readUtf8()).isEqualTo("{\"issue\":{\"status_id\":\"1\"}}");
            }

            // 2レコード目
            {
                RecordedRequest request = server.takeRequest();
                assertThat(request.getMethod()).isEqualTo("GET");
                assertThat(request.getHeader("X-Redmine-API-Key")).isEqualTo("apikey1234567890");
                assertThat(request.getPath()).isEqualTo("/issues.json?status_id=*&cf_1=B");
            }
            {
                RecordedRequest request = server.takeRequest();
                assertThat(request.getMethod()).isEqualTo("PUT");
                assertThat(request.getHeader("X-Redmine-API-Key")).isEqualTo("apikey1234567890");
                assertThat(request.getPath()).isEqualTo("/issues/2.json");
                assertThat(request.getBody().readUtf8()).isEqualTo("{\"issue\":{\"status_id\":\"2\"}}");
            }

            // 3レコード目
            {
                RecordedRequest request = server.takeRequest();
                assertThat(request.getMethod()).isEqualTo("GET");
                assertThat(request.getHeader("X-Redmine-API-Key")).isEqualTo("apikey1234567890");
                assertThat(request.getPath()).isEqualTo("/issues.json?status_id=*&cf_1=C");
            }
            {
                RecordedRequest request = server.takeRequest();
                assertThat(request.getMethod()).isEqualTo("PUT");
                assertThat(request.getHeader("X-Redmine-API-Key")).isEqualTo("apikey1234567890");
                assertThat(request.getPath()).isEqualTo("/issues/3.json");
                assertThat(request.getBody().readUtf8()).isEqualTo("{\"issue\":{\"status_id\":\"3\"}}");
            }
        }
    }

    @Test
    public void execute_新規作成_プロジェクトIDなし() throws URISyntaxException, IOException, InterruptedException {

        try (MockWebServer server = new MockWebServer()) {

            Path configPath = Paths.get(IssueLoadRunnerTest.class.getResource("create-none-project_id.json").toURI());
            Config config = Config.of(configPath);

            Path csvPath = Paths.get(IssueLoadRunnerTest.class.getResource("issues-all_fields.csv").toURI());

            IssueLoadRunner runner = new IssueLoadRunner();

            // 例外がスローされることを確認
            assertThatThrownBy(() -> runner.execute(config, csvPath))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Project ID and Subject are required when created.");
        }
    }

    @Test
    public void execute_新規作成_題名無し() throws URISyntaxException, IOException, InterruptedException {

        try (MockWebServer server = new MockWebServer()) {

            Path configPath = Paths.get(IssueLoadRunnerTest.class.getResource("create-none-subject.json").toURI());
            Config config = Config.of(configPath);

            Path csvPath = Paths.get(IssueLoadRunnerTest.class.getResource("issues-all_fields.csv").toURI());

            IssueLoadRunner runner = new IssueLoadRunner();

            // 例外がスローされることを確認
            assertThatThrownBy(() -> runner.execute(config, csvPath))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Project ID and Subject are required when created.");
        }
    }

    @Test
    public void execute_更新_PK無し() throws URISyntaxException, IOException, InterruptedException {

        try (MockWebServer server = new MockWebServer()) {

            Path configPath = Paths.get(IssueLoadRunnerTest.class.getResource("update-none-pk.json").toURI());
            Config config = Config.of(configPath);

            Path csvPath = Paths.get(IssueLoadRunnerTest.class.getResource("issues-all_fields.csv").toURI());

            IssueLoadRunner runner = new IssueLoadRunner();

            // 例外がスローされることを確認
            assertThatThrownBy(() -> runner.execute(config, csvPath))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Primary key was not found.");
        }
    }

    @Test
    public void execute_更新_PK複数() throws URISyntaxException, IOException, InterruptedException {

        try (MockWebServer server = new MockWebServer()) {

            Path configPath = Paths.get(IssueLoadRunnerTest.class.getResource("update-multi-pk.json").toURI());
            Config config = Config.of(configPath);

            Path csvPath = Paths.get(IssueLoadRunnerTest.class.getResource("issues-all_fields.csv").toURI());

            IssueLoadRunner runner = new IssueLoadRunner();

            // 例外がスローされることを確認
            assertThatThrownBy(() -> runner.execute(config, csvPath))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("There are multiple primary keys.");
        }
    }

    @Test
    public void execute_更新_PK以外のフィールド指定無し() throws URISyntaxException, IOException, InterruptedException {

        try (MockWebServer server = new MockWebServer()) {

            Path configPath = Paths.get(IssueLoadRunnerTest.class.getResource("update-pk-only.json").toURI());
            Config config = Config.of(configPath);

            Path csvPath = Paths.get(IssueLoadRunnerTest.class.getResource("issues-all_fields.csv").toURI());

            IssueLoadRunner runner = new IssueLoadRunner();

            // 例外がスローされることを確認
            assertThatThrownBy(() -> runner.execute(config, csvPath))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("The field to be updated is not set.");
        }
    }

    @Test
    public void execute_更新_チケットIDを更新() throws URISyntaxException, IOException, InterruptedException {

        try (MockWebServer server = new MockWebServer()) {

            Path configPath = Paths.get(IssueLoadRunnerTest.class.getResource("update-issue_id.json").toURI());
            Config config = Config.of(configPath);

            Path csvPath = Paths.get(IssueLoadRunnerTest.class.getResource("issues-all_fields.csv").toURI());

            IssueLoadRunner runner = new IssueLoadRunner();

            // 例外がスローされることを確認
            assertThatThrownBy(() -> runner.execute(config, csvPath))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Issue ID can only be used as a primary key.");
        }
    }

    @Test
    public void execute_更新_PKとしてチケットIDとカスタムフィールド以外指定() throws URISyntaxException, IOException, InterruptedException {

        try (MockWebServer server = new MockWebServer()) {

            Path configPath = Paths.get(IssueLoadRunnerTest.class.getResource("update-with-subject.json").toURI());
            Config config = Config.of(configPath);

            Path csvPath = Paths.get(IssueLoadRunnerTest.class.getResource("issues-all_fields.csv").toURI());

            IssueLoadRunner runner = new IssueLoadRunner();

            // 例外がスローされることを確認
            assertThatThrownBy(() -> runner.execute(config, csvPath))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Field type [SUBJECT] can not be used as a primary key.");
        }
    }

    @Test
    public void execute_マッピング表に一致するものが無い() throws URISyntaxException, IOException, InterruptedException {

        try (MockWebServer server = new MockWebServer()) {

            Path configPath = Paths.get(IssueLoadRunnerTest.class.getResource("mapping-unmatch.json").toURI());
            Config config = Config.of(configPath);

            Path csvPath = Paths.get(IssueLoadRunnerTest.class.getResource("issues-all_fields.csv").toURI());

            IssueLoadRunner runner = new IssueLoadRunner();

            // 例外がスローされることを確認
            assertThatThrownBy(() -> runner.execute(config, csvPath))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Could not mapping \"プロジェクト1\" of field [Project].");
        }
    }

    @Test
    public void execute_正規化() throws URISyntaxException, IOException, InterruptedException {

        try (MockWebServer server = new MockWebServer()) {

            server.enqueue(new MockResponse().setBody("{\"issue\":{\"id\":1}}"));
            server.enqueue(new MockResponse().setBody("{\"issue\":{\"id\":2}}"));
            server.enqueue(new MockResponse().setBody("{\"issue\":{\"id\":3}}"));
            server.enqueue(new MockResponse().setBody("{\"issue\":{\"id\":4}}"));

            server.start();

            Path configPath = Paths.get(IssueLoadRunnerTest.class.getResource("create-normalize.json").toURI());
            Config config = Config.of(configPath);

            // Mockに対してリクエスト送信するよう設定
            config.setReadmineUrl(server.url("/").toString());

            Path csvPath = Paths.get(IssueLoadRunnerTest.class.getResource("issues-normalize.csv").toURI());

            IssueLoadRunner runner = new IssueLoadRunner(System.out);
            runner.execute(config, csvPath);

            assertThat(server.getRequestCount()).isEqualTo(4);

            // 1レコード目
            {
                RecordedRequest request = server.takeRequest();
                assertThat(request.getMethod()).isEqualTo("POST");
                assertThat(request.getHeader("X-Redmine-API-Key")).isEqualTo("apikey1234567890");
                assertThat(request.getPath()).isEqualTo("/issues.json");
                assertThat(request.getBody().readUtf8()).isEqualTo(
                        "{\"issue\":{\"project_id\":\"1\",\"subject\":\"ハイフン、0埋めあり\",\"start_date\":\"2012-01-01\",\"due_date\":\"2012-03-01\",\"is_private\":\"true\"}}");
            }

            // 2レコード目
            {
                RecordedRequest request = server.takeRequest();
                assertThat(request.getMethod()).isEqualTo("POST");
                assertThat(request.getHeader("X-Redmine-API-Key")).isEqualTo("apikey1234567890");
                assertThat(request.getPath()).isEqualTo("/issues.json");
                assertThat(request.getBody().readUtf8()).isEqualTo(
                        "{\"issue\":{\"project_id\":\"1\",\"subject\":\"ハイフン、0埋め無し\",\"start_date\":\"2012-01-02\",\"due_date\":\"2012-03-02\",\"is_private\":\"false\"}}");
            }

            // 3レコード目
            {
                RecordedRequest request = server.takeRequest();
                assertThat(request.getMethod()).isEqualTo("POST");
                assertThat(request.getHeader("X-Redmine-API-Key")).isEqualTo("apikey1234567890");
                assertThat(request.getPath()).isEqualTo("/issues.json");
                assertThat(request.getBody().readUtf8()).isEqualTo(
                        "{\"issue\":{\"project_id\":\"1\",\"subject\":\"スラッシュ、0埋めあり\",\"start_date\":\"2012-02-01\",\"due_date\":\"2012-04-01\",\"is_private\":\"true\"}}");
            }

            // 4レコード目
            {
                RecordedRequest request = server.takeRequest();
                assertThat(request.getMethod()).isEqualTo("POST");
                assertThat(request.getHeader("X-Redmine-API-Key")).isEqualTo("apikey1234567890");
                assertThat(request.getPath()).isEqualTo("/issues.json");
                assertThat(request.getBody().readUtf8()).isEqualTo(
                        "{\"issue\":{\"project_id\":\"1\",\"subject\":\"スラッシュ、0埋め無し\",\"start_date\":\"2012-02-02\",\"due_date\":\"2012-04-02\",\"is_private\":\"false\"}}");
            }
        }
    }

    @Test
    public void execute_タイムアウト設定_デフォルト() throws URISyntaxException, IOException, InterruptedException {

        try (MockWebServer server = new MockWebServer()) {

            // わざと15秒遅らせる(デフォルト10秒なのでタイムアウト発生)
            server.enqueue(new MockResponse().setBody("{\"issue\":{\"id\":1}}").setBodyDelay(15, TimeUnit.SECONDS));
            server.enqueue(new MockResponse().setBody("{\"issue\":{\"id\":2}}"));

            server.start();

            Path configPath = Paths
                    .get(IssueLoadRunnerTest.class.getResource("create-project_id-subject.json").toURI());
            Config config = Config.of(configPath);

            // Mockに対してリクエスト送信するよう設定
            config.setReadmineUrl(server.url("/").toString());

            Path csvPath = Paths.get(IssueLoadRunnerTest.class.getResource("issues-project_id-subject.csv").toURI());

            IssueLoadRunner runner = new IssueLoadRunner();

            // 例外がスローされることを確認
            assertThatThrownBy(() -> runner.execute(config, csvPath))
                    .isInstanceOf(SocketTimeoutException.class);
        }
    }

    @Test
    public void execute_タイムアウト設定_デフォルトから変更() throws URISyntaxException, IOException, InterruptedException {

        try (MockWebServer server = new MockWebServer()) {

            // わざと15秒遅らせる(設定ファイルでデフォルト10秒のものを20秒に変えているのでタイムアウトしない)
            server.enqueue(new MockResponse().setBody("{\"issue\":{\"id\":1}}").setBodyDelay(15, TimeUnit.SECONDS));
            server.enqueue(new MockResponse().setBody("{\"issue\":{\"id\":2}}"));

            server.start();

            Path configPath = Paths
                    .get(IssueLoadRunnerTest.class.getResource("timeout.json").toURI());
            Config config = Config.of(configPath);

            // Mockに対してリクエスト送信するよう設定
            config.setReadmineUrl(server.url("/").toString());

            Path csvPath = Paths.get(IssueLoadRunnerTest.class.getResource("issues-project_id-subject.csv").toURI());

            IssueLoadRunner runner = new IssueLoadRunner();
            runner.execute(config, csvPath);

            assertThat(server.getRequestCount()).isEqualTo(2);

            // 1レコード目
            {
                RecordedRequest request = server.takeRequest();
                assertThat(request.getMethod()).isEqualTo("POST");
                assertThat(request.getHeader("X-Redmine-API-Key")).isEqualTo("apikey1234567890");
                assertThat(request.getHeader("Authorization")).isNull();
                assertThat(request.getPath()).isEqualTo("/issues.json");
                assertThat(request.getBody().readUtf8()).isEqualTo(
                        "{\"issue\":{\"project_id\":\"1\",\"subject\":\"タイトル1\"}}");
            }

            // 2レコード目
            {
                RecordedRequest request = server.takeRequest();
                assertThat(request.getMethod()).isEqualTo("POST");
                assertThat(request.getHeader("X-Redmine-API-Key")).isEqualTo("apikey1234567890");
                assertThat(request.getHeader("Authorization")).isNull();
                assertThat(request.getPath()).isEqualTo("/issues.json");
                assertThat(request.getBody().readUtf8()).isEqualTo(
                        "{\"issue\":{\"project_id\":\"2\",\"subject\":\"タイトル2\"}}");
            }
        }
    }

    @Test
    public void execute_文字置換() throws URISyntaxException, IOException, InterruptedException {

        try (MockWebServer server = new MockWebServer()) {

            server.enqueue(new MockResponse().setBody("{\"issue\":{\"id\":1}}"));

            server.start();

            Path configPath = Paths.get(IssueLoadRunnerTest.class.getResource("replace.json").toURI());
            Config config = Config.of(configPath);

            // Mockに対してリクエスト送信するよう設定
            config.setReadmineUrl(server.url("/").toString());

            Path csvPath = Paths.get(IssueLoadRunnerTest.class.getResource("replace.csv").toURI());

            IssueLoadRunner runner = new IssueLoadRunner();
            runner.execute(config, csvPath);

            assertThat(server.getRequestCount()).isEqualTo(1);

            // 1レコード目
            {
                RecordedRequest request = server.takeRequest();
                assertThat(request.getMethod()).isEqualTo("POST");
                assertThat(request.getHeader("X-Redmine-API-Key")).isEqualTo("apikey1234567890");
                assertThat(request.getPath()).isEqualTo("/issues.json");
                assertThat(request.getBody().readUtf8()).isEqualTo(
                        "{\"issue\":{\"project_id\":\"1\",\"subject\":\"_田\",\"description\":\"絵文字___\"}}");
            }
        }
    }
}
