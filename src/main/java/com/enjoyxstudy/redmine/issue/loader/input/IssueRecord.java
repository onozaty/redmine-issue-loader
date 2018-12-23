package com.enjoyxstudy.redmine.issue.loader.input;

import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class IssueRecord {

    private PrimaryKey primaryKey;

    private Map<String, Object> fields;
}
