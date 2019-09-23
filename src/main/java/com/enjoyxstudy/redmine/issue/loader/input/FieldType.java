package com.enjoyxstudy.redmine.issue.loader.input;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum FieldType {

    ISSUE_ID("id"),

    PROJECT_ID("project_id"),

    TRACKER_ID("tracker_id"),

    STATUS_ID("status_id"),

    PRIORITY_ID("priority_id"),

    SUBJECT("subject"),

    DESCRIPTION("description"),

    CATEGORY_ID("category_id"),

    PARENT_ISSUE_ID("parent_issue_id"),

    CUSTOM_FIELD("custom_field"),

    ASSIGNED_TO_ID("assigned_to_id"),

    FIXED_VERSION_ID("fixed_version_id"),

    START_DATE("start_date"),

    DUE_DATE("due_date"),

    DONE_RATIO("done_ratio"),

    ESTIMATED_HOURS("estimated_hours"),

    IS_PRIVATE("is_private");

    private final String fieldName;
}
