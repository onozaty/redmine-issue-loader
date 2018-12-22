package com.enjoyxstudy.redmine.issue.updater;

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

    CUSTOM_FIELD("custom_field");

    private final String fieldName;
}
