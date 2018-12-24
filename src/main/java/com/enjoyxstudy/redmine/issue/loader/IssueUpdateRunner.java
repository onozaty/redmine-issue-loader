package com.enjoyxstudy.redmine.issue.loader;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.enjoyxstudy.redmine.issue.loader.client.Client;
import com.enjoyxstudy.redmine.issue.loader.input.Config;
import com.enjoyxstudy.redmine.issue.loader.input.FieldSetting;
import com.enjoyxstudy.redmine.issue.loader.input.IssueId;
import com.enjoyxstudy.redmine.issue.loader.input.IssueRecord;
import com.enjoyxstudy.redmine.issue.loader.input.IssueRecords;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class IssueUpdateRunner {

    private PrintStream out;

    public static void main(String[] args) throws IOException {

        if (args.length != 2) {
            System.err.println("usage: java -jar redmine-issue-updater-all.jar <config file> <csv file>");
            System.exit(1);
        }

        Path configPath = Paths.get(args[0]);
        Path csvPath = Paths.get(args[1]);

        System.out.println("Processing start...");

        IssueUpdateRunner issueUpdateRunner = new IssueUpdateRunner(System.out);
        int updatedCount = issueUpdateRunner.execute(configPath, csvPath);

        System.out.println(
                String.format("Processing is completed. %d issues were updated.", updatedCount));
    }

    public int execute(Path configPath, Path csvPath) throws IOException {

        Config config = Config.of(configPath);

        return execute(config, csvPath);
    }

    public int execute(Config config, Path csvPath) throws IOException {

        long primaryKeyCount = config.getFields().stream()
                .filter(FieldSetting::isPrimaryKey)
                .count();

        // 1件ではない場合はエラー
        if (primaryKeyCount == 0) {
            throw new IllegalArgumentException("Primary key was not found.");
        } else if (primaryKeyCount > 1) {
            throw new IllegalArgumentException("There are multiple primary keys.");
        }

        if (config.getFields().size() == 1) {
            // Primary keyしかない
            // -> 更新対象のフィールドが無い
            throw new IllegalArgumentException("The field to be updated is not set.");
        }

        Client client = Client.builder()
                .redmineBaseUrl(config.getReadmineUrl())
                .apiKey(config.getApyKey())
                .build();

        IssueUpdater updater = new IssueUpdater(client);

        int updatedCount = 0;
        try (IssueRecords issueRecords = IssueRecords.parse(csvPath, config)) {

            for (IssueRecord issueRecord : issueRecords) {

                IssueId issueId = updater.update(issueRecord.getPrimaryKey(), issueRecord.getFields());
                println(String.format("#%d is updated.", issueId.getId()));

                updatedCount++;
            }
        }

        return updatedCount;
    }

    private void println(String message) {
        if (out != null) {
            out.println(message);
        }
    }
}
