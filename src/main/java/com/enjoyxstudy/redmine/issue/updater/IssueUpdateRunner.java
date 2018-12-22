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
        try (
                Reader csvReader = new InputStreamReader(
                        // UTF-8のBOMを考慮
                        new BOMInputStream(Files.newInputStream(csvPath)), Charset.forName(config.getCsvEncoding()));
                CSVParser parser = CSVFormat.EXCEL.withHeader().parse(csvReader)) {

            for (CSVRecord csvRecord : parser) {

                IssueRecord issueRecord = toIssueRecord(csvRecord, config);

                IssueId issueId = updater.update(issueRecord.getPrimaryKey(), issueRecord.getUpdateTargetFields());
                println(String.format("#%d is updated.", issueId.getId()));

                updatedCount++;
            }
        }

        return updatedCount;
    }

    private IssueRecord toIssueRecord(CSVRecord csvRecord, Config config) {

        PrimaryKey primaryKey = null;
        IssueUpdateTargetFieldsBuilder updateTargetFieldsBuilder = new IssueUpdateTargetFieldsBuilder();

        for (FieldSetting fieldSetting : config.getFields()) {

            String value = convertValue(csvRecord.get(fieldSetting.getHeaderName()), fieldSetting);

            FieldType fieldType = fieldSetting.getType();
            switch (fieldType) {
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
                        updateTargetFieldsBuilder.customField(customField);
                    }

                    break;

                default:
                    // その他の項目は更新対象フィールドとして利用

                    if (fieldSetting.isPrimaryKey()) {
                        // PKとしては使えない
                        throw new IllegalArgumentException("Field " + fieldType + " can not be used as a primary key.");
                    }

                    updateTargetFieldsBuilder.field(fieldType, value);
                    break;
            }
        }

        return new IssueRecord(primaryKey, updateTargetFieldsBuilder.build());
    }

    private String convertValue(String value, FieldSetting fieldSetting) {

        if (value.isEmpty() || fieldSetting.getMappings() == null) {
            return value;
        }

        // 変換表がある場合、CSVから取り出した値を変換
        String convertedValue = fieldSetting.getMappings().get(value);

        if (convertedValue == null) {
            // 一致するものが無い場合エラー
            throw new IllegalArgumentException(
                    String.format(
                            "Could not mapping %s of field %s.",
                            value,
                            fieldSetting.getHeaderName()));
        }

        return convertedValue;
    }

    private void println(String message) {
        if (out != null) {
            out.println(message);
        }
    }
}
