package com.enjoyxstudy.redmine.issue.updater;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.input.BOMInputStream;

import com.enjoyxstudy.redmine.issue.updater.client.Client;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class IssueUpdateRunner {

    private static final ObjectMapper objectMapper = new ObjectMapper();

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

        Config config = objectMapper.readValue(configPath.toFile(), Config.class);

        long primaryKeyCount = config.getFields().stream()
                .filter(FieldSetting::isPrimaryKey)
                .count();

        // 1件ではない場合はエラー
        if (primaryKeyCount == 0) {
            throw new IllegalStateException("Primary key was not found.");
        } else if (primaryKeyCount > 1) {
            throw new IllegalStateException("There are multiple primary keys.");
        }

        Client client = Client.builder()
                .redmineBaseUrl(config.getReadmineUrl())
                .apiKey(config.getApyKey())
                .build();

        IssueUpdater updater = new IssueUpdater(client);

        int updatedCount = 0;
        try (
                Reader csvReader = new InputStreamReader(
                        // UTF-8のBOMを考慮
                        new BOMInputStream(Files.newInputStream(csvPath)), Charset.forName(config.getCsvEncoding()));
                CSVParser parser = CSVFormat.EXCEL.withHeader().parse(csvReader)) {

            for (CSVRecord csvRecord : parser) {

                IssueRecord issueRecord = toIssueRecord(csvRecord, config);

                IssueId issueId = updater.update(issueRecord.getPrimaryKey(), issueRecord.getIssue());
                println(String.format("#%d is updated.", issueId.getId()));

                updatedCount++;
            }
        }

        return updatedCount;
    }

    private IssueRecord toIssueRecord(CSVRecord csvRecord, Config config) {

        PrimaryKey primaryKey = null;
        Issue.IssueBuilder issueBuilder = Issue.builder();

        for (FieldSetting fieldSetting : config.getFields()) {

            String value = csvRecord.get(fieldSetting.getHeaderName());

            switch (fieldSetting.getType()) {
                case ISSUE_ID:

                    if (!fieldSetting.isPrimaryKey()) {
                        // チケットIDはPKとしてしか使えないので
                        throw new IllegalArgumentException("Issue ID can only be used as a primary key.");
                    }

                    primaryKey = new IssueId(Integer.parseInt(value));
                    break;

                case CUSTOM_FIELD:

                    CustomField customField = new CustomField(fieldSetting.getCustomFieldId(), value);

                    if (fieldSetting.isPrimaryKey()) {
                        primaryKey = customField;
                    } else {
                        issueBuilder.customField(customField);
                    }

                    break;

                default:
                    break;
            }
        }

        return new IssueRecord(primaryKey, issueBuilder.build());
    }

    private void println(String message) {
        if (out != null) {
            out.println(message);
        }
    }
}
