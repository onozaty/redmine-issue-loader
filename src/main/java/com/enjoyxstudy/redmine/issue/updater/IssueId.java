package com.enjoyxstudy.redmine.issue.updater;

import com.enjoyxstudy.redmine.issue.updater.client.QueryParameter;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class IssueId implements PrimaryKey {

    private int id;

    @Override
    public QueryParameter getQueryParameter() {
        return new QueryParameter("issue_id", String.valueOf(id));
    }
}
