package com.enjoyxstudy.redmine.issue.loader.input;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.input.BOMInputStream;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class IssueRecords implements Iterable<IssueRecord>, Closeable {

    private final Config config;
    private final CSVParser csvParser;

    public static IssueRecords parse(Path csvPath, Config config) throws IOException {

        Reader csvReader = new InputStreamReader(
                // UTF-8のBOMを考慮
                new BOMInputStream(Files.newInputStream(csvPath)), Charset.forName(config.getCsvEncoding()));

        return new IssueRecords(
                config,
                CSVFormat.EXCEL.withHeader().parse(csvReader));
    }

    @Override
    public Iterator<IssueRecord> iterator() {

        Iterator<CSVRecord> csvIterator = csvParser.iterator();

        return new Iterator<IssueRecord>() {

            @Override
            public boolean hasNext() {
                return csvIterator.hasNext();
            }

            @Override
            public IssueRecord next() {

                CSVRecord nextCsvRecord = csvIterator.next();
                if (nextCsvRecord == null) {
                    return null;
                }

                return toIssueRecord(nextCsvRecord);
            }
        };
    }

    @Override
    public void close() throws IOException {
        csvParser.close();
    }

    private IssueRecord toIssueRecord(CSVRecord csvRecord) {

        PrimaryKey primaryKey = null;
        IssueTargetFieldsBuilder targetFieldsBuilder = new IssueTargetFieldsBuilder();

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
                        targetFieldsBuilder.customField(customField);
                    }

                    break;

                default:
                    // その他の項目は更新対象フィールドとして利用

                    if (fieldSetting.isPrimaryKey()) {
                        // PKとしては使えない
                        throw new IllegalArgumentException("Field " + fieldType + " can not be used as a primary key.");
                    }

                    targetFieldsBuilder.field(fieldType, value);
                    break;
            }
        }

        return new IssueRecord(primaryKey, targetFieldsBuilder.build());
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
}
