package com.github.onozaty.redmine.issue.loader;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.github.onozaty.redmine.issue.loader.client.Client;
import com.github.onozaty.redmine.issue.loader.input.Config;
import com.github.onozaty.redmine.issue.loader.input.FieldSetting;
import com.github.onozaty.redmine.issue.loader.input.FieldType;
import com.github.onozaty.redmine.issue.loader.input.IssueId;
import com.github.onozaty.redmine.issue.loader.input.IssueRecord;
import com.github.onozaty.redmine.issue.loader.input.IssueRecords;
import com.github.onozaty.redmine.issue.loader.input.LoadMode;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class IssueLoadRunner {

    private PrintStream out;

    public static void main(String[] args) throws IOException {

        if (args.length != 2) {
            System.err.println("usage: java -jar redmine-issue-loader-all.jar <config file> <csv file>");
            System.exit(1);
        }

        Path configPath = Paths.get(args[0]);
        Path csvPath = Paths.get(args[1]);

        System.out.println("Processing start...");

        IssueLoadRunner issueLoadRunner = new IssueLoadRunner(System.out);
        int loadedCount = issueLoadRunner.execute(configPath, csvPath);

        System.out.println(
                String.format("Processing is completed. %d issues were loaded.", loadedCount));
    }

    public int execute(Path configPath, Path csvPath) throws IOException {

        Config config = Config.of(configPath);

        return execute(config, csvPath);
    }

    public int execute(Config config, Path csvPath) throws IOException {

        validate(config);

        Client client = Client.builder()
                .redmineBaseUrl(config.getReadmineUrl())
                .apiKey(config.getApiKey())
                .basicAuth(config.getBasicAuth())
                .timeout(config.getTimeout())
                .build();

        IssueLoader loader = new IssueLoader(client);

        int issueCount = 0;
        try (IssueRecords issueRecords = IssueRecords.parse(csvPath, config)) {

            for (IssueRecord issueRecord : issueRecords) {

                if (config.getMode() == LoadMode.CREATE) {
                    IssueId issueId = loader.create(issueRecord.getFields());
                    println(String.format("#%d is created.", issueId.getId()));
                } else {
                    IssueId issueId = loader.update(issueRecord.getPrimaryKey(), issueRecord.getFields());
                    println(String.format("#%d is updated.", issueId.getId()));
                }

                issueCount++;
            }
        }

        return issueCount;
    }

    private void validate(Config config) {

        if (config.getMode() == LoadMode.CREATE) {
            // 新規作成

            // プロジェクトID、Subjectは必須
            if (config.getFields().stream().noneMatch(x -> x.getType() == FieldType.PROJECT_ID)
                    || config.getFields().stream().noneMatch(x -> x.getType() == FieldType.SUBJECT)) {
                throw new IllegalArgumentException("Project ID and Subject are required when created.");
            }

        } else {
            // 更新

            long primaryKeyCount = config.getFields().stream()
                    .filter(FieldSetting::isPrimaryKey)
                    .count();

            // プライマリーキーが1件ではない場合はエラー
            if (primaryKeyCount == 0) {
                throw new IllegalArgumentException("Primary key was not found.");
            } else if (primaryKeyCount > 1) {
                throw new IllegalArgumentException("There are multiple primary keys.");
            }

            if (config.getFields().size() == 1) {
                // プライマリーキーしかない
                // -> 更新対象のフィールドが無い
                throw new IllegalArgumentException("The field to be updated is not set.");
            }

            if (config.getFields().stream()
                    .anyMatch(x -> x.getType() == FieldType.ISSUE_ID && !x.isPrimaryKey())) {
                // チケットIDがプライマリキー以外で指定
                throw new IllegalArgumentException("Issue ID can only be used as a primary key.");
            }

            FieldType primaryKeyFieldType = config.getFields().stream()
                    .filter(FieldSetting::isPrimaryKey)
                    .map(FieldSetting::getType)
                    .findFirst()
                    .get();

            if (primaryKeyFieldType != FieldType.ISSUE_ID
                    && primaryKeyFieldType != FieldType.CUSTOM_FIELD) {
                // チケットIDとカスタムフィールド以外はプライマリキーとして使用不可
                throw new IllegalArgumentException(
                        "Field type [" + primaryKeyFieldType + "] can not be used as a primary key.");
            }
        }
    }

    private void println(String message) {
        if (out != null) {
            out.println(message);
        }
    }
}
