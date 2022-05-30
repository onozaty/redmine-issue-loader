package com.github.onozaty.redmine.issue.loader.input;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;

import lombok.Getter;

public enum FieldType {

    ISSUE_ID("id"),

    PROJECT_ID("project_id"),

    TRACKER_ID("tracker_id"),

    STATUS_ID("status_id"),

    PRIORITY_ID("priority_id"),

    ASSIGNED_TO_ID("assigned_to_id"),

    CATEGORY_ID("category_id"),

    FIXED_VERSION_ID("fixed_version_id"),

    PARENT_ISSUE_ID("parent_issue_id"),

    SUBJECT("subject"),

    DESCRIPTION("description"),

    START_DATE("start_date", FieldType::normalizeDate),

    DUE_DATE("due_date", FieldType::normalizeDate),

    DONE_RATIO("done_ratio"),

    IS_PRIVATE("is_private", StringUtils::lowerCase),

    ESTIMATED_HOURS("estimated_hours"),

    CUSTOM_FIELD("custom_field"),

    WATCHER_USER_IDS("watcher_user_ids");

    @Getter
    private final String fieldName;

    private final Function<String, String> normalize;

    private FieldType(String fieldName, Function<String, String> normalize) {
        this.fieldName = fieldName;
        this.normalize = normalize;
    }

    private FieldType(String fieldName) {
        this.fieldName = fieldName;
        this.normalize = x -> x;
    }

    public String normalize(String value) {
        return normalize.apply(value);
    }

    private static final List<DateTimeFormatter> BEFORE_NORMALIZE_DATE_FORMATTERS = Collections.unmodifiableList(
            Arrays.asList(
                    DateTimeFormatter.ofPattern("yyyy/M/d"),
                    DateTimeFormatter.ofPattern("yyyy-M-d")));

    private static final DateTimeFormatter NORMALIZED_DATE_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd");

    private static String normalizeDate(String value) {

        if (StringUtils.isEmpty(value)) {
            return value;
        }

        LocalDate date = null;
        for (DateTimeFormatter dateFormatter : BEFORE_NORMALIZE_DATE_FORMATTERS) {
            try {
                date = LocalDate.parse(value, dateFormatter);
                // 変換できたら抜ける
                break;
            } catch (DateTimeParseException e) {
                // 変換できなかったら次へ
            }
        }

        if (date == null) {
            // 全てのフォーマットに一致しなかった場合
            throw new IllegalArgumentException(String.format("%s is invalid date format.", value));
        }

        return NORMALIZED_DATE_FORMATTER.format(date);
    }
}
