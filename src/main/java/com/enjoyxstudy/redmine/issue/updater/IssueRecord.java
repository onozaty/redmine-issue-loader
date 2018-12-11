package com.enjoyxstudy.redmine.issue.updater;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class IssueRecord {

    private PrimaryKey primaryKey;

    private Issue issue;
}
