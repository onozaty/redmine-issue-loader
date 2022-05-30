package com.github.onozaty.redmine.issue.loader.input;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang3.StringUtils;

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
                CSVFormat.EXCEL.builder()
                        .setHeader()
                        .build()
                        .parse(csvReader));
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

            String value = csvRecord.get(fieldSetting.getHeaderName());

            FieldType fieldType = fieldSetting.getType();
            switch (fieldType) {
                case ISSUE_ID:

                    value = convertValue(value, fieldSetting);
                    primaryKey = new IssueId(Integer.parseInt(value));
                    break;

                case CUSTOM_FIELD:

                    // 複数選択の場合は文字列のリスト、それ以外は文字列
                    Object customFiledValue;
                    if (StringUtils.isNotEmpty(fieldSetting.getMultipleItemSeparator())) {

                        customFiledValue = Stream.of(StringUtils.split(value, fieldSetting.getMultipleItemSeparator()))
                                .map(v -> convertValue(v, fieldSetting))
                                .collect(Collectors.toList());
                    } else {

                        customFiledValue = convertValue(value, fieldSetting);
                    }

                    CustomField customField = new CustomField(fieldSetting.getCustomFieldId(), customFiledValue);

                    if (fieldSetting.isPrimaryKey()) {
                        primaryKey = customField;
                    } else {
                        targetFieldsBuilder.customField(customField);
                    }

                    break;

                case WATCHER_USER_IDS:

                    // ウォッチャーはリスト

                    List<Integer> watcherUserIds;

                    if (StringUtils.isEmpty(value)) {

                        watcherUserIds = Collections.emptyList();

                    } else if (StringUtils.isNotEmpty(fieldSetting.getMultipleItemSeparator())) {

                        watcherUserIds = Stream.of(StringUtils.split(value, fieldSetting.getMultipleItemSeparator()))
                                .map(v -> convertValue(v, fieldSetting))
                                .map(Integer::valueOf)
                                .collect(Collectors.toList());

                    } else {

                        // 区切り文字が無い場合、1ユーザとして登録
                        watcherUserIds = Arrays.asList(Integer.valueOf(convertValue(value, fieldSetting)));
                    }

                    targetFieldsBuilder.field(fieldType, watcherUserIds);

                    break;

                default:
                    // その他の項目は更新対象フィールドとして利用
                    value = convertValue(value, fieldSetting);
                    targetFieldsBuilder.field(fieldType, value);
                    break;
            }
        }

        return new IssueRecord(primaryKey, targetFieldsBuilder.build());
    }

    private String convertValue(String value, FieldSetting fieldSetting) {

        if (value.isEmpty()) {
            return value;
        }

        if (config.getReplaceString() != null) {
            // 置換文字が設定されている場合、置換後の文字を使う
            value = config.getReplaceString().replace(value);
        }

        if (fieldSetting.getMappings() == null) {
            // 変換表が無い場合、正規化だけ行う
            return fieldSetting.getType().normalize(value);
        }

        // 変換表がある場合、CSVから取り出した値を変換
        String convertedValue = fieldSetting.getMappings().get(value);

        if (convertedValue == null) {
            // 一致するものが無い場合エラー
            throw new IllegalArgumentException(
                    String.format(
                            "Could not mapping \"%s\" of field [%s].",
                            value,
                            fieldSetting.getHeaderName()));
        }

        return convertedValue;
    }
}
